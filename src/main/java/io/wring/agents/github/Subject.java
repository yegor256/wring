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
package io.wring.agents.github;

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.RepoCommit;
import io.wring.model.Base;
import io.wring.model.Events;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Subject of event.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 0.13
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class Subject {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Github repo coordinates.
     */
    private final transient Coordinates coords;

    /**
     * JSON.
     */
    private final transient JsonObject json;

    /**
     * Ctor.
     * @param bse Base
     * @param crds Coordinates
     * @param obj JSON object
     */
    Subject(final Base bse, final Coordinates crds, final JsonObject obj) {
        this.base = bse;
        this.coords = crds;
        this.json = obj;
    }

    /**
     * Post an event.
     * @param github Github client
     * @param events Events
     * @throws IOException If fails
     */
    public void push(final Github github, final Events events)
        throws IOException {
        final String type = this.json.getString("type");
        final Body body;
        if ("Issue".equals(type) || "PullRequest".equals(type)) {
            body = new BoIssue(
                this.base,
                new Issue.Smart(
                    github.repos().get(this.coords).issues().get(
                        Integer.parseInt(
                            StringUtils.substringAfterLast(
                                this.json.getString("url"),
                                "/"
                            )
                        )
                    )
                )
            );
        } else if ("Commit".equals(type)) {
            body = new BoCommit(
                this.base,
                new RepoCommit.Smart(
                    github.repos().get(this.coords).commits().get(
                        StringUtils.substringAfterLast(
                            this.json.getString("url"),
                            "/"
                        )
                    )
                )
            );
        } else {
            try (final ByteArrayOutputStream baos =
                new ByteArrayOutputStream()) {
                Json.createWriter(baos).write(this.json);
                throw new IOException(
                    String.format(
                        "subject ignored: %s", baos.toString()
                    )
                );
            }
        }
        body.push(events);
    }

}
