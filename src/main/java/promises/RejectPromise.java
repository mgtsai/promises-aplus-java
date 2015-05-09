//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines single-reason {@code rejectPromise} callbacks.
 */
public interface RejectPromise
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r The reason which the target promise would be rejected with
     * @param e The exception which the target promise would be rejected with
     */
    void reject(final Object r, final Throwable e);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r The reason which the target promise would be rejected with
     */
    void reject(final Object r);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param e The exception which the target promise would be rejected with
     */
    void reject(final Throwable e);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
