package com.wang.bookloading;

import android.content.Context;

/**
 * Created on 2017/8/23.
 * Author: wang
 */

public class ViewUtil {

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
