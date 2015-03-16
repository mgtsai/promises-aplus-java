//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.FR4;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onRejected} callbacks with triple-reason input.
 *
 * @param <RI1> Type of 1st input rejected reason
 * @param <RI2> Type of 2nd input rejected reason
 * @param <RI3> Type of 3rd input rejected reason
 * @param <VO> Type of output resolution value
 * @param <RO> Type of output resolution reason
 */
public interface OnRejected3<RI1, RI2, RI3, VO, RO>
    extends FR4<RI1, RI2, RI3, Throwable, Resolution<? extends VO, ? extends RO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
