package com.outlook.wn123o.androidblekit.common;

import java.util.Objects;

public class Msg {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_FILE = 2;

    public final int type;
    public final String content;

    public Msg(int type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Msg msg = (Msg) o;
        return type == msg.type && Objects.equals(content, msg.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }
}
