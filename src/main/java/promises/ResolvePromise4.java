//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines quad-value {@code resolvePromise} callbacks.
 */
public interface ResolvePromise4 extends ResolvePromise
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v1 The 1st value which the target promise would be fulfilled with
     * @param v2 The 2nd value which the target promise would be fulfilled with
     * @param v3 The 3rd value which the target promise would be fulfilled with
     * @param v4 The 4th value which the target promise would be fulfilled with
     */
    void resolve(final Object v1, final Object v2, final Object v3, final Object v4);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
