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

import io.wring.model.Base;
import io.wring.model.Events;
import io.wring.model.Pipe;
import io.wring.model.XePrint;
import java.io.IOException;
import java.util.Queue;
import javax.json.Json;
import javax.json.JsonObject;
import org.cactoos.func.FuncWithCallback;
import org.cactoos.func.ProcAsFunc;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.BytesAsInput;
import org.cactoos.text.BytesAsText;
import org.cactoos.text.TextAsBytes;
import org.cactoos.text.ThrowableAsBytes;

/**
 * Single cycle.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
     * @checkstyle IllegalCatchCheck (20 lines)
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void process(final Pipe pipe) throws IOException {
        final XePrint print = new XePrint(pipe.asXembly());
        final Events events = this.base.user(
            print.text("{/pipe/urn/text()}")
        ).events();
        final String json = print.text("{/pipe/json/text()}");
        new UncheckedFunc<>(
            new FuncWithCallback<String, JsonObject>(
                str -> Json.createReader(
                    new BytesAsInput(new TextAsBytes(str)).stream()
                ).readObject(),
                new ProcAsFunc<>(
                    error -> events.post(
                        Cycle.class.getCanonicalName(),
                        String.format(
                            "Failed to parse JSON:\n%s\n\n%s",
                            json,
                            new BytesAsText(
                                new ThrowableAsBytes(error)
                            ).asString()
                        )
                    )
                ),
                new ProcAsFunc<>(
                    obj -> new Exec(
                        new JsonAgent(this.base, obj),
                        new BoostEvents(
                            new IgnoreEvents(events, obj),
                            obj
                        )
                    ).run()
                )
            )
        ).apply(json);
    }

}
