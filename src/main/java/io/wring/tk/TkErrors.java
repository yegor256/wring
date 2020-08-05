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
package io.wring.tk;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Tv;
import io.wring.model.Base;
import io.wring.model.Error;
import io.wring.model.XePrint;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.misc.Href;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * List of errors.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkErrors implements Take {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Ctor.
     * @param bse Base
     */
    TkErrors(final Base bse) {
        this.base = bse;
    }

    @Override
    public Response act(final Request req) throws IOException {
        final Iterable<Error> errors = this.base.user(new RqUser(req).urn())
            .errors()
            .iterate();
        return new RsPage(
            "/xsl/errors.xsl",
            req,
            new XeAppend(
                "errors",
                new XeChain(
                    new XeTransform<>(
                        Iterables.limit(errors, Tv.TWENTY),
                        TkErrors::source
                    )
                ),
                new XeDirectives(
                    new Directives().attr("total", Iterables.size(errors))
                )
            )
        );
    }

    /**
     * Convert error to Xembly.
     * @param error The error
     * @return Xembly
     * @throws IOException If fails
     */
    private static XeSource source(final Error error) throws IOException {
        final Iterable<Directive> dirs = error.asXembly();
        final String title = new XePrint(dirs).text("{/error/title/text()}");
        final String time =
            new XePrint(dirs).text("{/error/time/text()}");
        return new XeDirectives(
            new Directives()
                .append(dirs)
                .append(
                    new XeLink(
                        "delete",
                        new Href("/error-delete")
                            .with("title", title)
                            .with("time", time)
                    ).toXembly()
                )
        );
    }
}
