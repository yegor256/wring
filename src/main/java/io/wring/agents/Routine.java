/*
 * Copyright (c) 2016-2020, Yegor Bugayenko
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
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.sentry.Sentry;
import io.wring.model.Base;
import io.wring.model.Pipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.cactoos.Fallback;
import org.cactoos.func.FuncOf;
import org.cactoos.func.FuncWithFallback;
import org.cactoos.iterable.Mapped;
import org.cactoos.proc.ProcOf;
import org.cactoos.proc.RunnableOf;

/**
 * Routine.
 *
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class Routine implements Callable<Integer>, AutoCloseable {

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
    private final transient ScheduledExecutorService ticker;

    /**
     * Executor.
     */
    private final transient ExecutorService executor;

    /**
     * Telegram.
     */
    private final transient TelegramBot telegram;

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
        this.ticker = Executors.newSingleThreadScheduledExecutor(
            new VerboseThreads(Routine.class)
        );
        this.executor = Executors.newFixedThreadPool(
            this.threads,
            new VerboseThreads(
                String.format(
                    "Routine-%04x",
                    // @checkstyle MagicNumber (1 line)
                    System.currentTimeMillis() % 0xffffL
                )
            )
        );
        this.telegram = new TelegramBot(Manifests.read("Wring-TelegramToken"));
    }

    /**
     * Start it.
     */
    public void start() {
        this.telegram.setUpdatesListener(
            updates -> {
                for (final Update update : updates) {
                    final long chat = update.message().chat().id();
                    this.telegram.execute(
                        new SendMessage(
                            chat,
                            String.format("Your chat ID is %d", chat)
                        )
                    );
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        );
        Sentry.init(Manifests.read("Wring-SentryDsn"));
        this.ticker.scheduleWithFixedDelay(
            new VerboseRunnable(
                () -> {
                    try {
                        this.call();
                    } catch (final InterruptedException ex) {
                        Sentry.capture(ex);
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                },
                true, true
            ),
            1L, 1L, TimeUnit.MINUTES
        );
        Logger.info(this, "Routine started with %d threads", this.threads);
    }

    @Override
    @SuppressWarnings("PMD.PrematureDeclaration")
    public Integer call() throws InterruptedException {
        Thread.getAllStackTraces().forEach(
            (thread, stack) -> {
                if (thread.getName().contains("Routine-")
                    && thread.isInterrupted()) {
                    Logger.info(
                        this,
                        String.format(
                            "Interrupted thread, cleaning %s/%s/%B/%B",
                            thread.getName(),
                            thread.getState(),
                            thread.isAlive(),
                            thread.isInterrupted()
                        )
                    );
                    try {
                        thread.join();
                    } catch (final InterruptedException err) {
                        Logger.info(
                            this,
                            String.format(
                                "Cleared thread %s",
                                thread.getName()
                            )
                        );
                    }
                }
            }
        );
        final long start = System.currentTimeMillis();
        final Collection<Future<?>> futures = new ArrayList<>(this.threads);
        for (final Pipe pipe : this.base.pipes()) {
            futures.add(this.executor.submit(this.job(pipe)));
        }
        try {
            for (final Future<?> future : futures) {
                future.get(Routine.LAG, TimeUnit.MINUTES);
            }
        } catch (final ExecutionException | TimeoutException ex) {
            throw new IllegalStateException(ex);
        }
        if (Logger.isInfoEnabled(this)) {
            Logger.info(
                this, "%d pipes processed in %[ms]s, threads=%d: %s",
                futures.size(), System.currentTimeMillis() - start,
                Thread.getAllStackTraces().size(),
                String.join(
                    ", ",
                    new Mapped<>(
                        Thread::getName, Thread.getAllStackTraces().keySet()
                    )
                )
            );
        }
        return futures.size();
    }

    @Override
    public void close() {
        try {
            Routine.close(this.ticker);
            Routine.close(this.executor);
        } catch (final InterruptedException ex) {
            Sentry.capture(ex);
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Close it.
     * @param svc The service
     * @throws InterruptedException If fails
     */
    private static void close(final ExecutorService svc)
        throws InterruptedException {
        svc.shutdown();
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
    }

    /**
     * Create one job for the pipe.
     * @param pipe The pipe
     * @return The job for this pipe
     */
    private Runnable job(final Pipe pipe) {
        return new RunnableOf(
            new ProcOf<>(
                new FuncWithFallback<>(
                    new FuncOf<>(new Cycle(this.base, this.telegram), null),
                    new Fallback.From<Object>(
                        Exception.class,
                        error -> {
                            Sentry.capture(error);
                            throw new IllegalStateException(error);
                        }
                    )
                )
            ),
            pipe
        );
    }

}
