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
abstract class ResolvingTask extends BaseTask implements Runnable
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
    abstract void fulfillChainDstPromise(final Object vo);
    abstract void rejectChainDstPromise(final Object ro, final Throwable eo);
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByUntypedPromise(final promises.Promise promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.OnFulfilled<Object>() { @Override public final Object call(final Object vo) {
                if (onePass.pass())
                    fulfillChainDstPromise(vo);
                return UntypedPromiseImpl.factory.alwaysPendingPromise();
            }},
            new promises.OnRejected<Object>() {
                @Override public final Object call(final Object ro, final Throwable eo) {
                    if (onePass.pass())
                        rejectChainDstPromise(ro, eo);
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByTypedPromise(final promises.typed.Promise<?, ?> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.typed.OnFulfilled<Object, Object, Object>() {
                @Override public final Resolution<?, ?> call(final Object vo) {
                    if (onePass.pass())
                        fulfillChainDstPromise(vo);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            },
            new promises.typed.OnRejected<Object, Object, Object>() {
                @Override public final Resolution<?, ?> call(final Object ro, final Throwable eo) {
                    if (onePass.pass())
                        rejectChainDstPromise(ro, eo);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveChainDstPromiseByLightWeightPromise(final P<?> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new OnFul<Object, Object>() { @Override public final RV<?> call(final Object vo) {
                if (onePass.pass())
                    fulfillChainDstPromise(vo);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
            }},
            new OnRej<Object>() { @Override public final RV<?> call(final Throwable eo) {
                if (onePass.pass())
                    rejectChainDstPromise(null, eo);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
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
                            rejectChainDstPromise(ro, eo);
                    }

                    @Override public final void reject(final Object ro) {
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
    private void resolveChainDstPromiseByTypedThenable(final promises.typed.Thenable<Object, Object> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.typed.ResolvePromise<Object, Object>() {
                    @Override public final void resolve(final Resolution<?, ?> res) {
                        if (onePass.pass())
                            resolveChainDstPromise(res);
                    }

                    @Override public final void resolve(final Object vo) {
                        if (onePass.pass())
                            fulfillChainDstPromise(vo);
                    }
                },
                new promises.typed.RejectPromise<Object>() {
                    @Override public final void reject(final Object ro, final Throwable eo) {
                        if (onePass.pass())
                            rejectChainDstPromise(ro, eo);
                    }

                    @Override public final void reject(final Object ro) {
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
    private void resolveChainDstPromiseByLightWeightThenable(final promises.lw.Thenable<Object> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new ResP<Object>() {
                    @Override public final void resolve(final RV<?> res) {
                        if (onePass.pass())
                            resolveChainDstPromise(res);
                    }

                    @Override public final void resolve(final Object vo) {
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
    private void resolveChainDstPromiseByResolution(final Resolution<?, ?> res)
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
        if (v instanceof BasePromiseImpl) {
            ((BasePromiseImpl) v).resolveDestination(this);
            return;
        }

        if (v instanceof promises.Promise) {
            resolveChainDstPromiseByUntypedPromise((promises.Promise) v);
            return;
        }

        if (v instanceof promises.typed.Promise) {
            resolveChainDstPromiseByTypedPromise((promises.typed.Promise<?, ?>) v);
            return;
        }

        if (v instanceof P) {
            resolveChainDstPromiseByLightWeightPromise((P<?>) v);
            return;
        }

        if (v instanceof promises.Thenable) {
            resolveChainDstPromiseByUntypedThenable((promises.Thenable) v);
            return;
        }

        if (v instanceof promises.typed.Thenable) {
            resolveChainDstPromiseByTypedThenable(ImplUtil.<promises.typed.Thenable<Object, Object>>cast(v));
            return;
        }

        if (v instanceof promises.lw.Thenable) {
            resolveChainDstPromiseByLightWeightThenable(ImplUtil.<promises.lw.Thenable<Object>>cast(v));
            return;
        }

        if (v instanceof Resolution) {
            resolveChainDstPromiseByResolution((Resolution<?, ?>) v);
            return;
        }

        if (v instanceof RV) {
            fulfillChainDstPromise(((RV<?>) v).value());
            return;
        }

        fulfillChainDstPromise(v);
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
