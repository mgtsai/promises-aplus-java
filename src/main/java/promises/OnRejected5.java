//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onRejected} callbacks with penta-reason input.
 *
 * @param <RI1> Type of 1st input rejected reason
 * @param <RI2> Type of 2nd input rejected reason
 * @param <RI3> Type of 3rd input rejected reason
 * @param <RI4> Type of 4th input rejected reason
 * @param <RI5> Type of 5th input rejected reason
 */
public interface OnRejected5<RI1, RI2, RI3, RI4, RI5> extends FR6<RI1, RI2, RI3, RI4, RI5, Throwable, Object>
{ }
//---------------------------------------------------------------------------------------------------------------------
