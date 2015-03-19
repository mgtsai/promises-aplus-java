//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.lw.P;
import promises.lw.RV;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public abstract class LightWeightPromiseImpl<V> extends BasePromiseImpl implements P<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseFactory<LightWeightPromiseImpl<Object>>
    factorySingleton = new PromiseFactory<LightWeightPromiseImpl<Object>>() {
        @Override public final LightWeightPromiseImpl<Object> alwaysPendingPromise() {
            return new LightWeightPromiseImpl<Object>(new Object()) {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override final boolean inSyncIsAlwaysPending() { return true; }
                @Override final Object doAwait() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override final Object
                doAwait(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitForever(timeout, unit);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> pendingPromise(final PromiseStore store) {
            return new LightWeightPromiseImpl<Object>(store) {
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

                @Override public final promises.Promise toUntypedPromise() {
                    return store.promiseInterface(UntypedPromiseImpl.factory);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return store.promiseInterface(TypedPromiseImpl.<Object, R>factory());
                }

                @Override final void
                inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask) {
                    store.inSyncAppendTasksToPendingQueue(onFulfilledTask, onRejectedTask);
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> fulfilledPromise(final Object value) {
            return new LightWeightPromiseImpl<Object>(new Object()) {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override final Object getValue() { return value; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override final boolean inSyncIsAlwaysPending() { return false; }
                @Override final Object doAwait() { return value; }
                @Override final Object doAwait(final long timeout, final TimeUnit unit) { return value; }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().fulfilledPromise(value);
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new LightWeightPromiseImpl<Object>(new Object()) {
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

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.rejectedPromise(reason, exception);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().rejectedPromise(reason, exception);
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    public static <V> PromiseFactory<LightWeightPromiseImpl<V>> factory()
    {
        return ImplUtil.cast(factorySingleton);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private LightWeightPromiseImpl(final Object syncLock)
    {
        super(syncLock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V value()
    {
        return ImplUtil.cast(getValue());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V await() throws InterruptedException, PromiseRejectedException
    {
        return ImplUtil.cast(doAwait());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V await(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return ImplUtil.cast(doAwait(timeout, unit));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(
        final Executor exec,
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    ) {
        final Executor actualExec = executor(exec);

        return doThen(
            LightWeightPromiseImpl.<VO>factory(),
            ResolutionSupplier.byOnFullfilled(this, actualExec, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, actualExec, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final Executor exec, final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(
            LightWeightPromiseImpl.<VO>factory(),
            ResolutionSupplier.byOnFullfilled(this, executor(exec), onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    ) {
        return doThen(
            LightWeightPromiseImpl.<VO>factory(),
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(
            LightWeightPromiseImpl.<VO>factory(),
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
