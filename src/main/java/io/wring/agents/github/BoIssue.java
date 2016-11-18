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

import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Issue;
import com.jcabi.github.Smarts;
import com.jcabi.log.Logger;
import io.wring.agents.Printable;
import io.wring.model.Base;
import io.wring.model.Events;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Body of issue.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.13
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class BoIssue implements Body {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Issue.
     */
    private final transient Issue.Smart issue;

    /**
     * Ctor.
     * @param bse Base
     * @param subj Issue
     */
    BoIssue(final Base bse, final Issue.Smart subj) {
        this.base = bse;
        this.issue = subj;
    }

    @Override
    public void push(final Events events) throws IOException {
        final String body = this.text();
        final Coordinates coords = this.issue.repo().coordinates();
        if (body.isEmpty()) {
            Logger.info(
                this, "%s#%d ignored",
                coords, this.issue.number()
            );
        } else {
            events.post(
                String.format(
                    "[%s#%d] %s",
                    coords, this.issue.number(), this.issue.title()
                ),
                body
            );
            Logger.info(
                this, "new event in %s#%d",
                coords, this.issue.number()
            );
        }
    }

    /**
     * Collect all important texts from the issue.
     * @return Body text
     * @throws IOException If fails
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public String text() throws IOException {
        final Iterator<Comment.Smart> comments = new Smarts<Comment.Smart>(
            new Bulk<>(this.issue.comments().iterate())
        ).iterator();
        final String self = this.issue.repo().github().users().self().login();
        final Pattern ptn = Pattern.compile(
            String.format(
                ".*(?<![a-zA -Z0-9-])%s(?![a-zA-Z0-9-]).*",
                Pattern.quote(String.format("@%s", self))
            ),
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE
        );
        int seen = this.seen();
        Logger.info(
            this, "last seen comment in %s#%d is #%d",
            this.issue.repo().coordinates(), this.issue.number(), seen
        );
        final StringBuilder body = new StringBuilder();
        while (comments.hasNext()) {
            final Comment.Smart comment = comments.next();
            if (comment.number() <= seen) {
                continue;
            }
            // @checkstyle MagicNumber (1 line)
            if (comment.number() < 188060467) {
                Logger.info(
                    this,
                    "%s#%d/%d ignored since too old",
                    this.issue.repo().coordinates(), this.issue.number(),
                    comment.number()
                );
                continue;
            }
            if (comment.author().login().equals(self)) {
                Logger.info(
                    this,
                    "%s#%d/%d ignored since you're the author",
                    this.issue.repo().coordinates(), this.issue.number(),
                    comment.number()
                );
                continue;
            }
            final String cmt = comment.body();
            if (ptn.matcher(cmt).matches()) {
                body.append('@')
                    .append(comment.author().login())
                    .append(" at [")
                    .append(String.format("%te-%<tb-%<tY", comment.createdAt()))
                    .append("](")
                    .append(this.issue.htmlUrl())
                    .append("#issuecomment-")
                    .append(comment.number())
                    .append("): ")
                    .append(StringEscapeUtils.escapeHtml4(cmt))
                    .append("\n\n");
                Logger.info(
                    this,
                    "%s#%d/%d accepted: %s",
                    this.issue.repo().coordinates(), this.issue.number(),
                    comment.number(), new Printable(cmt)
                );
            } else {
                Logger.info(
                    this,
                    "%s#%d/%d ignored: %s",
                    this.issue.repo().coordinates(), this.issue.number(),
                    comment.number(), new Printable(cmt)
                );
            }
            seen = comment.number();
        }
        this.base.vault().save(
            this.key(),
            Optional.of(Integer.toString(seen))
        );
        Logger.info(
            this, "seen comment set to %d for %s#%d",
            seen, this.issue.repo().coordinates(), this.issue.number()
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
            "%s#%d", this.issue.repo().coordinates(), this.issue.number()
        );
    }

}
