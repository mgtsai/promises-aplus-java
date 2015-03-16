//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.M2;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines dual-value {@code resolvePromise} callbacks.
 *
 * @param <V1> Type of 1st fulfilled value
 * @param <V2> Type of 2nd fulfilled value
 */
public interface ResP2<V1, V2>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param res The resolution which the target promise would be resolved with
     */
    public abstract void resolve(final RV<? extends M2<? extends V1, ? extends V2>> res);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v1 The 1st value which the target promise would be fulfilled with
     * @param v2 The 2nd value which the target promise would be fulfilled with
     */
    public abstract void resolve(final V1 v1, final V2 v2);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
