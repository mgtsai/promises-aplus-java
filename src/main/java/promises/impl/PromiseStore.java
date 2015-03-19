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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
final class PromiseStore
{
    //-----------------------------------------------------------------------------------------------------------------
    private final CountDownLatch waitUntilResolved = new CountDownLatch(1);
    PromiseState state = PromiseState.PENDING;
    Object value = null;
    Object reason = null;
    Throwable exception = null;
    boolean inSyncIsAlwaysPending = false;
    private ArrayList<BaseTask> onFulfilledTaskQ = null;
    private ArrayList<BaseTask> onRejectedTaskQ = null;
    //-----------------------------------------------------------------------------------------------------------------
    final Object doAwait() throws InterruptedException, PromiseRejectedException
    {
        waitUntilResolved.await();
        final PromiseState state = this.state;

        switch (state) {
        case FULFILLED:
            return value;
        case REJECTED:
            throw new PromiseRejectedException(this, reason, exception);
        default:
            throw new InternalException("Invalid state %s for this resolved promise", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final Object doAwait(final long timeout, final TimeUnit unit)
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
            throw new PromiseRejectedException(this, reason, exception);
        default:
            throw new InternalException("Invalid state %s for this resolved promise", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final synchronized <P extends BasePromiseImpl> P promiseInterface(final PromiseFactory<P> factory)
    {
        if (inSyncIsAlwaysPending)
            return factory.alwaysPendingPromise();

        switch (state) {
        case PENDING:
            return factory.pendingPromise(this);
        case FULFILLED:
            return factory.fulfilledPromise(value);
        case REJECTED:
            return factory.rejectedPromise(reason, exception);
        default:
            throw new InternalException("Invalid state %s for specifying promise interface", state);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void setAlwaysPending()
    {
        synchronized (this) {
            if (state != PromiseState.PENDING)
                throw new InternalException("Not allowed setting always pending after this promise is resolved.");

            if (inSyncIsAlwaysPending)
                return;

            inSyncIsAlwaysPending = true;
        }

        if (onFulfilledTaskQ != null) {
            for (final BaseTask task : onFulfilledTaskQ)
                task.onAlwaysPending();
            onFulfilledTaskQ = null;
        }

        if (onRejectedTaskQ != null) {
            for (final BaseTask task : onRejectedTaskQ)
                task.onAlwaysPending();
            onRejectedTaskQ = null;
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask)
    {
        if (inSyncIsAlwaysPending)
            return;

        if (onFulfilledTaskQ == null)
            onFulfilledTaskQ = new ArrayList<BaseTask>();
        onFulfilledTaskQ.add(onFulfilledTask);

        if (onRejectedTaskQ == null)
            onRejectedTaskQ = new ArrayList<BaseTask>();
        onRejectedTaskQ.add(onRejectedTask);
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void doFulfill(final Object value)
    {
        synchronized (this) {
            if (inSyncIsAlwaysPending)
                throw new InternalException("Unexpected fulfilling this always-pending promise");

            this.state = PromiseState.FULFILLED;
            this.value = value;

            waitUntilResolved.countDown();
            onRejectedTaskQ = null;

            if (onFulfilledTaskQ == null)
                return;
        }

        for (final BaseTask task : onFulfilledTaskQ)
            task.doExec();

        onFulfilledTaskQ = null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void doReject(final Object reason, final Throwable exception)
    {
        synchronized (this) {
            if (inSyncIsAlwaysPending)
                throw new InternalException("Unexpected rejecting this always-pending promise");

            this.state = PromiseState.REJECTED;
            this.reason = reason;
            this.exception = exception;

            waitUntilResolved.countDown();
            onFulfilledTaskQ = null;

            if (onRejectedTaskQ == null)
                return;
        }

        for (final BaseTask task : onRejectedTaskQ)
            task.doExec();

        onRejectedTaskQ = null;
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
