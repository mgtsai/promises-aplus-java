//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
import java.io.Serializable;
//---------------------------------------------------------------------------------------------------------------------
/**
 * Defines quad-value tuples.
 *
 * @param <T1> Type of 1st value
 * @param <T2> Type of 2nd value
 * @param <T3> Type of 3rd value
 * @param <T4> Type of 4th value
 */
public final class M4<T1, T2, T3, T4> implements Serializable
{
    //-----------------------------------------------------------------------------------------------------------------
    public final T1 v1;
    public final T2 v2;
    public final T3 v3;
    public final T4 v4;
    //-----------------------------------------------------------------------------------------------------------------
    public static <T1, T2, T3, T4> M4<T1, T2, T3, T4> of(final T1 v1, final T2 v2, final T3 v3, final T4 v4)
    {
        return new M4<T1, T2, T3, T4>(v1, v2, v3, v4);
    }
    //-----------------------------------------------------------------------------------------------------------------
    private M4(final T1 v1, final T2 v2, final T3 v3, final T4 v4)
    {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
    }
    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public final String toString()
    {
        return String.format("(%s,%s,%s,%s)", v1, v2, v3, v4);
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
