//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
import promises.F2;
import promises.M2;
import promises.M3;
import promises.M4;
import promises.M5;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines thenable callbacks.
 *
 * @param <V> Type of fulfilled value for the resolved promise
 */
public abstract class Thenable<V> implements RV<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2> ResP2<V1, V2>
    resolvePromise2(final ResP<M2<? extends V1, ? extends V2>> resP)
    {
        return new ResP2<V1, V2>() {
            @Override public void resolve(final RV<? extends M2<? extends V1, ? extends V2>> res) {
                resP.resolve(res);
            }

            @Override public void resolve(final V1 v1, final V2 v2) {
                resP.resolve(new M2<V1, V2>(v1, v2));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3> ResP3<V1, V2, V3>
    resolvePromise3(final ResP<M3<? extends V1, ? extends V2, ? extends V3>> resP)
    {
        return new ResP3<V1, V2, V3>() {
            @Override public void resolve(final RV<? extends M3<? extends V1, ? extends V2, ? extends V3>> res) {
                resP.resolve(res);
            }

            @Override public void resolve(final V1 v1, final V2 v2, final V3 v3) {
                resP.resolve(new M3<V1, V2, V3>(v1, v2, v3));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3, V4> ResP4<V1, V2, V3, V4>
    resolvePromise4(final ResP<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>> resP)
    {
        return new ResP4<V1, V2, V3, V4>() {
            @Override public void
            resolve(final RV<? extends M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>> res) {
                resP.resolve(res);
            }

            @Override public void resolve(final V1 v1, final V2 v2, final V3 v3, final V4 v4) {
                resP.resolve(new M4<V1, V2, V3, V4>(v1, v2, v3, v4));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3, V4, V5> ResP5<V1, V2, V3, V4, V5>
    resolvePromise5(final ResP<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>> resP)
    {
        return new ResP5<V1, V2, V3, V4, V5>() {
            @Override public void
            resolve(final RV<? extends M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>> res) {
                resP.resolve(res);
            }

            @Override public void resolve(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5) {
                resP.resolve(new M5<V1, V2, V3, V4, V5>(v1, v2, v3, v4, v5));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V> Thenable<V>
    of(final F2<ResP<V>, RejP> then)
    {
        return new Thenable<V>() {
            @Override public void then(final ResP<V> resP, final RejP rejP) throws Throwable {
                then.call(resP, rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as dual-value {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2>
    Thenable<M2<? extends V1, ? extends V2>>
    of2(final F2<ResP2<V1, V2>, RejP> then)
    {
        return new Thenable<M2<? extends V1, ? extends V2>>() {
            @Override public void then(
                final ResP<M2<? extends V1, ? extends V2>> resP,
                final RejP rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3>
    Thenable<M3<? extends V1, ? extends V2, ? extends V3>>
    of3(final F2<ResP3<V1, V2, V3>, RejP> then)
    {
        return new Thenable<M3<? extends V1, ? extends V2, ? extends V3>>() {
            @Override public void then(
                final ResP<M3<? extends V1, ? extends V2, ? extends V3>> resP,
                final RejP rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4>
    Thenable<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>>
    of4(final F2<ResP4<V1, V2, V3, V4>, RejP> then)
    {
        return new Thenable<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>>() {
            @Override public void then(
                final ResP<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>> resP,
                final RejP rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as penta-value single-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, V5>
    Thenable<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>>
    of5(final F2<ResP5<V1, V2, V3, V4, V5>, RejP> then)
    {
        return new Thenable<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>>() {
            @Override public void then(
                final ResP<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>> resP,
                final RejP rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * The {@code then} method.
     *
     * @param resolvePromise Being invoked when resolving the target promise
     * @param rejectPromise Being invoked when rejecting the target promise
     * @throws Throwable The throwable thrown by this invocation, and this throwable would be the rejection reason of
     *      the target promise
     */
    public abstract void then(final ResP<V> resolvePromise, final RejP rejectPromise) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V value()
    {
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
