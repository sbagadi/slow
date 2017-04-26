package com.bagadi.apps.slow;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import com.bagadi.apps.slow.adapters.AccentConfigListAdapter;
import com.bagadi.apps.slow.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * An {@link Activity} showing the accent configuration options for the Slow watch face.
 */
public class SlowWatchFaceAccentConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {
    private static final String TAG = "AccentConfigActivity";

    private GoogleApiClient mGoogleApiClient;
    private TextView mTextView;
    private AccentConfigListAdapter mAccentConfigListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_watch_face_config);
        final BoxInsetLayout insetLayout = (BoxInsetLayout) findViewById(R.id.watch_boxInsetLayout);
        mTextView = (TextView) findViewById(R.id.header);
        mTextView.setText(R.string.label_accent);

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

        final WearableListView listView = (WearableListView) findViewById(R.id.customize_listView);

        listView.setHasFixedSize(true);
        listView.setClickListener(SlowWatchFaceAccentConfigActivity.this);
        listView.addOnScrollListener(SlowWatchFaceAccentConfigActivity.this);

        ArrayList<String> accentsArray = generateAccentConfigData();
        mAccentConfigListAdapter = new AccentConfigListAdapter(accentsArray);
        listView.setAdapter(mAccentConfigListAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                        }

                        SlowWatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                                new SlowWatchFaceUtil.FetchConfigDataMapCallback() {
                                    @Override
                                    public void onConfigDataMapFetched(DataMap dataMap) {
                                        if (dataMap.containsKey(Constants.KEY_ACCENT_COLOR)) {
                                            @Constants.ColorName
                                            String selectedColorIndex = dataMap.getString(
                                                    Constants.KEY_ACCENT_COLOR);
                                            int selectedPosition = mAccentConfigListAdapter
                                                    .getItemsList()
                                                    .indexOf(selectedColorIndex);
                                            listView.smoothScrollToPosition(selectedPosition);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Generates the accents config data to be set to the {@link #mAccentConfigListAdapter}.
     *
     * @return a list of strings with the Accent color names.
     */
    private ArrayList<String> generateAccentConfigData() {
        ArrayList<String> configItemDataArray = new ArrayList<>();

        configItemDataArray.add(Constants.COLOR_RED);
        configItemDataArray.add(Constants.COLOR_PINK);
        configItemDataArray.add(Constants.COLOR_PURPLE);
        configItemDataArray.add(Constants.COLOR_INDIGO);
        configItemDataArray.add(Constants.COLOR_BLUE);
        configItemDataArray.add(Constants.COLOR_CYAN);
        configItemDataArray.add(Constants.COLOR_TEAL);
        configItemDataArray.add(Constants.COLOR_GREEN);
        configItemDataArray.add(Constants.COLOR_LIME);
        configItemDataArray.add(Constants.COLOR_YELLOW);
        configItemDataArray.add(Constants.COLOR_AMBER);
        configItemDataArray.add(Constants.COLOR_ORANGE);

        return configItemDataArray;
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        @Constants.ColorName
        String accentColorName = mAccentConfigListAdapter.getItemsList().get(adapterPosition);
        updateConfigDataItem(accentColorName);
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onScroll(int i) {
    }

    @Override
    public void onAbsoluteScrollChange(int scroll) {
        int translation = Math.min(-scroll, 0);
        mTextView.setTranslationY(translation);
    }

    @Override
    public void onScrollStateChanged(int i) {
    }

    @Override
    public void onCentralPositionChanged(int i) {
    }

    /**
     * Updates the Accent config with the given accent color name.
     *
     * @param accentColorName the color name to be updates. See the
     *                        {@link com.bagadi.apps.slow.common.Constants.ColorName} for accepted
     *                        values.
     */
    public void updateConfigDataItem(@Constants.ColorName String accentColorName) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(Constants.KEY_ACCENT_COLOR, accentColorName);
        SlowWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }
}
