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
import io.wring.model.Pipe;
import io.wring.model.Pipes;
import java.io.IOException;
import java.util.Date;
import javax.json.Json;
import org.cactoos.io.InputOf;

/**
 * Dynamo Pitches.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DyPipes implements Pipes {

    /**
     * The region to work with.
     */
    private final transient Region region;

    /**
     * The user.
     */
    private final transient String urn;

    /**
     * Ctor.
     * @param reg Region
     * @param user URN of the user
     */
    public DyPipes(final Region reg, final String user) {
        this.region = reg;
        this.urn = user;
    }

    @Override
    public Iterable<Pipe> iterate() {
        return () -> this.table()
            .frame()
            .through(
                new QueryValve()
                    .withSelect(Select.ALL_ATTRIBUTES)
                    .withLimit(Tv.HUNDRED)
                    .withConsistentRead(true)
            )
            .where("urn", Conditions.equalTo(this.urn))
            .stream()
            .map(DyPipe::new)
            .map(Pipe.class::cast)
            .iterator();
    }

    @Override
    public void add(final String json) throws IOException {
        Json.createReader(new InputOf(json).stream()).readObject();
        final long num = System.currentTimeMillis();
        this.table().put(
            new Attributes()
                .with("urn", this.urn)
                .with("id", num)
                .with("json", json)
                .with("status", String.format("Created at %s", new Date()))
                .with("time", System.currentTimeMillis())
        );
        Logger.info(
            this, "New pipe #%d created by %s",
            num, this.urn
        );
    }

    @Override
    public Pipe pipe(final long number) {
        final Item item = this.table()
            .frame()
            .through(new QueryValve().withLimit(1).withConsistentRead(true))
            .where("urn", Conditions.equalTo(this.urn))
            .where("id", Conditions.equalTo(number))
            .iterator()
            .next();
        return new DyPipe(item);
    }

    /**
     * Table to work with.
     * @return Table
     */
    private Table table() {
        return this.region.table("pipes");
    }

}
