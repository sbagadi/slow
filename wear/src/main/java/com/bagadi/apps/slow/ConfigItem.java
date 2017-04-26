package com.bagadi.apps.slow;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A {@link LinearLayout} used to show a label as {@link TextView} and an icon as
 * {@link CircledImageView}. Implements the
 * {@link android.support.wearable.view.WearableListView.OnCenterProximityListener} to animate the
 * views based on their position in a {@link WearableListView}
 */
public class ConfigItem extends LinearLayout implements
        WearableListView.OnCenterProximityListener {

    private static final int ANIMATION_DURATION_MS = 150;
    private static final float SHRINK_ICON_RATIO = 0.75f;

    private static final float SHRINK_LABEL_ALPHA = 0.5f;
    private static final float EXPAND_LABEL_ALPHA = 1f;

    private final TextView mLabel;
    private final CircledImageView mIcon;

    private final float mExpandIconRadius;
    private final float mShrinkIconRadius;

    private final ObjectAnimator mExpandIconAnimator;
    private final ObjectAnimator mExpandLabelAnimator;
    private final AnimatorSet mExpandAnimatorSet;

    private final ObjectAnimator mShrinkIconAnimator;
    private final ObjectAnimator mShrinkLabelAnimator;
    private final AnimatorSet mShrinkAnimatorSet;

    public ConfigItem(Context context) {
        super(context);
        View.inflate(context, R.layout.customize_item, this);

        mLabel = (TextView) findViewById(R.id.customize_label);
        mIcon = (CircledImageView) findViewById(R.id.customize_icon);

        mExpandIconRadius = mIcon.getCircleRadius();
        mShrinkIconRadius = mExpandIconRadius * SHRINK_ICON_RATIO;

        mExpandIconAnimator = ObjectAnimator.ofFloat(
                mIcon,
                "circleRadius",
                mShrinkIconRadius,
                mExpandIconRadius);
        mExpandLabelAnimator = ObjectAnimator.ofFloat(
                mLabel,
                "alpha",
                SHRINK_LABEL_ALPHA,
                EXPAND_LABEL_ALPHA);
        mExpandAnimatorSet = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
        mExpandAnimatorSet.playTogether(mExpandIconAnimator, mExpandLabelAnimator);

        mShrinkIconAnimator = ObjectAnimator.ofFloat(
                mIcon,
                "circleRadius",
                mExpandIconRadius,
                mShrinkIconRadius);
        mShrinkLabelAnimator = ObjectAnimator.ofFloat(
                mLabel,
                "alpha",
                EXPAND_LABEL_ALPHA,
                SHRINK_LABEL_ALPHA);
        mShrinkAnimatorSet = new AnimatorSet().setDuration(ANIMATION_DURATION_MS);
        mShrinkAnimatorSet.playTogether(mShrinkIconAnimator, mShrinkLabelAnimator);
    }

    /**
     * Sets the config data for the view.
     *
     * @param iconResId the icon drawable resource to be set to the circular image view icon.
     * @param label the string value to be set to the label.
     */
    public void setConfigData(@DrawableRes int iconResId, String label) {
        mIcon.setImageResource(iconResId);
        mLabel.setText(label);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            mShrinkAnimatorSet.cancel();
            if (!mExpandAnimatorSet.isRunning()) {
                mExpandIconAnimator.setFloatValues(mIcon.getCircleRadius(), mExpandIconRadius);
                mExpandLabelAnimator.setFloatValues(mLabel.getAlpha(), EXPAND_LABEL_ALPHA);
                mExpandAnimatorSet.start();

            }
        } else {
            mExpandAnimatorSet.cancel();
            mIcon.setCircleRadius(mExpandIconRadius);
            mLabel.setAlpha(EXPAND_LABEL_ALPHA);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            mExpandAnimatorSet.cancel();
            if (!mShrinkAnimatorSet.isRunning()) {
                mShrinkIconAnimator.setFloatValues(mIcon.getCircleRadius(), mShrinkIconRadius);
                mShrinkLabelAnimator.setFloatValues(mLabel.getAlpha(), SHRINK_LABEL_ALPHA);
                mShrinkAnimatorSet.start();
            }
        } else {
            mShrinkAnimatorSet.cancel();
            mIcon.setCircleRadius(mShrinkIconRadius);
            mLabel.setAlpha(SHRINK_LABEL_ALPHA);
        }
    }
}
