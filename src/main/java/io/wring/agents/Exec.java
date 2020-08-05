/**
 * Copyright (c) 2016-2020, Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the wring.io nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.wring.agents;

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import io.sentry.Sentry;
import io.wring.model.Errors;
import io.wring.model.Events;
import io.wring.model.Pipe;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.scalar.Ternary;
import org.cactoos.scalar.UncheckedScalar;

/**
 * One execution.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 0.13
 * @todo #76:30min Errors occurred during execution must be saved and later
 *  listed to user in a page. User should be able to mark errors as `read`
 *  in this page. Wire Error and Errors implementations in Exec flow so
 *  these errors are properly saved once they happen and remove
 *  Exec(final Agent agt, final Events evt, final Pipe ppe) constructor so
 *  errors is injected in Exec every time. Then remove Singularfield and
 *  UnusedPrivateField check ignores below and uncomment tests for error
 *  registering in ExecTest.registerErrors.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings({"PMD.SingularField", "PMD.UnusedPrivateField"})
final class Exec {

    /**
     * Agent.
     */
    private final transient Agent agent;

    /**
     * Events.
     */
    private final transient Events events;

    /**
     * Pipe.
     */
    private final transient Pipe pipe;

    /**
     * Errors.
     */
    private final Errors errors;

    /**
     * Ctor.
     * @param agt Agent
     * @param evt Events
     * @param ppe Pipe
     */
    Exec(final Agent agt, final Events evt, final Pipe ppe) {
        this(agt, evt, ppe, new Errors.Simple());
    }

    /**
     * Ctor.
     * @param agt Agent
     * @param evt Events
     * @param ppe Pipe
     * @param err Errors
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    Exec(final Agent agt, final Events evt, final Pipe ppe, final Errors err) {
        this.agent = agt;
        this.events = evt;
        this.pipe = ppe;
        this.errors = err;
    }

    /**
     * Run it.
     * @throws IOException If fails
     */
    public void run() throws IOException {
        final StringBuilder title = new StringBuilder(0);
        final String body = new UncheckedFunc<>(
            new FuncWithFallback<Boolean, String>(
                input -> {
                    title.append(this.agent.name());
                    return this.body();
                },
                err -> {
                    this.pipe.status(err.getClass().getCanonicalName());
                    title.setLength(0);
                    final String text;
                    if (err instanceof Agent.UserException) {
                        title.append("It's your fault");
                        text = err.getLocalizedMessage();
                    } else {
                        Sentry.capture(err);
                        final String msg = StringEscapeUtils.escapeHtml4(
                            StringUtils.abbreviate(
                                new UncheckedScalar<>(
                                    new Ternary<>(
                                        () -> err.getLocalizedMessage() == null,
                                        () -> "null",
                                        () -> err.getLocalizedMessage()
                                            .replaceAll("\\s+", " ")
                                    )
                                ).value(),
                                Tv.FIFTY
                            )
                        );
                        title.append(
                            String.format(
                                "Internal error (%s): \"%s\"",
                                err.getClass().getCanonicalName(),
                                msg
                            )
                        );
                        text = String.format(
                            // @checkstyle LineLength (1 line)
                            "%tFT%<tRZ %s\n\nIf you see this message, please report it to https://github.com/yegor256/wring/issues",
                            new Date(),
                            StringEscapeUtils.escapeHtml4(
                                ExceptionUtils.getStackTrace(err)
                            )
                        );
                    }
                    return text;
                }
            )
        )
            .apply(true);
        if (!body.isEmpty()) {
            this.events.post(
                String.format(
                    "%s by %s",
                    title, Manifests.read("Wring-Version")
                ),
                body
            );
        }
    }

    /**
     * Push, collect logs, and wrap.
     * @return Logs
     * @throws IOException If fails
     */
    private String body() throws IOException {
        final long start = System.currentTimeMillis();
        String log = this.log();
        if (!log.isEmpty()) {
            log = Logger.format(
                "%s\nDone. %tFT%<tRZ. %[ms]s spent.",
                log.trim(),
                new Date(),
                System.currentTimeMillis() - start
            );
        }
        return log;
    }

    /**
     * Push and collect logs.
     * @return Logs
     * @throws IOException If fails
     */
    private String log() throws IOException {
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Appender appender = new ThreadAppender(
            new PatternLayout("%t %p %m\n"),
            baos
        );
        root.addAppender(appender);
        try {
            this.pipe.status(this.agent.push(this.events));
            return baos.toString();
        } finally {
            root.removeAppender(appender);
        }
    }

}
