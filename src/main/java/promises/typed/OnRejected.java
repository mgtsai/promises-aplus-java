//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.FR2;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines {@code onRejected} callbacks with single-reason input.
 *
 * @param <RI> Type of input rejected reason
 * @param <VO> Type of output resolution value
 * @param <RO> Type of output resolution reason
 */
public interface OnRejected<RI, VO, RO> extends FR2<RI, Throwable, Resolution<? extends VO, ? extends RO>>
{ }
//---------------------------------------------------------------------------------------------------------------------
