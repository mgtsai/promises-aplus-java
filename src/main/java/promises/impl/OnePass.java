//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
//---------------------------------------------------------------------------------------------------------------------
public final class OnePass
{
    //-----------------------------------------------------------------------------------------------------------------
    private boolean isPassed = false;
    //-----------------------------------------------------------------------------------------------------------------
    public final synchronized boolean pass()
    {
        return !isPassed && (isPassed = true);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------