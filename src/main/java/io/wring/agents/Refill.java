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
package io.wring.agents;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcabi.log.Logger;
import io.wring.model.Base;
import io.wring.model.Pipe;
import java.util.Collection;
import java.util.Queue;

/**
 * Refill the queue.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id: c79829f9e91907f21c716854779af4233e496fa9 $
 * @since 1.0
 */
final class Refill implements Runnable {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Pipes to process.
     */
    private final transient Queue<Pipe> pipes;

    /**
     * Ctor.
     * @param bse Base
     * @param queue List of them
     */
    Refill(final Base bse, final Queue<Pipe> queue) {
        this.base = bse;
        this.pipes = queue;
    }

    @Override
    public void run() {
        if (this.pipes.isEmpty()) {
            final Collection<Pipe> ext = Lists.newArrayList(this.base.pipes());
            this.pipes.addAll(ext);
            Logger.info(
                this, "%d pipes added to the queue (%d total)",
                ext.size(), Iterables.size(this.base.pipes())
            );
        }
    }

}
