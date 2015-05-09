//---------------------------------------------------------------------------------------------------------------------
// Copyright (C) 2015-2020, Joseph M. G. Tsai
// under the terms of the Apache License, Version 2.0 (ALv2),
// found at http://www.apache.org/licenses/LICENSE-2.0
//---------------------------------------------------------------------------------------------------------------------
package promises;
//---------------------------------------------------------------------------------------------------------------------
public final class TestData
{
    //-----------------------------------------------------------------------------------------------------------------
    public static Object[][] rows(final Object[][] paramsList, final int... indexes)
    {
        final Object[][] rowsParamsList = new Object[indexes.length][];

        for (int i = 0; i < indexes.length; ++i)
            rowsParamsList[i] = paramsList[indexes[i]];

        return rowsParamsList;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object[][] union(final Object[][]... paramsLists)
    {
        int cnt = 0;
        for (final Object[][] paramsList : paramsLists)
            cnt += paramsList.length;

        final Object[][] unionParamsList = new Object[cnt][];

        int pos = 0;
        for (final Object[][] paramsList : paramsLists) {
            System.arraycopy(paramsList, 0, unionParamsList, pos, paramsList.length);
            pos += paramsList.length;
        }

        return unionParamsList;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object[][] merge(final Object[][]... paramsLists)
    {
        int cnt = 0;
        for (final Object[][] paramsList : paramsLists)
            cnt = Math.max(cnt, paramsList.length);

        final Object[][] mergeParamsList = new Object[cnt][];
        final int[] indexes = new int[paramsLists.length];

        for (int i = 0; i < mergeParamsList.length; ++i) {
            int len = 0;
            for (int j = 0; j < paramsLists.length; ++j)
                len += paramsLists[j][indexes[j]].length;

            mergeParamsList[i] = new Object[len];

            for (int j = 0, pos = 0; j < paramsLists.length; ++j) {
                final Object[] params = paramsLists[j][indexes[j]];
                System.arraycopy(params, 0, mergeParamsList[i], pos, params.length);
                pos += params.length;

                if (++indexes[j] >= paramsLists[j].length)
                    indexes[j] = 0;
            }
        }

        return mergeParamsList;
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static Object[][] product(final Object[][]... paramsLists)
    {
        int cnt = 1;
        for (final Object[][] paramsList : paramsLists)
            cnt *= paramsList.length;

        final Object[][] productParamsList = new Object[cnt][];
        final int[] indexes = new int[paramsLists.length];

        for (int i = 0; i < productParamsList.length; ++i) {
            int len = 0;
            for (int j = 0; j < paramsLists.length; ++j)
                len += paramsLists[j][indexes[j]].length;

            productParamsList[i] = new Object[len];

            for (int j = 0, pos = 0; j < paramsLists.length; ++j) {
                final Object[] params = paramsLists[j][indexes[j]];
                System.arraycopy(params, 0, productParamsList[i], pos, params.length);
                pos += params.length;
            }

            for (int j = paramsLists.length - 1; j >= 0; --j) {
                if (++indexes[j] < paramsLists[j].length)
                    break;
                indexes[j] = 0;
            }
        }

        return productParamsList;
    }
    //-----------------------------------------------------------------------------------------------------------------
}
//---------------------------------------------------------------------------------------------------------------------
