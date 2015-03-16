//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines handlers for handling {@link InternalException}.
 */
public interface InternalExceptionHandler
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Invoked when a new {@link InternalException} is created, which normally being thrown a while later.
     *
     * @param exception The newly created {@link InternalException}
     */
    public abstract void onCreated(final InternalException exception);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Invoked when an {@link InternalException} is caught by the promise callback handler.
     *
     * @param exception The caught {@link InternalException}
     */
    public abstract void onCaught(final InternalException exception);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
