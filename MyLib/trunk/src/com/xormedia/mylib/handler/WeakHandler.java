package com.xormedia.mylib.handler;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class WeakHandler {
  public WeakReference<Handler> weakHandler = null;

  public WeakHandler() {

  }

  public WeakHandler(Handler handler) {
    if (handler != null) {
      weakHandler = new WeakReference<Handler>(handler);
    }
  }

  public void setHandler(Handler handler) {
    if (weakHandler == null) {
      if (handler != null) {
        weakHandler = new WeakReference<Handler>(handler);
      }
    } else {
      synchronized (weakHandler) {
        if (handler == null) {
          weakHandler = null;
        } else {
          weakHandler = new WeakReference<Handler>(handler);
        }
      }
    }
  }

  public boolean sendMessge(Message msg) {
    boolean ret = false;
    if (msg != null && weakHandler != null) {
      synchronized (weakHandler) {
        if (weakHandler != null) {
          Handler handler = weakHandler.get();
          if (handler != null) {
            handler.sendMessage(msg);
            ret = true;
          } else {
            weakHandler = null;
          }
        }
      }
    }
    return ret;
  }

}
