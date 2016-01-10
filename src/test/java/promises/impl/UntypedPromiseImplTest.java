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
import promises.RejectPromise;
import promises.ResolvePromise;
import promises.Promise;
import promises.PromiseState;
import promises.TestStep;
import promises.TestUtil;
import promises.Thenable;
import promises.lw.P;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class UntypedPromiseImplTest extends BasePromiseTest<Object, UntypedPromiseImpl, Thenable>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final BasePromiseTest<Object, UntypedPromiseImpl, Thenable> test = new UntypedPromiseImplTest();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final PromiseFactory<? extends UntypedPromiseImpl> promiseFactory()
    {
        return UntypedPromiseImpl.factory;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(final UntypedPromiseImpl promise)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(final UntypedPromiseImpl promise, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await(timeout, unit);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final UntypedPromiseImpl toUntypedPromise(final UntypedPromiseImpl promise)
    {
        return promise.toTypedPromise().toUntypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final TypedPromiseImpl<?, ?> toTypedPromise(final UntypedPromiseImpl promise)
    {
        return promise.toTypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final LightWeightPromiseImpl<?> toLightWeightPromise(final UntypedPromiseImpl promise)
    {
        return promise.toLightWeightPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final void applyResolveAction(final UntypedPromiseImpl promise, final ResolveAction resolveAction)
    {
        promise.applyResolveAction(resolveAction);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final UntypedPromiseImpl doThen(
        final UntypedPromiseImpl promise,
        final Executor exec,
        final FR1<Object, ?> onFulfilled,
        final FR2<Object, Throwable, ?> onRejected
    ) {
        return promise.doThen(exec, onFulfilled, onRejected);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Matcher<UntypedPromiseImpl> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<UntypedPromiseImpl>() {
            @Override protected boolean matchesSafely(final UntypedPromiseImpl item) {
                return (expectedType == null || TestUtil.equals(item.type(), "UNTYPED-" + expectedType))
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.equals(item.reason(), expectedReason)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Untyped promise ").appendValueList(
                    "[", ", ", "]",
                    "UNTYPED-" + expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
                );
            }

            @Override protected void describeMismatchSafely(final UntypedPromiseImpl item, final Description desc) {
                desc.appendValueList(
                    "[", ", ", "]",
                    item.type(), item.state(), item.value(), item.reason(), item.exception()
                );
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object fulfilledResolution(final Object value)
    {
        return value;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object rejectedResolution(final Object reason, final Throwable exception)
    {
        return UntypedPromiseImpl.factory.rejectedPromise(reason, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object alwaysPendingResolution()
    {
        return UntypedPromiseImpl.factory.alwaysPendingPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenable(final Thenable thenable, final TestStep resStep)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
                thenable.then(resP, rejP);
                resStep.finish();
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableNop()
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) { }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableResolve(final Return<?> retResolution, final Params params, final TestStep resStep)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
                resP.resolve(retResolution.call(params, resStep));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableResolve(final Object value)
    {
        return new Thenable() { @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
            resP.resolve(value);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableReject(final Object reason, final Throwable exception)
    {
        return new Thenable() { @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
            rejP.reject(reason, exception);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableReject(final Object reason)
    {
        return new Thenable() { @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
            rejP.reject(reason);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable thenableReject(final Throwable exception)
    {
        return new Thenable() { @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
            rejP.reject(exception);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Promise testPromise(final F2<FR1<Object, Object>, FR2<Object, Throwable, Object>> thenCall)
    {
        return new Promise() {
            @Override public PromiseState state() { return null; }
            @Override public <V> V value() { return null; }
            @Override public <R> R reason() { return null; }
            @Override public Throwable exception() { return null; }
            @Override public <V> V await() { return null; }
            @Override public <V> V await(final long timeout, final TimeUnit unit) { return null; }
            @Override public <V, R> promises.typed.Promise<V, R> toTypedPromise() { return null; }
            @Override public <V> P<V> toLightWeightPromise() { return null; }

            @Override public Promise
            then(final Executor exec, final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                try {
                    thenCall.call(
                        ImplUtil.<FR1<Object, Object>>cast(onFulfilled),
                        ImplUtil.<FR2<Object, Throwable, Object>>cast(onRejected)
                    );
                } catch (final Throwable e) {
                    //
                }

                return null;
            }

            @Override public Promise then(final Executor exec, final FR1<?, ?> onFulfilled) {
                return then(exec, onFulfilled, null);
            }

            @Override public Promise then(final FR1<?, ?> onFulfilled, final FR2<?, Throwable, ?> onRejected) {
                return then(null, onFulfilled, onRejected);
            }

            @Override public Promise then(final FR1<?, ?> onFulfilled) {
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
        private static UntypedPromiseImpl newTestPromise()
        {
            return new UntypedPromiseImpl() {
                @Override String type() { return null; }
                @Override public PromiseState state() { return null; }
                @Override public <V> V value() { return null; }
                @Override public <R> R reason() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public <V> V await() { return null; }
                @Override public <V> V await(final long timeout, final TimeUnit unit) { return null; }
                @Override public <V, R> TypedPromiseImpl<V, R> toTypedPromise() { return null; }
                @Override public <V> LightWeightPromiseImpl<V> toLightWeightPromise() { return null; }
                @Override void applyResolveAction(final ResolveAction resAction) { }

                @Override <V, R> UntypedPromiseImpl
                doThen(final Executor exec, final FR1<V, ?> onFulfilled, final FR2<R, Throwable, ?> onRejected) {
                    return null;
                }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static FR1<?, ?> newTestOnFulfilled()
        {
            return new FR1<Object, Object>() {
                @Override public Object call(final Object value) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static FR2<?, Throwable, ?> newTestOnRejected()
        {
            return new FR2<Object, Throwable, Object>() {
                @Override public Object call(final Object reason, final Throwable exception) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramThenWithExecutorAndWithOnRejected()
        {
            final Executor exec = newTestExecutor();
            final FR1<?, ?> onFulfilled = newTestOnFulfilled();
            final FR2<?, Throwable, ?> onRejected = newTestOnRejected();

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
        public final void testThenWithExecutorAndWithOnRejected(
            final Executor exec,
            final FR1<?, ?> onFulfilled,
            final FR2<?, Throwable, ?> onRejected
        ) {
            final UntypedPromiseImpl promise = newTestPromise();

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
        public final void testThenWithExecutorAndWithoutOnRejected(final Executor exec)
        {
            final UntypedPromiseImpl promise = newTestPromise();
            final FR1<?, ?> onFulfilled = newTestOnFulfilled();

            new StrictExpectations(promise) {{
                promise.doThen(exec, onFulfilled, null);
            }};

            promise.then(exec, onFulfilled);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramThenWithoutExecutorAndWithOnRejected()
        {
            final FR1<?, ?> onFulfilled = newTestOnFulfilled();
            final FR2<?, Throwable, ?> onRejected = newTestOnRejected();

            return new Object[][] {
                {onFulfilled, onRejected},
                {onFulfilled, null},
                {null,        onRejected},
            };
        }

        @Test
        @Parameters(method = "paramThenWithoutExecutorAndWithOnRejected")
        public final void testThenWithoutExecutorAndWithOnRejected(
            final FR1<?, ?> onFulfilled,
            final FR2<?, Throwable, ?> onRejected
        ) {
            final UntypedPromiseImpl promise = newTestPromise();

            new StrictExpectations(promise) {{
                promise.doThen(null, onFulfilled, onRejected);
            }};

            promise.then(onFulfilled, onRejected);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void testThenWithoutExecutorAndWithoutOnRejected()
        {
            final UntypedPromiseImpl promise = newTestPromise();

            final FR1<?, ?> onFulfilled = newTestOnFulfilled();

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
