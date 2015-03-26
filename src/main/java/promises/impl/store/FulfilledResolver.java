//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl.store;
import promises.FR1;
import promises.impl.PromiseFactory;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
abstract class FulfilledResolver<VI, PO>
{
    //-----------------------------------------------------------------------------------------------------------------
    static final FulfilledResolver<Object, Object> byNonNull = new FulfilledResolver<Object, Object>() {
        @Override final void resolve(
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int stackDiff,
            final Object value,
            final ResolveAction resAction
        ) {
            exec.execute(new Runnable() { @Override public final void run() {
                try {
                    resAction.resolve(onFulfilled.call(value));
                } catch (final Throwable e) {
                    resAction.setRejected(null, e);
                }
            }});
        }

        @Override final Object chainDstPromise(
            final PromiseFactory<Object> factory,
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int stackDiff,
            final Object value
        ) {
            final ResolvedChain<Object> chain = new ResolvedChain<Object>(factory);
            resolve(exec, onFulfilled, stackDiff, value, chain);
            return chain.dstPromise();
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static final FulfilledResolver<Object, Object> byNull = new FulfilledResolver<Object, Object>() {
        @Override final void resolve(
            final Executor exec,
            final FR1<Object, ?> onFulfilled,
            final int stackDiff,
            final Object value,
            final ResolveAction resAction
        ) {
            resAction.setFulfilled(value);
        }

        @Override final Object chainDstPromise(
            final PromiseFactory<Object> factory,
            final Executor exec,
            final FR1<Object, ?> callback,
            final int stackDiff,
            final Object value
        ) {
            return factory.fulfilledPromise(value);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    abstract void resolve(
        final Executor exec,
        final FR1<VI, ?> onFulfilled,
        final int stackDiff,
        final Object value,
        final ResolveAction resAction
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract PO chainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR1<VI, ?> onFulfilled,
        final int stackDiff,
        final Object value
    );
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
