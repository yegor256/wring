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
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import io.wring.agents.Agent;
import io.wring.model.Base;
import io.wring.model.Events;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import javax.json.JsonObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * GitHub agent.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
        final Collection<String> done = new LinkedList<>();
        for (final JsonObject event : list) {
            final String reason = event.getString("reason");
            if (!"mention".equals(reason)) {
                continue;
            }
            this.push(github, event, events);
            done.add(event.getString("id"));
        }
        req.uri()
            .queryParam("last_read_at", since).back()
            .method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
        if (!done.isEmpty()) {
            Logger.info(
                this, "%d GitHub events for @%s processed: %s",
                done.size(), github.users().self().login(), done
            );
        }
    }

    /**
     * Post an event.
     * @param github Github client
     * @param json JSON object of the notification event
     * @param events Events
     * @throws IOException If fails
     */
    private void push(final Github github, final JsonObject json,
        final Events events)
        throws IOException {
        new Subject(
            this.base,
            new Coordinates.Simple(
                json.getJsonObject("repository").getString("full_name")
            ),
            json.getJsonObject("subject")
        ).push(github, events);
    }

}
