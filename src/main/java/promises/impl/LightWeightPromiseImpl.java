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
        @Override public final LightWeightPromiseImpl<Object> alwaysPendingPromise() {
            return new LightWeightPromiseImpl<Object>() {
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

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().alwaysPendingPromise();
                }

                @Override final <VO> P<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return LightWeightPromiseImpl.<VO>factory().alwaysPendingPromise();
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.onAlwaysPending();
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> pendingPromise(final PromiseStore store) {
            return new LightWeightPromiseImpl<Object>() {
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

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().pendingPromise(store);
                }

                @Override final <VO> P<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return store.doThen(
                        LightWeightPromiseImpl.<VO>factory(),
                        ResolutionSupplier.byOnFullfilled(this, exec, onFulfilled, 0),
                        ResolutionSupplier.byOnRejected(this, exec, onRejected, 0)
                    );
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    store.resolveDestination(resDstTask);
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object> fulfilledPromise(final Object value) {
            return new LightWeightPromiseImpl<Object>() {
                @Override public final PromiseState state() { return PromiseState.FULFILLED; }
                @Override final Object getValue() { return value; }
                @Override final Object getReason() { return null; }
                @Override public final Throwable exception() { return null; }
                @Override public final Object await() { return value; }
                @Override public final Object await(final long timeout, final TimeUnit unit) { return value; }

                @Override public final promises.Promise toUntypedPromise() {
                    return UntypedPromiseImpl.factory.fulfilledPromise(value);
                }

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().fulfilledPromise(value);
                }

                @Override final <VO> P<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return super.fulfilledThen(LightWeightPromiseImpl.<VO>factory(), exec, onFulfilled, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.fulfillChainDstPromise(value);
                }
            };
        }

        @Override public final LightWeightPromiseImpl<Object>
        rejectedPromise(final Object reason, final Throwable exception) {
            return new LightWeightPromiseImpl<Object>() {
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

                @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() {
                    return TypedPromiseImpl.<Object, R>factory().rejectedPromise(reason, exception);
                }

                @Override final <VO> P<VO> doThen(
                    final Executor exec,
                    final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return super.rejectedThen(LightWeightPromiseImpl.<VO>factory(), exec, onRejected, 0);
                }

                @Override final void resolveDestination(final ResolvingTask resDstTask) {
                    resDstTask.rejectChainDstPromise(reason, exception);
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
    public final V value()
    {
        return ImplUtil.cast(getValue());
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract <VO> P<VO> doThen(
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
