//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.FR1;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onFulfilled} callbacks with single-value input.
 *
 * @param <VI> Type of input fulfilled value
 * @param <VO> Type of output resolution value
 */
public interface OnFul<VI, VO> extends FR1<VI, RV<? extends VO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
