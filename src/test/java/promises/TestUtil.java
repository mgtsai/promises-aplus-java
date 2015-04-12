//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
public final class TestUtil
{
    //-----------------------------------------------------------------------------------------------------------------
    public static final Executor NOP_EXECUTOR = new Executor() {
        @Override public void execute(@Nonnull final Runnable command) { }
    };
    //-----------------------------------------------------------------------------------------------------------------
    public static boolean equals(final Object lhs, final Object rhs)
    {
        return lhs == rhs
            || (lhs != null && rhs != null && lhs.equals(rhs));
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static boolean isInstanceOf(final Object obj, final Class<?> objClass)
    {
        if (objClass != null)
            return obj.getClass() == objClass;
        else
            return obj == null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Class<?> exceptionClass(final Throwable exception)
    {
        return exception != null ? exception.getClass() : null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void sleep(final long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            //
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
