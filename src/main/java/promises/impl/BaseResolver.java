//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
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
class BaseResolver
{
    //-----------------------------------------------------------------------------------------------------------------
    private static void resolveByUntypedPromise(final promises.Promise promise, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new promises.OnFulfilled<Object>() { @Override public Object call(final Object value) {
                if (onePass.pass())
                    action.setFulfilled(value);
                return UntypedPromiseImpl.factory.alwaysPendingPromise();
            }},
            new promises.OnRejected<Object>() {
                @Override public Object call(final Object reason, final Throwable exception) {
                    if (onePass.pass())
                        action.setRejected(reason, exception);
                    return UntypedPromiseImpl.factory.alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void resolveByTypedPromise(final promises.typed.Promise<?, ?> promise, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new promises.typed.OnFulfilled<Object, Object, Object>() {
                @Override public Resolution<?, ?> call(final Object value) {
                    if (onePass.pass())
                        action.setFulfilled(value);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            },
            new promises.typed.OnRejected<Object, Object, Object>() {
                @Override public Resolution<?, ?> call(final Object reason, final Throwable exception) {
                    if (onePass.pass())
                        action.setRejected(reason, exception);
                    return TypedPromiseImpl.factory().alwaysPendingPromise();
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void resolveByLightWeightPromise(final P<?> promise, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        promise.then(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new OnFul<Object, Object>() { @Override public RV<?> call(final Object value) {
                if (onePass.pass())
                    action.setFulfilled(value);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
            }},
            new OnRej<Object>() { @Override public RV<?> call(final Throwable exception) {
                if (onePass.pass())
                    action.setRejected(null, exception);
                return LightWeightPromiseImpl.factory().alwaysPendingPromise();
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void resolveByUntypedThenable(final promises.Thenable thenable, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.ResolvePromise() { @Override public void resolve(final Object value) {
                    if (onePass.pass())
                        resolveValue(value, action);
                }},
                new promises.RejectPromise() {
                    @Override public void reject(final Object reason, final Throwable exception) {
                        if (onePass.pass())
                            action.setRejected(reason, exception);
                    }

                    @Override public void reject(final Object reason) {
                        if (onePass.pass())
                            action.setRejected(reason, null);
                    }

                    @Override public void reject(final Throwable exception) {
                        if (onePass.pass())
                            action.setRejected(null, exception);
                    }
                }
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                action.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> void
    resolveByTypedThenable(final promises.typed.Thenable<V, R> thenable, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new promises.typed.ResolvePromise<V, R>() {
                    @Override public void resolve(final Resolution<? extends V, ? extends R> res) {
                        if (onePass.pass())
                            resolveValue(res, action);
                    }

                    @Override public void resolve(final Object value) {
                        if (onePass.pass())
                            action.setFulfilled(value);
                    }
                },
                new promises.typed.RejectPromise<R>() {
                    @Override public void reject(final R reason, final Throwable exception) {
                        if (onePass.pass())
                            action.setRejected(reason, exception);
                    }

                    @Override public void reject(final R reason) {
                        if (onePass.pass())
                            action.setRejected(reason, null);
                    }

                    @Override public void reject(final Throwable exception) {
                        if (onePass.pass())
                            action.setRejected(null, exception);
                    }
                }
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                action.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> void
    resolveByLightWeightThenable(final promises.lw.Thenable<V> thenable, final ResolveAction action)
    {
        final OnePass onePass = new OnePass();

        try {
            thenable.then(
                new ResP<V>() {
                    @Override public void resolve(final RV<? extends V> res) {
                        if (onePass.pass())
                            resolveValue(res, action);
                    }

                    @Override public void resolve(final Object value) {
                        if (onePass.pass())
                            action.setFulfilled(value);
                    }
                },
                new RejP() { @Override public void reject(final Throwable exception) {
                    if (onePass.pass())
                        action.setRejected(null, exception);
                }}
            );
        } catch (final Throwable e) {
            if (onePass.pass())
                action.setRejected(null, e);
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static void resolveByResolution(final Resolution<?, ?> res, final ResolveAction action)
    {
        final PromiseState state = res.state();

        if (state == null) {
            action.setRejected(null, new NullPointerException("Null resolution state"));
            return;
        }

        switch (state) {
        case FULFILLED:
            action.setFulfilled(res.value());
            return;

        case REJECTED:
            action.setRejected(res.reason(), res.exception());
            return;

        default:
            action.setRejected(null, new TypeErrorException("Invalid resolution state %s", state));
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    static void resolveValue(final Object value, final ResolveAction action)
    {
        if (value instanceof BasePromiseImpl) {
            ((BasePromiseImpl) value).applyResolveAction(action);
            return;
        }

        if (value instanceof promises.Promise) {
            resolveByUntypedPromise((promises.Promise) value, action);
            return;
        }

        if (value instanceof promises.typed.Promise) {
            resolveByTypedPromise((promises.typed.Promise<?, ?>) value, action);
            return;
        }

        if (value instanceof P) {
            resolveByLightWeightPromise((P<?>) value, action);
            return;
        }

        if (value instanceof promises.Thenable) {
            resolveByUntypedThenable((promises.Thenable) value, action);
            return;
        }

        if (value instanceof promises.typed.Thenable) {
            resolveByTypedThenable((promises.typed.Thenable<?, ?>) value, action);
            return;
        }

        if (value instanceof promises.lw.Thenable) {
            resolveByLightWeightThenable((promises.lw.Thenable<?>) value, action);
            return;
        }

        if (value instanceof Resolution) {
            resolveByResolution((Resolution<?, ?>) value, action);
            return;
        }

        if (value instanceof RV) {
            action.setFulfilled(((RV<?>) value).value());
            return;
        }

        action.setFulfilled(value);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
