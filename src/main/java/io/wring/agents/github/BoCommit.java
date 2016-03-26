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
import com.jcabi.github.RepoCommit;
import com.jcabi.github.RtPagination;
import com.jcabi.log.Logger;
import io.wring.agents.Printable;
import io.wring.model.Base;
import io.wring.model.Events;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Body of commit.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 0.13
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class BoCommit implements Body {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Issue.
     */
    private final transient RepoCommit.Smart commit;

    /**
     * Ctor.
     * @param bse Base
     * @param subj Issue
     */
    BoCommit(final Base bse, final RepoCommit.Smart subj) {
        this.base = bse;
        this.commit = subj;
    }

    @Override
    public void push(final Events events) throws IOException {
        final Coordinates coords = this.commit.repo().coordinates();
        final String body = this.text(coords);
        if (body.isEmpty()) {
            Logger.info(
                this, "%s %s ignored",
                coords, this.commit.sha()
            );
        } else {
            events.post(
                String.format(
                    "[%s] %s",
                    coords, this.commit.sha()
                ),
                body
            );
            Logger.info(
                this, "new event in %s#%s",
                coords, this.commit.sha()
            );
        }
    }

    /**
     * Collect all important texts from the issue.
     * @param coords Coords
     * @return Body text
     * @throws IOException If fails
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public String text(final Coordinates coords) throws IOException {
        final Iterator<JsonObject> comments = new RtPagination<>(
            this.commit.repo().github().entry()
                .uri()
                .path("repos")
                .path(coords.user())
                .path(coords.repo())
                .path("commits")
                .path(this.commit.sha())
                .path("comments")
                .back(),
            object -> object
        ).iterator();
        final String self = this.commit.repo().github().users().self().login();
        int seen = this.seen();
        Logger.info(
            this, "last seen comment in %s is #%d",
            this.commit.sha(), seen
        );
        final Pattern ptn = Pattern.compile(
            String.format(
                ".*(?<![a-zA -Z0-9-])%s(?![a-zA-Z0-9-]).*",
                Pattern.quote(String.format("@%s", self))
            ),
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE
        );
        final StringBuilder body = new StringBuilder();
        while (comments.hasNext()) {
            final JsonObject comment = comments.next();
            final int number = comment.getInt("id");
            if (number <= seen) {
                continue;
            }
            final String author = comment.getJsonObject("user")
                .getString("login");
            if (author.equals(self)) {
                Logger.info(
                    this,
                    "%s/%d ignored since you're the author",
                    this.commit.sha(), number
                );
                continue;
            }
            final String cmt = comment.getString("body");
            if (ptn.matcher(cmt).matches()) {
                body.append('@')
                    .append(author)
                    .append(" at [")
                    .append(comment.getString("updated_at"))
                    .append("](")
                    .append(comment.getString("html_url"))
                    .append("): ")
                    .append(StringEscapeUtils.escapeHtml4(cmt))
                    .append("\n\n");
                Logger.info(
                    this,
                    "%s/%d accepted: %s",
                    this.commit.sha(), number, new Printable(cmt)
                );
            } else {
                Logger.info(
                    this,
                    "%s/%d ignored: %s",
                    this.commit.sha(), number, new Printable(cmt)
                );
            }
            seen = number;
        }
        this.base.vault().save(
            this.key(),
            Optional.of(Integer.toString(seen))
        );
        Logger.info(
            this, "seen comment set to %d for %s",
            seen, this.commit.sha()
        );
        return body.toString();
    }

    /**
     * Get the latest seen comment number in the issue.
     * @return Comment number of zero
     * @throws IOException If fails
     */
    private int seen() throws IOException {
        final Optional<String> before = this.base.vault().value(this.key());
        final int seen;
        if (before.isPresent()) {
            seen = Integer.parseInt(before.get());
        } else {
            seen = 0;
        }
        return seen;
    }

    /**
     * Make key for this issue.
     * @return The key
     */
    private String key() {
        return String.format(
            "%s#%s", this.commit.repo().coordinates(), this.commit.sha()
        );
    }

}
