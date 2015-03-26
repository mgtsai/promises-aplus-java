//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl.store;
import promises.FR1;
import promises.FR2;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.impl.ImplUtil;
import promises.impl.PromiseFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class StoreFacade
{
    //-----------------------------------------------------------------------------------------------------------------
    public static PromiseState state(final PromiseStore store)
    {
        return store.state;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object value(final PromiseStore store)
    {
        return store.value;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object reason(final PromiseStore store)
    {
        return store.reason;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Throwable exception(final PromiseStore store)
    {
        return store.exception;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object await(final Object promise, final PromiseStore store)
        throws InterruptedException, PromiseRejectedException
    {
        return store.await(promise);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object await(final Object promise, final PromiseStore store, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return store.await(promise, timeout, unit);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void applyResolveAction(final PromiseStore store, final ResolveAction resAction)
    {
        store.applyResolveAction(resAction);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <VI, RI, PO> PO doThen(
        final PromiseStore store,
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR1<VI, ?> onFulfilled,
        final int onFulStackDiff,
        final FR2<RI, Throwable, ?> onRejected,
        final int onRejStackDiff
    ) {
        return store.doThen(
            factory, exec,
            StoreFacade.<VI, PO>fulfilledResolver(onFulfilled), onFulfilled, onFulStackDiff,
            StoreFacade.<RI, PO>rejectedResolver(onRejected), onRejected, onRejStackDiff
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <VI, PO> PO doThen(
        final PromiseStore store,
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR1<VI, ?> onFulfilled,
        final int onFulStackDiff,
        final FR1<Throwable, ?> onRejected,
        final int onRejStackDiff
    ) {
        return store.doThen(
            factory, exec,
            StoreFacade.<VI, PO>fulfilledResolver(onFulfilled), onFulfilled, onFulStackDiff,
            StoreFacade.<PO>rejectedResolver(onRejected), onRejected, onRejStackDiff
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <VI, PO> FulfilledResolver<VI, PO> fulfilledResolver(final FR1<VI, ?> onFulfilled)
    {
        if (onFulfilled != null)
            return ImplUtil.cast(FulfilledResolver.byNonNull);
        else
            return ImplUtil.cast(FulfilledResolver.byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <VI, PO> PO fulfilledChainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR1<VI, ?> onFulfilled,
        final int stackDiff,
        final Object value
    ) {
        return StoreFacade.<VI, PO>fulfilledResolver(onFulfilled)
            .chainDstPromise(factory, exec, onFulfilled, stackDiff, value);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <RI, PO> RejectedResolver<FR2<RI, Throwable, ?>, PO>
    rejectedResolver(final FR2<RI, Throwable, ?> onRejected)
    {
        if (onRejected != null)
            return ImplUtil.cast(RejectedResolver.byNonNull);
        else
            return ImplUtil.cast(RejectedResolver.byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <RI, PO> PO rejectedChainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR2<RI, Throwable, ?> onRejected,
        final int stackDiff,
        final Object reason,
        final Throwable exception
    ) {
        return StoreFacade.<RI, PO>rejectedResolver(onRejected)
            .chainDstPromise(factory, exec, onRejected, stackDiff, reason, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <PO> RejectedResolver<FR1<Throwable, ?>, PO> rejectedResolver(final FR1<Throwable, ?> onRejected)
    {
        if (onRejected != null)
            return ImplUtil.cast(RejectedResolver.byNonNullLightWeight);
        else
            return ImplUtil.cast(RejectedResolver.byNull);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static <PO> PO rejectedChainDstPromise(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FR1<Throwable, ?> onRejected,
        final int stackDiff,
        final Object reason,
        final Throwable exception
    ) {
        return StoreFacade.<PO>rejectedResolver(onRejected)
            .chainDstPromise(factory, exec, onRejected, stackDiff, reason, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void setAlwaysPending(final ResolveAction resAction)
    {
        resAction.setAlwaysPending();
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void setFulfilled(final ResolveAction resAction, final Object value)
    {
        resAction.setFulfilled(value);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void setRejected(final ResolveAction resAction, final Object reason, final Throwable exception)
    {
        resAction.setRejected(reason, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
