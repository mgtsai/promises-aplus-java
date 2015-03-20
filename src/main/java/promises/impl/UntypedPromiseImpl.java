//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.FR2;
import promises.Promise;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.lw.P;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public abstract class UntypedPromiseImpl extends BasePromiseImpl implements Promise
{
    //-----------------------------------------------------------------------------------------------------------------
    public static PromiseFactory<UntypedPromiseImpl> factory = new PromiseFactory<UntypedPromiseImpl>() {
        @Override public final UntypedPromiseImpl alwaysPendingPromise() {
            return new UntypedPromiseImpl(new Object()) {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override final boolean inSyncIsAlwaysPending() { return true; }
                @Override final Object doAwait() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override final Object
                doAwait(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().alwaysPendingPromise();
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public final UntypedPromiseImpl pendingPromise(final PromiseStore store) {
            return new UntypedPromiseImpl(store) {
                @Override public final PromiseState state() { return store.state; }
                @Override final Object getValue() { return store.value; }
                @Override final Object getReason() { return store.reason; }
                @Override public final Throwable exception() { return store.exception; }
                @Override final boolean inSyncIsAlwaysPending() { return store.inSyncIsAlwaysPending; }

                @Override final Object doAwait() throws InterruptedException, PromiseRejectedException {
                    return store.doAwait();
                }

                @Override final Object doAwait(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return store.doAwait(timeout, unit);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return store.promiseInterface(TypedPromiseImpl.<V, R>factory());
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return store.promiseInterface(LightWeightPromiseImpl.<V>factory());
                }

                @Override final void
                inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask) {
                    store.inSyncAppendTasksToPendingQueue(onFulfilledTask, onRejectedTask);
                }
            };
        }

        @Override public final UntypedPromiseImpl fulfilledPromise(final Object value) {
            return new UntypedPromiseImpl(new Object()) {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override final Object getValue() { return value; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override final boolean inSyncIsAlwaysPending() { return false; }
                @Override final Object doAwait() { return value; }
                @Override final Object doAwait(final long timeout, final TimeUnit unit) { return value; }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().fulfilledPromise(value);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().fulfilledPromise(value);
                }
            };
        }

        @Override public final UntypedPromiseImpl rejectedPromise(final Object reason, final Throwable exception) {
            return new UntypedPromiseImpl(new Object()) {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return reason; }
                @Override public final Throwable exception() { return exception; }
                @Override final boolean inSyncIsAlwaysPending() { return false; }

                @Override final Object doAwait() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override final Object
                doAwait(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().rejectedPromise(reason, exception);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().rejectedPromise(reason, exception);
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private UntypedPromiseImpl(final Object syncLock)
    {
        super(syncLock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <V> V value()
    {
        return ImplUtil.cast(getValue());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <R> R reason()
    {
        return ImplUtil.cast(getReason());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <V> V await() throws InterruptedException, PromiseRejectedException
    {
        return ImplUtil.cast(doAwait());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <V> V await(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return ImplUtil.cast(doAwait(timeout, unit));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        final Executor actualExec = executor(exec);

        return doThen(
            factory,
            ResolutionSupplier.byOnFullfilled(this, actualExec, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, actualExec, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled)
    {
        return doThen(
            factory,
            ResolutionSupplier.byOnFullfilled(this, executor(exec), onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        return doThen(
            factory,
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled)
    {
        return doThen(
            factory,
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
