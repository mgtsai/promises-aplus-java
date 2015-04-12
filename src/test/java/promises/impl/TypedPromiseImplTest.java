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
import promises.typed.Promise;
import promises.typed.RejectPromise;
import promises.typed.Resolution;
import promises.typed.ResolvePromise;
import promises.typed.Thenable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//---------------------------------------------------------------------------------------------------------------------
@RunWith(JUnitParamsRunner.class)
public final class TypedPromiseImplTest
{
    //-----------------------------------------------------------------------------------------------------------------
    @Injectable private TestLogger loggerMock = null;
    @Injectable private ResolveAction resolveActionMock = null;
    @Injectable private FR1<Object, Resolution<?, ?>> unusedOnFulfilled = null;
    @Injectable private FR2<Object, Throwable, Resolution<?, ?>> unusedOnRejected = null;
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR1<Object, Resolution<?, ?>>
    defaultOnFulfilled = new FR1<Object, Resolution<?, ?>>() {
        @Override public Resolution<?, ?> call(final Object value) {
            return new Resolution<Object, Object>() {
                @Override public PromiseState state() { return PromiseState.FULFILLED; }
                @Override public Object value() { return value; }
                @Override public Object reason() { return null; }
                @Override public Throwable exception() { return null; }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final FR2<Object, Throwable, Resolution<?, ?>>
    defaultOnRejected = new FR2<Object, Throwable, Resolution<?, ?>>() {
        @Override public Resolution<?, ?> call(final Object reason, final Throwable exception) {
            return new Resolution<Object, Object>() {
                @Override public PromiseState state() { return PromiseState.REJECTED; }
                @Override public Object value() { return null; }
                @Override public Object reason() { return reason; }
                @Override public Throwable exception() { return exception; }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>
    callNothing = new F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>() {
        @Override public void call(
            final FR1<Object, Resolution<?, ?>> onFulfilled,
            final FR2<Object, Throwable, Resolution<?, ?>> onRejected
        ) { }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>
    retPendingPromise = new TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>() {
        @Override public TypedPromiseImpl<Object, Object>
        call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
            cbStep.pause();
            resStep.finish();

            return TypedPromiseImpl.factory().fulfilledPromise(null)
                .doThen(TestUtil.NOP_EXECUTOR, self.unusedOnFulfilled, self.unusedOnRejected);
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private static final
    TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<Object, Object>>
    suppPendingMutablePromise
    = new TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<Object, Object>>() {
        @Override public TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>
        get(final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution) {
            return new TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>() {
                @Override public TypedPromiseImpl<Object, Object>
                call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    cbStep.pause();
                    final TestStep chainResolveStep = new TestStep();

                    return promiseDoThen(
                        TypedPromiseImpl.factory().fulfilledPromise(null),
                        new FR1<Object, Resolution<?, ?>>() {
                            @Override public Resolution<?, ?> call(final Object value) throws Throwable {
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
    private static final
    TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<Object, Object>>
    suppResolvedMutablePromise
    = new TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<Object, Object>>() {
        @Override public TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>
        get(final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution) {
            return new TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<Object, Object>>() {
                @Override public TypedPromiseImpl<Object, Object>
                call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep)
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
    private static final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, Thenable<Object, Object>>
    suppThenableResolve
    = new TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, Thenable<Object, Object>>() {
        @Override public TestStep.Return<TypedPromiseImplTest, Thenable<Object, Object>>
        get(final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution) {
            return new TestStep.Return<TypedPromiseImplTest, Thenable<Object, Object>>() {
                @Override public Thenable<Object, Object>
                call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                    return new Thenable<Object, Object>() {
                        @Override public void then(
                            final ResolvePromise<Object, Object> resP,
                            final RejectPromise<Object> rejP
                        ) throws Throwable {
                            resP.resolve(retResolution.call(self, cbStep, resStep));
                        }
                    };
                }
            };
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static Matcher<TypedPromiseImpl<?, ?>> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) {
        return new TypeSafeMatcher<TypedPromiseImpl<?, ?>>() {
            @Override protected boolean matchesSafely(final TypedPromiseImpl<?, ?> item) {
                return TestUtil.equals(item.type(), expectedType)
                    && item.state() == expectedState
                    && TestUtil.equals(item.value(), expectedValue)
                    && TestUtil.equals(item.reason(), expectedReason)
                    && TestUtil.isInstanceOf(item.exception(), expectedExceptionClass);
            }

            @Override public void describeTo(final Description desc) {
                desc.appendText("Typed promise ").appendValueList(
                    "[", ", ", "]",
                    expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
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
    private static <V, R> Resolution<V, R> fulfilledResolution(final V value)
    {
        return new Resolution<V, R>() {
            @Override public PromiseState state() { return PromiseState.FULFILLED; }
            @Override public V value() { return value; }
            @Override public R reason() { return null; }
            @Override public Throwable exception() { return null; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> Resolution<V, R> rejectedResolution(final R reason, final Throwable exception)
    {
        return new Resolution<V, R>() {
            @Override public PromiseState state() { return PromiseState.REJECTED; }
            @Override public V value() { return null; }
            @Override public R reason() { return reason; }
            @Override public Throwable exception() { return exception; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> Resolution<V, R> alwaysPendingResolution()
    {
        return TypedPromiseImpl.<V, R>factory().alwaysPendingPromise();
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>
    callOnFulfilled(final Object fulfilledValue)
    {
        return new F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>() {
            @Override public void call(
                final FR1<Object, Resolution<?, ?>> onFulfilled,
                final FR2<Object, Throwable, Resolution<?, ?>> onRejected
            ) throws Throwable {
                onFulfilled.call(fulfilledValue);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>
    callOnRejected(final Object rejectedReason, final Throwable rejectedException)
    {
        return new F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>>() {
            @Override public void call(
                final FR1<Object, Resolution<?, ?>> onFulfilled,
                final FR2<Object, Throwable, Resolution<?, ?>> onRejected
            ) throws Throwable {
                onRejected.call(rejectedReason, rejectedException);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> Promise<V, R>
    testTypedPromise(final F2<FR1<Object, Resolution<?, ?>>, FR2<Object, Throwable, Resolution<?, ?>>> thenCall)
    {
        return new Promise<V, R>() {
            @Override public PromiseState state() { return null; }
            @Override public V value() { return null; }
            @Override public R reason() { return null; }
            @Override public Throwable exception() { return null; }
            @Override public V await() { return null; }
            @Override public V await(final long timeout, final TimeUnit unit) { return null; }
            @Override public promises.Promise toUntypedPromise() { return null; }
            @Override public P<V> toLightWeightPromise() { return null; }

            @Override public <VO, RO> Promise<VO, RO> then(
                final Executor exec,
                final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
            ) {
                try {
                    thenCall.call(
                        onFulfilled != null
                            ? ImplUtil.<FR1<Object, Resolution<?, ?>>>cast(onFulfilled)
                            : defaultOnFulfilled,
                        onRejected != null
                            ? ImplUtil.<FR2<Object, Throwable, Resolution<?, ?>>>cast(onRejected)
                            : defaultOnRejected
                    );
                } catch (final Throwable e) {
                    //
                }

                return null;
            }

            @Override public <VO, RO> Promise<VO, RO> then(
                final Executor exec,
                final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled
            ) {
                return then(exec, onFulfilled, null);
            }

            @Override public <VO, RO> Promise<VO, RO> then(
                final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
                final FR2<? super R, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected
            ) {
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, onRejected);
            }

            @Override public <VO, RO> Promise<VO, RO>
            then(final FR1<? super V, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled) {
                return then(ImplUtil.CURRENT_THREAD_EXECUTOR, onFulfilled, null);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <VI, RI, VO, RO> TypedPromiseImpl<VO, RO> promiseDoThen(
        final TypedPromiseImpl<VI, RI> srcPromise,
        final FR1<? super VI, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled,
        final FR2<? super RI, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected,
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
    private static <T extends Resolution<?, ?>> TestStep.Return<TypedPromiseImplTest, T> retNoWait(final T ret)
    {
        return TestStep.retNoWait(ret);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <T extends Resolution<?, ?>> TestStep.Return<TypedPromiseImplTest, T>
    retThrowException(final Throwable exception)
    {
        return TestStep.retThrowException(exception);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>
    retThenableResolveValue(final V value)
    {
        return new TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>() {
            @Override public Thenable<V, R>
            call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V, R>() {
                    @Override public void then(final ResolvePromise<V, R> resP, final RejectPromise<R> rejP) {
                        cbStep.pause();
                        resP.resolve(value);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>
    retThenableReject(final R reason, final Throwable exception)
    {
        return new TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>() {
            @Override public Thenable<V, R>
            call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V, R>() {
                    @Override public void then(final ResolvePromise<V, R> resP, final RejectPromise<R> rejP) {
                        cbStep.pause();
                        rejP.reject(reason, exception);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> TestStep.Return<TypedPromiseImplTest, Thenable<V, R>> retThenableReject(final R reason)
    {
        return new TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>() {
            @Override public Thenable<V, R>
            call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V, R>() {
                    @Override public void then(final ResolvePromise<V, R> resP, final RejectPromise<R> rejP) {
                        cbStep.pause();
                        rejP.reject(reason);
                        resStep.finish();
                    }
                };
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V, R> TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>
    retThenableReject(final Throwable exception)
    {
        return new TestStep.Return<TypedPromiseImplTest, Thenable<V, R>>() {
            @Override public Thenable<V, R>
            call(final TypedPromiseImplTest self, final TestStep cbStep, final TestStep resStep) {
                return new Thenable<V, R>() {
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
        {retNoWait(fulfilledResolution(null)),                                                          false, "TYPED-FULFILLED", PromiseState.FULFILLED, null,  null, null},
        {retNoWait(fulfilledResolution(123)),                                                           false, "TYPED-FULFILLED", PromiseState.FULFILLED, 123,   null, null},
        {retNoWait(testTypedPromise(callOnFulfilled("abc"))),                                           false, "TYPED-FULFILLED", PromiseState.FULFILLED, "abc", null, null},
        {retNoWait(TypedPromiseImpl.factory().fulfilledPromise('D')),                                   false, "TYPED-FULFILLED", PromiseState.FULFILLED, 'D',   null, null},
        {suppPendingMutablePromise.get(retNoWait(fulfilledResolution(4.5))),                            false, "TYPED-MUTABLE",   PromiseState.FULFILLED, 4.5,   null, null},
        {suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(false))),                         false, "TYPED-FULFILLED", PromiseState.FULFILLED, false, null, null},
        {retThenableResolveValue(6543L),                                                                false, "TYPED-FULFILLED", PromiseState.FULFILLED, 6543L, null, null},
        {suppThenableResolve.get(retNoWait(fulfilledResolution(null))),                                 false, "TYPED-FULFILLED", PromiseState.FULFILLED, null,  null, null},
        {suppThenableResolve.get(retNoWait(fulfilledResolution(true))),                                 false, "TYPED-FULFILLED", PromiseState.FULFILLED, true,  null, null},
        {suppThenableResolve.get(retNoWait(testTypedPromise(callOnFulfilled(678)))),                    false, "TYPED-FULFILLED", PromiseState.FULFILLED, 678,   null, null},
        {suppThenableResolve.get(retNoWait(TypedPromiseImpl.factory().fulfilledPromise("ijk"))),        false, "TYPED-FULFILLED", PromiseState.FULFILLED, "ijk", null, null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(fulfilledResolution('E')))),   false, "TYPED-MUTABLE",   PromiseState.FULFILLED, 'E',   null, null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(-9.0)))), false, "TYPED-FULFILLED", PromiseState.FULFILLED, -9.0,  null, null},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsRejectedResolution() { return new Object[][] {
        {retNoWait(rejectedResolution(false, new Throwable())),                                                                   false, "TYPED-REJECTED", PromiseState.REJECTED, null, false, Throwable.class},
        {retThrowException(new Exception()),                                                                                      false, "TYPED-REJECTED", PromiseState.REJECTED, null, null,  Exception.class},
        {retNoWait(testTypedPromise(callOnRejected(true, new RuntimeException()))),                                               false, "TYPED-REJECTED", PromiseState.REJECTED, null, true,  RuntimeException.class},
        {retNoWait(TypedPromiseImpl.factory().rejectedPromise(-987, new ClassCastException())),                                   false, "TYPED-REJECTED", PromiseState.REJECTED, null, -987,  ClassCastException.class},
        {suppPendingMutablePromise.get(retNoWait(rejectedResolution("pqr", new Throwable()))),                                    false, "TYPED-MUTABLE",  PromiseState.REJECTED, null, "pqr", Throwable.class},
        {suppResolvedMutablePromise.get(retNoWait(rejectedResolution('F', new Exception()))),                                     false, "TYPED-REJECTED", PromiseState.REJECTED, null, 'F',   Exception.class},
        {suppThenableResolve.get(retNoWait(rejectedResolution(-6.5, new RuntimeException()))),                                    false, "TYPED-REJECTED", PromiseState.REJECTED, null, -6.5,  RuntimeException.class},
        {suppThenableResolve.get(retThrowException(new ClassCastException())),                                                    false, "TYPED-REJECTED", PromiseState.REJECTED, null, null,  ClassCastException.class},
        {suppThenableResolve.get(retNoWait(testTypedPromise(callOnRejected(false, new Throwable())))),                            false, "TYPED-REJECTED", PromiseState.REJECTED, null, false, Throwable.class},
        {suppThenableResolve.get(retNoWait(TypedPromiseImpl.factory().rejectedPromise(true, new Exception()))),                   false, "TYPED-REJECTED", PromiseState.REJECTED, null, true,  Exception.class},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(rejectedResolution(-654, new RuntimeException())))),     false, "TYPED-MUTABLE",  PromiseState.REJECTED, null, -654,  RuntimeException.class},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(rejectedResolution("xyz", new ClassCastException())))), false, "TYPED-REJECTED", PromiseState.REJECTED, null, "xyz", ClassCastException.class},
        {retThenableReject('G', new Throwable()),                                                                                 false, "TYPED-REJECTED", PromiseState.REJECTED, null, 'G',   Throwable.class},
        {retThenableReject(0.432),                                                                                                false, "TYPED-REJECTED", PromiseState.REJECTED, null, 0.432, null},
        {retThenableReject(new Exception()),                                                                                      false, "TYPED-REJECTED", PromiseState.REJECTED, null, null,  Exception.class},
    };}
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[][] paramsPendingResolution() { return new Object[][] {
        {retNoWait(testTypedPromise(callNothing)),                                                      false, "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {retNoWait(TypedPromiseImpl.factory().alwaysPendingPromise()),                                  true,  "TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {retPendingPromise,                                                                             false, "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution())),                           true,  "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution())),                          true,  "TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(retNoWait(testTypedPromise(callNothing))),                             false, "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(retNoWait(TypedPromiseImpl.factory().alwaysPendingPromise())),         true,  "TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(retPendingPromise),                                                    false, "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution()))),  true,  "TYPED-MUTABLE",        PromiseState.PENDING, null, null, null},
        {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution()))), true,  "TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null},
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
            TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue),
            promiseMatcher("TYPED-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseUnlimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(fulfilledValue, TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).await());
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseLimitedAwait(final Object fulfilledValue) throws Exception
    {
        new StrictExpectations() {};

        Assert.assertEquals(
            fulfilledValue,
            TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).await(1, TimeUnit.SECONDS)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseToUntypedPromise(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher(
                "UNTYPED-FULFILLED",
                PromiseState.FULFILLED, fulfilledValue, null, null
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseToLightWeightPromise(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).toLightWeightPromise(),
            LightWeightPromiseImplTest.promiseMatcher("LW-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null)
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

        TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "fulfilled")
    public final void testFulfilledPromiseNullOnFulfilled(final Object fulfilledValue)
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        final TypedPromiseImpl<?, ?> promise = TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue)
            .doThen(exec, null, unusedOnRejected);

        Assert.assertThat(
            promise,
            promiseMatcher("TYPED-FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null)
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
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue).doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Resolution<?, ?>>() {
                @Override public Resolution<?, ?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retResolution.call(TypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }
            },
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
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = promiseDoThen(
            TypedPromiseImpl.factory().fulfilledPromise(fulfilledValue),
            new FR1<Object, Resolution<?, ?>>() {
                @Override public Resolution<?, ?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retResolution.call(TypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            unusedOnRejected,
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("TYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
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
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException),
            promiseMatcher("TYPED-REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
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
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException).await();
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
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException).await(1, TimeUnit.SECONDS);
            Assert.fail();
        } catch (final PromiseRejectedException e) {
            Assert.assertEquals(rejectedReason, e.reason());
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
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException).toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher(
                "UNTYPED-REJECTED",
                PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(source = PromiseTestData.class, method = "rejected")
    public final void testRejectedPromiseToLightWeightPromise(
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass
    ) {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException).toLightWeightPromise(),
            LightWeightPromiseImplTest.promiseMatcher(
                "LW-REJECTED",
                PromiseState.REJECTED, null, rejectedExceptionClass
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
            resolveActionMock.setRejected(rejectedReason, rejectedException);
        }};

        TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException)
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

        final TypedPromiseImpl<?, ?> promise = TypedPromiseImpl.factory()
            .rejectedPromise(rejectedReason, rejectedException)
            .doThen(exec, unusedOnFulfilled, null);

        Assert.assertThat(
            promise,
            promiseMatcher("TYPED-REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
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
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = TypedPromiseImpl.factory()
            .rejectedPromise(rejectedReason, rejectedException)
            .doThen(
                ImplUtil.CURRENT_THREAD_EXECUTOR,
                unusedOnFulfilled,
                new FR2<Object, Throwable, Resolution<?, ?>>() {
                    @Override public Resolution<?, ?>
                    call(final Object reason, final Throwable exception) throws Throwable {
                        loggerMock.log("onRejected", reason, exception);
                        return retResolution.call(TypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
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
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = promiseDoThen(
            TypedPromiseImpl.factory().rejectedPromise(rejectedReason, rejectedException),
            unusedOnFulfilled,
            new FR2<Object, Throwable, Resolution<?, ?>>() {
                @Override public Resolution<?, ?>
                call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retResolution.call(TypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            callbackStep
        );

        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("TYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testFactoryAlwaysPendingPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().alwaysPendingPromise(),
            promiseMatcher("TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
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

        TypedPromiseImpl.factory().alwaysPendingPromise().await();
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test(expected = TimeoutException.class)
    public final void testAlwaysPendingPromiseLimitedAwait() throws Exception
    {
        new StrictExpectations() {};

        TypedPromiseImpl.factory().alwaysPendingPromise().await(100, TimeUnit.MILLISECONDS);
        Assert.fail();
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseToUntypedPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().alwaysPendingPromise().toUntypedPromise(),
            UntypedPromiseImplTest.promiseMatcher("UNTYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseToLightWeightPromise()
    {
        new StrictExpectations() {};

        Assert.assertThat(
            TypedPromiseImpl.factory().alwaysPendingPromise().toLightWeightPromise(),
            LightWeightPromiseImplTest.promiseMatcher("LW-ALWAYS-PENDING", PromiseState.PENDING, null, null)
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    public final void testAlwaysPendingPromiseApplyResolveAction()
    {
        new StrictExpectations() {{
            resolveActionMock.setAlwaysPending();
        }};

        TypedPromiseImpl.factory().alwaysPendingPromise().applyResolveAction(resolveActionMock);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private static Object[][] paramsAlwaysPendingDoThen()
    {
        return new Object[][] {{
            new FR2<TypedPromiseImplTest, Executor, TypedPromiseImpl<?, ?>>() {
                @Override public TypedPromiseImpl<?, ?> call(final TypedPromiseImplTest self, final Executor exec) {
                    return TypedPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<TypedPromiseImplTest, Executor, TypedPromiseImpl<?, ?>>() {
                @Override public TypedPromiseImpl<?, ?> call(final TypedPromiseImplTest self, final Executor exec) {
                    return TypedPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, null, self.unusedOnRejected);
                }
            }
        }, {
            new FR2<TypedPromiseImplTest, Executor, TypedPromiseImpl<?, ?>>() {
                @Override public TypedPromiseImpl<?, ?> call(final TypedPromiseImplTest self, final Executor exec) {
                    return TypedPromiseImpl.factory().alwaysPendingPromise()
                        .doThen(exec, self.unusedOnFulfilled, null);
                }
            }
        }};
    }

    @Test
    @Parameters(method = "paramsAlwaysPendingDoThen")
    public final void
    testAlwaysPendingDoThen(final FR2<TypedPromiseImplTest, Executor, TypedPromiseImpl<?, ?>> retSrcPromise)
        throws Throwable
    {
        new StrictExpectations() {};

        final ExecutorService exec = Executors.newSingleThreadExecutor();

        Assert.assertThat(
            retSrcPromise.call(this, exec),
            promiseMatcher("TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
        final TypedPromiseImpl<?, ?> promise = suppPromise.get(retResolution).call(this, new TestStep().pass(), step);

        final Matcher<TypedPromiseImpl<?, ?>> resolveMatcher = promiseMatcher(
            "TYPED-MUTABLE",
            expectedState, expectedValue, expectedReason, expectedExceptionClass
        );

        final Matcher<TypedPromiseImpl<?, ?>> presyncMatcher = !isResolved
            ? promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null)
            : resolveMatcher;

        Assert.assertThat(promise, presyncMatcher);
        step.sync();
        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseUnlimitedAwait(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppPromise.get(retResolution)
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppPromise.get(retResolution)
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
    public final void testMutablePromiseToUntypedPromise(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppPromise.get(retResolution).call(this, new TestStep().pass(), step);

        final UntypedPromiseImpl toPromise1 = promise.toUntypedPromise();

        final String resolvedType
            = expectedState == PromiseState.FULFILLED ? "UNTYPED-FULFILLED"
            : expectedState == PromiseState.REJECTED ? "UNTYPED-REJECTED"
            : isAlwaysPending ? "UNTYPED-ALWAYS-PENDING" : "UNTYPED-MUTABLE";

        final Matcher<UntypedPromiseImpl> resolveMatcher = UntypedPromiseImplTest.promiseMatcher(
            !isResolved ? "UNTYPED-MUTABLE" : resolvedType,
            expectedState, expectedValue, expectedReason, expectedExceptionClass
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
                expectedState, expectedValue, expectedReason, expectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseToLightWeightPromise(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppPromise.get(retResolution).call(this, new TestStep().pass(), step);

        final LightWeightPromiseImpl<?> toPromise1 = promise.toLightWeightPromise();

        final String resolvedType
            = expectedState == PromiseState.FULFILLED ? "LW-FULFILLED"
            : expectedState == PromiseState.REJECTED ? "LW-REJECTED"
            : isAlwaysPending ? "LW-ALWAYS-PENDING" : "LW-MUTABLE";

        final Matcher<LightWeightPromiseImpl<?>> resolveMatcher = LightWeightPromiseImplTest.promiseMatcher(
            !isResolved ? "LW-MUTABLE" : resolvedType,
            expectedState, expectedValue, expectedExceptionClass
        );

        final Matcher<LightWeightPromiseImpl<?>> presyncMatcher = !isResolved
            ? LightWeightPromiseImplTest.promiseMatcher("LW-MUTABLE", PromiseState.PENDING, null, null)
            : resolveMatcher;

        Assert.assertThat(toPromise1, presyncMatcher);

        step.sync();

        Assert.assertThat(toPromise1, resolveMatcher);

        Assert.assertThat(
            promise.toLightWeightPromise(),
            LightWeightPromiseImplTest.promiseMatcher(
                resolvedType,
                expectedState, expectedValue, expectedExceptionClass
            )
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsMutablePromise")
    public final void testMutablePromiseApplyResolveAction(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppPromise,
        final boolean isResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, null, unusedOnRejected);

        final Matcher<TypedPromiseImpl<?, ?>> presyncMatcher;
        final Matcher<TypedPromiseImpl<?, ?>> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null);
            resolveMatcher = promiseMatcher("TYPED-MUTABLE", PromiseState.FULFILLED, expectedValue, null, null);
        } else
            presyncMatcher = resolveMatcher
                = promiseMatcher("TYPED-FULFILLED", PromiseState.FULFILLED, expectedValue, null, null);

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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, null);

        final Matcher<TypedPromiseImpl<?, ?>> presyncMatcher;
        final Matcher<TypedPromiseImpl<?, ?>> resolveMatcher;

        if (!isSrcResolved) {
            presyncMatcher = promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null);

            resolveMatcher = promiseMatcher(
                "TYPED-MUTABLE",
                PromiseState.REJECTED, null, expectedReason, expectedExceptionClass
            );
        } else
            presyncMatcher = resolveMatcher = promiseMatcher(
                "TYPED-REJECTED",
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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

        final TypedPromiseImpl<?, ?> promise = suppSrcPromise.get(retResolution)
            .call(this, new TestStep().pass(), step)
            .doThen(exec, unusedOnFulfilled, unusedOnRejected);

        final String chainDstPromiseType = !isAlwaysPending ? "TYPED-MUTABLE" : "TYPED-ALWAYS-PENDING";
        final String promiseType = !isSrcResolved ? "TYPED-MUTABLE" : chainDstPromiseType;

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
        final TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<?, ?>> retSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retCallback
            = suppCallbackReturn.get(retResolution);

        final TestStep srcPromiseStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final TypedPromiseImpl<?, ?> promise = retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep).doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Resolution<?, ?>>() {
                @Override public Resolution<?, ?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retCallback.call(TypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }
            },
            new FR2<Object, Throwable, Resolution<?, ?>>() {
                @Override public Resolution<?, ?>
                call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retCallback.call(TypedPromiseImplTest.this, new TestStep().pass(), resolveStep);
                }
            }
        );

        srcPromiseStep.sync();
        resolveStep.sync();

        final Matcher<TypedPromiseImpl<?, ?>> resolveMatcher = !isSrcResolved
            ? promiseMatcher("TYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
            : promiseMatcher(expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass);

        Assert.assertThat(promise, resolveMatcher);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void testMutablePromiseResolveAfterCallback(
        final TestStep.Return<TypedPromiseImplTest, TypedPromiseImpl<?, ?>> retSrcPromise,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    ) throws Throwable
    {
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retCallback
            = suppCallbackReturn.get(retResolution);

        final TestStep srcPromiseStep = new TestStep();
        final TestStep callbackStep = new TestStep();
        final TestStep resolveStep = new TestStep();

        final TypedPromiseImpl<?, ?> promise = promiseDoThen(
            retSrcPromise.call(this, new TestStep().pass(), srcPromiseStep),
            new FR1<Object, Resolution<?, ?>>() {
                @Override public Resolution<?, ?> call(final Object value) throws Throwable {
                    loggerMock.log("onFulfilled", value);
                    return retCallback.call(TypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            new FR2<Object, Throwable, Resolution<?, ?>>() {
                @Override public Resolution<?, ?>
                call(final Object reason, final Throwable exception) throws Throwable {
                    loggerMock.log("onRejected", reason, exception);
                    return retCallback.call(TypedPromiseImplTest.this, callbackStep, resolveStep);
                }
            },
            callbackStep
        );

        srcPromiseStep.sync();
        callbackStep.sync();
        resolveStep.sync();

        Assert.assertThat(
            promise,
            promiseMatcher("TYPED-MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass)
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
            suppSrcPromise.get(retNoWait(fulfilledResolution(fulfilledValue))), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsFulfilledMutablePromiseResolve")
    public final void testFulfilledMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final Object fulfilledValue,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
            suppSrcPromise.get(retNoWait(fulfilledResolution(fulfilledValue))),
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
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
            suppSrcPromise.get(retNoWait(rejectedResolution(rejectedReason, rejectedException))), isSrcResolved,
            suppCallbackReturn, retResolution,
            expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Test
    @Parameters(method = "paramsRejectedMutablePromiseResolve")
    public final void testRejectedMutablePromiseResolveAfterCallback(
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, TypedPromiseImpl<?, ?>> suppSrcPromise,
        final boolean isSrcResolved,
        final TestStep.ReturnSupplier<TypedPromiseImplTest, Resolution<?, ?>, ? extends Resolution<?, ?>> suppCallbackReturn,
        final Object rejectedReason,
        final Throwable rejectedException,
        final Class<?> rejectedExceptionClass,
        final TestStep.Return<TypedPromiseImplTest, ? extends Resolution<?, ?>> retResolution,
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
            suppSrcPromise.get(retNoWait(rejectedResolution(rejectedReason, rejectedException))),
            suppCallbackReturn, retResolution,
            expectedState, expectedValue, expectedReason, expectedExceptionClass
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private TypedPromiseImpl<?, ?> mutablePromiseMultiDoThen(
        final TypedPromiseImpl<?, ?> srcPromise,
        final String id,
        final Resolution<?, ?> resolution
    ) {
        return srcPromise.doThen(
            ImplUtil.CURRENT_THREAD_EXECUTOR,
            new FR1<Object, Resolution<?, ?>>() { @Override public Resolution<?, ?> call(final Object value) {
                loggerMock.log(id, "onFulfilled", value);
                return resolution;
            }},
            new FR2<Object, Throwable, Resolution<?, ?>>() {
                @Override public Resolution<?, ?> call(final Object reason, final Throwable exception) {
                    loggerMock.log(id, "onRejected", reason, TestUtil.exceptionClass(exception));
                    return resolution;
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
    private void mutablePromiseMultiApplyResolveAction(final TypedPromiseImpl<?, ?> srcPromise, final String id)
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
    private void
    testMutablePromiseMultiCallback(final Resolution<?, ?> srcPromiseResolution, final boolean isAlwaysPending)
        throws Throwable
    {
        final TestStep step = new TestStep();

        final TypedPromiseImpl<?, ?> srcPromise = suppPendingMutablePromise.get(retNoWait(srcPromiseResolution))
            .call(this, new TestStep().pass(), step);

        final TypedPromiseImpl<?, ?> promise0
            = mutablePromiseMultiDoThen(srcPromise, "doThen-0", fulfilledResolution(123));

        loggerMock.log("seperate-01");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-1");

        loggerMock.log("seperate-12");

        final TypedPromiseImpl<?, ?> promise2
            = mutablePromiseMultiDoThen(srcPromise, "doThen-2", rejectedResolution("abc", new Throwable()));

        loggerMock.log("seperate-23");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-3");

        loggerMock.log("seperate-34");

        final TypedPromiseImpl<?, ?> promise4
            = mutablePromiseMultiDoThen(srcPromise, "doThen-4", alwaysPendingResolution());

        Assert.assertThat(promise0, promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null));
        Assert.assertThat(promise2, promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null));
        Assert.assertThat(promise4, promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null));

        loggerMock.log("sync", "before");
        step.sync();
        loggerMock.log("sync", "after");

        final Matcher<TypedPromiseImpl<?, ?>> matcher0 = !isAlwaysPending
            ? promiseMatcher("TYPED-MUTABLE", PromiseState.FULFILLED, 123, null, null)
            : promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise0, matcher0);

        final Matcher<TypedPromiseImpl<?, ?>> matcher2 = !isAlwaysPending
            ? promiseMatcher("TYPED-MUTABLE", PromiseState.REJECTED, null, "abc", Throwable.class)
            : promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise2, matcher2);

        final Matcher<TypedPromiseImpl<?, ?>> matcher4
            = promiseMatcher("TYPED-MUTABLE", PromiseState.PENDING, null, null, null);

        Assert.assertThat(promise4, matcher4);

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-5");

        loggerMock.log("seperate-56");

        final Matcher<TypedPromiseImpl<?, ?>> matcher6 = !isAlwaysPending
            ? promiseMatcher("TYPED-FULFILLED", PromiseState.FULFILLED, true, null, null)
            : promiseMatcher("TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null);

        Assert.assertThat(mutablePromiseMultiDoThen(srcPromise, "doThen-6", fulfilledResolution(true)), matcher6);

        loggerMock.log("seperate-67");

        mutablePromiseMultiApplyResolveAction(srcPromise, "applyResolveAction-7");

        loggerMock.log("seperate-78");

        final Matcher<TypedPromiseImpl<?, ?>> matcher8 = !isAlwaysPending
            ? promiseMatcher("TYPED-REJECTED", PromiseState.REJECTED, null, 'D', Exception.class)
            : promiseMatcher("TYPED-ALWAYS-PENDING", PromiseState.PENDING, null, null, null);

        Assert.assertThat(
            mutablePromiseMultiDoThen(srcPromise, "doThen-8", rejectedResolution('D', new Exception())),
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

        testMutablePromiseMultiCallback(rejectedResolution("test", new RuntimeException()), false);
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
