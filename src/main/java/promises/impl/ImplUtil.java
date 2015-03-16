//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
public final class ImplUtil
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The executor running at the current thread.
     */
    public static Executor CURRENT_THREAD_EXECUTOR = new Executor() {
        @Override public final void execute(@Nonnull final Runnable cmd) { cmd.run(); }
    };
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object obj)
    {
        return (T) obj;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static String formatString(final String msgFormat, final Object... args)
    {
        try {
            return String.format(msgFormat, args);
        } catch (final Throwable e) {
            return msgFormat;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
