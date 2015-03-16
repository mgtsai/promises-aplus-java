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
final class MutablePromise<V, R> extends AbstractPromise<V, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    private final CountDownLatch waitUntilResolved = new CountDownLatch(1);
    private PromiseState state = PromiseState.PENDING;
    private V value = null;
    private R reason = null;
    private Throwable exception = null;
    private boolean inSyncIsAlwaysPending = false;
    private TaskQueue onFulfilledTaskQ = new TaskQueue();
    private TaskQueue onRejectedTaskQ = new TaskQueue();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final PromiseState state()
    {
        return state;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V value()
    {
        return value;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final R reason()
    {
        return reason;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Throwable exception()
    {
        return exception;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V await() throws PromiseRejectedException, InterruptedException
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
    @Override
    public final V await(final long timeout, final TimeUnit unit)
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
    @Override
    final boolean inSyncIsAlwaysPending()
    {
        return inSyncIsAlwaysPending;
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void setAlwaysPending()
    {
        synchronized (super.syncLock) {
            if (state != PromiseState.PENDING)
                throw new InternalException("Unalways setting always pending after this promise is resolved.");

            if (inSyncIsAlwaysPending)
                return;

            inSyncIsAlwaysPending = true;
        }

        onFulfilledTaskQ.onAllTasksAlwaysPending();
        onRejectedTaskQ.onAllTasksAlwaysPending();
        onFulfilledTaskQ = onRejectedTaskQ = null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override final void
    inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask)
    {
        if (inSyncIsAlwaysPending)
            return;

        onFulfilledTaskQ.add(onFulfilledTask);
        onRejectedTaskQ.add(onRejectedTask);
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void doFulfill(final V value)
    {
        final TaskQueue selectedTaskQ;

        synchronized (super.syncLock) {
            if (inSyncIsAlwaysPending)
                throw new InternalException("Unexpected fulfilling this always-pending promise");

            this.state = PromiseState.FULFILLED;
            this.value = value;
            selectedTaskQ = onFulfilledTaskQ;

            waitUntilResolved.countDown();
            onFulfilledTaskQ = onRejectedTaskQ = null;
        }

        selectedTaskQ.execAllTasks();
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void doReject(final R reason, final Throwable exception)
    {
        final TaskQueue selectedTaskQ;

        synchronized (super.syncLock) {
            if (inSyncIsAlwaysPending)
                throw new InternalException("Unexpected rejecting this always-pending promise");

            this.state = PromiseState.REJECTED;
            this.reason = reason;
            this.exception = exception;
            selectedTaskQ = onRejectedTaskQ;

            waitUntilResolved.countDown();
            onFulfilledTaskQ = onRejectedTaskQ = null;
        }

        selectedTaskQ.execAllTasks();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static final class TaskQueue extends ArrayList<BaseTask>
    {
        //-------------------------------------------------------------------------------------------------------------
        final void onAllTasksAlwaysPending()
        {
            for (final BaseTask task : this)
                task.onAlwaysPending();
        }
        //-------------------------------------------------------------------------------------------------------------
        final void execAllTasks()
        {
            for (final BaseTask task : this)
                task.doExec();
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
