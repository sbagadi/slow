package com.bagadi.apps.slow.adapters;

import android.support.wearable.view.WearableListView;

import com.bagadi.apps.slow.ConfigItem;
import com.bagadi.apps.slow.common.Constants;
import com.bagadi.apps.slow.utils.SlowWatchFaceCustomizeUtil;

import java.util.ArrayList;

/**
 * Created by Santosh on 3/27/2017.
 */

public class AccentConfigListAdapter extends ConfigListAdapter {

    public AccentConfigListAdapter(ArrayList<String> itemsList) {
        super(itemsList);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        if (!(holder.itemView instanceof ConfigItem)) {
            return;
        }

        ConfigItem item = (ConfigItem) holder.itemView;

        @Constants.ColorName
        String accentColor = getItemsList().get(position);
        item.setConfigData(SlowWatchFaceCustomizeUtil.getIconResourceIdForColorName(accentColor), accentColor);
    }
}
