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

import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import io.wring.model.Base;
import io.wring.model.Events;
import io.wring.model.Pipe;
import io.wring.model.XePrint;
import java.io.IOException;
import java.util.Date;
import java.util.Queue;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.PatternLayout;

/**
 * Single cycle.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 1.0
 */
final class Cycle implements Runnable {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Pipes to process.
     */
    private final transient Queue<Pipe> pipes;

    /**
     * Ctor.
     * @param bse Base
     * @param queue List of them
     */
    Cycle(final Base bse, final Queue<Pipe> queue) {
        this.base = bse;
        this.pipes = queue;
    }

    @Override
    public void run() {
        final Pipe pipe = this.pipes.poll();
        if (pipe != null) {
            try {
                this.process(pipe);
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * Process a single pipe.
     * @param pipe The pipe
     * @throws IOException If fails
     */
    private void process(final Pipe pipe) throws IOException {
        final XePrint print = new XePrint(pipe.asXembly());
        final JsonObject json = Cycle.json(print.text("{/pipe/json/text()}"));
        Cycle.process(
            new JsonAgent(this.base, json),
            Cycle.ignoring(
                this.base.user(print.text("{/pipe/urn/text()}")).events(),
                json
            )
        );
    }

    /**
     * Get object from JSON.
     * @param json JSON as a string
     * @return JSON object
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private static JsonObject json(final String json) {
        try {
            return Json.createReader(
                IOUtils.toInputStream(json)
            ).readObject();
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            throw new IllegalStateException(
                String.format("failed to parse JSON: %s", json),
                ex
            );
        }
    }

    /**
     * Process a single pipe.
     * @param agent The agent
     * @param events User events
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private static void process(final Agent agent, final Events events)
        throws IOException {
        String title;
        String body;
        try {
            title = agent.toString();
            body = Cycle.wrap(agent, events);
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            title = Cycle.class.getCanonicalName();
            body = ExceptionUtils.getStackTrace(ex);
        }
        if (!body.isEmpty()) {
            events.post(
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
     * @param agent The agent
     * @param events User events
     * @return Logs
     * @throws IOException If fails
     */
    private static String wrap(final Agent agent, final Events events)
        throws IOException {
        final long start = System.currentTimeMillis();
        String log = Cycle.log(agent, events);
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
     * @param agent The agent
     * @param events User events
     * @return Logs
     * @throws IOException If fails
     */
    private static String log(final Agent agent, final Events events)
        throws IOException {
        final org.apache.log4j.Logger root =
            org.apache.log4j.Logger.getRootLogger();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Appender appender = new ThreadAppender(
            new PatternLayout("%p %m\n"),
            baos
        );
        root.addAppender(appender);
        agent.push(events);
        root.removeAppender(appender);
        return baos.toString();
    }

    /**
     * Make ignoring events, if necessary.
     * @param origin Original
     * @param json JSON
     * @return Events that ignore
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Events ignoring(final Events origin, final JsonObject json) {
        Events events = origin;
        final JsonArray ignore = json.getJsonArray("ignore");
        if (ignore != null) {
            for (final JsonString regex
                : ignore.getValuesAs(JsonString.class)) {
                events = new IgnoreEvents(events, regex.toString());
            }
        }
        return events;
    }

}
