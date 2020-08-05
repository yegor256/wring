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

import com.jcabi.log.VerboseProcess;
import com.jcabi.manifests.Manifests;
import io.wring.model.Base;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.takes.Take;
import org.takes.facets.auth.TkSecure;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkAnonymous;
import org.takes.facets.fork.FkAuthenticated;
import org.takes.facets.fork.FkFixed;
import org.takes.facets.fork.FkHitRefresh;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkFiles;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkSslOnly;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * App.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class TkApp extends TkWrap {

    /**
     * Revision of app.
     */
    private static final String REV = Manifests.read("Wring-Revision");

    /**
     * Ctor.
     * @param base Base
     * @throws IOException If fails
     */
    public TkApp(final Base base) throws IOException {
        super(TkApp.make(base));
    }

    /**
     * Ctor.
     * @param base Base
     * @return Takes
     * @throws IOException If fails
     */
    private static Take make(final Base base) throws IOException {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "Default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        return new TkSslOnly(
            new TkWithHeaders(
                new TkVersioned(
                    new TkMeasured(
                        new TkGzip(
                            new TkFlash(
                                new TkAppFallback(
                                    new TkAppAuth(
                                        new TkForward(
                                            TkApp.regex(base)
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                String.format("X-Wring-Revision: %s", TkApp.REV),
                "Vary: Cookie"
            )
        );
    }

    /**
     * Regex takes.
     * @param base Base
     * @return Takes
     * @throws IOException If fails
     */
    private static Take regex(final Base base) throws IOException {
        return new TkFork(
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/org/takes/.+\\.xsl",
                new TkClasspath()
            ),
            new FkRegex(
                "/xsl/[a-z\\-]+\\.xsl",
                new TkWithType(
                    TkApp.refresh("./src/main/xsl"),
                    "text/xsl"
                )
            ),
            new FkRegex(
                "/css/[a-z]+\\.css",
                new TkWithType(
                    TkApp.refresh("./src/main/scss"),
                    "text/css"
                )
            ),
            new FkRegex(
                "/images/[a-z]+\\.svg",
                new TkWithType(
                    TkApp.refresh("./src/main/resources"),
                    "image/svg+xml"
                )
            ),
            new FkRegex(
                "/images/[a-z]+\\.png",
                new TkWithType(
                    TkApp.refresh("./src/main/resources"),
                    "image/png"
                )
            ),
            new FkAnonymous(
                new TkFork(
                    new FkRegex("/", new TkIndex(base))
                )
            ),
            new FkAuthenticated(
                new TkSecure(
                    new TkFork(
                        new FkRegex("/", new TkEvents(base)),
                        new FkRegex("/favicon", new TkFavicon(base)),
                        new FkRegex("/pipes", new TkPipes(base)),
                        new FkRegex("/api", new TkApi()),
                        new FkRegex("/api/total.json", new TkApiTotal(base)),
                        new FkRegex("/pipe-add", new TkPipeAdd(base)),
                        new FkRegex("/pipe-delete", new TkPipeDelete(base)),
                        new FkRegex("/event-delete", new TkEventDelete(base)),
                        new FkRegex("/event-down", new TkEventDown(base)),
                        new FkRegex("/error-delete", new TkErrorDelete(base))
                    )
                )
            )
        );
    }

    /**
     * Hit refresh fork.
     * @param path Path of files
     * @return Fork
     * @throws IOException If fails
     */
    private static Take refresh(final String path) throws IOException {
        return new TkFork(
            new FkHitRefresh(
                new File(path),
                () -> new VerboseProcess(
                    new ProcessBuilder(
                        "mvn",
                        "generate-resources"
                    )
                ).stdout(),
                new TkFiles("./target/classes")
            ),
            new FkFixed(new TkClasspath())
        );
    }

}
