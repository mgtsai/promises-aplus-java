//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines dual-argument functions without return value.
 *
 * @param <A1> Type of 1st argument
 * @param <A2> Type of 2nd argument
 */
public interface F2<A1, A2>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Calls this function.
     *
     * @param a1 The 1st argument
     * @param a2 The 2nd argument
     * @throws Throwable The throwable thrown by this execution
     */
    void call(final A1 a1, final A2 a2) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
