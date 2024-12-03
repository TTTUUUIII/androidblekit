package com.outlook.wn123o.blekit.util;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SimplePacketizer {
    private static final String TAG = "DataCoder";

    public static final int SLICE_TYPE_BEGIN = 0;
    public static final int SLICE_TYPE_DATA = 1;
    public static final int SLICE_TYPE_END = 2;
    private static final int CTRL_SLICE_SIZE = 10;
    private static final String EXTENSION_SIMPLE_STRING = "str";
    private int packetSizeInBytes;

    private final Callback callback;

    public SimplePacketizer(Callback callback) {
        this(callback, 20);
    }

    public SimplePacketizer(Callback callback, int packetSizeInBytes) {
        this.callback = callback;
        this.packetSizeInBytes = packetSizeInBytes;
    }

    public void setPacketSizeInBytes(int size) {
        packetSizeInBytes = size;
    }

    public void encode(@NonNull String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        callback.onTx(begin(EXTENSION_SIMPLE_STRING, data.length), SLICE_TYPE_BEGIN);
        int frameCount = (int) Math.ceil((double) data.length / packetSizeInBytes);
        for (int i = 0; i < frameCount; ++i) {
            int left = packetSizeInBytes * i;
            byte[] frame = left + packetSizeInBytes < data.length ? new byte[packetSizeInBytes] : new byte[data.length - left];
            System.arraycopy(data, left, frame, 0, frame.length);
            callback.onTx(frame, SLICE_TYPE_DATA);
        }
        callback.onTx(end(), SLICE_TYPE_END);
    }

    public void encode(File file) {
        try(FileInputStream in = new FileInputStream(file)) {
            encode(in, getFileExtension(file.getName()), (int) file.length());
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void encode(@NonNull InputStream in, @NonNull String extension, int length) throws IOException {
        callback.onTx(begin(extension, length), SLICE_TYPE_BEGIN);
        byte[] frame = new byte[packetSizeInBytes];
        int readNumInBytes;
        while ((readNumInBytes = in.read(frame)) != -1) {
            if (readNumInBytes > 0) {
                if (readNumInBytes == frame.length) {
                    callback.onTx(frame.clone(), SLICE_TYPE_DATA);
                } else {
                    byte[] bytes = new byte[readNumInBytes];
                    System.arraycopy(frame, 0, bytes, 0, readNumInBytes);
                    callback.onTx(bytes, SLICE_TYPE_DATA);
                }
            }
        }
        callback.onTx(end(), SLICE_TYPE_END);
    }

    public void decode(byte[] data) {
        if (data.length == CTRL_SLICE_SIZE) {
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

    private byte[] begin(@NonNull String extension, int length) {
        byte[] ctrl = new byte[CTRL_SLICE_SIZE];
        ctrl[0] = 0;
        ctrl[1] = 0;
        ctrl[2] = 0;
        ctrl[3] = (byte) extension.charAt(0);
        ctrl[4] = (byte) extension.charAt(1);
        ctrl[5] = (byte) extension.charAt(2);
        byte[] lengthInBytes = ByteUtils.asByteArray(length);
        System.arraycopy(lengthInBytes, 0, ctrl, 6, lengthInBytes.length);
        return ctrl;
    }

    private byte[] end() {
        byte[] ctrl = new byte[CTRL_SLICE_SIZE];
        ctrl[0] = 0;
        ctrl[1] = 0;
        ctrl[2] = 1;
        return ctrl;
    }

    private String getFileExtension(String filename) {
        int index = filename.lastIndexOf(".");
        return filename.substring(index + 1);
    }

    public interface Callback {
        void onRxBegin(@NonNull String extension, int length);
        void onRx(byte[] slice);
        void onRxEnd();
        void onTx(@NonNull byte[] slice, int sliceType);
    }
}
