package com.zw.player.playerclient.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.Toast;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyToast;
import com.xormedia.socket.MulticastSocketMessage;
import com.xormedia.socket.MyMulticastSocketServer;
import com.xormedia.socket.SocketTaskExecution.MyMulticastSocketCallback;

public class PlayerServerList {
  private static Logger Log = Logger.getLogger(PlayerServerList.class);

  public static MyMulticastSocketServer multicastSocketServer = null;

  public static String FIND_SERVER = "Search Player Server.";
  public static String I_AM_SERVER = "I'm Player Server!";

  public static int MULTICAST_PORT = 8124;
  public static int SERVER_MULTICAST_PORT = 8123;
  private static Context mContext;

  private static WeakReference<Handler> weakPlayerServerListChangeHandler = null;

  public static ArrayList<PlayerServer> list;

  private static BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (checkWIFIConnect(context)) {
        startMulticastSocketServer();
      } else {
        stopMulticastSocketServer();
      }
    }
  };

  private static boolean checkWIFIConnect(Context context) {
    boolean ret = false;
    ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

  public PlayerServerList(Context context) {
    mContext = context;
    if (list == null) {
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
      mContext.registerReceiver(connectionReceiver, intentFilter);
      String SQL = "SELECT * FROM " + DatabaseHelper.SQL_PLAYER_SERVER_TABLE;
      list = DatabaseHelper.getPlayerServerListBySQL(SQL);
    }
  }

  public static boolean deletePlayerServer(PlayerServer obj) {
    boolean ret = false;
    for (int i = 0; i < list.size(); i++) {
      if (list.get(i) == obj) {
        list.remove(i);
        ret = true;
        break;
      }
    }
    if (ret == true) {
      if (obj.databaseIndex >= 0) {
        String SQL = "DELETE FROM " + DatabaseHelper.SQL_PLAYER_SERVER_TABLE;
        SQL += " WHERE _id=" + obj.databaseIndex;
        DatabaseHelper.execSQL(SQL);
      }
      sendMessagePlayerServerListChangeHandler();
    }
    return ret;
  }

  public static boolean updatePlayerServer(PlayerServer obj) {
    boolean ret = false;
    String SQL = "SELECT * FROM " + DatabaseHelper.SQL_PLAYER_SERVER_TABLE;
    SQL += " WHERE " + DatabaseHelper.SQL_PLAYER_SERVER_IPADDRESS + "=\"" + obj.serverIPAddress + "\"";
    SQL += " AND " + DatabaseHelper.SQL_PLAYER_SERVER_SOCKETPORT + "=\"" + obj.socketPort + "\"";
    if (obj.databaseIndex >= 0) {
      SQL += " AND _id!=" + obj.databaseIndex;
    }
    ArrayList<PlayerServer> list = DatabaseHelper.getPlayerServerListBySQL(SQL);
    if (list.size() == 0) {
      boolean found = false;
      for (int i = 0; i < list.size(); i++) {
        if (list.get(i) != obj &&
            list.get(i).serverIPAddress.compareTo(obj.serverIPAddress) == 0 &&
            list.get(i).socketPort == obj.socketPort) {
          found = true;
          break;
        }
      }
      if (found == false) {
        ret = DatabaseHelper.updatePlayerServer(obj);
        if (ret == true) {
          if (obj.databaseIndex == -1) {
            list = DatabaseHelper.getPlayerServerListBySQL(SQL);
            if (list.size() > 0) {
              obj.databaseIndex = list.get(0).databaseIndex;
              list.clear();
            }
          }
          for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == obj) {
              found = true;
              break;
            }
          }
          if (found == false) {
            list.add(obj);
          }
          sendMessagePlayerServerListChangeHandler();
        }
      }
    }
    list.clear();
    return ret;
  }

  private static void startMulticastSocketServer() {
    if (multicastSocketServer == null) {
      multicastSocketServer = new MyMulticastSocketServer(MULTICAST_PORT, new MyMulticastSocketCallback() {
        @Override
        public void onProcess(int _multicastPort, MulticastSocketMessage msg) {
          if (_multicastPort == MULTICAST_PORT && msg != null && msg.isBroadMessage == true && msg.content != null && msg.content.length() > 0) {
            try {
              JSONObject obj = new JSONObject(msg.content);
              if (obj.has("msg") == true && obj.getString("msg").compareTo(I_AM_SERVER) == 0) {
                PlayerServer playerServer = new PlayerServer(msg.fromAddress.getHostAddress(), obj.getString("deviceName"),
                    obj.getString("deviceBrand"), obj.getInt("socketPort"));
                boolean found = false;
                for (int i = 0; i < list.size(); i++) {
                  if (list.get(i).serverIPAddress.compareTo(playerServer.serverIPAddress) == 0
                      && list.get(i).socketPort == playerServer.socketPort) {
                    found = true;
                    break;
                  }
                }
                if (found == false) {
                  list.add(playerServer);
                  sendMessagePlayerServerListChangeHandler();
                }
              }
            } catch (JSONException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        }

        @Override
        public void onConnectOK(int _multicastPort) {
          try {
            JSONObject _obj = new JSONObject();
            _obj.put("msg", FIND_SERVER);
            _obj.put("deviceName", android.os.Build.MODEL);
            _obj.put("deviceBrand", android.os.Build.BRAND);
            MulticastSocketMessage _msg = new MulticastSocketMessage(SERVER_MULTICAST_PORT, _obj.toString());
            multicastSocketServer.sendMessage(_msg);
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }

        @Override
        public void onClose(int _multicastPort) {
          if (multicastSocketServer != null) {
            multicastSocketServer.close();
          }
        }
      });
    } else {
      if (multicastSocketServer.isConnected() == false) {
        multicastSocketServer.start();
      }
    }
  }

  private static void stopMulticastSocketServer() {
    if (multicastSocketServer != null) {
      multicastSocketServer.close();
    }
  }

  public static void setListenPlayerServerListChangeHandler(Handler handler) {
    if (weakPlayerServerListChangeHandler == null) {
      if (handler != null) {
        weakPlayerServerListChangeHandler = new WeakReference<Handler>(handler);
      }
    } else {
      synchronized (weakPlayerServerListChangeHandler) {
        if (handler != null) {
          weakPlayerServerListChangeHandler = new WeakReference<Handler>(handler);
        } else {
          weakPlayerServerListChangeHandler = null;
        }
      }
    }
  }

  public static void sendMessagePlayerServerListChangeHandler() {
    if (weakPlayerServerListChangeHandler != null) {
      synchronized (weakPlayerServerListChangeHandler) {
        if (weakPlayerServerListChangeHandler != null) {
          Handler handler = weakPlayerServerListChangeHandler.get();
          if (handler != null) {
            handler.sendEmptyMessage(0);
          } else {
            weakPlayerServerListChangeHandler = null;
          }
        }
      }
    }
  }

  public synchronized static void searchPlayerServer(Handler playerServerListChangeHandler) {
    if (playerServerListChangeHandler != null) {
      setListenPlayerServerListChangeHandler(playerServerListChangeHandler);
    }
    if (multicastSocketServer == null) {
      startMulticastSocketServer();
    } else {
      try {
        JSONObject _obj = new JSONObject();
        _obj.put("msg", FIND_SERVER);
        _obj.put("deviceName", android.os.Build.MODEL);
        _obj.put("deviceBrand", android.os.Build.BRAND);
        MulticastSocketMessage _msg = new MulticastSocketMessage(SERVER_MULTICAST_PORT, _obj.toString());
        multicastSocketServer.sendMessage(_msg);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  public static void close() {
    if (list != null) {
      list.clear();
    }
    if (multicastSocketServer != null) {
      multicastSocketServer.close();
      multicastSocketServer = null;
    }
  }

}
