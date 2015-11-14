package com.zw.player.playerserver;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zw.player.playerserver.service.socketServerService;

public class LaunchReceiver extends BroadcastReceiver {
  private static Logger Log = Logger.getLogger(LaunchReceiver.class);

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    Log.info("LaunchReceiver:onReceive() action = " + action);
    socketServerService.start(context);
  }
}