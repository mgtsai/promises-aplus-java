//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines single-argument functions with return value.
 *
 * @param <A1> Type of 1st argument
 * @param <R> Type of return value
 */
public interface FR1<A1, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Calls this function.
     *
     * @param a1 The 1st argument
     * @return The returned value
     * @throws Throwable The throwable thrown by this execution
     */
    R call(final A1 a1) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
