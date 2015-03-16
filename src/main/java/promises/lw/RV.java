//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises.lw;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines value resolutions.
 * <p/>
 * A value resolution is an object which owns a fulfilled value in the {@code FULFILLED} state, which can be a promise
 * object, a thenable callback, or simply a value.
 *
 * @param <V> The value type
 */
public interface RV<V>
{
    //-----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the fulfilled value owned by this resolution.
     *
     * @return The fulfilled value
     */
    public abstract V value();
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
