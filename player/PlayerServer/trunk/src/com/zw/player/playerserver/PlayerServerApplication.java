package com.zw.player.playerserver;

import org.apache.log4j.Logger;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

import com.xormedia.mylib.MyApplication;
import com.xormedia.mylib.fontsize.DisplayUtil;
import com.xormedia.mylib.media.AudioPlayer;
import com.zw.player.playerserver.service.DatabaseHelper;
import com.zw.player.playerserver.service.MyPlayer;
import com.zw.player.playerserver.service.socketServerService;

public class PlayerServerApplication extends MyApplication {
  private static Logger Log = Logger.getLogger(PlayerServerApplication.class);
  private static MulticastLock multicastLock;

  @Override
  public void onCreate() {
    Log.info("PlayerServerApplication::onCreate();");
    super.onCreate();
    new DatabaseHelper(getApplicationContext());
    new AudioPlayer(getApplicationContext());
    new MyPlayer(getApplicationContext());
    socketServerService.start(getApplicationContext());

    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    multicastLock = wifiManager
        .createMulticastLock(getPackageName());
    multicastLock.acquire();
    new DisplayUtil(getApplicationContext(),720,1280);
  }

  @Override
  public void quitApp() {
    socketServerService.stop(getApplicationContext());
    if (multicastLock != null) {
      multicastLock.release();
    }
    System.exit(0);
  }

  @Override
  public void setDefaultSettingValue() {
  }

  @Override
  public void setLibPermission() {
  }
}
