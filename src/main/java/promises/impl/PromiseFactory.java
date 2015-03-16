//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import promises.PromiseRejectedException;
import promises.PromiseState;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class PromiseFactory
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final CountDownLatch waitForever = new CountDownLatch(1);
    //-----------------------------------------------------------------------------------------------------------------
    public static <V, R> AbstractPromise<V, R> alwaysPendingPromise()
    {
        return new AbstractPromise<V, R>() {
            @Override public final PromiseState state() { return PromiseState.PENDING; }
            @Override public final V value() { return null; }
            @Override public final R reason() { return null; }
            @Override public final Throwable exception() { return null; }

            @Override public final V await() throws InterruptedException {
                waitForever.await();
                throw new InternalException("Running after indefinite waiting");
            }

            @Override public final V
            await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                waitForever.await(timeout, unit);
                throw new TimeoutException("Timeout is reached for waiting this promise being resolved");
            }

            @Override final boolean inSyncIsAlwaysPending() { return true; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <V, R> AbstractPromise<V, R> fulfilledPromise(final V value)
    {
        return new AbstractPromise<V, R>() {
            @Override public final PromiseState state() { return PromiseState.FULFILLED; }
            @Override public final V value() { return value; }
            @Override public final R reason() { return null; }
            @Override public final Throwable exception() { return null; }
            @Override public final V await() { return value; }
            @Override public final V await(final long timeout, final TimeUnit unit) { return value; }
            @Override final boolean inSyncIsAlwaysPending() { return false; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <V, R> AbstractPromise<V, R> rejectedPromise(final R reason, final Throwable exception)
    {
        return new AbstractPromise<V, R>() {
            @Override public final PromiseState state() { return PromiseState.REJECTED; }
            @Override public final V value() { return null; }
            @Override public final R reason() { return reason; }
            @Override public final Throwable exception() { return exception; }

            @Override public final V await() throws PromiseRejectedException {
                throw new PromiseRejectedException(this, reason, exception);
            }

            @Override public final V await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                throw new PromiseRejectedException(this, reason, exception);
            }

            @Override final boolean inSyncIsAlwaysPending() { return false; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
