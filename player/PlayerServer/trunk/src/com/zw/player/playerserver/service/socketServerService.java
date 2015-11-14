package com.zw.player.playerserver.service;

import org.apache.log4j.Logger;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.widget.Toast;

import com.xormedia.mylib.MyToast;

public class socketServerService extends Service {
  private static Logger Log = Logger.getLogger(socketServerService.class);

  public static boolean start(Context context) {
    boolean ret = false;
    if (context != null) {
      Intent serviceIntent = new Intent(context, socketServerService.class);
      context.startService(serviceIntent);
      ret = true;
    }
    return ret;
  }

  public static boolean stop(Context context) {
    boolean ret = false;
    if (context != null) {
      Intent serviceIntent = new Intent(context, socketServerService.class);
      context.stopService(serviceIntent);
      ret = true;
    }
    return ret;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private static BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (checkWIFIConnect(context)) {
        startSocketServer();
      } else {
        stopSocketServer();
      }
    }
  };

  private static boolean checkWIFIConnect(Context context) {
    boolean ret = false;
    ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo activeInfo = connectMgr.getActiveNetworkInfo();
    if (wifiNetInfo == null || !wifiNetInfo.isConnected()) {
      MyToast.show("WIFI网络无连接，请检查您的网络！", Toast.LENGTH_LONG);
    } else {
      ret = true;
    }
    Log.info("mobile:" + (mobNetInfo != null ? mobNetInfo.isConnected() : "null")
        + ";" + "wifi:" + (wifiNetInfo != null ? wifiNetInfo.isConnected() : "null")
        + ";" + "active:" + (activeInfo != null ? activeInfo.getTypeName() : "null"));
    return ret;
  }

  @Override
  public void onCreate() {
    Log.info("socketServerService:onCreate::onCreate()!");
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(connectionReceiver, intentFilter);
    if (checkWIFIConnect(getApplicationContext())) {
      startSocketServer();
    }
    super.onCreate();
  }

  private static void startSocketServer() {
    MulticastSocketServer.start();
    SocketServer.start();
  }

  private static void stopSocketServer() {
    MulticastSocketServer.stop();
    SocketServer.stop();
  }

  @Override
  public void onDestroy() {
    MulticastSocketServer.stop();
    SocketServer.stop();
    super.onDestroy();
  }

}
