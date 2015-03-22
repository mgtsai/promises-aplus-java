//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.FR2;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
abstract class BasePromiseImpl
{
    //-----------------------------------------------------------------------------------------------------------------
    static Executor executor(final Executor exec)
    {
        return exec != null ? exec : ImplUtil.CURRENT_THREAD_EXECUTOR;
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object getValue();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object getReason();
    //-----------------------------------------------------------------------------------------------------------------
    public abstract Throwable exception();
    //-----------------------------------------------------------------------------------------------------------------
    abstract void resolveDestination(final ResolvingTask resDstTask);
    //-----------------------------------------------------------------------------------------------------------------
    final <P> P fulfilledThen(
        final PromiseFactory<P> factory,
        final Executor exec,
        final FR1<?, ?> onFulfilled,
        final int stackDiff
    ) {
        if (onFulfilled == null)
            return factory.fulfilledPromise(getValue());

        final ChainingTask<P> task = PromiseStore.newResolvedTask(
            factory,
            ResolutionSupplier.byOnFullfilled(this, exec, onFulfilled, 0)
        );

        task.doExec();
        return task.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    final <P> P rejectedThen(
        final PromiseFactory<P> factory,
        final Executor exec,
        final FR2<?, Throwable, ?> onRejected,
        final int stackDiff
    ) {
        if (onRejected == null)
            return factory.rejectedPromise(getReason(), exception());

        final ChainingTask<P> task = PromiseStore.newResolvedTask(
            factory,
            ResolutionSupplier.byOnRejected(this, exec, onRejected, 0)
        );

        task.doExec();
        return task.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    final <P> P rejectedThen(
        final PromiseFactory<P> factory,
        final Executor exec,
        final FR1<Throwable, ?> onRejected,
        final int stackDiff
    ) {
        if (onRejected == null)
            return factory.rejectedPromise(getReason(), exception());

        final ChainingTask<P> task = PromiseStore.newResolvedTask(
            factory,
            ResolutionSupplier.byOnRejected(this, exec, onRejected, 0)
        );

        task.doExec();
        return task.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
