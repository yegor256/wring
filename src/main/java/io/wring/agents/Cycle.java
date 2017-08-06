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
import org.cactoos.Func;
import org.cactoos.Proc;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.BytesOf;
import org.cactoos.io.ReaderOf;
import org.cactoos.text.TextOf;

/**
 * Single cycle.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Cycle implements Proc<Base> {

    /**
     * Pipes to process.
     */
    private final transient Queue<Pipe> pipes;

    /**
     * Ctor.
     * @param queue List of them
     */
    Cycle(final Queue<Pipe> queue) {
        this.pipes = queue;
    }

    @Override
    public void exec(final Base base) throws Exception {
        final Pipe pipe = this.pipes.poll();
        if (pipe != null) {
            Cycle.process(base, pipe);
        }
    }

    /**
     * Process a single pipe.
     * @param base The base
     * @param pipe The pipe
     * @throws IOException If fails
     */
    private static void process(final Base base, final Pipe pipe)
        throws IOException {
        final XePrint print = new XePrint(pipe.asXembly());
        final Events events = base.user(
            print.text("{/pipe/urn/text()}")
        ).events();
        final String json = print.text("{/pipe/json/text()}");
        new UncheckedFunc<>(
            new FuncWithFallback<>(
                (Func<String, JsonObject>) str -> Json.createReader(
                    new ReaderOf(str)
                ).readObject(),
                (Proc<Throwable>) error -> events.post(
                    Cycle.class.getCanonicalName(),
                    String.format(
                        "Failed to parse JSON:\n%s\n\n%s",
                        json, new TextOf(new BytesOf(error)).asString()
                    )
                ),
                obj -> {
                    new Exec(
                        new JsonAgent(base, obj),
                        new IgnoreEvents(new BoostEvents(events, obj), obj),
                        pipe
                    ).run();
                }
            )
        ).apply(json);
    }

}
