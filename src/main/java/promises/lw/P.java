//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.PromiseRejectedException;
import promises.PromiseState;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines promise objects.
 *
 * @param <V> Type of promise fulfilled value
 */
public interface P<V> extends RV<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the state of this promise.
     *
     * @return The promise state
     */
    public abstract PromiseState state();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected exception of this promise if in the {@code REJECTED} state, or {@code null} if in other
     * states.
     *
     * @return The rejected exception
     */
    public abstract Throwable exception();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Waits until this promise is resolved by returning the fulfilled value when the state becomes {@code FULFILLED}
     * or throwing {@link PromiseRejectedException} when the state becomes {@code REJECTED}.
     *
     * @return The fulfilled value when the state of this promise becomes {@code FULFILLED}
     * @throws PromiseRejectedException If the state of this promise becomes {@code REJECTED}
     * @throws InterruptedException If the current thread is interrupted while waiting
     */
    public abstract V await() throws PromiseRejectedException, InterruptedException;
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Waits until this promise is resolved by returning the fulfilled value when the state becomes {@code FULFILLED}
     * or throwing {@link PromiseRejectedException} when the state becomes {@code REJECTED}, or until the specified
     * timeout is reached by throwing {@link TimeoutException}.
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the {@code timeout} argument
     * @return The fulfilled value when the state of this promise becomes {@code FULFILLED}
     * @throws PromiseRejectedException If the state of this promise becomes {@code REJECTED}
     * @throws InterruptedException If the current thread is interrupted while waiting
     * @throws TimeoutException If the specified timeout is reached
     */
    public abstract V await(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the untyped interface representing this promise.
     *
     * @return The untyped interface of this promise
     */
    public abstract promises.Promise toUntypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the typed interface representing this promise.
     *
     * @return The typed interface of this promise
     */
    public abstract <R> promises.typed.Promise<V, R> toTypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    public abstract <VO> P<VO>
    then(final Executor exec, final OnFul<? super V, VO> onFulfilled, final OnRej<VO> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    public abstract <VO> P<VO> then(final Executor exec, final OnFul<? super V, VO> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback by using the current-thread executor.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    public abstract <VO> P<VO> then(final OnFul<? super V, VO> onFulfilled, final OnRej<VO> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback by using the current-thread executor.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    public abstract <VO> P<VO> then(final OnFul<? super V, VO> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
