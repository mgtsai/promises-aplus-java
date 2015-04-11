//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
abstract class FulfilledResolver<CI> extends BaseResolver
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final FulfilledResolver<FR1<Object, ?>>
    byNonNull = new FulfilledResolver<FR1<Object, ?>>() {
        @Override void execAndResolve(
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int stackDiff,
            final Object value,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public void run() {
                try {
                    resolveValue(onFulfilled.call(value), resAction);
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int stackDiff,
            final Object value
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            execAndResolve(exec, onFulfilled, stackDiff, value, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FulfilledResolver<Object> byNull = new FulfilledResolver<Object>() {
        @Override void execAndResolve(
            final Executor exec,
            final Object onFulfilled,
            final int stackDiff,
            final Object value,
            final ResolveAction resAction
        ) {
            resAction.setFulfilled(value);
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object callback,
            final int stackDiff,
            final Object value
        ) {
            return factory.fulfilledPromise(value);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static <VI> FulfilledResolver<FR1<? super VI, ?>> of(final FR1<? super VI, ?> onFulfilled)
    {
        if (onFulfilled != null)
            return ImplUtil.cast(byNonNull);
        else
            return ImplUtil.cast(byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract void execAndResolve(
        final Executor exec,
        final CI onFulfilled,
        final int stackDiff,
        final Object value,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO chainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final CI onFulfilled,
        final int stackDiff,
        final Object value
    );
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
