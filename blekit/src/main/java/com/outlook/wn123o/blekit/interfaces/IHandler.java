package com.outlook.wn123o.blekit.interfaces;

import androidx.annotation.NonNull;

public interface IHandler {
    void post(@NonNull Runnable task);
    void postDelayed(@NonNull Runnable task, long delayed);
    void postAtTime(@NonNull Runnable task, long time);
}
