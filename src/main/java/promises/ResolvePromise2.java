//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines dual-value {@code resolvePromise} callbacks.
 */
public interface ResolvePromise2 extends ResolvePromise
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v1 The 1st value which the target promise would be fulfilled with
     * @param v2 The 2nd value which the target promise would be fulfilled with
     */
    void resolve(final Object v1, final Object v2);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
