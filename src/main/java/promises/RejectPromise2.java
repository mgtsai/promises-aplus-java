//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines dual-reason {@code rejectPromise} callbacks.
 */
public interface RejectPromise2
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r1 The 1st reason which the target promise would be rejected with
     * @param r2 The 2nd reason which the target promise would be rejected with
     * @param e The exception which the target promise would be rejected with
     */
    void reject(final Object r1, final Object r2, final Throwable e);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r1 The 1st reason which the target promise would be rejected with
     * @param r2 The 2nd reason which the target promise would be rejected with
     */
    void reject(final Object r1, final Object r2);
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
