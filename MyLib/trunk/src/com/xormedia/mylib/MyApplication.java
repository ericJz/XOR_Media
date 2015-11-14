package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.content.ComponentCallbacks2;

public abstract class MyApplication extends IApplication {
  private static Logger Log = Logger.getLogger(MyApplication.class);

  @Override
  public void onCreate() {
    super.onCreate();
    new ConfigureLog4J(getApplicationContext());
    new StopWatch();
    StopWatch.start("ApplicationStart");
    Log.info("onCreate()");
    new Pinyin4j();
    Thread.setDefaultUncaughtExceptionHandler(this);
    new AlarmCallBackReceiver(getApplicationContext());
    setDefaultSettingValue();
    setLibPermission();
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    Log.error("[" + thread.getName() + ":" + thread.getId() + "]" + ex.getLocalizedMessage());
    if (ex.getStackTrace() != null) {
      for (int i = 0; i < ex.getStackTrace().length; i++) {
        Log.error("[" + thread.getName() + ":" + thread.getId() + "]" + ex.getStackTrace()[i].toString());
      }
    }
    System.exit(0);
  }

  @Override
  public void onLowMemory() {
    Log.info("onLowMemory()");
    super.onLowMemory();
  }

  @Override
  public void onTrimMemory(int level) {
    switch (level) {
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
        Log.info("onTrimMemory(level=TRIM_MEMORY_RUNNING_LOW)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
        Log.info("onTrimMemory(level=TRIM_MEMORY_BACKGROUND)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
        Log.info("onTrimMemory(level=TRIM_MEMORY_COMPLETE)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
        Log.info("onTrimMemory(level=TRIM_MEMORY_MODERATE)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
        Log.info("onTrimMemory(level=TRIM_MEMORY_RUNNING_CRITICAL)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
        Log.info("onTrimMemory(level=TRIM_MEMORY_RUNNING_MODERATE)");
        break;
      case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
        Log.info("onTrimMemory(level=TRIM_MEMORY_UI_HIDDEN)");
        break;
      default:
        Log.info("onTrimMemory(level=" + level + ")");
        break;
    }
    super.onTrimMemory(level);
  }
}
