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

import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import io.wring.model.Base;
import io.wring.model.Pipe;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Routine.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
public final class Routine implements Runnable, AutoCloseable {

    /**
     * Base.
     */
    private final transient Base base;

    /**
     * Threads.
     */
    private final transient int threads;

    /**
     * Pipes to process.
     */
    private final transient Queue<Pipe> pipes;

    /**
     * Executor.
     */
    private final transient ScheduledExecutorService executor;

    /**
     * Ctor.
     * @param bse Base
     */
    public Routine(final Base bse) {
        this(bse, Runtime.getRuntime().availableProcessors() << 2);
    }

    /**
     * Ctor.
     * @param bse Base
     * @param total How many threads to run
     */
    public Routine(final Base bse, final int total) {
        this.base = bse;
        this.threads = total;
        this.pipes = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newScheduledThreadPool(
            total, new VerboseThreads(Routine.class)
        );
    }

    @Override
    public void run() {
        final Runnable cycle = new VerboseRunnable(
            new Cycle(this.base, this.pipes), true, true
        );
        for (int thread = 0; thread < this.threads - 1; ++thread) {
            this.executor.scheduleWithFixedDelay(
                cycle,
                (long) Tv.HUNDRED, (long) Tv.HUNDRED,
                TimeUnit.MILLISECONDS
            );
        }
        this.executor.scheduleWithFixedDelay(
            new VerboseRunnable(new Refill(this.base, this.pipes), true, true),
            1L, 1L, TimeUnit.MINUTES
        );
        Logger.info(this, "%d threads started", this.threads);
    }

    @Override
    public void close() {
        this.executor.shutdown();
        this.pipes.clear();
        try {
            if (!this.executor.awaitTermination(1L, TimeUnit.MINUTES)) {
                throw new IllegalStateException("failed to terminate");
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        Logger.info(this, "%d threads stopped", this.threads);
    }

}
