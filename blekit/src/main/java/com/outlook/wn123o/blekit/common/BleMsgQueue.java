package com.outlook.wn123o.blekit.common;

import androidx.annotation.Nullable;

import java.util.ArrayDeque;

public class BleMsgQueue extends ArrayDeque<BleMsg> {
    private boolean active = false;

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public synchronized boolean add(BleMsg msg) {
        return super.add(msg);
    }

    @Nullable
    @Override
    public synchronized BleMsg poll() {
        return super.poll();
    }
}
