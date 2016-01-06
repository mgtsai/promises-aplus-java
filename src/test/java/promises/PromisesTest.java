//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import mockit.FullVerificationsInOrder;
import mockit.Injectable;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static promises.Promises.async;
import static promises.Promises.pf;
import static promises.Promises.pn;
import static promises.Promises.pr;
import static promises.Promises.pt;
import static promises.Promises.v;
import static promises.Promises.wf;
import static promises.Promises.wr;
import static promises.Thenable.thenable;
import static promises.Thenable.thenable_1_2;
import static promises.Thenable.thenable_1_3;
import static promises.Thenable.thenable_1_4;
import static promises.Thenable.thenable_1_5;
import static promises.Thenable.thenable_2_1;
import static promises.Thenable.thenable_2_2;
import static promises.Thenable.thenable_2_3;
import static promises.Thenable.thenable_2_4;
import static promises.Thenable.thenable_2_5;
import static promises.Thenable.thenable_3_1;
import static promises.Thenable.thenable_3_2;
import static promises.Thenable.thenable_3_3;
import static promises.Thenable.thenable_3_4;
import static promises.Thenable.thenable_3_5;
import static promises.Thenable.thenable_4_1;
import static promises.Thenable.thenable_4_2;
import static promises.Thenable.thenable_4_3;
import static promises.Thenable.thenable_4_4;
import static promises.Thenable.thenable_4_5;
import static promises.Thenable.thenable_5_1;
import static promises.Thenable.thenable_5_2;
import static promises.Thenable.thenable_5_3;
import static promises.Thenable.thenable_5_4;
import static promises.Thenable.thenable_5_5;
//---------------------------------------------------------------------------------------------------------------------
public final class PromisesTest
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Thread nullThread = null;
    private static final ResolvePromise nResP1 = null;
    private static final ResolvePromise2 nResP2 = null;
    private static final ResolvePromise3 nResP3 = null;
    private static final ResolvePromise4 nResP4 = null;
    private static final ResolvePromise5 nResP5 = null;
    private static final RejectPromise nRejP1 = null;
    private static final RejectPromise2 nRejP2 = null;
    private static final RejectPromise3 nRejP3 = null;
    private static final RejectPromise4 nRejP4 = null;
    private static final RejectPromise5 nRejP5 = null;
    //-----------------------------------------------------------------------------------------------------------------
    private static Object[] l(final Object... args)
    {
        return args;
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static FR0<Object> supp(final Object item)
    {
        return new FR0<Object>() { @Override public Object call() {
            return item;
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static FR0<Object> suppThrow(final Throwable exception)
    {
        return new FR0<Object>() { @Override public Object call() throws Throwable {
            throw exception;
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Thenable thenableResolve(final Object resolution)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resolvePromise, final RejectPromise rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static Thenable thenableReject(final Object rejectedReason, final Throwable rejectedException)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resolvePromise, final RejectPromise rejectPromise) {
                rejectPromise.reject(rejectedReason, rejectedException);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise, RJ> thenResolve(final RJ dummy, final Object resolution)
    {
        return new F2<ResolvePromise, RJ>() {
            @Override public void call(final ResolvePromise resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise2, RJ> thenResolve2(final RJ dummy, final Object resolution)
    {
        return new F2<ResolvePromise2, RJ>() {
            @Override public void call(final ResolvePromise2 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise2, RJ> thenResolve2(final RJ dummy, final Object v1, final Object v2)
    {
        return new F2<ResolvePromise2, RJ>() {
            @Override public void call(final ResolvePromise2 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(v1, v2);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise3, RJ> thenResolve3(final RJ dummy, final Object resolution)
    {
        return new F2<ResolvePromise3, RJ>() {
            @Override public void call(final ResolvePromise3 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise3, RJ>
    thenResolve3(final RJ dummy, final Object v1, final Object v2, final Object v3)
    {
        return new F2<ResolvePromise3, RJ>() {
            @Override public void call(final ResolvePromise3 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(v1, v2, v3);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise4, RJ> thenResolve4(final RJ dummy, final Object resolution)
    {
        return new F2<ResolvePromise4, RJ>() {
            @Override public void call(final ResolvePromise4 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise4, RJ>
    thenResolve4(final RJ dummy, final Object v1, final Object v2, final Object v3, final Object v4)
    {
        return new F2<ResolvePromise4, RJ>() {
            @Override public void call(final ResolvePromise4 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(v1, v2, v3, v4);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise5, RJ> thenResolve5(final RJ dummy, final Object resolution)
    {
        return new F2<ResolvePromise5, RJ>() {
            @Override public void call(final ResolvePromise5 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(resolution);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RJ> F2<ResolvePromise5, RJ>
    thenResolve5(final RJ dummy, final Object v1, final Object v2, final Object v3, final Object v4, final Object v5)
    {
        return new F2<ResolvePromise5, RJ>() {
            @Override public void call(final ResolvePromise5 resolvePromise, final RJ rejectPromise) {
                resolvePromise.resolve(v1, v2, v3, v4, v5);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise> thenReject(final RS dummy, final Object r, final Throwable e)
    {
        return new F2<RS, RejectPromise>() {
            @Override public void call(final RS resolvePromise, final RejectPromise rejectPromise) {
                rejectPromise.reject(r, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise> thenReject(final RS dummy, final Object r)
    {
        return new F2<RS, RejectPromise>() {
            @Override public void call(final RS resolvePromise, final RejectPromise rejectPromise) {
                rejectPromise.reject(r);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise> thenReject(final RS dummy, final Throwable e)
    {
        return new F2<RS, RejectPromise>() {
            @Override public void call(final RS resolvePromise, final RejectPromise rejectPromise) {
                rejectPromise.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise2>
    thenReject2(final RS dummy, final Object r1, final Object r2, final Throwable e)
    {
        return new F2<RS, RejectPromise2>() {
            @Override public void call(final RS resolvePromise, final RejectPromise2 rejectPromise) {
                rejectPromise.reject(r1, r2, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise2> thenReject2(final RS dummy, final Object r1, final Object r2)
    {
        return new F2<RS, RejectPromise2>() {
            @Override public void call(final RS resolvePromise, final RejectPromise2 rejectPromise) {
                rejectPromise.reject(r1, r2);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise2> thenReject2(final RS dummy, final Throwable e)
    {
        return new F2<RS, RejectPromise2>() {
            @Override public void call(final RS resolvePromise, final RejectPromise2 rejectPromise) {
                rejectPromise.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise3>
    thenReject3(final RS dummy, final Object r1, final Object r2, final Object r3, final Throwable e)
    {
        return new F2<RS, RejectPromise3>() {
            @Override public void call(final RS resolvePromise, final RejectPromise3 rejectPromise) {
                rejectPromise.reject(r1, r2, r3, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise3>
    thenReject3(final RS dummy, final Object r1, final Object r2, final Object r3)
    {
        return new F2<RS, RejectPromise3>() {
            @Override public void call(final RS resolvePromise, final RejectPromise3 rejectPromise) {
                rejectPromise.reject(r1, r2, r3);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise3> thenReject3(final RS dummy, final Throwable e)
    {
        return new F2<RS, RejectPromise3>() {
            @Override public void call(final RS resolvePromise, final RejectPromise3 rejectPromise) {
                rejectPromise.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise4>
    thenReject4(final RS dummy, final Object r1, final Object r2, final Object r3, final Object r4, final Throwable e)
    {
        return new F2<RS, RejectPromise4>() {
            @Override public void call(final RS resolvePromise, final RejectPromise4 rejectPromise) {
                rejectPromise.reject(r1, r2, r3, r4, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise4>
    thenReject4(final RS dummy, final Object r1, final Object r2, final Object r3, final Object r4)
    {
        return new F2<RS, RejectPromise4>() {
            @Override public void call(final RS resolvePromise, final RejectPromise4 rejectPromise) {
                rejectPromise.reject(r1, r2, r3, r4);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise4> thenReject4(final RS dummy, final Throwable e)
    {
        return new F2<RS, RejectPromise4>() {
            @Override public void call(final RS resolvePromise, final RejectPromise4 rejectPromise) {
                rejectPromise.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise5> thenReject5(
        final RS dummy,
        final Object r1,
        final Object r2,
        final Object r3,
        final Object r4,
        final Object r5,
        final Throwable e
    ) {
        return new F2<RS, RejectPromise5>() {
            @Override public void call(final RS resolvePromise, final RejectPromise5 rejectPromise) {
                rejectPromise.reject(r1, r2, r3, r4, r5, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise5>
    thenReject5(final RS dummy, final Object r1, final Object r2, final Object r3, final Object r4, final Object r5)
    {
        return new F2<RS, RejectPromise5>() {
            @Override public void call(final RS resolvePromise, final RejectPromise5 rejectPromise) {
                rejectPromise.reject(r1, r2, r3, r4, r5);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS> F2<RS, RejectPromise5> thenReject5(final RS dummy, final Throwable e)
    {
        return new F2<RS, RejectPromise5>() {
            @Override public void call(final RS resolvePromise, final RejectPromise5 rejectPromise) {
                rejectPromise.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <RS, RJ> F2<RS, RJ> thenThrow(final RS dummyResP, final RJ dummyRejP, final Throwable e)
    {
        return new F2<RS, RJ>() {
            @Override public void call(final RS resolvePromise, final RJ rejectPromise) throws Throwable {
                throw e;
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseSupplier suppPromise(final Object id, final Promise promise)
    {
        return new PromiseSupplier() {
            @Override Promise get(final TestLogger logger, final FR0<Object> suppResolution) {
                logger.log(id);
                return promise;
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseSupplier suppPromiseByPt(final Object id, final Thenable thenable)
    {
        return new PromiseSupplier() {
            @Override Promise get(final TestLogger logger, FR0<Object> suppResolution) {
                final ExecutorService exec = Executors.newSingleThreadExecutor();
                final TestStep step = new TestStep();

                try {
                    return pt(
                        exec,
                        new Thenable() {
                            @Override public void
                            then(final ResolvePromise resolvePromise, final RejectPromise rejectPromise)
                                throws Throwable
                            {
                                logger.log(id);
                                thenable.then(resolvePromise, rejectPromise);
                                step.finish();
                            }
                        }
                    );
                } finally {
                    step.sync();
                    exec.shutdown();
                }
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseSupplier suppPromiseByAsync(final Object id, final FR0<Object> supp)
    {
        return new PromiseSupplier() {
            @Override Promise get(final TestLogger logger, FR0<Object> suppResolution) {
                final ExecutorService exec = Executors.newSingleThreadExecutor();
                final TestStep step = new TestStep();

                try {
                    return async(
                        exec,
                        new FR0<Object>() { @Override public Object call() throws Throwable {
                            exec.execute(new Runnable() { @Override public void run() {
                                step.finish();
                            }});

                            logger.log(id);
                            return supp.call();
                        }}
                    );
                } finally {
                    step.sync();
                    exec.shutdown();
                }
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseSupplier suppPromiseFromOnFulfilled(final Object id, final Object fulfilledValue)
    {
        return new PromiseSupplier() {
            @Override Promise get(final TestLogger logger, final FR0<Object> suppResolution) {
                return pf(fulfilledValue)
                .then(new OnFulfilled<Object>() { @Override public Object call(final Object v) throws Throwable {
                    logger.log(id, v);
                    return suppResolution.call();
                }});
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static PromiseSupplier
    suppPromiseFromOnRejected(final Object id, final Object rejectedReason, final Throwable rejectedException)
    {
        return new PromiseSupplier() {
            @Override Promise get(final TestLogger logger, final FR0<Object> suppResolution) {
                return pr(rejectedReason, rejectedException)
                .then(
                    null,
                    new OnRejected<Object>() {
                        @Override public Object call(final Object r, final Throwable e) throws Throwable {
                            logger.log(id, r, e.getClass());
                            return suppResolution.call();
                        }
                    }
                );
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private interface CallbackLogger
    {
        void onFulfilled(final Object id, final Thread thread, final Object... values);
        void onRejected(final Object id, final Thread thread, final Object exception, final Object... reasons);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static abstract class PromiseSupplier
    {
        abstract Promise get(final TestLogger logger, final FR0<Object> suppResolution);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static class Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @Injectable TestLogger loggerMock = null;
        @Injectable CallbackLogger callbackLoggerMock = null;
        //-------------------------------------------------------------------------------------------------------------
        static Object[][] paramsPromiseSupplier()
        {
            return new Object[][] {{
                suppPromiseFromOnFulfilled("onFulfilled", "fulfilled"),
                l("onFulfilled", "fulfilled")
            }, {
                suppPromiseFromOnRejected("onRejected", "rejected", new RuntimeException()),
                l("onRejected", "rejected", RuntimeException.class)
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class FulfilledThenChain extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsIntegerValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                    l("p1"), null, null},
                {suppPromise("p2", pf(null)),                l("p2"), null, null},
                {suppPromise("p3", pf(1)),                   l("p3"), null, 1},
                {suppPromise("p4", pf(-2)),                  l("p4"), null, -2},
                {suppPromise("p5", pt(thenableResolve(3))),  l("p5"), null, 3},
                {suppPromiseByPt("p6", thenableResolve(-4)), l("p6"), null, -4},
                {suppPromiseByAsync("p7", supp(5)),          l("p7"), null, 5},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                    null},
                    {supp(-6),                                                      -6},
                    {supp(v(null)),                                                 null},
                    {supp(v(7)),                                                    7},
                    {supp(pn()),                                                    null},
                    {supp(pf(null)),                                                null},
                    {supp(pf(-8)),                                                  -8},
                    {supp(thenable(thenResolve(nRejP1, null))),                     null},
                    {supp(thenable(thenResolve(nRejP1, 9))),                        9},
                    {supp(thenable(thenResolve(nRejP1, v(-10)))),                   -10},
                    {supp(thenable(thenResolve(nRejP1, pf(11)))),                   11},
                    {supp(thenable(thenResolve(nRejP1, thenableResolve(-12)))),     -12},
                    {supp(thenable_1_2(thenResolve(nRejP2, null))),                 null},
                    {supp(thenable_1_2(thenResolve(nRejP2, 13))),                   13},
                    {supp(thenable_1_2(thenResolve(nRejP2, v(-14)))),               -14},
                    {supp(thenable_1_2(thenResolve(nRejP2, pf(15)))),               15},
                    {supp(thenable_1_2(thenResolve(nRejP2, thenableResolve(-16)))), -16},
                    {supp(thenable_1_3(thenResolve(nRejP3, null))),                 null},
                    {supp(thenable_1_3(thenResolve(nRejP3, 17))),                   17},
                    {supp(thenable_1_3(thenResolve(nRejP3, v(-18)))),               -18},
                    {supp(thenable_1_3(thenResolve(nRejP3, pf(19)))),               19},
                    {supp(thenable_1_3(thenResolve(nRejP3, thenableResolve(-20)))), -20},
                    {supp(thenable_1_4(thenResolve(nRejP4, null))),                 null},
                    {supp(thenable_1_4(thenResolve(nRejP4, 21))),                   21},
                    {supp(thenable_1_4(thenResolve(nRejP4, v(-22)))),               -22},
                    {supp(thenable_1_4(thenResolve(nRejP4, pf(23)))),               23},
                    {supp(thenable_1_4(thenResolve(nRejP4, thenableResolve(-24)))), -24},
                    {supp(thenable_1_5(thenResolve(nRejP5, null))),                 null},
                    {supp(thenable_1_5(thenResolve(nRejP5, 25))),                   25},
                    {supp(thenable_1_5(thenResolve(nRejP5, v(-26)))),               -26},
                    {supp(thenable_1_5(thenResolve(nRejP5, pf(27)))),               27},
                    {supp(thenable_1_5(thenResolve(nRejP5, thenableResolve(-28)))), -28},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsIntegerValueCall")
        public final void integerValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedValue
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(new OnFulfilled<Integer>() { @Override public Object call(final Integer v) {
                callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v);
                return null;
            }});

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);
                callbackLoggerMock.onFulfilled(1, withAny(nullThread), expectedValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsStringValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                       l("p1"), null, null},
                {suppPromise("p2", pf(null)),                   l("p2"), null, null},
                {suppPromise("p3", pf("abc")),                  l("p3"), null, "abc"},
                {suppPromise("p4", pf("DEF")),                  l("p4"), null, "DEF"},
                {suppPromise("p5", pt(thenableResolve("ghi"))), l("p5"), null, "ghi"},
                {suppPromiseByPt("p6", thenableResolve("JKL")), l("p6"), null, "JKL"},
                {suppPromiseByAsync("p7", supp("mno")),         l("p7"), null, "mno"},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                     null},
                    {supp("PQR"),                                                    "PQR"},
                    {supp(v(null)),                                                  null},
                    {supp(v("stu")),                                                 "stu"},
                    {supp(pn()),                                                     null},
                    {supp(pf(null)),                                                 null},
                    {supp(pf("XYZ")),                                                "XYZ"},
                    {supp(thenable(thenResolve(nRejP1, null))),                      null},
                    {supp(thenable(thenResolve(nRejP1, "123"))),                     "123"},
                    {supp(thenable(thenResolve(nRejP1, v("456")))),                  "456"},
                    {supp(thenable(thenResolve(nRejP1, pf("789")))),                 "789"},
                    {supp(thenable(thenResolve(nRejP1, thenableResolve("AB")))),     "AB"},
                    {supp(thenable_1_2(thenResolve(nRejP2, null))),                  null},
                    {supp(thenable_1_2(thenResolve(nRejP2, "cd"))),                  "cd"},
                    {supp(thenable_1_2(thenResolve(nRejP2, v("EF")))),               "EF"},
                    {supp(thenable_1_2(thenResolve(nRejP2, pf("gh")))),              "gh"},
                    {supp(thenable_1_2(thenResolve(nRejP2, thenableResolve("IJ")))), "IJ"},
                    {supp(thenable_1_3(thenResolve(nRejP3, null))),                  null},
                    {supp(thenable_1_3(thenResolve(nRejP3, "kl"))),                  "kl"},
                    {supp(thenable_1_3(thenResolve(nRejP3, v("MN")))),               "MN"},
                    {supp(thenable_1_3(thenResolve(nRejP3, pf("op")))),              "op"},
                    {supp(thenable_1_3(thenResolve(nRejP3, thenableResolve("QR")))), "QR"},
                    {supp(thenable_1_4(thenResolve(nRejP4, null))),                  null},
                    {supp(thenable_1_4(thenResolve(nRejP4, "st"))),                  "st"},
                    {supp(thenable_1_4(thenResolve(nRejP4, v("UV")))),               "UV"},
                    {supp(thenable_1_4(thenResolve(nRejP4, pf("wx")))),              "wx"},
                    {supp(thenable_1_4(thenResolve(nRejP4, thenableResolve("YZ")))), "YZ"},
                    {supp(thenable_1_5(thenResolve(nRejP5, null))),                  null},
                    {supp(thenable_1_5(thenResolve(nRejP5, "12"))),                  "12"},
                    {supp(thenable_1_5(thenResolve(nRejP5, v("34")))),               "34"},
                    {supp(thenable_1_5(thenResolve(nRejP5, pf("56")))),              "56"},
                    {supp(thenable_1_5(thenResolve(nRejP5, thenableResolve("78")))), "78"},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsStringValueCall")
        public final void stringValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final String expectedValue
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(new OnFulfilled<String>() { @Override public Object call(final String v) {
                callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v);
                return null;
            }});

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);
                callbackLoggerMock.onFulfilled(1, withAny(nullThread), expectedValue);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsDualValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                              l("p1"), null, null, null},
                {suppPromise("p2", pf(null)),                          l("p2"), null, null, null},
                {suppPromise("p3", pf(M2.of(1, "abc"))),               l("p3"), null, 1,    "abc"},
                {suppPromise("p4", pf(-2, "DEF")),                     l("p4"), null, -2,   "DEF"},
                {suppPromise("p5", pt(thenableResolve(v(3, "ghi")))),  l("p5"), null, 3,    "ghi"},
                {suppPromiseByPt("p6", thenableResolve(v(-4, "JKL"))), l("p6"), null, -4,   "JKL"},
                {suppPromiseByAsync("p7", supp(v(5, "mno"))),          l("p7"), null, 5,    "mno"},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                                  null, null},
                    {supp(v(null)),                                                               null, null},
                    {supp(v(-6, "PQR")),                                                          -6,   "PQR"},
                    {supp(pn()),                                                                  null, null},
                    {supp(pf(null)),                                                              null, null},
                    {supp(pf(M2.of(7, "stu"))),                                                   7,    "stu"},
                    {supp(pf(-8, "XYZ")),                                                         -8,   "XYZ"},
                    {supp(thenable_2_1(thenResolve2(nRejP1, null))),                              null, null},
                    {supp(thenable_2_1(thenResolve2(nRejP1, v(9, "123")))),                       9,    "123"},
                    {supp(thenable_2_1(thenResolve2(nRejP1, pf(-10, "456")))),                    -10,  "456"},
                    {supp(thenable_2_1(thenResolve2(nRejP1, thenableResolve(M2.of(11, "789"))))), 11,   "789"},
                    {supp(thenable_2_1(thenResolve2(nRejP1, -12, "AB"))),                         -12,  "AB"},
                    {supp(thenable_2_2(thenResolve2(nRejP2, null))),                              null, null},
                    {supp(thenable_2_2(thenResolve2(nRejP2, v(13, "cd")))),                       13,   "cd"},
                    {supp(thenable_2_2(thenResolve2(nRejP2, pf(-14, "EF")))),                     -14,  "EF"},
                    {supp(thenable_2_2(thenResolve2(nRejP2, thenableResolve(M2.of(15, "gh"))))),  15,   "gh"},
                    {supp(thenable_2_2(thenResolve2(nRejP2, -16, "IJ"))),                         -16,  "IJ"},
                    {supp(thenable_2_3(thenResolve2(nRejP3, null))),                              null, null},
                    {supp(thenable_2_3(thenResolve2(nRejP3, v(17, "kl")))),                       17,   "kl"},
                    {supp(thenable_2_3(thenResolve2(nRejP3, pf(-18, "MN")))),                     -18,  "MN"},
                    {supp(thenable_2_3(thenResolve2(nRejP3, thenableResolve(M2.of(19, "op"))))),  19,   "op"},
                    {supp(thenable_2_3(thenResolve2(nRejP3, -20, "QR"))),                         -20,  "QR"},
                    {supp(thenable_2_4(thenResolve2(nRejP4, null))),                              null, null},
                    {supp(thenable_2_4(thenResolve2(nRejP4, v(21, "st")))),                       21,   "st"},
                    {supp(thenable_2_4(thenResolve2(nRejP4, pf(-22, "UV")))),                     -22,  "UV"},
                    {supp(thenable_2_4(thenResolve2(nRejP4, thenableResolve(M2.of(23, "wx"))))),  23,   "wx"},
                    {supp(thenable_2_4(thenResolve2(nRejP4, -24, "YZ"))),                         -24,  "YZ"},
                    {supp(thenable_2_5(thenResolve2(nRejP5, null))),                              null, null},
                    {supp(thenable_2_5(thenResolve2(nRejP5, v(25, "12")))),                       25,   "12"},
                    {supp(thenable_2_5(thenResolve2(nRejP5, pf(-26, "34")))),                     -26,  "34"},
                    {supp(thenable_2_5(thenResolve2(nRejP5, thenableResolve(M2.of(27, "56"))))),  27,   "56"},
                    {supp(thenable_2_5(thenResolve2(nRejP5, -28, "78"))),                         -28,  "78"},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsDualValueCall")
        public final void dualValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedValue1,
            final String expectedValue2
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled2<Integer, String>() {
                @Override public Object call(final Integer v1, final String v2) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2);
                    return null;
                }
            }));

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);
                callbackLoggerMock.onFulfilled(1, withAny(nullThread), expectedValue1, expectedValue2);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsTripleValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                                     l("p1"), null, null, null,  null},
                {suppPromise("p2", pf(null)),                                 l("p2"), null, null, null,  null},
                {suppPromise("p3", pf(M3.of(1, "abc", true))),                l("p3"), null, 1,    "abc", true},
                {suppPromise("p4", pf(-2, "DEF", false)),                     l("p4"), null, -2,   "DEF", false},
                {suppPromise("p5", pt(thenableResolve(v(3, "ghi", true)))),   l("p5"), null, 3,    "ghi", true},
                {suppPromiseByPt("p6", thenableResolve(v(-4, "JKL", false))), l("p6"), null, -4,   "JKL", false},
                {suppPromiseByAsync("p7", supp(v(5, "mno", true))),           l("p7"), null, 5,    "mno", true},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                                        null, null,  null},
                    {supp(v(null)),                                                                     null, null,  null},
                    {supp(v(-6, "PQR", false)),                                                         -6,   "PQR", false},
                    {supp(pn()),                                                                        null, null,  null},
                    {supp(pf(null)),                                                                    null, null,  null},
                    {supp(pf(M3.of(7, "stu", true))),                                                   7,    "stu", true},
                    {supp(pf(-8, "XYZ", false)),                                                        -8,   "XYZ", false},
                    {supp(thenable_3_1(thenResolve3(nRejP1, null))),                                    null, null,  null},
                    {supp(thenable_3_1(thenResolve3(nRejP1, v(9, "123", true)))),                       9,    "123", true},
                    {supp(thenable_3_1(thenResolve3(nRejP1, pf(-10, "456", false)))),                   -10,  "456", false},
                    {supp(thenable_3_1(thenResolve3(nRejP1, thenableResolve(M3.of(11, "789", true))))), 11,   "789", true},
                    {supp(thenable_3_1(thenResolve3(nRejP1, -12, "AB", false))),                        -12,  "AB",  false},
                    {supp(thenable_3_2(thenResolve3(nRejP2, null))),                                    null, null,  null},
                    {supp(thenable_3_2(thenResolve3(nRejP2, v(13, "cd", true)))),                       13,   "cd",  true},
                    {supp(thenable_3_2(thenResolve3(nRejP2, pf(-14, "EF", false)))),                    -14,  "EF",  false},
                    {supp(thenable_3_2(thenResolve3(nRejP2, thenableResolve(M3.of(15, "gh", true))))),  15,   "gh",  true},
                    {supp(thenable_3_2(thenResolve3(nRejP2, -16, "IJ", false))),                        -16,  "IJ",  false},
                    {supp(thenable_3_3(thenResolve3(nRejP3, null))),                                    null, null,  null},
                    {supp(thenable_3_3(thenResolve3(nRejP3, v(17, "kl", true)))),                       17,   "kl",  true},
                    {supp(thenable_3_3(thenResolve3(nRejP3, pf(-18, "MN", false)))),                    -18,  "MN",  false},
                    {supp(thenable_3_3(thenResolve3(nRejP3, thenableResolve(M3.of(19, "op", true))))),  19,   "op",  true},
                    {supp(thenable_3_3(thenResolve3(nRejP3, -20, "QR", false))),                        -20,  "QR",  false},
                    {supp(thenable_3_4(thenResolve3(nRejP4, null))),                                    null, null,  null},
                    {supp(thenable_3_4(thenResolve3(nRejP4, v(21, "st", true)))),                       21,   "st",  true},
                    {supp(thenable_3_4(thenResolve3(nRejP4, pf(-22, "UV", false)))),                    -22,  "UV",  false},
                    {supp(thenable_3_4(thenResolve3(nRejP4, thenableResolve(M3.of(23, "wx", true))))),  23,   "wx",  true},
                    {supp(thenable_3_4(thenResolve3(nRejP4, -24, "YZ", false))),                        -24,  "YZ",  false},
                    {supp(thenable_3_5(thenResolve3(nRejP5, null))),                                    null, null,  null},
                    {supp(thenable_3_5(thenResolve3(nRejP5, v(25, "12", true)))),                       25,   "12",  true},
                    {supp(thenable_3_5(thenResolve3(nRejP5, pf(-26, "34", false)))),                    -26,  "34",  false},
                    {supp(thenable_3_5(thenResolve3(nRejP5, thenableResolve(M3.of(27, "56", true))))),  27,   "56",  true},
                    {supp(thenable_3_5(thenResolve3(nRejP5, -28, "78", false))),                        -28,  "78",  false},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsTripleValueCall")
        public final void tripleValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedValue1,
            final String expectedValue2,
            final Boolean expectedValue3
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled3<Integer, String, Boolean>() {
                @Override public Object call(final Integer v1, final String v2, final Boolean v3) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3);
                    return null;
                }
            }));

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);
                callbackLoggerMock.onFulfilled(1, withAny(nullThread), expectedValue1, expectedValue2, expectedValue3);
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsQuadValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                                          l("p1"), null, null, null,  null,  null},
                {suppPromise("p2", pf(null)),                                      l("p2"), null, null, null,  null,  null},
                {suppPromise("p3", pf(M4.of(1, "abc", true, 'A'))),                l("p3"), null, 1,    "abc", true,  'A'},
                {suppPromise("p4", pf(-2, "DEF", false, 'b')),                     l("p4"), null, -2,   "DEF", false, 'b'},
                {suppPromise("p5", pt(thenableResolve(v(3, "ghi", true, 'C')))),   l("p5"), null, 3,    "ghi", true,  'C'},
                {suppPromiseByPt("p6", thenableResolve(v(-4, "JKL", false, 'd'))), l("p6"), null, -4,   "JKL", false, 'd'},
                {suppPromiseByAsync("p7", supp(v(5, "mno", true, 'E'))),           l("p7"), null, 5,    "mno", true,  'E'},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                                             null, null,  null,  null},
                    {supp(v(null)),                                                                          null, null,  null,  null},
                    {supp(v(-6, "PQR", false, 'f')),                                                         -6,   "PQR", false, 'f'},
                    {supp(pn()),                                                                             null, null,  null,  null},
                    {supp(pf(null)),                                                                         null, null,  null,  null},
                    {supp(pf(M4.of(7, "stu", true, 'G'))),                                                   7,    "stu", true,  'G'},
                    {supp(pf(-8, "XYZ", false, 'h')),                                                        -8,   "XYZ", false, 'h'},
                    {supp(thenable_4_1(thenResolve4(nRejP1, null))),                                         null, null,  null,  null},
                    {supp(thenable_4_1(thenResolve4(nRejP1, v(9, "123", true, 'I')))),                       9,    "123", true,  'I'},
                    {supp(thenable_4_1(thenResolve4(nRejP1, pf(-10, "456", false, 'j')))),                   -10,  "456", false, 'j'},
                    {supp(thenable_4_1(thenResolve4(nRejP1, thenableResolve(M4.of(11, "789", true, 'K'))))), 11,   "789", true,  'K'},
                    {supp(thenable_4_1(thenResolve4(nRejP1, -12, "AB", false, 'l'))),                        -12,  "AB",  false, 'l'},
                    {supp(thenable_4_2(thenResolve4(nRejP2, null))),                                         null, null,  null,  null},
                    {supp(thenable_4_2(thenResolve4(nRejP2, v(13, "cd", true, 'M')))),                       13,   "cd",  true,  'M'},
                    {supp(thenable_4_2(thenResolve4(nRejP2, pf(-14, "EF", false, 'n')))),                    -14,  "EF",  false, 'n'},
                    {supp(thenable_4_2(thenResolve4(nRejP2, thenableResolve(M4.of(15, "gh", true, 'O'))))),  15,   "gh",  true,  'O'},
                    {supp(thenable_4_2(thenResolve4(nRejP2, -16, "IJ", false, 'p'))),                        -16,  "IJ",  false, 'p'},
                    {supp(thenable_4_3(thenResolve4(nRejP3, null))),                                         null, null,  null,  null},
                    {supp(thenable_4_3(thenResolve4(nRejP3, v(17, "kl", true, 'Q')))),                       17,   "kl",  true,  'Q'},
                    {supp(thenable_4_3(thenResolve4(nRejP3, pf(-18, "MN", false, 'r')))),                    -18,  "MN",  false, 'r'},
                    {supp(thenable_4_3(thenResolve4(nRejP3, thenableResolve(M4.of(19, "op", true, 'S'))))),  19,   "op",  true,  'S'},
                    {supp(thenable_4_3(thenResolve4(nRejP3, -20, "QR", false, 't'))),                        -20,  "QR",  false, 't'},
                    {supp(thenable_4_4(thenResolve4(nRejP4, null))),                                         null, null,  null,  null},
                    {supp(thenable_4_4(thenResolve4(nRejP4, v(21, "st", true, 'U')))),                       21,   "st",  true,  'U'},
                    {supp(thenable_4_4(thenResolve4(nRejP4, pf(-22, "UV", false, 'v')))),                    -22,  "UV",  false, 'v'},
                    {supp(thenable_4_4(thenResolve4(nRejP4, thenableResolve(M4.of(23, "wx", true, 'W'))))),  23,   "wx",  true,  'W'},
                    {supp(thenable_4_4(thenResolve4(nRejP4, -24, "YZ", false, 'x'))),                        -24,  "YZ",  false, 'x'},
                    {supp(thenable_4_5(thenResolve4(nRejP5, null))),                                         null, null,  null,  null},
                    {supp(thenable_4_5(thenResolve4(nRejP5, v(25, "12", true, 'Y')))),                       25,   "12",  true,  'Y'},
                    {supp(thenable_4_5(thenResolve4(nRejP5, pf(-26, "34", false, 'z')))),                    -26,  "34",  false, 'z'},
                    {supp(thenable_4_5(thenResolve4(nRejP5, thenableResolve(M4.of(27, "56", true, '1'))))),  27,   "56",  true,  '1'},
                    {supp(thenable_4_5(thenResolve4(nRejP5, -28, "78", false, '2'))),                        -28,  "78",  false, '2'},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsQuadValueCall")
        public final void quadValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedValue1,
            final String expectedValue2,
            final Boolean expectedValue3,
            final Character expectedValue4
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled4<Integer, String, Boolean, Character>() {
                @Override public Object call(final Integer v1, final String v2, final Boolean v3, final Character v4) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3, v4);
                    return null;
                }
            }));

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onFulfilled(
                    1, withAny(nullThread),
                    expectedValue1, expectedValue2, expectedValue3, expectedValue4
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsPentaValueCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pn()),                                               l("p1"), null, null, null,  null,  null, null},
                {suppPromise("p2", pf(null)),                                           l("p2"), null, null, null,  null,  null, null},
                {suppPromise("p3", pf(M5.of(1, "abc", true, 'A', -1.2))),               l("p3"), null, 1,    "abc", true,  'A',  -1.2},
                {suppPromise("p4", pf(-2, "DEF", false, 'b', 23.)),                     l("p4"), null, -2,   "DEF", false, 'b',  23.},
                {suppPromise("p5", pt(thenableResolve(v(3, "ghi", true, 'C', -.34)))),  l("p5"), null, 3,    "ghi", true,  'C',  -.34},
                {suppPromiseByPt("p6", thenableResolve(v(-4, "JKL", false, 'd', 4.5))), l("p6"), null, -4,   "JKL", false, 'd',  4.5},
                {suppPromiseByAsync("p7", supp(v(5, "mno", true, 'E', -56.))),          l("p7"), null, 5,    "mno", true,  'E',  -56.},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(null),                                                                                   null, null,  null,  null, null},
                    {supp(v(null)),                                                                                null, null,  null,  null, null},
                    {supp(v(-6, "PQR", false, 'f', .67)),                                                          -6,   "PQR", false, 'f',  .67},
                    {supp(pn()),                                                                                   null, null,  null,  null, null},
                    {supp(pf(null)),                                                                               null, null,  null,  null, null},
                    {supp(pf(M5.of(7, "stu", true, 'G', -7.8))),                                                   7,    "stu", true,  'G',  -7.8},
                    {supp(pf(-8, "XYZ", false, 'h', 89.)),                                                         -8,   "XYZ", false, 'h',  89.},
                    {supp(thenable_5_1(thenResolve5(nRejP1, null))),                                               null, null,  null,  null, null},
                    {supp(thenable_5_1(thenResolve5(nRejP1, v(9, "123", true, 'I', -.13)))),                       9,    "123", true,  'I',  -.13},
                    {supp(thenable_5_1(thenResolve5(nRejP1, pf(-10, "456", false, 'j', 2.4)))),                    -10,  "456", false, 'j',  2.4},
                    {supp(thenable_5_1(thenResolve5(nRejP1, thenableResolve(M5.of(11, "789", true, 'K', -35.))))), 11,   "789", true,  'K',  -35.},
                    {supp(thenable_5_1(thenResolve5(nRejP1, -12, "AB", false, 'l', .46))),                         -12,  "AB",  false, 'l',  .46},
                    {supp(thenable_5_2(thenResolve5(nRejP2, null))),                                               null, null,  null,  null, null},
                    {supp(thenable_5_2(thenResolve5(nRejP2, v(13, "cd", true, 'M', -5.7)))),                       13,   "cd",  true,  'M',  -5.7},
                    {supp(thenable_5_2(thenResolve5(nRejP2, pf(-14, "EF", false, 'n', 68.)))),                     -14,  "EF",  false, 'n',  68.},
                    {supp(thenable_5_2(thenResolve5(nRejP2, thenableResolve(M5.of(15, "gh", true, 'O', -.79))))),  15,   "gh",  true,  'O',  -.79},
                    {supp(thenable_5_2(thenResolve5(nRejP2, -16, "IJ", false, 'p', 1.4))),                         -16,  "IJ",  false, 'p',  1.4},
                    {supp(thenable_5_3(thenResolve5(nRejP3, null))),                                               null, null,  null,  null, null},
                    {supp(thenable_5_3(thenResolve5(nRejP3, v(17, "kl", true, 'Q', -25.)))),                       17,   "kl",  true,  'Q',  -25.},
                    {supp(thenable_5_3(thenResolve5(nRejP3, pf(-18, "MN", false, 'r', .36)))),                     -18,  "MN",  false, 'r',  .36},
                    {supp(thenable_5_3(thenResolve5(nRejP3, thenableResolve(M5.of(19, "op", true, 'S', -4.7))))),  19,   "op",  true,  'S',  -4.7},
                    {supp(thenable_5_3(thenResolve5(nRejP3, -20, "QR", false, 't', 58.))),                         -20,  "QR",  false, 't',  58.},
                    {supp(thenable_5_4(thenResolve5(nRejP4, null))),                                               null, null,  null,  null, null},
                    {supp(thenable_5_4(thenResolve5(nRejP4, v(21, "st", true, 'U', -.69)))),                       21,   "st",  true,  'U',  -.69},
                    {supp(thenable_5_4(thenResolve5(nRejP4, pf(-22, "UV", false, 'v', 1.5)))),                     -22,  "UV",  false, 'v',  1.5},
                    {supp(thenable_5_4(thenResolve5(nRejP4, thenableResolve(M5.of(23, "wx", true, 'W', -26.))))),  23,   "wx",  true,  'W',  -26.},
                    {supp(thenable_5_4(thenResolve5(nRejP4, -24, "YZ", false, 'x', .37))),                         -24,  "YZ",  false, 'x',  .37},
                    {supp(thenable_5_5(thenResolve5(nRejP5, null))),                                               null, null,  null,  null, null},
                    {supp(thenable_5_5(thenResolve5(nRejP5, v(25, "12", true, 'Y', -4.8)))),                       25,   "12",  true,  'Y',  -4.8},
                    {supp(thenable_5_5(thenResolve5(nRejP5, pf(-26, "34", false, 'z', 59.)))),                     -26,  "34",  false, 'z',  59.},
                    {supp(thenable_5_5(thenResolve5(nRejP5, thenableResolve(M5.of(27, "56", true, '1', -.16))))),  27,   "56",  true,  '1',  -.16},
                    {supp(thenable_5_5(thenResolve5(nRejP5, -28, "78", false, '2', 2.7))),                         -28,  "78",  false, '2',  2.7},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsPentaValueCall")
        public final void pentaValueCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedValue1,
            final String expectedValue2,
            final Boolean expectedValue3,
            final Character expectedValue4,
            final Double expectedValue5
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled5<Integer, String, Boolean, Character, Double>() {
                @Override public Object
                call(final Integer v1, final String v2, final Boolean v3, final Character v4, final Double v5) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3, v4, v5);
                    return null;
                }
            }));

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onFulfilled(
                    1, withAny(nullThread),
                    expectedValue1, expectedValue2, expectedValue3, expectedValue4, expectedValue5
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsClassCastException() { return TestData.merge(
            paramsPromiseSupplier(),
            new Object[][]{
                {supp("DEF")},
                {supp(v(false))},
                {supp(v(true, "abc"))},
                {supp(v('g', "abc", true))},
                {supp(v(35., "abc", true, 'D'))},
                {supp(v(468L, "abc", true, 'D', -4.5))},
                {supp(pf('e'))},
                {supp(pf(123, 'F'))},
                {supp(pf(123, .13, true))},
                {supp(pf(123, 246L, true, 'D'))},
                {supp(pf(123, 5.7f, true, 'D', -4.5))},
                {supp(thenable(thenResolve(nRejP1, 56.)))},
                {supp(thenable_1_2(thenResolve(nRejP2, -234L)))},
                {supp(thenable_1_3(thenResolve(nRejP3, -.67f)))},
                {supp(thenable_1_4(thenResolve(nRejP4, (byte) 34)))},
                {supp(thenable_1_5(thenResolve(nRejP5, (short) -456)))},
                {supp(thenable_2_1(thenResolve2(nRejP1, 7.8, "abc")))},
                {supp(thenable_2_2(thenResolve2(nRejP2, 123, 567L)))},
                {supp(thenable_2_3(thenResolve2(nRejP3, -89.f, "abc")))},
                {supp(thenable_2_4(thenResolve2(nRejP4, 123, -678)))},
                {supp(thenable_2_5(thenResolve2(nRejP5, "ghi", "abc")))},
                {supp(thenable_3_1(thenResolve3(nRejP1, 123, "abc", 789L)))},
                {supp(thenable_3_2(thenResolve3(nRejP2, -2.4f, "abc", true)))},
                {supp(thenable_3_3(thenResolve3(nRejP3, 123, -135, true)))},
                {supp(thenable_3_4(thenResolve3(nRejP4, 123, "abc", "JKL")))},
                {supp(thenable_3_5(thenResolve3(nRejP5, false, "abc", true)))},
                {supp(thenable_4_1(thenResolve4(nRejP1, 123, "abc", -.46f, 'D')))},
                {supp(thenable_4_2(thenResolve4(nRejP2, 123, "abc", true, -357)))},
                {supp(thenable_4_3(thenResolve4(nRejP3, "mno", "abc", true, 'D')))},
                {supp(thenable_4_4(thenResolve4(nRejP4, 123, true, true, 'D')))},
                {supp(thenable_4_5(thenResolve4(nRejP5, 123, "abc", 'H', 'D')))},
                {supp(thenable_5_1(thenResolve5(nRejP1, 123, "abc", -579, 'D', -4.5)))},
                {supp(thenable_5_2(thenResolve5(nRejP2, 123, "abc", true, "PQR", -4.5)))},
                {supp(thenable_5_3(thenResolve5(nRejP3, 123, "abc", true, 'D', false)))},
                {supp(thenable_5_4(thenResolve5(nRejP4, 'i', "abc", true, 'D', -4.5)))},
                {supp(thenable_5_5(thenResolve5(nRejP5, 123, -68., true, 'D', -4.5)))},
            }
        );}
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void singleValueClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(new OnFulfilled<Integer>() { @Override public Object call(final Integer v) {
                callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v);
                return null;
            }}).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void dualValueClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled2<Integer, String>() {
                @Override public Object call(final Integer v1, final String v2) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2);
                    return null;
                }
            })).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void tripleValueClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled3<Integer, String, Boolean>() {
                @Override public Object call(final Integer v1, final String v2, final Boolean v3) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3);
                    return null;
                }
            })).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void quadValueClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled4<Integer, String, Boolean, Character>() {
                @Override public Object call(final Integer v1, final String v2, final Boolean v3, final Character v4) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3, v4);
                    return null;
                }
            })).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void pentaValueClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(wf(new OnFulfilled5<Integer, String, Boolean, Character, Double>() {
                @Override public Object
                call(final Integer v1, final String v2, final Boolean v3, final Character v4, final Double v5) {
                    callbackLoggerMock.onFulfilled(1, Thread.currentThread(), v1, v2, v3, v4, v5);
                    return null;
                }
            })).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
    @RunWith(JUnitParamsRunner.class)
    public static final class RejectedThenChain extends Params
    {
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsIntegerReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                          l("p1"),  null, null, Throwable.class},
                {suppPromise("p2", pr(1, new Exception())),                             l("p2"),  null, 1,    Exception.class},
                {suppPromise("p3", pr(-2, new RuntimeException())),                     l("p3"),  null, -2,   RuntimeException.class},
                {suppPromise("p4", pr(null)),                                           l("p4"),  null, null, null},
                {suppPromise("p5", pr(3)),                                              l("p5"),  null, 3,    null},
                {suppPromise("p6", pr(-4)),                                             l("p6"),  null, -4,   null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                 l("p7"),  null, null, IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject(5, new IllegalStateException()))), l("p8"),  null, 5,    IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject(-6, new InterruptedException())), l("p9"),  null, -6,   InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),       l("p10"), null, null, ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                              null, Throwable.class},
                    {supp(pr(7, new Exception())),                                                                 7,    Exception.class},
                    {supp(pr(-8, new RuntimeException())),                                                         -8,   RuntimeException.class},
                    {supp(pr(null)),                                                                               null, null},
                    {supp(pr(9)),                                                                                  9,    null},
                    {supp(pr(-10)),                                                                                -10,  null},
                    {supp(pr(new IllegalArgumentException())),                                                     null, IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                       null, IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                        null, InterruptedException.class},
                    {supp(thenable(thenResolve(nRejP1, pr(11, new ArithmeticException())))),                       11,   ArithmeticException.class},
                    {supp(thenable(thenResolve(nRejP1, thenableReject(-12, new Throwable())))),                    -12,  Throwable.class},
                    {supp(thenable(thenReject(nResP1, 13, new Exception()))),                                      13,   Exception.class},
                    {supp(thenable(thenReject(nResP1, -14))),                                                      -14,  null},
                    {supp(thenable(thenReject(nResP1, new RuntimeException()))),                                   null, RuntimeException.class},
                    {supp(thenable(thenThrow(nResP1, nRejP1, new IllegalArgumentException()))),                    null, IllegalArgumentException.class},
                    {supp(thenable_1_2(thenResolve(nRejP2, pr(15, new IllegalStateException())))),                 15,   IllegalStateException.class},
                    {supp(thenable_1_2(thenResolve(nRejP2, thenableReject(-16, new InterruptedException())))),     -16,  InterruptedException.class},
                    {supp(thenable_2_1(thenReject(nResP2, 17, new ArithmeticException()))),                        17,   ArithmeticException.class},
                    {supp(thenable_2_1(thenReject(nResP2, -18))),                                                  -18,  null},
                    {supp(thenable_2_1(thenReject(nResP2, new Throwable()))),                                      null, Throwable.class},
                    {supp(thenable_2_1(thenThrow(nResP2, nRejP1, new Exception()))),                               null, Exception.class},
                    {supp(thenable_1_3(thenResolve(nRejP3, pr(19, new RuntimeException())))),                      19,   RuntimeException.class},
                    {supp(thenable_1_3(thenResolve(nRejP3, thenableReject(-20, new IllegalArgumentException())))), -20,  IllegalArgumentException.class},
                    {supp(thenable_3_1(thenReject(nResP3, 21, new IllegalStateException()))),                      21,   IllegalStateException.class},
                    {supp(thenable_3_1(thenReject(nResP3, -22))),                                                  -22,  null},
                    {supp(thenable_3_1(thenReject(nResP3, new InterruptedException()))),                           null, InterruptedException.class},
                    {supp(thenable_3_1(thenThrow(nResP3, nRejP1, new ArithmeticException()))),                     null, ArithmeticException.class},
                    {supp(thenable_1_4(thenResolve(nRejP4, pr(23, new Throwable())))),                             23,   Throwable.class},
                    {supp(thenable_1_4(thenResolve(nRejP4, thenableReject(-24, new Exception())))),                -24,  Exception.class},
                    {supp(thenable_4_1(thenReject(nResP4, 25, new RuntimeException()))),                           25,   RuntimeException.class},
                    {supp(thenable_4_1(thenReject(nResP4, -26))),                                                  -26,  null},
                    {supp(thenable_4_1(thenReject(nResP4, new IllegalArgumentException()))),                       null, IllegalArgumentException.class},
                    {supp(thenable_4_1(thenThrow(nResP4, nRejP1, new IllegalStateException()))),                   null, IllegalStateException.class},
                    {supp(thenable_1_5(thenResolve(nRejP5, pr(27, new InterruptedException())))),                  27,   InterruptedException.class},
                    {supp(thenable_1_5(thenResolve(nRejP5, thenableReject(-28, new ArithmeticException())))),      -28,  ArithmeticException.class},
                    {supp(thenable_5_1(thenReject(nResP5, 29, new Throwable()))),                                  29,   Throwable.class},
                    {supp(thenable_5_1(thenReject(nResP5, -30))),                                                  -30,  null},
                    {supp(thenable_5_1(thenReject(nResP5, new Exception()))),                                      null, Exception.class},
                    {supp(thenable_5_1(thenThrow(nResP5, nRejP1, new RuntimeException()))),                        null, RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsIntegerReasonCall")
        public final void integerReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedReason,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                new OnRejected<Integer>() { @Override public Object call(final Integer r, final Throwable e) {
                    callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsStringReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                              l("p1"),  null, null,  Throwable.class},
                {suppPromise("p2", pr("abc", new Exception())),                             l("p2"),  null, "abc", Exception.class},
                {suppPromise("p3", pr("DEF", new RuntimeException())),                      l("p3"),  null, "DEF", RuntimeException.class},
                {suppPromise("p4", pr(null)),                                               l("p4"),  null, null,  null},
                {suppPromise("p5", pr("ghi")),                                              l("p5"),  null, "ghi", null},
                {suppPromise("p6", pr("JKL")),                                              l("p6"),  null, "JKL", null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                     l("p7"),  null, null,  IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject("mno", new IllegalStateException()))), l("p8"),  null, "mno", IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject("PQR", new InterruptedException())),  l("p9"),  null, "PQR", InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),           l("p10"), null, null,  ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                               null,  Throwable.class},
                    {supp(pr("stu", new Exception())),                                                              "stu", Exception.class},
                    {supp(pr("XYZ", new RuntimeException())),                                                       "XYZ", RuntimeException.class},
                    {supp(pr(null)),                                                                                null,  null},
                    {supp(pr("123")),                                                                               "123", null},
                    {supp(pr("456")),                                                                               "456", null},
                    {supp(pr(new IllegalArgumentException())),                                                      null,  IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                        null,  IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                         null,  InterruptedException.class},
                    {supp(thenable(thenResolve(nRejP1, pr("789", new ArithmeticException())))),                     "789", ArithmeticException.class},
                    {supp(thenable(thenResolve(nRejP1, thenableReject("AB", new Throwable())))),                    "AB",  Throwable.class},
                    {supp(thenable(thenReject(nResP1, "cd", new Exception()))),                                     "cd",  Exception.class},
                    {supp(thenable(thenReject(nResP1, "EF"))),                                                      "EF",  null},
                    {supp(thenable(thenReject(nResP1, new RuntimeException()))),                                    null,  RuntimeException.class},
                    {supp(thenable(thenThrow(nResP1, nRejP1, new IllegalArgumentException()))),                     null,  IllegalArgumentException.class},
                    {supp(thenable_1_2(thenResolve(nRejP2, pr("gh", new IllegalStateException())))),                "gh",  IllegalStateException.class},
                    {supp(thenable_1_2(thenResolve(nRejP2, thenableReject("IJ", new InterruptedException())))),     "IJ",  InterruptedException.class},
                    {supp(thenable_2_1(thenReject(nResP2, "kl", new ArithmeticException()))),                       "kl",  ArithmeticException.class},
                    {supp(thenable_2_1(thenReject(nResP2, "MN"))),                                                  "MN",  null},
                    {supp(thenable_2_1(thenReject(nResP2, new Throwable()))),                                       null,  Throwable.class},
                    {supp(thenable_2_1(thenThrow(nResP2, nRejP1, new Exception()))),                                null,  Exception.class},
                    {supp(thenable_1_3(thenResolve(nRejP3, pr("op", new RuntimeException())))),                     "op",  RuntimeException.class},
                    {supp(thenable_1_3(thenResolve(nRejP3, thenableReject("QR", new IllegalArgumentException())))), "QR",  IllegalArgumentException.class},
                    {supp(thenable_3_1(thenReject(nResP3, "st", new IllegalStateException()))),                     "st",  IllegalStateException.class},
                    {supp(thenable_3_1(thenReject(nResP3, "UV"))),                                                  "UV",  null},
                    {supp(thenable_3_1(thenReject(nResP3, new InterruptedException()))),                            null,  InterruptedException.class},
                    {supp(thenable_3_1(thenThrow(nResP3, nRejP1, new ArithmeticException()))),                      null,  ArithmeticException.class},
                    {supp(thenable_1_4(thenResolve(nRejP4, pr("wx", new Throwable())))),                            "wx",  Throwable.class},
                    {supp(thenable_1_4(thenResolve(nRejP4, thenableReject("YZ", new Exception())))),                "YZ",  Exception.class},
                    {supp(thenable_4_1(thenReject(nResP4, "12", new RuntimeException()))),                          "12",  RuntimeException.class},
                    {supp(thenable_4_1(thenReject(nResP4, "34"))),                                                  "34",  null},
                    {supp(thenable_4_1(thenReject(nResP4, new IllegalArgumentException()))),                        null,  IllegalArgumentException.class},
                    {supp(thenable_4_1(thenThrow(nResP4, nRejP1, new IllegalStateException()))),                    null,  IllegalStateException.class},
                    {supp(thenable_1_5(thenResolve(nRejP5, pr("56", new InterruptedException())))),                 "56",  InterruptedException.class},
                    {supp(thenable_1_5(thenResolve(nRejP5, thenableReject("78", new ArithmeticException())))),      "78",  ArithmeticException.class},
                    {supp(thenable_5_1(thenReject(nResP5, "a", new Throwable()))),                                  "a",   Throwable.class},
                    {supp(thenable_5_1(thenReject(nResP5, "B"))),                                                   "B",   null},
                    {supp(thenable_5_1(thenReject(nResP5, new Exception()))),                                       null,  Exception.class},
                    {supp(thenable_5_1(thenThrow(nResP5, nRejP1, new RuntimeException()))),                         null,  RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsStringReasonCall")
        public final void stringReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final String expectedReason,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                new OnRejected<String>() { @Override public Object call(final String r, final Throwable e) {
                    callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsDualReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                                        l("p1"),  null, null, null,  Throwable.class},
                {suppPromise("p2", pr(M2.of(1, "abc"), new Exception())),                             l("p2"),  null, 1,    "abc", Exception.class},
                {suppPromise("p3", pr(-2, "DEF", new RuntimeException())),                            l("p3"),  null, -2,   "DEF", RuntimeException.class},
                {suppPromise("p4", pr(null)),                                                         l("p4"),  null, null, null,  null},
                {suppPromise("p5", pr(M2.of(3, "ghi"))),                                              l("p5"),  null, 3,    "ghi", null},
                {suppPromise("p6", pr(-4, "JKL")),                                                    l("p6"),  null, -4,   "JKL", null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                               l("p7"),  null, null, null,  IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject(M2.of(5, "mno"), new IllegalStateException()))), l("p8"),  null, 5,    "mno", IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject(M2.of(-6, "PQR"), new InterruptedException())), l("p9"),  null, -6,   "PQR", InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),                     l("p10"), null, null, null,  ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                                            null, null,  Throwable.class},
                    {supp(pr(M2.of(7, "stu"), new Exception())),                                                                 7,    "stu", Exception.class},
                    {supp(pr(-8, "XYZ", new RuntimeException())),                                                                -8,   "XYZ", RuntimeException.class},
                    {supp(pr(null)),                                                                                             null, null,  null},
                    {supp(pr(M2.of(9, "123"))),                                                                                  9,    "123", null},
                    {supp(pr(-10, "456")),                                                                                       -10,  "456", null},
                    {supp(pr(new IllegalArgumentException())),                                                                   null, null,  IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                                     null, null,  IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                                      null, null,  InterruptedException.class},
                    {supp(thenable_2_1(thenResolve2(nRejP1, pr(11, "789", new ArithmeticException())))),                         11,   "789", ArithmeticException.class},
                    {supp(thenable_2_1(thenResolve2(nRejP1, thenableReject(M2.of(-12, "AB"), new Throwable())))),                -12,  "AB",  Throwable.class},
                    {supp(thenable_1_2(thenReject2(nResP1, 13, "cd", new Exception()))),                                         13,   "cd",  Exception.class},
                    {supp(thenable_1_2(thenReject2(nResP1, -14, "EF"))),                                                         -14,  "EF",  null},
                    {supp(thenable_1_2(thenReject2(nResP1, new RuntimeException()))),                                            null, null,  RuntimeException.class},
                    {supp(thenable_1_2(thenThrow(nResP1, nRejP2, new IllegalArgumentException()))),                              null, null,  IllegalArgumentException.class},
                    {supp(thenable_2_2(thenResolve2(nRejP2, pr(15, "gh", new IllegalStateException())))),                        15,   "gh",  IllegalStateException.class},
                    {supp(thenable_2_2(thenResolve2(nRejP2, thenableReject(M2.of(-16, "IJ"), new InterruptedException())))),     -16,  "IJ",  InterruptedException.class},
                    {supp(thenable_2_2(thenReject2(nResP2, 17, "kl", new ArithmeticException()))),                               17,   "kl",  ArithmeticException.class},
                    {supp(thenable_2_2(thenReject2(nResP2, -18, "MN"))),                                                         -18,  "MN",  null},
                    {supp(thenable_2_2(thenReject2(nResP2, new Throwable()))),                                                   null, null,  Throwable.class},
                    {supp(thenable_2_2(thenThrow(nResP2, nRejP2, new Exception()))),                                             null, null,  Exception.class},
                    {supp(thenable_2_3(thenResolve2(nRejP3, pr(19, "op", new RuntimeException())))),                             19,   "op",  RuntimeException.class},
                    {supp(thenable_2_3(thenResolve2(nRejP3, thenableReject(M2.of(-20, "QR"), new IllegalArgumentException())))), -20,  "QR",  IllegalArgumentException.class},
                    {supp(thenable_3_2(thenReject2(nResP3, 21, "st", new IllegalStateException()))),                             21,   "st",  IllegalStateException.class},
                    {supp(thenable_3_2(thenReject2(nResP3, -22, "UV"))),                                                         -22,  "UV",  null},
                    {supp(thenable_3_2(thenReject2(nResP3, new InterruptedException()))),                                        null, null,  InterruptedException.class},
                    {supp(thenable_3_2(thenThrow(nResP3, nRejP2, new ArithmeticException()))),                                   null, null,  ArithmeticException.class},
                    {supp(thenable_2_4(thenResolve2(nRejP4, pr(23, "wx", new Throwable())))),                                    23,   "wx",  Throwable.class},
                    {supp(thenable_2_4(thenResolve2(nRejP4, thenableReject(M2.of(-24, "YZ"), new Exception())))),                -24,  "YZ",  Exception.class},
                    {supp(thenable_4_2(thenReject2(nResP4, 25, "12", new RuntimeException()))),                                  25,   "12",  RuntimeException.class},
                    {supp(thenable_4_2(thenReject2(nResP4, -26, "34"))),                                                         -26,  "34",  null},
                    {supp(thenable_4_2(thenReject2(nResP4, new IllegalArgumentException()))),                                    null, null,  IllegalArgumentException.class},
                    {supp(thenable_4_2(thenThrow(nResP4, nRejP2, new IllegalStateException()))),                                 null, null,  IllegalStateException.class},
                    {supp(thenable_2_5(thenResolve2(nRejP5, pr(27, "56", new InterruptedException())))),                         27,   "56",  InterruptedException.class},
                    {supp(thenable_2_5(thenResolve2(nRejP5, thenableReject(M2.of(-28, "78"), new ArithmeticException())))),      -28,  "78",  ArithmeticException.class},
                    {supp(thenable_5_2(thenReject2(nResP5, 29, "a", new Throwable()))),                                          29,   "a",   Throwable.class},
                    {supp(thenable_5_2(thenReject2(nResP5, -30, "B"))),                                                          -30,  "B",   null},
                    {supp(thenable_5_2(thenReject2(nResP5, new Exception()))),                                                   null, null,  Exception.class},
                    {supp(thenable_5_2(thenThrow(nResP5, nRejP2, new RuntimeException()))),                                      null, null,  RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsDualReasonCall")
        public final void dualReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedReason1,
            final String expectedReason2,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected2<Integer, String>() {
                    @Override public Object call(final Integer r1, final String r2, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2);
                        return null;
                    }
                })
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason1, expectedReason2
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsTripleReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                                               l("p1"),  null, null, null,  null,  Throwable.class},
                {suppPromise("p2", pr(M3.of(1, "abc", true), new Exception())),                              l("p2"),  null, 1,    "abc", true,  Exception.class},
                {suppPromise("p3", pr(-2, "DEF", false, new RuntimeException())),                            l("p3"),  null, -2,   "DEF", false, RuntimeException.class},
                {suppPromise("p4", pr(null)),                                                                l("p4"),  null, null, null,  null,  null},
                {suppPromise("p5", pr(M3.of(3, "ghi", true))),                                               l("p5"),  null, 3,    "ghi", true,  null},
                {suppPromise("p6", pr(-4, "JKL", false)),                                                    l("p6"),  null, -4,   "JKL", false, null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                                      l("p7"),  null, null, null,  null,  IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject(M3.of(5, "mno", true), new IllegalStateException()))),  l("p8"),  null, 5,    "mno", true,  IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject(M3.of(-6, "PQR", false), new InterruptedException())), l("p9"),  null, -6,   "PQR", false, InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),                            l("p10"), null, null, null,  null,  ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                                                   null, null,  null,  Throwable.class},
                    {supp(pr(M3.of(7, "stu", true), new Exception())),                                                                  7,    "stu", true,  Exception.class},
                    {supp(pr(-8, "XYZ", false, new RuntimeException())),                                                                -8,   "XYZ", false, RuntimeException.class},
                    {supp(pr(null)),                                                                                                    null, null,  null,  null},
                    {supp(pr(M3.of(9, "123", true))),                                                                                   9,    "123", true,  null},
                    {supp(pr(-10, "456", false)),                                                                                       -10,  "456", false, null},
                    {supp(pr(new IllegalArgumentException())),                                                                          null, null,  null,  IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                                            null, null,  null,  IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                                             null, null,  null,  InterruptedException.class},
                    {supp(thenable_3_1(thenResolve3(nRejP1, pr(11, "789", true, new ArithmeticException())))),                          11,   "789", true,  ArithmeticException.class},
                    {supp(thenable_3_1(thenResolve3(nRejP1, thenableReject(M3.of(-12, "AB", false), new Throwable())))),                -12,  "AB",  false, Throwable.class},
                    {supp(thenable_1_3(thenReject3(nResP1, 13, "cd", true, new Exception()))),                                          13,   "cd",  true,  Exception.class},
                    {supp(thenable_1_3(thenReject3(nResP1, -14, "EF", false))),                                                         -14,  "EF",  false, null},
                    {supp(thenable_1_3(thenReject3(nResP1, new RuntimeException()))),                                                   null, null,  null,  RuntimeException.class},
                    {supp(thenable_1_3(thenThrow(nResP1, nRejP3, new IllegalArgumentException()))),                                     null, null,  null,  IllegalArgumentException.class},
                    {supp(thenable_3_2(thenResolve3(nRejP2, pr(15, "gh", true, new IllegalStateException())))),                         15,   "gh",  true,  IllegalStateException.class},
                    {supp(thenable_3_2(thenResolve3(nRejP2, thenableReject(M3.of(-16, "IJ", false), new InterruptedException())))),     -16,  "IJ",  false, InterruptedException.class},
                    {supp(thenable_2_3(thenReject3(nResP2, 17, "kl", true, new ArithmeticException()))),                                17,   "kl",  true,  ArithmeticException.class},
                    {supp(thenable_2_3(thenReject3(nResP2, -18, "MN", false))),                                                         -18,  "MN",  false, null},
                    {supp(thenable_2_3(thenReject3(nResP2, new Throwable()))),                                                          null, null,  null,  Throwable.class},
                    {supp(thenable_2_3(thenThrow(nResP2, nRejP3, new Exception()))),                                                    null, null,  null,  Exception.class},
                    {supp(thenable_3_3(thenResolve3(nRejP3, pr(19, "op", true, new RuntimeException())))),                              19,   "op",  true,  RuntimeException.class},
                    {supp(thenable_3_3(thenResolve3(nRejP3, thenableReject(M3.of(-20, "QR", false), new IllegalArgumentException())))), -20,  "QR",  false, IllegalArgumentException.class},
                    {supp(thenable_3_3(thenReject3(nResP3, 21, "st", true, new IllegalStateException()))),                              21,   "st",  true,  IllegalStateException.class},
                    {supp(thenable_3_3(thenReject3(nResP3, -22, "UV", false))),                                                         -22,  "UV",  false, null},
                    {supp(thenable_3_3(thenReject3(nResP3, new InterruptedException()))),                                               null, null,  null,  InterruptedException.class},
                    {supp(thenable_3_3(thenThrow(nResP3, nRejP3, new ArithmeticException()))),                                          null, null,  null,  ArithmeticException.class},
                    {supp(thenable_3_4(thenResolve3(nRejP4, pr(23, "wx", true, new Throwable())))),                                     23,   "wx",  true,  Throwable.class},
                    {supp(thenable_3_4(thenResolve3(nRejP4, thenableReject(M3.of(-24, "YZ", false), new Exception())))),                -24,  "YZ",  false, Exception.class},
                    {supp(thenable_4_3(thenReject3(nResP4, 25, "12", true, new RuntimeException()))),                                   25,   "12",  true,  RuntimeException.class},
                    {supp(thenable_4_3(thenReject3(nResP4, -26, "34", false))),                                                         -26,  "34",  false, null},
                    {supp(thenable_4_3(thenReject3(nResP4, new IllegalArgumentException()))),                                           null, null,  null,  IllegalArgumentException.class},
                    {supp(thenable_4_3(thenThrow(nResP4, nRejP3, new IllegalStateException()))),                                        null, null,  null,  IllegalStateException.class},
                    {supp(thenable_3_5(thenResolve3(nRejP5, pr(27, "56", true, new InterruptedException())))),                          27,   "56",  true,  InterruptedException.class},
                    {supp(thenable_3_5(thenResolve3(nRejP5, thenableReject(M3.of(-28, "78", false), new ArithmeticException())))),      -28,  "78",  false, ArithmeticException.class},
                    {supp(thenable_5_3(thenReject3(nResP5, 29, "a", true, new Throwable()))),                                           29,   "a",   true,  Throwable.class},
                    {supp(thenable_5_3(thenReject3(nResP5, -30, "B", false))),                                                          -30,  "B",   false, null},
                    {supp(thenable_5_3(thenReject3(nResP5, new Exception()))),                                                          null, null,  null,  Exception.class},
                    {supp(thenable_5_3(thenThrow(nResP5, nRejP3, new RuntimeException()))),                                             null, null,  null,  RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsTripleReasonCall")
        public final void tripleReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedReason1,
            final String expectedReason2,
            final Boolean expectedReason3,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected3<Integer, String, Boolean>() {
                    @Override public Object
                    call(final Integer r1, final String r2, final Boolean r3, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3);
                        return null;
                    }
                })
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason1, expectedReason2, expectedReason3
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsQuadReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                                                    l("p1"),  null, null, null,  null,  null, Throwable.class},
                {suppPromise("p2", pr(M4.of(1, "abc", true, 'A'), new Exception())),                              l("p2"),  null, 1,    "abc", true,  'A',  Exception.class},
                {suppPromise("p3", pr(-2, "DEF", false, 'b', new RuntimeException())),                            l("p3"),  null, -2,   "DEF", false, 'b',  RuntimeException.class},
                {suppPromise("p4", pr(null)),                                                                     l("p4"),  null, null, null,  null,  null, null},
                {suppPromise("p5", pr(M4.of(3, "ghi", true, 'C'))),                                               l("p5"),  null, 3,    "ghi", true,  'C',  null},
                {suppPromise("p6", pr(-4, "JKL", false, 'd')),                                                    l("p6"),  null, -4,   "JKL", false, 'd',  null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                                           l("p7"),  null, null, null,  null,  null, IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject(M4.of(5, "mno", true, 'E'), new IllegalStateException()))),  l("p8"),  null, 5,    "mno", true,  'E',  IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject(M4.of(-6, "PQR", false, 'f'), new InterruptedException())), l("p9"),  null, -6,   "PQR", false, 'f',  InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),                                 l("p10"), null, null, null,  null,  null, ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                                                        null, null,  null,  null, Throwable.class},
                    {supp(pr(M4.of(7, "stu", true, 'G'), new Exception())),                                                                  7,    "stu", true,  'G',  Exception.class},
                    {supp(pr(-8, "XYZ", false, 'h', new RuntimeException())),                                                                -8,   "XYZ", false, 'h',  RuntimeException.class},
                    {supp(pr(null)),                                                                                                         null, null,  null,  null, null},
                    {supp(pr(M4.of(9, "123", true, 'I'))),                                                                                   9,    "123", true,  'I',  null},
                    {supp(pr(-10, "456", false, 'j')),                                                                                       -10,  "456", false, 'j',  null},
                    {supp(pr(new IllegalArgumentException())),                                                                               null, null,  null,  null, IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                                                 null, null,  null,  null, IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                                                  null, null,  null,  null, InterruptedException.class},
                    {supp(thenable_4_1(thenResolve4(nRejP1, pr(11, "789", true, 'K', new ArithmeticException())))),                          11,   "789", true,  'K',  ArithmeticException.class},
                    {supp(thenable_4_1(thenResolve4(nRejP1, thenableReject(M4.of(-12, "AB", false, 'l'), new Throwable())))),                -12,  "AB",  false, 'l',  Throwable.class},
                    {supp(thenable_1_4(thenReject4(nResP1, 13, "cd", true, 'M', new Exception()))),                                          13,   "cd",  true,  'M',  Exception.class},
                    {supp(thenable_1_4(thenReject4(nResP1, -14, "EF", false, 'n'))),                                                         -14,  "EF",  false, 'n',  null},
                    {supp(thenable_1_4(thenReject4(nResP1, new RuntimeException()))),                                                        null, null,  null,  null, RuntimeException.class},
                    {supp(thenable_1_4(thenThrow(nResP1, nRejP4, new IllegalArgumentException()))),                                          null, null,  null,  null, IllegalArgumentException.class},
                    {supp(thenable_4_2(thenResolve4(nRejP2, pr(15, "gh", true, 'O', new IllegalStateException())))),                         15,   "gh",  true,  'O',  IllegalStateException.class},
                    {supp(thenable_4_2(thenResolve4(nRejP2, thenableReject(M4.of(-16, "IJ", false, 'p'), new InterruptedException())))),     -16,  "IJ",  false, 'p',  InterruptedException.class},
                    {supp(thenable_2_4(thenReject4(nResP2, 17, "kl", true, 'Q', new ArithmeticException()))),                                17,   "kl",  true,  'Q',  ArithmeticException.class},
                    {supp(thenable_2_4(thenReject4(nResP2, -18, "MN", false, 'r'))),                                                         -18,  "MN",  false, 'r',  null},
                    {supp(thenable_2_4(thenReject4(nResP2, new Throwable()))),                                                               null, null,  null,  null, Throwable.class},
                    {supp(thenable_2_4(thenThrow(nResP2, nRejP4, new Exception()))),                                                         null, null,  null,  null, Exception.class},
                    {supp(thenable_4_3(thenResolve4(nRejP3, pr(19, "op", true, 'S', new RuntimeException())))),                              19,   "op",  true,  'S',  RuntimeException.class},
                    {supp(thenable_4_3(thenResolve4(nRejP3, thenableReject(M4.of(-20, "QR", false, 't'), new IllegalArgumentException())))), -20,  "QR",  false, 't',  IllegalArgumentException.class},
                    {supp(thenable_3_4(thenReject4(nResP3, 21, "st", true, 'U', new IllegalStateException()))),                              21,   "st",  true,  'U',  IllegalStateException.class},
                    {supp(thenable_3_4(thenReject4(nResP3, -22, "UV", false, 'v'))),                                                         -22,  "UV",  false, 'v',  null},
                    {supp(thenable_3_4(thenReject4(nResP3, new InterruptedException()))),                                                    null, null,  null,  null, InterruptedException.class},
                    {supp(thenable_3_4(thenThrow(nResP3, nRejP4, new ArithmeticException()))),                                               null, null,  null,  null, ArithmeticException.class},
                    {supp(thenable_4_4(thenResolve4(nRejP4, pr(23, "wx", true, 'W', new Throwable())))),                                     23,   "wx",  true,  'W',  Throwable.class},
                    {supp(thenable_4_4(thenResolve4(nRejP4, thenableReject(M4.of(-24, "YZ", false, 'x'), new Exception())))),                -24,  "YZ",  false, 'x',  Exception.class},
                    {supp(thenable_4_4(thenReject4(nResP4, 25, "12", true, 'Y', new RuntimeException()))),                                   25,   "12",  true,  'Y',  RuntimeException.class},
                    {supp(thenable_4_4(thenReject4(nResP4, -26, "34", false, 'z'))),                                                         -26,  "34",  false, 'z',  null},
                    {supp(thenable_4_4(thenReject4(nResP4, new IllegalArgumentException()))),                                                null, null,  null,  null, IllegalArgumentException.class},
                    {supp(thenable_4_4(thenThrow(nResP4, nRejP4, new IllegalStateException()))),                                             null, null,  null,  null, IllegalStateException.class},
                    {supp(thenable_4_5(thenResolve4(nRejP5, pr(27, "56", true, '1', new InterruptedException())))),                          27,   "56",  true,  '1',  InterruptedException.class},
                    {supp(thenable_4_5(thenResolve4(nRejP5, thenableReject(M4.of(-28, "78", false, '2'), new ArithmeticException())))),      -28,  "78",  false, '2',  ArithmeticException.class},
                    {supp(thenable_5_4(thenReject4(nResP5, 29, "a", true, '3', new Throwable()))),                                           29,   "a",   true,  '3',  Throwable.class},
                    {supp(thenable_5_4(thenReject4(nResP5, -30, "B", false, '4'))),                                                          -30,  "B",   false, '4',  null},
                    {supp(thenable_5_4(thenReject4(nResP5, new Exception()))),                                                               null, null,  null,  null, Exception.class},
                    {supp(thenable_5_4(thenThrow(nResP5, nRejP4, new RuntimeException()))),                                                  null, null,  null,  null, RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsQuadReasonCall")
        public final void quadReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedReason1,
            final String expectedReason2,
            final Boolean expectedReason3,
            final Character expectedReason4,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected4<Integer, String, Boolean, Character>() {
                    @Override public Object
                    call(final Integer r1, final String r2, final Boolean r3, final Character r4, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3, r4);
                        return null;
                    }
                })
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason1, expectedReason2, expectedReason3, expectedReason4
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsPentaReasonCall() { return TestData.union(
            new Object[][] {
                {suppPromise("p1", pr(null, new Throwable())),                                                         l("p1"),  null, null, null,  null,  null, null, Throwable.class},
                {suppPromise("p2", pr(M5.of(1, "abc", true, 'A', -1.2), new Exception())),                             l("p2"),  null, 1,    "abc", true,  'A',  -1.2, Exception.class},
                {suppPromise("p3", pr(-2, "DEF", false, 'b', 23., new RuntimeException())),                            l("p3"),  null, -2,   "DEF", false, 'b',  23.,  RuntimeException.class},
                {suppPromise("p4", pr(null)),                                                                          l("p4"),  null, null, null,  null,  null, null, null},
                {suppPromise("p5", pr(M5.of(3, "ghi", true, 'C', -.34))),                                              l("p5"),  null, 3,    "ghi", true,  'C',  -.34, null},
                {suppPromise("p6", pr(-4, "JKL", false, 'd', 4.5)),                                                    l("p6"),  null, -4,   "JKL", false, 'd',  4.5,  null},
                {suppPromise("p7", pr(new IllegalArgumentException())),                                                l("p7"),  null, null, null,  null,  null, null, IllegalArgumentException.class},
                {suppPromise("p8", pt(thenableReject(M5.of(5, "mno", true, 'E', -56.), new IllegalStateException()))), l("p8"),  null, 5,    "mno", true,  'E',  -56., IllegalStateException.class},
                {suppPromiseByPt("p9", thenableReject(M5.of(-6, "PQR", false, 'f', .67), new InterruptedException())), l("p9"),  null, -6,   "PQR", false, 'f',  .67,  InterruptedException.class},
                {suppPromiseByAsync("p10", suppThrow(new ArithmeticException())),                                      l("p10"), null, null, null,  null,  null, null, ArithmeticException.class},
            },
            TestData.merge(
                paramsPromiseSupplier(),
                new Object[][] {
                    {supp(pr(null, new Throwable())),                                                                                             null, null,  null,  null, null, Throwable.class},
                    {supp(pr(M5.of(7, "stu", true, 'G', -7.8), new Exception())),                                                                 7,    "stu", true,  'G',  -7.8, Exception.class},
                    {supp(pr(-8, "XYZ", false, 'h', 89., new RuntimeException())),                                                                -8,   "XYZ", false, 'h',  89.,  RuntimeException.class},
                    {supp(pr(null)),                                                                                                              null, null,  null,  null, null, null},
                    {supp(pr(M5.of(9, "123", true, 'I', -.13))),                                                                                  9,    "123", true,  'I',  -.13, null},
                    {supp(pr(-10, "456", false, 'j', 2.4)),                                                                                       -10,  "456", false, 'j',  2.4,  null},
                    {supp(pr(new IllegalArgumentException())),                                                                                    null, null,  null,  null, null, IllegalArgumentException.class},
                    {suppThrow(new IllegalStateException()),                                                                                      null, null,  null,  null, null, IllegalStateException.class},
                    {suppThrow(new InterruptedException()),                                                                                       null, null,  null,  null, null, InterruptedException.class},
                    {supp(thenable_5_1(thenResolve5(nRejP1, pr(11, "789", true, 'K', -35., new ArithmeticException())))),                         11,   "789", true,  'K',  -35., ArithmeticException.class},
                    {supp(thenable_5_1(thenResolve5(nRejP1, thenableReject(M5.of(-12, "AB", false, 'l', .46), new Throwable())))),                -12,  "AB",  false, 'l',  .46,  Throwable.class},
                    {supp(thenable_1_5(thenReject5(nResP1, 13, "cd", true, 'M', -5.7, new Exception()))),                                         13,   "cd",  true,  'M',  -5.7, Exception.class},
                    {supp(thenable_1_5(thenReject5(nResP1, -14, "EF", false, 'n', 68.))),                                                         -14,  "EF",  false, 'n',  68.,  null},
                    {supp(thenable_1_5(thenReject5(nResP1, new RuntimeException()))),                                                             null, null,  null,  null, null, RuntimeException.class},
                    {supp(thenable_1_5(thenThrow(nResP1, nRejP5, new IllegalArgumentException()))),                                               null, null,  null,  null, null, IllegalArgumentException.class},
                    {supp(thenable_5_2(thenResolve5(nRejP2, pr(15, "gh", true, 'O', -.79, new IllegalStateException())))),                        15,   "gh",  true,  'O',  -.79, IllegalStateException.class},
                    {supp(thenable_5_2(thenResolve5(nRejP2, thenableReject(M5.of(-16, "IJ", false, 'p', 1.4), new InterruptedException())))),     -16,  "IJ",  false, 'p',  1.4,  InterruptedException.class},
                    {supp(thenable_2_5(thenReject5(nResP2, 17, "kl", true, 'Q', -25., new ArithmeticException()))),                               17,   "kl",  true,  'Q',  -25., ArithmeticException.class},
                    {supp(thenable_2_5(thenReject5(nResP2, -18, "MN", false, 'r', .36))),                                                         -18,  "MN",  false, 'r',  .36,  null},
                    {supp(thenable_2_5(thenReject5(nResP2, new Throwable()))),                                                                    null, null,  null,  null, null, Throwable.class},
                    {supp(thenable_2_5(thenThrow(nResP2, nRejP5, new Exception()))),                                                              null, null,  null,  null, null, Exception.class},
                    {supp(thenable_5_3(thenResolve5(nRejP3, pr(19, "op", true, 'S', -4.7, new RuntimeException())))),                             19,   "op",  true,  'S',  -4.7, RuntimeException.class},
                    {supp(thenable_5_3(thenResolve5(nRejP3, thenableReject(M5.of(-20, "QR", false, 't', 58.), new IllegalArgumentException())))), -20,  "QR",  false, 't',  58.,  IllegalArgumentException.class},
                    {supp(thenable_3_5(thenReject5(nResP3, 21, "st", true, 'U', -.69, new IllegalStateException()))),                             21,   "st",  true,  'U',  -.69, IllegalStateException.class},
                    {supp(thenable_3_5(thenReject5(nResP3, -22, "UV", false, 'v', 1.5))),                                                         -22,  "UV",  false, 'v',  1.5,  null},
                    {supp(thenable_3_5(thenReject5(nResP3, new InterruptedException()))),                                                         null, null,  null,  null, null, InterruptedException.class},
                    {supp(thenable_3_5(thenThrow(nResP3, nRejP5, new ArithmeticException()))),                                                    null, null,  null,  null, null, ArithmeticException.class},
                    {supp(thenable_5_4(thenResolve5(nRejP4, pr(23, "wx", true, 'W', -26., new Throwable())))),                                    23,   "wx",  true,  'W',  -26., Throwable.class},
                    {supp(thenable_5_4(thenResolve5(nRejP4, thenableReject(M5.of(-24, "YZ", false, 'x', .37), new Exception())))),                -24,  "YZ",  false, 'x',  .37,  Exception.class},
                    {supp(thenable_4_5(thenReject5(nResP4, 25, "12", true, 'Y', -4.8, new RuntimeException()))),                                  25,   "12",  true,  'Y',  -4.8, RuntimeException.class},
                    {supp(thenable_4_5(thenReject5(nResP4, -26, "34", false, 'z', 59.))),                                                         -26,  "34",  false, 'z',  59.,  null},
                    {supp(thenable_4_5(thenReject5(nResP4, new IllegalArgumentException()))),                                                     null, null,  null,  null, null, IllegalArgumentException.class},
                    {supp(thenable_4_5(thenThrow(nResP4, nRejP5, new IllegalStateException()))),                                                  null, null,  null,  null, null, IllegalStateException.class},
                    {supp(thenable_5_5(thenResolve5(nRejP5, pr(27, "56", true, '1', -.16, new InterruptedException())))),                         27,   "56",  true,  '1',  -.16, InterruptedException.class},
                    {supp(thenable_5_5(thenResolve5(nRejP5, thenableReject(M5.of(-28, "78", false, '2', 2.7), new ArithmeticException())))),      -28,  "78",  false, '2',  2.7,  ArithmeticException.class},
                    {supp(thenable_5_5(thenReject5(nResP5, 29, "a", true, '3', -38., new Throwable()))),                                          29,   "a",   true,  '3',  -38., Throwable.class},
                    {supp(thenable_5_5(thenReject5(nResP5, -30, "B", false, '4', .49))),                                                          -30,  "B",   false, '4',  .49,  null},
                    {supp(thenable_5_5(thenReject5(nResP5, new Exception()))),                                                                    null, null,  null,  null, null, Exception.class},
                    {supp(thenable_5_5(thenThrow(nResP5, nRejP5, new RuntimeException()))),                                                       null, null,  null,  null, null, RuntimeException.class},
                }
            )
        );}

        @Test
        @Parameters(method = "paramsPentaReasonCall")
        public final void pentaReasonCall(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution,
            final Integer expectedReason1,
            final String expectedReason2,
            final Boolean expectedReason3,
            final Character expectedReason4,
            final Double expectedReason5,
            final Class<?> expectedExceptionClass
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected5<Integer, String, Boolean, Character, Double>() {
                    @Override public Object call(
                        final Integer r1,
                        final String r2,
                        final Boolean r3,
                        final Character r4,
                        final Double r5,
                        final Throwable e
                    ) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3, r4, r5);
                        return null;
                    }
                })
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    1, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(expectedExceptionClass)),
                    expectedReason1, expectedReason2, expectedReason3, expectedReason4, expectedReason5
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @SuppressWarnings("unused")
        private static Object[][] paramsClassCastException() { return TestData.merge(
            paramsPromiseSupplier(),
            new Object[][] {
                {supp(pr("DEF", new Exception()))},
                {supp(pr(true, "abc", new Exception()))},
                {supp(pr('g', "abc", true, new Exception()))},
                {supp(pr(35., "abc", true, 'D', new Exception()))},
                {supp(pr(-357L, "abc", true, 'D', -4.5, new Exception()))},
                {supp(pr(false))},
                {supp(pr(123, 'F'))},
                {supp(pr(123, .13, true))},
                {supp(pr(123, -135L, true, 'D'))},
                {supp(pr(123, 5.7f, true, 'D', -4.5))},
                {supp(thenable(thenReject(nResP1, 'e', new Exception())))},
                {supp(thenable_1_2(thenReject2(nResP1, 7.8, "abc", new Exception())))},
                {supp(thenable_1_3(thenReject3(nResP1, 123, "abc", -678L, new Exception())))},
                {supp(thenable_1_4(thenReject4(nResP1, 123, "abc", -.46f, 'D', new Exception())))},
                {supp(thenable_1_5(thenReject5(nResP1, 123, "abc", 468, 'D', -4.5, new Exception())))},
                {supp(thenable_2_1(thenReject(nResP2, 56., new Exception())))},
                {supp(thenable_2_2(thenReject2(nResP2, 123, -456L, new Exception())))},
                {supp(thenable_2_3(thenReject3(nResP2, -2.4f, "abc", true, new Exception())))},
                {supp(thenable_2_4(thenReject4(nResP2, 123, "abc", true, 246, new Exception())))},
                {supp(thenable_2_5(thenReject5(nResP2, 123, "abc", true, "PQR", -4.5, new Exception())))},
                {supp(thenable_3_1(thenReject(nResP3, -234L, new Exception())))},
                {supp(thenable_3_2(thenReject2(nResP3, -89.f, "abc", new Exception())))},
                {supp(thenable_3_3(thenReject3(nResP3, 123, 789, true, new Exception())))},
                {supp(thenable_3_4(thenReject4(nResP3, "mno", "abc", true, 'D', new Exception())))},
                {supp(thenable_3_5(thenReject5(nResP3, 123, "abc", true, 'D', false, new Exception())))},
                {supp(thenable_4_1(thenReject(nResP4, -.67f, new Exception())))},
                {supp(thenable_4_2(thenReject2(nResP4, 123, 567, new Exception())))},
                {supp(thenable_4_3(thenReject3(nResP4, 123, "abc", "JKL", new Exception())))},
                {supp(thenable_4_4(thenReject4(nResP4, 123, true, true, 'D', new Exception())))},
                {supp(thenable_4_5(thenReject5(nResP4, 123, 'i', true, 'D', -4.5, new Exception())))},
                {supp(thenable_5_1(thenReject(nResP5, (byte) 34, new Exception())))},
                {supp(thenable_5_2(thenReject2(nResP5, "ghi", "abc", new Exception())))},
                {supp(thenable_5_3(thenReject3(nResP5, false, "abc", true, new Exception())))},
                {supp(thenable_5_4(thenReject4(nResP5, 123, "abc", 'H', 'D', new Exception())))},
                {supp(thenable_5_5(thenReject5(nResP5, 123, "abc", -68., 'D', -4.5, new Exception())))},
            }
        );}
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void singleReasonClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                new OnRejected<Integer>() { @Override public Object call(final Integer r, final Throwable e) {
                    callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r);
                    return null;
                }}
            ).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void dualReasonClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected2<Integer, String>() {
                    @Override public Object call(final Integer r1, final String r2, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2);
                        return null;
                    }
                })
            ).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void tripleReasonClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected3<Integer, String, Boolean>() {
                    @Override public Object
                    call(final Integer r1, final String r2, final Boolean r3, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3);
                        return null;
                    }
                })
            ).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void quadReasonClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected4<Integer, String, Boolean, Character>() {
                    @Override public Object
                    call(final Integer r1, final String r2, final Boolean r3, final Character r4, final Throwable e) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3, r4);
                        return null;
                    }
                })
            ).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
        @Test
        @Parameters(method = "paramsClassCastException")
        public final void pentaReasonClassCastException(
            final PromiseSupplier suppPromise,
            final Object[] promiseLog,
            final FR0<Object> suppResolution
        ) {
            suppPromise.get(loggerMock, suppResolution)
            .then(
                null,
                wr(new OnRejected5<Integer, String, Boolean, Character, Double>() {
                    @Override public Object call(
                        final Integer r1,
                        final String r2,
                        final Boolean r3,
                        final Character r4,
                        final Double r5,
                        final Throwable e
                    ) {
                        callbackLoggerMock.onRejected(1, Thread.currentThread(), e, r1, r2, r3, r4, r5);
                        return null;
                    }
                })
            ).then(
                null,
                new OnRejected<Object>() { @Override public Object call(final Object r, final Throwable e) {
                    callbackLoggerMock.onRejected(2, Thread.currentThread(), e, r);
                    return null;
                }}
            );

            new FullVerificationsInOrder() {{
                loggerMock.log(promiseLog);

                callbackLoggerMock.onRejected(
                    2, withAny(nullThread),
                    withArgThat(TestUtil.ofClass(ClassCastException.class)), (Object) null
                );
            }};
        }
        //-------------------------------------------------------------------------------------------------------------
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
