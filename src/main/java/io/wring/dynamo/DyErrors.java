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

import com.amazonaws.services.dynamodbv2.model.Select;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.log.Logger;
import io.wring.model.Error;
import io.wring.model.Errors;
import java.io.IOException;
import java.util.Iterator;

/**
 * Errors stored in Dynamo database.
 *
 * @author Paulo Lobo (pauloeduardolobo@gmail.com)
 * @version $Id$
 * @since 1.0
 * @todo #78:30min Add errors table to dynamo. This table must have the columns
 *  'urn', 'title', 'description' and 'time' just like columns from events
 *  table. Then remove ignore annotation from DyErrorsTest and DyError test.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DyErrors implements Errors {

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
    public DyErrors(final Region reg, final String user) {
        this.region = reg;
        this.urn = user;
    }

    @Override
    public Iterable<Error> iterate() {
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
            .map(DyError::new)
            .map(Error.class::cast)
            .iterator();
    }

    @Override
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public void register(final String title, final String description) {
        try {
            this.table().put(
                new Attributes()
                    .with("urn", this.urn)
                    .with("title", title)
                    .with("description", description)
                    .with("time", System.currentTimeMillis())
            );
            Logger.info(
                this, "Error registered for %s: \"%s\"",
                this.urn, title
            );
        } catch (final IOException err) {
            throw new RuntimeException(err);
        }
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
        return this.region.table("errors");
    }
}
