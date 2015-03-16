//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines single-value {@code resolvePromise} callbacks.
 *
 * @param <V> Type of fulfilled value
 * @param <R> Type of rejection reason
 */
public interface ResolvePromise<V, R>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param res The resolution which the target promise would be resolved with
     */
    public abstract void resolve(final Resolution<? extends V, ? extends R> res);
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Resolves the target promise.
     *
     * @param v The value which the target promise would be fulfilled with
     */
    public abstract void resolve(final V v);
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
