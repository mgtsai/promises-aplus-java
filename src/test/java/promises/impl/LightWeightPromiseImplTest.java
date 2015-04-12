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
import promises.PromiseState;
import promises.TestData;
import promises.TestLogger;
import promises.TestStep;
import promises.TestUtil;
import promises.lw.P;
import promises.lw.RV;
import promises.lw.RejP;
import promises.lw.ResP;
import promises.lw.Thenable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
@RunWith(JUnitParamsRunner.class)
public final class LightWeightPromiseImplTest
{
    //-----------------------------------------------------------------------------------------------------------------
    @Injectable private TestLogger loggerMock = null;
    @Injectable private ResolveAction resolveActionMock = null;
    @Injectable private FR1<Object, RV<?>> unusedOnFulfilled = null;
    @Injectable private FR1<Throwable, RV<?>> unusedOnRejected = null;
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR1<Object, RV<?>> defaultOnFulfilled = new FR1<Object, RV<?>>() {
        @Override public RV<?> call(final Object value) {
            return new RV<Object>() {
                @Override public Object value() { return value; }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR1<Throwable, RV<?>> defaultOnRejected = new FR1<Throwable, RV<?>>() {
        @Override public RV<?> call(final Throwable exception) {
            return LightWeightPromiseImpl.factory().rejectedPromise(null, exception);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>>
    callNothing = new F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>>() {
        @Override public void call(final FR1<Object, RV<?>> onFulfilled, final FR1<Throwable, RV<?>> onRejected) { }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>
    retPendingPromise = new TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>() {
        @Override public LightWeightPromiseImpl<Object>
        call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
            cbStep.pause();
            resStep.finish();

            return LightWeightPromiseImpl.factory().fulfilledPromise(null)
                .doThen(TestUtil.NOP_EXECUTOR, self.unusedOnFulfilled, self.unusedOnRejected);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<Object>>
    suppPendingMutablePromise
    = new TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<Object>>() {
        @Override public TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>
        get(final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution) {
            return new TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>() {
                @Override public LightWeightPromiseImpl<Object>
                call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    cbStep.pause();
                    final TestStep chainResolveStep = new TestStep();

                    return promiseDoThen(
                        LightWeightPromiseImpl.factory().fulfilledPromise(null),
                        new FR1<Object, RV<?>>() {
                            @Override public RV<?> call(final Object value) throws Throwable {
                                resStep.pause();
                                return retResolution.call(self, cbStep, chainResolveStep);
                            }
                        },
                        self.unusedOnRejected,
                        resStep,
                        chainResolveStep
                    );
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<Object>>
    suppResolvedMutablePromise
    = new TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<Object>>() {
        @Override public TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>
        get(final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution) {
            return new TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<Object>>() {
                @Override public LightWeightPromiseImpl<Object>
                call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep)
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
    private static final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, Thenable<Object>>
    suppThenableResolve = new TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, Thenable<Object>>() {
        @Override public TestStep.Return<LightWeightPromiseImplTest, Thenable<Object>>
        get(final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution) {
            return new TestStep.Return<LightWeightPromiseImplTest, Thenable<Object>>() {
                @Override public Thenable<Object>
                call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    return new Thenable<Object>() {
                        @Override public void then(final ResP<Object> resP, final RejP rejP) throws Throwable {
                            resP.resolve(retResolution.call(self, cbStep, resStep));
                        }
                    };
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static Matcher<LightWeightPromiseImpl<?>> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<LightWeightPromiseImpl<?>>() {
            @Override protected boolean matchesSafely(final LightWeightPromiseImpl<?> item) {
                return TestUtil.equals(item.type(), expectedType)
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Light-weight promise ").appendValueList(
                    "[", ", ", "]",
                    expectedType, expectedState, expectedValue, expectedExceptionClass
                );
            }

            @Override protected void
            describeMismatchSafely(final LightWeightPromiseImpl<?> item, final Description desc) {
                desc.appendValueList("[", ", ", "]", item.type(), item.state(), item.value(), item.exception());
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> RV<V> fulfilledResolution(final V value)
    {
        return new RV<V>() {
            @Override public V value() { return value; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> RV<V> rejectedResolution(final Throwable exception)
    {
        return LightWeightPromiseImpl.<V>factory().rejectedPromise(null, exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> RV<V> alwaysPendingResolution()
    {
        return LightWeightPromiseImpl.<V>factory().alwaysPendingPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>> callOnFulfilled(final Object fulfilledValue)
    {
        return new F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>>() {
            @Override public void
            call(final FR1<Object, RV<?>> onFulfilled, final FR1<Throwable, RV<?>> onRejected) throws Throwable {
                onFulfilled.call(fulfilledValue);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>> callOnRejected(final Throwable rejectedException)
    {
        return new F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>>() {
            @Override public void
            call(final FR1<Object, RV<?>> onFulfilled, final FR1<Throwable, RV<?>> onRejected) throws Throwable {
                onRejected.call(rejectedException);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> P<V> testLightWeightPromise(final F2<FR1<Object, RV<?>>, FR1<Throwable, RV<?>>> thenCall)
    {
        return new P<V>() {
            @Override public PromiseState state() { return null; }
            @Override public V value() { return null; }
            @Override public Throwable exception() { return null; }
            @Override public V await() { return null; }
            @Override public V await(final long timeout, final TimeUnit unit) { return null; }
            @Override public promises.Promise toUntypedPromise() { return null; }
            @Override public <R> promises.typed.Promise<V, R> toTypedPromise() { return null; }

            @Override public <VO> P<VO> then(
                final Executor exec,
                final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
                final FR1<Throwable, ? extends RV<? extends VO>> onRejected
            ) {
                try {
                    thenCall.call(
                        onFulfilled != null ? ImplUtil.<FR1<Object, RV<?>>>cast(onFulfilled) : defaultOnFulfilled,
                        onRejected != null ? ImplUtil.<FR1<Throwable, RV<?>>>cast(onRejected) : defaultOnRejected
                    );
                } catch (final Throwable e) {
                    //
                }

                return null;
            }

            @Override public <VO> P<VO>
            then(final Executor exec, final FR1<? super V, ? extends RV<? extends VO>> onFulfilled) {
                return then(exec, onFulfilled, null);
            }

            @Override public <VO> P<VO> then(
                final FR1<? super V, ? extends RV<? extends VO>> onFulfilled,
                final FR1<Throwable, ? extends RV<? extends VO>> onRejected
            ) {
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
            }

            @Override public <VO> P<VO> then(final FR1<? super V, ? extends RV<? extends VO>> onFulfilled) {
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <VI, VO> LightWeightPromiseImpl<VO> promiseDoThen(
        final LightWeightPromiseImpl<VI> srcPromise,
        final FR1<? super VI, ? extends RV<? extends VO>> onFulfilled,
        final FR1<Throwable, ? extends RV<? extends VO>> onRejected,
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
    private static <T extends RV<?>> TestStep.Return<LightWeightPromiseImplTest, T> retNoWait(final T ret)
    {
        return TestStep.retNoWait(ret);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <T extends RV<?>> TestStep.Return<LightWeightPromiseImplTest, T>
    retThrowException(final Throwable exception)
    {
        return TestStep.retThrowException(exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> TestStep.Return<LightWeightPromiseImplTest, Thenable<V>> retThenableResolveValue(final V value)
    {
        return new TestStep.Return<LightWeightPromiseImplTest, Thenable<V>>() {
            @Override public Thenable<V>
            call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V>() { @Override public void then(final ResP<V> resP, final RejP rejP) {
                    cbStep.pause();
                    resP.resolve(value);
                    resStep.finish();
                }};
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V> TestStep.Return<LightWeightPromiseImplTest, Thenable<V>>
    retThenableReject(final Throwable exception)
    {
        return new TestStep.Return<LightWeightPromiseImplTest, Thenable<V>>() {
            @Override public Thenable<V>
            call(final LightWeightPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V>() { @Override public void then(final ResP<V> resP, final RejP rejP) {
                    cbStep.pause();
                    rejP.reject(exception);
                    resStep.finish();
                }};
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsFulfilledResolution() { return new Object[][] {
        {retNoWait(fulfilledResolution(null)),                                                          false, "LW-FULFILLED", PromiseState.FULFILLED, null,  null},
        {retNoWait(fulfilledResolution(123)),                                                           false, "LW-FULFILLED", PromiseState.FULFILLED, 123,   null},
        {retNoWait(testLightWeightPromise(callOnFulfilled("abc"))),                                     false, "LW-FULFILLED", PromiseState.FULFILLED, "abc", null},
        {retNoWait(LightWeightPromiseImpl.factory().fulfilledPromise('D')),                             false, "LW-FULFILLED", PromiseState.FULFILLED, 'D',   null},
        {suppPendingMutablePromise.get(retNoWait(fulfilledResolution(4.5))),                            false, "LW-MUTABLE",   PromiseState.FULFILLED, 4.5,   null},
        {suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(false))),                         false, "LW-FULFILLED", PromiseState.FULFILLED, false, null},
        {retThenableResolveValue(6543L),                                                                false, "LW-FULFILLED", PromiseState.FULFILLED, 6543L, null},
        {suppThenableResolve.get(retNoWait(fulfilledResolution(null))),                                 false, "LW-FULFILLED", PromiseState.FULFILLED, null,  null},
        {suppThenableResolve.get(retNoWait(fulfilledResolution(true))),                                 false, "LW-FULFILLED", PromiseState.FULFILLED, true,  null},
        {suppThenableResolve.get(retNoWait(testLightWeightPromise(callOnFulfilled(678)))),              false, "LW-FULFILLED", PromiseState.FULFILLED, 678,   null},
        {suppThenableResolve.get(retNoWait(LightWeightPromiseImpl.factory().fulfilledPromise("ijk"))),  false, "LW-FULFILLED", PromiseState.FULFILLED, "ijk", null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(fulfilledResolution('E')))),   false, "LW-MUTABLE",   PromiseState.FULFILLED, 'E',   null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(-9.0)))), false, "LW-FULFILLED", PromiseState.FULFILLED, -9.0,  null},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsRejectedResolution() { return new Object[][] {
        {retThrowException(new Throwable()),                                                                                   false, "LW-REJECTED", PromiseState.REJECTED, null, Throwable.class},
        {retNoWait(testLightWeightPromise(callOnRejected(new Exception()))),                                                   false, "LW-REJECTED", PromiseState.REJECTED, null, Exception.class},
        {retNoWait(LightWeightPromiseImpl.factory().rejectedPromise(null, new RuntimeException())),                            false, "LW-REJECTED", PromiseState.REJECTED, null, RuntimeException.class},
        {suppPendingMutablePromise.get(retNoWait(rejectedResolution(new ClassCastException()))),                               false, "LW-MUTABLE",  PromiseState.REJECTED, null, ClassCastException.class},
        {suppResolvedMutablePromise.get(retNoWait(rejectedResolution(new Throwable()))),                                       false, "LW-REJECTED", PromiseState.REJECTED, null, Throwable.class},
        {suppThenableResolve.get(retThrowException(new Exception())),                                                          false, "LW-REJECTED", PromiseState.REJECTED, null, Exception.class},
        {suppThenableResolve.get(retNoWait(testLightWeightPromise(callOnRejected(new RuntimeException())))),                   false, "LW-REJECTED", PromiseState.REJECTED, null, RuntimeException.class},
        {suppThenableResolve.get(retNoWait(LightWeightPromiseImpl.factory().rejectedPromise(null, new ClassCastException()))), false, "LW-REJECTED", PromiseState.REJECTED, null, ClassCastException.class},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(rejectedResolution(new Throwable())))),               false, "LW-MUTABLE",  PromiseState.REJECTED, null, Throwable.class},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(rejectedResolution(new Exception())))),              false, "LW-REJECTED", PromiseState.REJECTED, null, Exception.class},
        {retThenableReject(new RuntimeException()),                                                                            false, "LW-REJECTED", PromiseState.REJECTED, null, RuntimeException.class},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsPendingResolution() { return new Object[][] {
        {retNoWait(testLightWeightPromise(callNothing)),                                                false, "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {retNoWait(LightWeightPromiseImpl.factory().alwaysPendingPromise()),                            true,  "LW-ALWAYS-PENDING", PromiseState.PENDING, null, null},
        {retPendingPromise,                                                                             false, "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution())),                           true,  "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution())),                          true,  "LW-ALWAYS-PENDING", PromiseState.PENDING, null, null},
        {suppThenableResolve.get(retNoWait(testLightWeightPromise(callNothing))),                       false, "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {suppThenableResolve.get(retNoWait(LightWeightPromiseImpl.factory().alwaysPendingPromise())),   true,  "LW-ALWAYS-PENDING", PromiseState.PENDING, null, null},
        {suppThenableResolve.get(retPendingPromise),                                                    false, "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution()))),  true,  "LW-MUTABLE",        PromiseState.PENDING, null, null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution()))), true,  "LW-ALWAYS-PENDING", PromiseState.PENDING, null, null},
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
            LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue),
            promiseMatcher("LW-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseUnlimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(fulfilledValue, LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue).await());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseLimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(
            fulfilledValue,
            LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue).await(1, TimeUnit.SECONDS)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseToUntypedPromise(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue).toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher(
                "UNTYPED-FULFILLED",
                PromiseState.FULFILLED, fulfilledValue, null, null
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseToTypedPromise(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue).toTypedPromise(),
            TypedPromiseImplTest.promiseMatcher("TYPED-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null)
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

        LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue).applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseNullOnFulfilled(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final LightWeightPromiseImpl<?> promise = LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue)
            .doThen(exec, null, unusedOnRejected);

        Assert.assertThat(promise, promiseMatcher("LW-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null));

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
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = LightWeightPromiseImpl.factory()
            .fulfilledPromise(fulfilledValue)
            .doThen(
                ImplUtil.CURRENT_THREAD_EXECUTOR,
                new FR1<Object, RV<?>>() { @Override public RV<?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retResolution.call(LightWeightPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }},
                unusedOnRejected
            );

        resolveStep.sync();

        Assert.assertThat(promise, promiseMatcher(expectedType, expectedState, expectedValue, expectedExceptionClass));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledPromiseResolve")
    public final void testFulfilledPromiseResolveAfterCallback(
        final Object fulfilledValue,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = promiseDoThen(
            LightWeightPromiseImpl.factory().fulfilledPromise(fulfilledValue),
            new FR1<Object, RV<?>>() { @Override public RV<?> call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retResolution.call(LightWeightPromiseImplTest.this, callbackStep, resolveStep);
            }},
            unusedOnRejected,
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(promise, promiseMatcher("LW-MUTABLE", expectedState, expectedValue, expectedExceptionClass));
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
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException),
            promiseMatcher("LW-REJECTED", PromiseState.REJECTED, null, rejectedExceptionClass)
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
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException).await();
            Assert.fail();
        } catch (final PromiseRejectedException e) {
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
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException).await(1, TimeUnit.SECONDS);
            Assert.fail();
        } catch (final PromiseRejectedException e) {
            Assert.assertEquals(rejectedException, e.exception());
        }
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseToUntypedPromise(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException).toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher(
                "UNTYPED-REJECTED",
                PromiseState.REJECTED, null, null, rejectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseToTypedPromise(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException).toTypedPromise(),
            TypedPromiseImplTest.promiseMatcher(
                "TYPED-REJECTED",
                PromiseState.REJECTED, null, null, rejectedExceptionClass
            )
        );
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
            resolveActionMock.setRejected(null, rejectedException);
        }};

        LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException)
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

        final LightWeightPromiseImpl<?> promise = LightWeightPromiseImpl.factory()
            .rejectedPromise(null, rejectedException)
            .doThen(exec, unusedOnFulfilled, null);

        Assert.assertThat(promise, promiseMatcher("LW-REJECTED", PromiseState.REJECTED, null, rejectedExceptionClass));

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
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedException);
        }};

        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = LightWeightPromiseImpl.factory()
            .rejectedPromise(null, rejectedException)
            .doThen(
                ImplUtil.CURRENT_THREAD_EXECUTOR,
                unusedOnFulfilled,
                new FR1<Throwable, RV<?>>() { @Override public RV<?> call(final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", exception);
                    return retResolution.call(LightWeightPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }}
            );

        resolveStep.sync();

        Assert.assertThat(promise, promiseMatcher(expectedType, expectedState, expectedValue, expectedExceptionClass));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedPromiseResolve")
    public final void testRejectedPromiseResolveAfterCallback(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedException);
        }};

        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = promiseDoThen(
            LightWeightPromiseImpl.factory().rejectedPromise(null, rejectedException),
            unusedOnFulfilled,
            new FR1<Throwable, RV<?>>() { @Override public RV<?> call(final Throwable exception) throws Throwable {
                loggerMock.log("onRejected", exception);
                return retResolution.call(LightWeightPromiseImplTest.this, callbackStep, resolveStep);
            }},
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(promise, promiseMatcher("LW-MUTABLE", expectedState, expectedValue, expectedExceptionClass));
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testFactoryAlwaysPendingPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().alwaysPendingPromise(),
            promiseMatcher("LW-ALWAYS-PENDING", PromiseState.PENDING, null, null)
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

        LightWeightPromiseImpl.factory().alwaysPendingPromise().await();
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test(expected = TimeoutException.class)
    public final void testAlwaysPendingPromiseLimitedAwait() throws Exception
    {
        new StrictExpectations() {};

        LightWeightPromiseImpl.factory().alwaysPendingPromise().await(100, TimeUnit.MILLISECONDS);
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseToUntypedPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().alwaysPendingPromise().toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseToTypedPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            LightWeightPromiseImpl.factory().alwaysPendingPromise().toTypedPromise(),
            TypedPromiseImplTest.promiseMatcher("TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseApplyResolveAction()
    {
        new StrictExpectations() {{
            resolveActionMock.setAlwaysPending();
        }};

        LightWeightPromiseImpl.factory().alwaysPendingPromise().applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsAlwaysPendingDoThen()
    {
        return new Object[][] {{
            new FR2<LightWeightPromiseImplTest, Executor, LightWeightPromiseImpl<?>>() {
                @Override public LightWeightPromiseImpl<?>
                call(final LightWeightPromiseImplTest self, final Executor exec) {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<LightWeightPromiseImplTest, Executor, LightWeightPromiseImpl<?>>() {
                @Override public LightWeightPromiseImpl<?>
                call(final LightWeightPromiseImplTest self, final Executor exec) {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, null, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<LightWeightPromiseImplTest, Executor, LightWeightPromiseImpl<?>>() {
                @Override public LightWeightPromiseImpl<?>
                call(final LightWeightPromiseImplTest self, final Executor exec) {
                    return LightWeightPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, null);
                }
            }
        }};
    }

    @Test
    @Parameters(method = "paramsAlwaysPendingDoThen")
    public final void
    testAlwaysPendingDoThen(final FR2<LightWeightPromiseImplTest, Executor, LightWeightPromiseImpl<?>> retSrcPromise)
        throws Throwable
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        Assert.assertThat(
            retSrcPromise.call(this, exec),
            promiseMatcher("LW-ALWAYS-PENDING", PromiseState.PENDING, null, null)
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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();

        final LightWeightPromiseImpl<?> promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), step);

        final Matcher<LightWeightPromiseImpl<?>> resolveMatcher
            = promiseMatcher("LW-MUTABLE", expectedState, expectedValue, expectedExceptionClass);

        final Matcher<LightWeightPromiseImpl<?>> presyncMatcher = !isResolved
            ? promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null)
            : resolveMatcher;

        Assert.assertThat(promise, presyncMatcher);
        step.sync();
        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseUnlimitedAwait(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep threadStep = new TestStep();
        final TestStep promiseStep = new TestStep();

        if (!isResolved || expectedState == PromiseState.PENDING) {
            new StrictExpectations() {{
                loggerMock.log("sync");
                loggerMock.log("await", expectedState, expectedValue, expectedExceptionClass);
            }};

            threadStep.pass();
        } else
            new StrictExpectations() {{
                loggerMock.log("await", expectedState, expectedValue, expectedExceptionClass);
                loggerMock.log("sync");
            }};

        final LightWeightPromiseImpl<?> promise = suppPromise.get(retResolution)
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
            loggerMock.log("await", PromiseState.FULFILLED, value, null);
        } catch (final PromiseRejectedException e) {
            loggerMock.log("await", PromiseState.REJECTED, null, TestUtil.exceptionClass(e.exception()));
        } catch (final InterruptedException e) {
            loggerMock.log("await", PromiseState.PENDING, null, null);
        }

        threadStep.sync();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseLimitedAwait(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep threadStep = new TestStep();
        final TestStep promiseStep = new TestStep();

        if (!isResolved || expectedState == PromiseState.PENDING) {
            new StrictExpectations() {{
                loggerMock.log("sync");
                loggerMock.log("await", expectedState, expectedValue, expectedExceptionClass);
            }};

            threadStep.pass();
        } else
            new StrictExpectations() {{
                loggerMock.log("await", expectedState, expectedValue, expectedExceptionClass);
                loggerMock.log("sync");
            }};

        final LightWeightPromiseImpl<?> promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), promiseStep);

        new Thread() { @Override public void run() {
            threadStep.pause();
            loggerMock.log("sync");
            promiseStep.sync();
            threadStep.finish();
        }}.start();

        try {
            final Object value = promise.await(100, TimeUnit.MILLISECONDS);
            loggerMock.log("await", PromiseState.FULFILLED, value, null);
        } catch (final PromiseRejectedException e) {
            loggerMock.log("await", PromiseState.REJECTED, null, TestUtil.exceptionClass(e.exception()));
        } catch (final TimeoutException e) {
            loggerMock.log("await", PromiseState.PENDING, null, null);
        }

        threadStep.sync();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseToUntypedPromise(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();

        final LightWeightPromiseImpl<?> promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), step);

        final UntypedPromiseImpl toPromise1 = promise.toUntypedPromise();

        final String resolvedType
            = expectedState == PromiseState.FULFILLED ? "UNTYPED-FULFILLED"
            : expectedState == PromiseState.REJECTED ? "UNTYPED-REJECTED"
            : isAlwaysPending ? "UNTYPED-ALWAYS-PENDING" : "UNTYPED-MUTABLE";

        final Matcher<UntypedPromiseImpl> resolveMatcher = UntypedPromiseImplTest.promiseMatcher(
            !isResolved ? "UNTYPED-MUTABLE" : resolvedType,
            expectedState, expectedValue, null, expectedExceptionClass
        );

        final Matcher<UntypedPromiseImpl> presyncMatcher = !isResolved
            ? UntypedPromiseImplTest.promiseMatcher("UNTYPED-MUTABLE", PromiseState.PENDING, null, null, null)
            : resolveMatcher;

        Assert.assertThat(toPromise1, presyncMatcher);

        step.sync();

        Assert.assertThat(toPromise1, resolveMatcher);

        Assert.assertThat(
            promise.toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher(
                resolvedType,
                expectedState, expectedValue, null, expectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseToTypedPromise(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();

        final LightWeightPromiseImpl<?> promise = suppPromise.get(retResolution)
            .call(this, new TestStep().pass(), step);

        final TypedPromiseImpl<?, ?> toPromise1 = promise.toTypedPromise();

        final String resolvedType
            = expectedState == PromiseState.FULFILLED ? "TYPED-FULFILLED"
            : expectedState == PromiseState.REJECTED ? "TYPED-REJECTED"
            : isAlwaysPending ? "TYPED-ALWAYS-PENDING" : "TYPED-MUTABLE";

        final Matcher<TypedPromiseImpl<?, ?>> resolveMatcher = TypedPromiseImplTest.promiseMatcher(
            !isResolved ? "TYPED-MUTABLE" : resolvedType,
            expectedState, expectedValue, null, expectedExceptionClass
        );

        final Matcher<TypedPromiseImpl<?, ?>> presyncMatcher = !isResolved
            ? TypedPromiseImplTest.promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null)
            : resolveMatcher;

        Assert.assertThat(toPromise1, presyncMatcher);

        step.sync();

        Assert.assertThat(toPromise1, resolveMatcher);

        Assert.assertThat(
            promise.toTypedPromise(),
            TypedPromiseImplTest.promiseMatcher(
                resolvedType,
                expectedState, expectedValue, null, expectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseApplyResolveAction(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
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
                    loggerMock.log("setRejected", null, expectedExceptionClass);
                }};
            else
                new StrictExpectations() {{
                    loggerMock.log("setRejected", null, expectedExceptionClass);
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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final LightWeightPromiseImpl<?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, null, unusedOnRejected);

        final Matcher<LightWeightPromiseImpl<?>> presyncMatcher;
        final Matcher<LightWeightPromiseImpl<?>> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null);
            resolveMatcher = promiseMatcher("LW-MUTABLE", PromiseState.FULFILLED, expectedValue, null);
        } else
            presyncMatcher = resolveMatcher
                = promiseMatcher("LW-FULFILLED", PromiseState.FULFILLED, expectedValue, null);

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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final LightWeightPromiseImpl<?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, null);

        final Matcher<LightWeightPromiseImpl<?>> presyncMatcher;
        final Matcher<LightWeightPromiseImpl<?>> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null);
            resolveMatcher = promiseMatcher("LW-MUTABLE", PromiseState.REJECTED, null, expectedExceptionClass);
        } else
            presyncMatcher = resolveMatcher
                = promiseMatcher("LW-REJECTED", PromiseState.REJECTED, null, expectedExceptionClass);

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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {};

        final TestStep step = new TestStep();
        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final LightWeightPromiseImpl<?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, unusedOnRejected);

        final String chainDstPromiseType = !isAlwaysPending ? "LW-MUTABLE" : "LW-ALWAYS-PENDING";
        final String promiseType = !isSrcResolved ? "LW-MUTABLE" : chainDstPromiseType;

        Assert.assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null));

        step.sync();

        Assert.assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null));

        Assert.assertThat(
            promise.doThen(exec, unusedOnFulfilled, unusedOnRejected),
            promiseMatcher(chainDstPromiseType, PromiseState.PENDING, null, null)
        );

        exec.shutdown();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseResolveBeforeCallback(
        final TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<?>> retSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retCallback
            = suppCallbackReturn.get(retResolution);

        final TestStep srcPromiseStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep)
            .doThen(
                ImplUtil.CURRENT_THREAD_EXECUTOR,
                new FR1<Object, RV<?>>() { @Override public RV<?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retCallback.call(LightWeightPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }},
                new FR1<Throwable, RV<?>>() { @Override public RV<?> call(final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", exception);
                    return retCallback.call(LightWeightPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }}
            );

        srcPromiseStep.sync();
        resolveStep.sync();

        final Matcher<LightWeightPromiseImpl<?>> resolveMatcher = !isSrcResolved
            ? promiseMatcher("LW-MUTABLE", expectedState, expectedValue, expectedExceptionClass)
            : promiseMatcher(expectedType, expectedState, expectedValue, expectedExceptionClass);

        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseResolveAfterCallback(
        final TestStep.Return<LightWeightPromiseImplTest, LightWeightPromiseImpl<?>> retSrcPromise,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retCallback
            = suppCallbackReturn.get(retResolution);

        final TestStep srcPromiseStep = new TestStep();
        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final LightWeightPromiseImpl<?> promise = promiseDoThen(
            retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep),
            new FR1<Object, RV<?>>() { @Override public RV<?> call(final Object value) throws Throwable {
                loggerMock.log("onFulfilled", value);
                return retCallback.call(LightWeightPromiseImplTest.this, callbackStep, resolveStep);
            }},
            new FR1<Throwable, RV<?>>() { @Override public RV<?> call(final Throwable exception) throws Throwable {
                loggerMock.log("onRejected", exception);
                return retCallback.call(LightWeightPromiseImplTest.this, callbackStep, resolveStep);
            }},
            callbackStep
        );

        srcPromiseStep.sync();
        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(promise, promiseMatcher("LW-MUTABLE", expectedState, expectedValue, expectedExceptionClass));
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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        testMutablePromiseResolveBeforeCallback(
            suppSrcPromise.get(retNoWait(fulfilledResolution(fulfilledValue))), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledMutablePromiseResolve")
    public final void testFulfilledMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onFulfilled", fulfilledValue);
        }};

        testMutablePromiseResolveAfterCallback(
            suppSrcPromise.get(retNoWait(fulfilledResolution(fulfilledValue))),
            suppCallbackReturn, retResolution,
            expectedState, expectedValue, expectedExceptionClass
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
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedException);
        }};

        testMutablePromiseResolveBeforeCallback(
            suppSrcPromise.get(retNoWait(rejectedResolution(rejectedException))), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedMutablePromiseResolve")
    public final void testRejectedMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, LightWeightPromiseImpl<?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<LightWeightPromiseImplTest, RV<?>, ? extends RV<?>> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<LightWeightPromiseImplTest, ? extends RV<?>> retResolution,
        final boolean isAlwaysPending,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        new StrictExpectations() {{
            loggerMock.log("onRejected", rejectedException);
        }};

        testMutablePromiseResolveAfterCallback(
            suppSrcPromise.get(retNoWait(rejectedResolution(rejectedException))),
            suppCallbackReturn, retResolution,
            expectedState, expectedValue, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private LightWeightPromiseImpl<?>
    mutablePromiseMultiDoThen(final LightWeightPromiseImpl<?> srcPromise, final String id, final RV<?> resolution)
    {
        return srcPromise.doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, RV<?>>() { @Override public RV<?> call(final Object value) {
                loggerMock.log(id, "onFulfilled", value);
                return resolution;
            }},
            new FR1<Throwable, RV<?>>() { @Override public RV<?> call(final Throwable exception) {
                loggerMock.log(id, "onRejected", TestUtil.exceptionClass(exception));
                return resolution;
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void mutablePromiseMultiApplyResolveAction(final LightWeightPromiseImpl<?> srcPromise, final String id)
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
    private void testMutablePromiseMultiCallback(final RV<?> srcPromiseResolution, final boolean isAlwaysPending)
        throws Throwable
    {
        final TestStep step = new TestStep();

        final LightWeightPromiseImpl<?> srcPromise = suppPendingMutablePromise.get(retNoWait(srcPromiseResolution))
            .call(this, new TestStep().pass(), step);

        final LightWeightPromiseImpl<?> promise0
            = mutablePromiseMultiDoThen(srcPromise, "doThen-0", fulfilledResolution(123));

        loggerMock.log("seperate-01");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-1");

        loggerMock.log("seperate-12");

        final LightWeightPromiseImpl<?> promise2
            = mutablePromiseMultiDoThen(srcPromise, "doThen-2", rejectedResolution(new Throwable()));

        loggerMock.log("seperate-23");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-3");

        loggerMock.log("seperate-34");

        final LightWeightPromiseImpl<?> promise4
            = mutablePromiseMultiDoThen(srcPromise, "doThen-4", alwaysPendingResolution());

        Assert.assertThat(promise0, promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null));
        Assert.assertThat(promise2, promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null));
        Assert.assertThat(promise4, promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null));

        loggerMock.log("sync", "before");
        step.sync();
        loggerMock.log("sync", "after");

        final Matcher<LightWeightPromiseImpl<?>> matcher0 = !isAlwaysPending
            ? promiseMatcher("LW-MUTABLE", PromiseState.FULFILLED, 123, null)
            : promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null);

        Assert.assertThat(promise0, matcher0);

        final Matcher<LightWeightPromiseImpl<?>> matcher2 = !isAlwaysPending
            ? promiseMatcher("LW-MUTABLE", PromiseState.REJECTED, null, Throwable.class)
            : promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null);

        Assert.assertThat(promise2, matcher2);

        final Matcher<LightWeightPromiseImpl<?>> matcher4
            = promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null);

        Assert.assertThat(promise4, matcher4);

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-5");

        loggerMock.log("seperate-56");

        final Matcher<LightWeightPromiseImpl<?>> matcher6 = !isAlwaysPending
            ? promiseMatcher("LW-FULFILLED", PromiseState.FULFILLED, true, null)
            : promiseMatcher("LW-ALWAYS-PENDING", PromiseState.PENDING, null, null);

        Assert.assertThat(mutablePromiseMultiDoThen(srcPromise, "doThen-6", fulfilledResolution(true)), matcher6);

        loggerMock.log("seperate-67");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-7");

        loggerMock.log("seperate-78");

        final Matcher<LightWeightPromiseImpl<?>> matcher8 = !isAlwaysPending
            ? promiseMatcher("LW-REJECTED", PromiseState.REJECTED, null, Exception.class)
            : promiseMatcher("LW-ALWAYS-PENDING", PromiseState.PENDING, null, null);

        Assert.assertThat(
            mutablePromiseMultiDoThen(srcPromise, "doThen-8", rejectedResolution(new Exception())),
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

        testMutablePromiseMultiCallback(fulfilledResolution("test"), false);
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
            loggerMock.log("doThen-0", "onRejected", RuntimeException.class);
            loggerMock.log("applyResolveAction-1", "setRejected", null, RuntimeException.class);
            loggerMock.log("doThen-2", "onRejected", RuntimeException.class);
            loggerMock.log("applyResolveAction-3", "setRejected", null, RuntimeException.class);
            loggerMock.log("doThen-4", "onRejected", RuntimeException.class);
            loggerMock.log("sync", "after");
            loggerMock.log("applyResolveAction-5", "setRejected", null, RuntimeException.class);
            loggerMock.log("seperate-56");
            loggerMock.log("doThen-6", "onRejected", RuntimeException.class);
            loggerMock.log("seperate-67");
            loggerMock.log("applyResolveAction-7", "setRejected", null, RuntimeException.class);
            loggerMock.log("seperate-78");
            loggerMock.log("doThen-8", "onRejected", RuntimeException.class);
            loggerMock.log("seperate-89");
            loggerMock.log("applyResolveAction-9", "setRejected", null, RuntimeException.class);
        }};

        testMutablePromiseMultiCallback(rejectedResolution(new RuntimeException()), false);
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

        testMutablePromiseMultiCallback(alwaysPendingResolution(), true);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
