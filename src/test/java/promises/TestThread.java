//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
//---------------------------------------------------------------------------------------------------------------------
public final class TestThread extends Thread implements ThreadFactory
{
    //-----------------------------------------------------------------------------------------------------------------
    public final ExecutorService executor;
    private Runnable runnable;
    //-----------------------------------------------------------------------------------------------------------------
    public TestThread()
    {
        executor = Executors.newSingleThreadExecutor(this);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Thread newThread(@Nonnull final Runnable runnable)
    {
        this.runnable = runnable;
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void run()
    {
        runnable.run();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
