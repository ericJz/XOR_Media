package com.xormedia.mylib;

import android.app.Application;

public abstract class IApplication extends Application implements Thread.UncaughtExceptionHandler {
  /**
   * 系统退出后调用的函数
   */
  public abstract void quitApp();

  /**
   * 系统创建的时候调用的设置系统所需默认值得函数
   */
  public abstract void setDefaultSettingValue();

  /**
   * 系统创建的时候调用的设置库权限的函数。
   */
  public abstract void setLibPermission();
}
