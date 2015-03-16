//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onRejected} callbacks with dual-reason input.
 *
 * @param <RI1> Type of 1st input rejected reason
 * @param <RI2> Type of 2nd input rejected reason
 */
public interface OnRejected2<RI1, RI2> extends FR3<RI1, RI2, Throwable, Object>
{ }
//---------------------------------------------------------------------------------------------------------------------
