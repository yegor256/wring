/**
 * Copyright (c) 2016, wring.io
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
import io.wring.model.Events;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;

/**
 * One execution.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 0.13
 */
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
     * Ctor.
     * @param agt Agent
     * @param evt Events
     */
    Exec(final Agent agt, final Events evt) {
        this.agent = agt;
        this.events = evt;
    }

    /**
     * Run it.
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void run() throws IOException {
        String title;
        String body;
        try {
            title = this.agent.toString();
            body = this.body();
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            Logger.warn(
                this, "%s: %s", ex.getClass().getCanonicalName(),
                StringUtils.abbreviate(ex.getLocalizedMessage(), Tv.FIFTY)
            );
            title = String.format(
                "internal error (%s): %s",
                ex.getClass().getCanonicalName(),
                StringEscapeUtils.escapeHtml4(
                    StringUtils.abbreviate(ex.getLocalizedMessage(), Tv.FIFTY)
                )
            );
            body = String.format(
                // @checkstyle LineLength (1 line)
                "%tFT%<tRZ %s\n\nIf you see this message, please report it to https://github.com/yegor256/wring/issues",
                new Date(),
                StringEscapeUtils.escapeHtml4(ExceptionUtils.getStackTrace(ex))
            );
        }
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
                "%s\ndone. %tFT%<tRZ. %[ms]s spent.",
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
            new PatternLayout("%p %m\n"),
            baos
        );
        root.addAppender(appender);
        this.agent.push(this.events);
        root.removeAppender(appender);
        return baos.toString();
    }

}
