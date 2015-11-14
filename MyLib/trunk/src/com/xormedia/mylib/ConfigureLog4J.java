package com.xormedia.mylib;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.app.ActivityManager;
import android.content.Context;
import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Call {@link #configure()} from your application's activity.
 */

public class ConfigureLog4J {
  // private static Logger Log = Logger.getLogger(ConfigureLog4J.class);

  private static Context mContext = null;
  private static File logFolder = null;

  public String getCurProcessName(Context context) {
    int pid = android.os.Process.myPid();
    ActivityManager mActivityManager = (ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
        .getRunningAppProcesses()) {
      if (appProcess.pid == pid) {
        return appProcess.processName;
      }
    }
    return null;
  }

  public ConfigureLog4J(Context context) {
    mContext = context;
    
    logFolder = new File(StoragePathHelper.getRootFilePath() + File.separator + mContext.getPackageName() + File.separator
        + "logs" + File.separator);
    boolean ret = true;
  //  MyToast.show(StoragePathHelper.getRootFilePath(), Toast.LENGTH_LONG);
    if (!logFolder.exists())
      ret = logFolder.mkdirs();
    if (ret == true) {
     // MyToast.show(logFolder.getAbsolutePath() + "创建成功！", Toast.LENGTH_LONG);
      configure(mContext.getPackageName(), getCurProcessName(mContext).replace(".", "_").replace(":", "_") + "_log.txt");
    }
    xhr.start(mContext);
  }

  public static File getLogFolder()
  {
    return logFolder;
  }

  public static void printStackTrace(Throwable e, Logger log) {
    if (e != null && log != null) {
      log.debug(e.getLocalizedMessage());
      if (e.getStackTrace() != null) {
        for (int i = 0; i < e.getStackTrace().length; i++) {
          log.debug(e.getStackTrace()[i].toString());
        }
      }
    }
  }

  private static void configure(String applicationName, String fileName) {

    final LogConfigurator logConfigurator = new LogConfigurator();
    logConfigurator.setFileName(StoragePathHelper.getRootFilePath() + File.separator + applicationName + File.separator + "logs"
        + File.separator + fileName);
    logConfigurator.setRootLevel(Level.DEBUG);
    logConfigurator.setFilePattern("%d [ %t:%r ] - %-5p [%c{2}]-[%l] %m%n");
    logConfigurator.setMaxBackupSize(5);
    logConfigurator.setMaxFileSize(1024 * 1024 * 3);
    logConfigurator.setImmediateFlush(true);
    logConfigurator.setInternalDebugging(true);
    logConfigurator.configure();
  }
}
