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
import io.wring.model.Event;
import io.wring.model.Events;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * Events that boost by regular expression.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.13
 */
final class BoostEvents implements Events {

    /**
     * Origin.
     */
    private final transient Events origin;

    /**
     * Regex to boost.
     */
    private final transient Pattern regex;

    /**
     * Ctor.
     * @param events Agent original
     * @param cfg JSON config
     */
    BoostEvents(final Events events, final JsonObject cfg) {
        this(events, BoostEvents.pattern(cfg));
    }

    /**
     * Ctor.
     * @param events Agent original
     * @param ptn Pattern
     */
    BoostEvents(final Events events, final String ptn) {
        this(events, Pattern.compile(ptn));
    }

    /**
     * Ctor.
     * @param events Agent original
     * @param ptn Pattern
     */
    BoostEvents(final Events events, final Pattern ptn) {
        this.origin = events;
        this.regex = ptn;
    }

    @Override
    public Iterable<Event> iterate() throws IOException {
        return this.origin.iterate();
    }

    @Override
    public void post(final String title, final String text) throws IOException {
        this.origin.post(title, text);
        if (this.regex.matcher(text).find()
            || this.regex.matcher(title).find()) {
            this.origin.event(title).vote(Tv.FIVE);
            Logger.info(
                this, "boosting \"%s\" because of %s",
                new Printable(text),
                this.regex
            );
        }
    }

    @Override
    public Event event(final String title) throws IOException {
        return this.origin.event(title);
    }

    /**
     * Make regex from a JSON config.
     * @param json JSON config
     * @return Pattern
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Pattern pattern(final JsonObject json) {
        final JsonArray ignore = json.getJsonArray("boost");
        final Collection<Pattern> ptns = new LinkedList<>();
        if (ignore != null) {
            ptns.addAll(
                ignore.getValuesAs(JsonString.class)
                    .stream()
                    .map(JsonString::getString)
                    .map(Pattern::compile)
                    .collect(Collectors.toList())
            );
        }
        return Pattern.compile(
            String.format(
                "(%s)",
                ptns.stream()
                    .map(Pattern::toString)
                    .collect(Collectors.joining(")|("))
            )
        );
    }

}

