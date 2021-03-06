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
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public abstract class UntypedPromiseImpl extends BasePromiseImpl implements Promise
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final UntypedPromiseImpl originPromise = new UntypedPromiseImpl()
    {
        @Override String type() { return "UNTYPED-ORIGIN"; }
        @Override public PromiseState state() { return PromiseState.FULFILLED; }
        @Override public <V> V value() { return null; }
        @Override public <R> R reason() { return null; }
        @Override public Throwable exception() { return null; }
        @Override public <V> V await() { return null; }
        @Override public <V> V await(final long timeout, final TimeUnit unit) { return null; }

        @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() {
            return TypedPromiseImpl.<V, R>factory().originPromise();
        }

        @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() {
            return LightWeightPromiseImpl.<V>factory().originPromise();
        }

        @Override void applyResolveAction(final ResolveAction resAction) { resAction.setFulfilled(null); }

        @Override <V, R> UntypedPromiseImpl
        doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
            final FulfilledResolver<FR1<? super V, ?>, ?> resolver = FulfilledResolver.of(exec, onFulfilled);
            return resolver.chainDstPromise(factory, exec, onFulfilled, 0, null);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    public static PromiseFactory<UntypedPromiseImpl> factory = new PromiseFactory<UntypedPromiseImpl>() {
        @Override public UntypedPromiseImpl originPromise() { return originPromise; }

        @Override public UntypedPromiseImpl fulfilledPromise(final Object value) {
            return new UntypedPromiseImpl() {
                @Override String type() { return "UNTYPED-FULFILLED"; }
                @Override public PromiseState state() { return PromiseState.FULFILLED; }
                @Override public <V> V value() { return ImplUtil.cast(value); }
                @Override public <R> R reason() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public <V> V await() { return ImplUtil.cast(value); }
                @Override public <V> V await(final long timeout, final TimeUnit unit) { return ImplUtil.cast(value); }

                @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().fulfilledPromise(value);
                }

                @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().fulfilledPromise(value);
                }

                @Override void applyResolveAction(final ResolveAction resAction) { resAction.setFulfilled(value); }

                @Override <V, R> UntypedPromiseImpl
                doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
                    final FulfilledResolver<FR1<? super V, ?>, ?> resolver = FulfilledResolver.of(exec, onFulfilled);
                    return resolver.chainDstPromise(factory, exec, onFulfilled, 0, value);
                }
            };
        }

        @Override public UntypedPromiseImpl rejectedPromise(final Object reason, final Throwable exception) {
            return new UntypedPromiseImpl() {
                @Override String type() { return "UNTYPED-REJECTED"; }
                @Override public PromiseState state() { return PromiseState.REJECTED; }
                @Override public <V> V value() { return null; }
                @Override public <R> R reason() { return ImplUtil.cast(reason); }
                @Override public Throwable exception() { return exception; }

                @Override public <V> V await() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public <V> V await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().rejectedPromise(reason, exception);
                }

                @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().rejectedPromise(reason, exception);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setRejected(reason, exception);
                }

                @Override <V, R> UntypedPromiseImpl
                doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
                    final RejectedResolver<?, FR2<? super R, Throwable, ?>>
                        resolver = RejectedResolver.of(exec, onRejected);

                    return resolver.chainDstPromise(factory, exec, onRejected, 0, reason, exception);
                }
            };
        }

        @Override public UntypedPromiseImpl alwaysPendingPromise() {
            return new UntypedPromiseImpl() {
                @Override String type() { return "UNTYPED-ALWAYS-PENDING"; }
                @Override public PromiseState state() { return PromiseState.PENDING; }
                @Override public <V> V value() { return null; }
                @Override public <R> R reason() { return null; }
                @Override public Throwable exception() { return null; }

                @Override public <V> V await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public <V> V
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().alwaysPendingPromise();
                }

                @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().alwaysPendingPromise();
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setAlwaysPending();
                }

                @Override <V, R> UntypedPromiseImpl
                doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
                    return factory.alwaysPendingPromise();
                }
            };
        }

        @Override public UntypedPromiseImpl mutablePromise(final PromiseStore store) {
            return new UntypedPromiseImpl() {
                @Override String type() { return "UNTYPED-MUTABLE"; }
                @Override public PromiseState state() { return store.state; }
                @Override public <V> V value() { return ImplUtil.cast(store.value); }
                @Override public <R> R reason() { return ImplUtil.cast(store.reason); }
                @Override public Throwable exception() { return store.exception; }

                @Override public <V> V await() throws PromiseRejectedException, InterruptedException {
                    return ImplUtil.cast(store.await(this));
                }

                @Override public <V> V await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return ImplUtil.cast(store.await(this, timeout, unit));
                }

                @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() {
                    return store.createPromise(TypedPromiseImpl.<V, R>factory());
                }

                @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() {
                    return store.createPromise(LightWeightPromiseImpl.<V>factory());
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    store.applyResolveAction(resAction);
                }

                @Override <V, R> UntypedPromiseImpl
                doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
                    return store.doThen(
                        factory, exec,
                        FulfilledResolver.<V, FR2<? super R, Throwable, ?>>of(exec, onFulfilled), onFulfilled, 0,
                        RejectedResolver.<FR1<? super V, ?>, R>of(exec, onRejected), onRejected, 0
                    );
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public abstract <V, R> TypedPromiseImpl<V, R> toTypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public abstract <V> LightWeightPromiseImpl<V> toLightWeightPromise();
    //-----------------------------------------------------------------------------------------------------------------
    abstract <V, R> UntypedPromiseImpl
    doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        return doThen(exec, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final Executor exec, final FR1<?, ?> onFulfilled)
    {
        return doThen(exec, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected)
    {
        return doThen(null, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Promise then(final FR1<?, ?> onFulfilled)
    {
        return doThen(null, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
