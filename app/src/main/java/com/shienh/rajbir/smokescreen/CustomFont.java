package com.shienh.rajbir.smokescreen;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

public class CustomFont {
    public static void replaceDefaultFont(Context context, String defaultFont, String newFont) {
        Typeface customFontTypeFace = Typeface.createFromAsset(context.getAssets(), newFont);
        replaceFont(defaultFont, customFontTypeFace);
    }

    private static void replaceFont(String defaultFont, Typeface customFontTypeFace) {
        try {
            Field myField = Typeface.class.getDeclaredField(defaultFont);
            myField.setAccessible(true);
            myField.set(null, customFontTypeFace);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
