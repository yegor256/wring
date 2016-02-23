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
package io.wring.dynamo;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import com.jcabi.matchers.XhtmlMatchers;
import io.wring.model.Pipe;
import io.wring.model.Pipes;
import io.wring.model.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Xembler;

/**
 * Integration case for {@link DyPipes}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DyPipesITCase {

    /**
     * DyPipes can add and remove pipes.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsAndRemovePipes() throws Exception {
        final User user = new DyUser(new Dynamo(), "jeffrey");
        final Pipes pipes = user.pipes();
        final Pipe pipe = pipes.add("name: hello");
        MatcherAssert.assertThat(
            new Xembler(pipe.asXembly()).xml(),
            XhtmlMatchers.hasXPaths(
                "/pipe/json",
                "/pipe/id"
            )
        );
        pipe.delete();
    }

    /**
     * DyPipes can add many pipes.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsManyPipes() throws Exception {
        final User user = new DyUser(new Dynamo(), "sarah");
        final Pipes pipes = user.pipes();
        for (int idx = 0; idx < Tv.FIVE; ++idx) {
            pipes.add("oops");
        }
        MatcherAssert.assertThat(
            Iterables.size(pipes.iterate()),
            Matchers.equalTo(Tv.FIVE)
        );
    }

}
