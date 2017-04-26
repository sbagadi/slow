package com.bagadi.apps.slow;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bagadi.apps.slow.common.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

/**
 * An {@link Activity} showing the date and time configuration options for the Slow watch face.
 */
public class SlowWatchFaceDateTimeConfigActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "DateTimeConfigActivity";

    private GoogleApiClient mGoogleApiClient;
    private TextView mTextView;
    @Constants.ConfigShowDateTimeType
    private int mDateTimePreference = Constants.SHOW_DATE_TIME_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_watch_face_date_time_config);
        final BoxInsetLayout insetLayout = (BoxInsetLayout) findViewById(R.id.watch_boxInsetLayout);
        mTextView = (TextView) findViewById(R.id.header);
        mTextView.setText(R.string.label_date_time);

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
                                        if (dataMap.containsKey(Constants.KEY_DATE_TIME)) {
                                            @Constants.ConfigShowDateTimeType int preference =
                                                    dataMap.getInt(Constants.KEY_DATE_TIME);
                                            mDateTimePreference = preference;
                                        }
                                        updateUiForDataChange();
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

    /**
     * Updates the radio buttons based on the date-time configuration data change.
     */
    private void updateUiForDataChange() {
        if (mDateTimePreference == Constants.CONFIG_SHOW_DATE) {
            ((RadioButton) findViewById(R.id.radio_date)).setChecked(true);
        } else if (mDateTimePreference == Constants.CONFIG_SHOW_TIME) {
            ((RadioButton) findViewById(R.id.radio_time)).setChecked(true);
        } else if (mDateTimePreference == Constants.CONFIG_SHOW_DATE_TIME) {
            ((RadioButton) findViewById(R.id.radio_date_time)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.radio_none)).setChecked(true);
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int dateTimePreference = Constants.CONFIG_SHOW_NONE;
        switch (view.getId()) {
            case R.id.radio_none:
                if (checked) {
                    dateTimePreference = Constants.CONFIG_SHOW_NONE;
                }
                break;
            case R.id.radio_date:
                if (checked) {
                    dateTimePreference = Constants.CONFIG_SHOW_DATE;
                }
                break;
            case R.id.radio_time:
                if (checked) {
                    dateTimePreference = Constants.CONFIG_SHOW_TIME;
                }
                break;
            case R.id.radio_date_time:
                if (checked) {
                    dateTimePreference = Constants.CONFIG_SHOW_DATE_TIME;
                }
                break;
        }

        updateConfigDataItem(dateTimePreference);
        finish();
    }

    /**
     * Updates the date-time config data item with the new date time preference provided.
     *
     * @param dateTimePreference the date time preference to be set.
     */
    private void updateConfigDataItem(final int dateTimePreference) {
        DataMap configKeysToOverride = new DataMap();
        configKeysToOverride.putInt(Constants.KEY_DATE_TIME, dateTimePreference);
        SlowWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverride);
    }
}
