//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class ImplUtil
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final CountDownLatch waitForever = new CountDownLatch(1);
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object obj)
    {
        return (T) obj;
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <V> V waitForever() throws InterruptedException
    {
        waitForever.await();
        throw new InternalException("Running after indefinite waiting");
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <V> V waitTimeout(final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException
    {
        waitForever.await(timeout, unit);
        throw new TimeoutException("Timeout arrived");
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
