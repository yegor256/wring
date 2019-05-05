/**
 * Copyright (c) 2016-2019, Wring.io
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
import io.wring.model.Event;
import io.wring.model.Events;
import javax.json.Json;
import org.cactoos.io.InputStreamOf;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link BoostEvents}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.13
 */
public final class BoostEventsTest {

    /**
     * BoostEvents can boost events.
     * @throws Exception If some problem inside
     */
    @Test
    public void boostsEvents() throws Exception {
        final Events events = Mockito.mock(Events.class);
        final Event event = Mockito.mock(Event.class);
        Mockito.doReturn(event).when(events).event(Mockito.anyString());
        new BoostEvents(events, "alpha.*").post("x", "an\nalpha one\nhere");
        Mockito.verify(event).vote(Tv.FIVE);
    }

    /**
     * BoostEvents can ignore events.
     * @throws Exception If some problem inside
     */
    @Test
    public void ignoresEventsThrough() throws Exception {
        final Events events = Mockito.mock(Events.class);
        new BoostEvents(events, "/beta.*/").post("y", "there is no text here");
    }

    /**
     * BoostEvents can boost events by title.
     * @throws Exception If some problem inside
     */
    @Test
    public void boostsEventsByTitle() throws Exception {
        final Events events = Mockito.mock(Events.class);
        final Event event = Mockito.mock(Event.class);
        Mockito.doReturn(event).when(events).event(Mockito.anyString());
        new BoostEvents(events, "xyz.*").post("xyz", "some body");
        Mockito.verify(event).vote(Tv.FIVE);
    }

    /**
     * IgnoreEvents can filter out events.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesEventsThroughByJsonConfig() throws Exception {
        final Events events = Mockito.mock(Events.class);
        final Event event = Mockito.mock(Event.class);
        Mockito.doReturn(event).when(events).event(Mockito.anyString());
        new BoostEvents(
            events,
            Json.createReader(
                new InputStreamOf("{}")
            ).readObject()
        ).post("the title", "the body");
        Mockito.verify(event, Mockito.never()).vote(Mockito.anyInt());
    }

}
