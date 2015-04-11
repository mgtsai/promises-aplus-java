//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines thenable callbacks.
 */
public abstract class Thenable
{
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvePromise2 resolvePromise2(final ResolvePromise resP)
    {
        return new ResolvePromise2() {
            @Override public void resolve(final Object v) { resP.resolve(v); }

            @Override public void resolve(final Object v1, final Object v2) {
                resP.resolve(Promises.v(v1, v2));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvePromise3 resolvePromise3(final ResolvePromise resP)
    {
        return new ResolvePromise3() {
            @Override public void resolve(final Object v) { resP.resolve(v); }

            @Override public void resolve(final Object v1, final Object v2, final Object v3) {
                resP.resolve(Promises.v(v1, v2, v3));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvePromise4 resolvePromise4(final ResolvePromise resP)
    {
        return new ResolvePromise4() {
            @Override public void resolve(final Object v) { resP.resolve(v); }

            @Override public void resolve(final Object v1, final Object v2, final Object v3, final Object v4) {
                resP.resolve(Promises.v(v1, v2, v3, v4));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static ResolvePromise5 resolvePromise5(final ResolvePromise resP)
    {
        return new ResolvePromise5() {
            @Override public void resolve(final Object v) { resP.resolve(v); }

            @Override public void
            resolve(final Object v1, final Object v2, final Object v3, final Object v4, final Object v5) {
                resP.resolve(Promises.v(v1, v2, v3, v4, v5));
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static RejectPromise2 rejectPromise2(final RejectPromise rejP)
    {
        return new RejectPromise2() {
            @Override public void reject(final Object r1, final Object r2, final Throwable e) {
                rejP.reject(Promises.v(r1, r2), e);
            }

            @Override public void reject(final Object r1, final Object r2) {
                rejP.reject(Promises.v(r1, r2));
            }

            @Override public void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static RejectPromise3 rejectPromise3(final RejectPromise rejP)
    {
        return new RejectPromise3() {
            @Override public void reject(final Object r1, final Object r2, final Object r3, final Throwable e) {
                rejP.reject(Promises.v(r1, r2, r3), e);
            }

            @Override public void reject(final Object r1, final Object r2, final Object r3) {
                rejP.reject(Promises.v(r1, r2, r3));
            }

            @Override public void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static RejectPromise4 rejectPromise4(final RejectPromise rejP)
    {
        return new RejectPromise4() {
            @Override public void
            reject(final Object r1, final Object r2, final Object r3, final Object r4, final Throwable e) {
                rejP.reject(Promises.v(r1, r2, r3, r4), e);
            }

            @Override public void reject(final Object r1, final Object r2, final Object r3, final Object r4) {
                rejP.reject(Promises.v(r1, r2, r3, r4));
            }

            @Override public void reject(final Throwable e) {
                rejP.reject(e);
            }
        };
    }
    //-----------------------------------------------------------------------------------------------------------------
    private static RejectPromise5 rejectPromise5(final RejectPromise rejP)
    {
        return new RejectPromise5() {
            @Override public void reject(
                final Object r1,
                final Object r2,
                final Object r3,
                final Object r4,
                final Object r5,
                final Throwable e
            ) {
                rejP.reject(Promises.v(r1, r2, r3, r4, r5), e);
            }

            @Override public void
            reject(final Object r1, final Object r2, final Object r3, final Object r4, final Object r5) {
                rejP.reject(Promises.v(r1, r2, r3, r4, r5));
            }

            @Override public void reject(final Throwable e) {
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
    public static Thenable of(final F2<ResolvePromise, RejectPromise> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_1_2(final F2<ResolvePromise, RejectPromise2> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_1_3(final F2<ResolvePromise, RejectPromise3> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_1_4(final F2<ResolvePromise, RejectPromise4> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_1_5(final F2<ResolvePromise, RejectPromise5> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_2_1(final F2<ResolvePromise2, RejectPromise> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_2_2(final F2<ResolvePromise2, RejectPromise2> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_2_3(final F2<ResolvePromise2, RejectPromise3> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_2_4(final F2<ResolvePromise2, RejectPromise4> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_2_5(final F2<ResolvePromise2, RejectPromise5> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_3_1(final F2<ResolvePromise3, RejectPromise> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_3_2(final F2<ResolvePromise3, RejectPromise2> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_3_3(final F2<ResolvePromise3, RejectPromise3> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_3_4(final F2<ResolvePromise3, RejectPromise4> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_3_5(final F2<ResolvePromise3, RejectPromise5> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_4_1(final F2<ResolvePromise4, RejectPromise> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_4_2(final F2<ResolvePromise4, RejectPromise2> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_4_3(final F2<ResolvePromise4, RejectPromise3> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_4_4(final F2<ResolvePromise4, RejectPromise4> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_4_5(final F2<ResolvePromise4, RejectPromise5> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_5_1(final F2<ResolvePromise5, RejectPromise> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_5_2(final F2<ResolvePromise5, RejectPromise2> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_5_3(final F2<ResolvePromise5, RejectPromise3> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_5_4(final F2<ResolvePromise5, RejectPromise4> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public static Thenable of_5_5(final F2<ResolvePromise5, RejectPromise5> then)
    {
        return new Thenable() {
            @Override public void then(final ResolvePromise resP, final RejectPromise rejP) throws Throwable {
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
    public abstract void then(final ResolvePromise resolvePromise, final RejectPromise rejectPromise) throws Throwable;
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
