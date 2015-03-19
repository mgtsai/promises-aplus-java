//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import promises.PromiseRejectedException;
import promises.PromiseState;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
abstract class BasePromiseImpl
{
    //-----------------------------------------------------------------------------------------------------------------
    final Object syncLock;
    //-----------------------------------------------------------------------------------------------------------------
    static Executor executor(final Executor exec)
    {
        return exec != null ? exec : ImplUtil.CURRENT_THREAD_EXECUTOR;
    }
    //-----------------------------------------------------------------------------------------------------------------
    BasePromiseImpl(final Object syncLock)
    {
        this.syncLock = syncLock;
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract PromiseState state();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object getValue();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object getReason();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Throwable exception();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object doAwait() throws InterruptedException, PromiseRejectedException;
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object doAwait(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    abstract boolean inSyncIsAlwaysPending();
    //-----------------------------------------------------------------------------------------------------------------
    void inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask)
    {
        throw new InternalException("Not allowed to append tasks to pending queue");
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <PO extends BasePromiseImpl> ResolvingTask<PO>
    inSyncNewResolvedTask(final PromiseFactory<PO> factory, final ResolutionSupplier resSupp)
    {
        return new ResolvingTask<PO>(resSupp) {
            private PromiseStore store = null;
            private PO promise = null;

            @Override final PO chainDstPromise() { return promise; }
            @Override final void onAlwaysPending() { throw new InternalException("Unexpected OnAlwaysPending"); }

            @Override final synchronized void afterExec() {
                if (promise == null) {
                    store = new PromiseStore();
                    promise = factory.pendingPromise(store);
                }
            }

            @Override final void fulfillChainDstPromise(final Object vo) {
                synchronized (this) {
                    if (store == null) {
                        promise = factory.fulfilledPromise(vo);
                        return;
                    }
                }

                store.doFulfill(vo);
            }

            @Override final void rejectChainDstPromise(final Object ro, final Throwable eo) {
                synchronized (this) {
                    if (store == null) {
                        promise = factory.rejectedPromise(ro, eo);
                        return;
                    }
                }

                store.doReject(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvingTask<BasePromiseImpl>
    inSyncNewPendingTask(final PromiseStore store, final ResolutionSupplier resSupp)
    {
        return new ResolvingTask<BasePromiseImpl>(resSupp) {
            @Override final BasePromiseImpl chainDstPromise() { return null; }
            @Override final void onAlwaysPending() { store.setAlwaysPending(); }
            @Override final void afterExec() { }
            @Override final void fulfillChainDstPromise(final Object vo) {
                store.doFulfill(vo);
            }

            @Override final void rejectChainDstPromise(final Object ro, final Throwable eo) {
                store.doReject(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask
    inSyncNewOnFulfilledPendingTask(final PromiseStore store, final ResolutionSupplier onFulfilledResSupp)
    {
        if (onFulfilledResSupp != null)
            return inSyncNewPendingTask(store, onFulfilledResSupp);
        else
            return new BaseTask() {
                @Override final void onAlwaysPending() { store.setAlwaysPending(); }
                @Override final void doExec() { store.doFulfill(getValue()); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask
    inSyncNewOnRejectedPendingTask(final PromiseStore store, final ResolutionSupplier onRejectedResSupp)
    {
        if (onRejectedResSupp != null)
            return inSyncNewPendingTask(store, onRejectedResSupp);
        else
            return new BaseTask() {
                @Override final void onAlwaysPending() { store.setAlwaysPending(); }
                @Override final void doExec() { store.doReject(getReason(), exception()); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    final <PO extends BasePromiseImpl> PO doThen(
        final PromiseFactory<PO> factory,
        final ResolutionSupplier onFulfilledResSupp,
        final ResolutionSupplier onRejectedResSupp
    ) {
        final ChainingTask<PO> resolvedTask;

        synchronized (syncLock) {
            if (inSyncIsAlwaysPending())
                return factory.alwaysPendingPromise();

            final PromiseState state = this.state();

            switch (state) {
            case PENDING:
                final PromiseStore store = new PromiseStore();

                inSyncAppendTasksToPendingQueue(
                    inSyncNewOnFulfilledPendingTask(store, onFulfilledResSupp),
                    inSyncNewOnRejectedPendingTask(store, onRejectedResSupp)
                );

                return factory.pendingPromise(store);

            case FULFILLED:
                if (onFulfilledResSupp != null) {
                    resolvedTask = inSyncNewResolvedTask(factory, onFulfilledResSupp);
                    break;
                } else
                    return factory.fulfilledPromise(getValue());

            case REJECTED:
                if (onRejectedResSupp != null) {
                    resolvedTask = inSyncNewResolvedTask(factory, onRejectedResSupp);
                    break;
                } else
                    return factory.rejectedPromise(getReason(), exception());

            default:
                throw new InternalException("Unknown state %s", state);
            }
        }

        resolvedTask.doExec();
        return resolvedTask.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask inSyncNewOnFulfilledResolvingTask(final ResolvingTask<?> resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.fulfillChainDstPromise(getValue()); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask inSyncNewOnRejectedResolvingTask(final ResolvingTask<?> resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.rejectChainDstPromise(getReason(), exception()); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void resolveDestination(final ResolvingTask<?> resDstTask)
    {
        final boolean isAlwaysPending;
        final BaseTask resolvedTask;

        synchronized (syncLock) {
            if (isAlwaysPending = inSyncIsAlwaysPending())
                resolvedTask = null;
            else {
                final PromiseState state = this.state();

                switch (state) {
                case PENDING:
                    inSyncAppendTasksToPendingQueue(
                        inSyncNewOnFulfilledResolvingTask(resDstTask),
                        inSyncNewOnRejectedResolvingTask(resDstTask)
                    );

                    return;

                case FULFILLED:
                    resolvedTask = inSyncNewOnFulfilledResolvingTask(resDstTask);
                    break;

                case REJECTED:
                    resolvedTask = inSyncNewOnRejectedResolvingTask(resDstTask);
                    break;

                default:
                    throw new InternalException("Unknown state %s", state);
                }
            }
        }

        if (!isAlwaysPending)
            resolvedTask.doExec();
        else
            resDstTask.onAlwaysPending();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
