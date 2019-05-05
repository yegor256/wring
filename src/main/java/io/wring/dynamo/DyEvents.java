/**
 * Copyright (c) 2016-2019, Wring.io
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

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.Select;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.log.Logger;
import io.wring.model.Event;
import io.wring.model.Events;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;

/**
 * Dynamo events.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
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
    public Iterable<Event> iterate() {
        return () -> this.table()
            .frame()
            .through(
                new QueryValve()
                    .withLimit(Tv.TWENTY)
                    .withIndexName("top")
                    .withSelect(Select.ALL_ATTRIBUTES)
                    .withScanIndexForward(false)
                    .withConsistentRead(false)
            )
            .where("urn", Conditions.equalTo(this.urn))
            .stream()
            .map(DyEvent::new)
            .map(Event.class::cast)
            .iterator();
    }

    @Override
    public void post(final String title, final String text)
        throws IOException {
        final Iterator<Item> items = this.items(title);
        if (items.hasNext()) {
            final Item item = items.next();
            item.put(
                new AttributeUpdates()
                    .with(
                        "rank",
                        new AttributeValueUpdate()
                            .withValue(new AttributeValue().withN("1"))
                            .withAction(AttributeAction.ADD)
                    )
                    .with(
                        "text",
                        new AttributeValueUpdate()
                            .withAction(AttributeAction.PUT)
                            .withValue(
                                new AttributeValue().withS(
                                    DyEvents.concat(
                                        item.get("text").getS(),
                                        text
                                    )
                                )
                            )
                    )
            );
            Logger.info(
                this, "Event updated for %s: \"%s\"",
                this.urn, title
            );
        } else {
            final int rank;
            if (title.startsWith("io.wring.agents.")) {
                rank = -Tv.THOUSAND;
            } else {
                rank = 1;
            }
            this.table().put(
                new Attributes()
                    .with("urn", this.urn)
                    .with("title", title)
                    .with("text", text)
                    .with("rank", rank)
                    .with("time", System.currentTimeMillis())
            );
            Logger.info(
                this, "Event created for %s: \"%s\"",
                this.urn, title
            );
        }
    }

    @Override
    public Event event(final String title) {
        final Iterator<Item> items = this.items(title);
        if (!items.hasNext()) {
            throw new IllegalArgumentException(
                String.format("Event with title \"%s\" not found", title)
            );
        }
        return new DyEvent(items.next());
    }

    /**
     * Find items by title.
     * @param title Unique title of the event
     * @return Items or empty
     */
    public Iterator<Item> items(final String title) {
        return this.table()
            .frame()
            .through(new QueryValve())
            .where("urn", Conditions.equalTo(this.urn))
            .where("title", Conditions.equalTo(title))
            .iterator();
    }

    /**
     * Table to work with.
     * @return Table
     */
    private Table table() {
        return this.region.table("events");
    }

    /**
     * Concatenate old and new messages.
     * @param before What do we have now
     * @param extra What do add
     * @return Table
     */
    private static String concat(final String before, final String extra) {
        return String.format(
            "%s\n\n---\n\n%s",
            extra.trim(),
            StringUtils.abbreviate(before.trim(), Tv.TEN * Tv.THOUSAND)
        );
    }

}
