package com.bagadi.apps.slow;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.utils.SlowWatchFaceCustomizeUtil;

/**
 * An {@link ArrayAdapter<String>} used to show the themes list in a spinner.
 */
public class ThemesSpinnerAdapter extends ArrayAdapter<String> {

    public ThemesSpinnerAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);

        @Constants.ThemeName String theme = getItem(position);
        label.setText(theme);
        label.setCompoundDrawablesRelativeWithIntrinsicBounds(
                SlowWatchFaceCustomizeUtil.getIconResourceIdForTheme(theme),
                0,
                0,
                0);

        return label;
    }

    @Override
    public View getDropDownView(int position,
                                @Nullable View convertView,
                                @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);

        @Constants.ThemeName String theme = getItem(position);
        label.setText(theme);
        label.setCompoundDrawablesRelativeWithIntrinsicBounds(
                SlowWatchFaceCustomizeUtil.getIconResourceIdForTheme(theme),
                0,
                0,
                0);

        return label;
    }
}
