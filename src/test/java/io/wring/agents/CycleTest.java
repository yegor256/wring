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
import io.wring.fake.FkPipe;
import io.wring.model.Base;
import io.wring.model.Pipe;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Cycle}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.4
 */
public final class CycleTest {

    /**
     * Cycle can process a single pipe.
     * @throws Exception If some problem inside
     */
    @Test
    public void processesSinglePipe() throws Exception {
        final Base base = new FkBase();
        final Pipe pipe = new FkPipe();
        new Cycle(base).exec(pipe);
    }

    /**
     * Cycle can process a single pipe with broken JSOn.
     * @throws Exception If some problem inside
     */
    @Test
    public void processesSinglePipeWithBrokenJson() throws Exception {
        final Base base = new FkBase();
        final Pipe pipe = new FkPipe(
            new Directives()
                .add("pipe")
                .add("urn").set("urn:test:1").up()
                .add("json").set("{\"a\":\"/@[a-z0-9\\\\-]\\\\s+\"}").up()
                .up()
        );
        new Cycle(base).exec(pipe);
    }

}
