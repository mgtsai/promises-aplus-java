//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines penta-reason {@code rejectPromise} callbacks.
 */
public interface RejectPromise5
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r1 The 1st reason which the target promise would be rejected with
     * @param r2 The 2nd reason which the target promise would be rejected with
     * @param r3 The 3rd reason which the target promise would be rejected with
     * @param r4 The 4th reason which the target promise would be rejected with
     * @param r5 The 5th reason which the target promise would be rejected with
     * @param e The exception which the target promise would be rejected with
     */
    public abstract void
    reject(final Object r1, final Object r2, final Object r3, final Object r4, final Object r5, final Throwable e);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param r1 The 1st reason which the target promise would be rejected with
     * @param r2 The 2nd reason which the target promise would be rejected with
     * @param r3 The 3rd reason which the target promise would be rejected with
     * @param r4 The 4th reason which the target promise would be rejected with
     * @param r5 The 5th reason which the target promise would be rejected with
     */
    public abstract void reject(final Object r1, final Object r2, final Object r3, final Object r4, final Object r5);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Rejects the target promise.
     *
     * @param e The exception which the target promise would be rejected with
     */
    public abstract void reject(final Throwable e);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
