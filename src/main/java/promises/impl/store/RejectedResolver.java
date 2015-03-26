//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl.store;
import promises.FR1;
import promises.FR2;
import promises.impl.PromiseFactory;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
abstract class RejectedResolver<CI, PO>
{
    //-----------------------------------------------------------------------------------------------------------------
    static final RejectedResolver<FR2<Object, Throwable, ?>, Object>
    byNonNull = new RejectedResolver<FR2<Object, Throwable, ?>, Object>() {
        @Override final void resolve(
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public final void run() {
                try {
                    resAction.resolve(onRejected.call(reason, exception));
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override final Object chainDstPromise(
            final PromiseFactory<Object> factory,
            final Executor exec,
            final FR2<Object, Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<Object> chain = new ResolvedChain<Object>(factory);
            resolve(exec, onRejected, stackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static final RejectedResolver<FR1<Throwable, ?>, Object>
    byNonNullLightWeight = new RejectedResolver<FR1<Throwable, ?>, Object>() {
        @Override final void resolve(
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public final void run() {
                try {
                    resAction.resolve(onRejected.call(exception));
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override final Object chainDstPromise(
            final PromiseFactory<Object> factory,
            final Executor exec,
            final FR1<Throwable, ?> onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception
        ) {
            final ResolvedChain<Object> chain = new ResolvedChain<Object>(factory);
            resolve(exec, onRejected, stackDiff, reason, exception, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static final RejectedResolver<Object, Object>
    byNull = new RejectedResolver<Object, Object>() {
        @Override final void resolve(
            final Executor exec,
            final Object onRejected,
            final int stackDiff,
            final Object reason,
            final Throwable exception,
            final ResolveAction resAction
        ) {
            resAction.setRejected(reason, exception);
        }

        @Override final Object chainDstPromise(
            final PromiseFactory<Object> factory,
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
    abstract void resolve(
        final Executor exec,
        final CI onRejected,
        final int stackDiff,
        final Object reason,
        final Throwable exception,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract PO chainDstPromise(
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
