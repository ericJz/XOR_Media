package com.xormedia.mylib;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MyRunLastHandler extends Handler {
  // private static Logger Log = Logger.getLogger(MyRunLastHandler.class);

  private long _sendTime = 0l;
  private boolean isCancel = false;

  public MyRunLastHandler() {
    super();
  }

  public MyRunLastHandler(Looper looper) {
    super(looper);
  }

  public MyRunLastHandler(Callback callback) {
    super(callback);
  }

  public MyRunLastHandler(Looper looper, Callback callback) {
    super(looper, callback);
  }

  @Override
  public void handleMessage(Message msg) {
    long msgSendTime = 0;
    if (msg != null) {
      Bundle data = msg.getData();
      if (data != null && data.getLong("__sendTime__") > 0) {
        msgSendTime = data.getLong("__sendTime__");
      }
    }
    // Log.info("handleMessage _sendTime=" + _sendTime + "; msgSendTime=" +
    // msgSendTime + "; isCancel =" + isCancel);
    if (msgSendTime == _sendTime && isCancel == false) {
      super.handleMessage(msg);
    }
  }

  @Override
  public void dispatchMessage(Message msg) {
    long msgSendTime = 0;
    if (msg != null) {
      Bundle data = msg.getData();
      if (data != null && data.getLong("__sendTime__") > 0) {
        msgSendTime = data.getLong("__sendTime__");
      }
    }
    // Log.info("dispatchMessage _sendTime=" + _sendTime + "; msgSendTime=" +
    // msgSendTime + "; isCancel =" + isCancel);
    if (msgSendTime == _sendTime && isCancel == false) {
      super.dispatchMessage(msg);
    }
  }

  @Override
  public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
    if (msg != null) {
      _sendTime = System.nanoTime();
      Bundle data = msg.getData();
      if (data == null) {
        data = new Bundle();
      }
      data.putLong("__sendTime__", _sendTime);
      msg.setData(data);
      isCancel = false;
      // Log.info("sendMessageAtTime _sendTime=" + _sendTime + "; isCancel =" +
      // isCancel);
    }
    return super.sendMessageAtTime(msg, uptimeMillis);
  }

  public void cancel() {
    isCancel = true;
  }
}
