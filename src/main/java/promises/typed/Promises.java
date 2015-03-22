//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.FR0;
import promises.FR2;
import promises.FR3;
import promises.FR4;
import promises.FR5;
import promises.FR6;
import promises.M2;
import promises.M3;
import promises.M4;
import promises.M5;
import promises.PromiseState;
import promises.impl.TypedPromiseImpl;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines static methods for manipulating promise stuffs with Typed Style.
 */
public class Promises
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified value as a fulfilled resolution.
     *
     * @param v The value to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R> Resolution<V, R> v(final V v)
    {
        return new Resolution<V, R>() {
            @Override public final PromiseState state() { return PromiseState.FULFILLED; }
            @Override public final V value() { return v; }
            @Override public final R reason() { return null; }
            @Override public final Throwable exception() { return null; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified dual-value as a fulfilled resolution.
     *
     * @param v1 The 1st value to be wrapped
     * @param v2 The 2nd value to be wrapped
     * @return The fulfilled resolution
     */
    public static <V1, V2, R> Resolution<M2<V1, V2>, R> v(final V1 v1, final V2 v2)
    {
        return v(new M2<V1, V2>(v1, v2));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified triple-value as a fulfilled resolution.
     *
     * @param v1 The 1st value to be wrapped
     * @param v2 The 2nd value to be wrapped
     * @param v3 The 3rd value to be wrapped
     * @return The fulfilled resolution
     */
    public static <V1, V2, V3, R> Resolution<M3<V1, V2, V3>, R> v(final V1 v1, final V2 v2, final V3 v3)
    {
        return v(new M3<V1, V2, V3>(v1, v2, v3));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified quad-value as a fulfilled resolution.
     *
     * @param v1 The 1st value to be wrapped
     * @param v2 The 2nd value to be wrapped
     * @param v3 The 3rd value to be wrapped
     * @param v4 The 4th value to be wrapped
     * @return The fulfilled resolution
     */
    public static <V1, V2, V3, V4, R> Resolution<M4<V1, V2, V3, V4>, R>
    v(final V1 v1, final V2 v2, final V3 v3, final V4 v4)
    {
        return v(new M4<V1, V2, V3, V4>(v1, v2, v3, v4));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified penta-value as a fulfilled resolution.
     *
     * @param v1 The 1st value to be wrapped
     * @param v2 The 2nd value to be wrapped
     * @param v3 The 3rd value to be wrapped
     * @param v4 The 4th value to be wrapped
     * @param v5 The 5th value to be wrapped
     * @return The fulfilled resolution
     */
    public static <V1, V2, V3, V4, V5, R> Resolution<M5<V1, V2, V3, V4, V5>, R>
    v(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5)
    {
        return v(new M5<V1, V2, V3, V4, V5>(v1, v2, v3, v4, v5));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified reason and exception as a rejected resolution.
     *
     * @param r The reason to be wrapped
     * @param e The exception to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R> Resolution<V, R> r(final R r, final Throwable e)
    {
        return new Resolution<V, R>() {
            @Override public final PromiseState state() { return PromiseState.REJECTED; }
            @Override public final V value() { return null; }
            @Override public final R reason() { return r; }
            @Override public final Throwable exception() { return e; }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified dual-reason and exception as a rejected resolution.
     *
     * @param r1 The 1st reason to be wrapped
     * @param r2 The 2nd reason to be wrapped
     * @param e The exception to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R1, R2> Resolution<V, M2<R1, R2>> r(final R1 r1, final R2 r2, final Throwable e)
    {
        return r(new M2<R1, R2>(r1, r2), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified triple-reason and exception as a rejected resolution.
     *
     * @param r1 The 1st reason to be wrapped
     * @param r2 The 2nd reason to be wrapped
     * @param r3 The 3rd reason to be wrapped
     * @param e The exception to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R1, R2, R3> Resolution<V, M3<R1, R2, R3>>
    r(final R1 r1, final R2 r2, final R3 r3, final Throwable e)
    {
        return r(new M3<R1, R2, R3>(r1, r2, r3), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified quad-reason and exception as a rejected resolution.
     *
     * @param r1 The 1st reason to be wrapped
     * @param r2 The 2nd reason to be wrapped
     * @param r3 The 3rd reason to be wrapped
     * @param r4 The 4th reason to be wrapped
     * @param e The exception to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R1, R2, R3, R4> Resolution<V, M4<R1, R2, R3, R4>>
    r(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final Throwable e)
    {
        return r(new M4<R1, R2, R3, R4>(r1, r2, r3, r4), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified penta-reason and exception as a rejected resolution.
     *
     * @param r1 The 1st reason to be wrapped
     * @param r2 The 2nd reason to be wrapped
     * @param r3 The 3rd reason to be wrapped
     * @param r4 The 4th reason to be wrapped
     * @param r5 The 5th reason to be wrapped
     * @param e The exception to be wrapped
     * @return The fulfilled resolution
     */
    public static <V, R1, R2, R3, R4, R5> Resolution<V, M5<R1, R2, R3, R4, R5>>
    r(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5, final Throwable e)
    {
        return r(new M5<R1, R2, R3, R4, R5>(r1, r2, r3, r4, r5), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new promise fulfilled with {@code null}.
     *
     * @return The created promise
     */
    public static <V, R> Promise<V, R> pn()
    {
        return TypedPromiseImpl.<V, R>factory().fulfilledPromise(null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-value fulfilled promise.
     *
     * @param v The fulfilled value of the new promise
     * @return The created promise
     */
    public static <V, R> Promise<V, R> pf(final V v)
    {
        return TypedPromiseImpl.<V, R>factory().fulfilledPromise(v);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new dual-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @return The created promise
     */
    public static <V1, V2, R> Promise<M2<V1, V2>, R> pf(final V1 v1, final V2 v2)
    {
        return pf(new M2<V1, V2>(v1, v2));
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
    public static <V1, V2, V3, R> Promise<M3<V1, V2, V3>, R> pf(final V1 v1, final V2 v2, final V3 v3)
    {
        return pf(new M3<V1, V2, V3>(v1, v2, v3));
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
    public static <V1, V2, V3, V4, R> Promise<M4<V1, V2, V3, V4>, R>
    pf(final V1 v1, final V2 v2, final V3 v3, final V4 v4)
    {
        return pf(new M4<V1, V2, V3, V4>(v1, v2, v3, v4));
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
    public static <V1, V2, V3, V4, V5, R> Promise<M5<V1, V2, V3, V4, V5>, R>
    pf(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5)
    {
        return pf(new M5<V1, V2, V3, V4, V5>(v1, v2, v3, v4, v5));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-reason rejected promise with exception.
     *
     * @param r The rejected value of the new promise
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static <V, R> Promise<V, R> pr(final R r, final Throwable e)
    {
        return TypedPromiseImpl.<V, R>factory().rejectedPromise(r, e);
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
    public static <V, R1, R2> Promise<V, M2<R1, R2>> pr(final R1 r1, final R2 r2, final Throwable e)
    {
        return pr(new M2<R1, R2>(r1, r2), e);
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
    public static <V, R1, R2, R3> Promise<V, M3<R1, R2, R3>>
    pr(final R1 r1, final R2 r2, final R3 r3, final Throwable e)
    {
        return pr(new M3<R1, R2, R3>(r1, r2, r3), e);
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
    public static <V, R1, R2, R3, R4> Promise<V, M4<R1, R2, R3, R4>>
    pr(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final Throwable e)
    {
        return pr(new M4<R1, R2, R3, R4>(r1, r2, r3, r4), e);
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
    public static <V, R1, R2, R3, R4, R5> Promise<V, M5<R1, R2, R3, R4, R5>>
    pr(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5, final Throwable e)
    {
        return pr(new M5<R1, R2, R3, R4, R5>(r1, r2, r3, r4, r5), e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-reason rejected promise without exception.
     *
     * @param r The rejected value of the new promise
     * @return The created promise
     */
    public static <V, R> Promise<V, R> pr(final R r)
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
    public static <V, R1, R2> Promise<V, M2<R1, R2>> pr(final R1 r1, final R2 r2)
    {
        return pr(new M2<R1, R2>(r1, r2), null);
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
    public static <V, R1, R2, R3> Promise<V, M3<R1, R2, R3>> pr(final R1 r1, final R2 r2, final R3 r3)
    {
        return pr(new M3<R1, R2, R3>(r1, r2, r3), null);
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
    public static <V, R1, R2, R3, R4> Promise<V, M4<R1, R2, R3, R4>>
    pr(final R1 r1, final R2 r2, final R3 r3, final R4 r4)
    {
        return pr(new M4<R1, R2, R3, R4>(r1, r2, r3, r4), null);
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
    public static <V, R1, R2, R3, R4, R5> Promise<V, M5<R1, R2, R3, R4, R5>>
    pr(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5)
    {
        return pr(new M5<R1, R2, R3, R4, R5>(r1, r2, r3, r4, r5), null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new exception only rejected promise.
     *
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static <V, R> Promise<V, R> pr(final Throwable e)
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
    public static <VI1, VI2, VO, RO> OnFulfilled<M2<VI1, VI2>, VO, RO>
    wf(final FR2<? super VI1, ? super VI2, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return new OnFulfilled<M2<VI1, VI2>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO> call(final M2<VI1, VI2> v) throws Throwable {
                if (v != null)
                    return onFulfilled.call(v.v1, v.v2);
                else
                    return onFulfilled.call(null, null);
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
    public static <VI1, VI2, VI3, VO, RO> OnFulfilled<M3<VI1, VI2, VI3>, VO, RO>
    wf(final FR3<? super VI1, ? super VI2, ? super VI3, ? extends Resolution<? extends VO, ? extends RO>> onFulfilled)
    {
        return new OnFulfilled<M3<VI1, VI2, VI3>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M3<VI1, VI2, VI3> v) throws Throwable {
                if (v != null)
                    return onFulfilled.call(v.v1, v.v2, v.v3);
                else
                    return onFulfilled.call(null, null, null);
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
    public static <VI1, VI2, VI3, VI4, VO, RO> OnFulfilled<M4<VI1, VI2, VI3, VI4>, VO, RO> wf(
        final FR4<
            ? super VI1, ? super VI2, ? super VI3, ? super VI4,
            ? extends Resolution<? extends VO, ? extends RO>
        > onFulfilled
    ) {
        return new OnFulfilled<M4<VI1, VI2, VI3, VI4>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M4<VI1, VI2, VI3, VI4> v) throws Throwable {
                if (v != null)
                    return onFulfilled.call(v.v1, v.v2, v.v3, v.v4);
                else
                    return onFulfilled.call(null, null, null, null);
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
    public static <VI1, VI2, VI3, VI4, VI5, VO, RO> OnFulfilled<M5<VI1, VI2, VI3, VI4, VI5>, VO, RO> wf(
        final FR5<
            ? super VI1, ? super VI2, ? super VI3, ? super VI4, ? super VI5,
            ? extends Resolution<? extends VO, ? extends RO>
        > onFulfilled
    ) {
        return new OnFulfilled<M5<VI1, VI2, VI3, VI4, VI5>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M5<VI1, VI2, VI3, VI4, VI5> v) throws Throwable {
                if (v != null)
                    return onFulfilled.call(v.v1, v.v2, v.v3, v.v4, v.v5);
                else
                    return onFulfilled.call(null, null, null, null, null);
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
    public static <RI1, RI2, VO, RO> OnRejected<M2<RI1, RI2>, VO, RO>
    wr(final FR3<? super RI1, ? super RI2, Throwable, ? extends Resolution<? extends VO, ? extends RO>> onRejected)
    {
        return new OnRejected<M2<RI1, RI2>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M2<RI1, RI2> r, final Throwable e) throws Throwable {
                if (r != null)
                    return onRejected.call(r.v1, r.v2, e);
                else
                    return onRejected.call(null, null, e);
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
    public static <RI1, RI2, RI3, VO, RO> OnRejected<M3<RI1, RI2, RI3>, VO, RO> wr(
        final FR4<
            ? super RI1, ? super RI2, ? super RI3, Throwable,
            ? extends Resolution<? extends VO, ? extends RO>
        > onRejected
    ) {
        return new OnRejected<M3<RI1, RI2, RI3>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M3<RI1, RI2, RI3> r, final Throwable e) throws Throwable {
                if (r != null)
                    return onRejected.call(r.v1, r.v2, r.v3, e);
                else
                    return onRejected.call(null, null, null, e);
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
    public static <RI1, RI2, RI3, RI4, VO, RO> OnRejected<M4<RI1, RI2, RI3, RI4>, VO, RO> wr(
        final FR5<
            ? super RI1, ? super RI2, ? super RI3, ? super RI4, Throwable,
            ? extends Resolution<? extends VO, ? extends RO>
        > onRejected
    ) {
        return new OnRejected<M4<RI1, RI2, RI3, RI4>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M4<RI1, RI2, RI3, RI4> r, final Throwable e) throws Throwable {
                if (r != null)
                    return onRejected.call(r.v1, r.v2, r.v3, r.v4, e);
                else
                    return onRejected.call(null, null, null, null, e);
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
    public static <RI1, RI2, RI3, RI4, RI5, VO, RO> OnRejected<M5<RI1, RI2, RI3, RI4, RI5>, VO, RO> wr(
        final FR6<
            ? super RI1, ? super RI2, ? super RI3, ? super RI4, ? super RI5, Throwable,
            ? extends Resolution<? extends VO, ? extends RO>
        > onRejected
    ) {
        return new OnRejected<M5<RI1, RI2, RI3, RI4, RI5>, VO, RO>() {
            @Override public final Resolution<? extends VO, ? extends RO>
            call(final M5<RI1, RI2, RI3, RI4, RI5> r, final Throwable e) throws Throwable {
                if (r != null)
                    return onRejected.call(r.v1, r.v2, r.v3, r.v4, r.v5, e);
                else
                    return onRejected.call(null, null, null, null, null, e);
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
    public static <V, R> Promise<V, R> async(final Executor exec, final FR0<V> onExec)
    {
        return pf(null).then(
            exec,
            new OnFulfilled<Object, V, R>() {
                @Override public final Resolution<V, R> call(final Object dummy) throws Throwable {
                    return v(onExec.call());
                }
            }
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
