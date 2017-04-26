package com.bagadi.apps.slow.common;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Contains all the common constants used by both the wear and mobile modules.
 */
public class Constants {

    public static final String PATH_WITH_FEATURE = "/watch_face_config/slow";

    public static final String KEY_THEME_NAME = "theme_name";

    public static final String KEY_ACCENT_COLOR = "accent_color";

    public static final String KEY_DATE_TIME = "date_time";

    /**
     * Customization items for the Slow app.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CUSTOMIZE_ITEM_THEME, CUSTOMIZE_ITEM_ACCENT, CUSTOMIZE_ITEM_DATE_TIME})

    public @interface CustomizeItemName {}
    public static final String CUSTOMIZE_ITEM_THEME = "Theme";
    public static final String CUSTOMIZE_ITEM_ACCENT = "Accent";
    public static final String CUSTOMIZE_ITEM_DATE_TIME = "Date/Time";

    /**
     * Themes that the Slow app.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({THEME_DARK, THEME_LIGHT})

    public @interface ThemeName {}
    public static final String THEME_DARK = "Dark";
    public static final String THEME_LIGHT = "Light";

    /**
     * Accent color names for the Slow app.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({COLOR_AMBER,
            COLOR_BLUE,
            COLOR_CYAN,
            COLOR_GREEN,
            COLOR_INDIGO,
            COLOR_LIME,
            COLOR_ORANGE,
            COLOR_PINK,
            COLOR_PURPLE,
            COLOR_RED,
            COLOR_TEAL,
            COLOR_YELLOW})

    public @interface ColorName {}
    public static final String COLOR_AMBER = "Amber";
    public static final String COLOR_BLUE = "Blue";
    public static final String COLOR_CYAN = "Cyan";
    public static final String COLOR_GREEN = "Green";
    public static final String COLOR_INDIGO = "Indigo";
    public static final String COLOR_LIME = "Lime";
    public static final String COLOR_ORANGE = "Orange";
    public static final String COLOR_PINK = "Pink";
    public static final String COLOR_PURPLE = "Purple";
    public static final String COLOR_RED = "Red";
    public static final String COLOR_TEAL = "Teal";
    public static final String COLOR_YELLOW = "Yellow";

    /**
     * Date-time preferences for the Slow app.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONFIG_SHOW_NONE, CONFIG_SHOW_DATE, CONFIG_SHOW_TIME, CONFIG_SHOW_DATE_TIME})

    public @interface ConfigShowDateTimeType {}
    public static final int CONFIG_SHOW_NONE = 101;
    public static final int CONFIG_SHOW_DATE = 102;
    public static final int CONFIG_SHOW_TIME = 103;
    public static final int CONFIG_SHOW_DATE_TIME = 104;

    /**
     * Typefaces that can be used.
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPEFACE_ROBOTO_REGULAR,
            TYPEFACE_ROBOTO_THIN,
            TYPEFACE_ROBOTO_LIGHT,
            TYPEFACE_ROBOTO_BOLD})

    public @interface TypefaceName {}
    public static final String TYPEFACE_ROBOTO_REGULAR = "roboto-regular";
    public static final String TYPEFACE_ROBOTO_THIN = "roboto-thin";
    public static final String TYPEFACE_ROBOTO_LIGHT = "roboto-light";
    public static final String TYPEFACE_ROBOTO_BOLD = "roboto-bold";

    @ThemeName
    public static final String THEME_NAME_DEFAULT = Constants.THEME_DARK;

    @ColorName
    public static final String ACCENT_COLOR_NAME_DEFAULT = Constants.COLOR_AMBER;

    @ConfigShowDateTimeType
    public static final int SHOW_DATE_TIME_DEFAULT = Constants.CONFIG_SHOW_NONE;

    /**
     * Hiding the constructor.
     */
    private Constants() { };
}
