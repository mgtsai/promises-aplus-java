//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.FR5;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onFulfilled} callbacks with penta-value input.
 *
 * @param <VI1> Type of 1st input fulfilled value
 * @param <VI2> Type of 2nd input fulfilled value
 * @param <VI3> Type of 3rd input fulfilled value
 * @param <VI4> Type of 4th input fulfilled value
 * @param <VI5> Type of 5th input fulfilled value
 * @param <VO> Type of output resolution value
 */
public interface OnFul5<VI1, VI2, VI3, VI4, VI5, VO> extends FR5<VI1, VI2, VI3, VI4, VI5, RV<? extends VO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
