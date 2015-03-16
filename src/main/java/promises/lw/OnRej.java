//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.FR1;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onRejected} callbacks.
 *
 * @param <VO> Type of output resolution value
 */
public interface OnRej<VO> extends FR1<Throwable, RV<? extends VO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
