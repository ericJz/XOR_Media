package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class BroadcastNotice extends Intent {
  protected static Logger Log = Logger.getLogger(BroadcastNotice.class);
  public static final String APPLICATION_NAME = "com.xormedia.mylib.BroadcastNotice.application";
  private Context mContext;

  public BroadcastNotice(Context context) {
    super();
    mContext = context;
    putExtra(APPLICATION_NAME, context.getApplicationInfo().packageName);
  }

  @Override
  public Intent putExtras(Bundle extras) {
    Intent ret = null;
    if (extras != null && mContext != null) {
      extras.putString(APPLICATION_NAME, mContext.getApplicationInfo().packageName);
      ret = super.putExtras(extras);
    }
    return ret;
  }

  public static boolean isCurrentApplictionNotice(Context context, Intent intent) {
    boolean ret = false;
    if (intent != null && intent.getExtras() != null && intent.getExtras().isEmpty() == false
        && intent.getExtras().getString(APPLICATION_NAME) != null
        && intent.getExtras().getString(APPLICATION_NAME).compareTo(context.getApplicationInfo().packageName) == 0) {
      Log.info("BroadcastNotice.isCurrentApplictionNotice::" + context.getApplicationInfo().packageName);
      ret = true;
    }
    return ret;
  }
}
