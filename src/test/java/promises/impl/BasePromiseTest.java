//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.impl;
import junitparams.Parameters;
import mockit.FullVerificationsInOrder;
import mockit.Injectable;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import promises.F2;
import promises.FR1;
import promises.FR2;
import promises.FR3;
import promises.PromiseRejectedException;
import promises.PromiseState;
import promises.TestData;
import promises.TestLogger;
import promises.TestStep;
import promises.TestThread;
import promises.TestUtil;
import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
//---------------------------------------------------------------------------------------------------------------------
public abstract class BasePromiseTest<R, P extends R, T extends R>
{
    //-----------------------------------------------------------------------------------------------------------------
    private final F2<FR1<Object, R>, FR2<Object, Throwable, R>>
    callNothing = new F2<FR1<Object, R>, FR2<Object, Throwable, R>>() {
        @Override public void call(final FR1<Object, R> onFulfilled, final FR2<Object, Throwable, R> onRejected) { }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private final Return<P>
    retPendingPromise = new Return<P>() { @Override P call(final Params params, final TestStep resStep) {
        resStep.finish();

        return doThen(
            promiseFactory().originPromise(),
            TestUtil.NOP_EXECUTOR,
            params.unusedOnFulfilled,
            params.unusedOnRejected
        );
    }};
    //-----------------------------------------------------------------------------------------------------------------
    private final ReturnSupplier<R, R> suppIdentity = new ReturnSupplier<R, R>() {
        @Override Return<R> get(final Return<? extends R> retResolution) {
            return new Return<R>() { @Override R call(final Params params, final TestStep resStep) throws Throwable {
                return retResolution.call(params, resStep);
            }};
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private final ReturnSupplier<R, P> suppPendingMutablePromise = new ReturnSupplier<R, P>() {
        @Override Return<P> get(final Return<? extends R> retResolution) {
            return new Return<P>() { @Override P call(final Params params, final TestStep resStep) {
                final ExecutorService exec = Executors.newSingleThreadExecutor();
                final TestStep innerResStep = new TestStep();

                try {
                    return doThen(
                        promiseFactory().originPromise(),
                        exec,
                        new FR1<Object, R>() { @Override public R call(final Object value) throws Throwable {
                            resStep.pause();

                            exec.execute(new Runnable() { @Override public void run() {
                                innerResStep.waitFinished();
                                resStep.finish();
                                exec.shutdown();
                            }});

                            return retResolution.call(params, innerResStep);
                        }},
                        params.unusedOnRejected
                    );
                } finally {
                    innerResStep.pass();
                }
            }};
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private final ReturnSupplier<R, P> suppResolvedMutablePromise = new ReturnSupplier<R, P>() {
        @Override Return<P> get(final Return<? extends R> retResolution) {
            return new Return<P>() { @Override P call(final Params params, final TestStep resStep) throws Throwable {
                final TestStep innerResStep = new TestStep();

                try {
                    return suppPendingMutablePromise.get(retResolution).call(params, innerResStep);
                } finally {
                    innerResStep.sync();
                    resStep.finish();
                }
            }};
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    private final ReturnSupplier<R, T> suppThenableResolve = new ReturnSupplier<R, T>() {
        @Override Return<T> get(final Return<? extends R> retResolution) {
            return new Return<T>() { @Override T call(final Params params, final TestStep resStep) {
                return thenableResolve(retResolution, params, resStep);
            }};
        }
    };
    //-----------------------------------------------------------------------------------------------------------------
    static Executor newTestExecutor()
    {
        return new Executor() {
            @Override public void execute(@Nonnull final Runnable command) { }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract PromiseFactory<? extends P> promiseFactory();
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object await(final P promise) throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    abstract Object await(final P promise, final long timeout, final TimeUnit unit)
        throws PromiseRejectedException, InterruptedException, TimeoutException;
    //-----------------------------------------------------------------------------------------------------------------
    abstract UntypedPromiseImpl toUntypedPromise(final P promise);
    //-----------------------------------------------------------------------------------------------------------------
    abstract TypedPromiseImpl<?, ?> toTypedPromise(final P promise);
    //-----------------------------------------------------------------------------------------------------------------
    abstract LightWeightPromiseImpl<?> toLightWeightPromise(final P promise);
    //-----------------------------------------------------------------------------------------------------------------
    abstract void applyResolveAction(final P promise, final ResolveAction resolveAction);
    //-----------------------------------------------------------------------------------------------------------------
    abstract P doThen(
        final P promise,
        final Executor exec,
        final FR1<Object, ? extends R> onFulfilled,
        final FR2<Object, Throwable, ? extends R> onRejected
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract Matcher<P> promiseMatcher(
        final String expectedType,
        final PromiseState expectedState,
        final Object expectedValue,
        final Object expectedReason,
        final Class<?> expectedExceptionClass
    );
    //-----------------------------------------------------------------------------------------------------------------
    abstract R fulfilledResolution(final Object value);
    //-----------------------------------------------------------------------------------------------------------------
    abstract R rejectedResolution(final Object reason, final Throwable exception);
    //-----------------------------------------------------------------------------------------------------------------
    abstract R alwaysPendingResolution();
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenable(final T thenable, final TestStep resStep);
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableNop();
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableResolve(final Return<? extends R> retResolution, final Params params, final TestStep resStep);
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableResolve(final Object value);
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableReject(final Object reason, final Throwable exception);
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableReject(final Object reason);
    //-----------------------------------------------------------------------------------------------------------------
    abstract T thenableReject(final Throwable exception);
    //-----------------------------------------------------------------------------------------------------------------
    abstract R testPromise(final F2<FR1<Object, R>, FR2<Object, Throwable, R>> thenCall);
    //-----------------------------------------------------------------------------------------------------------------
    Object translateReason(final Object reason)
    {
        return reason;
    }
    //-----------------------------------------------------------------------------------------------------------------
    private F2<FR1<Object, R>, FR2<Object, Throwable, R>> callOnFulfilled(final Object fulfilledValue)
    {
        return new F2<FR1<Object, R>, FR2<Object, Throwable, R>>() {
            @Override public void
            call(final FR1<Object, R> onFulfilled, final FR2<Object, Throwable, R> onRejected) throws Throwable {
                onFulfilled.call(fulfilledValue);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private F2<FR1<Object, R>, FR2<Object, Throwable, R>>
    callOnRejected(final Object rejectedReason, final Throwable rejectedException)
    {
        return new F2<FR1<Object, R>, FR2<Object, Throwable, R>>() {
            @Override public void
            call(final FR1<Object, R> onFulfilled, final FR2<Object, Throwable, R> onRejected) throws Throwable {
                onRejected.call(rejectedReason, rejectedException);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private PromiseFactory<P> originPromiseFactory()
    {
        return new PromiseFactory<P>() {
            @Override public P originPromise() { return null; }
            @Override public P fulfilledPromise(final Object value) { return promiseFactory().originPromise(); }
            @Override public P rejectedPromise(final Object reason, final Throwable exception) { return null; }
            @Override public P alwaysPendingPromise() { return null; }
            @Override public P mutablePromise(final PromiseStore store) { return null; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private <E> Return<E> retNoWait(final E ret)
    {
        return new Return<E>() { @Override E call(final Params params, final TestStep resStep) {
            resStep.finish();
            return ret;
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private Return<R> retThrowException(final Throwable exception)
    {
        return new Return<R>() { @Override R call(final Params params, final TestStep resStep) throws Throwable {
            resStep.finish();
            throw exception;
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private Return<T> retThenable(final T thenable)
    {
        return new Return<T>() { @Override T call(final Params params, final TestStep resStep) {
            return thenable(thenable, resStep);
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private interface CallbackLogger
    {
        void onFulfilled(final Object id, final Object thread, final Object value);
        void onRejected(final Object id, final Object thread, final Object reason, final Object exception);
        void onPending(final Object id);
    }
    //-----------------------------------------------------------------------------------------------------------------
    abstract class Return<E>
    {
        abstract E call(final Params params, final TestStep resStep) throws Throwable;
    }
    //-----------------------------------------------------------------------------------------------------------------
    private abstract class ReturnSupplier<TI, TO>
    {
        abstract Return<TO> get(final Return<? extends TI> retResolution);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private abstract class ToPromiseSupplier<TP>
    {
        abstract TP get(final P promise);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private abstract class PromiseDoThen
    {
        abstract P get(final Params params, final Executor exec);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static abstract class ExecutorSupplier
    {
        //-------------------------------------------------------------------------------------------------------------
        static ExecutorSupplier forResolveBeforeCallback = new ExecutorSupplier() {
            @Override ExecutorService executor() { return null; }
            @Override Thread thread() { return null; }
        };
        //-------------------------------------------------------------------------------------------------------------
        static ExecutorSupplier forResolveAfterCallback()
        {
            return new ExecutorSupplier() {
                private TestThread thread;
                @Override ExecutorService executor() { return (thread = new TestThread()).executor; }
                @Override Thread thread() { return thread; }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        abstract ExecutorService executor();
        //-------------------------------------------------------------------------------------------------------------
        abstract Thread thread();
        //-------------------------------------------------------------------------------------------------------------
        private Matcher<Thread> ofThread()
        {
            return new BaseMatcher<Thread>() {
                @Override public boolean matches(final Object item) {
                    final Thread thread = thread();
                    return thread == null || thread == item;
                }

                @Override public void describeTo(final Description desc) {
                    desc.appendValue(thread());
                }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    class Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Injectable TestLogger loggerMock = null;
        @Injectable BasePromiseTest.CallbackLogger callbackMock = null;
        @Injectable ResolveAction resolveActionMock = null;
        @Injectable FR1<Object, R> unusedOnFulfilled = null;
        @Injectable FR2<Object, Throwable, R> unusedOnRejected = null;
        @Injectable FR2<TestStep, Object, R> unusedOnFulfilledWithResStep = null;
        @Injectable FR3<TestStep, Object, Throwable, R> unusedOnRejectedWithResStep = null;
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsFulfilled() { return new Object[][] {
            {originPromiseFactory(), "ORIGIN",    null},
            {promiseFactory(),       "FULFILLED", null},
            {promiseFactory(),       "FULFILLED", 123},
            {promiseFactory(),       "FULFILLED", "abc"},
        };}
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsRejected() { return new Object[][] {
            {null,  null,                   null},
            {123,   null,                   null},
            {"xyz", new Exception(),        Exception.class},
            {true,  new RuntimeException(), RuntimeException.class},
        };}
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsFulfilledResolution() { return new Object[][] {
            {retNoWait(fulfilledResolution(null)),                                                          false, "FULFILLED", "FULFILLED", null,  null, null},
            {retNoWait(fulfilledResolution(123)),                                                           false, "FULFILLED", "FULFILLED", 123,   null, null},
            {retNoWait(testPromise(callOnFulfilled("abc"))),                                                false, "FULFILLED", "FULFILLED", "abc", null, null},
            {retNoWait(promiseFactory().fulfilledPromise('D')),                                             false, "FULFILLED", "FULFILLED", 'D',   null, null},
            {suppPendingMutablePromise.get(retNoWait(fulfilledResolution(4.5))),                            false, "MUTABLE",   "FULFILLED", 4.5,   null, null},
            {suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(false))),                         false, "FULFILLED", "FULFILLED", false, null, null},
            {suppThenableResolve.get(retNoWait(fulfilledResolution(null))),                                 false, "FULFILLED", "FULFILLED", null,  null, null},
            {suppThenableResolve.get(retNoWait(fulfilledResolution(true))),                                 false, "FULFILLED", "FULFILLED", true,  null, null},
            {suppThenableResolve.get(retNoWait(testPromise(callOnFulfilled(678)))),                         false, "FULFILLED", "FULFILLED", 678,   null, null},
            {suppThenableResolve.get(retNoWait(promiseFactory().fulfilledPromise("ijk"))),                  false, "FULFILLED", "FULFILLED", "ijk", null, null},
            {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(fulfilledResolution('E')))),   false, "MUTABLE",   "FULFILLED", 'E',   null, null},
            {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(fulfilledResolution(-9.0)))), false, "FULFILLED", "FULFILLED", -9.0,  null, null},
            {retThenable(thenableResolve(6543L)),                                                           false, "FULFILLED", "FULFILLED", 6543L, null, null},
        };}
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsSelectedFulfilledResolution()
        {
            return TestData.rows(paramsFulfilledResolution(), 0, 4, 7, 12);
        }
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsRejectedResolution() { return new Object[][] {
            {retNoWait(rejectedResolution(false, new Throwable())),                                                                   false, "REJECTED", "REJECTED", null, false, Throwable.class},
            {retThrowException(new Exception()),                                                                                      false, "REJECTED", "REJECTED", null, null,  Exception.class},
            {retNoWait(testPromise(callOnRejected(true, new RuntimeException()))),                                                    false, "REJECTED", "REJECTED", null, true,  RuntimeException.class},
            {retNoWait(promiseFactory().rejectedPromise(-987, new ClassCastException())),                                             false, "REJECTED", "REJECTED", null, -987,  ClassCastException.class},
            {suppPendingMutablePromise.get(retNoWait(rejectedResolution("pqr", new Throwable()))),                                    false, "MUTABLE",  "REJECTED", null, "pqr", Throwable.class},
            {suppResolvedMutablePromise.get(retNoWait(rejectedResolution('F', new Exception()))),                                     false, "REJECTED", "REJECTED", null, 'F',   Exception.class},
            {suppThenableResolve.get(retNoWait(rejectedResolution(-6.5, new RuntimeException()))),                                    false, "REJECTED", "REJECTED", null, -6.5,  RuntimeException.class},
            {suppThenableResolve.get(retThrowException(new ClassCastException())),                                                    false, "REJECTED", "REJECTED", null, null,  ClassCastException.class},
            {suppThenableResolve.get(retNoWait(testPromise(callOnRejected(false, new Throwable())))),                                 false, "REJECTED", "REJECTED", null, false, Throwable.class},
            {suppThenableResolve.get(retNoWait(promiseFactory().rejectedPromise(true, new Exception()))),                             false, "REJECTED", "REJECTED", null, true,  Exception.class},
            {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(rejectedResolution(-654, new RuntimeException())))),     false, "MUTABLE",  "REJECTED", null, -654,  RuntimeException.class},
            {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(rejectedResolution("xyz", new ClassCastException())))), false, "REJECTED", "REJECTED", null, "xyz", ClassCastException.class},
            {retThenable(thenableReject('G', new Throwable())),                                                                       false, "REJECTED", "REJECTED", null, 'G',   Throwable.class},
            {retThenable(thenableReject(0.432)),                                                                                      false, "REJECTED", "REJECTED", null, 0.432, null},
            {retThenable(thenableReject(new Exception())),                                                                            false, "REJECTED", "REJECTED", null, null,  Exception.class},
        };}
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsSelectedRejectedResolution()
        {
            return TestData.rows(paramsRejectedResolution(), 1, 4, 9, 12);
        }
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsPendingResolution() { return new Object[][] {
            {retNoWait(testPromise(callNothing)),                                                           false, "MUTABLE",        "PENDING", null, null, null},
            {retNoWait(promiseFactory().alwaysPendingPromise()),                                            true,  "ALWAYS-PENDING", "PENDING", null, null, null},
            {retPendingPromise,                                                                             false, "MUTABLE",        "PENDING", null, null, null},
            {suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution())),                           true,  "MUTABLE",        "PENDING", null, null, null},
            {suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution())),                          true,  "ALWAYS-PENDING", "PENDING", null, null, null},
            {suppThenableResolve.get(retNoWait(testPromise(callNothing))),                                  false, "MUTABLE",        "PENDING", null, null, null},
            {suppThenableResolve.get(retNoWait(promiseFactory().alwaysPendingPromise())),                   true,  "ALWAYS-PENDING", "PENDING", null, null, null},
            {suppThenableResolve.get(retPendingPromise),                                                    false, "MUTABLE",        "PENDING", null, null, null},
            {suppThenableResolve.get(suppPendingMutablePromise.get(retNoWait(alwaysPendingResolution()))),  true,  "MUTABLE",        "PENDING", null, null, null},
            {suppThenableResolve.get(suppResolvedMutablePromise.get(retNoWait(alwaysPendingResolution()))), true,  "ALWAYS-PENDING", "PENDING", null, null, null},
            {retThenable(thenableNop()),                                                                    false, "MUTABLE",        "PENDING", null, null, null},
        };}
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsSelectedPendingResolution()
        {
            return TestData.rows(paramsPendingResolution(), 1, 2, 3, 10);
        }
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsResolution()
        {
            return TestData.union(
                paramsFulfilledResolution(),
                paramsRejectedResolution(),
                paramsPendingResolution()
            );
        }
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsSelectedResolution()
        {
            return TestData.union(
                paramsSelectedFulfilledResolution(),
                paramsSelectedRejectedResolution(),
                paramsSelectedPendingResolution()
            );
        }
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsMutablePromisePrepend() {
            return new Object[][] {
                {suppPendingMutablePromise, false},
                {suppResolvedMutablePromise, true}
            };}
        //-------------------------------------------------------------------------------------------------------------
        final Object[][] paramsMutable()
        {
            return TestData.merge(paramsMutablePromisePrepend(), paramsSelectedResolution());
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseFactoryMethods extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsFulfilled")
        public final void testFulfilledPromise(
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) {
            assertThat(
                promiseFactory.fulfilledPromise(fulfilledValue),
                promiseMatcher(promiseType, PromiseState.FULFILLED, fulfilledValue, null, null)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsRejected")
        public final void testRejectedPromise(
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) {
            assertThat(
                promiseFactory().rejectedPromise(rejectedReason, rejectedException),
                promiseMatcher("REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void testAlwaysPendingPromise()
        {
            assertThat(
                promiseFactory().alwaysPendingPromise(),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsMutable")
        public final void testMutablePromise(
            final ReturnSupplier<R, ? extends P> suppPromise,
            final boolean isResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();
            final P promise = suppPromise.get(retResolution).call(this, promiseStep);

            final Matcher<P> resolveMatcher
                = promiseMatcher("MUTABLE", expectedState, expectedValue, expectedReason, expectedExceptionClass);

            final Matcher<P> presyncMatcher = !isResolved
                ? promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null)
                : resolveMatcher;

            assertThat(promise, presyncMatcher);
            promiseStep.sync();
            assertThat(promise, resolveMatcher);

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseAwaitMethods extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsFulfilled")
        public final void withFulfilledWaitUnlimited(
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) throws Exception
        {
            assertEquals(fulfilledValue, await(promiseFactory.fulfilledPromise(fulfilledValue)));

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsFulfilled")
        public final void withFulfilledWaitLimited(
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) throws Exception
        {
            assertEquals(
                fulfilledValue,
                await(promiseFactory.fulfilledPromise(fulfilledValue), 1, TimeUnit.SECONDS)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsRejected")
        public final void withRejectedWaitUnlimited(
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) throws Exception {
            try {
                await(promiseFactory().rejectedPromise(rejectedReason, rejectedException));
                fail();
            } catch (final PromiseRejectedException e) {
                assertEquals(translateReason(rejectedReason), e.reason());
                assertEquals(rejectedException, e.exception());
            }

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsRejected")
        public final void withRejectedWaitLimited(
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) throws Exception {
            try {
                await(promiseFactory().rejectedPromise(rejectedReason, rejectedException), 1, TimeUnit.SECONDS);
                fail();
            } catch (final PromiseRejectedException e) {
                assertEquals(translateReason(rejectedReason), e.reason());
                assertEquals(rejectedException, e.exception());
            }

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test(expected = InterruptedException.class)
        public final void withAlwaysPendingWaitUnlimited() throws Exception
        {
            final Thread testThread = Thread.currentThread();

            new Thread() {@Override public void run() {
                TestUtil.sleep(100);
                testThread.interrupt();
            }}.start();

            try {
                await(promiseFactory().alwaysPendingPromise());
            } finally {
                new FullVerificationsInOrder() {};
            }
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test(expected = TimeoutException.class)
        public final void withAlwaysPendingWaitLimited() throws Exception
        {
            try {
                await(promiseFactory().alwaysPendingPromise(), 100, TimeUnit.MILLISECONDS);
            } finally {
                new FullVerificationsInOrder() {};
            }
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsMutable")
        public final void withMutableWaitUnlimited(
            final ReturnSupplier<R, ? extends P> suppPromise,
            final boolean isResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep threadStep = new TestStep();
            final TestStep promiseStep = new TestStep();

            if (!isResolved || expectedState == PromiseState.PENDING)
                threadStep.pass();

            final P promise = suppPromise.get(retResolution).call(this, promiseStep);

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
                final Object value = await(promise);
                callbackMock.onFulfilled("test", Thread.currentThread(), value);
            } catch (final PromiseRejectedException e) {
                callbackMock.onRejected("test", Thread.currentThread(), e.reason(), e.exception());
            } catch (final InterruptedException e) {
                callbackMock.onPending("test");
            }

            threadStep.sync();

            final Runnable awaitVerifications = new Runnable() { @Override public void run() {
                switch (expectedState) {
                case FULFILLED:
                    new Verifications() {{ callbackMock.onFulfilled("test", Thread.currentThread(), expectedValue); }};
                    break;

                case REJECTED:
                    new Verifications() {{
                        callbackMock.onRejected(
                            "test", Thread.currentThread(),
                            translateReason(expectedReason), withArgThat(TestUtil.ofClass(expectedExceptionClass))
                        );
                    }};
                    break;

                case PENDING:
                    new Verifications() {{ callbackMock.onPending("test"); }};
                    break;

                default:
                    fail();
                }
            }};

            final Runnable syncVerifications = new Runnable() { @Override public void run() {
                new Verifications() {{ loggerMock.log("sync"); }};
            }};

            if (!isResolved || expectedState == PromiseState.PENDING) {
                syncVerifications.run();
                awaitVerifications.run();
            } else {
                awaitVerifications.run();
                syncVerifications.run();
            }

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsMutable")
        public final void withMutableWaitLimited(
            final ReturnSupplier<R, ? extends P> suppPromise,
            final boolean isResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep threadStep = new TestStep();
            final TestStep promiseStep = new TestStep();

            if (!isResolved || expectedState == PromiseState.PENDING)
                threadStep.pass();

            final P promise = suppPromise.get(retResolution).call(this, promiseStep);

            new Thread() { @Override public void run() {
                threadStep.pause();
                loggerMock.log("sync");
                promiseStep.sync();
                threadStep.finish();
            }}.start();

            try {
                final Object value = await(promise, 100, TimeUnit.MILLISECONDS);
                callbackMock.onFulfilled("test", Thread.currentThread(), value);
            } catch (final PromiseRejectedException e) {
                callbackMock.onRejected("test", Thread.currentThread(), e.reason(), e.exception());
            } catch (final TimeoutException e) {
                callbackMock.onPending("test");
            }

            threadStep.sync();

            final Runnable awaitVerifications = new Runnable() { @Override public void run() {
                switch (expectedState) {
                case FULFILLED:
                    new Verifications() {{ callbackMock.onFulfilled("test", Thread.currentThread(), expectedValue); }};
                    break;

                case REJECTED:
                    new Verifications() {{
                        callbackMock.onRejected(
                            "test", Thread.currentThread(),
                            translateReason(expectedReason), withArgThat(TestUtil.ofClass(expectedExceptionClass))
                        );
                    }};
                    break;

                case PENDING:
                    new Verifications() {{ callbackMock.onPending("test"); }};
                    break;

                default:
                    fail();
                }
            }};

            final Runnable syncVerifications = new Runnable() { @Override public void run() {
                new Verifications() {{ loggerMock.log("sync"); }};
            }};

            if (!isResolved || expectedState == PromiseState.PENDING) {
                syncVerifications.run();
                awaitVerifications.run();
            } else {
                awaitVerifications.run();
                syncVerifications.run();
            }

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseToPromiseMethods extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsToPromise() {
            return new Object[][] {{
                new ToPromiseSupplier<UntypedPromiseImpl>() {
                    @Override UntypedPromiseImpl get(final P promise) { return toUntypedPromise(promise); }
                },
                new UntypedPromiseImplTest()
            }, {
                new ToPromiseSupplier<TypedPromiseImpl<?, ?>>() {
                    @Override TypedPromiseImpl<?, ?> get(final P promise) { return toTypedPromise(promise); }
                },
                new TypedPromiseImplTest()
            }, {
                new ToPromiseSupplier<LightWeightPromiseImpl<?>>() {
                    @Override LightWeightPromiseImpl<?> get(final P promise) { return toLightWeightPromise(promise); }
                },
                new LightWeightPromiseImplTest()
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithFulfilled()
        {
            return TestData.product(paramsToPromise(), paramsFulfilled());
        }

        @Test
        @Parameters(method = "paramsWithFulfilled")
        public final <TR, TP extends TR> void withFulfilled(
            final ToPromiseSupplier<TP> suppToPromise,
            final BasePromiseTest<TR, TP, ?> toTest,
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) {
            assertThat(
                suppToPromise.get(promiseFactory.fulfilledPromise(fulfilledValue)),
                toTest.promiseMatcher(promiseType, PromiseState.FULFILLED, fulfilledValue, null, null)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithRejected()
        {
            return TestData.product(paramsToPromise(), paramsRejected());
        }

        @Test
        @Parameters(method = "paramsWithRejected")
        public final <TR, TP extends TR> void withRejected(
            final ToPromiseSupplier<TP> suppToPromise,
            final BasePromiseTest<TR, TP, ?> toTest,
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) {
            assertThat(
                suppToPromise.get(promiseFactory().rejectedPromise(rejectedReason, rejectedException)),
                toTest.promiseMatcher(
                    "REJECTED",
                    PromiseState.REJECTED, null, translateReason(rejectedReason), rejectedExceptionClass
                )
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsToPromise")
        public final <TR, TP extends TR> void withAlwaysPending(
            final ToPromiseSupplier<TP> suppToPromise,
            final BasePromiseTest<TR, TP, ?> toTest
        ) {
            assertThat(
                suppToPromise.get(promiseFactory().alwaysPendingPromise()),
                toTest.promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithMutable()
        {
            return TestData.product(paramsToPromise(), paramsMutable());
        }

        @Test
        @Parameters(method = "paramsWithMutable")
        public final <TR, TP extends TR> void withMutable(
            final ToPromiseSupplier<TP> suppToPromise,
            final BasePromiseTest<TR, TP, ?> toTest,
            final ReturnSupplier<R, ? extends P> suppPromise,
            final boolean isResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();

            final P promise = suppPromise.get(retResolution).call(this, promiseStep);

            final TP toPromise1 = suppToPromise.get(promise);

            final String resolvedType
                = expectedState == PromiseState.FULFILLED ? "FULFILLED"
                : expectedState == PromiseState.REJECTED ? "REJECTED"
                : isAlwaysPending ? "ALWAYS-PENDING" : "MUTABLE";

            final Matcher<TP> resolveMatcher = toTest.promiseMatcher(
                !isResolved ? "MUTABLE" : resolvedType,
                expectedState, expectedValue, translateReason(expectedReason), expectedExceptionClass
            );

            final Matcher<TP> presyncMatcher = !isResolved
                ? toTest.promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null)
                : resolveMatcher;

            assertThat(toPromise1, presyncMatcher);

            promiseStep.sync();

            assertThat(toPromise1, resolveMatcher);

            assertThat(
                suppToPromise.get(promise),
                toTest.promiseMatcher(
                    resolvedType,
                    expectedState, expectedValue, translateReason(expectedReason), expectedExceptionClass
                )
            );

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseApplyResolveActionMethods extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsFulfilled")
        public final void withFulfilled(
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) {
            applyResolveAction(promiseFactory.fulfilledPromise(fulfilledValue), resolveActionMock);

            new FullVerificationsInOrder() {{
                resolveActionMock.setFulfilled(fulfilledValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsRejected")
        public final void withRejected(
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) {
            applyResolveAction(
                promiseFactory().rejectedPromise(rejectedReason, rejectedException),
                resolveActionMock
            );

            new FullVerificationsInOrder() {{
                resolveActionMock.setRejected(translateReason(rejectedReason), rejectedException);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void withAlwaysPending()
        {
            applyResolveAction(promiseFactory().alwaysPendingPromise(), resolveActionMock);

            new FullVerificationsInOrder() {{
                resolveActionMock.setAlwaysPending();
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsMutable")
        public final void withMutable(
            final ReturnSupplier<R, ? extends P> suppPromise,
            final boolean isResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();

            applyResolveAction(
                suppPromise.get(retResolution).call(this, promiseStep),
                resolveActionMock
            );

            loggerMock.log("sync");
            promiseStep.sync();

            final Runnable resolveActionMockVerifications = new Runnable() { @Override public void run() {
                switch (expectedState) {
                case PENDING:
                    new Verifications() {{ resolveActionMock.setAlwaysPending(); }};
                    break;

                case FULFILLED:
                    new Verifications() {{ resolveActionMock.setFulfilled(expectedValue); }};
                    break;

                case REJECTED:
                    new Verifications() {{
                        resolveActionMock.setRejected(
                            translateReason(expectedReason),
                            this.<Throwable>withArgThat(TestUtil.ofClass(expectedExceptionClass))
                        );
                    }};
                    break;

                default:
                    fail();
                }
            }};

            final Runnable syncVerifications = new Runnable() { @Override public void run() {
                new Verifications() {{ loggerMock.log("sync"); }};
            }};

            if (!isAlwaysPending && expectedState == PromiseState.PENDING)
                syncVerifications.run();
            else if (!isResolved) {
                syncVerifications.run();
                resolveActionMockVerifications.run();
            } else {
                resolveActionMockVerifications.run();
                syncVerifications.run();
            }

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseDoThenMethods extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        private P callDoThen(
            final TestStep outerResStep,
            final P promise,
            final ExecutorService exec,
            final FR2<TestStep, Object, ? extends R> onFulfilled,
            final FR3<TestStep, Object, Throwable, ? extends R> onRejected
        ) {
            final TestStep innerResStep = new TestStep();

            final Runnable callbackInvoked = new Runnable() { @Override public void run() {
                final ExecutorService finishExec;

                if (exec != null) {
                    outerResStep.pause();
                    finishExec = exec;
                } else
                    finishExec = Executors.newSingleThreadExecutor();

                finishExec.execute(new Runnable() { @Override public void run() {
                    innerResStep.waitFinished();
                    outerResStep.finish();
                    finishExec.shutdown();
                }});
            }};

            try {
                return doThen(
                    promise,
                    exec,
                    new FR1<Object, R>() {
                        @Override public R call(final Object value) throws Throwable {
                            callbackInvoked.run();
                            return onFulfilled.call(innerResStep, value);
                        }
                    },
                    new FR2<Object, Throwable, R>() {
                        @Override public R call(final Object reason, final Throwable exception) throws Throwable {
                            callbackInvoked.run();
                            return onRejected.call(innerResStep, reason, exception);
                        }
                    }
                );
            } finally {
                innerResStep.pass();
            }
        }
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsExecutorSupplierPrepend() { return new Object[][] {
            {ExecutorSupplier.forResolveBeforeCallback},
            {ExecutorSupplier.forResolveAfterCallback()}
        };}
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsDoThenMutablePromisePrepend() { return new Object[][] {
            {suppPendingMutablePromise,  ExecutorSupplier.forResolveBeforeCallback,  false},
            {suppPendingMutablePromise,  ExecutorSupplier.forResolveAfterCallback(), false},
            {suppResolvedMutablePromise, ExecutorSupplier.forResolveBeforeCallback,  true},
            {suppResolvedMutablePromise, ExecutorSupplier.forResolveAfterCallback(), false},
        };}
        //-------------------------------------------------------------------------------------------------------------
        private Object[][] paramsFullResolutionPrepend() { return new Object[][] {
            {suppIdentity},
            {suppThenableResolve}
        };}
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsFulfilled")
        public final void withFulfilledNullCallback(
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue
        ) {
            final ExecutorService exec = Executors.newSingleThreadExecutor();
            final P promise = doThen(promiseFactory.fulfilledPromise(fulfilledValue), exec, null, unusedOnRejected);
            assertThat(promise, promiseMatcher("FULFILLED", PromiseState.FULFILLED, fulfilledValue, null, null));
            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithFulfilledNonnullCallback()
        {
            return TestData.product(
                paramsExecutorSupplierPrepend(),
                TestData.merge(paramsFulfilled(), paramsFullResolutionPrepend(), paramsSelectedResolution())
            );
        }

        @Test
        @Parameters(method = "paramsWithFulfilledNonnullCallback")
        public final void withFulfilledNonnullCallback(
            final ExecutorSupplier suppExec,
            final PromiseFactory<? extends P> srcPromiseFactory,
            final String srcPromiseType,
            final Object fulfilledValue,
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) {
            final ExecutorService exec = suppExec.executor();
            final TestStep resolutionStep = new TestStep();
            final Return<? extends R> retCallbackReturn = suppCallbackReturn.get(retResolution);

            final P promise = callDoThen(
                resolutionStep,
                srcPromiseFactory.fulfilledPromise(fulfilledValue),
                exec,
                new FR2<TestStep, Object, R>() {
                    @Override public R call(final TestStep resStep, final Object value) throws Throwable {
                        callbackMock.onFulfilled("test", Thread.currentThread(), value);
                        return retCallbackReturn.call(BaseDoThenMethods.this, resStep);
                    }
                },
                unusedOnRejectedWithResStep
            );

            if (exec != null)
                assertThat(promise, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));

            resolutionStep.sync();

            assertThat(
                promise,
                promiseMatcher(
                    exec == null ? expectedType : "MUTABLE",
                    expectedState, expectedValue, expectedReason, expectedExceptionClass
                )
            );

            new FullVerificationsInOrder() {{
                callbackMock.onFulfilled("test", withArgThat(suppExec.ofThread()), fulfilledValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsRejected")
        public final void withRejectedNullCallback(
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass
        ) {
            final ExecutorService exec = Executors.newSingleThreadExecutor();

            final P promise = doThen(
                promiseFactory().rejectedPromise(rejectedReason, rejectedException),
                exec, unusedOnFulfilled, null
            );

            assertThat(
                promise,
                promiseMatcher("REJECTED", PromiseState.REJECTED, null, rejectedReason, rejectedExceptionClass)
            );

            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithRejectedNonnullCallback()
        {
            return TestData.product(
                paramsExecutorSupplierPrepend(),
                TestData.merge(paramsRejected(), paramsFullResolutionPrepend(), paramsSelectedResolution())
            );
        }

        @Test
        @Parameters(method = "paramsWithRejectedNonnullCallback")
        public final void withRejectedNonnullCallback(
            final ExecutorSupplier suppExec,
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass,
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) {
            final ExecutorService exec = suppExec.executor();
            final TestStep resolutionStep = new TestStep();
            final Return<? extends R> retCallbackReturn = suppCallbackReturn.get(retResolution);

            final P promise = callDoThen(
                resolutionStep,
                promiseFactory().rejectedPromise(rejectedReason, rejectedException),
                exec,
                unusedOnFulfilledWithResStep,
                new FR3<TestStep, Object, Throwable, R>() {
                    @Override public R call(final TestStep resStep, final Object reason, final Throwable exception)
                        throws Throwable
                    {
                        callbackMock.onRejected("test", Thread.currentThread(), reason, exception);
                        return retCallbackReturn.call(BaseDoThenMethods.this, resStep);
                    }
                }
            );

            if (exec != null)
                assertThat(promise, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));

            resolutionStep.sync();

            assertThat(
                promise,
                promiseMatcher(
                    exec == null ? expectedType : "MUTABLE",
                    expectedState, expectedValue, expectedReason, expectedExceptionClass
                )
            );

            new FullVerificationsInOrder() {{
                callbackMock.onRejected(
                    "test", withArgThat(suppExec.ofThread()),
                    translateReason(rejectedReason), rejectedException
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithAlwaysPending()
        {
            return new Object[][] {{
                new PromiseDoThen() { @Override P get(final Params params, final Executor exec) {
                    return doThen(
                        promiseFactory().alwaysPendingPromise(),
                        exec, params.unusedOnFulfilled, params.unusedOnRejected
                    );
                }}
            }, {
                new PromiseDoThen() { @Override P get(final Params params, final Executor exec) {
                    return doThen(promiseFactory().alwaysPendingPromise(), exec, null, params.unusedOnRejected);
                }}
            }, {
                new PromiseDoThen() { @Override P get(final Params params, final Executor exec) {
                    return doThen(promiseFactory().alwaysPendingPromise(), exec, params.unusedOnFulfilled, null);
                }}
            }};
        }

        @Test
        @Parameters(method = "paramsWithAlwaysPending")
        public final void withAlwaysPending(final PromiseDoThen suppSrcPromise)
        {
            final ExecutorService exec = Executors.newSingleThreadExecutor();

            assertThat(
                suppSrcPromise.get(this, exec),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null)
            );

            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithMutableNullOnFulfilled()
        {
            return TestData.merge(paramsMutablePromisePrepend(), paramsSelectedFulfilledResolution());
        }

        @Test
        @Parameters(method = "paramsWithMutableNullOnFulfilled")
        public final void withMutableNullOnFulfilled(
            final ReturnSupplier<R, ? extends P> suppSrcPromise,
            final boolean isSrcResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();
            final ExecutorService exec = Executors.newSingleThreadExecutor();

            final P promise = doThen(
                suppSrcPromise.get(retResolution).call(this, promiseStep),
                exec, null, unusedOnRejected
            );

            final Matcher<P> presyncMatcher;
            final Matcher<P> resolveMatcher;

            if (!isSrcResolved) {
                presyncMatcher = promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null);
                resolveMatcher = promiseMatcher("MUTABLE", PromiseState.FULFILLED, expectedValue, null, null);
            } else
                presyncMatcher = resolveMatcher
                    = promiseMatcher("FULFILLED", PromiseState.FULFILLED, expectedValue, null, null);

            assertThat(promise, presyncMatcher);
            promiseStep.sync();
            assertThat(promise, resolveMatcher);

            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithMutableNullOnRejected()
        {
            return TestData.merge(paramsMutablePromisePrepend(), paramsSelectedRejectedResolution());
        }

        @Test
        @Parameters(method = "paramsWithMutableNullOnRejected")
        public final void withMutableNullOnRejected(
            final ReturnSupplier<R, ? extends P> suppSrcPromise,
            final boolean isSrcResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();
            final ExecutorService exec = Executors.newSingleThreadExecutor();

            final P promise = doThen(
                suppSrcPromise.get(retResolution).call(this, promiseStep),
                exec, unusedOnFulfilled, null
            );

            final Matcher<P> presyncMatcher;
            final Matcher<P> resolveMatcher;

            if (!isSrcResolved) {
                presyncMatcher = promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null);

                resolveMatcher
                    = promiseMatcher("MUTABLE", PromiseState.REJECTED, null, expectedReason, expectedExceptionClass);
            } else
                presyncMatcher = resolveMatcher
                    = promiseMatcher("REJECTED", PromiseState.REJECTED, null, expectedReason, expectedExceptionClass);

            assertThat(promise, presyncMatcher);
            promiseStep.sync();
            assertThat(promise, resolveMatcher);

            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithPendingMutable()
        {
            return TestData.merge(paramsMutablePromisePrepend(), paramsSelectedPendingResolution());
        }

        @Test
        @Parameters(method = "paramsWithPendingMutable")
        public final void withPendingMutable(
            final ReturnSupplier<R, ? extends P> suppSrcPromise,
            final boolean isSrcResolved,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep promiseStep = new TestStep();
            final ExecutorService exec = Executors.newSingleThreadExecutor();

            final P promise = doThen(
                suppSrcPromise.get(retResolution).call(this, promiseStep),
                exec, unusedOnFulfilled, unusedOnRejected
            );

            final String chainDstPromiseType = !isAlwaysPending ? "MUTABLE" : "ALWAYS-PENDING";
            final String promiseType = !isSrcResolved ? "MUTABLE" : chainDstPromiseType;

            assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null, null));

            promiseStep.sync();

            assertThat(promise, promiseMatcher(promiseType, PromiseState.PENDING, null, null, null));

            assertThat(
                doThen(promise, exec, unusedOnFulfilled, unusedOnRejected),
                promiseMatcher(chainDstPromiseType, PromiseState.PENDING, null, null, null)
            );

            exec.shutdown();

            new FullVerificationsInOrder() {};
        }
        //-------------------------------------------------------------------------------------------------------------
        private void testMutableResolve(
            final Object id,
            final Return<? extends P> retSrcPromise,
            final ExecutorService exec,
            final boolean resolveBeforeCallback,
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final Return<? extends R> retCallbackReturn = suppCallbackReturn.get(retResolution);
            final TestStep srcPromiseStep = new TestStep();
            final TestStep resolutionStep = new TestStep();

            final P promise = callDoThen(
                resolutionStep,
                retSrcPromise.call(this, srcPromiseStep),
                exec,
                new FR2<TestStep, Object, R>() {
                    @Override public R call(final TestStep resStep, final Object value) throws Throwable {
                        callbackMock.onFulfilled(id, Thread.currentThread(), value);
                        return retCallbackReturn.call(BaseDoThenMethods.this, resStep);
                    }
                },
                new FR3<TestStep, Object, Throwable, R>() {
                    @Override public R call(final TestStep resStep, final Object reason, final Throwable exception)
                        throws Throwable
                    {
                        callbackMock.onRejected(id, Thread.currentThread(), reason, exception);
                        return retCallbackReturn.call(BaseDoThenMethods.this, resStep);
                    }
                }
            );

            if (!resolveBeforeCallback)
                assertThat(promise, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));

            srcPromiseStep.sync();
            resolutionStep.sync();

            assertThat(
                promise,
                promiseMatcher(
                    resolveBeforeCallback ? expectedType : "MUTABLE",
                    expectedState, expectedValue, expectedReason, expectedExceptionClass
                )
            );
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithFulfilledMutable()
        {
            return TestData.product(
                paramsDoThenMutablePromisePrepend(),
                TestData.merge(paramsFulfilled(), paramsFullResolutionPrepend(), paramsSelectedResolution())
            );
        }

        @Test
        @Parameters(method = "paramsWithFulfilledMutable")
        public final void withFulfilledMutable(
            final ReturnSupplier<R, ? extends P> suppSrcPromise,
            final ExecutorSupplier suppExec,
            final boolean resolveBeforeCallback,
            final PromiseFactory<? extends P> promiseFactory,
            final String promiseType,
            final Object fulfilledValue,
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final ExecutorService exec = suppExec.executor();

            testMutableResolve(
                "test",
                suppSrcPromise.get(retNoWait(fulfilledResolution(fulfilledValue))),
                exec, resolveBeforeCallback,
                suppCallbackReturn, retResolution,
                expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
            );

            new FullVerificationsInOrder() {{
                callbackMock.onFulfilled("test", withArgThat(suppExec.ofThread()), fulfilledValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsWithRejectedMutable()
        {
            return TestData.product(
                paramsDoThenMutablePromisePrepend(),
                TestData.merge(paramsRejected(), paramsFullResolutionPrepend(), paramsSelectedResolution())
            );
        }

        @Test
        @Parameters(method = "paramsWithRejectedMutable")
        public final void withRejectedMutable(
            final ReturnSupplier<R, ? extends P> suppSrcPromise,
            final ExecutorSupplier suppExec,
            final boolean resolveBeforeCallback,
            final Object rejectedReason,
            final Throwable rejectedException,
            final Class<?> rejectedExceptionClass,
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final ExecutorService exec = suppExec.executor();

            testMutableResolve(
                "test",
                suppSrcPromise.get(retNoWait(rejectedResolution(rejectedReason, rejectedException))),
                exec, resolveBeforeCallback,
                suppCallbackReturn, retResolution,
                expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass
            );

            new FullVerificationsInOrder() {{
                callbackMock.onRejected(
                    "test", withArgThat(suppExec.ofThread()),
                    translateReason(rejectedReason), rejectedException
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private Object[][] paramsFullResolution()
        {
            return TestData.product(paramsFullResolutionPrepend(), paramsResolution());
        }

        @Test
        @Parameters(method = "paramsFullResolution")
        public final void testFullResolution(
            final ReturnSupplier<R, ? extends R> suppCallbackReturn,
            final Return<? extends R> retResolution,
            final boolean isAlwaysPending,
            final String expectedType,
            final PromiseState expectedState,
            final Object expectedValue,
            final Object expectedReason,
            final Class<?> expectedExceptionClass
        ) throws Throwable
        {
            final TestStep resolutionStep = new TestStep();
            final Return<? extends R> retCallbackReturn = suppCallbackReturn.get(retResolution);

            final P promise = callDoThen(
                resolutionStep,
                promiseFactory().fulfilledPromise("test"),
                null,
                new FR2<TestStep, Object, R>() {
                    @Override public R call(final TestStep resStep, final Object value) throws Throwable {
                        callbackMock.onFulfilled("test", Thread.currentThread(), value);
                        return retCallbackReturn.call(BaseDoThenMethods.this, resStep);
                    }
                },
                unusedOnRejectedWithResStep
            );

            resolutionStep.sync();

            assertThat(
                promise,
                promiseMatcher(expectedType, expectedState, expectedValue, expectedReason, expectedExceptionClass)
            );

            new FullVerificationsInOrder() {{
                callbackMock.onFulfilled("test", any, "test");
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseMultiCallbacks extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        private FR1<Object, R> newOnFulfilled(final CallbackLogger logger, final Object id, final R resolution)
        {
            return new FR1<Object, R>() { @Override public R call(final Object value) throws Throwable {
                logger.onFulfilled(id, Thread.currentThread(), value);
                return resolution;
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        private FR2<Object, Throwable, R>
        newOnRejected(final CallbackLogger logger, final Object id, final R resolution)
        {
            return new FR2<Object, Throwable, R>() {
                @Override public R call(final Object reason, final Throwable exception) throws Throwable {
                    logger.onRejected(id, Thread.currentThread(), reason, exception);
                    return resolution;
                }
            };
        }
        //-------------------------------------------------------------------------------------------------------------
        private void doTest(
            final R srcPromiseResolution,
            final ExecutorService exec,
            final TestLogger syncLogger,
            final CallbackLogger loggerWithoutExecutor,
            final CallbackLogger loggerWithExecutor,
            final Matcher<P> matcher0,
            final Matcher<P> matcher1,
            final Matcher<P> matcher3,
            final Matcher<P> matcher4,
            final Matcher<P> matcher6,
            final Matcher<P> matcher7,
            final Matcher<P> matcher9,
            final Matcher<P> matcher10,
            final Matcher<P> matcher12,
            final Matcher<P> matcher13,
            final Matcher<P> matcher15,
            final Matcher<P> matcher16,
            final ResolveAction resolveAction2,
            final ResolveAction resolveAction5,
            final ResolveAction resolveAction8,
            final ResolveAction resolveAction11,
            final ResolveAction resolveAction14,
            final ResolveAction resolveAction17
        ) throws Throwable
        {
            final TestStep srcPromiseStep = new TestStep();
            final TestStep execStep1 = new TestStep();
            final TestStep execStep2 = new TestStep();

            final P srcPromise = suppPendingMutablePromise.get(retNoWait(srcPromiseResolution))
                .call(this, srcPromiseStep);

            final R resolution0 = fulfilledResolution(123);

            final P promise0 = doThen(
                srcPromise, exec,
                newOnFulfilled(loggerWithExecutor, 0, resolution0),
                newOnRejected(loggerWithExecutor, 0, resolution0)
            );

            syncLogger.log("separate", 0, 1);

            final R resolution1 = rejectedResolution("abc", new Throwable());
            final P promise1 = doThen(srcPromise, null, newOnFulfilled(loggerWithoutExecutor, 1, resolution1), null);

            syncLogger.log("separate", 1, 2);

            applyResolveAction(srcPromise, resolveAction2);

            syncLogger.log("separate", 2, 3);

            final R resolution3 = thenableNop();
            final P promise3 = doThen(srcPromise, exec, null, newOnRejected(loggerWithExecutor, 3, resolution3));

            syncLogger.log("separate", 3, 4);

            final R resolution4 = alwaysPendingResolution();

            final P promise4 = doThen(
                srcPromise, null,
                newOnFulfilled(loggerWithoutExecutor, 4, resolution4),
                newOnRejected(loggerWithoutExecutor, 4, resolution4)
            );

            syncLogger.log("separate", 4, 5);

            applyResolveAction(srcPromise, resolveAction5);

            syncLogger.log("separate", 5, 6);

            final R resolution6 = fulfilledResolution(false);
            final P promise6 = doThen(srcPromise, exec, newOnFulfilled(loggerWithExecutor, 6, resolution6), null);

            syncLogger.log("separate", 6, 7);

            final R resolution7 = rejectedResolution('D', new Exception());
            final P promise7 = doThen(srcPromise, null, null, newOnRejected(loggerWithoutExecutor, 7, resolution7));

            syncLogger.log("separate", 7, 8);

            applyResolveAction(srcPromise, resolveAction8);

            assertThat(promise0, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));
            assertThat(promise1, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));
            assertThat(promise3, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));
            assertThat(promise4, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));
            assertThat(promise6, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));
            assertThat(promise7, promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null));

            syncLogger.log("syncWithoutExecutor", "1.before");
            syncLogger.log("syncWithExecutor", "1.before");
            srcPromiseStep.sync();
            exec.execute(new Runnable() { @Override public void run() { execStep1.finish(); }});
            execStep1.sync();
            syncLogger.log("syncWithoutExecutor", "1.after");
            syncLogger.log("syncWithExecutor", "1.after");

            assertThat(promise0, matcher0);
            assertThat(promise1, matcher1);
            assertThat(promise3, matcher3);
            assertThat(promise4, matcher4);
            assertThat(promise6, matcher6);
            assertThat(promise7, matcher7);

            final R resolution9 = thenableNop();

            final P promise9 = doThen(
                srcPromise, exec,
                newOnFulfilled(loggerWithExecutor, 9, resolution9),
                newOnRejected(loggerWithExecutor, 9, resolution9)
            );

            syncLogger.log("separate", 9, 10);

            final R resolution10 = alwaysPendingResolution();

            assertThat(
                doThen(srcPromise, null, newOnFulfilled(loggerWithoutExecutor, 10, resolution10), null),
                matcher10
            );

            syncLogger.log("separate", 10, 11);

            applyResolveAction(srcPromise, resolveAction11);

            syncLogger.log("separate", 11, 12);

            final R resolution12 = fulfilledResolution(-456);
            final P promise12 = doThen(srcPromise, exec, null, newOnRejected(loggerWithExecutor, 12, resolution12));

            syncLogger.log("separate", 12, 13);

            final R resolution13 = thenableNop();

            assertThat(
                doThen(
                    srcPromise, null,
                    newOnFulfilled(loggerWithoutExecutor, 13, resolution13),
                    newOnRejected(loggerWithoutExecutor, 13, resolution13)
                ),
                matcher13
            );

            syncLogger.log("separate", 13, 14);

            applyResolveAction(srcPromise, resolveAction14);

            syncLogger.log("separate", 14, 15);

            final R resolution15 = rejectedResolution("XYZ", new RuntimeException());
            final P promise15 = doThen(srcPromise, exec, newOnFulfilled(loggerWithExecutor, 15, resolution15), null);

            syncLogger.log("separate", 15, 16);

            final R resolution16 = alwaysPendingResolution();

            assertThat(
                doThen(srcPromise, null, null, newOnRejected(loggerWithoutExecutor, 16, resolution16)),
                matcher16
            );

            syncLogger.log("separate", 16, 17);

            applyResolveAction(srcPromise, resolveAction17);

            exec.execute(new Runnable() { @Override public void run() { execStep2.finish(); }});
            execStep2.sync();
            syncLogger.log("syncWithExecutor", "2.after");

            assertThat(promise9, matcher9);
            assertThat(promise12, matcher12);
            assertThat(promise15, matcher15);
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void withFulfilled(
            @Injectable final TestLogger syncLogger,
            @Injectable final CallbackLogger loggerWithoutExecutorMock,
            @Injectable final CallbackLogger loggerWithExecutorMock,
            @Injectable final ResolveAction resolveActionMock2,
            @Injectable final ResolveAction resolveActionMock5,
            @Injectable final ResolveAction resolveActionMock8,
            @Injectable final ResolveAction resolveActionMock11,
            @Injectable final ResolveAction resolveActionMock14,
            @Injectable final ResolveAction resolveActionMock17
        ) throws Throwable
        {
            final Object srcFulfilledValue = "test";
            final TestThread thread = new TestThread();

            doTest(
                fulfilledResolution(srcFulfilledValue),
                thread.executor,
                syncLogger,
                loggerWithoutExecutorMock,
                loggerWithExecutorMock,
                promiseMatcher("MUTABLE", PromiseState.FULFILLED, 123, null, null),
                promiseMatcher("MUTABLE", PromiseState.REJECTED, null, "abc", Throwable.class),
                promiseMatcher("MUTABLE", PromiseState.FULFILLED, srcFulfilledValue, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.FULFILLED, false, null, null),
                promiseMatcher("MUTABLE", PromiseState.FULFILLED, srcFulfilledValue, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("FULFILLED", PromiseState.FULFILLED, srcFulfilledValue, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher(null, PromiseState.REJECTED, null, "XYZ", RuntimeException.class),
                promiseMatcher("FULFILLED", PromiseState.FULFILLED, srcFulfilledValue, null, null),
                resolveActionMock2,
                resolveActionMock5,
                resolveActionMock8,
                resolveActionMock11,
                resolveActionMock14,
                resolveActionMock17
            );

            thread.executor.shutdown();

            new VerificationsInOrder() {{
                syncLogger.log("syncWithExecutor", "1.before");
                loggerWithExecutorMock.onFulfilled(0, thread, srcFulfilledValue);
                loggerWithExecutorMock.onFulfilled(6, thread, srcFulfilledValue);
                syncLogger.log("syncWithExecutor", "1.after");
                loggerWithExecutorMock.onFulfilled(9, thread, srcFulfilledValue);
                loggerWithExecutorMock.onFulfilled(15, thread, srcFulfilledValue);
                syncLogger.log("syncWithExecutor", "2.after");
            }};

            new FullVerificationsInOrder() {{
                syncLogger.log("separate", 0, 1);
                syncLogger.log("separate", 1, 2);
                syncLogger.log("separate", 2, 3);
                syncLogger.log("separate", 3, 4);
                syncLogger.log("separate", 4, 5);
                syncLogger.log("separate", 5, 6);
                syncLogger.log("separate", 6, 7);
                syncLogger.log("separate", 7, 8);
                syncLogger.log("syncWithoutExecutor", "1.before");
                resolveActionMock2.setFulfilled(srcFulfilledValue);
                resolveActionMock5.setFulfilled(srcFulfilledValue);
                resolveActionMock8.setFulfilled(srcFulfilledValue);
                loggerWithoutExecutorMock.onFulfilled(1, any, srcFulfilledValue);
                loggerWithoutExecutorMock.onFulfilled(4, any, srcFulfilledValue);
                syncLogger.log("syncWithoutExecutor", "1.after");
                syncLogger.log("separate", 9, 10);
                loggerWithoutExecutorMock.onFulfilled(10, any, srcFulfilledValue);
                syncLogger.log("separate", 10, 11);
                resolveActionMock11.setFulfilled(srcFulfilledValue);
                syncLogger.log("separate", 11, 12);
                syncLogger.log("separate", 12, 13);
                loggerWithoutExecutorMock.onFulfilled(13, any, srcFulfilledValue);
                syncLogger.log("separate", 13, 14);
                resolveActionMock14.setFulfilled(srcFulfilledValue);
                syncLogger.log("separate", 14, 15);
                syncLogger.log("separate", 15, 16);
                syncLogger.log("separate", 16, 17);
                resolveActionMock17.setFulfilled(srcFulfilledValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void withRejected(
            @Injectable final TestLogger syncLogger,
            @Injectable final CallbackLogger loggerWithoutExecutorMock,
            @Injectable final CallbackLogger loggerWithExecutorMock,
            @Injectable final ResolveAction resolveActionMock2,
            @Injectable final ResolveAction resolveActionMock5,
            @Injectable final ResolveAction resolveActionMock8,
            @Injectable final ResolveAction resolveActionMock11,
            @Injectable final ResolveAction resolveActionMock14,
            @Injectable final ResolveAction resolveActionMock17
        ) throws Throwable
        {
            final Object srcRejectedReason = "test";
            final RuntimeException srcRejectedException = new RuntimeException();
            final Class<?> srcRejectedExceptionClass = RuntimeException.class;
            final TestThread thread = new TestThread();

            doTest(
                rejectedResolution(srcRejectedReason, srcRejectedException),
                thread.executor,
                syncLogger,
                loggerWithoutExecutorMock,
                loggerWithExecutorMock,
                promiseMatcher("MUTABLE", PromiseState.FULFILLED, 123, null, null),
                promiseMatcher("MUTABLE", PromiseState.REJECTED, null, srcRejectedReason, srcRejectedExceptionClass),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.REJECTED, null, srcRejectedReason, srcRejectedExceptionClass),
                promiseMatcher("MUTABLE", PromiseState.REJECTED, null, 'D', Exception.class),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("REJECTED", PromiseState.REJECTED, null, srcRejectedReason, srcRejectedExceptionClass),
                promiseMatcher(null, PromiseState.FULFILLED, -456, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("REJECTED", PromiseState.REJECTED, null, srcRejectedReason, srcRejectedExceptionClass),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                resolveActionMock2,
                resolveActionMock5,
                resolveActionMock8,
                resolveActionMock11,
                resolveActionMock14,
                resolveActionMock17
            );

            thread.executor.shutdown();

            final Object expectedReason = translateReason(srcRejectedReason);

            new VerificationsInOrder() {{
                syncLogger.log("syncWithExecutor", "1.before");
                loggerWithExecutorMock.onRejected(0, thread, expectedReason, srcRejectedException);
                loggerWithExecutorMock.onRejected(3, thread, expectedReason, srcRejectedException);
                syncLogger.log("syncWithExecutor", "1.after");
                loggerWithExecutorMock.onRejected(9, thread, expectedReason, srcRejectedException);
                loggerWithExecutorMock.onRejected(12, thread, expectedReason, srcRejectedException);
                syncLogger.log("syncWithExecutor", "2.after");
            }};

            new FullVerificationsInOrder() {{
                syncLogger.log("separate", 0, 1);
                syncLogger.log("separate", 1, 2);
                syncLogger.log("separate", 2, 3);
                syncLogger.log("separate", 3, 4);
                syncLogger.log("separate", 4, 5);
                syncLogger.log("separate", 5, 6);
                syncLogger.log("separate", 6, 7);
                syncLogger.log("separate", 7, 8);
                syncLogger.log("syncWithoutExecutor", "1.before");
                resolveActionMock2.setRejected(expectedReason, srcRejectedException);
                resolveActionMock5.setRejected(expectedReason, srcRejectedException);
                resolveActionMock8.setRejected(expectedReason, srcRejectedException);
                loggerWithoutExecutorMock.onRejected(4, any, expectedReason, srcRejectedException);
                loggerWithoutExecutorMock.onRejected(7, any, expectedReason, srcRejectedException);
                syncLogger.log("syncWithoutExecutor", "1.after");
                syncLogger.log("separate", 9, 10);
                syncLogger.log("separate", 10, 11);
                resolveActionMock11.setRejected(expectedReason, srcRejectedException);
                syncLogger.log("separate", 11, 12);
                syncLogger.log("separate", 12, 13);
                loggerWithoutExecutorMock.onRejected(13, any, expectedReason, srcRejectedException);
                syncLogger.log("separate", 13, 14);
                resolveActionMock14.setRejected(expectedReason, srcRejectedException);
                syncLogger.log("separate", 14, 15);
                syncLogger.log("separate", 15, 16);
                loggerWithoutExecutorMock.onRejected(16, any, expectedReason, srcRejectedException);
                syncLogger.log("separate", 16, 17);
                resolveActionMock17.setRejected(expectedReason, srcRejectedException);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void withAlwaysPending(
            @Injectable final TestLogger syncLogger,
            @Injectable final CallbackLogger loggerWithoutExecutorMock,
            @Injectable final CallbackLogger loggerWithExecutorMock,
            @Injectable final ResolveAction resolveActionMock2,
            @Injectable final ResolveAction resolveActionMock5,
            @Injectable final ResolveAction resolveActionMock8,
            @Injectable final ResolveAction resolveActionMock11,
            @Injectable final ResolveAction resolveActionMock14,
            @Injectable final ResolveAction resolveActionMock17
        ) throws Throwable
        {
            final TestThread thread = new TestThread();

            doTest(
                alwaysPendingResolution(),
                thread.executor,
                syncLogger,
                loggerWithoutExecutorMock,
                loggerWithExecutorMock,
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("MUTABLE", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                promiseMatcher("ALWAYS-PENDING", PromiseState.PENDING, null, null, null),
                resolveActionMock2,
                resolveActionMock5,
                resolveActionMock8,
                resolveActionMock11,
                resolveActionMock14,
                resolveActionMock17
            );

            thread.executor.shutdown();

            new VerificationsInOrder() {{
                syncLogger.log("syncWithExecutor", "1.before");
                syncLogger.log("syncWithExecutor", "1.after");
                syncLogger.log("syncWithExecutor", "2.after");
            }};

            new FullVerificationsInOrder() {{
                syncLogger.log("separate", 0, 1);
                syncLogger.log("separate", 1, 2);
                syncLogger.log("separate", 2, 3);
                syncLogger.log("separate", 3, 4);
                syncLogger.log("separate", 4, 5);
                syncLogger.log("separate", 5, 6);
                syncLogger.log("separate", 6, 7);
                syncLogger.log("separate", 7, 8);
                syncLogger.log("syncWithoutExecutor", "1.before");
                resolveActionMock2.setAlwaysPending();
                resolveActionMock5.setAlwaysPending();
                resolveActionMock8.setAlwaysPending();
                syncLogger.log("syncWithoutExecutor", "1.after");
                syncLogger.log("separate", 9, 10);
                syncLogger.log("separate", 10, 11);
                resolveActionMock11.setAlwaysPending();
                syncLogger.log("separate", 11, 12);
                syncLogger.log("separate", 12, 13);
                syncLogger.log("separate", 13, 14);
                resolveActionMock14.setAlwaysPending();
                syncLogger.log("separate", 14, 15);
                syncLogger.log("separate", 15, 16);
                syncLogger.log("separate", 16, 17);
                resolveActionMock17.setAlwaysPending();
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    public class BaseChains extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Test
        public final void testPromiseChain() throws Exception
        {
            final ExecutorService exec1 = Executors.newSingleThreadExecutor();
            final ExecutorService exec2 = Executors.newSingleThreadExecutor();

            final P promise1 = doThen(
                promiseFactory().originPromise(),
                null,
                new FR1<Object, R>() { @Override public R call(final Object value) {
                    callbackMock.onFulfilled("1.onFulfilled", Thread.currentThread(), value);

                    final P promise1_1 = doThen(
                        promiseFactory().originPromise(),
                        exec1,
                        new FR1<Object, R>() { @Override public R call(final Object value) {
                            callbackMock.onFulfilled("1_1.onFulfilled", Thread.currentThread(), value);
                            return fulfilledResolution("v.1_1");
                        }},
                        unusedOnRejected
                    );

                    final P promise1_2 = doThen(promise1_1, exec2, null, unusedOnRejected);

                    return doThen(
                        promise1_2,
                        null,
                        new FR1<Object, R>() { @Override public R call(final Object value) {
                            callbackMock.onFulfilled("1_3.onFulfilled", Thread.currentThread(), value);

                            return doThen(
                                promiseFactory().originPromise(),
                                exec1,
                                new FR1<Object, R>() { @Override public R call(final Object value) {
                                    callbackMock.onFulfilled("1_3_1.onFulfilled", Thread.currentThread(), value);
                                    return fulfilledResolution("v.1_3_1");
                                }},
                                unusedOnRejected
                            );
                        }},
                        unusedOnRejected
                    );
                }},
                unusedOnRejected
            );

            final P promise2 = doThen(
                promise1,
                exec2,
                new FR1<Object, R>() { @Override public R call(final Object value) {
                    callbackMock.onFulfilled("2.onFulfilled", Thread.currentThread(), value);

                    final P promise2_1 = doThen(
                        promiseFactory().originPromise(),
                        null,
                        new FR1<Object, R>() { @Override public R call(final Object value) throws Exception {
                            callbackMock.onFulfilled("2_1.onFulfilled", Thread.currentThread(), value);
                            throw new Exception();
                        }},
                        unusedOnRejected
                    );

                    final P promise2_2 = doThen(promise2_1, exec1, unusedOnFulfilled, null);

                    return doThen(
                        promise2_2,
                        exec2,
                        unusedOnFulfilled,
                        new FR2<Object, Throwable, R>() {
                            @Override public R call(final Object reason, final Throwable exception) {
                                callbackMock.onRejected("2_3.onRejected", Thread.currentThread(), reason, exception);

                                return doThen(
                                    promiseFactory().originPromise(),
                                    null,
                                    new FR1<Object, R>() { @Override public R call(final Object value) {
                                        callbackMock.onFulfilled("2_3_1.onFulfilled", Thread.currentThread(), value);
                                        return rejectedResolution("r.2_3_1", new RuntimeException());
                                    }},
                                    unusedOnRejected
                                );
                            }
                        }
                    );
                }},
                unusedOnRejected
            );

            final P promise3 = doThen(
                promise2,
                exec1,
                unusedOnFulfilled,
                new FR2<Object, Throwable, R>() {
                    @Override public R call(final Object reason, final Throwable exception) {
                        callbackMock.onRejected("3.onRejected", Thread.currentThread(), reason, exception);

                        final P promise3_1 = doThen(
                            promiseFactory().originPromise(),
                            exec2,
                            new FR1<Object, R>() { @Override public R call(final Object value) {
                                callbackMock.onFulfilled("3_1.onFulfilled", Thread.currentThread(), value);
                                return promiseFactory().fulfilledPromise("v.3_1");
                            }},
                            unusedOnRejected
                        );

                        final P promise3_2 = doThen(promise3_1, null, null, unusedOnRejected);

                        return doThen(
                            promise3_2,
                            exec1,
                            new FR1<Object, R>() { @Override public R call(final Object value) {
                                callbackMock.onFulfilled("3_3.onFulfilled", Thread.currentThread(), value);

                                final P promise3_3_1 = doThen(
                                    promiseFactory().originPromise(),
                                    exec2,
                                    new FR1<Object, R>() { @Override public R call(final Object value) {
                                        callbackMock.onFulfilled("3_3_1.onFulfilled", Thread.currentThread(), value);
                                        return rejectedResolution("r.3_3_1", null);
                                    }},
                                    unusedOnRejected
                                );

                                return doThen(
                                    promise3_3_1,
                                    null,
                                    unusedOnFulfilled,
                                    new FR2<Object, Throwable, R>() {
                                        @Override public R call(final Object reason, final Throwable exception) {
                                            callbackMock.onRejected(
                                                "3_3_2.onRejected", Thread.currentThread(),
                                                reason, exception
                                            );

                                            return rejectedResolution(null, new NullPointerException());
                                        }
                                    }

                                );
                            }},
                            unusedOnRejected
                        );
                    }
                }
            );

            final P promise4 = doThen(
                promise3,
                exec1,
                unusedOnFulfilled,
                new FR2<Object, Throwable, R>() {
                    @Override public R call(final Object reason, final Throwable exception) {
                        callbackMock.onRejected("4.onRejected", Thread.currentThread(), reason, exception);

                        final P promise4_1 = doThen(
                            promiseFactory().originPromise(),
                            exec2,
                            new FR1<Object, R>() { @Override public R call(final Object value) {
                                callbackMock.onFulfilled("4_1.onFulfilled", Thread.currentThread(), value);
                                throw new ClassCastException();
                            }},
                            unusedOnRejected
                        );

                        final P promise4_2 = doThen(promise4_1, null, unusedOnFulfilled, null);

                        return doThen(
                            promise4_2,
                            exec1,
                            unusedOnFulfilled,
                            new FR2<Object, Throwable, R>() {
                                @Override public R call(final Object reason, final Throwable exception) {
                                    callbackMock.onRejected(
                                        "4_3.onRejected", Thread.currentThread(),
                                        reason, exception
                                    );

                                    final P promise4_3_1 = doThen(
                                        promiseFactory().originPromise(),
                                        exec2,
                                        new FR1<Object, R>() { @Override public R call(final Object value) {
                                            callbackMock.onFulfilled(
                                                "4_3_1.onFulfilled", Thread.currentThread(),
                                                value
                                            );

                                            return rejectedResolution("r.4_3_1", new IllegalArgumentException());
                                        }},
                                        unusedOnRejected
                                    );

                                    return doThen(
                                        promise4_3_1,
                                        null,
                                        unusedOnFulfilled,
                                        new FR2<Object, Throwable, R>() {
                                            @Override public R call(final Object reason, final Throwable exception) {
                                                callbackMock.onRejected(
                                                    "4_3_2.onRejected", Thread.currentThread(),
                                                    reason, exception
                                                );

                                                return fulfilledResolution("v.4_3_2");
                                            }
                                        }
                                    );
                                }
                            }
                        );
                    }
                }
            );

            assertEquals("v.4_3_2", await(promise4, 1, TimeUnit.SECONDS));
            assertThat(promise4, promiseMatcher(null, PromiseState.FULFILLED, "v.4_3_2", null, null));

            exec1.shutdown();
            exec2.shutdown();

            new FullVerificationsInOrder() {{
                callbackMock.onFulfilled("1.onFulfilled", any, null);
                callbackMock.onFulfilled("1_1.onFulfilled", any, null);
                callbackMock.onFulfilled("1_3.onFulfilled", any, "v.1_1");
                callbackMock.onFulfilled("1_3_1.onFulfilled", any, null);
                callbackMock.onFulfilled("2.onFulfilled", any, "v.1_3_1");
                callbackMock.onFulfilled("2_1.onFulfilled", any, null);
                callbackMock.onRejected("2_3.onRejected", any, null, withArgThat(TestUtil.ofClass(Exception.class)));
                callbackMock.onFulfilled("2_3_1.onFulfilled", any, null);
                callbackMock.onRejected("3.onRejected", any, translateReason("r.2_3_1"), withArgThat(TestUtil.ofClass(RuntimeException.class)));
                callbackMock.onFulfilled("3_1.onFulfilled", any, null);
                callbackMock.onFulfilled("3_3.onFulfilled", any, "v.3_1");
                callbackMock.onFulfilled("3_3_1.onFulfilled", any, null);
                callbackMock.onRejected("3_3_2.onRejected", any, translateReason("r.3_3_1"), null);
                callbackMock.onRejected("4.onRejected", any, null, withArgThat(TestUtil.ofClass(NullPointerException.class)));
                callbackMock.onFulfilled("4_1.onFulfilled", any, null);
                callbackMock.onRejected("4_3.onRejected", any, null, withArgThat(TestUtil.ofClass(ClassCastException.class)));
                callbackMock.onFulfilled("4_3_1.onFulfilled", any, null);
                callbackMock.onRejected("4_3_2.onRejected", any, translateReason("r.4_3_1"), withArgThat(TestUtil.ofClass(IllegalArgumentException.class)));
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
