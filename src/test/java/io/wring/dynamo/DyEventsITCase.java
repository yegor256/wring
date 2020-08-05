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
package io.wring.dynamo;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import io.wring.model.Event;
import io.wring.model.Events;
import io.wring.model.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Integration case for {@link DyEvents}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DyEventsITCase {

    /**
     * DyEvents can add many events.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsManyEvents() throws Exception {
        final User user = new DyUser(new Dynamo(), "william");
        final Events events = user.events();
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            events.post(String.format("event #%d", idx), "some text");
        }
        MatcherAssert.assertThat(
            Iterables.size(events.iterate()),
            Matchers.equalTo(Tv.FIVE)
        );
    }

    /**
     * DyEvents can delete events.
     * @throws Exception If some problem inside
     */
    @Test
    public void deletesEvent() throws Exception {
        final User user = new DyUser(new Dynamo(), "boris");
        final Events events = user.events();
        events.post("subj", "body");
        final Event event = events.iterate().iterator().next();
        event.delete();
        MatcherAssert.assertThat(
            Iterables.size(events.iterate()),
            Matchers.equalTo(0)
        );
    }

    /**
     * DyEvents can append text to.
     * @throws Exception If some problem inside
     */
    @Test
    public void appendsToExistingEvents() throws Exception {
        final User user = new DyUser(new Dynamo(), "peter");
        final Events events = user.events();
        final String title = "a simple title";
        events.post(title, "\n\tfirst body");
        events.post(title, "\n\u0000\u00fdin between");
        events.post(title, "second body\n\n");
        MatcherAssert.assertThat(
            new Xembler(events.iterate().iterator().next().asXembly()).xml(),
            XhtmlMatchers.hasXPaths(
                "/event/text[contains(.,'first')]",
                "/event/text[contains(.,'second body')]",
                "/event/text[not(contains(.,'first body\n'))]"
            )
        );
    }

    /**
     * DyEvents can post and vote.
     * @throws Exception If some problem inside
     */
    @Test
    public void postsAndVotes() throws Exception {
        final User user = new DyUser(new Dynamo(), "erikk");
        final Events events = user.events();
        final String title = "the title of the Event --+";
        events.post(title, "some body text of the event");
        events.event(title).vote(1);
        MatcherAssert.assertThat(
            new Xembler(events.event(title).asXembly()).xml(),
            XhtmlMatchers.hasXPaths("/event[rank=2]")
        );
    }

}
