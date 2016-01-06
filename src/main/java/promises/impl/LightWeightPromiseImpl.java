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
    private static final LightWeightPromiseImpl<Object> originPromise = new LightWeightPromiseImpl<Object>() {
        @Override String type() { return "LW-ORIGIN"; }
        @Override public PromiseState state() { return PromiseState.FULFILLED; }
        @Override public Object value() { return null; }
        @Override public Throwable exception() { return null; }
        @Override public Object await() { return null; }
        @Override public Object await(final long timeout, final TimeUnit unit) { return null; }

        @Override public UntypedPromiseImpl toUntypedPromise() {
            return UntypedPromiseImpl.factory.originPromise();
        }

        @Override public <R> TypedPromiseImpl<Object, R> toTypedPromise() {
            return TypedPromiseImpl.<Object, R>factory().originPromise();
        }

        @Override void applyResolveAction(final ResolveAction resAction) {
            resAction.setFulfilled(null);
        }

        @Override <VO> LightWeightPromiseImpl<VO> doThen(
            final Executor exec,
            final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
            final FR1<Throwable, ? extends RV<? extends VO>> onRejected
        ) {
            return FulfilledResolver.of(exec, onFulfilled)
                .chainDstPromise(LightWeightPromiseImpl.<VO>factory(), exec, onFulfilled, 0, null);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseFactory<LightWeightPromiseImpl<Object>>
    factorySingleton = new PromiseFactory<LightWeightPromiseImpl<Object>>() {
        @Override public LightWeightPromiseImpl<Object> originPromise() { return originPromise; }

        @Override public LightWeightPromiseImpl<Object> fulfilledPromise(final Object value) {
            return new LightWeightPromiseImpl<Object>() {
                @Override String type() { return "LW-FULFILLED"; }
                @Override public PromiseState state() { return PromiseState.FULFILLED; }
                @Override public Object value() { return value; }
                @Override public Throwable exception() { return null; }
                @Override public Object await() { return value; }
                @Override public Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public <R> TypedPromiseImpl<Object, R> toTypedPromise() {
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
                    return FulfilledResolver.of(exec, onFulfilled)
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
                    throw new PromiseRejectedException(this, null, exception);
                }

                @Override public Object
                await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, null, exception);
                }

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.rejectedPromise(null, exception);
                }

                @Override public <R> TypedPromiseImpl<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().rejectedPromise(null, exception);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setRejected(null, exception);
                }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return RejectedResolver.of(exec, onRejected)
                        .chainDstPromise(LightWeightPromiseImpl.<VO>factory(), exec, onRejected, 0, null, exception);
                }
            };
        }

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

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public <R> TypedPromiseImpl<Object, R> toTypedPromise() {
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
                @Override String type() { return "LW-MUTABLE"; }
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

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return store.createPromise(UntypedPromiseImpl.factory);
                }

                @Override public <R> TypedPromiseImpl<Object, R> toTypedPromise() {
                    return store.createPromise(TypedPromiseImpl.<Object, R>factory());
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
                        FulfilledResolver.<Object, FR1<Throwable, ?>>of(exec, onFulfilled), onFulfilled, 0,
                        RejectedResolver.<FR1<? super Object, ?>>of(exec, onRejected), onRejected, 0
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
    @Override
    public abstract UntypedPromiseImpl toUntypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public abstract <R> TypedPromiseImpl<V, R> toTypedPromise();
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
        return doThen(exec, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final Executor exec, final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(exec, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(
        final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected
    ) {
        return doThen(null, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final FR1<? super V, ? extends RV<? extends VO>> onFulfilled)
    {
        return doThen(null, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
