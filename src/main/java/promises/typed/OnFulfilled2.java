//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.FR2;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onFulfilled} callbacks with dual-value input.
 *
 * @param <VI1> Type of 1st input fulfilled value
 * @param <VI2> Type of 2nd input fulfilled value
 * @param <VO> Type of output resolution value
 * @param <RO> Type of output resolution reason
 */
public interface OnFulfilled2<VI1, VI2, VO, RO> extends FR2<VI1, VI2, Resolution<? extends VO, ? extends RO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
