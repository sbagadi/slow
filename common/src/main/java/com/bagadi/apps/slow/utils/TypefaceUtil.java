package com.bagadi.apps.slow.utils;

import android.content.Context;
import android.graphics.Typeface;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.common.R;

/**
 * A utility class to help with fonts and typefaces.
 */
public class TypefaceUtil {

    /**
     * Gets a {@link TypefaceUtil} for the given typeface name.
     * @param context an Android {@link Context} required to get the string resource.
     * @param typefaceName a {@link String} representing the typeface name. See
     *                     {@link com.bagadi.apps.slow.common.Constants.TypefaceName} for accepted
     *                     values.
     * @return a {@link Typeface} corresponding to the typeface name provided.
     */
    public static Typeface getTypefaceForName(Context context,
                                               @Constants.TypefaceName String typefaceName) {
        switch (typefaceName) {
            case Constants.TYPEFACE_ROBOTO_REGULAR:
                return Typeface.createFromAsset(
                        context.getAssets(),
                        context.getString(R.string.font_roboto_regular));
            case Constants.TYPEFACE_ROBOTO_THIN:
                return Typeface.createFromAsset(
                        context.getAssets(),
                        context.getString(R.string.font_roboto_thin));
            case Constants.TYPEFACE_ROBOTO_LIGHT:
                return Typeface.createFromAsset(
                        context.getAssets(),
                        context.getString(R.string.font_roboto_light));
            case Constants.TYPEFACE_ROBOTO_BOLD:
                return Typeface.createFromAsset(
                        context.getAssets(),
                        context.getString(R.string.font_roboto_bold));
            default:
                return Typeface.DEFAULT;
        }
    }

    /**
     * Hiding the constructor. This is a utility class and need not be instantiated.
     */
    private TypefaceUtil() { }
}
