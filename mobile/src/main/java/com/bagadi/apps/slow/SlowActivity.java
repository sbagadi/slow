package com.bagadi.apps.slow;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.utils.SlowWatchFaceCustomizeUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import static com.bagadi.apps.slow.common.Constants.PATH_WITH_FEATURE;

/**
 * An activity showing the Slow watch face preferences. Also displays the watch face with the
 * customizations applied.
 */
public class SlowActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<DataApi.DataItemResult> {
    private static final String TAG = SlowActivity.class.getSimpleName();

    private static final String MARKET_URI_BASE = "market://details?id=";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    private String[] mThemes = new String[]{Constants.THEME_DARK, Constants.THEME_LIGHT};
    private String[] mAccents = new String[]{Constants.COLOR_RED,
            Constants.COLOR_PINK,
            Constants.COLOR_PURPLE,
            Constants.COLOR_INDIGO,
            Constants.COLOR_BLUE,
            Constants.COLOR_CYAN,
            Constants.COLOR_TEAL,
            Constants.COLOR_GREEN,
            Constants.COLOR_LIME,
            Constants.COLOR_YELLOW,
            Constants.COLOR_AMBER,
            Constants.COLOR_ORANGE};

    private SlowWatchFaceView mWatchFaceView;

    private Spinner mThemesSpinner;
    private Spinner mAccentsSpinner;
    private Spinner mDateTimeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);

        mGoogleApiClient = new GoogleApiClient.Builder(SlowActivity.this)
                .addConnectionCallbacks(this)
                .addApiIfAvailable(Wearable.API)
                .build();

        ((TextView) findViewById(R.id.customize_textView)).setText(R.string.customize_watch_face);
        ((TextView) findViewById(R.id.theme_textView)).setText(R.string.label_theme);
        ((TextView) findViewById(R.id.accent_textView)).setText(R.string.label_accent);
        ((TextView) findViewById(R.id.date_time_textView)).setText(R.string.label_date_time);

        mWatchFaceView = (SlowWatchFaceView) findViewById(R.id.watch_face);

        ToggleButton shapeToggleButton = (ToggleButton) findViewById(R.id.shape_toggleButton);
        shapeToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mWatchFaceView.setIsRound(!isChecked);
            }
        });

        mThemesSpinner = (Spinner) findViewById(R.id.themes_spinner);
        ThemesSpinnerAdapter themesAdapter =
                new ThemesSpinnerAdapter(SlowActivity.this, R.layout.spinner_item);
        themesAdapter.addAll(mThemes);
        mThemesSpinner.setAdapter(themesAdapter);

        mAccentsSpinner = (Spinner) findViewById(R.id.accents_spinner);
        AccentsSpinnerAdapter accentsAdapter =
                new AccentsSpinnerAdapter(SlowActivity.this, R.layout.spinner_item);
        accentsAdapter.addAll(mAccents);
        mAccentsSpinner.setAdapter(accentsAdapter);

        mDateTimeSpinner = (Spinner) findViewById(R.id.date_time_spinner);
        ArrayAdapter<String> dateTimeAdapter =
                new ArrayAdapter<>(SlowActivity.this, R.layout.spinner_item);
        dateTimeAdapter.addAll(
                SlowWatchFaceCustomizeUtil.getDateTimePreferenceNameForConfig(SlowActivity.this,
                        Constants.CONFIG_SHOW_NONE),
                SlowWatchFaceCustomizeUtil.getDateTimePreferenceNameForConfig(SlowActivity.this,
                        Constants.CONFIG_SHOW_DATE),
                SlowWatchFaceCustomizeUtil.getDateTimePreferenceNameForConfig(SlowActivity.this,
                        Constants.CONFIG_SHOW_TIME),
                SlowWatchFaceCustomizeUtil.getDateTimePreferenceNameForConfig(SlowActivity.this,
                        Constants.CONFIG_SHOW_DATE_TIME));
        mDateTimeSpinner.setAdapter(dateTimeAdapter);

        setUpSpinnerListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.slow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rate:
                showRateAppAction();
                return true;
            case R.id.action_share:
                sendShareAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Creates a {@link Intent#ACTION_VIEW} intent for taking the user to the app page for this app.
     */
    private void showRateAppAction() {
        String appPackageName = getApplicationContext().getPackageName();
        Uri playStoreUri = Uri.parse(MARKET_URI_BASE + appPackageName);
        Intent viewAppIntent = new Intent(Intent.ACTION_VIEW, playStoreUri);

        if (getPackageManager().queryIntentActivities(viewAppIntent, 0).size() > 0) {
            startActivity(viewAppIntent);
            return;
        }

        Uri webSiteUri =
                Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName);
        Intent viewInWebIntent = new Intent(Intent.ACTION_VIEW, webSiteUri);

        if (getPackageManager().queryIntentActivities(viewInWebIntent, 0).size() > 0) {
            startActivity(viewInWebIntent);
            return;
        }

        showErrorDialog(getString(R.string.title_no_apps_to_show_app));

    }

    /**
     * Creates a {@link Intent#ACTION_SEND} intent that lets the user share this app.
     */
    private void sendShareAction() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String appPackageName = getApplicationContext().getPackageName();
        Uri webSiteUri =
                Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName);
        String shareText = String.format(getString(R.string.share_text), webSiteUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        if (getPackageManager().queryIntentActivities(shareIntent, 0).size() > 0) {
            startActivity(shareIntent);
        } else {
            showErrorDialog(getString(R.string.title_no_apps_to_share_app));
        }
    }

    /**
     * Shows an {@link AlertDialog} with the message provided.
     *
     * @param messageText text to be displayed in the alert dialog.
     */
    private void showErrorDialog(String messageText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String okText = getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onConnected: " + connectionHint);
        }

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder
                    .scheme("wear")
                    .path(PATH_WITH_FEATURE)
                    .authority(mPeerId)
                    .build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
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

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getString(R.string.title_no_device_connected);
        String okText = getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
        String themeConfig = Constants.THEME_NAME_DEFAULT;
        String accentConfig = Constants.ACCENT_COLOR_NAME_DEFAULT;
        int dateTimeConfig = Constants.SHOW_DATE_TIME_DEFAULT;
        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();

            themeConfig = config.getString(Constants.KEY_THEME_NAME, Constants.THEME_NAME_DEFAULT);
            accentConfig = config.getString(
                    Constants.KEY_ACCENT_COLOR, Constants.ACCENT_COLOR_NAME_DEFAULT);
            dateTimeConfig =
                    config.getInt(Constants.KEY_DATE_TIME, Constants.SHOW_DATE_TIME_DEFAULT);
        }

        setUpSpinner(mThemesSpinner, themeConfig, mThemes);
        setUpSpinner(mAccentsSpinner, accentConfig, mAccents);
        setUpDateTimeSpinner(dateTimeConfig);

        setUpSpinnerListeners();
    }

    /**
     * Sets the spinner's selected position to the given config.
     *
     * @param spinner a {@link Spinner} for which to set the selection.
     * @param config  the config to set the Spinner's selection to
     * @param configs array of configs set on the spinner.
     */
    private void setUpSpinner(Spinner spinner, @NonNull String config, String[] configs) {
        for (int i = 0; i < configs.length; i++) {
            if (config.equals(configs[i])) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    /**
     * Sets the selection for the {@link #mDateTimeSpinner} based on the config provide.
     *
     * @param config config to which to set the Spinner's position.
     */
    private void setUpDateTimeSpinner(int config) {
        int selection;
        switch (config) {
            case Constants.CONFIG_SHOW_DATE:
                selection = 1;
                break;
            case Constants.CONFIG_SHOW_TIME:
                selection = 2;
                break;
            case Constants.CONFIG_SHOW_DATE_TIME:
                selection = 3;
                break;
            case Constants.CONFIG_SHOW_NONE:
            default:
                selection = 0;
        }

        mDateTimeSpinner.setSelection(selection);
    }

    /**
     * Adds the listeners to all the spinners.
     */
    private void setUpSpinnerListeners() {
        mThemesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> adapterView, View view, int position, long id) {
                @Constants.ThemeName
                final String preference = (String) adapterView.getItemAtPosition(position);
                sendStringConfigUpdateMessage(Constants.KEY_THEME_NAME, preference);
                mWatchFaceView.setTheme(preference);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mAccentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> adapterView, View view, int position, long id) {
                @Constants.ColorName
                final String preference = (String) adapterView.getItemAtPosition(position);
                sendStringConfigUpdateMessage(Constants.KEY_THEME_NAME, preference);
                mWatchFaceView.setAccentColor(preference);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDateTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> adapterView, View view, int position, long id) {
                @Constants.ConfigShowDateTimeType int preference;
                switch (position) {
                    case 1:
                        preference = Constants.CONFIG_SHOW_DATE;
                        break;
                    case 2:
                        preference = Constants.CONFIG_SHOW_TIME;
                        break;
                    case 3:
                        preference = Constants.CONFIG_SHOW_DATE_TIME;
                        break;
                    case 0:
                    default:
                        preference = Constants.CONFIG_SHOW_NONE;
                        break;
                }

                sendIntConfigUpdateMessage(Constants.KEY_DATE_TIME, preference);
                mWatchFaceView.setShowDateTime(preference);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Updates the Wearable Data API with the string config provided.
     *
     * @param configKey  Key for which to update the config.
     * @param preference a {@link String} representing the preference for the configKey.
     */
    private void sendStringConfigUpdateMessage(String configKey, String preference) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, preference);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + preference);
            }
        }
    }

    /**
     * Updates the Wearable Data API with the int config provided.
     *
     * @param configKey  Key for which to update the config.
     * @param preference a {@code int} representing the preference for the configKey.
     */
    private void sendIntConfigUpdateMessage(String configKey, int preference) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putInt(configKey, preference);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_WITH_FEATURE, rawData);

            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + preference);
            }
        }
    }
}
