//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
final class BlockingCommandQueue extends ArrayList<Runnable> implements Executor
{
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void execute(@Nonnull final Runnable command)
    {
        add(command);
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void runBlockingCommands()
    {
        for (final Runnable command : this)
            command.run();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
