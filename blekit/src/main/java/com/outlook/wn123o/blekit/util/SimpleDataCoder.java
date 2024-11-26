package com.outlook.wn123o.blekit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleDataCoder {
    private static final String TAG = "DataCoder";

    public static final int FRAME_TYPE_BEGIN = 0;
    public static final int FRAME_TYPE_DATA = 1;
    public static final int FRAME_TYPE_END = 2;
    private static final int CTRL_FRAME_SIZE = 10;
    private int chunkSizeInBytes = 20;

    private final Callback callback;

    public SimpleDataCoder(Callback callback) {
        this(callback, 20);
    }

    public SimpleDataCoder(Callback callback, int chunkSize) {
        this.callback = callback;
        this.chunkSizeInBytes = chunkSize;
    }

    public void setChuckSize(int size) {
        chunkSizeInBytes = size;
    }

    public void encode(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        callback.onTx(begin("txt", data.length), FRAME_TYPE_BEGIN);
        int frameCount = (int) Math.ceil((double) data.length / chunkSizeInBytes);
        for (int i = 0; i < frameCount; ++i) {
            int left = chunkSizeInBytes * i;
            byte[] frame = left + chunkSizeInBytes < data.length ? new byte[chunkSizeInBytes] : new byte[data.length - left];
            System.arraycopy(data, left, frame, 0, frame.length);
            callback.onTx(frame, FRAME_TYPE_DATA);
        }
        callback.onTx(end(), FRAME_TYPE_END);
    }

    public void encode(File file) {
        String name = file.getName();
        int index = name.lastIndexOf(".");
        String type = name.substring(index + 1);
        int length = (int) file.length();
        callback.onTx(begin(type, length), FRAME_TYPE_BEGIN);
        try (FileInputStream in = new FileInputStream(file)){
            byte[] frame = new byte[chunkSizeInBytes];
            int readNumInBytes;
            while ((readNumInBytes = in.read(frame)) != -1) {
                if (readNumInBytes > 0) {
                    if (readNumInBytes == frame.length) {
                        callback.onTx(frame.clone(), FRAME_TYPE_DATA);
                    } else {
                        byte[] bytes = new byte[readNumInBytes];
                        System.arraycopy(frame, 0, bytes, 0, readNumInBytes);
                        callback.onTx(bytes, FRAME_TYPE_DATA);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        callback.onTx(end(), FRAME_TYPE_END);
    }

    public void decode(byte[] data) {
        if (data.length == CTRL_FRAME_SIZE) {
            int c = data[0] + data[1] + data[2];
            if (c == 0) {
                callback.onRxBegin(String.format("%c%c%c", data[3], data[4], data[5]), ByteUtils.asInt(data, 6));
            } else if (c == 1) {
                callback.onRxEnd();
            } else {
                callback.onRx(data);
            }
        } else {
            callback.onRx(data);
        }
    }

    public boolean isEndFrame(byte[] data) {
        if (data.length == CTRL_FRAME_SIZE) {
            int c = data[0] + data[1] + data[2];
            return c == 1;
        }
        return false;
    }

    private byte[] begin(String type, int length) {
        byte[] ctrl = new byte[CTRL_FRAME_SIZE];
        ctrl[0] = 0;
        ctrl[1] = 0;
        ctrl[2] = 0;
        ctrl[3] = (byte) type.charAt(0);
        ctrl[4] = (byte) type.charAt(1);
        ctrl[5] = (byte) type.charAt(2);
        byte[] lengthInBytes = ByteUtils.asByteArray(length);
        System.arraycopy(lengthInBytes, 0, ctrl, 6, lengthInBytes.length);
        return ctrl;
    }

    private byte[] end() {
        byte[] ctrl = new byte[CTRL_FRAME_SIZE];
        ctrl[0] = 0;
        ctrl[1] = 0;
        ctrl[2] = 1;
        return ctrl;
    }

    public interface Callback {
        void onRxBegin(String type, int length);
        void onRx(byte[] data);
        void onRxEnd();
        void onTx(byte[] data, int frameType);
    }
}
