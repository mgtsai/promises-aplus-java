//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.FullVerificationsInOrder;
import mockit.StrictExpectations;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import promises.F2;
import promises.FR1;
import promises.FR2;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.TestStep;
import promises.TestUtil;
import promises.lw.P;
import promises.typed.Promise;
import promises.typed.RejectPromise;
import promises.typed.Resolution;
import promises.typed.ResolvePromise;
import promises.typed.Thenable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class TypedPromiseImplTest
    extends BasePromiseTest<Resolution<?, ?>, TypedPromiseImpl<?, ?>, Thenable<?, ?>>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final BasePromiseTest<Resolution<?, ?>, TypedPromiseImpl<?, ?>, Thenable<?, ?>>
    test = new TypedPromiseImplTest();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final PromiseFactory<? extends TypedPromiseImpl<?, ?>> promiseFactory()
    {
        return TypedPromiseImpl.factory();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(final TypedPromiseImpl<?, ?> promise)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(final TypedPromiseImpl<?, ?> promise, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await(timeout, unit);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final UntypedPromiseImpl toUntypedPromise(final TypedPromiseImpl<?, ?> promise)
    {
        return promise.toUntypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final TypedPromiseImpl<?, ?> toTypedPromise(final TypedPromiseImpl<?, ?> promise)
    {
        return promise.toUntypedPromise().toTypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final LightWeightPromiseImpl<?> toLightWeightPromise(final TypedPromiseImpl<?, ?> promise)
    {
        return promise.toLightWeightPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final void applyResolveAction(final TypedPromiseImpl<?, ?> promise, final ResolveAction resolveAction)
    {
        promise.applyResolveAction(resolveAction);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final TypedPromiseImpl<?, ?> doThen(
        final TypedPromiseImpl<?, ?> promise,
        final Executor exec,
        final FR1<Object, ? extends Resolution<?, ?>> onFulfilled,
        final FR2<Object, Throwable, ? extends Resolution<?, ?>> onRejected
    ) {
        return promise.doThen(exec, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Matcher<TypedPromiseImpl<?, ?>> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<TypedPromiseImpl<?, ?>>() {
            @Override protected boolean matchesSafely(final TypedPromiseImpl<?, ?> item) {
                return (expectedType == null || TestUtil.equals(item.type(), "TYPED-" + expectedType))
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.equals(item.reason(), expectedReason)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Typed promise ").appendValueList(
                    "[", ", ", "]",
                    "TYPED-" + expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
                );
            }

            @Override protected void
            describeMismatchSafely(final TypedPromiseImpl<?, ?> item, final Description desc) {
                desc.appendValueList(
                    "[", ", ", "]",
                    item.type(), item.state(), item.value(), item.reason(), item.exception()
                );
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Resolution<?, ?> fulfilledResolution(final Object value)
    {
        return new Resolution<Object, Object>() {
            @Override public PromiseState state() { return PromiseState.FULFILLED; }
            @Override public Object value() { return value; }
            @Override public Object reason() { return null; }
            @Override public Throwable exception() { return null; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Resolution<?, ?> rejectedResolution(final Object reason, final Throwable exception)
    {
        return new Resolution<Object, Object>() {
            @Override public PromiseState state() { return PromiseState.REJECTED; }
            @Override public Object value() { return null; }
            @Override public Object reason() { return reason; }
            @Override public Throwable exception() { return exception; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Resolution<?, ?> alwaysPendingResolution()
    {
        return TypedPromiseImpl.factory().alwaysPendingPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> Thenable<V, R>
    doThenable(final Thenable<V, R> thenable, final TestStep cbStep, final TestStep resStep)
    {
        return new Thenable<V, R>() {
            @Override public void then(final ResolvePromise<V, R> resP, final RejectPromise<R> rejP) throws Throwable {
                cbStep.pause();
                thenable.then(resP, rejP);
                resStep.finish();
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenable(final Thenable<?, ?> thenable, final TestStep cbStep, final TestStep resStep)
    {
        return doThenable(thenable, cbStep, resStep);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableNop()
    {
        return new Thenable<Object, Object>() {
            @Override public void then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) { }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableResolve(
        final Return<? extends Resolution<?, ?>> retResolution,
        final Params params,
        final TestStep cbStep,
        final TestStep resStep
    ) {
        return new Thenable<Object, Object>() {
            @Override public void
            then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) throws Throwable {
                resP.resolve(retResolution.call(params, cbStep, resStep));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableResolve(final Object value)
    {
        return new Thenable<Object, Object>() {
            @Override public void then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) {
                resP.resolve(value);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableReject(final Object reason, final Throwable exception)
    {
        return new Thenable<Object, Object>() {
            @Override public void then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) {
                rejP.reject(reason, exception);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableReject(final Object reason)
    {
        return new Thenable<Object, Object>() {
            @Override public void then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) {
                rejP.reject(reason);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?, ?> thenableReject(final Throwable exception)
    {
        return new Thenable<Object, Object>() {
            @Override public void then(final ResolvePromise<Object, Object> resP, final RejectPromise<Object> rejP) {
                rejP.reject(exception);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Promise<?, ?>
    testPromise(final F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>> thenCall)
    {
        return new Promise<Object, Object>() {
            @Override public PromiseState state() { return null; }
            @Override public Object value() { return null; }
            @Override public Object reason() { return null; }
            @Override public Throwable exception() { return null; }
            @Override public Object await() { return null; }
            @Override public Object await(final long timeout, final TimeUnit unit) { return null; }
            @Override public promises.Promise toUntypedPromise() { return null; }
            @Override public P<Object> toLightWeightPromise() { return null; }

            @Override public <VO, RO> Promise<VO, RO> then(
                final Executor exec,
                final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
            ) {
                try {
                    thenCall.call(
                        ImplUtil.<FR1<Object, Resolution<?, ?>>>cast(onFulfilled),
                        ImplUtil.<FR2<Object, Throwable, Resolution<?, ?>>>cast(onRejected)
                    );
                } catch (final Throwable e) {
                    //
                }

                return null;
            }

            @Override public <VO, RO> Promise<VO, RO> then(
                final Executor exec,
                final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled
            ) {
                return then(exec, onFulfilled, null);
            }

            @Override public <VO, RO> Promise<VO, RO> then(
                final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                final FR2<? super Object, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
            ) {
                return then(null, onFulfilled, onRejected);
            }

            @Override public <VO, RO> Promise<VO, RO>
            then(final FR1<? super Object, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled) {
                return then(null, onFulfilled, null);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class FactoryMethods extends BaseFactoryMethods
    {
        public FactoryMethods()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class AwaitMethods extends BaseAwaitMethods
    {
        public AwaitMethods()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class ToPromiseMethods extends BaseToPromiseMethods
    {
        public ToPromiseMethods()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class ApplyResolveActionMethods extends BaseApplyResolveActionMethods
    {
        public ApplyResolveActionMethods()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class DoThenMethods extends BaseDoThenMethods
    {
        public DoThenMethods()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class MultiCallbacks extends BaseMultiCallbacks
    {
        public MultiCallbacks()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class Chains extends BaseChains
    {
        public Chains()
        {
            test.super();
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class ThenMethods
    {
        //-------------------------------------------------------------------------------------------------------------
        private static <V, R> TypedPromiseImpl<V, R> newTestPromise()
        {
            return new TypedPromiseImpl<V, R>() {
                @Override String type() { return null; }
                @Override public PromiseState state() { return null; }
                @Override public V value() { return null; }
                @Override public R reason() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public V await() { return null; }
                @Override public V await(final long timeout, final TimeUnit unit) { return null; }
                @Override public UntypedPromiseImpl toUntypedPromise() { return null; }
                @Override public LightWeightPromiseImpl<V> toLightWeightPromise() { return null; }
                @Override void applyResolveAction(final ResolveAction resAction) { }

                @Override <VO, RO> TypedPromiseImpl<VO, RO> doThen(
                    final Executor exec,
                    final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                    final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
                ) {
                    return null;
                }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static <V, VO, RO> FR1<V, Resolution<VO, RO>> newTestOnFulfilled()
        {
            return new FR1<V, Resolution<VO, RO>>() {
                @Override public Resolution<VO, RO> call(final V value) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static <R, VO, RO> FR2<R, Throwable, Resolution<VO, RO>> newTestOnRejected()
        {
            return new FR2<R, Throwable, Resolution<VO, RO>>() {
                @Override public Resolution<VO, RO> call(final R reason, final Throwable exception) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static <V, R, VO, RO> Object[][] paramThenWithExecutorAndWithOnRejected()
        {
            final Executor exec = newTestExecutor();
            final FR1<V, Resolution<VO, RO>> onFulfilled = newTestOnFulfilled();
            final FR2<R, Throwable, Resolution<VO, RO>> onRejected = newTestOnRejected();

            return new Object[][] {
                {exec, onFulfilled, onRejected},
                {exec, onFulfilled, null},
                {exec, null,        onRejected},
                {null, onFulfilled, onRejected},
                {null, onFulfilled, null},
                {null, null,        onRejected},
            };
        }

        @Test
        @Parameters(method = "paramThenWithExecutorAndWithOnRejected")
        public final <V, R, VO, RO> void testThenWithExecutorAndWithOnRejected(
            final Executor exec,
            final FR1<V, Resolution<VO, RO>> onFulfilled,
            final FR2<R, Throwable, Resolution<VO, RO>> onRejected
        ) {
            final TypedPromiseImpl<V, R> promise = newTestPromise();

            new StrictExpectations(promise) {{
                promise.doThen(exec, onFulfilled, onRejected);
            }};

            promise.then(exec, onFulfilled, onRejected);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramThenWithExecutorAndWithoutOnRejected() { return new Object[][] {
            {newTestExecutor()},
            {null},
        };}

        @Test
        @Parameters(method = "paramThenWithExecutorAndWithoutOnRejected")
        public final <V, R, VO, RO> void testThenWithExecutorAndWithoutOnRejected(final Executor exec)
        {
            final TypedPromiseImpl<V, R> promise = newTestPromise();

            final FR1<V, Resolution<VO, RO>> onFulfilled = newTestOnFulfilled();

            new StrictExpectations(promise) {{
                promise.doThen(exec, onFulfilled, null);
            }};

            promise.then(exec, onFulfilled);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static <V, R, VO, RO> Object[][] paramThenWithoutExecutorAndWithOnRejected()
        {
            final FR1<V, Resolution<VO, RO>> onFulfilled = newTestOnFulfilled();
            final FR2<R, Throwable, Resolution<VO, RO>> onRejected = newTestOnRejected();

            return new Object[][] {
                {onFulfilled, onRejected},
                {onFulfilled, null},
                {null,        onRejected},
            };
        }

        @Test
        @Parameters(method = "paramThenWithoutExecutorAndWithOnRejected")
        public final <V, R, VO, RO> void testThenWithoutExecutorAndWithOnRejected(
            final FR1<V, Resolution<VO, RO>> onFulfilled,
            final FR2<R, Throwable, Resolution<VO, RO>> onRejected
        ) {
            final TypedPromiseImpl<V, R> promise = newTestPromise();

            new StrictExpectations(promise) {{
                promise.doThen(null, onFulfilled, onRejected);
            }};

            promise.then(onFulfilled, onRejected);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final <V, R, VO, RO> void testThenWithoutExecutorAndWithoutOnRejected()
        {
            final TypedPromiseImpl<V, R> promise = newTestPromise();

            final FR1<V, Resolution<VO, RO>> onFulfilled = newTestOnFulfilled();

            new StrictExpectations(promise) {{
                promise.doThen(null, onFulfilled, null);
            }};

            promise.then(onFulfilled);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
