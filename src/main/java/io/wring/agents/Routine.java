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

import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.jcabi.manifests.Manifests;
import io.sentry.Sentry;
import io.wring.model.Base;
import io.wring.model.Pipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.func.RunnableOf;
import org.cactoos.iterable.Mapped;

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
     * Time to wait, in minutes.
     */
    private static final long LAG = 10L;

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
        Logger.info(this, "Routine started with %d threads", this.threads);
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public void run() {
        final long start = System.currentTimeMillis();
        final Collection<Future<?>> futures = new ArrayList<>(this.threads);
        final ExecutorService runner = Executors.newFixedThreadPool(
            this.threads, new VerboseThreads("routine-run")
        );
        for (final Pipe pipe : this.base.pipes()) {
            futures.add(runner.submit(this.job(pipe)));
        }
        try {
            for (final Future<?> future : futures) {
                future.get(Routine.LAG, TimeUnit.MINUTES);
            }
        } catch (final ExecutionException | TimeoutException ex) {
            throw new IllegalStateException(ex);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } finally {
            Routine.close(runner);
        }
        Logger.info(
            this, "%d pipes processed in %[ms]s, threads=%d: %s",
            futures.size(), System.currentTimeMillis() - start,
            Thread.getAllStackTraces().size(),
            new Mapped<>(Thread::getName, Thread.getAllStackTraces().keySet())
        );
    }

    @Override
    public void close() {
        Routine.close(this.executor);
    }

    /**
     * Close it.
     * @param svc The service
     */
    private static void close(final ExecutorService svc) {
        svc.shutdown();
        try {
            if (!svc.awaitTermination(Routine.LAG, TimeUnit.MINUTES)) {
                Logger.error(
                    Routine.class,
                    "Service has been terminated with %d jobs left",
                    svc.shutdownNow().size()
                );
            }
            if (!svc.awaitTermination(Routine.LAG, TimeUnit.MINUTES)) {
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
                        error -> {
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
