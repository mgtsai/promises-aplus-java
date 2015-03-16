//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import promises.impl.ImplUtil;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines exceptions indicating rejected promises.
 */
public final class PromiseRejectedException extends Exception
{
    //-----------------------------------------------------------------------------------------------------------------
    private final Object promise;
    //-----------------------------------------------------------------------------------------------------------------
    private final Object reason;
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The constructor of this exception.
     *
     * @param promise The rejected promise object
     * @param reason The rejected reason of the {@code promise} argument
     * @param exception The rejected exception of the {@code promise} argument
     */
    public PromiseRejectedException(final Object promise, final Object reason, final Throwable exception)
    {
        super("Promise is rejected", exception);
        this.promise = promise;
        this.reason = reason;
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected promise.
     *
     * @return The rejected promise
     */
    public final <P> P promise()
    {
        return ImplUtil.cast(promise);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected reason of the target promise.
     *
     * @return The rejected reason
     */
    public final <R> R reason()
    {
        return ImplUtil.cast(reason);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected exception of the target promise.
     *
     * @return The rejected exception
     */
    public final Throwable exception()
    {
        return getCause();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
