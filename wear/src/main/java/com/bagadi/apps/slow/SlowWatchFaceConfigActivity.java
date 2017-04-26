package com.bagadi.apps.slow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import com.bagadi.apps.slow.adapters.CustomizeListAdapter;
import com.bagadi.apps.slow.common.Constants;

import java.util.ArrayList;

/**
 * An {@link Activity} showing the configurations for the Slow watch face.
 */
public class SlowWatchFaceConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {

    private TextView mHeaderTextView;
    private CustomizeListAdapter mCustomizeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_watch_face_config);
        final BoxInsetLayout insetLayout = (BoxInsetLayout) findViewById(R.id.watch_boxInsetLayout);
        mHeaderTextView = (TextView) findViewById(R.id.header);
        mHeaderTextView.setText(R.string.customize_watch_face);

        WearableListView listView = (WearableListView) findViewById(R.id.customize_listView);
        insetLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                if (!insets.isRound()) {
                    view.setPaddingRelative(
                            (int) getResources()
                                    .getDimensionPixelOffset(R.dimen.content_padding_start),
                            view.getPaddingTop(),
                            view.getPaddingEnd(),
                            view.getPaddingBottom());
                }
                return view.onApplyWindowInsets(insets);
            }
        });

        listView.setHasFixedSize(true);
        listView.setClickListener(this);
        listView.addOnScrollListener(this);

        ArrayList<String> customizeItemsArray = generateCustomizeData();
        mCustomizeListAdapter = new CustomizeListAdapter(customizeItemsArray);
        listView.setAdapter(mCustomizeListAdapter);
    }

    /**
     * Generates the configuration items to be set to the {@link #mCustomizeListAdapter}.
     *
     * @return a list of string containing the accent names.
     */
    private ArrayList<String> generateCustomizeData() {
        ArrayList<String> customizeItemsArray =
                new ArrayList<>(3);
        customizeItemsArray.add(Constants.CUSTOMIZE_ITEM_THEME);
        customizeItemsArray.add(Constants.CUSTOMIZE_ITEM_ACCENT);
        customizeItemsArray.add(Constants.CUSTOMIZE_ITEM_DATE_TIME);
        return customizeItemsArray;
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        @Constants.CustomizeItemName
        String customizeItem = mCustomizeListAdapter.getItemsList().get(adapterPosition);
        if (customizeItem.equals(Constants.CUSTOMIZE_ITEM_THEME)) {
            startActivity(new Intent(
                    SlowWatchFaceConfigActivity.this, SlowWatchFaceThemeConfigActivity.class));
        } else if (customizeItem.equals(Constants.CUSTOMIZE_ITEM_ACCENT)) {
            startActivity(new Intent(
                    SlowWatchFaceConfigActivity.this, SlowWatchFaceAccentConfigActivity.class));
        } else if (customizeItem.equals(Constants.CUSTOMIZE_ITEM_DATE_TIME)) {
            startActivity(new Intent(
                    SlowWatchFaceConfigActivity.this, SlowWatchFaceDateTimeConfigActivity.class));
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onScroll(int scroll) {

    }

    @Override
    public void onAbsoluteScrollChange(int scroll) {
        float translation = Math.min(-scroll, 0);
        mHeaderTextView.setTranslationY(translation);

    }

    @Override
    public void onScrollStateChanged(int i) {

    }

    @Override
    public void onCentralPositionChanged(int i) {

    }
}
