//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.impl.store.PromiseStore;
import promises.impl.store.ResolveAction;
import promises.impl.store.StoreFacade;
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
            return new LightWeightPromiseImpl<Object>() {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override public final Object value() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public final Object
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().alwaysPendingPromise();
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setAlwaysPending(resAction);
                }

                @Override final <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return LightWeightPromiseImpl.<VO>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> pendingPromise(final PromiseStore store) {
            return new LightWeightPromiseImpl<Object>() {
                @Override public final PromiseState state() { return StoreFacade.state(store); }
                @Override public final Object value() { return StoreFacade.value(store); }
                @Override public final Throwable exception() { return StoreFacade.exception(store); }

                @Override public final Object await() throws PromiseRejectedException, InterruptedException {
                    return StoreFacade.await(this, store);
                }

                @Override public final Object await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return StoreFacade.await(this, store, timeout, unit);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.pendingPromise(store);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().pendingPromise(store);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.applyResolveAction(store, resAction);
                }

                @Override final <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return StoreFacade.doThen(
                        store, LightWeightPromiseImpl.<VO>factory(), exec,
                        onFulfilled, 0,
                        onRejected, 0
                    );
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> fulfilledPromise(final Object value) {
            return new LightWeightPromiseImpl<Object>() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override public final Object value() { return value; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() { return value; }
                @Override public final Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().fulfilledPromise(value);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setFulfilled(resAction, value);
                }

                @Override final <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return StoreFacade.fulfilledChainDstPromise(
                        LightWeightPromiseImpl.<VO>factory(),
                        exec, onFulfilled, 0, value
                    );
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new LightWeightPromiseImpl<Object>() {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override public final Object value() { return null; }
                @Override public final Throwable exception() { return exception; }

                @Override public final Object await() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public final Object
                await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.rejectedPromise(reason, exception);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().rejectedPromise(reason, exception);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setRejected(resAction, reason, exception);
                }

                @Override final <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return StoreFacade.rejectedChainDstPromise(
                        LightWeightPromiseImpl.<VO>factory(),
                        exec, onRejected, 0, reason, exception
                    );
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
    abstract <VO> LightWeightPromiseImpl<VO> doThen(
        final Executor exec,
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    );
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(
        final Executor exec,
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    ) {
        return doThen(executor(exec), onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final Executor exec, final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(executor(exec), onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    ) {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
