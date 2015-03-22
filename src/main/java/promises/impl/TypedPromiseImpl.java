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
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.PENDING; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return null; }
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

                @Override final <VO, RO> Promise<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return TypedPromiseImpl.<VO, RO>factory().alwaysPendingPromise();
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.onAlwaysPending();
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> pendingPromise(final PromiseStore store) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return store.state; }
                @Override final Object getValue() { return store.value; }
                @Override final Object getReason() { return store.reason; }
                @Override public final Throwable exception() { return store.exception; }

                @Override public final Object await() throws PromiseRejectedException, InterruptedException {
                    return store.await();
                }

                @Override public final Object await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return store.await(timeout, unit);
                }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.pendingPromise(store);
                }

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().pendingPromise(store);
                }

                @Override final <VO, RO> Promise<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return store.doThen(
                        TypedPromiseImpl.<VO, RO>factory(),
                        ResolutionSupplier.byOnFullfilled(this, exec, onFulfilled, 0),
                        ResolutionSupplier.byOnRejected(this, exec, onRejected, 0)
                    );
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    store.resolveDestination(resDstTask);
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object> fulfilledPromise(final Object value) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override final Object getValue() { return value; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() { return value; }
                @Override public final Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public final P<Object> toLightWeightPromise() {
                    return LightWeightPromiseImpl.factory().fulfilledPromise(value);
                }

                @Override final <VO, RO> Promise<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return super.fulfilledThen(TypedPromiseImpl.<VO, RO>factory(), exec, onFulfilled, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.fulfillChainDstPromise(value);
                }
            };
        }

        @Override public final TypedPromiseImpl<Object, Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new TypedPromiseImpl<Object, Object>() {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return reason; }
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

                @Override final <VO, RO> Promise<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return super.rejectedThen(TypedPromiseImpl.<VO, RO>factory(), exec, onRejected, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.rejectChainDstPromise(reason, exception);
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
    abstract <VO, RO> Promise<VO, RO> doThen(
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
