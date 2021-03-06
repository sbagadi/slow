package com.bagadi.apps.slow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.utils.SlowWatchFaceCustomizeUtil;
import com.bagadi.apps.slow.utils.TypefaceUtil;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A custom view showing the preview of the Slow watch face.
 */
public class SlowWatchFaceView extends View {

    /* All the Paint instances needed to for drawing the watch face.*/
    private Paint mBackgroundPaint;
    private Paint mSecondTickPaint;
    private Paint mMinuteTickPaint;
    private Paint mHourTickPaint;
    private Paint mSmallHourPaint;
    private Paint mHandPaint;
    private Paint mWidgetsPaint;

    /**
     * Flag to determine whether or not the watch face is round.
     */
    private boolean mIsRound = true;

    private int mBackgroundPaintColor = SlowWatchFaceCustomizeUtil.getBackgroundColorForThemeName(
            getContext(),
            Constants.THEME_NAME_DEFAULT);
    private int mPrimaryColor = SlowWatchFaceCustomizeUtil.getPrimaryColorForThemeName(
            getContext(),
            Constants.THEME_NAME_DEFAULT);
    private int mSecondaryColor = SlowWatchFaceCustomizeUtil.getSecondaryColorForThemeName(
            getContext(),
            Constants.THEME_NAME_DEFAULT);
    private int mAccentPaintColor = SlowWatchFaceCustomizeUtil.getAccentColorIdForColorName(
            getContext(),
            Constants.ACCENT_COLOR_NAME_DEFAULT);

    @Constants.ConfigShowDateTimeType int mShowDateTime = Constants.SHOW_DATE_TIME_DEFAULT;

    /**
     * Center X coordinate of the Canvas.
     */
    private int mCenterX;

    /**
     * Center Y coordinate of the Canvas.
     */
    private int mCenterY;

    private Calendar mCalendar;

    /**
     * A {@link BroadcastReceiver} used for receiving time zone change events using
     * {@link #mCalendar}.
     */
    final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCalendar.setTimeZone(TimeZone.getDefault());
            invalidate();
        }
    };

    public SlowWatchFaceView(Context context) {
        super(context);
        if (!isInEditMode()) {
            init();
        }
    }

    public SlowWatchFaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    public SlowWatchFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            init();
        }
    }

    public SlowWatchFaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            init();
        }
    }

    /**
     * Initializes the {@link Typeface} and {@link Paint} widgets needed for drawing the watch face.
     */
    private void init() {

        Resources resources = getContext().getResources();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundPaintColor);

        Typeface robotoBold =
                TypefaceUtil.getTypefaceForName(getContext(), Constants.TYPEFACE_ROBOTO_BOLD);
        Typeface robotoRegular =
                TypefaceUtil.getTypefaceForName(getContext(), Constants.TYPEFACE_ROBOTO_REGULAR);
        Typeface robotoThin =
                TypefaceUtil.getTypefaceForName(getContext(), Constants.TYPEFACE_ROBOTO_THIN);

        mSecondTickPaint = new Paint();
        mSecondTickPaint.setColor(mSecondaryColor);
        mSecondTickPaint.setStrokeWidth(2f);
        mSecondTickPaint.setAntiAlias(true);
        mSecondTickPaint.setStrokeCap(Paint.Cap.ROUND);

        mMinuteTickPaint = new Paint();
        mMinuteTickPaint.setColor(mSecondaryColor);
        mMinuteTickPaint.setStrokeWidth(3f);
        mMinuteTickPaint.setAntiAlias(true);
        mMinuteTickPaint.setStrokeCap(Paint.Cap.ROUND);

        mHourTickPaint = new Paint();
        mHourTickPaint.setColor(mPrimaryColor);
        mHourTickPaint.setAntiAlias(true);
        mHourTickPaint.setTextSize(resources.getDimension(R.dimen.watch_hour_number_font));
        mHourTickPaint.setTextAlign(Paint.Align.CENTER);
        mHourTickPaint.setTypeface(robotoBold);

        mSmallHourPaint = new Paint();
        mSmallHourPaint.setColor(mPrimaryColor);
        mSmallHourPaint.setAntiAlias(true);
        mSmallHourPaint.setTextSize(resources.getDimension(R.dimen.small_hour_font));
        mSmallHourPaint.setTextAlign(Paint.Align.CENTER);
        mSmallHourPaint.setTypeface(robotoRegular);

        mHandPaint = new Paint();
        mHandPaint.setColor(mAccentPaintColor);
        mHandPaint.setStrokeWidth(resources.getDimension(R.dimen.analog_hand_stroke));
        mHandPaint.setAntiAlias(true);
        mHandPaint.setStrokeCap(Paint.Cap.ROUND);

        mWidgetsPaint = new Paint();
        mWidgetsPaint.setColor(mSecondaryColor);
        mWidgetsPaint.setStrokeWidth(resources.getDimension(R.dimen.widget_stroke));
        mWidgetsPaint.setAntiAlias(true);
        mWidgetsPaint.setStrokeCap(Paint.Cap.ROUND);
        mWidgetsPaint.setStyle(Paint.Style.STROKE);
        mWidgetsPaint.setTypeface(robotoThin);
        mWidgetsPaint.setTextSize(resources.getDimension(R.dimen.widget_font));
        mWidgetsPaint.setTextAlign(Paint.Align.CENTER);
        mWidgetsPaint.setLetterSpacing(0.15f);

        mCalendar = Calendar.getInstance();
    }

    /**
     * Sets whether or not the preview should be round.
     */
    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }

    /**
     * Sets the theme for the watch face by updating the background, primary and secondary colors.
     *
     * @param themeName the theme name to be set. See
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for acceptable
     *                  values.
     */
    public void setTheme(@Constants.ThemeName String themeName) {
        mBackgroundPaintColor =
                SlowWatchFaceCustomizeUtil.getBackgroundColorForThemeName(
                        getContext(),
                        themeName);
        setPrimaryColor(
                SlowWatchFaceCustomizeUtil.getPrimaryColorForThemeName(getContext(), themeName));
        setSecondaryColor(
                SlowWatchFaceCustomizeUtil.getSecondaryColorForThemeName(getContext(), themeName));
        updateWatchHandStyle();
    }

    /**
     * Sets the primary color for this watch face.
     */
    private void setPrimaryColor(int color) {
        mPrimaryColor = color;
    }

    /**
     * Sets the secondary color for this watch face.
     */
    private void setSecondaryColor(int color) {
        mSecondaryColor = color;
    }

    /**
     * Sets the accent color for this watch face.
     *
     * @param colorName the color name to be set.  See the
     *                  {@link com.bagadi.apps.slow.common.Constants.ColorName} for acceptable
     *                  values.
     */
    public void setAccentColor(@Constants.ColorName String colorName) {
        mAccentPaintColor = SlowWatchFaceCustomizeUtil
                .getAccentColorIdForColorName(getContext(), colorName);
        updateWatchHandStyle();
    }

    /**
     * Sets the date time widget preferences.
     *
     * @param preference the preference value to be set. See the
     *                   {@link com.bagadi.apps.slow.common.Constants.ConfigShowDateTimeType}
     *                   for acceptable values.
     */
    public void setShowDateTime(@Constants.ConfigShowDateTimeType int preference) {
        mShowDateTime = preference;
    }

    /**
     * Updates all the paint objects to their corresponding colors and invalidates the view. Call
     * this after a preference change.
     */
    private void updateWatchHandStyle() {
        mHandPaint.setColor(mAccentPaintColor);
        mSecondTickPaint.setColor(mSecondaryColor);
        mMinuteTickPaint.setColor(mSecondaryColor);
        mHourTickPaint.setColor(mPrimaryColor);
        mBackgroundPaint.setColor(mBackgroundPaintColor);
        mWidgetsPaint.setColor(mSecondaryColor);
        mSmallHourPaint.setColor(mSecondaryColor);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mCenterX = width / 2;
        mCenterY = height / 2;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInEditMode()) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            if (mIsRound) {
                onDrawRound(canvas);
            } else {
                onDrawSquare(canvas);
            }

            // Get the half height of the mWidgetsPaint to correctly center the date time text.
            float widgetPaintHalfTextHeight =
                    (mWidgetsPaint.descent() + mWidgetsPaint.ascent()) / 2;
            // Draw the date time widget on the center of the canvas.
            drawDateTime(canvas, mCenterX, mCenterY - widgetPaintHalfTextHeight);

            drawSecondsWidget(canvas);

            // TODO: Use a timer to update every second, instead of invalidating.
            invalidate();
        }
    }

    /**
     * Draws the round watch face on a given canvas.
     * TODO: Some of the calculations can be done in {@link #onSizeChanged(int, int, int, int)} to
     * improve efficiency.
     *
     * @param canvas a {@link Canvas} instance to draw on.
     */
    private void onDrawRound(Canvas canvas) {

        canvas.drawCircle(mCenterX, mCenterX, canvas.getWidth() / 2, mBackgroundPaint);

        /*
         * Draw ticks. Usually you will want to bake this directly into the photo, but in
         * cases where you want to allow users to select their own photos, this dynamically
         * creates them on top of the photo.
         */
        float outerTickRadius = mCenterX * 0.85f;
        float innerNumberRadius = mCenterX * 0.70f;
        float paintHalfTextHeight = (mHourTickPaint.descent() + mHourTickPaint.ascent()) / 2;
        float smallHourHalfTextHeight =
                (mSmallHourPaint.descent() + mSmallHourPaint.ascent()) / 2;
        float hourTickRadius = mCenterX * 0.020f;
        float halfHourTickRadius = mCenterX * 0.010f;
        for (int tickIndex = 0; tickIndex < 48; tickIndex++) {
            float tickRot = (float) (tickIndex * Math.PI * 2 / 48);
            float outerTickX = (float) Math.sin(tickRot) * outerTickRadius;
            float outerTickY = (float) -Math.cos(tickRot) * outerTickRadius;
            float innerNumberX = (float) Math.sin(tickRot) * innerNumberRadius;
            float innerNumberY = (float) -Math.cos(tickRot) * innerNumberRadius;
            if (tickIndex % 2 == 0) {
                if (tickIndex % 3 == 0) {
                    canvas.drawText(
                            String.format("%02d", (tickIndex / 2)),
                            mCenterX - outerTickX,
                            mCenterY - outerTickY - paintHalfTextHeight,
                            mHourTickPaint);
                } else {
                    canvas.drawText(
                            String.format("%02d", (tickIndex / 2)),
                            mCenterX - outerTickX,
                            mCenterY - outerTickY - smallHourHalfTextHeight,
                            mSmallHourPaint);
                }
                canvas.drawCircle(mCenterX - innerNumberX,
                        mCenterY - innerNumberY,
                        hourTickRadius,
                        mHourTickPaint);
            } else {
                canvas.drawCircle(mCenterX - innerNumberX,
                        mCenterY - innerNumberY,
                        halfHourTickRadius,
                        mMinuteTickPaint);
            }
        }

        final float timeHandRotation = ((mCalendar.get(Calendar.HOUR_OF_DAY) + 12) * 15)
                + (mCalendar.get(Calendar.MINUTE) * 0.25f);
            /*
            * Save the canvas state before we can begin to rotate it.
            */
        canvas.save();

        canvas.rotate(timeHandRotation, mCenterX, mCenterY);
        float triangleHalfLength = (float) ((mCenterX * 0.1) / 2);
        Path path = new Path();
        path.moveTo(mCenterX, mCenterY * 0.35f);
        path.lineTo(mCenterX - triangleHalfLength, mCenterY * 0.45f);
        path.lineTo(mCenterX + triangleHalfLength, mCenterY * 0.45f);
        path.lineTo(mCenterX, mCenterY * 0.35f);
        canvas.drawPath(path, mHandPaint);

            /* Restore the canvas' original orientation. */
        canvas.restore();
    }

    /**
     * Draws the square watch face on a given canvas.
     * TODO: Some of the calculations can be done in {@link #onSizeChanged(int, int, int, int)} to
     * improve efficiency.
     *
     * @param canvas a {@link Canvas} instance to draw on.
     */
    private void onDrawSquare(Canvas canvas) {

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
        /*
         * Draw ticks. Usually you will want to bake this directly into the photo, but in
         * cases where you want to allow users to select their own photos, this dynamically
         * creates them on top of the photo.
             */
        float tickOffset = mCenterX * 0.30f;
        float numberOffset = mCenterX * 0.15f;
        float paintHalfTextHeight =
                (mHourTickPaint.descent() + mHourTickPaint.ascent()) / 2;
        float smallHourHalfTextHeight =
                (mSmallHourPaint.descent() + mSmallHourPaint.ascent()) / 2;
        float squareTickSize = mCenterX * 0.04f;

        float numberSpace = (canvas.getWidth() - (2 * tickOffset)) / 6.0f;
        float squareSpace = (canvas.getWidth() - (2 * tickOffset)) / 6.0f;
        for (int tickIdx = 0; tickIdx < 6; tickIdx++) {

            /*
             * Naming: each of the variable is used twice in each of the drawRect call.
             * The two parts of the name show where the variables are being used.
             *
             * In each of the part, the first value denotes in which drawRect function the
             * value is being used, the second value denotes the coordinate of the drawRect
             * function.
             *
             * Eg: in topLeft_rightTop is used in top and right ticks denoted by the first
             * parts of the two values. the value is used as the left coordinate in top tick
             * and as top coordinate in right tick.
             */
            float topTop_leftLeft = tickOffset - (squareTickSize / 2.0f);
            float topLeft_rightTop = topTop_leftLeft + ((float) tickIdx * squareSpace);
            float topRight_rightBottom = topLeft_rightTop + squareTickSize;
            float topBottom_leftRight = topTop_leftLeft + squareTickSize;
            float rightLeft_bottomTop = topTop_leftLeft + (6.0f * squareSpace);
            float rightRight_bottomBottom = rightLeft_bottomTop + squareTickSize;
            float bottomLeft_leftTop = topTop_leftLeft + ((6.0f - (float) tickIdx) * squareSpace);
            float bottomRight_leftBottom = bottomLeft_leftTop + squareTickSize;

            // Draws top ticks
            canvas.drawRect(topLeft_rightTop,
                    topTop_leftLeft,
                    topRight_rightBottom,
                    topBottom_leftRight,
                    mHourTickPaint);

            // Draws right ticks
            canvas.drawRect(rightLeft_bottomTop,
                    topLeft_rightTop,
                    rightRight_bottomBottom,
                    topRight_rightBottom,
                    mHourTickPaint);

            // Draws bottom ticks
            canvas.drawRect(bottomLeft_leftTop,
                    rightLeft_bottomTop,
                    bottomRight_leftBottom,
                    rightRight_bottomBottom,
                    mHourTickPaint);

            // Draws left ticks
            canvas.drawRect(topTop_leftLeft,
                    bottomLeft_leftTop,
                    topBottom_leftRight,
                    bottomRight_leftBottom,
                    mHourTickPaint);

            float topX = numberOffset;
            float topY = topX - paintHalfTextHeight;
            float topYSmall = numberOffset - smallHourHalfTextHeight;
            float rightX = (float) canvas.getWidth() - numberOffset;
            float bottomY = rightX - paintHalfTextHeight;
            float bottomYSmall = rightX - smallHourHalfTextHeight;

            if (tickIdx == 0) {
                // Need to get the 4 corners.

                canvas.drawText("09", topX, topY, mHourTickPaint);

                canvas.drawText("15", rightX, topY, mHourTickPaint);

                canvas.drawText("21", rightX, bottomY, mHourTickPaint);

                canvas.drawText("03", topX, bottomY, mHourTickPaint);
            } else if (tickIdx == 3) {
                canvas.drawText("12", mCenterX, topY, mHourTickPaint);
                canvas.drawText("18", rightX, mCenterY - paintHalfTextHeight, mHourTickPaint);
                canvas.drawText("00", mCenterX, bottomY, mHourTickPaint);
                canvas.drawText("06", topX, mCenterY - paintHalfTextHeight, mHourTickPaint);
            } else {
                int topNumber = tickIdx + 9;
                int rightNumber = tickIdx + 15;
                int bottomNumber = tickIdx + 21 < 24 ? tickIdx + 21 : tickIdx - 3;
                int leftNumber = tickIdx + 3;

                float startDist = tickOffset + (numberSpace * tickIdx);
                float endDist = canvas.getWidth() - startDist;

                canvas.drawText(String.format(Locale.getDefault(), "%02d", topNumber),
                        startDist,
                        topYSmall,
                        mSmallHourPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%02d", rightNumber),
                        rightX,
                        startDist - smallHourHalfTextHeight,
                        mSmallHourPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%02d", bottomNumber),
                        endDist,
                        bottomYSmall,
                        mSmallHourPaint);
                canvas.drawText(String.format(Locale.getDefault(), "%02d", leftNumber),
                        topX,
                        endDist - smallHourHalfTextHeight,
                        mSmallHourPaint);
            }
        }

        float halfHourSquareTickSize = squareTickSize * 0.5f;
        float halfHourTickOffset = tickOffset + (squareSpace / 2);

        for (int tickIdx = 0; tickIdx < 6; tickIdx++) {
            /*
             * Naming: each of the variable is used twice in each of the drawRect call.
             * The two parts of the name show where the variables are being used.
             *
             * In each of the part, the first value denotes in which drawRect function the
             * value is being used, the second value denotes the coordinate of the drawRect
             * function.
             *
             * Eg: in topLeft_rightTop is used in top and right ticks denoted by the first
             * parts of the two values. the value is used as the left coordinate in top tick
             * and as top coordinate in right tick.
             */
            float topTop_leftLeft = tickOffset - (halfHourSquareTickSize / 2.0f);
            float topLeft_rightTop = halfHourTickOffset
                    + (tickIdx * squareSpace)
                    - (halfHourSquareTickSize / 2.0f);
            float topRight_rightBottom = topLeft_rightTop + halfHourSquareTickSize;
            float topBottom_leftRight = topTop_leftLeft + halfHourSquareTickSize;
            float rightLeft_bottomTop =
                    tickOffset + (6 * squareSpace) - (halfHourSquareTickSize / 2);
            float rightRight_bottomBottom = rightLeft_bottomTop + halfHourSquareTickSize;
            float bottomLeft_leftTop = halfHourTickOffset
                    + ((5 - tickIdx) * squareSpace)
                    - (halfHourSquareTickSize / 2);
            float bottomRight_leftBottom = bottomLeft_leftTop + halfHourSquareTickSize;

            // Draws top ticks
            canvas.drawRect(topLeft_rightTop,
                    topTop_leftLeft,
                    topRight_rightBottom,
                    topBottom_leftRight,
                    mHourTickPaint);

            // Draws right ticks
            canvas.drawRect(rightLeft_bottomTop,
                    topLeft_rightTop,
                    rightRight_bottomBottom,
                    topRight_rightBottom,
                    mHourTickPaint);

            // Draws bottom ticks
            canvas.drawRect(bottomLeft_leftTop,
                    rightLeft_bottomTop,
                    bottomRight_leftBottom,
                    rightRight_bottomBottom,
                    mHourTickPaint);

            // Draws left ticks
            canvas.drawRect(topTop_leftLeft,
                    bottomLeft_leftTop,
                    topBottom_leftRight,
                    bottomRight_leftBottom,
                    mHourTickPaint);
        }

        float timeHandXStartPos;
        float timeHandYStartPos;

        float handLength = mCenterX * 0.1f;
        float triangleHalfLength = handLength / 2;
        float handStart = mCenterX * 0.35f;

        int hourOfDay = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        float distanceBetweenMinutes =
                (canvas.getWidth() - (2 * tickOffset)) / (6 * 60);

        Path handPath = new Path();

        if (hourOfDay >= 0 && hourOfDay < 3) {
            timeHandYStartPos = canvas.getHeight() - handStart;
            canvas.getHeight();

            timeHandXStartPos =
                    (((((2 - hourOfDay) * 60) + (60 - minute)) * distanceBetweenMinutes)
                            + tickOffset);

            handPath.moveTo(timeHandXStartPos, timeHandYStartPos);
            handPath.lineTo(timeHandXStartPos - triangleHalfLength,
                    timeHandYStartPos - handLength);
            handPath.lineTo(timeHandXStartPos + triangleHalfLength,
                    timeHandYStartPos - handLength);
            handPath.lineTo(timeHandXStartPos, timeHandYStartPos);
        } else if (hourOfDay >= 3 && hourOfDay < 9) {
            timeHandXStartPos = handStart;
            timeHandYStartPos =
                    ((((8 - hourOfDay) * 60) + (60 - minute)) * distanceBetweenMinutes)
                            + tickOffset;

            handPath.moveTo(timeHandXStartPos, timeHandYStartPos);
            handPath.lineTo(timeHandXStartPos + handLength,
                    timeHandYStartPos - triangleHalfLength);
            handPath.lineTo(timeHandXStartPos + handLength,
                    timeHandYStartPos + triangleHalfLength);
            handPath.lineTo(timeHandXStartPos, timeHandYStartPos);
        } else if (hourOfDay >= 9 && hourOfDay < 15) {
            timeHandYStartPos = handStart;
            timeHandXStartPos = ((((hourOfDay - 9) * 60) + (minute)) * distanceBetweenMinutes)
                    + tickOffset;

            handPath.moveTo(timeHandXStartPos, timeHandYStartPos);
            handPath.lineTo(timeHandXStartPos - triangleHalfLength,
                    timeHandYStartPos + handLength);
            handPath.lineTo(timeHandXStartPos + triangleHalfLength,
                    timeHandYStartPos + handLength);
            handPath.lineTo(timeHandXStartPos, timeHandYStartPos);
        } else if (hourOfDay >= 15 && hourOfDay < 21) {
            timeHandXStartPos = canvas.getWidth() - handStart;
            timeHandYStartPos = ((((hourOfDay - 15) * 60) + (minute)) * distanceBetweenMinutes)
                    + tickOffset;

            handPath.moveTo(timeHandXStartPos, timeHandYStartPos);
            handPath.lineTo(timeHandXStartPos - handLength,
                    timeHandYStartPos - triangleHalfLength);
            handPath.lineTo(timeHandXStartPos - handLength,
                    timeHandYStartPos + triangleHalfLength);
            handPath.lineTo(timeHandXStartPos, timeHandYStartPos);
        } else {
            timeHandYStartPos = canvas.getHeight() - handStart;
            timeHandXStartPos =
                    ((((26 - hourOfDay) * 60) + (60 - minute)) * distanceBetweenMinutes)
                            + tickOffset;

            handPath.moveTo(timeHandXStartPos, timeHandYStartPos);
            handPath.lineTo(timeHandXStartPos - triangleHalfLength,
                    timeHandYStartPos - handLength);
            handPath.lineTo(timeHandXStartPos + triangleHalfLength,
                    timeHandYStartPos - handLength);
            handPath.lineTo(timeHandXStartPos, timeHandYStartPos);
        }

        canvas.drawPath(handPath, mHandPaint);
    }

    /**
     * Draws the date-time widget in the position provided based on the {@link #mShowDateTime}
     * preference value.
     *
     * @param canvas  a {@link Canvas} on which to draw the date time.
     * @param centerX the center X coordinate of the widget to be drawn.
     * @param centerY the center Y coordinate of the widget to be drawn.
     */
    private void drawDateTime(Canvas canvas, float centerX, float centerY) {
        if (mShowDateTime != Constants.CONFIG_SHOW_NONE && mShowDateTime != 0) {
            int hour = (mCalendar.get(Calendar.HOUR) == 0
                    && mCalendar.get(Calendar.AM_PM) == Calendar.PM)
                    ? 12 : mCalendar.get(Calendar.HOUR);
            String dateTime;
            switch (mShowDateTime) {
                case Constants.CONFIG_SHOW_DATE:
                    dateTime = String.format(
                            Locale.getDefault(),
                            "%02d %s",
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                            mCalendar.getDisplayName(
                                    Calendar.DAY_OF_WEEK,
                                    Calendar.SHORT,
                                    Locale.getDefault()));
                    break;
                case Constants.CONFIG_SHOW_TIME:
                    dateTime = String.format(Locale.getDefault(),
                            "%02d:%02d %s",
                            hour,
                            mCalendar.get(Calendar.MINUTE),
                            mCalendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
                    break;
                case Constants.CONFIG_SHOW_DATE_TIME:
                    dateTime = String.format(
                            Locale.getDefault(),
                            "%02d %s  %02d:%02d %s",
                            mCalendar.get(Calendar.DAY_OF_MONTH),
                            mCalendar.getDisplayName(
                                    Calendar.DAY_OF_WEEK,
                                    Calendar.SHORT,
                                    Locale.getDefault()),
                            hour,
                            mCalendar.get(Calendar.MINUTE),
                            mCalendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
                    break;
                default:
                    dateTime = "";
            }

            canvas.drawText(dateTime, centerX, centerY, mWidgetsPaint);
        }
    }

    /**
     * Draws the seconds widget.
     * TODO: Some of the calculations can be done in {@link #onSizeChanged(int, int, int, int)} to
     * improve efficiency.
     *
     * @param canvas a {@link Canvas} on which to draw the seconds widget.
     */
    private void drawSecondsWidget(Canvas canvas) {
        float widgetOffset = 0.33f;
        float widgetRadius = mCenterX * 0.20f;

        // Draw ticks for the seconds dial.
        float secondsCenterX = mCenterX;
        float secondsCenterY = mCenterY * widgetOffset;
        float innerSecTickRadius = mCenterX * 0.15f;
        for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
            float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
            // Note: These calculations are offset to the center. When drawing the line, we
            // add these  values to mCenterX and mCenterY values.
            float innerX = (float) Math.sin(tickRot) * innerSecTickRadius;
            float innerY =
                    ((float) -Math.cos(tickRot) * innerSecTickRadius) + secondsCenterY;
            float outerX = (float) Math.sin(tickRot) * widgetRadius;
            float outerY =
                    ((float) -Math.cos(tickRot) * widgetRadius) + secondsCenterY;
            canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                    mCenterX + outerX, mCenterY + outerY, mMinuteTickPaint);
        }

                /*
                 * These calculations reflect the rotation in degrees per unit of time, e.g.,
                 * 360 / 60 = 6 and 360 / 12 = 30.
                 */
        final float seconds =
                (mCalendar.get(Calendar.SECOND)
                        + mCalendar.get(Calendar.MILLISECOND) / 1000f);
        float secondsRotation = (float) (seconds * Math.PI * 2 / 60);
        float secondsTickRotRight = (float) (secondsRotation + (Math.PI / 2));
        float secondsTickRotLeft = (float) (secondsTickRotRight + Math.PI);
        float secondsTickBackRotation = (float) (secondsRotation + Math.PI);
        float secondsHandLength = mCenterX * 0.15f;
        float secondsHandMaxWidth = secondsHandLength * 0.1f;
        float secondsHandBackLength = secondsHandLength * 0.2f;

        // Similar to the ticks, these values are offset to the center of the canvas.
        float secondsHandPosX = (float) Math.sin(secondsRotation) * secondsHandLength;
        float secondsHandPosY =
                (float) (-Math.cos(secondsRotation) * secondsHandLength) + secondsCenterY;

        float secondsHandRightX =
                (float) Math.sin(secondsTickRotRight) * secondsHandMaxWidth;
        float secondsHandRightY =
                (float) (-Math.cos(secondsTickRotRight) * secondsHandMaxWidth)
                        + secondsCenterY;

        float secondsHandLeftX =
                (float) Math.sin(secondsTickRotLeft) * secondsHandMaxWidth;
        float secondsHandLeftY =
                (float) (-Math.cos(secondsTickRotLeft) * secondsHandMaxWidth)
                        + secondsCenterY;

        float secondsHandBackX =
                (float) Math.sin(secondsTickBackRotation) * secondsHandBackLength;
        float secondsHandBackY =
                (float) (-Math.cos(secondsTickBackRotation) * secondsHandBackLength)
                        + secondsCenterY;

        Path path = new Path();
        path.moveTo(mCenterX + secondsHandPosX, mCenterY + secondsHandPosY);
        path.lineTo(mCenterX + secondsHandRightX, mCenterY + secondsHandRightY);
        path.lineTo(mCenterX + secondsHandBackX, mCenterY + secondsHandBackY);
        path.lineTo(mCenterX + secondsHandLeftX, mCenterY + secondsHandLeftY);
        path.close();

        canvas.drawPath(path, mHandPaint);
        canvas.drawCircle(secondsCenterX, mCenterY + secondsCenterY, 1, mBackgroundPaint);
    }
}
