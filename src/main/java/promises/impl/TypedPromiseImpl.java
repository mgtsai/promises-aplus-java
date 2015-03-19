//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.FR2;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.lw.P;
import promises.typed.Promise;
import promises.typed.Resolution;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public abstract class TypedPromiseImpl<V, R> extends BasePromiseImpl implements Promise<V, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final PromiseFactory<TypedPromiseImpl<Object, Object>>
    factorySingleton = new PromiseFactory<TypedPromiseImpl<Object, Object>>() {
        @Override public final TypedPromiseImpl<Object, Object> alwaysPendingPromise() {
            return new TypedPromiseImpl<Object, Object>(new Object()) {
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

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise();
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> pendingPromise(final PromiseStore store) {
            return new TypedPromiseImpl<Object, Object>(store) {
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

                @Override public final P<Object> toLightWeightPromise() {
                    return store.promiseInterface(LightWeightPromiseImpl.factory());
                }

                @Override final void
                inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask) {
                    store.inSyncAppendTasksToPendingQueue(onFulfilledTask, onRejectedTask);
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> fulfilledPromise(final Object value) {
            return new TypedPromiseImpl<Object, Object>(new Object()) {
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

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().fulfilledPromise(value);
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new TypedPromiseImpl<Object, Object>(new Object()) {
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

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().rejectedPromise(reason, exception);
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    public static <V, R> PromiseFactory<TypedPromiseImpl<V, R>> factory()
    {
        return ImplUtil.cast(factorySingleton);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private TypedPromiseImpl(final Object syncLock)
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
    public final R reason()
    {
        return ImplUtil.cast(getReason());
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
    public final <VO, RO> Promise<VO, RO> then(
        final Executor exec,
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    ) {
        final Executor actualExec = executor(exec);

        return doThen(
            TypedPromiseImpl.<VO, RO>factory(),
            ResolutionSupplier.byOnFullfilled(this, actualExec, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, actualExec, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO>
    then(final Executor exec, final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return doThen(
            TypedPromiseImpl.<VO, RO>factory(),
            ResolutionSupplier.byOnFullfilled(this, executor(exec), onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO> then(
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    ) {
        return doThen(
            TypedPromiseImpl.<VO, RO>factory(),
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO>
    then(final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return doThen(
            TypedPromiseImpl.<VO, RO>factory(),
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
