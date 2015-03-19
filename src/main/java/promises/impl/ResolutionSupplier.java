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
abstract class ResolutionSupplier
{
    //-----------------------------------------------------------------------------------------------------------------
    final Executor exec;
    //-----------------------------------------------------------------------------------------------------------------
    static <V> ResolutionSupplier byOnFullfilled(
        final BasePromiseImpl promise,
        final Executor exec,
        final FR1<V, ?> onFulfilled,
        final int stackDiff
    ) {
        if (onFulfilled == null)
            return null;

        return new ResolutionSupplier(exec) { @Override final Object resValue() throws Throwable {
            return onFulfilled.call(ImplUtil.<V>cast(promise.getValue()));
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <R> ResolutionSupplier byOnRejected(
        final BasePromiseImpl promise,
        final Executor exec,
        final FR2<R, Throwable, ?> onRejected,
        final int stackDiff
    ) {
        if (onRejected == null)
            return null;

        return new ResolutionSupplier(exec) { @Override final Object resValue() throws Throwable {
            return onRejected.call(ImplUtil.<R>cast(promise.getReason()), promise.exception());
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    static ResolutionSupplier byOnRejected(
        final BasePromiseImpl promise,
        final Executor exec,
        final FR1<Throwable, ?> onRejected,
        final int stackDiff
    ) {
        if (onRejected == null)
            return null;

        return new ResolutionSupplier(exec) { @Override final Object resValue() throws Throwable {
            return onRejected.call(promise.exception());
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private ResolutionSupplier(final Executor exec)
    {
        this.exec = exec;
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object resValue() throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
