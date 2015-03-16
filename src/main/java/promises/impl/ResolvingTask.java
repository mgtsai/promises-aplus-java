//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import promises.InternalException;
import promises.PromiseState;
import promises.TypeErrorException;
import promises.lw.OnFul;
import promises.lw.OnRej;
import promises.lw.P;
import promises.lw.RV;
import promises.lw.RejP;
import promises.lw.ResP;
import promises.typed.Resolution;
//---------------------------------------------------------------------------------------------------------------------
abstract class ResolvingTask<VO, RO> extends ChainingTask<VO, RO> implements Runnable
{
    //-----------------------------------------------------------------------------------------------------------------
    private final ResolutionSupplier resSupp;
    //-----------------------------------------------------------------------------------------------------------------
    ResolvingTask(final ResolutionSupplier resSupp)
    {
        this.resSupp = resSupp;
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract void afterExec();
    abstract void fulfillChainDstPromise(final VO vo);
    abstract void rejectChainDstPromise(final RO ro, final Throwable eo);
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByUntypedPromise(final promises.Promise promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.OnFulfilled<VO>() { @Override public final Object call(final VO vo) {
                if (onePass.pass())
                    fulfillChainDstPromise(vo);
                return PromiseFactory.alwaysPendingPromise();
            }},
            new promises.OnRejected<RO>() { @Override public final Object call(final RO ro, final Throwable eo) {
                if (onePass.pass())
                    rejectChainDstPromise(ro, eo);
                return PromiseFactory.alwaysPendingPromise();
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByTypedPromise(final promises.typed.Promise<VO, RO> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.typed.OnFulfilled<VO, Object, Object>() {
                @Override public final Resolution<?, ?> call(final VO vo) {
                    if (onePass.pass())
                        fulfillChainDstPromise(vo);
                    return PromiseFactory.alwaysPendingPromise();
                }
            },
            new promises.typed.OnRejected<RO, Object, Object>() {
                @Override public final Resolution<?, ?> call(final RO ro, final Throwable eo) {
                    if (onePass.pass())
                        rejectChainDstPromise(ro, eo);
                    return PromiseFactory.alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByLightWeightPromise(final P<VO> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new OnFul<VO, Object>() { @Override public final RV<?> call(final VO vo) {
                if (onePass.pass())
                    fulfillChainDstPromise(vo);
                return PromiseFactory.alwaysPendingPromise();
            }},
            new OnRej<Object>() { @Override public final RV<?> call(final Throwable eo) {
                if (onePass.pass())
                    rejectChainDstPromise(null, eo);
                return PromiseFactory.alwaysPendingPromise();
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByUntypedThenable(final promises.Thenable thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.ResolvePromise() { @Override public final void resolve(final Object vo) {
                    if (onePass.pass())
                        resolveChainDstPromise(vo);
                }},
                new promises.RejectPromise() {
                    @Override public final void reject(final Object ro, final Throwable eo) {
                        if (onePass.pass())
                            rejectChainDstPromise(ImplUtil.<RO>cast(ro), eo);
                    }

                    @Override public final void reject(final Object ro) {
                        if (onePass.pass())
                            rejectChainDstPromise(ImplUtil.<RO>cast(ro), null);
                    }

                    @Override public final void reject(final Throwable eo) {
                        if (onePass.pass())
                            rejectChainDstPromise(null, eo);
                    }
                }
            );
        } catch (final Throwable eo) {
            if (onePass.pass())
                rejectChainDstPromise(null, eo);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByTypedThenable(final promises.typed.Thenable<VO, RO> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.typed.ResolvePromise<VO, RO>() {
                    @Override public final void resolve(final Resolution<? extends VO, ? extends RO> res) {
                        if (onePass.pass())
                            resolveChainDstPromise(res);
                    }

                    @Override public final void resolve(final VO vo) {
                        if (onePass.pass())
                            fulfillChainDstPromise(vo);
                    }
                },
                new promises.typed.RejectPromise<RO>() {
                    @Override public final void reject(final RO ro, final Throwable eo) {
                        if (onePass.pass())
                            rejectChainDstPromise(ro, eo);
                    }

                    @Override public final void reject(final RO ro) {
                        if (onePass.pass())
                            rejectChainDstPromise(ro, null);
                    }

                    @Override public final void reject(final Throwable eo) {
                        if (onePass.pass())
                            rejectChainDstPromise(null, eo);
                    }
                }
            );
        } catch (final Throwable eo) {
            if (onePass.pass())
                rejectChainDstPromise(null, eo);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByLightWeightThenable(final promises.lw.Thenable<VO> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new ResP<VO>() {
                    @Override public final void resolve(final RV<? extends VO> res) {
                        if (onePass.pass())
                            resolveChainDstPromise(res);
                    }

                    @Override public final void resolve(final VO vo) {
                        if (onePass.pass())
                            fulfillChainDstPromise(vo);
                    }
                },
                new RejP() { @Override public final void reject(final Throwable eo) {
                    if (onePass.pass())
                        rejectChainDstPromise(null, eo);
                }}
            );
        } catch (final Throwable eo) {
            if (onePass.pass())
                rejectChainDstPromise(null, eo);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByResolution(final Resolution<VO, RO> res)
    {
        final PromiseState state = res.state();

        if (state == null) {
            rejectChainDstPromise(null, new NullPointerException("Null resolution state"));
            return;
        }

        switch (state) {
        case FULFILLED:
            fulfillChainDstPromise(res.value());
            return;

        case REJECTED:
            rejectChainDstPromise(res.reason(), res.exception());
            return;

        default:
            rejectChainDstPromise(null, new TypeErrorException("Invalid resolution state %s", state));
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromise(final Object v)
    {
        if (v instanceof AbstractPromise) {
            ImplUtil.<AbstractPromise<VO, RO>>cast(v).resolveDestination(this);
            return;
        }

        if (v instanceof promises.Promise) {
            resolveChainDstPromiseByUntypedPromise((promises.Promise) v);
            return;
        }

        if (v instanceof promises.typed.Promise) {
            resolveChainDstPromiseByTypedPromise(ImplUtil.<promises.typed.Promise<VO, RO>>cast(v));
            return;
        }

        if (v instanceof P) {
            resolveChainDstPromiseByLightWeightPromise(ImplUtil.<P<VO>>cast(v));
            return;
        }

        if (v instanceof promises.Thenable) {
            resolveChainDstPromiseByUntypedThenable(ImplUtil.<promises.Thenable>cast(v));
            return;
        }

        if (v instanceof promises.typed.Thenable) {
            resolveChainDstPromiseByTypedThenable(ImplUtil.<promises.typed.Thenable<VO, RO>>cast(v));
            return;
        }

        if (v instanceof promises.lw.Thenable) {
            resolveChainDstPromiseByLightWeightThenable(ImplUtil.<promises.lw.Thenable<VO>>cast(v));
            return;
        }

        if (v instanceof Resolution) {
            resolveChainDstPromiseByResolution(ImplUtil.<Resolution<VO, RO>>cast(v));
            return;
        }

        if (v instanceof RV) {
            fulfillChainDstPromise(ImplUtil.<RV<VO>>cast(v).value());
            return;
        }

        fulfillChainDstPromise(ImplUtil.<VO>cast(v));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final void run()
    {
        try {
            resolveChainDstPromise(resSupp.resValue());
        } catch (final Throwable e) {
            if (e instanceof InternalException)
                LoggerManager.singleton().registeredInternalExceptionHandler().onCaught((InternalException) e);
            else
                rejectChainDstPromise(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final void doExec()
    {
        resSupp.exec.execute(this);
        afterExec();
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
