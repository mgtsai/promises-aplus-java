//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import java.io.Serializable;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines penta-value tuples.
 *
 * @param <T1> Type of 1st value
 * @param <T2> Type of 2nd value
 * @param <T3> Type of 3rd value
 * @param <T4> Type of 4th value
 * @param <T5> Type of 5th value
 */
public final class M5<T1, T2, T3, T4, T5> implements Serializable
{
    //-----------------------------------------------------------------------------------------------------------------
    public final T1 v1;
    public final T2 v2;
    public final T3 v3;
    public final T4 v4;
    public final T5 v5;
    //-----------------------------------------------------------------------------------------------------------------
    public M5(final T1 v1, final T2 v2, final T3 v3, final T4 v4, final T5 v5)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final String toString()
    {
        return String.format("(%s,%s,%s,%s,%s)", v1, v2, v3, v4, v5);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
