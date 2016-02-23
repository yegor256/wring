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

import com.jcabi.aspects.Tv;
import com.jcabi.github.Bulk;
import com.jcabi.github.Comment;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.github.RtGithub;
import com.jcabi.github.RtPagination;
import com.jcabi.github.Smarts;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import io.wring.agents.Agent;
import io.wring.model.Base;
import io.wring.model.Events;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * GitHub agent.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 1.0
 */
public final class AgGithub implements Agent {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Config.
     */
    private final transient JsonObject config;

    /**
     * Ctor.
     * @param bse Base
     * @param json JSON config
     */
    public AgGithub(final Base bse, final JsonObject json) {
        this.base = bse;
        this.config = json;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public void push(final Events events) throws IOException {
        final Github github = new RtGithub(this.config.getString("token"));
        final String since = DateFormatUtils.formatUTC(
            DateUtils.addMinutes(new Date(), -Tv.THREE),
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        );
        final Request req = github.entry()
            .uri().path("/notifications").back();
        final Iterable<JsonObject> list = new RtPagination<>(
            req.uri().queryParam("participating", "true")
                .queryParam("since", since)
                .queryParam("all", Boolean.toString(true))
                .back(),
            RtPagination.COPYING
        );
        for (final JsonObject event : list) {
            final String reason = event.getString("reason");
            if (!"mention".equals(reason)) {
                continue;
            }
            this.push(github, event, events);
        }
        req.uri()
            .queryParam("last_read_at", since).back()
            .method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
    }

    /**
     * Post an event.
     * @param github Github client
     * @param json JSON object
     * @param events Events
     * @throws IOException If fails
     */
    private void push(final Github github, final JsonObject json,
        final Events events)
        throws IOException {
        final Coordinates coords = new Coordinates.Simple(
            json.getJsonObject("repository").getString("full_name")
        );
        final Issue.Smart issue = new Issue.Smart(
            github.repos().get(coords).issues().get(
                Integer.parseInt(
                    StringUtils.substringAfterLast(
                        json.getJsonObject("subject").getString("url"),
                        "/"
                    )
                )
            )
        );
        final String body = this.body(issue);
        if (body.isEmpty()) {
            Logger.info(this, "%s#%d ignored", coords, issue.number());
        } else {
            events.post(
                String.format(
                    "[%s#%d] %s",
                    coords, issue.number(), issue.title()
                ),
                String.format(
                    "[issue #%d](%s)\n%s",
                    issue.number(), issue.htmlUrl(), body
                )
            );
            Logger.info(this, "event in %s#%d", coords, issue.number());
        }
    }

    /**
     * Collect all important texts from the issue.
     * @param issue The issue
     * @return Body text
     * @throws IOException If fails
     */
    private String body(final Issue.Smart issue) throws IOException {
        final Iterator<Comment.Smart> comments = new Smarts<Comment.Smart>(
            new Bulk<>(issue.comments().iterate())
        ).iterator();
        final Pattern ptn = Pattern.compile(
            String.format(
                "(?<![a-zA-Z0-9-])@%s(?![a-zA-Z0-9-])",
                Pattern.quote(issue.repo().github().users().self().login())
            ),
            Pattern.CASE_INSENSITIVE
        );
        int seen = this.seen(issue);
        final StringBuilder body = new StringBuilder();
        while (comments.hasNext()) {
            final Comment.Smart comment = comments.next();
            if (comment.number() <= seen) {
                continue;
            }
            final String cmt = comment.body();
            if (ptn.matcher(cmt).matches()) {
                body.append(cmt);
            } else {
                Logger.info(
                    this,
                    "comment #%d in %s#%d ignored: %s",
                    comment.number(), issue.repo().coordinates(),
                    issue.number(),
                    StringUtils.abbreviate(cmt, Tv.FIFTY).replaceAll(
                        "[^a-zA-Z0-9-.@#%$]", " "
                    )
                );
            }
            seen = comment.number();
        }
        this.base.vault().save(
            AgGithub.key(issue),
            Optional.of(Integer.toString(seen))
        );
        return body.toString();
    }

    /**
     * Get the latest seen comment number in the issue.
     * @param issue The issue
     * @return Comment number of zero
     * @throws IOException If fails
     */
    private int seen(final Issue.Smart issue) throws IOException {
        final Optional<String> before = this.base.vault().value(
            AgGithub.key(issue)
        );
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
     * @param issue The issue
     * @return The key
     */
    private static String key(final Issue.Smart issue) {
        return String.format(
            "%s#%d", issue.repo().coordinates(), issue.number()
        );
    }

}
