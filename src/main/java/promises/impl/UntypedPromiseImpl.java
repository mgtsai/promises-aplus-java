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
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return null; }
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

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return factory.alwaysPendingPromise();
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.onAlwaysPending();
                }
            };
        }

        @Override public final UntypedPromiseImpl pendingPromise(final PromiseStore store) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return store.state; }
                @Override final Object getValue() { return store.value; }
                @Override final Object getReason() { return store.reason; }
                @Override public final Throwable exception() { return store.exception; }

                @Override public final <V> V await() throws PromiseRejectedException, InterruptedException {
                    return ImplUtil.cast(store.await());
                }

                @Override public final <V> V await(final long timeout, final TimeUnit unit)
                    throws PromiseRejectedException, InterruptedException, TimeoutException
                {
                    return ImplUtil.cast(store.await(timeout, unit));
                }

                @Override public final <V, R> promises.typed.Promise<V, R> toTypedPromise() {
                    return TypedPromiseImpl.<V, R>factory().pendingPromise(store);
                }

                @Override public final <V> P<V> toLightWeightPromise() {
                    return LightWeightPromiseImpl.<V>factory().pendingPromise(store);
                }

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return store.doThen(
                        factory,
                        ResolutionSupplier.byOnFullfilled(this, exec, onFulfilled, 0),
                        ResolutionSupplier.byOnRejected(this, exec, onRejected, 0)
                    );
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    store.resolveDestination(resDstTask);
                }
            };
        }

        @Override public final UntypedPromiseImpl fulfilledPromise(final Object value) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override final Object getValue() { return value; }
                @Override final Object getReason() { return null; }
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

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return super.fulfilledThen(factory, exec, onFulfilled, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.fulfillChainDstPromise(value);
                }
            };
        }

        @Override public final UntypedPromiseImpl rejectedPromise(final Object reason, final Throwable exception) {
            return new UntypedPromiseImpl() {
                @Override public final PromiseState state() { return PromiseState.REJECTED; }
                @Override final Object getValue() { return null; }
                @Override final Object getReason() { return reason; }
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

                @Override final UntypedPromiseImpl
                doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                    return super.rejectedThen(factory, exec, onRejected, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.rejectChainDstPromise(reason, exception);
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <V> V value()
    {
        return ImplUtil.cast(getValue());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <R> R reason()
    {
        return ImplUtil.cast(getReason());
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract Promise doThen(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected);
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
