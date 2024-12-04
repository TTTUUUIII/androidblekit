package com.outlook.wn123o.androidblekit.common;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class MsgHistoryViewAdapter extends ListAdapter<Msg, MsgViewHolder> {
    public MsgHistoryViewAdapter() {
        super(new DiffUtil.ItemCallback<Msg>() {
            @Override
            public boolean areItemsTheSame(@NonNull Msg oldItem, @NonNull Msg newItem) {
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Msg oldItem, @NonNull Msg newItem) {
                return false;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MsgViewHolder.create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}
