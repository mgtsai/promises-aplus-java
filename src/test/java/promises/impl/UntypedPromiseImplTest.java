//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.Injectable;
import mockit.StrictExpectations;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import promises.F2;
import promises.FR1;
import promises.FR2;
import promises.PromiseRejectedException;
import promises.RejectPromise;
import promises.ResolvePromise;
import promises.TestData;
import promises.TestLogger;
import promises.Promise;
import promises.PromiseState;
import promises.TestStep;
import promises.TestUtil;
import promises.Thenable;
import promises.lw.P;
import promises.typed.Resolution;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
@RunWith(JUnitParamsRunner.class)
public final class UntypedPromiseImplTest
{
    //-----------------------------------------------------------------------------------------------------------------
    @Injectable private TestLogger loggerMock = null;
    @Injectable private ResolveAction resolveActionMock = null;
    @Injectable private FR1<Object, ?> unusedOnFulfilled = null;
    @Injectable private FR2<Object, Throwable, ?> unusedOnRejected = null;
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR1<Object, Object> defaultOnFulfilled = new FR1<Object, Object>() {
        @Override public Object call(final Object value) { return value; }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR2<Object, Throwable, Object> defaultOnRejected = new FR2<Object, Throwable, Object>() {
        @Override public Object call(final Object reason, final Throwable exception) {
            return new Resolution<Object, Object>() {
                @Override public PromiseState state() { return PromiseState.REJECTED; }
                @Override public Object value() { return null; }
                @Override public Object reason() { return reason; }
                @Override public Throwable exception() { return exception; }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final F2<FR1<Object, ?>, FR2<Object, Throwable, ?>>
    callNothing = new F2<FR1<Object, ?>, FR2<Object, Throwable, ?>>() {
        @Override public void call(final FR1<Object, ?> onFulfilled, final FR2<Object, Throwable, ?> onRejected) { }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>
    retPendingPromise = new TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>() {
        @Override public UntypedPromiseImpl
        call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
            cbStep.pause();
            resStep.finish();

            return UntypedPromiseImpl.factory.fulfilledPromise(null)
                .doThen(TestUtil.NOP_EXECUTOR, self.unusedOnFulfilled, self.unusedOnRejected);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl>
    suppPendingMutablePromise = new TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl>() {
        @Override public TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>
        get(final TestStep.Return<UntypedPromiseImplTest, ?> retResolution) {
            return new TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>() {
                @Override public UntypedPromiseImpl
                call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    cbStep.pause();
                    final TestStep chainResolveStep = new TestStep();

                    return promiseDoThen(
                        UntypedPromiseImpl.factory.fulfilledPromise(null),
                        new FR1<Object, Object>() { @Override public Object call(final Object value) throws Throwable {
                            resStep.pause();
                            return retResolution.call(self, cbStep, chainResolveStep);
                        }},
                        self.unusedOnRejected,
                        resStep,
                        chainResolveStep
                    );
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl>
    suppResolvedMutablePromise = new TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl>() {
        @Override public TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>
        get(final TestStep.Return<UntypedPromiseImplTest, ?> retResolution) {
            return new TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl>() {
                @Override public UntypedPromiseImpl
                call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep)
                    throws Throwable
                {
                    resStep.finish();
                    final TestStep step = new TestStep();
                    try {
                        return suppPendingMutablePromise.get(retResolution).call(self, cbStep, step);
                    } finally {
                        step.sync();
                    }
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.ReturnSupplier<UntypedPromiseImplTest, Thenable>
    suppThenableResolve = new TestStep.ReturnSupplier<UntypedPromiseImplTest, Thenable>() {
        @Override public TestStep.Return<UntypedPromiseImplTest, Thenable>
        get(final TestStep.Return<UntypedPromiseImplTest, ?> retResolution) {
            return new TestStep.Return<UntypedPromiseImplTest, Thenable>() {
                @Override public Thenable
                call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    return new Thenable() {
                        @Override public void
                        then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
                            resP.resolve(retResolution.call(self, cbStep, resStep));
                        }
                    };
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static Matcher<UntypedPromiseImpl> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<UntypedPromiseImpl>() {
            @Override protected boolean matchesSafely(final UntypedPromiseImpl item) {
                return TestUtil.equals(item.type(), expectedType)
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.equals(item.reason(), expectedReason)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Untyped promise ").appendValueList(
                    "[", ", ", "]",
                    expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
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
    private static <T> TestStep.Return<UntypedPromiseImplTest, T> noWait(final T ret)
    {
        return TestStep.noWait(ret);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <T> TestStep.Return<UntypedPromiseImplTest, T> throwException(final Throwable exception)
    {
        return TestStep.throwException(exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, ?>, FR2<Object, Throwable, ?>> callOnFulfilled(final Object fulfilledValue)
    {
        return new F2<FR1<Object, ?>, FR2<Object, Throwable, ?>>() {
            @Override public void
            call(final FR1<Object, ?> onFulfilled, final FR2<Object, Throwable, ?> onRejected) throws Throwable {
                onFulfilled.call(fulfilledValue);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, ?>, FR2<Object, Throwable, ?>>
    callOnRejected(final Object rejectedReason, final Throwable rejectedException)
    {
        return new F2<FR1<Object, ?>, FR2<Object, Throwable, ?>>() {
            @Override public void
            call(final FR1<Object, ?> onFulfilled, final FR2<Object, Throwable, ?> onRejected) throws Throwable {
                onRejected.call(rejectedReason, rejectedException);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Promise testUntypedPromise(final F2<FR1<Object, ?>, FR2<Object, Throwable, ?>> thenCall)
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
                        onFulfilled != null ? ImplUtil.<FR1<Object, ?>>cast(onFulfilled) : defaultOnFulfilled,
                        onRejected != null ? ImplUtil.<FR2<Object, Throwable, ?>>cast(onRejected) : defaultOnRejected
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
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
            }

            @Override public Promise then(final FR1<?, ?> onFulfilled) {
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, ?> fulfilledResolution(final Object value)
    {
        return noWait(value);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, ?>
    rejectedResolution(final Object reason, final Throwable exception)
    {
        return noWait(UntypedPromiseImpl.factory.rejectedPromise(reason, exception));
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, ?> alwaysPendingResolution()
    {
        return noWait(UntypedPromiseImpl.factory.alwaysPendingPromise());
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static UntypedPromiseImpl promiseDoThen(
        final UntypedPromiseImpl srcPromise,
        final FR1<?, ?> onFulfilled,
        final FR2<?, Throwable, ?> onRejected,
        final TestStep resolveStep,
        final TestStep... chainResolveSteps
    ) {
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        try {
            return srcPromise.doThen(exec, onFulfilled, onRejected);
        } finally {
            srcPromise.applyResolveAction(new ResolveAction() {
                private void resolved() {
                    exec.execute(new Runnable() { @Override public void run() {
                        for (final TestStep chainResolveStep : chainResolveSteps)
                            chainResolveStep.sync();
                        resolveStep.finish();
                        exec.shutdown();
                    }});
                }

                @Override public void setAlwaysPending() { }
                @Override public void setFulfilled(final Object value) { resolved(); }
                @Override public void setRejected(final Object reason, final Throwable exception) { resolved(); }
            });
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, Thenable>
    retThenableReject(final Object reason, final Throwable exception)
    {
        return new TestStep.Return<UntypedPromiseImplTest, Thenable>() {
            @Override public Thenable
            call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable() {
                    @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
                        cbStep.pause();
                        rejP.reject(reason, exception);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, Thenable> retThenableReject(final Object reason)
    {
        return new TestStep.Return<UntypedPromiseImplTest, Thenable>() {
            @Override public Thenable
            call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable() {
                    @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
                        cbStep.pause();
                        rejP.reject(reason);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static TestStep.Return<UntypedPromiseImplTest, Thenable> retThenableReject(final Throwable exception)
    {
        return new TestStep.Return<UntypedPromiseImplTest, Thenable>() {
            @Override public Thenable
            call(final UntypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable() {
                    @Override public void then(final ResolvePromise resP, final RejectPromise rejP) {
                        cbStep.pause();
                        rejP.reject(exception);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsFulfilledResolution() { return new Object[][] {
        {noWait(null),                                                                        false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, null,  null, null},
        {noWait(123),                                                                         false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, 123,   null, null},
        {noWait(testUntypedPromise(callOnFulfilled("abc"))),                                  false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, "abc", null, null},
        {noWait(UntypedPromiseImpl.factory.fulfilledPromise('D')),                            false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, 'D',   null, null},
        {suppPendingMutablePromise.get(fulfilledResolution(4.5)),                             false, "UNTYPED-MUTABLE",   PromiseState.FULFILLED, 4.5,   null, null},
        {suppResolvedMutablePromise.get(fulfilledResolution(false)),                          false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, false, null, null},
        {suppThenableResolve.get(noWait(null)),                                               false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, null,  null, null},
        {suppThenableResolve.get(noWait(true)),                                               false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, true,  null, null},
        {suppThenableResolve.get(noWait(testUntypedPromise(callOnFulfilled(678)))),           false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, 678,   null, null},
        {suppThenableResolve.get(noWait(UntypedPromiseImpl.factory.fulfilledPromise("ijk"))), false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, "ijk", null, null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(fulfilledResolution('E'))),    false, "UNTYPED-MUTABLE",   PromiseState.FULFILLED, 'E',   null, null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(fulfilledResolution(-9.0))),  false, "UNTYPED-FULFILLED", PromiseState.FULFILLED, -9.0,  null, null},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsRejectedResolution() { return new Object[][] {
        {throwException(new Throwable()),                                                                             false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, null,  Throwable.class},
        {noWait(testUntypedPromise(callOnRejected(false, new Exception()))),                                          false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, false, Exception.class},
        {noWait(UntypedPromiseImpl.factory.rejectedPromise(true, new RuntimeException())),                            false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, true,  RuntimeException.class},
        {suppPendingMutablePromise.get(rejectedResolution(-987, new ClassCastException())),                           false, "UNTYPED-MUTABLE",  PromiseState.REJECTED, null, -987,  ClassCastException.class},
        {suppResolvedMutablePromise.get(rejectedResolution("pqr", new Throwable())),                                  false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, "pqr", Throwable.class},
        {suppThenableResolve.get(throwException(new Exception())),                                                    false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, null,  Exception.class},
        {suppThenableResolve.get(noWait(testUntypedPromise(callOnRejected('F', new RuntimeException())))),            false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, 'F',   RuntimeException.class},
        {suppThenableResolve.get(noWait(UntypedPromiseImpl.factory.rejectedPromise(-6.5, new ClassCastException()))), false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, -6.5,  ClassCastException.class},
        {suppThenableResolve.get(suppPendingMutablePromise.get(rejectedResolution(false, new Throwable()))),          false, "UNTYPED-MUTABLE",  PromiseState.REJECTED, null, false, Throwable.class},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(rejectedResolution(true, new Exception()))),          false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, true,  Exception.class},
        {retThenableReject(-654, new RuntimeException()),                                                             false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, -654,  RuntimeException.class},
        {retThenableReject("xyz"),                                                                                    false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, "xyz", null},
        {retThenableReject(new ClassCastException()),                                                                 false, "UNTYPED-REJECTED", PromiseState.REJECTED, null, null,  ClassCastException.class},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsPendingResolution() { return new Object[][] {
        {noWait(testUntypedPromise(callNothing)),                                            false, "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {noWait(UntypedPromiseImpl.factory.alwaysPendingPromise()),                          true,  "UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {retPendingPromise,                                                                  false, "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppPendingMutablePromise.get(alwaysPendingResolution()),                           true,  "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppResolvedMutablePromise.get(alwaysPendingResolution()),                          true,  "UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(noWait(testUntypedPromise(callNothing))),                   false, "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(noWait(UntypedPromiseImpl.factory.alwaysPendingPromise())), true,  "UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(retPendingPromise),                                         false, "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(alwaysPendingResolution())),  true,  "UNTYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(alwaysPendingResolution())), true,  "UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsResolution()
    {
        return TestData.union(
            paramsFulfilledResolution(),
            paramsRejectedResolution(),
            paramsPendingResolution()
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFactoryFulfilledPromise(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        Assert.assertThat(
            UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue),
            promiseMatcher("UNTYPED-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseUnlimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(fulfilledValue, UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue).await());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseLimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(
            fulfilledValue,
            UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue).await(1, TimeUnit.SECONDS)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseApplyResolveAction(final Object fulfilledValue)
    {
        new StrictExpectations() {{
            resolveActionMock.setFulfilled(fulfilledValue);
        }};

        UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue).applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseNullOnFulfilled(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final UntypedPromiseImpl promise = UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue)
            .doThen(exec, null, unusedOnRejected);

        Assert.assertThat(
            promise,
            promiseMatcher("UNTYPED-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null)
        );

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsFulfilledPromiseResolve()
    {
        return TestData.merge(PromiseTestData.fulfilled(), paramsResolution());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledPromiseResolve")
    public final void testFulfilledPromiseResolveBeforeCallback(
        final Object fulfilledValue,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue).doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Object>() { @Override public Object call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retResolution.call(UntypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
            }},
            unusedOnRejected
        );

        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher(expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledPromiseResolve")
    public final void testFulfilledPromiseResolveAfterCallback(
        final Object fulfilledValue,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = promiseDoThen(
            UntypedPromiseImpl.factory.fulfilledPromise(fulfilledValue),
            new FR1<Object, Object>() { @Override public Object call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retResolution.call(UntypedPromiseImplTest.this, callbackStep, resolveStep);
            }},
            unusedOnRejected,
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("UNTYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testFactoryRejectedPromise(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {};

        Assert.assertThat(
            UntypedPromiseImpl.factory.rejectedPromise(rejectedReason, rejectedException),
            promiseMatcher("UNTYPED-REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseUnlimitedAwait(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) throws Exception {
        new StrictExpectations() {};

        try {
            UntypedPromiseImpl.factory.rejectedPromise(rejectedReason, rejectedException).await();
            Assert.fail();
        } catch (final PromiseRejectedException e) {
            Assert.assertEquals(rejectedReason, e.reason());
            Assert.assertEquals(rejectedException, e.exception());
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseLimitedAwait(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) throws Exception {
        new StrictExpectations() {};

        try {
            UntypedPromiseImpl.factory.rejectedPromise(rejectedReason, rejectedException).await(1, TimeUnit.SECONDS);
            Assert.fail();
        } catch (final PromiseRejectedException e) {
            Assert.assertEquals(rejectedReason, e.reason());
            Assert.assertEquals(rejectedException, e.exception());
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseApplyResolveAction(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {{
            resolveActionMock.setRejected(rejectedReason, rejectedException);
        }};

        UntypedPromiseImpl.factory.rejectedPromise(rejectedReason, rejectedException)
            .applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseNullOnRejected(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final UntypedPromiseImpl promise = UntypedPromiseImpl.factory
            .rejectedPromise(rejectedReason, rejectedException)
            .doThen(exec, unusedOnFulfilled, null);

        Assert.assertThat(
            promise,
            promiseMatcher("UNTYPED-REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
        );

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsRejectedPromiseResolve()
    {
        return TestData.merge(PromiseTestData.rejected(), paramsResolution());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedPromiseResolve")
    public final void testRejectedPromiseResolveBeforeCallback(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedReason, rejectedException);
        }};

        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = UntypedPromiseImpl.factory
            .rejectedPromise(rejectedReason, rejectedException)
            .doThen(
                ImplUtil.CURRENT_THREAD_EXECUTOR,
                unusedOnFulfilled,
                new FR2<Object, Throwable, Object>() {
                    @Override public Object call(final Object reason, final Throwable exception) throws Throwable {
                        loggerMock.log("onRejected", reason, exception);
                        return retResolution.call(UntypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
                    }
                }
            );

        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher(expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedPromiseResolve")
    public final void testRejectedPromiseResolveAfterCallback(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedReason, rejectedException);
        }};

        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = promiseDoThen(
            UntypedPromiseImpl.factory.rejectedPromise(rejectedReason, rejectedException),
            unusedOnFulfilled,
            new FR2<Object, Throwable, Object>() {
                @Override public Object call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retResolution.call(UntypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("UNTYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testFactoryAlwaysPendingPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            UntypedPromiseImpl.factory.alwaysPendingPromise(),
            promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test(expected = InterruptedException.class)
    public final void testAlwaysPendingPromiseUnlimitedAwait() throws Exception
    {
        new StrictExpectations() {};

        final Thread testThread = Thread.currentThread();

        new Thread() {@Override public void run() {
            TestUtil.sleep(100);
            testThread.interrupt();
        }}.start();

        UntypedPromiseImpl.factory.alwaysPendingPromise().await();
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test(expected = TimeoutException.class)
    public final void testAlwaysPendingPromiseLimitedAwait() throws Exception
    {
        new StrictExpectations() {};

        UntypedPromiseImpl.factory.alwaysPendingPromise().await(100, TimeUnit.MILLISECONDS);
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseApplyResolveAction()
    {
        new StrictExpectations() {{
            resolveActionMock.setAlwaysPending();
        }};

        UntypedPromiseImpl.factory.alwaysPendingPromise().applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsAlwaysPendingDoThen()
    {
        return new Object[][] {{
            new FR2<UntypedPromiseImplTest, Executor, UntypedPromiseImpl>() {
                @Override public UntypedPromiseImpl call(final UntypedPromiseImplTest self, final Executor exec) {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<UntypedPromiseImplTest, Executor, UntypedPromiseImpl>() {
                @Override public UntypedPromiseImpl call(final UntypedPromiseImplTest self, final Executor exec) {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise()
                        .doThen(exec, null, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<UntypedPromiseImplTest, Executor, UntypedPromiseImpl>() {
                @Override public UntypedPromiseImpl call(final UntypedPromiseImplTest self, final Executor exec) {
                    return UntypedPromiseImpl.factory.alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, null);
                }
            }
        }};
    }

    @Test
    @Parameters(method = "paramsAlwaysPendingDoThen")
    public final void
    testAlwaysPendingDoThen(final FR2<UntypedPromiseImplTest, Executor, UntypedPromiseImpl> retSrcPromise)
        throws Throwable
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        Assert.assertThat(
            retSrcPromise.call(this, exec),
            promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
        );

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsPrependMutable(final Object[][] baseParams)
    {
        return TestData.product(
            new Object[][] {
                {suppPendingMutablePromise, false},
                {suppResolvedMutablePromise, true}
            },
            baseParams
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsMutablePromise()
    {
        return paramsPrependMutable(paramsResolution());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testFactoryMutablePromise(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppPromise,
        final boolean isResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final UntypedPromiseImpl promise = suppPromise.get(retResolution).call(this, new TestStep().pass(), step);

        final Matcher<UntypedPromiseImpl> resolveMatcher = promiseMatcher(
            "UNTYPED-MUTABLE",
            expectedState, expectedValue, expectedReason, expectedExceptionClass
        );

        final Matcher<UntypedPromiseImpl> presyncMatcher = !isResolved
            ? promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null)
            : resolveMatcher;

        Assert.assertThat(promise, presyncMatcher);
        step.sync();
        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseUnlimitedAwait(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppPromise,
        final boolean isResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep threadStep = new TestStep();
        final TestStep promiseStep = new TestStep();

        if (!isResolved || expectedState == PromiseState.PENDING) {
            new StrictExpectations() {{
                loggerMock.log("sync");
                loggerMock.log("await", expectedState, expectedValue, expectedReason, expectedExceptionClass);
            }};

            threadStep.pass();
        } else
            new StrictExpectations() {{
                loggerMock.log("await", expectedState, expectedValue, expectedReason, expectedExceptionClass);
                loggerMock.log("sync");
            }};

        final UntypedPromiseImpl promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), promiseStep);

        final Thread testThread = Thread.currentThread();

        new Thread() { @Override public void run() {
            threadStep.pause();
            loggerMock.log("sync");
            promiseStep.sync();

            if (expectedState == PromiseState.PENDING) {
                TestUtil.sleep(100);
                testThread.interrupt();
            }

            threadStep.finish();
        }}.start();

        try {
            final Object value = promise.await();
            loggerMock.log("await", PromiseState.FULFILLED, value, null, null);
        } catch (final PromiseRejectedException e) {
            loggerMock.log("await", PromiseState.REJECTED, null, e.reason(), TestUtil.exceptionClass(e.exception()));
        } catch (final InterruptedException e) {
            loggerMock.log("await", PromiseState.PENDING, null, null, null);
        }

        threadStep.sync();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseLimitedAwait(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppPromise,
        final boolean isResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep threadStep = new TestStep();
        final TestStep promiseStep = new TestStep();

        if (!isResolved || expectedState == PromiseState.PENDING) {
            new StrictExpectations() {{
                loggerMock.log("sync");
                loggerMock.log("await", expectedState, expectedValue, expectedReason, expectedExceptionClass);
            }};

            threadStep.pass();
        } else
            new StrictExpectations() {{
                loggerMock.log("await", expectedState, expectedValue, expectedReason, expectedExceptionClass);
                loggerMock.log("sync");
            }};

        final UntypedPromiseImpl promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), promiseStep);

        new Thread() { @Override public void run() {
            threadStep.pause();
            loggerMock.log("sync");
            promiseStep.sync();
            threadStep.finish();
        }}.start();

        try {
            final Object value = promise.await(100, TimeUnit.MILLISECONDS);
            loggerMock.log("await", PromiseState.FULFILLED, value, null, null);
        } catch (final PromiseRejectedException e) {
            loggerMock.log("await", PromiseState.REJECTED, null, e.reason(), TestUtil.exceptionClass(e.exception()));
        } catch (final TimeoutException e) {
            loggerMock.log("await", PromiseState.PENDING, null, null, null);
        }

        threadStep.sync();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseApplyResolveAction(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppPromise,
        final boolean isResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        switch (expectedState) {
        case PENDING:
            if (!isAlwaysPending)
                new StrictExpectations() {{
                    loggerMock.log("sync");
                }};
            else if (!isResolved)
                new StrictExpectations() {{
                    loggerMock.log("sync");
                    loggerMock.log("setAlwaysPending");
                }};
            else
                new StrictExpectations() {{
                    loggerMock.log("setAlwaysPending");
                    loggerMock.log("sync");
                }};
            break;

        case FULFILLED:
            if (!isResolved)
                new StrictExpectations() {{
                    loggerMock.log("sync");
                    loggerMock.log("setFulfilled", expectedValue);
                }};
            else
                new StrictExpectations() {{
                    loggerMock.log("setFulfilled", expectedValue);
                    loggerMock.log("sync");
                }};
            break;

        case REJECTED:
            if (!isResolved)
                new StrictExpectations() {{
                    loggerMock.log("sync");
                    loggerMock.log("setRejected", expectedReason, expectedExceptionClass);
                }};
            else
                new StrictExpectations() {{
                    loggerMock.log("setRejected", expectedReason, expectedExceptionClass);
                    loggerMock.log("sync");
                }};
            break;

        default:
            Assert.fail();
        }

        final TestStep promiseStep = new TestStep();

        suppPromise.get(retResolution).call(this, new TestStep().pass(), promiseStep).applyResolveAction(
            new ResolveAction() {
                @Override public void setAlwaysPending() { loggerMock.log("setAlwaysPending"); }
                @Override public void setFulfilled(final Object v) { loggerMock.log("setFulfilled", v); }

                @Override public void setRejected(final Object r, final Throwable e) {
                    loggerMock.log("setRejected", r, TestUtil.exceptionClass(e));
                }
            }
        );

        loggerMock.log("sync");
        promiseStep.sync();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsFulfilledMutablePromise()
    {
        return paramsPrependMutable(paramsFulfilledResolution());
    }

    @Test
    @Parameters(method = "paramsFulfilledMutablePromise")
    public final void testMutablePromiseNullOnFulfilled(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final UntypedPromiseImpl promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, null, unusedOnRejected);

        final Matcher<UntypedPromiseImpl> presyncMatcher;
        final Matcher<UntypedPromiseImpl> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null);
            resolveMatcher = promiseMatcher("UNTYPED-MUTABLE", PromiseState.FULFILLED, expectedValue, null, null);
        } else
            presyncMatcher = resolveMatcher
                = promiseMatcher("UNTYPED-FULFILLED", PromiseState.FULFILLED, expectedValue, null, null);

        Assert.assertThat(promise, presyncMatcher);
        step.sync();
        Assert.assertThat(promise, resolveMatcher);

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsRejectedMutablePromise()
    {
        return paramsPrependMutable(paramsRejectedResolution());
    }

    @Test
    @Parameters(method = "paramsRejectedMutablePromise")
    public final void testMutablePromiseNullOnRejected(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final UntypedPromiseImpl promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, null);

        final Matcher<UntypedPromiseImpl> presyncMatcher;
        final Matcher<UntypedPromiseImpl> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null);

            resolveMatcher = promiseMatcher(
                "UNTYPED-MUTABLE",
                PromiseState.REJECTED, null, expectedReason, expectedExceptionClass
            );
        } else
            presyncMatcher = resolveMatcher = promiseMatcher(
                "UNTYPED-REJECTED",
                PromiseState.REJECTED, null, expectedReason, expectedExceptionClass
            );

        Assert.assertThat(promise, presyncMatcher);
        step.sync();
        Assert.assertThat(promise, resolveMatcher);

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsPendingMutablePromise()
    {
        return paramsPrependMutable(paramsPendingResolution());
    }

    @Test
    @Parameters(method = "paramsPendingMutablePromise")
    public final void testPendingMutablePromiseDoThen(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final UntypedPromiseImpl promise = suppSrcPromise.get(retResolution).call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, unusedOnRejected);

        final String chainDstPromiseType = !isAlwaysPending ? "UNTYPED-MUTABLE" : "UNTYPED-ALWAYS-PENDING";
        final String promiseType = !isSrcResolved ? "UNTYPED-MUTABLE" : chainDstPromiseType;

        Assert.assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null, null));

        step.sync();

        Assert.assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null, null));

        Assert.assertThat(
            promise.doThen(exec, unusedOnFulfilled, unusedOnRejected),
            promiseMatcher(chainDstPromiseType, PromiseState.PENDING, null, null, null)
        );

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseResolveBeforeCallback(
        final TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl> retSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<UntypedPromiseImplTest, ?> retCallback = suppCallbackReturn.get(retResolution);
        final TestStep srcPromiseStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep).doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Object>() { @Override public Object call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retCallback.call(UntypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
            }},
            new FR2<Object, Throwable, Object>() {
                @Override public Object call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retCallback.call(UntypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }
            }
        );

        srcPromiseStep.sync();
        resolveStep.sync();

        final Matcher<UntypedPromiseImpl> resolveMatcher = !isSrcResolved
            ? promiseMatcher("UNTYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
            : promiseMatcher(expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass);

        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseResolveAfterCallback(
        final TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl> retSrcPromise,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<UntypedPromiseImplTest, ?> retCallback = suppCallbackReturn.get(retResolution);
        final TestStep srcPromiseStep = new TestStep();
        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final UntypedPromiseImpl promise = promiseDoThen(
            retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep),
            new FR1<Object, Object>() { @Override public Object call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retCallback.call(UntypedPromiseImplTest.this, callbackStep, resolveStep);
            }},
            new FR2<Object, Throwable, Object>() {
                @Override public Object call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retCallback.call(UntypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            callbackStep
        );

        srcPromiseStep.sync();
        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("UNTYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsFulfilledMutablePromiseResolve()
    {
        return paramsPrependMutable(TestData.product(
            new Object[][] {{TestStep.suppIdentity()}, {suppThenableResolve}},
            TestData.merge(PromiseTestData.fulfilled(), paramsResolution())
        ));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledMutablePromiseResolve")
    public final void testFulfilledMutablePromiseResolveBeforeCallback(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        testMutablePromiseResolveBeforeCallback(
            suppSrcPromise.get(fulfilledResolution(fulfilledValue)), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledMutablePromiseResolve")
    public final void testFulfilledMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        testMutablePromiseResolveAfterCallback(
            suppSrcPromise.get(fulfilledResolution(fulfilledValue)),
            suppCallbackReturn, retResolution,
            expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsRejectedMutablePromiseResolve()
    {
        return paramsPrependMutable(TestData.product(
            new Object[][] {{TestStep.suppIdentity()}, {suppThenableResolve}},
            TestData.merge(PromiseTestData.rejected(), paramsResolution())
        ));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedMutablePromiseResolve")
    public final void testRejectedMutablePromiseResolveBeforeCallback(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedReason, rejectedException);
        }};

        testMutablePromiseResolveBeforeCallback(
            suppSrcPromise.get(rejectedResolution(rejectedReason, rejectedException)), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedMutablePromiseResolve")
    public final void testRejectedMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, UntypedPromiseImpl> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<UntypedPromiseImplTest, ?> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<UntypedPromiseImplTest, ?> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedReason, rejectedException);
        }};

        testMutablePromiseResolveAfterCallback(
            suppSrcPromise.get(rejectedResolution(rejectedReason, rejectedException)),
            suppCallbackReturn, retResolution,
            expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private UntypedPromiseImpl
    mutablePromiseMultiDoThen(final UntypedPromiseImpl srcPromise, final String id, final Object resolution)
    {
        return srcPromise.doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Object>() { @Override public Object call(final Object value) {
                loggerMock.log(id, "onFulfilled", value);
                return resolution;
            }},
            new FR2<Object, Throwable, Object>() {
                @Override public Object call(final Object reason, final Throwable exception) {
                    loggerMock.log(id, "onRejected", reason, TestUtil.exceptionClass(exception));
                    return resolution;
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void mutablePromiseMultiApplyResolveAction(final UntypedPromiseImpl srcPromise, final String id)
    {
        srcPromise.applyResolveAction(new ResolveAction() {
            @Override public void setAlwaysPending() { loggerMock.log(id, "setAlwaysPending"); }
            @Override public void setFulfilled(final Object value) { loggerMock.log(id, "setFulfilled", value); }

            @Override public void setRejected(final Object reason, final Throwable exception) {
                loggerMock.log(id, "setRejected", reason, TestUtil.exceptionClass(exception));
            }
        });
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseMultiCallback(
        final TestStep.Return<UntypedPromiseImplTest, UntypedPromiseImpl> retSrcPromise,
        final boolean isAlwaysPending
    ) throws Throwable
    {
        final TestStep step = new TestStep();
        final UntypedPromiseImpl srcPromise = retSrcPromise.call(this, new TestStep().pass(), step);

        final UntypedPromiseImpl promise0 = mutablePromiseMultiDoThen(srcPromise, "doThen-0", 123);

        loggerMock.log("seperate-01");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-1");

        loggerMock.log("seperate-12");

        final UntypedPromiseImpl promise2 = mutablePromiseMultiDoThen(
            srcPromise, "doThen-2",
            UntypedPromiseImpl.factory.rejectedPromise("abc", new Throwable())
        );

        loggerMock.log("seperate-23");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-3");

        loggerMock.log("seperate-34");

        final UntypedPromiseImpl promise4 = mutablePromiseMultiDoThen(
            srcPromise, "doThen-4",
            UntypedPromiseImpl.factory.alwaysPendingPromise()
        );

        Assert.assertThat(promise0, promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null));
        Assert.assertThat(promise2, promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null));
        Assert.assertThat(promise4, promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null));

        loggerMock.log("sync", "before");
        step.sync();
        loggerMock.log("sync", "after");

        final Matcher<UntypedPromiseImpl> matcher0 = !isAlwaysPending
            ? promiseMatcher("UNTYPED-MUTABLE", PromiseState.FULFILLED, 123, null, null)
            : promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise0, matcher0);

        final Matcher<UntypedPromiseImpl> matcher2 = !isAlwaysPending
            ? promiseMatcher("UNTYPED-MUTABLE", PromiseState.REJECTED, null, "abc", Throwable.class)
            : promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise2, matcher2);

        final Matcher<UntypedPromiseImpl> matcher4
            = promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise4, matcher4);

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-5");

        loggerMock.log("seperate-56");

        final Matcher<UntypedPromiseImpl> matcher6 = !isAlwaysPending
            ? promiseMatcher("UNTYPED-FULFILLED", PromiseState.FULFILLED, true, null, null)
            : promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null);

        Assert.assertThat(mutablePromiseMultiDoThen(srcPromise, "doThen-6", true), matcher6);

        loggerMock.log("seperate-67");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-7");

        loggerMock.log("seperate-78");

        final Matcher<UntypedPromiseImpl> matcher8 = !isAlwaysPending
            ? promiseMatcher("UNTYPED-REJECTED", PromiseState.REJECTED, null, 'D', Exception.class)
            : promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null);

        Assert.assertThat(
            mutablePromiseMultiDoThen(
                srcPromise, "doThen-8",
                UntypedPromiseImpl.factory.rejectedPromise('D', new Exception())
            ),
            matcher8
        );

        loggerMock.log("seperate-89");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-9");
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testFulfilledMutablePromiseMultiCallback() throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("seperate-01");
            loggerMock.log("seperate-12");
            loggerMock.log("seperate-23");
            loggerMock.log("seperate-34");
            loggerMock.log("sync", "before");
            loggerMock.log("doThen-0", "onFulfilled", "test");
            loggerMock.log("applyResolveAction-1", "setFulfilled", "test");
            loggerMock.log("doThen-2", "onFulfilled", "test");
            loggerMock.log("applyResolveAction-3", "setFulfilled", "test");
            loggerMock.log("doThen-4", "onFulfilled", "test");
            loggerMock.log("sync", "after");
            loggerMock.log("applyResolveAction-5", "setFulfilled", "test");
            loggerMock.log("seperate-56");
            loggerMock.log("doThen-6", "onFulfilled", "test");
            loggerMock.log("seperate-67");
            loggerMock.log("applyResolveAction-7", "setFulfilled", "test");
            loggerMock.log("seperate-78");
            loggerMock.log("doThen-8", "onFulfilled", "test");
            loggerMock.log("seperate-89");
            loggerMock.log("applyResolveAction-9", "setFulfilled", "test");
        }};

        testMutablePromiseMultiCallback(suppPendingMutablePromise.get(fulfilledResolution("test")), false);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testRejectedMutablePromiseMultiCallback() throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("seperate-01");
            loggerMock.log("seperate-12");
            loggerMock.log("seperate-23");
            loggerMock.log("seperate-34");
            loggerMock.log("sync", "before");
            loggerMock.log("doThen-0", "onRejected", "test", RuntimeException.class);
            loggerMock.log("applyResolveAction-1", "setRejected", "test", RuntimeException.class);
            loggerMock.log("doThen-2", "onRejected", "test", RuntimeException.class);
            loggerMock.log("applyResolveAction-3", "setRejected", "test", RuntimeException.class);
            loggerMock.log("doThen-4", "onRejected", "test", RuntimeException.class);
            loggerMock.log("sync", "after");
            loggerMock.log("applyResolveAction-5", "setRejected", "test", RuntimeException.class);
            loggerMock.log("seperate-56");
            loggerMock.log("doThen-6", "onRejected", "test", RuntimeException.class);
            loggerMock.log("seperate-67");
            loggerMock.log("applyResolveAction-7", "setRejected", "test", RuntimeException.class);
            loggerMock.log("seperate-78");
            loggerMock.log("doThen-8", "onRejected", "test", RuntimeException.class);
            loggerMock.log("seperate-89");
            loggerMock.log("applyResolveAction-9", "setRejected", "test", RuntimeException.class);
        }};

        testMutablePromiseMultiCallback(
            suppPendingMutablePromise.get(rejectedResolution("test", new RuntimeException())),
            false
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingMutablePromiseMultiCallback() throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("seperate-01");
            loggerMock.log("seperate-12");
            loggerMock.log("seperate-23");
            loggerMock.log("seperate-34");
            loggerMock.log("sync", "before");
            loggerMock.log("applyResolveAction-1", "setAlwaysPending");
            loggerMock.log("applyResolveAction-3", "setAlwaysPending");
            loggerMock.log("sync", "after");
            loggerMock.log("applyResolveAction-5", "setAlwaysPending");
            loggerMock.log("seperate-56");
            loggerMock.log("seperate-67");
            loggerMock.log("applyResolveAction-7", "setAlwaysPending");
            loggerMock.log("seperate-78");
            loggerMock.log("seperate-89");
            loggerMock.log("applyResolveAction-9", "setAlwaysPending");
        }};

        testMutablePromiseMultiCallback(suppPendingMutablePromise.get(alwaysPendingResolution()), true);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
