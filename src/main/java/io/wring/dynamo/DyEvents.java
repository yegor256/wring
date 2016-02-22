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
package io.wring.dynamo;

import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import io.wring.model.Event;
import io.wring.model.Events;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Dynamo events.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DyEvents implements Events {

    /**
     * The region to work with.
     */
    private final transient Region region;

    /**
     * URN.
     */
    private final transient String urn;

    /**
     * Ctor.
     * @param reg Region
     * @param user URN of the user
     */
    public DyEvents(final Region reg, final String user) {
        this.region = reg;
        this.urn = user;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<Directive> asXembly(final String marker)
        throws IOException {
        final Iterable<Item> items = this.table()
            .frame()
            .through(
                new QueryValve()
                    .withLimit(Tv.TWENTY)
                    .withIndexName("top")
                    .withScanIndexForward(false)
                    .withConsistentRead(false)
            )
            .where("urn", Conditions.equalTo(this.urn));
        final Directives dirs = new Directives().add("events");
        for (final Item item : items) {
            dirs.append(new DyEvent(item).asXembly());
        }
        return dirs.up();
    }

    @Override
    public Event post(final String title, final String text)
        throws IOException {
        final Item item = this.table().put(
            new Attributes()
                .with("urn", this.urn)
                .with("title", title)
                .with("text", text)
                .with("rank", Tv.FIFTY)
                .with("time", System.currentTimeMillis())
        );
        return new DyEvent(item);
    }

    @Override
    public Event event(final String title) {
        final Item item = this.table()
            .frame()
            .through(new QueryValve())
            .where("urn", Conditions.equalTo(this.urn))
            .where("title", Conditions.equalTo(title))
            .iterator()
            .next();
        return new DyEvent(item);
    }

    /**
     * Table to work with.
     * @return Table
     */
    private Table table() {
        return this.region.table("events");
    }

}
