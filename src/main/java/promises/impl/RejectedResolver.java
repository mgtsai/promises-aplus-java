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
abstract class RejectedResolver<CI> extends BaseResolver
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<FR2<Object, Throwable, ?>>
    byNonNull = new RejectedResolver<FR2<Object, Throwable, ?>>() {
        @Override void resolve(
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public void run() {
                try {
                    resolveValue(onRejected.call(reason, exception), resAction);
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            resolve(exec, onRejected, stackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<FR1<Throwable, ?>>
    byNonNullLightWeight = new RejectedResolver<FR1<Throwable, ?>>() {
        @Override void resolve(
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public void run() {
                try {
                    resolveValue(onRejected.call(exception), resAction);
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<PO> chain = new ResolvedChain<PO>(factory);
            resolve(exec, onRejected, stackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final RejectedResolver<Object> byNull = new RejectedResolver<Object>() {
        @Override void resolve(
            final Executor exec,
            final Object onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            resAction.setRejected(reason, exception);
        }

        @Override <PO> PO chainDstPromise(
            final PromiseFactory<PO> factory,
            final Executor exec,
            final Object onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception
        ) {
            return factory.rejectedPromise(reason, exception);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static <RI> RejectedResolver<FR2<? super RI, Throwable, ?>> of(final FR2<? super RI, Throwable, ?> onRejected)
    {
        if (onRejected != null)
            return ImplUtil.cast(byNonNull);
        else
            return ImplUtil.cast(byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    static RejectedResolver<FR1<Throwable, ?>> of(final FR1<Throwable, ?> onRejected)
    {
        if (onRejected != null)
            return byNonNullLightWeight;
        else
            return ImplUtil.cast(byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract void resolve(
        final Executor exec,
        final CI onRejected,
        final int stackDiff,
        final Object reason,
        final Throwable exception,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract <PO> PO chainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final CI onRejected,
        final int stackDiff,
        final Object reason,
        final Throwable exception
    );
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
