package com.outlook.wn123o.androidblekit.common;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BindingAdapters {

    @BindingAdapter("submit")
    public static void submit(RecyclerView view, List<Msg> data) {
        if (view.getAdapter() instanceof MsgHistoryViewAdapter adapter) {
            adapter.submitList(data);
            adapter.notifyItemRangeChanged(0, data.size());
        }
    }
}
