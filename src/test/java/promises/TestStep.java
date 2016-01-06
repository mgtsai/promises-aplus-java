//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
//---------------------------------------------------------------------------------------------------------------------
public final class TestStep
{
    //-----------------------------------------------------------------------------------------------------------------
    private final CountDownLatch pause = new CountDownLatch(1);
    private final CountDownLatch finish = new CountDownLatch(1);
    //-----------------------------------------------------------------------------------------------------------------
    public final TestStep pass()
    {
        pause.countDown();
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void pause()
    {
        try {
            pause.await(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            //
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void finish()
    {
        finish.countDown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    public final void sync()
    {
        pause.countDown();

        try {
            finish.await(1, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            //
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
