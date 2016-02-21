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

import com.google.common.collect.Iterables;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import io.wring.model.Pipe;
import io.wring.model.Pipes;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Dynamo Pitches.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
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
    public Iterable<Directive> asXembly() {
        return new Directives().add("pipes").append(
            Iterables.concat(
                Iterables.transform(
                    this.table()
                        .frame()
                        .through(new QueryValve())
                        .where("urn", Conditions.equalTo(this.urn)),
                    DyPipes::asDirs
                )
            )
        ).up();
    }

    @Override
    public Pipe add(final String yaml) throws IOException {
        final Item item = this.table().put(
            new Attributes()
                .with("urn", this.urn)
                .with("id", System.currentTimeMillis())
                .with("yaml", yaml)
        );
        return new DyPipe(item);
    }

    @Override
    public Pipe pipe(final long number) {
        final Item item = this.table()
            .frame()
            .through(new QueryValve())
            .where("urn", Conditions.equalTo(this.urn))
            .where("id", Conditions.equalTo(number))
            .iterator()
            .next();
        return new DyPipe(item);
    }

    /**
     * Item into event.
     * @param item Item from Dynamo
     * @return Directives
     */
    private static Iterable<Directive> asDirs(final Item item) {
        try {
            return new Directives()
                .add("pipe")
                .add("id").set(item.get("id").getN()).up()
                .add("yaml").set(item.get("yaml").getS()).up()
                .up();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Table to work with.
     * @return Table
     */
    private Table table() {
        return this.region.table("pipes");
    }

}
