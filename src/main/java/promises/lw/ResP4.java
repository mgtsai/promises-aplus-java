//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.M4;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines quad-value {@code resolvePromise} callbacks.
 *
 * @param <V1> Type of 1st fulfilled value
 * @param <V2> Type of 2nd fulfilled value
 * @param <V3> Type of 3rd fulfilled value
 * @param <V4> Type of 4th fulfilled value
 */
public interface ResP4<V1, V2, V3, V4>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param res The resolution which the target promise would be resolved with
     */
    public abstract void resolve(final RV<? extends M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>> res);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v1 The 1st value which the target promise would be fulfilled with
     * @param v2 The 2nd value which the target promise would be fulfilled with
     * @param v3 The 3rd value which the target promise would be fulfilled with
     * @param v4 The 4th value which the target promise would be fulfilled with
     */
    public abstract void resolve(final V1 v1, final V2 v2, final V3 v3, final V4 v4);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
