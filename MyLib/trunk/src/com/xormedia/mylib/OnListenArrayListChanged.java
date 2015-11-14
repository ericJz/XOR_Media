package com.xormedia.mylib;

import java.util.ArrayList;

public interface OnListenArrayListChanged<M> {
    public void OnChanged(ArrayList<M> list);
}
