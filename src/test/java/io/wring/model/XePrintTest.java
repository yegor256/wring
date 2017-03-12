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
package io.wring.model;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link XePrint}.
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.15
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class XePrintTest {

    /**
     * Prints by XPath.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsByXpath() throws Exception {
        MatcherAssert.assertThat(
            new XePrint(
                new Directives().add("hello").add("world").set("you, dude")
            ).text("{/hello/world/text()}"),
            Matchers.containsString("dude")
        );
    }

    /**
     * Prints by XPath.
     * @throws Exception If some problem inside
     */
    @Test
    public void printsByMultiXpath() throws Exception {
        MatcherAssert.assertThat(
            new XePrint(
                new Directives().add("foo")
                    .add("bar")
                    .set("hello, bar 1")
                    .up()
                    .add("bar")
                    .set("hello, bar 2")
            ).text("{/foo/bar/text()}"),
            Matchers.containsString("hello, bar")
        );
    }

    /**
     * Prints by missed XPath.
     * @throws Exception If some problem inside
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void printsByMissedXpath() throws Exception {
        new XePrint(
            new Directives().add("oops")
        ).text("{/event/title/text()}");
    }

}
