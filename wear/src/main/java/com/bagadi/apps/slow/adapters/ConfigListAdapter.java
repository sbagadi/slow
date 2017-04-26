package com.bagadi.apps.slow.adapters;

import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;

import com.bagadi.apps.slow.ConfigItem;

import java.util.ArrayList;

/**
 * Created by Santosh on 1/14/2017.
 */
public abstract class ConfigListAdapter extends WearableListView.Adapter {
    private final ArrayList<String> mItemsList;

    public ConfigListAdapter(ArrayList<String> itemsList) {
        this.mItemsList = itemsList;
    }

    public ArrayList<String> getItemsList() {
        return this.mItemsList;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(new ConfigItem(parent.getContext()));
    }

    @Override
    public int getItemCount() {
        return mItemsList.size();
    }
}