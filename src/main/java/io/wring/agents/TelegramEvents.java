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
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import io.wring.model.Event;
import io.wring.model.Events;
import java.io.IOException;
import javax.json.JsonObject;

/**
 * Events being duplicated into Telegram.
 *
 * @since 0.20
 */
final class TelegramEvents implements Events {

    /**
     * Origin.
     */
    private final transient Events origin;

    /**
     * Telegram.
     */
    private final transient TelegramBot telegram;

    /**
     * Tg chat ID.
     */
    private final transient String chat;

    /**
     * Ctor.
     * @param events Agent original
     * @param bot The bot
     * @param cfg JSON config
     */
    TelegramEvents(final Events events, final TelegramBot bot,
        final JsonObject cfg) {
        this(events, bot, cfg.getString("telegram", ""));
    }

    /**
     * Ctor.
     * @param events Agent original
     * @param bot The bot
     * @param cht Chat ID
     */
    TelegramEvents(final Events events, final TelegramBot bot,
        final String cht) {
        this.origin = events;
        this.telegram = bot;
        this.chat = cht;
    }

    @Override
    public Iterable<Event> iterate() throws IOException {
        return this.origin.iterate();
    }

    @Override
    public void post(final String title, final String text) throws IOException {
        if (!this.chat.isEmpty()) {
            this.telegram.execute(
                new SendMessage(
                    this.chat,
                    String.format("%s\n\n%s", title, text)
                ).disableWebPagePreview(true).parseMode(ParseMode.Markdown)
            );
        }
        this.origin.post(title, text);
    }

    @Override
    public Event event(final String title) throws IOException {
        return this.origin.event(title);
    }

}
