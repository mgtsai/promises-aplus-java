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
abstract class RejectedResolver<VCI, RCI> extends BaseResolver<VCI, RCI>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object, Object>
    byNullOnRejected = new RejectedResolver<Object, Object>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final Object onRejected,
            final int onRejStackDiff,
            final ResolveAction resAction
        ) {
            resAction.setRejected(store.reason, store.exception);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onRejected,
            final int onRejStackDiff
        ) {
            return factory.rejectedPromise(store.reason, store.exception);
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
            return factory.rejectedPromise(store.reason, store.exception);
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onRejected,
            final int onRejStackDiff,
            final Object reason,
            final Throwable exception
        ) {
            return factory.rejectedPromise(reason, exception);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object, FR2<Object, Throwable, ?>>
    byNullExecutor = new RejectedResolver<Object, FR2<Object, Throwable, ?>>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(store.executor(), onRejected, onRejStackDiff, store.reason, store.exception, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            return null;
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onRejected, onRejStackDiff, store.reason, store.exception, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onRejected, onRejStackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object, FR2<Object, Throwable, ?>>
    byNonNull = new RejectedResolver<Object, FR2<Object, Throwable, ?>>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(exec, onRejected, onRejStackDiff, store.reason, store.exception, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            return store.inSyncNewMutablePromise(factory, exec, null, null, 0, this, onRejected, onRejStackDiff);
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onRejected, onRejStackDiff, store.reason, store.exception, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int onRejStackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onRejected, onRejStackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object, FR1<Throwable, ?>>
    byLightWeightNullExecutor = new RejectedResolver<Object, FR1<Throwable, ?>>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(store.executor(), onRejected, onRejStackDiff, store.exception, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            return null;
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onRejected, onRejStackDiff, store.exception, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doResolve(onRejected, onRejStackDiff, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object, FR1<Throwable, ?>>
    byLightWeightNonNull = new RejectedResolver<Object, FR1<Throwable, ?>>() {
        @Override void execAndResolve(
            final PromiseStore store,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff,
            final ResolveAction resAction
        ) {
            doExecAndResolve(exec, onRejected, onRejStackDiff, store.exception, resAction);
        }

        @Override <PO> PO inSyncNonBlockingChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            return store.inSyncNewMutablePromise(factory, exec, null, null, 0, this, onRejected, onRejStackDiff);
        }

        @Override <PO> PO delayedChainDstPromise(
            final PromiseStore store,
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onFulfilled,
            final int onFulStackDiff,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onRejected, onRejStackDiff, store.exception, chain);
            return chain.dstPromise();
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int onRejStackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            doExecAndResolve(exec, onRejected, onRejStackDiff, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static void doResolve(
        final FR2<Object, Throwable, ?> onRejected,
        final int onRejStackDiff,
        final Object reason,
        final Throwable exception,
        final ResolveAction resAction
    ) {
        try {
            resolveValue(onRejected.call(reason, exception), resAction);
        } catch (final Throwable e) {
            resAction.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void doExecAndResolve(
        final Executor exec,
        final FR2<Object, Throwable, ?> onRejected,
        final int onRejStackDiff,
        final Object reason,
        final Throwable exception,
        final ResolveAction resAction
    ) {
        exec.execute(new Runnable() { @Override public void run() {
            doResolve(onRejected, onRejStackDiff, reason, exception, resAction);
        }});
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <VCI, RI> RejectedResolver<VCI, FR2<? super RI, Throwable, ?>>
    of(final Executor exec, final FR2<? super RI, Throwable, ?> onRejected)
    {
        if (onRejected == null)
            return ImplUtil.cast(byNullOnRejected);
        else if (exec == null)
            return ImplUtil.cast(byNullExecutor);
        else
            return ImplUtil.cast(byNonNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void doResolve(
        final FR1<Throwable, ?> onRejected,
        final int onRejStackDiff,
        final Throwable exception,
        final ResolveAction resAction
    ) {
        try {
            resolveValue(onRejected.call(exception), resAction);
        } catch (final Throwable e) {
            resAction.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void doExecAndResolve(
        final Executor exec,
        final FR1<Throwable, ?> onRejected,
        final int onRejStackDiff,
        final Throwable exception,
        final ResolveAction resAction
    ) {
        exec.execute(new Runnable() { @Override public void run() {
            doResolve(onRejected, onRejStackDiff, exception, resAction);
        }});
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <VCI> RejectedResolver<VCI, FR1<Throwable, ?>> of(final Executor exec, final FR1<Throwable, ?> onRejected)
    {
        if (onRejected == null)
            return ImplUtil.cast(byNullOnRejected);
        else if (exec == null)
            return ImplUtil.cast(byLightWeightNullExecutor);
        else
            return ImplUtil.cast(byLightWeightNonNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract void execAndResolve(
        final PromiseStore store,
        final Executor exec,
        final RCI onRejected,
        final int onRejStackDiff,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO inSyncNonBlockingChainDstPromise(
        final PromiseStore store,
        final PromiseFactory<PO> factory,
        final Executor exec,
        final RCI onRejected,
        final int onRejStackDiff
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO chainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final RCI onRejected,
        final int onRejStackDiff,
        final Object reason,
        final Throwable exception
    );
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
