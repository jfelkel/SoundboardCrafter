package de.soundboardcrafter.activity.sound.edit.common;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import de.soundboardcrafter.model.SelectableSoundboard;

/**
 * Adapter for a {@link SelectableSoundboardListItemRow}.
 */
// TODO: 05.05.2019 move to another package it is also used in the gameeditview
public class SelectableSoundboardListItemAdapter extends BaseAdapter {
    private static final String TAG = SelectableSoundboardListItemAdapter.class.getName();
    private List<SelectableSoundboard> soundboards = new ArrayList<>();

    /**
     * Whether the soundboards can be changed.
     */
    private boolean editable;

    public SelectableSoundboardListItemAdapter(List<SelectableSoundboard> soundboards, boolean editable) {
        this.soundboards = soundboards;
        this.editable = editable;
    }

    @Override
    public int getCount() {
        return soundboards.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public SelectableSoundboard getItem(int position) {
        return soundboards.get(position);
    }

    @Override
    @UiThread
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (!(convertView instanceof SelectableSoundboardListItemRow)) {
            convertView = new SelectableSoundboardListItemRow(parent.getContext());
        }
        SelectableSoundboardListItemRow itemRow = (SelectableSoundboardListItemRow) convertView;
        itemRow.setSoundboard(soundboards.get(position));
        itemRow.setEnabled(editable);
        return convertView;
    }
}