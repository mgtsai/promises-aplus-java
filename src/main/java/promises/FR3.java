//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines triple-argument functions with return value.
 *
 * @param <A1> Type of 1st argument
 * @param <A2> Type of 2nd argument
 * @param <A3> Type of 3rd argument
 * @param <R> Type of return value
 */
public interface FR3<A1, A2, A3, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Calls this function.
     *
     * @param a1 The 1st argument
     * @param a2 The 2nd argument
     * @param a3 The 3rd argument
     * @return The returned value
     * @throws Throwable The throwable thrown by this execution
     */
    R call(final A1 a1, final A2 a2, final A3 a3) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
