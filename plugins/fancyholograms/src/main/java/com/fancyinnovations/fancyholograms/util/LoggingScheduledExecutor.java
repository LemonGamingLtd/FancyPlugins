package com.fancyinnovations.fancyholograms.util;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A single threaded scheduled executor that logs exceptions thrown by its tasks,
 * which a plain {@link ScheduledExecutorService} silently stores in the task's future.
 */
public class LoggingScheduledExecutor extends ScheduledThreadPoolExecutor {

    private final Supplier<Logger> logger;
    private final Map<String, AtomicInteger> seenErrors = new ConcurrentHashMap<>();

    public LoggingScheduledExecutor(@NotNull ThreadFactory threadFactory, @NotNull Supplier<Logger> logger) {
        super(1, threadFactory);
        this.logger = logger;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t == null && r instanceof Future<?> future && future.isDone()) {
            try {
                future.get();
            } catch (CancellationException ignored) {
            } catch (ExecutionException e) {
                t = e.getCause();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (t == null) {
            return;
        }

        StackTraceElement[] trace = t.getStackTrace();
        String key = t.getClass().getName() + (trace.length > 0 ? " at " + trace[0] : "");
        int count = seenErrors.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();

        if (count == 1) {
            logger.get().log(Level.SEVERE, "Uncaught exception in " + Thread.currentThread().getName() + " task", t);
        } else if (count % 100 == 0) {
            logger.get().severe("Uncaught exception in " + Thread.currentThread().getName() + " task happened " + count + " times: " + key + ": " + t.getMessage());
        }
    }
}
