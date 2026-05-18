package com.example.educationgame.data;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

public final class ProcessColorGenerator {

    private ProcessColorGenerator() {}

    public static int[] generate(Context context, int count) {
        boolean isDark = (context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int[] colors = new int[count];
        float saturation = isDark ? 0.70f : 0.65f;
        float lightness = isDark ? 0.65f : 0.55f;

        for (int i = 0; i < count; i++) {
            float hue = (360f * i / count + 15f) % 360f;
            colors[i] = Color.HSVToColor(new float[]{hue, saturation, lightness});
        }
        return colors;
    }
}
