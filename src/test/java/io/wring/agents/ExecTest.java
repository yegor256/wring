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

import io.wring.fake.FkPipe;
import io.wring.model.Errors;
import io.wring.model.Events;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

/**
 * Test case for {@link Exec}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 0.13
 */
public final class ExecTest {

    /**
     * Exec can catch in case of an error.
     * @throws Exception If some problem inside
     */
    @Test
    public void catchesExceptions() throws Exception {
        final Agent agent = Mockito.mock(Agent.class);
        final Events events = Mockito.mock(Events.class);
        Mockito.doThrow(new IOException("<bug>")).when(agent).push(events);
        new Exec(agent, events, new FkPipe()).run();
        Mockito.verify(events).post(
            MockitoHamcrest.argThat(
                Matchers.startsWith(
                    "Internal error (java.io.IOException): \"&lt;bug&gt;\""
                )
            ),
            MockitoHamcrest.argThat(
                Matchers.containsString(
                    "java.io.IOException: &lt;bug&gt;"
                )
            )
        );
    }

    /**
     * Exec can register an error.
     * @throws Exception If some problem inside
     */
    @Test
    @Disabled
    public void registerErrors() throws Exception {
        final Agent agent = Mockito.mock(Agent.class);
        final Events events = Mockito.mock(Events.class);
        final Errors errors = new Errors.Simple();
        Mockito.doThrow(new IOException("<error>")).when(agent).push(events);
        new Exec(agent, events, new FkPipe(), errors).run();
        MatcherAssert.assertThat(
            "Could not register error",
            errors.iterate(),
            new IsIterableWithSize<>(
                new IsEqual<>(1)
            )
        );
    }

}
