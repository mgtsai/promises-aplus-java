//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines triple-value {@code resolvePromise} callbacks.
 */
public interface ResolvePromise3 extends ResolvePromise
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v1 The 1st value which the target promise would be fulfilled with
     * @param v2 The 2nd value which the target promise would be fulfilled with
     * @param v3 The 3rd value which the target promise would be fulfilled with
     */
    public abstract void resolve(final Object v1, final Object v2, final Object v3);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
