//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl.store;
import promises.PromiseState;
import promises.TypeErrorException;
import promises.impl.BasePromiseImpl;
import promises.impl.ImplUtil;
import promises.impl.LightWeightPromiseImpl;
import promises.impl.TypedPromiseImpl;
import promises.impl.UntypedPromiseImpl;
import promises.lw.OnFul;
import promises.lw.OnRej;
import promises.lw.P;
import promises.lw.RV;
import promises.lw.RejP;
import promises.lw.ResP;
import promises.typed.Resolution;
//---------------------------------------------------------------------------------------------------------------------
public abstract class ResolveAction
{
    //-----------------------------------------------------------------------------------------------------------------
    abstract void setAlwaysPending();
    //-----------------------------------------------------------------------------------------------------------------
    abstract void setFulfilled(final Object value);
    //-----------------------------------------------------------------------------------------------------------------
    abstract void setRejected(final Object reason, final Throwable exception);
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByUntypedPromise(final promises.Promise promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.OnFulfilled<Object>() { @Override public final Object call(final Object value) {
                if (onePass.pass())
                    setFulfilled(value);
                return UntypedPromiseImpl.factory.alwaysPendingPromise();
            }},
            new promises.OnRejected<Object>() {
                @Override public final Object call(final Object reason, final Throwable exception) {
                    if (onePass.pass())
                        setRejected(reason, exception);
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByTypedPromise(final promises.typed.Promise<?, ?> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new promises.typed.OnFulfilled<Object, Object, Object>() {
                @Override public final Resolution<?, ?> call(final Object value) {
                    if (onePass.pass())
                        setFulfilled(value);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            },
            new promises.typed.OnRejected<Object, Object, Object>() {
                @Override public final Resolution<?, ?> call(final Object reason, final Throwable exception) {
                    if (onePass.pass())
                        setRejected(reason, exception);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByLightWeightPromise(final P<?> promise)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            new OnFul<Object, Object>() { @Override public final RV<?> call(final Object value) {
                if (onePass.pass())
                    setFulfilled(value);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
            }},
            new OnRej<Object>() { @Override public final RV<?> call(final Throwable exception) {
                if (onePass.pass())
                    setRejected(null, exception);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByUntypedThenable(final promises.Thenable thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.ResolvePromise() { @Override public final void resolve(final Object value) {
                    if (onePass.pass())
                        ResolveAction.this.resolve(value);
                }},
                new promises.RejectPromise() {
                    @Override public final void reject(final Object reason, final Throwable exception) {
                        if (onePass.pass())
                            setRejected(reason, exception);
                    }

                    @Override public final void reject(final Object reason) {
                        if (onePass.pass())
                            setRejected(reason, null);
                    }

                    @Override public final void reject(final Throwable exception) {
                        if (onePass.pass())
                            setRejected(null, exception);
                    }
                }
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByTypedThenable(final promises.typed.Thenable<Object, Object> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.typed.ResolvePromise<Object, Object>() {
                    @Override public final void resolve(final Resolution<?, ?> res) {
                        if (onePass.pass())
                            ResolveAction.this.resolve(res);
                    }

                    @Override public final void resolve(final Object value) {
                        if (onePass.pass())
                            setFulfilled(value);
                    }
                },
                new promises.typed.RejectPromise<Object>() {
                    @Override public final void reject(final Object reason, final Throwable exception) {
                        if (onePass.pass())
                            setRejected(reason, exception);
                    }

                    @Override public final void reject(final Object reason) {
                        if (onePass.pass())
                            setRejected(reason, null);
                    }

                    @Override public final void reject(final Throwable exception) {
                        if (onePass.pass())
                            setRejected(null, exception);
                    }
                }
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByLightWeightThenable(final promises.lw.Thenable<Object> thenable)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new ResP<Object>() {
                    @Override public final void resolve(final RV<?> res) {
                        if (onePass.pass())
                            ResolveAction.this.resolve(res);
                    }

                    @Override public final void resolve(final Object value) {
                        if (onePass.pass())
                            setFulfilled(value);
                    }
                },
                new RejP() { @Override public final void reject(final Throwable exception) {
                    if (onePass.pass())
                        setRejected(null, exception);
                }}
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void resolveByResolution(final Resolution<?, ?> res)
    {
        final PromiseState state = res.state();

        if (state == null) {
            setRejected(null, new NullPointerException("Null resolution state"));
            return;
        }

        switch (state) {
        case FULFILLED:
            setFulfilled(res.value());
            return;

        case REJECTED:
            setRejected(res.reason(), res.exception());
            return;

        default:
            setRejected(null, new TypeErrorException("Invalid resolution state %s", state));
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    final void resolve(final Object value)
    {
        if (value instanceof BasePromiseImpl) {
            ((BasePromiseImpl) value).applyResolveAction(this);
            return;
        }

        if (value instanceof promises.Promise) {
            resolveByUntypedPromise((promises.Promise) value);
            return;
        }

        if (value instanceof promises.typed.Promise) {
            resolveByTypedPromise((promises.typed.Promise<?, ?>) value);
            return;
        }

        if (value instanceof P) {
            resolveByLightWeightPromise((P<?>) value);
            return;
        }

        if (value instanceof promises.Thenable) {
            resolveByUntypedThenable((promises.Thenable) value);
            return;
        }

        if (value instanceof promises.typed.Thenable) {
            resolveByTypedThenable(ImplUtil.<promises.typed.Thenable<Object, Object>>cast(value));
            return;
        }

        if (value instanceof promises.lw.Thenable) {
            resolveByLightWeightThenable(ImplUtil.<promises.lw.Thenable<Object>>cast(value));
            return;
        }

        if (value instanceof Resolution) {
            resolveByResolution((Resolution<?, ?>) value);
            return;
        }

        if (value instanceof RV) {
            setFulfilled(((RV<?>) value).value());
            return;
        }

        setFulfilled(value);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static final class OnePass
    {
        //-------------------------------------------------------------------------------------------------------------
        private boolean isPassed = false;
        //-------------------------------------------------------------------------------------------------------------
        final synchronized boolean pass()
        {
            return !isPassed && (isPassed = true);
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
