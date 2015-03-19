//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.FR1;
import promises.FR2;
import promises.PromiseRejectedException;
import promises.lw.P;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines promise objects.
 *
 * @param <V> Type of promise fulfilled value
 * @param <R> Type of promise rejected reason
 */
public interface Promise<V, R> extends Resolution<V, R>
{
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
     * Returns the light-weight interface representing this promise.
     *
     * @return The light-weight interface of this promise
     */
    public abstract P<V> toLightWeightPromise();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    public abstract <VO, RO> Promise<VO, RO> then(
        final Executor exec,
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    );
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback by using the specified executor.
     *
     * @param exec The executor on which the callback is executed
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    public abstract <VO, RO> Promise<VO, RO>
    then(final Executor exec, final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method with {@code onRejected} callback by using the current-thread executor.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @param onRejected The {@code onRejected} callback being invoked when this promise is rejected
     * @return The next chained promise
     */
    public abstract <VO, RO> Promise<VO, RO> then(
        final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
    );
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method without {@code onRejected} callback by using the current-thread executor.
     *
     * @param onFulfilled The {@code onFulfilled} callback being invoked when this promise is fulfilled
     * @return The next chained promise
     */
    public abstract <VO, RO> Promise<VO, RO>
    then(final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
