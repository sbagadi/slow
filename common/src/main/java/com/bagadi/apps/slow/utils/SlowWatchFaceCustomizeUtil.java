package com.bagadi.apps.slow.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.common.R;

/**
 * A utility class to help with setting and getting customization options.
 */
public class SlowWatchFaceCustomizeUtil {

    /**
     * Gets the icon resource ID for a given customization item name.
     *
     * @param customizeItemName the customization item name for which to get the resource ID. See
     *                          the {@link com.bagadi.apps.slow.common.Constants.CustomizeItemName}
     *                          for accepted values.
     * @return the {@link DrawableRes} ID corresponding the given customize item name.
     */
    @DrawableRes
    public static int getIconResourceIdForCustomizeItemName(
            @Constants.CustomizeItemName String customizeItemName) {
        switch (customizeItemName) {
            case Constants.CUSTOMIZE_ITEM_THEME:
            case Constants.CUSTOMIZE_ITEM_ACCENT:
                return R.drawable.ic_palette_white_24px;
            case Constants.CUSTOMIZE_ITEM_DATE_TIME:
                return R.drawable.ic_calender_clock_white_24px;
            default:
                return -1;
        }
    }

    /**
     * Gets the icon resource ID for a given theme name.\
     *
     * @param themeName the theme name for which to get the drawable resource ID. See the
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for accepted values.
     * @return the {@link DrawableRes} ID corresponding the given theme name.
     */
    @DrawableRes
    public static int getIconResourceIdForTheme(@Constants.ThemeName String themeName) {
        switch (themeName) {
            case Constants.THEME_DARK:
                return R.drawable.ic_background_dark_24dp;
            case Constants.THEME_LIGHT:
                return R.drawable.ic_background_light_24dp;
            default:
                return -1;
        }
    }

    /**
     * Gets the icon resource ID for a given accent color name.
     *
     * @param colorName the accent color name for which to get the drawable resource ID. See the
     *                  {@link com.bagadi.apps.slow.common.Constants.ColorName} for accepted values.
     * @return the {@link DrawableRes} ID corresponding the given color name.
     */
    @DrawableRes
    public static int getIconResourceIdForColorName(@Constants.ColorName String colorName) {
        switch (colorName) {
            case Constants.COLOR_RED:
                return R.drawable.ic_accent_red_circle_24dp;
            case Constants.COLOR_PINK:
                return R.drawable.ic_accent_pink_circle_24dp;
            case Constants.COLOR_PURPLE:
                return R.drawable.ic_accent_purple_circle_24dp;
            case Constants.COLOR_INDIGO:
                return R.drawable.ic_accent_indigo_circle_24dp;
            case Constants.COLOR_BLUE:
                return R.drawable.ic_accent_blue_circle_24dp;
            case Constants.COLOR_CYAN:
                return R.drawable.ic_accent_cyan_circle_24dp;
            case Constants.COLOR_TEAL:
                return R.drawable.ic_accent_teal_circle_24dp;
            case Constants.COLOR_GREEN:
                return R.drawable.ic_accent_green_circle_24dp;
            case Constants.COLOR_LIME:
                return R.drawable.ic_accent_lime_circle_24dp;
            case Constants.COLOR_YELLOW:
                return R.drawable.ic_accent_yellow_circle_24dp;
            case Constants.COLOR_AMBER:
                return R.drawable.ic_accent_amber_circle_24dp;
            case Constants.COLOR_ORANGE:
                return R.drawable.ic_accent_orange_circle_24dp;
            default:
                return -1;
        }
    }

    /**
     * Gets the background color int for a given theme name.
     *
     * @param context   an Android {@link Context} required to get the color int.
     * @param themeName The theme name for which to get the color int. See
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for accepted values.
     * @return the background color's {@link ColorInt} corresponding to the theme name provided.
     */
    @ColorInt
    public static int getBackgroundColorForThemeName(Context context,
                                                     @Constants.ThemeName String themeName) {
        switch (themeName) {
            case Constants.THEME_DARK:
                return ContextCompat.getColor(context, R.color.background_dark);
            case Constants.THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.background_light);
            default:
                return -1;
        }
    }

    /**
     * Gets the primary color int for a given theme name.
     *
     * @param context   an Android {@link Context} required to get the color int.
     * @param themeName The theme name for which to get the color int. See
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for accepted values.
     * @return the primary color's {@link ColorInt} corresponding to the theme name provided.
     */
    @ColorInt
    public static int getPrimaryColorForThemeName(Context context,
                                                  @Constants.ThemeName String themeName) {
        switch (themeName) {
            case Constants.THEME_DARK:
                return ContextCompat.getColor(context, R.color.primary_dark);
            case Constants.THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.primary_light);
            default:
                return -1;
        }
    }

    /**
     * Gets the secondary color int for a given theme name.
     *
     * @param context   an Android {@link Context} required to get the color int.
     * @param themeName The theme name for which to get the color int. See
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for accepted values.
     * @return the secondary color's {@link ColorInt} corresponding to the theme name provided.
     */
    @ColorInt
    public static int getSecondaryColorForThemeName(Context context,
                                                    @Constants.ThemeName String themeName) {
        switch (themeName) {
            case Constants.THEME_DARK:
                return ContextCompat.getColor(context, R.color.secondary_dark);
            case Constants.THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.secondary_light);
            default:
                return -1;
        }
    }

    /**
     * Gets the Accent color int for a given accent color name.
     *
     * @param context   an Android {@link Context} required to get the color int.
     * @param colorName The accent color name for which to get the color int. See
     *                  {@link com.bagadi.apps.slow.common.Constants.ColorName} for accepted values.
     * @return the accent color's {@link ColorInt} corresponding to the theme name provided.
     */
    @ColorInt
    public static int getAccentColorIdForColorName(Context context,
                                                   @Constants.ColorName String colorName) {
        switch (colorName) {
            case Constants.COLOR_RED:
                return ContextCompat.getColor(context, R.color.accent_red);
            case Constants.COLOR_PINK:
                return ContextCompat.getColor(context, R.color.accent_pink);
            case Constants.COLOR_PURPLE:
                return ContextCompat.getColor(context, R.color.accent_purple);
            case Constants.COLOR_INDIGO:
                return ContextCompat.getColor(context, R.color.accent_indigo);
            case Constants.COLOR_BLUE:
                return ContextCompat.getColor(context, R.color.accent_blue);
            case Constants.COLOR_CYAN:
                return ContextCompat.getColor(context, R.color.accent_cyan);
            case Constants.COLOR_TEAL:
                return ContextCompat.getColor(context, R.color.accent_teal);
            case Constants.COLOR_GREEN:
                return ContextCompat.getColor(context, R.color.accent_green);
            case Constants.COLOR_LIME:
                return ContextCompat.getColor(context, R.color.accent_lime);
            case Constants.COLOR_YELLOW:
                return ContextCompat.getColor(context, R.color.accent_yellow);
            case Constants.COLOR_AMBER:
                return ContextCompat.getColor(context, R.color.accent_amber);
            case Constants.COLOR_ORANGE:
                return ContextCompat.getColor(context, R.color.accent_orange);
            default:
                return -1;
        }
    }

    /**
     * Gets the date time preference display name for the given date time config name.
     *
     * @param context an Android {@link Context} required to get the string resource.
     * @param config  the date time config for which to get the config name. See
     *                {@link com.bagadi.apps.slow.common.Constants.ConfigShowDateTimeType} for
     *                accepted values.
     * @return a {@link String} representing the display name for the given date time config.
     */
    public static String getDateTimePreferenceNameForConfig(
            Context context,
            @Constants.ConfigShowDateTimeType int config) {
        switch (config) {
            case Constants.CONFIG_SHOW_NONE:
                return context.getString(R.string.show_none);
            case Constants.CONFIG_SHOW_DATE:
                return context.getString(R.string.show_date);
            case Constants.CONFIG_SHOW_TIME:
                return context.getString(R.string.show_time);
            case Constants.CONFIG_SHOW_DATE_TIME:
                return context.getString(R.string.show_date_time);
            default:
                return "";
        }
    }

    /**
     * Hiding the constructor. This is a utility class and need not be instantiated.
     */
    private SlowWatchFaceCustomizeUtil() { }
}
