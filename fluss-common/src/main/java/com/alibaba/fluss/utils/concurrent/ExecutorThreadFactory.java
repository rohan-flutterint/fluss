/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.fluss.utils.concurrent;

import com.alibaba.fluss.utils.FatalExitExceptionHandler;

import javax.annotation.Nullable;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.alibaba.fluss.utils.Preconditions.checkNotNull;

/* This file is based on source code of Apache Flink Project (https://flink.apache.org/), licensed by the Apache
 * Software Foundation (ASF) under the Apache License, Version 2.0. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. */

/**
 * A thread factory intended for use by critical thread pools. Critical thread pools here mean
 * thread pools that support Fluss's core coordination and processing work, and which must not
 * simply cause unnoticed errors.
 *
 * <p>The thread factory can be given an {@link Thread.UncaughtExceptionHandler} for the threads. If
 * no handler is explicitly given, the default handler for uncaught exceptions will log the
 * exceptions and kill the process afterwards. That guarantees that critical exceptions are not
 * accidentally lost and leave the system running in an inconsistent state.
 *
 * <p>Threads created by this factory are all called '(pool-name)-thread-n', where
 * <i>(pool-name)</i> is configurable, and <i>n</i> is an incrementing number.
 *
 * <p>All threads created by this factory are daemon threads and have the default (normal) priority.
 */
public class ExecutorThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final ThreadGroup group;

    private final String namePrefix;

    private final int threadPriority;

    @Nullable private final Thread.UncaughtExceptionHandler exceptionHandler;
    @Nullable private final ClassLoader threadContextClassLoader;

    // ------------------------------------------------------------------------

    /**
     * Creates a new thread factory using the given thread pool name and the default uncaught
     * exception handler (log exception and kill process).
     *
     * @param poolName The pool name, used as the threads' name prefix
     */
    public ExecutorThreadFactory(String poolName) {
        this(poolName, FatalExitExceptionHandler.INSTANCE);
    }

    /**
     * Creates a new thread factory using the given thread pool name and the created thread will use
     * the given classloader.
     *
     * @param poolName The pool name, used as the threads' name prefix
     */
    public ExecutorThreadFactory(String poolName, @Nullable ClassLoader classLoader) {
        this(poolName, FatalExitExceptionHandler.INSTANCE, classLoader);
    }

    /**
     * Creates a new thread factory using the given thread pool name and the given uncaught
     * exception handler.
     *
     * @param poolName The pool name, used as the threads' name prefix
     * @param exceptionHandler The uncaught exception handler for the threads
     */
    public ExecutorThreadFactory(
            String poolName, Thread.UncaughtExceptionHandler exceptionHandler) {
        this(poolName, Thread.NORM_PRIORITY, exceptionHandler, null);
    }

    ExecutorThreadFactory(
            String poolName,
            Thread.UncaughtExceptionHandler exceptionHandler,
            @Nullable ClassLoader classLoader) {
        this(poolName, Thread.NORM_PRIORITY, exceptionHandler, classLoader);
    }

    ExecutorThreadFactory(
            final String poolName,
            final int threadPriority,
            @Nullable final Thread.UncaughtExceptionHandler exceptionHandler,
            @Nullable final ClassLoader threadContextClassLoader) {
        this.namePrefix = checkNotNull(poolName, "poolName") + "-thread-";
        this.threadPriority = threadPriority;
        this.exceptionHandler = exceptionHandler;

        SecurityManager securityManager = System.getSecurityManager();
        this.group =
                (securityManager != null)
                        ? securityManager.getThreadGroup()
                        : Thread.currentThread().getThreadGroup();
        this.threadContextClassLoader = threadContextClassLoader;
    }

    // ------------------------------------------------------------------------

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement());
        t.setDaemon(true);

        t.setPriority(threadPriority);

        // optional handler for uncaught exceptions
        if (exceptionHandler != null) {
            t.setUncaughtExceptionHandler(exceptionHandler);
        }

        if (threadContextClassLoader != null) {
            t.setContextClassLoader(threadContextClassLoader);
        }

        return t;
    }

    // --------------------------------------------------------------------------------------------

    /** Builder for {@link ExecutorThreadFactory}. */
    public static final class Builder {
        private String poolName;
        private int priority = Thread.NORM_PRIORITY;
        private Thread.UncaughtExceptionHandler exceptionHandler =
                FatalExitExceptionHandler.INSTANCE;
        private @Nullable ClassLoader threadContextClassLoader = null;

        public Builder setPoolName(final String poolName) {
            this.poolName = poolName;
            return this;
        }

        public Builder setThreadPriority(final int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setThreadContextClassloader(
                @Nullable final ClassLoader threadContextClassLoader) {
            this.threadContextClassLoader = threadContextClassLoader;
            return this;
        }

        public Builder setExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public ExecutorThreadFactory build() {
            return new ExecutorThreadFactory(
                    poolName, priority, exceptionHandler, threadContextClassLoader);
        }
    }
}
