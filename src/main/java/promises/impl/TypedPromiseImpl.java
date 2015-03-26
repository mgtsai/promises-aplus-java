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
import promises.impl.store.PromiseStore;
import promises.impl.store.ResolveAction;
import promises.impl.store.StoreFacade;
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
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override public final Object value() { return null; }
                @Override public final Object reason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() throws InterruptedException { return ImplUtil.waitForever(); }

                @Override public final Object
                await(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
                    return ImplUtil.waitTimeout(timeout, unit);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise();
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setAlwaysPending(resAction);
                }

                @Override final <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return TypedPromiseImpl.<VO, RO>factory().alwaysPendingPromise();
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> pendingPromise(final PromiseStore store) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return StoreFacade.state(store); }
                @Override public final Object value() { return StoreFacade.value(store); }
                @Override public final Object reason() { return StoreFacade.reason(store); }
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

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().pendingPromise(store);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.applyResolveAction(store, resAction);
                }

                @Override final <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return StoreFacade.doThen(
                        store, TypedPromiseImpl.<VO, RO>factory(), exec,
                        onFulfilled, 0,
                        onRejected, 0
                    );
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> fulfilledPromise(final Object value) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override public final Object value() { return value; }
                @Override public final Object reason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() { return value; }
                @Override public final Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().fulfilledPromise(value);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setFulfilled(resAction, value);
                }

                @Override final <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return StoreFacade.fulfilledChainDstPromise(
                        TypedPromiseImpl.<VO, RO>factory(),
                        exec, onFulfilled, 0, value
                    );
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override public final Object value() { return null; }
                @Override public final Object reason() { return reason; }
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

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().rejectedPromise(reason, exception);
                }

                @Override public final void applyResolveAction(final ResolveAction resAction) {
                    StoreFacade.setRejected(resAction, reason, exception);
                }

                @Override final <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return StoreFacade.rejectedChainDstPromise(
                        TypedPromiseImpl.<VO, RO>factory(),
                        exec, onRejected, 0, reason, exception
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
        return doThen(executor(exec), onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> Promise<VO, RO>
    then(final Executor exec, final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return doThen(executor(exec), onFulfilled, null);
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
