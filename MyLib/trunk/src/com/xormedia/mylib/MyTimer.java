package com.xormedia.mylib;

import java.util.Date;

import org.apache.log4j.Logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MyTimer {
  private static Logger Log = Logger.getLogger(MyTimer.class);
  private Context mContext = null;
  private String name = null;
  private AlarmManager alarmManager = null;
  private long timerId = 0;
  private PendingIntent pendingIntent = null;
  private static int index;

  public static interface MyTimerRunable {
    public void run(Context context);
  }

  public MyTimer(Context context, String _name) {
    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    mContext = context;
    name = _name;
  }

  public void schedule(MyTimerRunable myTimerRunable, Date when) {
    if (myTimerRunable != null && when != null && alarmManager != null && mContext != null) {
      Log.info("schedule["+(name != null ? name : "")+"]::when=" + when.toString());
      cancel();
      timerId = AlarmCallBackReceiver.setCallBackHandler(myTimerRunable, name);
      String tmp1 = "MyTimer_" + (name != null ? name : "") + "|" + timerId;
      Log.info("when:IntentAction=" + tmp1);
      Intent intent = new Intent(tmp1);
      Bundle data = new Bundle();
      data.putBoolean("isDelete", true);
      data.putLong("timeId", timerId);
      intent.putExtras(data);
      pendingIntent = PendingIntent.getBroadcast(mContext, ++index, intent, 0);
      alarmManager.set(AlarmManager.RTC_WAKEUP, when.getTime(), pendingIntent);
    }
  }

  public void schedule(MyTimerRunable myTimerRunable, long delay, long period) {
    if (myTimerRunable != null && alarmManager != null && mContext != null) {
      Log.info("schedule["+(name != null ? name : "")+"]::delay=" + new Date(System.currentTimeMillis() + delay).toString() + ";period=" + period);
      cancel();
      timerId = AlarmCallBackReceiver.setCallBackHandler(myTimerRunable, name);
      String tmp1 = "MyTimer_" + (name != null ? name : "") + "|" + timerId;
      Log.info("delay:IntentAction=" + tmp1);
      Intent intent = new Intent(tmp1);
      Bundle data = new Bundle();
      data.putBoolean("isDelete", false);
      data.putLong("timeId", timerId);
      intent.putExtras(data);
      pendingIntent = PendingIntent.getBroadcast(mContext, ++index, intent, 0);
      alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, period, pendingIntent);
    }
  }

  public void cancel() {
    if (alarmManager != null && mContext != null) {
      Log.info("cancel["+(name != null ? name : "")+"]");
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent);
        pendingIntent = null;
      }
      if (timerId != 0) {
        AlarmCallBackReceiver.removeCallBackHandlerById(timerId);
        timerId = 0;
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    cancel();
    super.finalize();
  }

}
