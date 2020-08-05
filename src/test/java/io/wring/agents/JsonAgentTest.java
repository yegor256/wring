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
package io.wring.agents;

import io.wring.fake.FkBase;
import io.wring.model.Events;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link JsonAgent}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class JsonAgentTest {

    /**
     * JsonAgent can make an agent.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesAgent() throws Exception {
        final Agent agent = new JsonAgent(
            new FkBase(),
            "{\"class\":\"io.wring.agents.FkAgent\"}"
        );
        final Events events = Mockito.mock(Events.class);
        agent.push(events);
        Mockito.verify(events).post(Mockito.anyString(), Mockito.anyString());
    }

    /**
     * JsonAgent can parse complex JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesComplexJson() throws Exception {
        MatcherAssert.assertThat(
            new JsonAgent(
                new FkBase(),
                "{\"class\":\"io.wring.agents.FkAgent\",\"x\":\"\\\\E\"}"
            ).name(),
            Matchers.equalTo(FkAgent.class.getCanonicalName())
        );
    }

    /**
     * JsonAgent can parse complex JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesJsonWithBackslashes() throws Exception {
        MatcherAssert.assertThat(
            new JsonAgent(
                new FkBase(),
                String.join(
                    "",
                    "{\"class\":\"io.wring.agents.FkAgent\",",
                    "\"x\":\"/@[a-z0-9\\\\-]\\\\s+\"}"
                )
            ).name(),
            Matchers.equalTo(FkAgent.class.getCanonicalName())
        );
    }

}
