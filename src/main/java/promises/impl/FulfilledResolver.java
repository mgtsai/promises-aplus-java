//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
abstract class FulfilledResolver<VCI, RCI> extends BaseResolver<VCI, RCI>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final FulfilledResolver<Object, Object>
    byNullOnFulfilled = new FulfilledResolver<Object, Object>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final ResolveAction resAction
        ) {
            resAction.setFulfilled(store.value);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff
        ) {
            return factory.fulfilledPromise(store.value);
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final Object onRejected,
            final int onRejStackDiff
        ) {
            return factory.fulfilledPromise(store.value);
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final Object value
        ) {
            return factory.fulfilledPromise(value);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FulfilledResolver<FR1<Object, ?>, Object>
    byNullExecutor = new FulfilledResolver<FR1<Object, ?>, Object>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(store.executor(), onFulfilled, onFulStackDiff, store.value, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff
        ) {
            return null;
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final Object onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onFulfilled, onFulStackDiff, store.value, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final Object value
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onFulfilled, onFulStackDiff, value, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FulfilledResolver<FR1<Object, ?>, Object>
    byNonNull = new FulfilledResolver<FR1<Object, ?>, Object>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(exec, onFulfilled, onFulStackDiff, store.value, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff
        ) {
            return store.inSyncNewMutablePromise(factory, exec, this, onFulfilled, onFulStackDiff, null, null, 0);
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final Object onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onFulfilled, onFulStackDiff, store.value, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int onFulStackDiff,
            final Object value
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onFulfilled, onFulStackDiff, value, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static void doResolve(
        final FR1<Object, ?> onFulfilled,
        final int onFulStackDiff,
        final Object value,
        final ResolveAction resAction
    ) {
        try {
            resolveValue(onFulfilled.call(value), resAction);
        } catch (final Throwable e) {
            resAction.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void doExecAndResolve(
        final Executor exec,
        final FR1<Object, ?> onFulfilled,
        final int onFulStackDiff,
        final Object value,
        final ResolveAction resAction
    ) {
        exec.execute(new Runnable() { @Override public void run() {
            doResolve(onFulfilled, onFulStackDiff, value, resAction);
        }});
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <VI, RCI> FulfilledResolver<FR1<? super VI, ?>, RCI>
    of(final Executor exec, final FR1<? super VI, ?> onFulfilled)
    {
        if (onFulfilled == null)
            return ImplUtil.cast(byNullOnFulfilled);
        else if (exec == null)
            return ImplUtil.cast(byNullExecutor);
        else
            return ImplUtil.cast(byNonNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract void execAndResolve(
        final PromiseStore store,
        final Executor exec,
        final VCI onFulfilled,
        final int onFulStackDiff,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO inSyncNonBlockingChainDstPromise(
        final PromiseStore store,
        final PromiseFactory<PO> factory,
        final Executor exec,
        final VCI onFulfilled,
        final int onFulStackDiff
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO chainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final VCI onFulfilled,
        final int onFulStackDiff,
        final Object value
    );
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
