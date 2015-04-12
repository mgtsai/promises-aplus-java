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
        @Override public TypedPromiseImpl<Object, Object> fulfilledPromise(final Object value) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override String type() { return "TYPED-FULFILLED"; }
                @Override public PromiseState state() { return PromiseState.FULFILLED; }
                @Override public Object value() { return value; }
                @Override public Object reason() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public Object await() { return value; }
                @Override public Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public LightWeightPromiseImpl<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().fulfilledPromise(value);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setFulfilled(value);
                }

                @Override <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return FulfilledResolver.of(onFulfilled)
                        .chainDstPromise(TypedPromiseImpl.<VO, RO>factory(), exec, onFulfilled, 0, value);
                }
            };
        }

        @Override public TypedPromiseImpl<Object, Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override String type() { return "TYPED-REJECTED"; }
                @Override public PromiseState state() { return PromiseState.REJECTED; }
                @Override public Object value() { return null; }
                @Override public Object reason() { return reason; }
                @Override public Throwable exception() { return exception; }

                @Override public Object await() throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public Object
                await(final long timeout, final TimeUnit unit) throws PromiseRejectedException {
                    throw new PromiseRejectedException(this, reason, exception);
                }

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.rejectedPromise(reason, exception);
                }

                @Override public LightWeightPromiseImpl<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().rejectedPromise(reason, exception);
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setRejected(reason, exception);
                }

                @Override <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return RejectedResolver.of(onRejected)
                        .chainDstPromise(TypedPromiseImpl.<VO, RO>factory(), exec, onRejected, 0, reason, exception);
                }
            };
        }

        @Override public TypedPromiseImpl<Object, Object> alwaysPendingPromise() {
            return new TypedPromiseImpl<Object, Object>() {
                @Override String type() { return "TYPED-ALWAYS-PENDING"; }
                @Override public PromiseState state() { return PromiseState.PENDING; }
                @Override public Object value() { return null; }
                @Override public Object reason() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public Object await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public Object
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public UntypedPromiseImpl toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public LightWeightPromiseImpl<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise();
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    resAction.setAlwaysPending();
                }

                @Override <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return TypedPromiseImpl.<VO, RO>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public TypedPromiseImpl<Object, Object> mutablePromise(final PromiseStore store) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override String type() { return "TYPED-MUTABLE"; }
                @Override public PromiseState state() { return store.state; }
                @Override public Object value() { return store.value; }
                @Override public Object reason() { return store.reason; }
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

                @Override public LightWeightPromiseImpl<Object> toLightWeightPromise() {
                    return store.createPromise(LightWeightPromiseImpl.factory());
                }

                @Override void applyResolveAction(final ResolveAction resAction) {
                    store.applyResolveAction(resAction);
                }

                @Override <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return store.doThen(
                        TypedPromiseImpl.<VO, RO>factory(), exec,
                        FulfilledResolver.of(onFulfilled), onFulfilled, 0,
                        RejectedResolver.of(onRejected), onRejected, 0
                    );
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
    @Override
    public abstract UntypedPromiseImpl toUntypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public abstract LightWeightPromiseImpl<V> toLightWeightPromise();
    //-----------------------------------------------------------------------------------------------------------------
    abstract <VO, RO> TypedPromiseImpl<VO, RO> doThen(
        final Executor exec,
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    );
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO> then(
        final Executor exec,
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    ) {
        return doThen(ImplUtil.executor(exec), onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO>
    then(final Executor exec, final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return doThen(ImplUtil.executor(exec), onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO> then(
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    ) {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO>
    then(final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return doThen(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
