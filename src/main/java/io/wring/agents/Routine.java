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

import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.manifests.Manifests;
import io.sentry.Sentry;
import io.wring.model.Base;
import io.wring.model.Pipe;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cactoos.Proc;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.RunnableOf;

/**
 * Routine.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
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
        this.executor = Executors.newScheduledThreadPool(
            total, new VerboseThreads(Routine.class)
        );
    }

    /**
     * Start it.
     */
    public void start() {
        Sentry.init(Manifests.read("Wring-SentryDsn"));
        this.executor.scheduleWithFixedDelay(
            new VerboseRunnable(this, true, true),
            1L, 1L, TimeUnit.MINUTES
        );
    }

    @Override
    public void run() {
        final ExecutorService svc = Executors.newFixedThreadPool(
            this.threads, new VerboseThreads()
        );
        try {
            final Collection<Future<?>> futures = new LinkedList<>();
            for (final Pipe pipe : this.base.pipes()) {
                futures.add(svc.submit(this.job(pipe)));
            }
            for (final Future<?> future : futures) {
                try {
                    future.get();
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                } catch (final ExecutionException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } finally {
            svc.shutdown();
        }
    }

    @Override
    public void close() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(1L, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Failed to terminate");
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Create one job for the pipe.
     * @param pipe The pipe
     * @return The job for this pipe
     */
    private Runnable job(final Pipe pipe) {
        return new VerboseRunnable(
            new RunnableOf<>(
                new FuncWithFallback<Pipe, Boolean>(
                    new FuncOf<>(new Cycle(this.base)),
                    new FuncOf<>(
                        (Proc<Throwable>) error -> {
                            Sentry.capture(error);
                            throw new IllegalStateException(error);
                        }
                    )
                ),
                pipe
            ),
            true, true
        );
    }

}
