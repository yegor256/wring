/*
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

import com.pengrad.telegrambot.TelegramBot;
import io.wring.model.Base;
import io.wring.model.Events;
import io.wring.model.Pipe;
import io.wring.model.XePrint;
import javax.json.Json;
import javax.json.JsonObject;
import org.cactoos.Fallback;
import org.cactoos.Proc;
import org.cactoos.bytes.BytesOf;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.UncheckedFunc;
import org.cactoos.io.ReaderOf;
import org.cactoos.text.TextOf;

/**
 * Single cycle.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class Cycle implements Proc<Pipe> {

    /**
     * Base to use.
     */
    private final transient Base base;

    /**
     * Telegram.
     */
    private final transient TelegramBot telegram;

    /**
     * Ctor.
     * @param bse Base
     */
    Cycle(final Base bse) {
        this(bse, new TelegramBot(""));
    }

    /**
     * Ctor.
     * @param bse Base
     * @param bot The telegram bot
     */
    Cycle(final Base bse, final TelegramBot bot) {
        this.base = bse;
        this.telegram = bot;
    }

    @Override
    public void exec(final Pipe pipe) throws Exception {
        final XePrint print = new XePrint(pipe.asXembly());
        final Events events = this.base.user(
            print.text("{/pipe/urn/text()}")
        ).events();
        final String json = print.text("{/pipe/json/text()}");
        new UncheckedFunc<>(
            new FuncWithFallback<String, JsonObject>(
                str -> Json.createReader(
                    new ReaderOf(str)
                ).readObject(),
                new Fallback.From<>(
                    Exception.class,
                    error -> {
                        events.post(
                            Cycle.class.getCanonicalName(),
                            String.format(
                                "Failed to parse JSON:\n%s\n\n%s",
                                json, new TextOf(new BytesOf(error)).asString()
                            )
                        );
                        return null;
                    }
                ),
                new Fallback.From<JsonObject>(
                    Exception.class,
                    obj -> {
                        if (obj != null) {
                            new Exec(
                                new JsonAgent(this.base, obj),
                                new IgnoreEvents(
                                    new TelegramEvents(
                                        new BoostEvents(events, obj),
                                        this.telegram,
                                        obj
                                    ),
                                    obj
                                ),
                                pipe
                            ).run();
                        }
                        return obj;
                    }
                )
            )
        ).apply(json);
    }

}
