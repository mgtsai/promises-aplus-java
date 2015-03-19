//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import promises.impl.LoggerManager;
import promises.impl.UntypedPromiseImpl;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines static methods for manipulating promise stuffs with Untyped Style.
 */
public class Promises
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Registers a handler for handling {@link InternalException}.
     *
     * @param handler The {@link InternalException} handler
     */
    public static void registerInternalExceptionHandler(final InternalExceptionHandler handler)
    {
        LoggerManager.singleton().registerInternalExceptionHandler(handler);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the specified value itself.
     *
     * @param v The value
     * @return The value itself
     */
    public static <V> V v(final V v)
    {
        return v;
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Builds a dual-value tuple.
     *
     * @param v1 The 1st value
     * @param v2 The 2nd value
     * @return The tuple
     */
    public static <V1, V2> M2<V1, V2> v(final V1 v1, final V2 v2)
    {
        return new M2<V1, V2>(v1, v2);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Builds a triple-value tuple.
     *
     * @param v1 The 1st value
     * @param v2 The 2nd value
     * @param v3 The 3rd value
     * @return The tuple
     */
    public static <V1, V2, V3> M3<V1, V2, V3> v(final V1 v1, final V2 v2, final V3 v3)
    {
        return new M3<V1, V2, V3>(v1, v2, v3);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Builds a quad-value tuple.
     *
     *
     * @param v1 The 1st value
     * @param v2 The 2nd value
     * @param v3 The 3rd value
     * @param v4 The 4th value
     * @return The tuple
     */
    public static <V1, V2, V3, V4> M4<V1, V2, V3, V4> v(final V1 v1, final V2 v2, final V3 v3, final V4 v4)
    {
        return new M4<V1, V2, V3, V4>(v1, v2, v3, v4);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Builds a penta-value tuple.
     *
     * @param v1 The 1st value
     * @param v2 The 2nd value
     * @param v3 The 3rd value
     * @param v4 The 4th value
     * @param v5 The 5th value
     * @return The tuple
     */
    public static <V1, V2, V3, V4, V5> M5<V1, V2, V3, V4, V5>
    v(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5)
    {
        return new M5<V1, V2, V3, V4, V5>(v1, v2, v3, v4, v5);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-value fulfilled promise.
     *
     * @param v The fulfilled value of the new promise
     * @return The created promise
     */
    public static Promise pf(final Object v)
    {
        return UntypedPromiseImpl.factory.fulfilledPromise(v);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new dual-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @return The created promise
     */
    public static Promise pf(final Object v1, final Object v2)
    {
        return pf(v(v1, v2));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new triple-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @param v3 The 3rd fulfilled value of the new promise
     * @return The created promise
     */
    public static Promise pf(final Object v1, final Object v2, final Object v3)
    {
        return pf(v(v1, v2, v3));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new quad-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @param v3 The 3rd fulfilled value of the new promise
     * @param v4 The 4th fulfilled value of the new promise
     * @return The created promise
     */
    public static Promise pf(final Object v1, final Object v2, final Object v3, final Object v4)
    {
        return pf(v(v1, v2, v3, v4));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new penta-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @param v3 The 3rd fulfilled value of the new promise
     * @param v4 The 4th fulfilled value of the new promise
     * @param v5 The 4th fulfilled value of the new promise
     * @return The created promise
     */
    public static Promise pf(final Object v1, final Object v2, final Object v3, final Object v4, final Object v5)
    {
        return pf(v(v1, v2, v3, v4, v5));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-reason rejected promise with exception.
     *
     * @param r The rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r, final Throwable e)
    {
        return UntypedPromiseImpl.factory.rejectedPromise(r, e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new dual-reason rejected promise with exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Throwable e)
    {
        return pr(v(r1, r2), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new triple-reason rejected promise with exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Object r3, final Throwable e)
    {
        return pr(v(r1, r2, r3), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new quad-reason rejected promise with exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @param r4 The 4th rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Object r3, final Object r4, final Throwable e)
    {
        return pr(v(r1, r2, r3, r4), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new penta-reason rejected promise with exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @param r4 The 4th rejected value of the new promise
     * @param r5 The 5th rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise
    pr(final Object r1, final Object r2, final Object r3, final Object r4, final Object r5, final Throwable e)
    {
        return pr(v(r1, r2, r3, r4, r5), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-reason rejected promise without exception.
     *
     * @param r The rejected value of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r)
    {
        return pr(r, null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new dual-reason rejected promise without exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2)
    {
        return pr(v(r1, r2), null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new triple-reason rejected promise without exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Object r3)
    {
        return pr(v(r1, r2, r3), null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new quad-reason rejected promise without exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @param r4 The 4th rejected value of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Object r3, final Object r4)
    {
        return pr(v(r1, r2, r3, r4), null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new penta-reason rejected promise without exception.
     *
     * @param r1 The 1st rejected value of the new promise
     * @param r2 The 2nd rejected value of the new promise
     * @param r3 The 3rd rejected value of the new promise
     * @param r4 The 4th rejected value of the new promise
     * @param r5 The 5th rejected value of the new promise
     * @return The created promise
     */
    public static Promise pr(final Object r1, final Object r2, final Object r3, final Object r4, final Object r5)
    {
        return pr(v(r1, r2, r3, r4, r5), null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new exception only rejected promise.
     *
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static Promise pr(final Throwable e)
    {
        return pr(null, e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified dual-argument {@code onFulfilled} callback to a single aggregated-argument callback.
     *
     * @param onFulfilled The dual-argument {@code onFulfilled} callback to be wrapped
     * @return The wrapper callback
     */
    public static <VI1, VI2> OnFulfilled<M2<VI1, VI2>> wf(final FR2<? super VI1, ? super VI2, ?> onFulfilled)
    {
        return new OnFulfilled<M2<VI1, VI2>>() {
            @Override public final Object call(final M2<VI1, VI2> v) throws Throwable {
                return onFulfilled.call(v.v1, v.v2);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified triple-argument {@code onFulfilled} callback to a single aggregated-argument callback.
     *
     * @param onFulfilled The triple-argument {@code onFulfilled} callback to be wrapped
     * @return The wrapper callback
     */
    public static <VI1, VI2, VI3> OnFulfilled<M3<VI1, VI2, VI3>>
    wf(final FR3<? super VI1, ? super VI2, ? super VI3, ?> onFulfilled)
    {
        return new OnFulfilled<M3<VI1, VI2, VI3>>() {
            @Override public final Object call(final M3<VI1, VI2, VI3> v) throws Throwable {
                return onFulfilled.call(v.v1, v.v2, v.v3);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified quad-argument {@code onFulfilled} callback to a single aggregated-argument callback.
     *
     * @param onFulfilled The quad-argument {@code onFulfilled} callback to be wrapped
     * @return The wrapper callback
     */
    public static <VI1, VI2, VI3, VI4> OnFulfilled<M4<VI1, VI2, VI3, VI4>>
    wf(final FR4<? super VI1, ? super VI2, ? super VI3, ? super VI4, ?> onFulfilled)
    {
        return new OnFulfilled<M4<VI1, VI2, VI3, VI4>>() {
            @Override public final Object call(final M4<VI1, VI2, VI3, VI4> v) throws Throwable {
                return onFulfilled.call(v.v1, v.v2, v.v3, v.v4);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified penta-argument {@code onFulfilled} callback to a single aggregated-argument callback.
     *
     * @param onFulfilled The penta-argument {@code onFulfilled} callback to be wrapped
     * @return The wrapper callback
     */
    public static <VI1, VI2, VI3, VI4, VI5> OnFulfilled<M5<VI1, VI2, VI3, VI4, VI5>>
    wf(final FR5<? super VI1, ? super VI2, ? super VI3, ? super VI4, ? super VI5, ?> onFulfilled)
    {
        return new OnFulfilled<M5<VI1, VI2, VI3, VI4, VI5>>() {
            @Override public final Object call(final M5<VI1, VI2, VI3, VI4, VI5> v) throws Throwable {
                return onFulfilled.call(v.v1, v.v2, v.v3, v.v4, v.v5);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified dual-argument {@code onRejected} callback to a single aggregated-argument callback.
     *
     * @param onRejected The dual-argument {@code onRejected} callback to be wrapped
     * @return The wrapper callback
     */
    public static <RI1, RI2> OnRejected<M2<RI1, RI2>> wr(final FR3<? super RI1, ? super RI2, Throwable, ?> onRejected)
    {
        return new OnRejected<M2<RI1, RI2>>() {
            @Override public final Object call(final M2<RI1, RI2> r, final Throwable e) throws Throwable {
                return onRejected.call(r.v1, r.v2, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified triple-argument {@code onRejected} callback to a single aggregated-argument callback.
     *
     * @param onRejected The triple-argument {@code onRejected} callback to be wrapped
     * @return The wrapper callback
     */
    public static <RI1, RI2, RI3> OnRejected<M3<RI1, RI2, RI3>>
    wr(final FR4<? super RI1, ? super RI2, ? super RI3, Throwable, ?> onRejected)
    {
        return new OnRejected<M3<RI1, RI2, RI3>>() {
            @Override public final Object call(final M3<RI1, RI2, RI3> r, final Throwable e) throws Throwable {
                return onRejected.call(r.v1, r.v2, r.v3, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified quad-argument {@code onRejected} callback to a single aggregated-argument callback.
     *
     * @param onRejected The quad-argument {@code onRejected} callback to be wrapped
     * @return The wrapper callback
     */
    public static <RI1, RI2, RI3, RI4> OnRejected<M4<RI1, RI2, RI3, RI4>>
    wr(final FR5<? super RI1, ? super RI2, ? super RI3, ? super RI4, Throwable, ?> onRejected)
    {
        return new OnRejected<M4<RI1, RI2, RI3, RI4>>() {
            @Override public final Object call(final M4<RI1, RI2, RI3, RI4> r, final Throwable e) throws Throwable {
                return onRejected.call(r.v1, r.v2, r.v3, r.v4, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified penta-argument {@code onRejected} callback to a single aggregated-argument callback.
     *
     * @param onRejected The penta-argument {@code onRejected} callback to be wrapped
     * @return The wrapper callback
     */
    public static <RI1, RI2, RI3, RI4, RI5> OnRejected<M5<RI1, RI2, RI3, RI4, RI5>>
    wr(final FR6<? super RI1, ? super RI2, ? super RI3, ? super RI4, ? super RI5, Throwable, ?> onRejected)
    {
        return new OnRejected<M5<RI1, RI2, RI3, RI4, RI5>>() {
            @Override public final Object
            call(final M5<RI1, RI2, RI3, RI4, RI5> r, final Throwable e) throws Throwable {
                return onRejected.call(r.v1, r.v2, r.v3, r.v4, r.v5, e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Asynchronously executes a callback function by returning a promise object to represent the execution state.
     * <p/>
     * The returned promise would be fulfilled with the value returned by the callback execution, or be rejected
     * with the exception thrown by the execution.
     *
     * @param exec The executor which the callback function would be executed on
     * @param onExec The callback function to be executed
     * @return The promise representing the callback execution state
     */
    public static Promise async(final Executor exec, final FR0<?> onExec)
    {
        return pf(null).then(
            exec,
            new OnFulfilled<Object>() { @Override public final Object call(final Object dummy) throws Throwable {
                return onExec.call();
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
