//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.FR0;
import promises.FR2;
import promises.FR3;
import promises.FR4;
import promises.FR5;
import promises.M2;
import promises.M3;
import promises.M4;
import promises.M5;
import promises.impl.LightWeightPromiseImpl;
import java.util.concurrent.Executor;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines static methods for manipulating promise stuffs with LightWeight Style.
 */
public class Promises
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified value into a pure-value handler.
     *
     * @param v The value to be handled
     * @return The value handler
     */
    public static <V> RV<V> v(final V v)
    {
        return new RV<V>() { @Override public V value() {
            return v;
        }};
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Builds a dual-value tuple.
     *
     * @param v1 The 1st value
     * @param v2 The 2nd value
     * @return The tuple
     */
    public static <V1, V2> RV<M2<V1, V2>> v(final V1 v1, final V2 v2)
    {
        return v(M2.of(v1, v2));
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
    public static <V1, V2, V3> RV<M3<V1, V2, V3>> v(final V1 v1, final V2 v2, final V3 v3)
    {
        return v(M3.of(v1, v2, v3));
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
    public static <V1, V2, V3, V4> RV<M4<V1, V2, V3, V4>> v(final V1 v1, final V2 v2, final V3 v3, final V4 v4)
    {
        return v(M4.of(v1, v2, v3, v4));
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
    public static <V1, V2, V3, V4, V5> RV<M5<V1, V2, V3, V4, V5>>
    v(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5)
    {
        return v(M5.of(v1, v2, v3, v4, v5));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new promise fulfilled with {@code null}.
     *
     * @return The created promise
     */
    public static <V> P<V> pn()
    {
        return LightWeightPromiseImpl.<V>factory().fulfilledPromise(null);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new single-value fulfilled promise.
     *
     * @param v The fulfilled value of the new promise
     * @return The created promise
     */
    public static <V> P<V> pf(final V v)
    {
        return LightWeightPromiseImpl.<V>factory().fulfilledPromise(v);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new dual-value fulfilled promise.
     *
     * @param v1 The 1st fulfilled value of the new promise
     * @param v2 The 2nd fulfilled value of the new promise
     * @return The created promise
     */
    public static <V1, V2> P<M2<V1, V2>> pf(final V1 v1, final V2 v2)
    {
        return pf(M2.of(v1, v2));
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
    public static <V1, V2, V3> P<M3<V1, V2, V3>> pf(final V1 v1, final V2 v2, final V3 v3)
    {
        return pf(M3.of(v1, v2, v3));
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
    public static <V1, V2, V3, V4> P<M4<V1, V2, V3, V4>> pf(final V1 v1, final V2 v2, final V3 v3, final V4 v4)
    {
        return pf(M4.of(v1, v2, v3, v4));
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
    public static <V1, V2, V3, V4, V5> P<M5<V1, V2, V3, V4, V5>>
    pf(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5)
    {
        return pf(M5.of(v1, v2, v3, v4, v5));
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new rejected promise.
     *
     * @param e The rejected exception of the new promise
     * @return The created promise
     */
    public static <V> P<V> pr(final Throwable e)
    {
        return LightWeightPromiseImpl.<V>factory().rejectedPromise(null, e);
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified dual-argument {@code onFulfilled} callback to a single aggregated-argument callback.
     *
     * @param onFulfilled The dual-argument {@code onFulfilled} callback to be wrapped
     * @return The wrapper callback
     */
    public static <VI1, VI2, VO> OnFul<M2<VI1, VI2>, VO>
    wf(final FR2<? super VI1, ? super VI2, ? extends RV<? extends VO>> onFulfilled)
    {
        return new OnFul<M2<VI1, VI2>, VO>() {
            @Override public RV<? extends VO> call(final M2<VI1, VI2> v) throws Throwable {
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
    public static <VI1, VI2, VI3, VO> OnFul<M3<VI1, VI2, VI3>, VO>
    wf(final FR3<? super VI1, ? super VI2, ? super VI3, ? extends RV<? extends VO>> onFulfilled)
    {
        return new OnFul<M3<VI1, VI2, VI3>, VO>() {
            @Override public RV<? extends VO> call(final M3<VI1, VI2, VI3> v) throws Throwable {
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
    public static <VI1, VI2, VI3, VI4, VO> OnFul<M4<VI1, VI2, VI3, VI4>, VO>
    wf(final FR4<? super VI1, ? super VI2, ? super VI3, ? super VI4, ? extends RV<? extends VO>> onFulfilled)
    {
        return new OnFul<M4<VI1, VI2, VI3, VI4>, VO>() {
            @Override public RV<? extends VO> call(final M4<VI1, VI2, VI3, VI4> v) throws Throwable {
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
    public static <VI1, VI2, VI3, VI4, VI5, VO> OnFul<M5<VI1, VI2, VI3, VI4, VI5>, VO> wf(
        final FR5<
            ? super VI1, ? super VI2, ? super VI3, ? super VI4, ? super VI5,
            ? extends RV<? extends VO>
        > onFulfilled
    ) {
        return new OnFul<M5<VI1, VI2, VI3, VI4, VI5>, VO>() {
            @Override public RV<? extends VO> call(final M5<VI1, VI2, VI3, VI4, VI5> v) throws Throwable {
                if (v != null)
                    return onFulfilled.call(v.v1, v.v2, v.v3, v.v4, v.v5);
                else
                    return onFulfilled.call(null, null, null, null, null);
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
    public static <V> P<V> async(final Executor exec, final FR0<V> onExec)
    {
        return pf(null).then(
            exec,
            new OnFul<Object, V>() { @Override public RV<V> call(final Object dummy) throws Throwable {
                return v(onExec.call());
            }}
        );
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
