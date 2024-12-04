package com.outlook.wn123o.androidblekit.common;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.outlook.wn123o.androidblekit.databinding.ItemMsgMediaBinding;
import com.outlook.wn123o.androidblekit.databinding.ItemMsgTextBinding;

import java.io.File;

public class MsgViewHolder extends RecyclerView.ViewHolder {
    private final ViewDataBinding binding;
    private MsgViewHolder(@NonNull ViewDataBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Msg msg) {
        if (binding instanceof ItemMsgTextBinding textBinding) {
            textBinding.timestamp.setText(ExtensionsKt.timeZone(this, "MM/dd HH:mm:ss"));
            textBinding.setMsg(msg);
        } else if (binding instanceof ItemMsgMediaBinding mediaBinding){
            mediaBinding.timestamp.setText(ExtensionsKt.timeZone(this, "MM/dd HH:mm:ss"));
            mediaBinding.imageView.setImageURI(Uri.fromFile(new File(msg.content)));
        }
    }

    public static MsgViewHolder create(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == Msg.TYPE_TEXT) {
            return new MsgViewHolder(ItemMsgTextBinding.inflate(inflater, parent, false));
        }
        return new MsgViewHolder(ItemMsgMediaBinding.inflate(inflater, parent, false));
    }
}
