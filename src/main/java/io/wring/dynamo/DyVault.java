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
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import io.wring.model.Vault;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

/**
 * Dynamo Vault.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
public final class DyVault implements Vault {

    /**
     * The region to work with.
     */
    private final transient Region region;

    /**
     * Ctor.
     * @param reg Region
     */
    public DyVault(final Region reg) {
        this.region = reg;
    }

    @Override
    public Optional<String> value(final String key) throws IOException {
        final Iterator<Item> items = this.table()
            .frame()
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withSelect(Select.ALL_ATTRIBUTES)
                    .withConsistentRead(true)
            )
            .where("key", Conditions.equalTo(key))
            .iterator();
        final Optional<String> value;
        if (items.hasNext()) {
            value = Optional.of(items.next().get("value").getS());
        } else {
            value = Optional.empty();
        }
        return value;
    }

    @Override
    public void save(final String key, final Optional<String> value)
        throws IOException {
        if (!value.isPresent()) {
            throw new UnsupportedOperationException("can't delete");
        }
        this.table().put(
            new Attributes()
                .with("key", key)
                .with("value", value.get())
        );
    }

    /**
     * Table to work with.
     * @return Table
     */
    private Table table() {
        return this.region.table("vault");
    }

}
