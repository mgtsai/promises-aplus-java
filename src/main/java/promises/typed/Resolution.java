//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.typed;
import promises.PromiseState;
import promises.lw.RV;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines resolutions.
 * <p/>
 * A resolution is an object which owns a fulfilled value, or rejected reason and exception used to resolve a target
 * promise, which can be a promise object, a thenable callback, or simply a value to fulfill a promise.
 *
 * @param <V> The value type
 * @param <R> The reason type
 */
public interface Resolution<V, R> extends RV<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the state of this resolution.
     *
     * @return The resolution state
     */
    public abstract PromiseState state();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected reason owned by this resolution.
     *
     * @return The rejected reason
     */
    public abstract R reason();
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the rejected exception owned by this resolution.
     *
     * @return The rejected exception
     */
    public abstract Throwable exception();
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
