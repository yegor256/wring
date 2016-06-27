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

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import io.wring.model.Base;
import io.wring.model.Pipe;
import io.wring.model.User;
import io.wring.model.Vault;
import java.util.regex.Pattern;

/**
 * Dynamo Base.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: b2608a3132210a18236a360d0ae5459430ed3a8f $
 * @since 1.0
 */
public final class DyBase implements Base {

    /**
     * URN matcher.
     */
    private static final Pattern URN = Pattern.compile(
        "urn:github:.*"
    );

    /**
     * The region to work with.
     */
    private final transient Region region;

    /**
     * Ctor.
     */
    public DyBase() {
        this(new Dynamo());
    }

    /**
     * Ctor.
     * @param reg Region
     */
    public DyBase(final Region reg) {
        this.region = reg;
    }

    @Override
    public User user(final String name) {
        if (!DyBase.URN.matcher(name).matches()) {
            throw new IllegalArgumentException(
                String.format("invalid user URN: \"%s\"", name)
            );
        }
        return new DyUser(this.region, name);
    }

    @Override
    public Iterable<Pipe> pipes() {
        return Iterables.transform(
            this.region.table("pipes").frame(),
            (Function<Item, Pipe>) DyPipe::new
        );
    }

    @Override
    public Vault vault() {
        return new DyVault(this.region);
    }

}
