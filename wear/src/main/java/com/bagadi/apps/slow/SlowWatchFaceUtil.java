package com.bagadi.apps.slow;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bagadi.apps.slow.common.Constants;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * A utility class used to fetch and update the data map containing all the configurations for the
 * Slow watch face.
 */
public class SlowWatchFaceUtil {
    private static final String TAG = SlowWatchFaceUtil.class.getSimpleName();

    /**
     * An interface for receiving configuration {@link DataMap} when fetched.
     */
    public interface FetchConfigDataMapCallback {
        /**
         * Called when the config {@link DataMap} is fetched.
         *
         * @param config a {@link DataMap} containing configurations.
         */
        void onConfigDataMapFetched(DataMap config);
    }

    /**
     * Fetched the configuration {@link DataMap} using the {@link GoogleApiClient} provided and
     * sends the callback to the {@link FetchConfigDataMapCallback} provided.
     *
     * @param client   a {@link GoogleApiClient} to be used to to fetch the config data map.
     * @param callback a {@link FetchConfigDataMapCallback} that to receive the data map once
     *                 received.
     */
    public static void fetchConfigDataMap(final GoogleApiClient client,
                                          final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(Constants.PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                }
        );
    }

    /**
     * Overwrites the keys in config map with the provided data map.
     *
     * @param apiClient             a {@link GoogleApiClient} to be used fetch the existing config
     *                              data map.
     * @param configKeysToOverwrite a {@link DataMap} containing key value pairs that need to be
     *                              overridden.
     */
    public static void overwriteKeysInConfigDataMap(final GoogleApiClient apiClient,
                                                    final DataMap configKeysToOverwrite) {
        SlowWatchFaceUtil.fetchConfigDataMap(apiClient, new FetchConfigDataMapCallback() {
            @Override
            public void onConfigDataMapFetched(DataMap config) {
                DataMap overwrittenConfig = new DataMap();
                overwrittenConfig.putAll(config);
                overwrittenConfig.putAll(configKeysToOverwrite);
                SlowWatchFaceUtil.putConfigDataItem(apiClient, overwrittenConfig);
            }
        });
    }

    /**
     * Puts the data item provided with the Wearable Data API layer.
     *
     * @param googleApiClient a {@link GoogleApiClient} to be used to update the values.
     * @param newConfig       a {@link DataMap} with the new config values.
     */
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_WITH_FEATURE);
        putDataMapRequest.setUrgent();
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                        }
                    }
                });
    }

    /**
     * A {@link ResultCallback<com.google.android.gms.wearable.DataApi.DataItemResult>} used to get
     * callbacks from Wearable Data API.
     */
    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallBack;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            this.mCallBack = callback;
        }

        @Override
        public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getDataItem() != null) {
                DataItem configDataItem = dataItemResult.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                DataMap config = dataMapItem.getDataMap();
                mCallBack.onConfigDataMapFetched(config);
            } else {
                mCallBack.onConfigDataMapFetched(new DataMap());
            }
        }
    }

    private SlowWatchFaceUtil() {
    }

    ;
}
