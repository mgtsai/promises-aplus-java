//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import promises.PromiseRejectedException;
import promises.PromiseState;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
final class PromiseStore implements ResolveAction
{
    //-----------------------------------------------------------------------------------------------------------------
    private final CountDownLatch waitUntilResolved = new CountDownLatch(1);
    boolean isAlwaysPending = false;
    PromiseState state = PromiseState.PENDING;
    Object value = null;
    Object reason = null;
    Throwable exception = null;
    private ArrayList<ResolveAction> pendingActionQ = null;
    //-----------------------------------------------------------------------------------------------------------------
    final Object await(final Object promise) throws InterruptedException, PromiseRejectedException
    {
        waitUntilResolved.await();
        final PromiseState state = this.state;

        switch (state) {
        case FULFILLED:
            return value;
        case REJECTED:
            throw new PromiseRejectedException(promise, reason, exception);
        default:
            throw new InternalException("Invalid state %s for this resolved promise", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final Object await(final Object promise, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        waitUntilResolved.await(timeout, unit);
        final PromiseState state = this.state;

        switch (state) {
        case PENDING:
            throw new TimeoutException("Timeout is reached for waiting this promise being resolved");
        case FULFILLED:
            return value;
        case REJECTED:
            throw new PromiseRejectedException(promise, reason, exception);
        default:
            throw new InternalException("Invalid state %s for this resolved promise", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final synchronized <P> P createPromise(final PromiseFactory<P> factory)
    {
        if (isAlwaysPending)
            return factory.alwaysPendingPromise();

        switch (state) {
        case FULFILLED:
            return factory.fulfilledPromise(value);
        case REJECTED:
            return factory.rejectedPromise(reason, exception);
        case PENDING:
            return factory.mutablePromise(this);
        default:
            throw new InternalException("Unknown state %s", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void inSyncAddToPendingActionQ(final ResolveAction resAction)
    {
        if (pendingActionQ == null)
            pendingActionQ = new ArrayList<ResolveAction>();

        pendingActionQ.add(resAction);
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void applyResolveAction(final ResolveAction resAction)
    {
        synchronized (this) {
            if (!isAlwaysPending && state == PromiseState.PENDING) {
                inSyncAddToPendingActionQ(resAction);
                return;
            }
        }

        switch (state) {
        case PENDING:
            resAction.setAlwaysPending();
            break;
        case FULFILLED:
            resAction.setFulfilled(value);
            break;
        case REJECTED:
            resAction.setRejected(reason, exception);
            break;
        default:
            throw new InternalException("Unknown state %s", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final <VCI, RCI, PO> PO doThen(
        final PromiseFactory<PO> factory,
        final Executor exec,
        final FulfilledResolver<VCI> fulResolver,
        final VCI onFulfilled,
        final int onFulStackDiff,
        final RejectedResolver<RCI> rejResolver,
        final RCI onRejected,
        final int onRejStackDiff
    ) {
        synchronized (this) {
            if (!isAlwaysPending && state == PromiseState.PENDING) {
                final PromiseStore chainDstStore = new PromiseStore();

                inSyncAddToPendingActionQ(new ResolveAction() {
                    @Override public void setAlwaysPending() { chainDstStore.setAlwaysPending(); }

                    @Override public void setFulfilled(final Object value) {
                        fulResolver.execAndResolve(exec, onFulfilled, onFulStackDiff, value, chainDstStore);
                    }

                    @Override public void setRejected(final Object reason, final Throwable exception) {
                        rejResolver.resolve(exec, onRejected, onRejStackDiff, reason, exception, chainDstStore);
                    }
                });

                return factory.mutablePromise(chainDstStore);
            }
        }

        switch (state) {
        case PENDING:
            return factory.alwaysPendingPromise();
        case FULFILLED:
            return fulResolver.chainDstPromise(factory, exec, onFulfilled, onFulStackDiff, value);
        case REJECTED:
            return rejResolver.chainDstPromise(factory, exec, onRejected, onRejStackDiff, reason, exception);
        default:
            throw new InternalException("Unknown state %s", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setAlwaysPending()
    {
        synchronized (this) {
            if (isAlwaysPending)
                throw new InternalException("Not allowed setting always pending more than once");
            if (state != PromiseState.PENDING)
                throw new InternalException("Not allowed setting always pending after this promise is resolved");

            isAlwaysPending = true;
        }

        if (pendingActionQ != null) {
            for (final ResolveAction action : pendingActionQ)
                action.setAlwaysPending();
            pendingActionQ = null;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setFulfilled(final Object value)
    {
        synchronized (this) {
            if (isAlwaysPending)
                throw new InternalException("Unexpected fulfilling this always-pending promise");
            if (state != PromiseState.PENDING)
                throw new InternalException("Not allowed fulfilling this resolved promise");

            this.state = PromiseState.FULFILLED;
            this.value = value;
        }

        waitUntilResolved.countDown();

        if (pendingActionQ != null) {
            for (final ResolveAction action : pendingActionQ)
                action.setFulfilled(value);
            pendingActionQ = null;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void setRejected(final Object reason, final Throwable exception)
    {
        synchronized (this) {
            if (isAlwaysPending)
                throw new InternalException("Unexpected rejecting this always-pending promise");
            if (state != PromiseState.PENDING)
                throw new InternalException("Not allowed rejecting this resolved promise");

            this.state = PromiseState.REJECTED;
            this.reason = reason;
            this.exception = exception;
        }

        waitUntilResolved.countDown();

        if (pendingActionQ != null) {
            for (final ResolveAction action : pendingActionQ)
                action.setRejected(reason, exception);
            pendingActionQ = null;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
