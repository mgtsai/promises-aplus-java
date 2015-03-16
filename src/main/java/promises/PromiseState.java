//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines promise states.
 */
public enum PromiseState
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The state of target promise is {@code PENDING}, which would transit to {@code FULFILLED} or {@code REJECTED}
     * later.
     */
    PENDING,
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The state of target promise is {@code FULFILLED}, and would not transit to another state.
     */
    FULFILLED,
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The state of target promise is {@code REJECTED}, and would not transit to another state.
     */
    REJECTED
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
