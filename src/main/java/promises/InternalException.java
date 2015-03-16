//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import promises.impl.ImplUtil;
import promises.impl.LoggerManager;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Denotes an internal exceptions occurred by some implementation errors.
 */
public final class InternalException extends RuntimeException
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The constructor of this exception.
     *
     * @param msgFormat The message format used by {@link String#format}
     * @param args The arguments used by the {@code msgFormat} argument
     */
    public InternalException(final String msgFormat, final Object... args)
    {
        super(ImplUtil.formatString(msgFormat, args));
        LoggerManager.singleton().registeredInternalExceptionHandler().onCreated(this);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
