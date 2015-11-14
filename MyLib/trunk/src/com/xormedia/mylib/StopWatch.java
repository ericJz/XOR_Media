package com.xormedia.mylib;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class StopWatch {
  private static Logger Log = Logger.getLogger(StopWatch.class);

  private static class logTime {
    public String title = null;
    public long time = 0l;
  }

  private static ArrayList<logTime> logTimes = new ArrayList<StopWatch.logTime>();

  public static void start(String title) {
    if (title != null && title.length() > 0) {
      synchronized (logTimes) {
        boolean found = false;
        for (int i = 0; i < logTimes.size(); i++) {
          if (logTimes.get(i).title.compareTo(title) == 0) {
            found = true;
            logTimes.get(i).time = System.currentTimeMillis();
            Log.info("[" + title + "]" + "计时器已存在！重新开始");
            break;
          }
        }
        if (found == false) {
          logTime tmp = new logTime();
          tmp.title = title;
          tmp.time = System.currentTimeMillis();
          logTimes.add(tmp);
          Log.info("[" + tmp.title + "]" + "计时器开始！");
        }
      }
    }

  }

  public static void stop(String title) {
    if (title != null && title.length() > 0) {
      synchronized (logTimes) {
        boolean found = false;
        for (int i = 0; i < logTimes.size(); i++) {
          if (logTimes.get(i).title.compareTo(title) == 0) {
            found = true;
            Log.info("[" + logTimes.get(i).title + "]" + "计时结束，耗时：" + (System.currentTimeMillis() - logTimes.get(i).time) + "毫秒！");
            logTimes.remove(i);
            break;
          }
        }
        if (found == false) {
          Log.info("[" + title + "]" + "计时器未找到！");
        }
      }
    }

  }

}
