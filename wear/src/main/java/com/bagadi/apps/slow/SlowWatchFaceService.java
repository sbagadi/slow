/*
 *
 */

package com.bagadi.apps.slow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.utils.SlowWatchFaceCustomizeUtil;
import com.bagadi.apps.slow.utils.TypefaceUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with 24 hours in a single rotation. It has a seconds widget and shows digital
 * date and time based on user's preference. In ambient mode, the seconds widget isn't shown.
 */
public class SlowWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = SlowWatchFaceService.class.getSimpleName();

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    /**
     * A handler used to send time update messages to the {@link Engine} class.
     */
    private static class EngineHandler extends Handler {
        private final WeakReference<SlowWatchFaceService.Engine> mWeakReference;

        public EngineHandler(SlowWatchFaceService.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SlowWatchFaceService.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    /**
     * A {@link android.support.wearable.watchface.CanvasWatchFaceService.Engine} instance used to
     * draw on a {@link Canvas} of a watch face.
     */
    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        /**
         * Flag to determine whether or not the watch face is round.
         */
        boolean mIsRound = true;

        /**
         * Flag to determine whether or not the watch face is in ambient mode. If true, we update
         * the watch face to be shown in black and white.
         */
        boolean mAmbient;

        /**
         * A {@link Calendar} instance used to update time.
         */
        private Calendar mCalendar;

        /**
         * Center X coordinate of the Canvas.
         */
        private float mCenterX;

        /**
         * Center Y coordinate of the Canvas.
         */
        private float mCenterY;

        /**
         * Background color for the watch face.
         */
        int mBackgroundPaintColor = SlowWatchFaceCustomizeUtil.getBackgroundColorForThemeName(
                SlowWatchFaceService.this,
                Constants.THEME_NAME_DEFAULT);

        /**
         * Primary color for the watch face. Used for drawing the hour ticks and hour numbers.
         */
        int mPrimaryColor = SlowWatchFaceCustomizeUtil.getPrimaryColorForThemeName(
                SlowWatchFaceService.this,
                Constants.THEME_NAME_DEFAULT);

        /**
         * Secondary color for the watch face. Used for drawing the half-hour ticks.
         */
        int mSecondaryColor = SlowWatchFaceCustomizeUtil.getSecondaryColorForThemeName(
                SlowWatchFaceService.this,
                Constants.THEME_NAME_DEFAULT);

        /**
         * Accent color for the watch face. Used for drawing the watch hands.
         */
        int mAccentPaintColor = SlowWatchFaceCustomizeUtil.getAccentColorIdForColorName(
                SlowWatchFaceService.this,
                Constants.ACCENT_COLOR_NAME_DEFAULT);

        /**
         * An int to keep track of what to show for date-time widget. See the
         * {@link com.bagadi.apps.slow.common.Constants.ConfigShowDateTimeType} interface for
         * all the possible values.
         */
        @Constants.ConfigShowDateTimeType
        int mShowDateTime = Constants.SHOW_DATE_TIME_DEFAULT;

        /* All the Paint instances needed to for drawing the watch face.*/
        Paint mBackgroundPaint;
        Paint mSecondTickPaint;
        Paint mMinuteTickPaint;
        Paint mHourTickPaint;
        Paint mSmallHourPaint;
        Paint mHandPaint;
        Paint mWidgetsPaint;

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

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        /**
         * Peek card bounds used for drawing a rectangle at the bottom of the watch face when in
         * ambient mode for better readability
         */
        private Rect mPeekCardBounds = new Rect();

        /**
         * The {@link GoogleApiClient} used for accessing the Wearable Data Layer APIs.
         */
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(SlowWatchFaceService.this)
                .addConnectionCallbacks(Engine.this)
                .addOnConnectionFailedListener(Engine.this)
                .addApiIfAvailable(Wearable.API)
                .build();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SlowWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(false)
                    .setStatusBarGravity(Gravity.CENTER)
                    .build());

            Resources resources = SlowWatchFaceService.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(mBackgroundPaintColor);

            Typeface robotoBold = TypefaceUtil.getTypefaceForName(
                    SlowWatchFaceService.this, Constants.TYPEFACE_ROBOTO_BOLD);
            Typeface robotoRegular = TypefaceUtil.getTypefaceForName(
                    SlowWatchFaceService.this, Constants.TYPEFACE_ROBOTO_REGULAR);
            Typeface robotoThin = TypefaceUtil.getTypefaceForName(
                    SlowWatchFaceService.this, Constants.TYPEFACE_ROBOTO_THIN);

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

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            // Set whether or not the watch face is round.
            mIsRound = insets.isRound();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            // Redraw after each time tick.
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mHandPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            updateWatchHandStyle();

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Updates the watch hand styles based on the {@link #mAmbient} flag.
         */
        private void updateWatchHandStyle() {
            if (mAmbient) {
                mHandPaint.setColor(Color.WHITE);
                mSecondTickPaint.setColor(Color.WHITE);
                mMinuteTickPaint.setColor(Color.WHITE);
                mHourTickPaint.setColor(Color.WHITE);
                mBackgroundPaint.setColor(Color.BLACK);
                mSmallHourPaint.setColor(Color.WHITE);
            } else {
                mHandPaint.setColor(mAccentPaintColor);
                mSecondTickPaint.setColor(mSecondaryColor);
                mMinuteTickPaint.setColor(mSecondaryColor);
                mHourTickPaint.setColor(mPrimaryColor);
                mBackgroundPaint.setColor(mBackgroundPaintColor);
                mWidgetsPaint.setColor(mSecondaryColor);
                mSmallHourPaint.setColor(mSecondaryColor);
            }
        }

        /**
         * Sets the theme for the watch face by updating the background, primary and secondary
         * colors.
         *
         * @param themeName the theme name to be set. See
         *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for acceptable
         *                  values.
         */
        private void setTheme(@Constants.ThemeName String themeName) {
            mBackgroundPaintColor =
                    SlowWatchFaceCustomizeUtil.getBackgroundColorForThemeName(
                            SlowWatchFaceService.this,
                            themeName);
            setPrimaryColor(SlowWatchFaceCustomizeUtil.getPrimaryColorForThemeName(
                    SlowWatchFaceService.this, themeName));
            setSecondaryColor(SlowWatchFaceCustomizeUtil.getSecondaryColorForThemeName(
                    SlowWatchFaceService.this, themeName));
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
        private void setAccentColor(@Constants.ColorName String colorName) {
            mAccentPaintColor = SlowWatchFaceCustomizeUtil
                    .getAccentColorIdForColorName(SlowWatchFaceService.this, colorName);
            updateWatchHandStyle();
        }

        /**
         * Sets the date time widget preferences.
         *
         * @param preference the preference value to be set. See the
         *                   {@link com.bagadi.apps.slow.common.Constants.ConfigShowDateTimeType}
         *                   for acceptable values.
         */
        private void setShowDateTime(@Constants.ConfigShowDateTimeType int preference) {
            mShowDateTime = preference;
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            }

            // Get the half height of the mWidgetsPaint to correctly center the date time text.
            float widgetPaintHalfTextHeight =
                    (mWidgetsPaint.descent() + mWidgetsPaint.ascent()) / 2;
            // Draw the date time widget on the center of the canvas.
            drawDateTime(canvas, mCenterX, mCenterY - widgetPaintHalfTextHeight);

            if (mIsRound) {
                onDrawRound(canvas);
            } else {
                onDrawSquare(canvas);
            }

            // Draw the seconds widget if not in ambient mode.
            if (!isInAmbientMode()) {
                drawSecondsWidget(canvas);
            }

            /* Draw rectangle behind peek card in ambient mode to improve readability. */
            if (mAmbient) {
                canvas.drawRect(mPeekCardBounds, mBackgroundPaint);
            }
        }

        /**
         * Draws the round watch face on a given canvas.
         * TODO: Some of the calculations can be done in
         * {@link #onSurfaceChanged(SurfaceHolder, int, int, int)} to improve efficiency.
         *
         * @param canvas a {@link Canvas} instance to draw on.
         */
        private void onDrawRound(Canvas canvas) {
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

            // Rotate to the time hand rotation to draw the watch hand.
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
         * TODO: Some of the calculations can be done in
         * {@link #onSurfaceChanged(SurfaceHolder, int, int, int)} to improve efficiency.
         *
         * @param canvas a {@link Canvas} instance to draw on.
         */
        private void onDrawSquare(Canvas canvas) {
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

            // TODO; Come up with an elegant way to draw the watch hand in the corners.
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
            if (mShowDateTime == Constants.CONFIG_SHOW_NONE) {
                // Nothing to draw.
                return;
            }

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

        /**
         * Draws the seconds widget.
         * TODO: Some of the calculations can be done in
         * {@link #onSurfaceChanged(SurfaceHolder, int, int, int)} to improve efficiency.
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

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, Engine.this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mPeekCardBounds.set(rect);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SlowWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SlowWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Updates the config data. Called once the {@link GoogleApiClient} is connected.
         */
        private void updateConfigDataItemAndUiOnStartup() {
            SlowWatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new SlowWatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            setDefaultValuesForMissingConfigFiles(startupConfig);
                            SlowWatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);

                            updateUiForConfigDataMap(startupConfig);
                        }
                    });
        }

        /**
         * Sets teh default values for the any missing config values in the provided data map.
         *
         * @param config a {@link DataMap} for which to update missing config values.
         */
        private void setDefaultValuesForMissingConfigFiles(DataMap config) {
            addStringKeyIfMissing(config,
                    Constants.KEY_THEME_NAME,
                    Constants.THEME_NAME_DEFAULT);
            addStringKeyIfMissing(config,
                    Constants.KEY_ACCENT_COLOR,
                    Constants.ACCENT_COLOR_NAME_DEFAULT);
            addIntKeyIfMissing(config,
                    Constants.KEY_DATE_TIME,
                    Constants.SHOW_DATE_TIME_DEFAULT);
        }

        /**
         * Sets the given key to the string value for the data map if the key is missing in the map.
         *
         * @param config     a {@link DataMap} for which to update the key.
         * @param key        the key to be checked and updated.
         * @param preference a string preference value to be set to the key if missing.
         */
        private void addStringKeyIfMissing(DataMap config, String key, String preference) {
            if (!config.containsKey(key)) {
                config.putString(key, preference);
            }
        }

        /**
         * Sets the give key to the integer value for the data pam if the key is missing in the map.
         *
         * @param config     a {@link DataMap} for which to update the key.
         * @param key        the key to be checked and updated.
         * @param preference an int preference value to be set to the key if missing.
         */
        private void addIntKeyIfMissing(DataMap config, String key, int preference) {
            if (!config.containsKey(key)) {
                config.putInt(key, preference);
            }
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (DataEvent dataEvent : dataEventBuffer) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }

                DataItem dataItem = dataEvent.getDataItem();
                if (!dataItem.getUri().getPath().equals(
                        Constants.PATH_WITH_FEATURE)) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                Log.d(TAG, "onDataChanged: " + config);
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "onDataChanged: " + config);
                }
                updateUiForConfigDataMap(config);
            }
        }

        /**
         * Updates the UI based on the new config.
         *
         * @param config a {@link DataMap} containing preference values to be updated.
         */
        private void updateUiForConfigDataMap(DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }

                Object preference = config.get(configKey);
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Found watch face config key: " + configKey + " -> "
                            + preference);
                }

                if (updateUiForKey(configKey, preference)) {
                    uiUpdated = true;
                }
            }

            if (uiUpdated) {
                invalidate();
            }
        }

        /**
         * Updates the UI for a give config preference update.
         *
         * @param configKey  the config key for which to update the UI.
         * @param preference the config value to be set.
         * @return {@code true} if the UI update was successful.
         */
        private boolean updateUiForKey(String configKey, Object preference) {
            switch (configKey) {
                case Constants.KEY_THEME_NAME:
                    @Constants.ThemeName
                    String themeName = (String) preference;
                    setTheme(themeName);
                    break;
                case Constants.KEY_ACCENT_COLOR:
                    @Constants.ColorName
                    String accentColorName = (String) preference;
                    setAccentColor(accentColorName);
                    break;
                case Constants.KEY_DATE_TIME:
                    @Constants.ConfigShowDateTimeType
                    int dateTimePreference = (int) preference;
                    setShowDateTime(dateTimePreference);
                    break;
                default:
                    Log.w(TAG, "Ignoring unknown key: " + configKey);
                    return false;
            }

            return true;
        }

        @Override
        public void onConnected(@Nullable Bundle connectionHint) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnected: " + connectionHint);
            }
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();
        }

        @Override
        public void onConnectionSuspended(int cause) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnectionSuspended: " + cause);
            }
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onConnectionFailed: " + connectionResult);
            }
        }
    }
}
