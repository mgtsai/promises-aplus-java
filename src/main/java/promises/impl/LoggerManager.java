//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import promises.InternalExceptionHandler;
//---------------------------------------------------------------------------------------------------------------------
public final class LoggerManager
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final LoggerManager singleton = new LoggerManager();
    private InternalExceptionHandler internalExceptionHandler = DEFAULT_INTERNAL_EXCEPTION_HANDLER;
    //-----------------------------------------------------------------------------------------------------------------
    private static InternalExceptionHandler DEFAULT_INTERNAL_EXCEPTION_HANDLER = new InternalExceptionHandler() {
        @Override public final void onCreated(final InternalException exception) {

        }

        @Override public final void onCaught(final InternalException exception) {

        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    public static LoggerManager singleton()
    {
        return LoggerManager.singleton;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final InternalExceptionHandler registeredInternalExceptionHandler()
    {
        return internalExceptionHandler;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void registerInternalExceptionHandler(final InternalExceptionHandler handler)
    {
        this.internalExceptionHandler = handler != null ? handler : DEFAULT_INTERNAL_EXCEPTION_HANDLER;
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
