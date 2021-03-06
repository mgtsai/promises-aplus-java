//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines penta-reason {@code rejectPromise} callbacks.
 *
 * @param <R1> Type of 1st rejected reason
 * @param <R2> Type of 2nd rejected reason
 * @param <R3> Type of 3rd rejected reason
 * @param <R4> Type of 4th rejected reason
 * @param <R5> Type of 5th rejected reason
 */
public interface RejectPromise5<R1, R2, R3, R4, R5>
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
    public abstract void reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5, final Throwable e);
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
    public abstract void reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5);
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
