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
package io.wring.tk;

import com.jcabi.matchers.XhtmlMatchers;
import io.wring.fake.FkBase;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkErrors}.
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class TkErrorsTest {

    /**
     * TkErrors can render list of errors.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersListOfErrors() throws Exception {
        final Take take = new TkAppAuth(new TkErrors(new FkBase()));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqWithHeader(
                            new RqFake("GET", "/"),
                            "Accept",
                            "text/xml"
                        )
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/errors/error",
                "/page/errors/error[title='Error title']",
                "/page/errors/error[description='Error Description']",
                "/page/errors/@total"
            )
        );
    }
}
