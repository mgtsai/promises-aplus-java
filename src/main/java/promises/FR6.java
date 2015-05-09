//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines hexa-argument functions with return value.
 *
 * @param <A1> Type of 1st argument
 * @param <A2> Type of 2nd argument
 * @param <A3> Type of 3rd argument
 * @param <A4> Type of 4th argument
 * @param <A5> Type of 5th argument
 * @param <A6> Type of 6th argument
 * @param <R> Type of return value
 */
public interface FR6<A1, A2, A3, A4, A5, A6, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Calls this function.
     *
     * @param a1 The 1st argument
     * @param a2 The 2nd argument
     * @param a3 The 3rd argument
     * @param a4 The 4th argument
     * @param a5 The 5th argument
     * @param a6 The 6th argument
     * @return The returned value
     * @throws Throwable The throwable thrown by this execution
     */
    R call(final A1 a1, final A2 a2, final A3 a3, final A4 a4, final A5 a5, final A6 a6) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
