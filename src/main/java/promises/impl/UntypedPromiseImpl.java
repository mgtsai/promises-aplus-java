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
import promises.impl.store.PromiseStore;
import promises.impl.store.ResolveAction;
import promises.impl.store.StoreFacade;
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
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override public final <V> V value() { return null; }
                @Override public final <R> R reason() { return null; }
                @Override public final Throwable exception() { return null; }

                @Override public final <V> V await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public final <V> V
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().alwaysPendingPromise();
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().alwaysPendingPromise();
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setAlwaysPending(resAction);
                }

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return factory.alwaysPendingPromise();
                }
            };
        }

        @Override public final UntypedPromiseImpl pendingPromise(final PromiseStore store) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return StoreFacade.state(store); }
                @Override public final <V> V value() { return ImplUtil.cast(StoreFacade.value(store)); }
                @Override public final <R> R reason() { return ImplUtil.cast(StoreFacade.reason(store)); }
                @Override public final Throwable exception() { return StoreFacade.exception(store); }

                @Override public final <V> V await() throws PromiseRejectedException, InterruptedException {
                    return ImplUtil.cast(StoreFacade.await(this, store));
                }

                @Override public final <V> V await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return ImplUtil.cast(StoreFacade.await(this, store, timeout, unit));
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().pendingPromise(store);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().pendingPromise(store);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.applyResolveAction(store, resAction);
                }

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return StoreFacade.doThen(store, factory, exec, onFulfilled, 0, onRejected, 0);
                }
            };
        }

        @Override public final UntypedPromiseImpl fulfilledPromise(final Object value) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override public final <V> V value() { return ImplUtil.cast(value); }
                @Override public final <R> R reason() { return null; }
                @Override public final Throwable exception() { return null; }

                @Override public final <V> V await() { return ImplUtil.cast(value); }

                @Override public final <V> V await(final long timeout, final TimeUnit unit) {
                    return ImplUtil.cast(value);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().fulfilledPromise(value);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().fulfilledPromise(value);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setFulfilled(resAction, value);
                }

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return StoreFacade.fulfilledChainDstPromise(factory, exec, onFulfilled, 0, value);
                }
            };
        }

        @Override public final UntypedPromiseImpl rejectedPromise(final Object reason, final Throwable exception) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override public final <V> V value() { return null; }
                @Override public final <R> R reason() { return ImplUtil.cast(reason); }
                @Override public final Throwable exception() { return exception; }

                @Override public final <V> V await() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public final <V> V
                await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().rejectedPromise(reason, exception);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().rejectedPromise(reason, exception);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setRejected(resAction, reason, exception);
                }

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return StoreFacade.rejectedChainDstPromise(factory, exec, onRejected, 0, reason, exception);
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    abstract UntypedPromiseImpl
    doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        return doThen(executor(exec), onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled)
    {
        return doThen(executor(exec), onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled)
    {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
