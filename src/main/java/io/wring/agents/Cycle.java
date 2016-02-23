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
import io.wring.model.Base;
import io.wring.model.Events;
import io.wring.model.Pipe;
import io.wring.model.XePrint;
import java.io.IOException;
import java.util.Queue;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void process(final Pipe pipe) throws IOException {
        Cycle.process(
            new JsonAgent(
                this.base,
                new XePrint(pipe.asXembly()).text("{/pipe/json/text()}")
            ),
            this.base.user(
                new XePrint(pipe.asXembly()).text("{/pipe/urn/text()}")
            ).events()
        );
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
        final long start = System.currentTimeMillis();
        try {
            agent.push(events);
            events.post(
                agent.toString(),
                Logger.format(
                    "all good, %[ms]s",
                    System.currentTimeMillis() - start
                )
            );
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Throwable ex) {
            events.post(
                agent.toString(),
                ExceptionUtils.getStackTrace(ex)
            );
        }
    }

}
