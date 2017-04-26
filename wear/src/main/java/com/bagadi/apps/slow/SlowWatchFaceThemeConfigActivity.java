package com.bagadi.apps.slow;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

import com.bagadi.apps.slow.adapters.ThemeConfigListAdapter;
import com.bagadi.apps.slow.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

/**
 * An {@link Activity} showing the theme configuration options for the Slow watch face.
 */
public class SlowWatchFaceThemeConfigActivity extends Activity implements
        WearableListView.ClickListener, WearableListView.OnScrollListener {
    private static final String TAG = "BgConfigActivity";

    private GoogleApiClient mGoogleApiClient;
    private TextView mTextView;
    private ThemeConfigListAdapter mThemeConfigListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_watch_face_config);
        final BoxInsetLayout insetLayout = (BoxInsetLayout) findViewById(R.id.watch_boxInsetLayout);
        mTextView = (TextView) findViewById(R.id.header);
        mTextView.setText(R.string.label_theme);

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
        listView.setClickListener(SlowWatchFaceThemeConfigActivity.this);
        listView.addOnScrollListener(SlowWatchFaceThemeConfigActivity.this);

        ArrayList<String> themesArray = generateThemeConfigData();
        mThemeConfigListAdapter = new ThemeConfigListAdapter(themesArray);
        listView.setAdapter(mThemeConfigListAdapter);

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
                                        if (dataMap.containsKey(Constants.KEY_THEME_NAME)) {
                                            @Constants.ColorName
                                            String accentColor =
                                                    dataMap.getString(Constants.KEY_THEME_NAME);
                                            int selectedPosition = mThemeConfigListAdapter
                                                    .getItemsList()
                                                    .indexOf(accentColor);
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
     * Generates the theme config data to be set to the {@link #mThemeConfigListAdapter}.
     *
     * @return a list of string containing the theme names.
     */
    private ArrayList<String> generateThemeConfigData() {
        ArrayList<String> configItemDataArray = new ArrayList<>(2);
        configItemDataArray.add(Constants.THEME_DARK);
        configItemDataArray.add(Constants.THEME_LIGHT);
        return configItemDataArray;
    }


    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        @Constants.ThemeName
        String theme = mThemeConfigListAdapter.getItemsList().get(adapterPosition);
        updateBackgroundConfigDataItem(theme);
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
     * Updates the theme config with the given accent theme name.
     *
     * @param themeName the theme name to be updates. See the
     *                  {@link com.bagadi.apps.slow.common.Constants.ThemeName} for accepted values.
     */
    private void updateBackgroundConfigDataItem(@Constants.ThemeName String themeName) {
        DataMap configKeysToOverwrite = new DataMap();
        configKeysToOverwrite.putString(Constants.KEY_THEME_NAME, themeName);
        SlowWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }
}
