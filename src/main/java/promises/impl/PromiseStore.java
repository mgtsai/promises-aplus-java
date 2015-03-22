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
    boolean isAlwaysPending = false;
    PromiseState state = PromiseState.PENDING;
    Object value = null;
    Object reason = null;
    Throwable exception = null;
    private ArrayList<BaseTask> onFulfilledTaskQ = null;
    private ArrayList<BaseTask> onRejectedTaskQ = null;
    //-----------------------------------------------------------------------------------------------------------------
    final Object await() throws InterruptedException, PromiseRejectedException
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
    final Object await(final long timeout, final TimeUnit unit)
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
    private void setAlwaysPending()
    {
        synchronized (this) {
            if (state != PromiseState.PENDING)
                throw new InternalException("Not allowed setting always pending after this promise is resolved.");

            if (isAlwaysPending)
                return;

            isAlwaysPending = true;
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
    private void setFulfilled(final Object value)
    {
        synchronized (this) {
            if (isAlwaysPending)
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
    private void setRejected(final Object reason, final Throwable exception)
    {
        synchronized (this) {
            if (isAlwaysPending)
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
    private void inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask)
    {
        if (isAlwaysPending)
            return;

        if (onFulfilledTaskQ == null)
            onFulfilledTaskQ = new ArrayList<BaseTask>();
        onFulfilledTaskQ.add(onFulfilledTask);

        if (onRejectedTaskQ == null)
            onRejectedTaskQ = new ArrayList<BaseTask>();
        onRejectedTaskQ.add(onRejectedTask);
    }
    //-----------------------------------------------------------------------------------------------------------------
    static <PO> ChainingTask<PO> newResolvedTask(final PromiseFactory<PO> factory, final ResolutionSupplier resSupp)
    {
        return new ChainingTask<PO>(resSupp) {
            private PromiseStore chainDstStore = null;
            private PO chainDstPromise = null;

            @Override final PO chainDstPromise() { return chainDstPromise; }
            @Override final void onAlwaysPending() { throw new InternalException("Unexpected OnAlwaysPending"); }

            @Override final synchronized void afterExec() {
                if (chainDstPromise == null) {
                    chainDstStore = new PromiseStore();
                    chainDstPromise = factory.pendingPromise(chainDstStore);
                }
            }

            @Override final void fulfillChainDstPromise(final Object vo) {
                synchronized (this) {
                    if (chainDstStore == null) {
                        chainDstPromise = factory.fulfilledPromise(vo);
                        return;
                    }
                }

                chainDstStore.setFulfilled(vo);
            }

            @Override final void rejectChainDstPromise(final Object ro, final Throwable eo) {
                synchronized (this) {
                    if (chainDstStore == null) {
                        chainDstPromise = factory.rejectedPromise(ro, eo);
                        return;
                    }
                }

                chainDstStore.setRejected(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvingTask newPendingTask(final PromiseStore dstChainStore, final ResolutionSupplier resSupp)
    {
        return new ResolvingTask(resSupp) {
            @Override final void onAlwaysPending() { dstChainStore.setAlwaysPending(); }
            @Override final void afterExec() { }
            @Override final void fulfillChainDstPromise(final Object vo) { dstChainStore.setFulfilled(vo); }

            @Override final void rejectChainDstPromise(final Object ro, final Throwable eo) {
                dstChainStore.setRejected(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask
    newOnFulfilledPendingTask(final PromiseStore dstChainStore, final ResolutionSupplier onFulfilledResSupp)
    {
        if (onFulfilledResSupp != null)
            return newPendingTask(dstChainStore, onFulfilledResSupp);
        else
            return new BaseTask() {
                @Override final void doExec() { dstChainStore.setFulfilled(value); }
                @Override final void onAlwaysPending() { dstChainStore.setAlwaysPending(); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask
    newOnRejectedPendingTask(final PromiseStore dstChainStore, final ResolutionSupplier onRejectedResSupp)
    {
        if (onRejectedResSupp != null)
            return newPendingTask(dstChainStore, onRejectedResSupp);
        else
            return new BaseTask() {
                @Override final void doExec() { dstChainStore.setRejected(reason, exception); }
                @Override final void onAlwaysPending() { dstChainStore.setAlwaysPending(); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    final <PO> PO doThen(
        final PromiseFactory<PO> factory,
        final ResolutionSupplier onFulfilledResSupp,
        final ResolutionSupplier onRejectedResSupp
    ) {
        final ChainingTask<PO> resolvedTask;

        synchronized (this) {
            if (isAlwaysPending)
                return factory.alwaysPendingPromise();

            switch (state) {
            case PENDING:
                final PromiseStore store = new PromiseStore();

                inSyncAppendTasksToPendingQueue(
                    newOnFulfilledPendingTask(store, onFulfilledResSupp),
                    newOnRejectedPendingTask(store, onRejectedResSupp)
                );

                return factory.pendingPromise(store);

            case FULFILLED:
                if (onFulfilledResSupp != null) {
                    resolvedTask = newResolvedTask(factory, onFulfilledResSupp);
                    break;
                } else
                    return factory.fulfilledPromise(value);

            case REJECTED:
                if (onRejectedResSupp != null) {
                    resolvedTask = newResolvedTask(factory, onRejectedResSupp);
                    break;
                } else
                    return factory.rejectedPromise(reason, exception);

            default:
                throw new InternalException("Unknown state %s", state);
            }
        }

        resolvedTask.doExec();
        return resolvedTask.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask newOnFulfilledResolvingTask(final ResolvingTask resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.fulfillChainDstPromise(value); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask newOnRejectedResolvingTask(final ResolvingTask resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.rejectChainDstPromise(reason, exception); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void resolveDestination(final ResolvingTask resDstTask)
    {
        final BaseTask task;

        synchronized (this) {
            if (isAlwaysPending)
                task = null;
            else {
                switch (state) {
                case PENDING:
                    inSyncAppendTasksToPendingQueue(
                        newOnFulfilledResolvingTask(resDstTask),
                        newOnRejectedResolvingTask(resDstTask)
                    );

                    return;

                case FULFILLED:
                    task = newOnFulfilledResolvingTask(resDstTask);
                    break;

                case REJECTED:
                    task = newOnRejectedResolvingTask(resDstTask);
                    break;

                default:
                    throw new InternalException("Unknown state %s", state);
                }
            }
        }

        if (!isAlwaysPending)
            task.doExec();
        else
            resDstTask.onAlwaysPending();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
