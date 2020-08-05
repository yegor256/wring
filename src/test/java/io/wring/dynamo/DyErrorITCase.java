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
package io.wring.dynamo;

import com.jcabi.matchers.XhtmlMatchers;
import io.wring.model.Error;
import io.wring.model.Errors;
import io.wring.model.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsEmptyIterable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xembly.Xembler;

/**
 * IT cases for {@link DyError}.
 *
 * @since 1.0
 */
@Disabled
public final class DyErrorITCase {

    /**
     * DyError can  generate Xembly.
     * @throws Exception If some problem inside
     */
    @Test
    public void asXembly() throws Exception {
        final User user = new DyUser(new Dynamo(), "nick");
        final Errors errors = user.errors();
        errors.register("Fresh error", "description and message");
        MatcherAssert.assertThat(
            new Xembler(errors.iterate().iterator().next().asXembly()).xml(),
            XhtmlMatchers.hasXPath(
                "/error[title='Fresh error']"
            )
        );
    }

    /**
     * DyErrors can delete errors.
     * @throws Exception If some problem inside
     */
    @Test
    public void deletesErrors() throws Exception {
        final User user = new DyUser(new Dynamo(), "boris");
        final Errors errors = user.errors();
        errors.register("error", "message");
        final Error error = errors.iterate().iterator().next();
        error.delete();
        MatcherAssert.assertThat(
            errors.iterate(),
            new IsEmptyIterable<>()
        );
    }
}
