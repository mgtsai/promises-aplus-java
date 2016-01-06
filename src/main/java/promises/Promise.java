//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import promises.lw.P;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines promise objects.
 */
public interface Promise
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the state of this promise.
     *
     * @return The promise state
     */
    PromiseState state();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the fulfilled value of this promise if in the {@code FULFILLED} state, or {@code null} if in other
     * states.
     *
     * @return The fulfilled value
     */
    <V> V value();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected reason of this promise if in the {@code REJECTED} state, or {@code null} if in other
     * states.
     *
     * @return The rejected reason
     */
    <R> R reason();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected exception of this promise if in the {@code REJECTED} state, or {@code null} if in other
     * states.
     *
     * @return The rejected exception
     */
    Throwable exception();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Waits until this promise is resolved by returning the fulfilled value when the state becomes {@code FULFILLED}
     * or throwing {@link PromiseRejectedException} when the state becomes {@code REJECTED}.
     *
     * @return The fulfilled value when the state of this promise becomes {@code FULFILLED}
     * @throws PromiseRejectedException If the state of this promise becomes {@code REJECTED}
     * @throws InterruptedException If the current thread is interrupted while waiting
     */
    <V> V await() throws PromiseRejectedException, InterruptedException;
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
    <V> V await(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the typed interface representing this promise.
     *
     * @return The typed interface of this promise
     */
    <V, R> promises.typed.Promise<V, R> toTypedPromise();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the light-weight interface representing this promise.
     *
     * @return The light-weight interface of this promise
     */
    <V> P<V> toLightWeightPromise();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed; if {@code exec} is {@code null}, the callback may be
     *             executed on arbitrary thread
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    Promise then(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed; if {@code exec} is {@code null}, the callback may be
     *             executed on arbitrary thread
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    Promise then(final Executor exec, final FR1<?, ?> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    Promise then(final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    Promise then(final FR1<?, ?> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
