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
        @Override public LightWeightPromiseImpl<Object> alwaysPendingPromise() {
            return new LightWeightPromiseImpl<Object>() {
                @Override String type() { return "LW-ALWAYS-PENDING"; }
                @Override public PromiseState state() { return PromiseState.PENDING; }
                @Override public Object value() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public Object await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public Object
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().alwaysPendingPromise();
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setAlwaysPending();
                }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return LightWeightPromiseImpl.<VO>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public LightWeightPromiseImpl<Object> mutablePromise(final PromiseStore store) {
            return new LightWeightPromiseImpl<Object>() {
                @Override String type() { return "LW-PENDING"; }
                @Override public PromiseState state() { return store.state; }
                @Override public Object value() { return store.value; }
                @Override public Throwable exception() { return store.exception; }

                @Override public Object await() throws PromiseRejectedException, InterruptedException {
                    return store.await(this);
                }

                @Override public Object await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return store.await(this, timeout, unit);
                }

                @Override public promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.mutablePromise(store);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().mutablePromise(store);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    store.applyResolveAction(resAction);
                }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return store.doThen(
                        LightWeightPromiseImpl.<VO>factory(), exec,
                        FulfilledResolver.of(onFulfilled), onFulfilled, 0,
                        RejectedResolver.of(onRejected), onRejected, 0
                    );
                }
            };
        }

        @Override public LightWeightPromiseImpl<Object> fulfilledPromise(final Object value) {
            return new LightWeightPromiseImpl<Object>() {
                @Override String type() { return "LW-FULFILLED"; }
                @Override public PromiseState state() { return PromiseState.FULFILLED; }
                @Override public Object value() { return value; }
                @Override public Throwable exception() { return null; }
                @Override public Object await() { return value; }
                @Override public Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().fulfilledPromise(value);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setFulfilled(value);
                }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return FulfilledResolver.of(onFulfilled)
                        .chainDstPromise(LightWeightPromiseImpl.<VO>factory(), exec, onFulfilled, 0, value);
                }
            };
        }

        @Override public LightWeightPromiseImpl<Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new LightWeightPromiseImpl<Object>() {
                @Override String type() { return "LW-REJECTED"; }
                @Override public PromiseState state() { return PromiseState.REJECTED; }
                @Override public Object value() { return null; }
                @Override public Throwable exception() { return exception; }

                @Override public Object await() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public Object
                await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.rejectedPromise(reason, exception);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().rejectedPromise(reason, exception);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setRejected(reason, exception);
                }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return RejectedResolver.of(onRejected)
                        .chainDstPromise(LightWeightPromiseImpl.<VO>factory(), exec, onRejected, 0, reason, exception);
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
        return doThen(ImplUtil.executor(exec), onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final Executor exec, final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(ImplUtil.executor(exec), onFulfilled, null);
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
