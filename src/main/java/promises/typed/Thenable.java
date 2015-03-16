//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.F2;
import promises.M2;
import promises.M3;
import promises.M4;
import promises.M5;
import promises.PromiseState;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines thenable callbacks.
 *
 * @param <V> Type of fulfilled value for the resolved promise
 * @param <R> Type of rejection reason for the resolved promise
 */
public abstract class Thenable<V, R> implements Resolution<V, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, R> ResolvePromise2<V1, V2, R>
    resolvePromise2(final ResolvePromise<M2<? extends V1, ? extends V2>, R> resP)
    {
        return new ResolvePromise2<V1, V2, R>() {
            @Override public final void resolve(
                final Resolution<
                    ? extends M2<? extends V1, ? extends V2>,
                    ? extends R
                > res
            ) {
                resP.resolve(res);
            }

            @Override public final void resolve(final V1 v1, final V2 v2) {
                resP.resolve(new M2<V1, V2>(v1, v2));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3, R> ResolvePromise3<V1, V2, V3, R>
    resolvePromise3(final ResolvePromise<M3<? extends V1, ? extends V2, ? extends V3>, R> resP)
    {
        return new ResolvePromise3<V1, V2, V3, R>() {
            @Override public final void resolve(
                final Resolution<
                    ? extends M3<? extends V1, ? extends V2, ? extends V3>,
                    ? extends R
                > res
            ) {
                resP.resolve(res);
            }

            @Override public final void resolve(final V1 v1, final V2 v2, final V3 v3) {
                resP.resolve(new M3<V1, V2, V3>(v1, v2, v3));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3, V4, R> ResolvePromise4<V1, V2, V3, V4, R>
    resolvePromise4(final ResolvePromise<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>, R> resP)
    {
        return new ResolvePromise4<V1, V2, V3, V4, R>() {
            @Override public final void resolve(
                final Resolution<
                    ? extends M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
                    ? extends R
                > res
            ) {
                resP.resolve(res);
            }

            @Override public final void resolve(final V1 v1, final V2 v2, final V3 v3, final V4 v4) {
                resP.resolve(new M4<V1, V2, V3, V4>(v1, v2, v3, v4));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <V1, V2, V3, V4, V5, R> ResolvePromise5<V1, V2, V3, V4, V5, R>
    resolvePromise5(
        final ResolvePromise<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>, R> resP
    ) {
        return new ResolvePromise5<V1, V2, V3, V4, V5, R>() {
            @Override public final void resolve(
                final Resolution<
                    ? extends M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
                    ? extends R
                > res
            ) {
                resP.resolve(res);
            }

            @Override public final void resolve(final V1 v1, final V2 v2, final V3 v3, final V4 v4, final V5 v5) {
                resP.resolve(new M5<V1, V2, V3, V4, V5>(v1, v2, v3, v4, v5));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <R1, R2> RejectPromise2<R1, R2>
    rejectPromise2(final RejectPromise<M2<? extends R1, ? extends R2>> rejP)
    {
        return new RejectPromise2<R1, R2>() {
            @Override public final void reject(final R1 r1, final R2 r2, final Throwable e) {
                rejP.reject(new M2<R1, R2>(r1, r2), e);
            }

            @Override public final void reject(final R1 r1, final R2 r2) {
                rejP.reject(new M2<R1, R2>(r1, r2));
            }

            @Override public final void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <R1, R2, R3> RejectPromise3<R1, R2, R3>
    rejectPromise3(final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP)
    {
        return new RejectPromise3<R1, R2, R3>() {
            @Override public final void reject(final R1 r1, final R2 r2, final R3 r3, final Throwable e) {
                rejP.reject(new M3<R1, R2, R3>(r1, r2, r3), e);
            }

            @Override public final void reject(final R1 r1, final R2 r2, final R3 r3) {
                rejP.reject(new M3<R1, R2, R3>(r1, r2, r3));
            }

            @Override public final void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <R1, R2, R3, R4> RejectPromise4<R1, R2, R3, R4>
    rejectPromise4(final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP)
    {
        return new RejectPromise4<R1, R2, R3, R4>() {
            @Override public final void reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final Throwable e) {
                rejP.reject(new M4<R1, R2, R3, R4>(r1, r2, r3, r4), e);
            }

            @Override public final void reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4) {
                rejP.reject(new M4<R1, R2, R3, R4>(r1, r2, r3, r4));
            }

            @Override public final void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static <R1, R2, R3, R4, R5> RejectPromise5<R1, R2, R3, R4, R5>
    rejectPromise5(final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP)
    {
        return new RejectPromise5<R1, R2, R3, R4, R5>() {
            @Override public final void
            reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5, final Throwable e) {
                rejP.reject(new M5<R1, R2, R3, R4, R5>(r1, r2, r3, r4, r5), e);
            }

            @Override public final void reject(final R1 r1, final R2 r2, final R3 r3, final R4 r4, final R5 r5) {
                rejP.reject(new M5<R1, R2, R3, R4, R5>(r1, r2, r3, r4, r5));
            }

            @Override public final void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value single-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V, R> Thenable<V, R>
    of(final F2<ResolvePromise<V, R>, RejectPromise<R>> then)
    {
        return new Thenable<V, R>() {
            @Override public final void
            then(final ResolvePromise<V, R> resP, final RejectPromise<R> rejP) throws Throwable {
                then.call(resP, rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value dual-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V, R1, R2>
    Thenable<V, M2<? extends R1, ? extends R2>>
    of_1_2(
        final F2<
            ResolvePromise<V, M2<? extends R1, ? extends R2>>,
            RejectPromise2<R1, R2>
        > then
    ) {
        return new Thenable<V, M2<? extends R1, ? extends R2>>() {
            @Override public final void then(
                final ResolvePromise<V, M2<? extends R1, ? extends R2>> resP,
                final RejectPromise<M2<? extends R1, ? extends R2>> rejP
            ) throws Throwable {
                then.call(resP, rejectPromise2(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value triple-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V, R1, R2, R3>
    Thenable<V, M3<? extends R1, ? extends R2, ? extends R3>>
    of_1_3(
        final F2<
            ResolvePromise<V, M3<? extends R1, ? extends R2, ? extends R3>>,
            RejectPromise3<R1, R2, R3>
        > then
    ) {
        return new Thenable<V, M3<? extends R1, ? extends R2, ? extends R3>>() {
            @Override public final void then(
                final ResolvePromise<V, M3<? extends R1, ? extends R2, ? extends R3>> resP,
                final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP
            ) throws Throwable {
                then.call(resP, rejectPromise3(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value quad-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V, R1, R2, R3, R4>
    Thenable<V, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>
    of_1_4(
        final F2<
            ResolvePromise<V, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>,
            RejectPromise4<R1, R2, R3, R4>
        > then
    ) {
        return new Thenable<V, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>() {
            @Override public final void then(
                final ResolvePromise<V, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> resP,
                final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP
            ) throws Throwable {
                then.call(resP, rejectPromise4(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as single-value penta-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V, R1, R2, R3, R4, R5>
    Thenable<V, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>
    of_1_5(
        final F2<
            ResolvePromise<V, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>,
            RejectPromise5<R1, R2, R3, R4, R5>
        > then
    ) {
        return new Thenable<V, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>() {
            @Override public final void then(
                final ResolvePromise<V, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> resP,
                final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP
            ) throws Throwable {
                then.call(resP, rejectPromise5(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as dual-value single-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, R>
    Thenable<M2<? extends V1, ? extends V2>, R>
    of_2_1(final F2<ResolvePromise2<V1, V2, R>, RejectPromise<R>> then)
    {
        return new Thenable<M2<? extends V1, ? extends V2>, R>() {
            @Override public final void then(
                final ResolvePromise<M2<? extends V1, ? extends V2>, R> resP,
                final RejectPromise<R> rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as double-value double-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, R1, R2>
    Thenable<
        M2<? extends V1, ? extends V2>,
        M2<? extends R1, ? extends R2>
    > of_2_2(
        final F2<
            ResolvePromise2<V1, V2, M2<? extends R1, ? extends R2>>,
            RejectPromise2<R1, R2>
        > then
    ) {
        return new Thenable<
            M2<? extends V1, ? extends V2>,
            M2<? extends R1, ? extends R2>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M2<? extends V1, ? extends V2>,
                    M2<? extends R1, ? extends R2>
                > resP,
                final RejectPromise<M2<? extends R1, ? extends R2>> rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejectPromise2(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as double-value triple-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, R1, R2, R3>
    Thenable<
        M2<? extends V1, ? extends V2>,
        M3<? extends R1, ? extends R2, ? extends R3>
    > of_2_3(
        final F2<
            ResolvePromise2<V1, V2, M3<? extends R1, ? extends R2, ? extends R3>>,
            RejectPromise3<R1, R2, R3>
        > then
    ) {
        return new Thenable<
            M2<? extends V1, ? extends V2>,
            M3<? extends R1, ? extends R2, ? extends R3>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M2<? extends V1, ? extends V2>,
                    M3<? extends R1, ? extends R2, ? extends R3>
                > resP,
                final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejectPromise3(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as double-value quad-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, R1, R2, R3, R4>
    Thenable<
        M2<? extends V1, ? extends V2>,
        M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
    > of_2_4(
        final F2<
            ResolvePromise2<V1, V2, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>,
            RejectPromise4<R1, R2, R3, R4>
        > then
    ) {
        return new Thenable<
            M2<? extends V1, ? extends V2>,
            M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M2<? extends V1, ? extends V2>,
                    M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
                > resP,
                final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejectPromise4(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as double-value penta-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, R1, R2, R3, R4, R5>
    Thenable<
        M2<? extends V1, ? extends V2>,
        M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
    > of_2_5(
        final F2<
            ResolvePromise2<V1, V2, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>,
            RejectPromise5<R1, R2, R3, R4, R5>
        > then
    ) {
        return new Thenable<
            M2<? extends V1, ? extends V2>,
            M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M2<? extends V1, ? extends V2>,
                    M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
                > resP,
                final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP
            ) throws Throwable {
                then.call(resolvePromise2(resP), rejectPromise5(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value single-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, R>
    Thenable<M3<? extends V1, ? extends V2, ? extends V3>, R>
    of_3_1(final F2<ResolvePromise3<V1, V2, V3, R>, RejectPromise<R>> then)
    {
        return new Thenable<M3<? extends V1, ? extends V2, ? extends V3>, R>() {
            @Override public final void then(
                final ResolvePromise<M3<? extends V1, ? extends V2, ? extends V3>, R> resP,
                final RejectPromise<R> rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value dual-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, R1, R2>
    Thenable<
        M3<? extends V1, ? extends V2, ? extends V3>,
        M2<? extends R1, ? extends R2>
    > of_3_2(
        final F2<
            ResolvePromise3<V1, V2, V3, M2<? extends R1, ? extends R2>>,
            RejectPromise2<R1, R2>
        > then
    ) {
        return new Thenable<
            M3<? extends V1, ? extends V2, ? extends V3>,
            M2<? extends R1, ? extends R2>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M3<? extends V1, ? extends V2, ? extends V3>,
                    M2<? extends R1, ? extends R2>
                > resP,
                final RejectPromise<M2<? extends R1, ? extends R2>> rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejectPromise2(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value triple-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, R1, R2, R3>
    Thenable<
        M3<? extends V1, ? extends V2, ? extends V3>,
        M3<? extends R1, ? extends R2, ? extends R3>
    > of_3_3(
        final F2<
            ResolvePromise3<V1, V2, V3, M3<? extends R1, ? extends R2, ? extends R3>>,
            RejectPromise3<R1, R2, R3>
        > then
    ) {
        return new Thenable<
            M3<? extends V1, ? extends V2, ? extends V3>,
            M3<? extends R1, ? extends R2, ? extends R3>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M3<? extends V1, ? extends V2, ? extends V3>,
                    M3<? extends R1, ? extends R2, ? extends R3>
                > resP,
                final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejectPromise3(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value quad-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, R1, R2, R3, R4>
    Thenable<
        M3<? extends V1, ? extends V2, ? extends V3>,
        M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
    > of_3_4(
        final F2<
            ResolvePromise3<V1, V2, V3, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>,
            RejectPromise4<R1, R2, R3, R4>
        > then
    ) {
        return new Thenable<
            M3<? extends V1, ? extends V2, ? extends V3>,
            M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M3<? extends V1, ? extends V2, ? extends V3>,
                    M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
                > resP,
                final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejectPromise4(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as triple-value penta-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, R1, R2, R3, R4, R5>
    Thenable<
        M3<? extends V1, ? extends V2, ? extends V3>,
        M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
    > of_3_5(
        final F2<
            ResolvePromise3<V1, V2, V3, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>,
            RejectPromise5<R1, R2, R3, R4, R5>
        > then
    ) {
        return new Thenable<
            M3<? extends V1, ? extends V2, ? extends V3>,
            M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M3<? extends V1, ? extends V2, ? extends V3>,
                    M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
                > resP,
                final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP
            ) throws Throwable {
                then.call(resolvePromise3(resP), rejectPromise5(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value single-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, R>
    Thenable<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>, R>
    of_4_1(final F2<ResolvePromise4<V1, V2, V3, V4, R>, RejectPromise<R>> then)
    {
        return new Thenable<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>, R>() {
            @Override public final void then(
                final ResolvePromise<M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>, R> resP,
                final RejectPromise<R> rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value dual-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, R1, R2>
    Thenable<
        M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
        M2<? extends R1, ? extends R2>
    > of_4_2(
        final F2<
            ResolvePromise4<V1, V2, V3, V4, M2<? extends R1, ? extends R2>>,
            RejectPromise2<R1, R2>
        > then
    ) {
        return new Thenable<
            M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
            M2<? extends R1, ? extends R2>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
                    M2<? extends R1, ? extends R2>
                > resP,
                final RejectPromise<M2<? extends R1, ? extends R2>> rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejectPromise2(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value triple-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, R1, R2, R3>
    Thenable<
        M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
        M3<? extends R1, ? extends R2, ? extends R3>
    > of_4_3(
        final F2<
            ResolvePromise4<V1, V2, V3, V4, M3<? extends R1, ? extends R2, ? extends R3>>,
            RejectPromise3<R1, R2, R3>
        > then
    ) {
        return new Thenable<
            M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
            M3<? extends R1, ? extends R2, ? extends R3>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
                    M3<? extends R1, ? extends R2, ? extends R3>
                > resP,
                final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejectPromise3(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value quad-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, R1, R2, R3, R4>
    Thenable<
        M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
        M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
    > of_4_4(
        final F2<
            ResolvePromise4<V1, V2, V3, V4, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>,
            RejectPromise4<R1, R2, R3, R4>
        > then
    ) {
        return new Thenable<
            M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
            M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
                    M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
                > resP,
                final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejectPromise4(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as quad-value penta-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, R1, R2, R3, R4, R5>
    Thenable<
        M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
        M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
    > of_4_5(
        final F2<
            ResolvePromise4<V1, V2, V3, V4, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>,
            RejectPromise5<R1, R2, R3, R4, R5>
        > then
    ) {
        return new Thenable<
            M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
            M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M4<? extends V1, ? extends V2, ? extends V3, ? extends V4>,
                    M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
                > resP,
                final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP
            ) throws Throwable {
                then.call(resolvePromise4(resP), rejectPromise5(rejP));
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
    public static <V1, V2, V3, V4, V5, R>
    Thenable<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>, R>
    of_5_1(final F2<ResolvePromise5<V1, V2, V3, V4, V5, R>, RejectPromise<R>> then)
    {
        return new Thenable<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>, R>()
        {
            @Override public final void then(
                final ResolvePromise<M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>, R> resP,
                final RejectPromise<R> rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejP);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as penta-value dual-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, V5, R1, R2>
    Thenable<
        M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
        M2<? extends R1, ? extends R2>
    > of_5_2(
        final F2<
            ResolvePromise5<V1, V2, V3, V4, V5, M2<? extends R1, ? extends R2>>,
            RejectPromise2<R1, R2>
        > then
    ) {
        return new Thenable<
            M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
            M2<? extends R1, ? extends R2>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
                    M2<? extends R1, ? extends R2>
                > resP,
                final RejectPromise<M2<? extends R1, ? extends R2>> rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejectPromise2(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as penta-value triple-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, V5, R1, R2, R3>
    Thenable<
        M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
        M3<? extends R1, ? extends R2, ? extends R3>
    > of_5_3(
        final F2<
            ResolvePromise5<V1, V2, V3, V4, V5, M3<? extends R1, ? extends R2, ? extends R3>>,
            RejectPromise3<R1, R2, R3>
        > then
    ) {
        return new Thenable<
            M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
            M3<? extends R1, ? extends R2, ? extends R3>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
                    M3<? extends R1, ? extends R2, ? extends R3>
                > resP,
                final RejectPromise<M3<? extends R1, ? extends R2, ? extends R3>> rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejectPromise3(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as penta-value quad-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, V5, R1, R2, R3, R4>
    Thenable<
        M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
        M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
    > of_5_4(
        final F2<
            ResolvePromise5<V1, V2, V3, V4, V5, M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>>,
            RejectPromise4<R1, R2, R3, R4>
        > then
    ) {
        return new Thenable<
            M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
            M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
                    M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>
                > resP,
                final RejectPromise<M4<? extends R1, ? extends R2, ? extends R3, ? extends R4>> rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejectPromise4(rejP));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Wraps the specified {@code then} function as penta-value penta-reason {@link Thenable} object.
     *
     * @param then The {@code then} function to be wrapped
     * @return The thenable object
     */
    public static <V1, V2, V3, V4, V5, R1, R2, R3, R4, R5>
    Thenable<
        M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
        M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
    > of_5_5(
        final F2<
            ResolvePromise5<V1, V2, V3, V4, V5, M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>>,
            RejectPromise5<R1, R2, R3, R4, R5>
        > then
    ) {
        return new Thenable<
            M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
            M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
        >() {
            @Override public final void then(
                final ResolvePromise<
                    M5<? extends V1, ? extends V2, ? extends V3, ? extends V4, ? extends V5>,
                    M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>
                > resP,
                final RejectPromise<M5<? extends R1, ? extends R2, ? extends R3, ? extends R4, ? extends R5>> rejP
            ) throws Throwable {
                then.call(resolvePromise5(resP), rejectPromise5(rejP));
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
    public abstract void then(final ResolvePromise<V, R> resolvePromise, final RejectPromise<R> rejectPromise)
        throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final PromiseState state()
    {
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final V value()
    {
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final R reason()
    {
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final Throwable exception()
    {
        return null;
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
