package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class MyToast {
  public static Handler mhander = new Handler(Looper.getMainLooper());
  public static Context mContext = null;
  private static Logger Log = Logger.getLogger(MyToast.class);

  public MyToast(Context context) {
    mContext = context;
  }

  public static void show(final CharSequence content, final int duration) {
    if (mContext != null) {
      mhander.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(mContext, content, duration).show();
        }
      });
    } else {
      Log.info("MyToast.mApp is null!");
    }
  }

}
