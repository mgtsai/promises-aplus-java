//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.FR1;
import promises.FR2;
import promises.InternalException;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.lw.OnFul;
import promises.lw.OnRej;
import promises.lw.P;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
abstract class AbstractPromise<V, R> implements promises.Promise, promises.typed.Promise<V, R>, P<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    final Object syncLock = new Object();
    //-----------------------------------------------------------------------------------------------------------------
    private static Executor executor(final Executor exec)
    {
        return exec != null ? exec : ImplUtil.CURRENT_THREAD_EXECUTOR;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public abstract V value();
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public abstract R reason();
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public abstract V await() throws InterruptedException, PromiseRejectedException;
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public abstract V await(final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final promises.Promise toUntypedPromise()
    {
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public final promises.typed.Promise<V, R> toTypedPromise()
    {
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public final P<V> toLightWeightPromise()
    {
        return this;
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract boolean inSyncIsAlwaysPending();
    //-----------------------------------------------------------------------------------------------------------------
    void inSyncAppendTasksToPendingQueue(final BaseTask onFulfilledTask, final BaseTask onRejectedTask)
    {
        throw new InternalException("Not allowed to append tasks to pending queue");
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <VO, RO> ResolvingTask<VO, RO> inSyncNewResolvedTask(final ResolutionSupplier resSupp)
    {
        return new ResolvingTask<VO, RO>(resSupp) {
            private AbstractPromise<VO, RO> chainDstPromise = null;
            private MutablePromise<VO, RO> pendingPromise = null;

            @Override final AbstractPromise<VO, RO> chainDstPromise() { return chainDstPromise; }
            @Override final void onAlwaysPending() { throw new InternalException("Unexpected OnAlwaysPending"); }

            @Override final synchronized void afterExec() {
                if (chainDstPromise == null)
                    chainDstPromise = pendingPromise = new MutablePromise<VO, RO>();
            }

            @Override final void fulfillChainDstPromise(final VO vo) {
                synchronized (this) {
                    if (pendingPromise == null) {
                        chainDstPromise = PromiseFactory.fulfilledPromise(vo);
                        return;
                    }
                }

                pendingPromise.doFulfill(vo);
            }

            @Override final void rejectChainDstPromise(final RO ro, final Throwable eo) {
                synchronized (this) {
                    if (pendingPromise == null) {
                        chainDstPromise = PromiseFactory.rejectedPromise(ro, eo);
                        return;
                    }
                }

                pendingPromise.doReject(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private <VO, RO> ResolvingTask<VO, RO>
    inSyncNewPendingTask(final MutablePromise<VO, RO> chainDstPromise, final ResolutionSupplier resSupp)
    {
        return new ResolvingTask<VO, RO>(resSupp) {
            @Override final AbstractPromise<VO, RO> chainDstPromise() { return chainDstPromise; }
            @Override final void onAlwaysPending() { chainDstPromise.setAlwaysPending(); }
            @Override final void afterExec() { }

            @Override final void fulfillChainDstPromise(final VO vo) {
                chainDstPromise.doFulfill(vo);
            }

            @Override final void rejectChainDstPromise(final RO ro, final Throwable eo) {
                chainDstPromise.doReject(ro, eo);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private <VO, RO> BaseTask inSyncNewOnFulfilledPendingTask(
        final MutablePromise<VO, RO> chainDstPromise,
        final ResolutionSupplier onFulfilledResSupp
    ) {
        if (onFulfilledResSupp != null)
            return inSyncNewPendingTask(chainDstPromise, onFulfilledResSupp);
        else
            return new BaseTask() {
                @Override final void onAlwaysPending() { chainDstPromise.setAlwaysPending(); }
                @Override final void doExec() { chainDstPromise.doFulfill(ImplUtil.<VO>cast(value())); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private <VO, RO> BaseTask inSyncNewOnRejectedPendingTask(
        final MutablePromise<VO, RO> chainDstPromise,
        final ResolutionSupplier onRejectedResSupp
    ) {
        if (onRejectedResSupp != null)
            return inSyncNewPendingTask(chainDstPromise, onRejectedResSupp);
        else
            return new BaseTask() {
                @Override final void onAlwaysPending() { chainDstPromise.setAlwaysPending(); }
                @Override final void doExec() { chainDstPromise.doReject(ImplUtil.<RO>cast(reason()), exception()); }
            };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private <VO, RO> AbstractPromise<VO, RO>
    doThen(final ResolutionSupplier onFulfilledResSupp, final ResolutionSupplier onRejectedResSupp)
    {
        final ChainingTask<VO, RO> resolvedTask;

        synchronized (syncLock) {
            if (inSyncIsAlwaysPending())
                return PromiseFactory.alwaysPendingPromise();

            final PromiseState state = this.state();

            switch (state) {
            case PENDING:
                final MutablePromise<VO, RO> chainDstPromise = new MutablePromise<VO, RO>();

                inSyncAppendTasksToPendingQueue(
                    inSyncNewOnFulfilledPendingTask(chainDstPromise, onFulfilledResSupp),
                    inSyncNewOnRejectedPendingTask(chainDstPromise, onRejectedResSupp)
                );

                return chainDstPromise;

            case FULFILLED:
                if (onFulfilledResSupp != null) {
                    resolvedTask = inSyncNewResolvedTask(onFulfilledResSupp);
                    break;
                } else
                    return PromiseFactory.fulfilledPromise(ImplUtil.<VO>cast(value()));

            case REJECTED:
                if (onRejectedResSupp != null) {
                    resolvedTask = inSyncNewResolvedTask(onRejectedResSupp);
                    break;
                } else
                    return PromiseFactory.rejectedPromise(ImplUtil.<RO>cast(reason()), exception());

            default:
                throw new InternalException("Unknown state %s", state);
            }
        }

        resolvedTask.doExec();
        return resolvedTask.chainDstPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask inSyncNewOnFulfilledResolvingTask(final ResolvingTask<V, R> resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.fulfillChainDstPromise(value()); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private BaseTask inSyncNewOnRejectedResolvingTask(final ResolvingTask<V, R> resDstTask)
    {
        return new BaseTask() {
            @Override final void onAlwaysPending() { resDstTask.onAlwaysPending(); }
            @Override final void doExec() { resDstTask.rejectChainDstPromise(reason(), exception()); }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void resolveDestination(final ResolvingTask<V, R> resDstTask)
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
    @Override
    public final promises.Promise
    then(final Executor exec, final promises.OnFulfilled<?> onFulfilled, final promises.OnRejected<?> onRejected)
    {
        final Executor actualExec = executor(exec);

        return doThen(
            ResolutionSupplier.byOnFullfilled(this, actualExec, ImplUtil.<FR1<V, ?>>cast(onFulfilled), 0),
            ResolutionSupplier.byOnRejected(this, actualExec, ImplUtil.<FR2<R, Throwable, ?>>cast(onRejected), 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final promises.Promise then(final Executor exec, promises.OnFulfilled<?> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, executor(exec), ImplUtil.<FR1<V, ?>>cast(onFulfilled), 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final promises.Promise
    then(final promises.OnFulfilled<?> onFulfilled, final promises.OnRejected<?> onRejected)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, ImplUtil.<FR1<V, ?>>cast(onFulfilled), 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, ImplUtil.<FR2<R, Throwable, ?>>cast(onRejected), 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final promises.Promise then(final promises.OnFulfilled<?> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, ImplUtil.<FR1<V, ?>>cast(onFulfilled), 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> promises.typed.Promise<VO, RO> then(
        final Executor exec,
        final promises.typed.OnFulfilled<? super V, VO, RO> onFulfilled,
        final promises.typed.OnRejected<? super R, VO, RO> onRejected
    ) {
        final Executor actualExec = executor(exec);

        return doThen(
            ResolutionSupplier.byOnFullfilled(this, actualExec, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, actualExec, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> promises.typed.Promise<VO, RO>
    then(final Executor exec, final promises.typed.OnFulfilled<? super V, VO, RO> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, executor(exec), onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> promises.typed.Promise<VO, RO> then(
        final promises.typed.OnFulfilled<? super V, VO, RO> onFulfilled,
        final promises.typed.OnRejected<? super R, VO, RO> onRejected
    ) {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO, RO> promises.typed.Promise<VO, RO>
    then(final promises.typed.OnFulfilled<? super V, VO, RO> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO>
    then(final Executor exec, final OnFul<? super V, VO> onFulfilled, final OnRej<VO> onRejected)
    {
        final Executor actualExec = executor(exec);

        return doThen(
            ResolutionSupplier.byOnFullfilled(this, actualExec, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, actualExec, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final Executor exec, final OnFul<? super V, VO> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, executor(exec), onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final OnFul<? super V, VO> onFulfilled, final OnRej<VO> onRejected)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            ResolutionSupplier.byOnRejected(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onRejected, 0)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final <VO> P<VO> then(final OnFul<? super V, VO> onFulfilled)
    {
        return doThen(
            ResolutionSupplier.byOnFullfilled(this, ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, 0),
            null
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
