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
import promises.lw.RV;
import promises.lw.RejP;
import promises.lw.ResP;
import promises.lw.Thenable;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
public final class LightWeightPromiseImplTest extends BasePromiseTest<RV<?>, LightWeightPromiseImpl<?>, Thenable<?>>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final BasePromiseTest<RV<?>, LightWeightPromiseImpl<?>, Thenable<?>>
    test = new LightWeightPromiseImplTest();
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final PromiseFactory<? extends LightWeightPromiseImpl<?>> promiseFactory()
    {
        return LightWeightPromiseImpl.factory();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(final LightWeightPromiseImpl<?> promise)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object await(LightWeightPromiseImpl<?> promise, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException
    {
        return promise.await(timeout, unit);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final UntypedPromiseImpl toUntypedPromise(final LightWeightPromiseImpl<?> promise)
    {
        return promise.toUntypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final TypedPromiseImpl<?, ?> toTypedPromise(final LightWeightPromiseImpl<?> promise)
    {
        return promise.toTypedPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final LightWeightPromiseImpl<?> toLightWeightPromise(final LightWeightPromiseImpl<?> promise)
    {
        return promise.toUntypedPromise().toLightWeightPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final void applyResolveAction(final LightWeightPromiseImpl<?> promise, final ResolveAction resolveAction)
    {
        promise.applyResolveAction(resolveAction);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final LightWeightPromiseImpl<?> doThen(
        final LightWeightPromiseImpl<?> srcPromise,
        final Executor exec,
        final FR1<Object, ? extends RV<?>> onFulfilled,
        final FR2<Object, Throwable, ? extends RV<?>> onRejected
    ) {
        return srcPromise.doThen(
            exec,
            onFulfilled,
            onRejected == null ? null
                : new FR1<Throwable, RV<?>>() {
                @Override public RV<?> call(final Throwable exception) throws Throwable {
                    return onRejected.call(null, exception);
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Matcher<LightWeightPromiseImpl<?>> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<LightWeightPromiseImpl<?>>() {
            @Override protected boolean matchesSafely(final LightWeightPromiseImpl<?> item) {
                return (expectedType == null || TestUtil.equals(item.type(), "LW-" + expectedType))
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Light-weight promise ").appendValueList(
                    "[", ", ", "]",
                    "LW-" + expectedType, expectedState, expectedValue, expectedExceptionClass
                );
            }

            @Override protected void
            describeMismatchSafely(final LightWeightPromiseImpl<?> item, final Description desc) {
                desc.appendValueList("[", ", ", "]", item.type(), item.state(), item.value(), item.exception());
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final RV<?> fulfilledResolution(final Object value)
    {
        return new RV<Object>() {
            @Override public Object value() { return value; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final RV<?> rejectedResolution(final Object reason, final Throwable exception)
    {
        return LightWeightPromiseImpl.factory().rejectedPromise(null, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final RV<?> alwaysPendingResolution()
    {
        return LightWeightPromiseImpl.factory().alwaysPendingPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> Thenable<V>
    doThenable(final Thenable<V> thenable, final TestStep cbStep, final TestStep resStep)
    {
        return new Thenable<V>() { @Override public void then(final ResP<V> resP, final RejP rejP) throws Throwable {
            cbStep.pause();
            thenable.then(resP, rejP);
            resStep.finish();
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenable(final Thenable<?> thenable, final TestStep cbStep, final TestStep resStep)
    {
        return doThenable(thenable, cbStep, resStep);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableNop()
    {
        return new Thenable<Object>() {
            @Override public void then(final ResP<Object> resP, final RejP rejP) { }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableResolve(
        final Return<? extends RV<?>> retResolution,
        final Params params,
        final TestStep cbStep,
        final TestStep resStep
    ) {
        return new Thenable<Object>() {
            @Override public void then(final ResP<Object> resP, final RejP rejP) throws Throwable {
                resP.resolve(retResolution.call(params, cbStep, resStep));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableResolve(final Object value)
    {
        return new Thenable<Object>() { @Override public void then(final ResP<Object> resP, final RejP rejP) {
            resP.resolve(value);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableReject(final Object reason, final Throwable exception)
    {
        return thenableReject(exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableReject(final Object reason)
    {
        return thenableReject(null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Thenable<?> thenableReject(final Throwable exception)
    {
        return new Thenable<Object>() { @Override public void then(final ResP<Object> resP, final RejP rejP) {
            rejP.reject(exception);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final P<?> testPromise(final F2<FR1<Object, RV<?>>, FR2<Object, Throwable, RV<?>>> thenCall)
    {
        return new P<Object>() {
            @Override public PromiseState state() { return null; }
            @Override public Object value() { return null; }
            @Override public Throwable exception() { return null; }
            @Override public Object await() { return null; }
            @Override public Object await(final long timeout, final TimeUnit unit) { return null; }
            @Override public promises.Promise toUntypedPromise() { return null; }
            @Override public <R> promises.typed.Promise<Object, R> toTypedPromise() { return null; }

            @Override public <VO> P<VO> then(
                final Executor exec,
                final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                final FR1<Throwable, ? extends RV<? extends VO>> onRejected
            ) {
                try {
                    thenCall.call(
                        ImplUtil.<FR1<Object, RV<?>>>cast(onFulfilled),
                        new FR2<Object, Throwable, RV<?>>() {
                            @Override public RV<?>
                            call(final Object reason, final Throwable exception) throws Throwable {
                                return onRejected.call(exception);
                            }
                        }
                    );
                } catch (final Throwable e) {
                    //
                }

                return null;
            }

            @Override public <VO> P<VO>
            then(final Executor exec, final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled) {
                return then(exec, onFulfilled, null);
            }

            @Override public <VO> P<VO> then(
                final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled,
                final FR1<Throwable, ? extends RV<? extends VO>> onRejected
            ) {
                return then(null, onFulfilled, onRejected);
            }

            @Override public <VO> P<VO> then(final FR1<? super Object, ? extends RV<? extends VO>> onFulfilled) {
                return then(null, onFulfilled, null);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    final Object translateReason(final Object reason)
    {
        return null;
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
        private static <V> LightWeightPromiseImpl<V> newTestPromise()
        {
            return new LightWeightPromiseImpl<V>() {
                @Override String type() { return null; }
                @Override public PromiseState state() { return null; }
                @Override public V value() { return null; }
                @Override public Throwable exception() { return null; }
                @Override public V await() { return null; }
                @Override public V await(final long timeout, final TimeUnit unit) { return null; }
                @Override public UntypedPromiseImpl toUntypedPromise() { return null; }
                @Override public <R> TypedPromiseImpl<V, R> toTypedPromise() { return null; }
                @Override void applyResolveAction(final ResolveAction resAction) { }

                @Override <VO> LightWeightPromiseImpl<VO> doThen(
                    final Executor exec,
                    final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
                    final FR1<Throwable, ? extends RV<? extends VO>> onRejected
                ) {
                    return null;
                }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static <V, VO> FR1<V, RV<VO>> newTestOnFulfilled()
        {
            return new FR1<V, RV<VO>>() {
                @Override public RV<VO> call(final V value) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private static <VO> FR1<Throwable, RV<VO>> newTestOnRejected()
        {
            return new FR1<Throwable, RV<VO>>() {
                @Override public RV<VO> call(final Throwable exception) { return null; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static <V, VO> Object[][] paramThenWithExecutorAndWithOnRejected()
        {
            final Executor exec = newTestExecutor();
            final FR1<V, RV<VO>> onFulfilled = newTestOnFulfilled();
            final FR1<Throwable, RV<VO>> onRejected = newTestOnRejected();

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
        public final <V, VO> void testThenWithExecutorAndWithOnRejected(
            final Executor exec,
            final FR1<V, RV<VO>> onFulfilled,
            final FR1<Throwable, RV<VO>> onRejected
        ) {
            final LightWeightPromiseImpl<V> promise = newTestPromise();

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
        public final <V, VO> void testThenWithNonnullExecutorAndWithoutOnRejected(final Executor exec)
        {
            final LightWeightPromiseImpl<V> promise = newTestPromise();

            final FR1<V, RV<VO>> onFulfilled = newTestOnFulfilled();

            new StrictExpectations(promise) {{
                promise.doThen(exec, onFulfilled, null);
            }};

            promise.then(exec, onFulfilled);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static <V, VO> Object[][] paramThenWithoutExecutorAndWithOnRejected()
        {
            final FR1<V, RV<VO>> onFulfilled = newTestOnFulfilled();
            final FR1<Throwable, RV<VO>> onRejected = newTestOnRejected();

            return new Object[][] {
                {onFulfilled, onRejected},
                {onFulfilled, null},
                {null,        onRejected},
            };
        }

        @Test
        @Parameters(method = "paramThenWithoutExecutorAndWithOnRejected")
        public final <V, VO> void testThenWithoutExecutorAndWithOnRejected(
            final FR1<V, RV<VO>> onFulfilled,
            final FR1<Throwable, RV<VO>> onRejected
        ) {
            final LightWeightPromiseImpl<V> promise = newTestPromise();

            new StrictExpectations(promise) {{
                promise.doThen(null, onFulfilled, onRejected);
            }};

            promise.then(onFulfilled, onRejected);

            new FullVerificationsInOrder(promise) {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final <V, VO> void testThenWithoutExecutorAndWithoutOnRejected()
        {
            final LightWeightPromiseImpl<V> promise = newTestPromise();

            final FR1<V, RV<VO>> onFulfilled = newTestOnFulfilled();

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
