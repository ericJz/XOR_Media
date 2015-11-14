package com.zw.player.playerclient.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import com.xormedia.mylib.smb.MySmb;
import com.xormedia.mylib.smb.MySmbServer;

public class SmbServerList {
  public static ArrayList<MySmbServer> smbServerList = null;
  public static Context mContext = null;
  private static WeakReference<Handler> changedHandler;

  public SmbServerList(Context _context) {
    mContext = _context;
    if (smbServerList == null) {
      String SQL = "SELECT * FROM " + DatabaseHelper.SQL_SMB_SERVER_TABLE;
      smbServerList = DatabaseHelper.getSmbServerListBySQL(SQL);
    }
  }

  public static void setListChangedHandler(Handler handler) {
    if (changedHandler == null) {
      if (handler != null) {
        changedHandler = new WeakReference<Handler>(handler);
      }
    } else {
      synchronized (changedHandler) {
        if (handler != null) {
          changedHandler = new WeakReference<Handler>(handler);
        } else {
          changedHandler = null;
        }
      }
    }
  }

  public static void sendListChangedHandler() {
    if (changedHandler != null) {
      synchronized (changedHandler) {
        if (changedHandler != null) {
          Handler handler = changedHandler.get();
          if (handler != null) {
            handler.sendEmptyMessage(0);
          } else {
            changedHandler = null;
          }
        }
      }
    }
  }

  private static Handler subHandler = new Handler(new Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (msg.obj != null) {
        boolean hasChanged = false;
        @SuppressWarnings("unchecked")
        ArrayList<MySmbServer> list = (ArrayList<MySmbServer>) msg.obj;
        for (int i = 0; i < list.size(); i++) {
          MySmbServer obj = list.get(i);
          boolean found = false;
          for (int j = 0; j < smbServerList.size(); j++) {
            if (obj.name != null && obj.name.compareTo(smbServerList.get(j).name) == 0) {
              found = true;
              break;
            }
          }
          if (found == false && obj.name != null) {
            smbServerList.add(obj);
            hasChanged = true;
          }
        }
        list.clear();
        if (hasChanged == true) {
          sendListChangedHandler();
        }
      }
      return false;
    }
  });

  /**
   * 使用手动刷新时使用
   * 
   */
  public static void searchLocalNetwork() {
    MySmb.scanSmbServerList(subHandler);
  }

  public static boolean deleteSmbServer(MySmbServer obj) {
    boolean ret = false;
    for (int i = 0; i < smbServerList.size(); i++) {
      if (smbServerList.get(i) == obj) {
        smbServerList.remove(i);
        ret = true;
        break;
      }
    }
    if (ret == true) {
      if (obj.databaseIndex >= 0) {
        String SQL = "DELETE FROM " + DatabaseHelper.SQL_SMB_SERVER_TABLE;
        SQL += " WHERE _id=" + obj.databaseIndex;
        DatabaseHelper.execSQL(SQL);
      }
      sendListChangedHandler();
    }
    return ret;
  }

  public static boolean updateSmbServer(MySmbServer obj) {
    boolean ret = false;
    if (obj.name != null && obj.name.length() > 0 && obj.rootPath != null
        && obj.rootPath.length() > 0) {
      String SQL = "SELECT * FROM " + DatabaseHelper.SQL_SMB_SERVER_TABLE;
      SQL += " WHERE " + DatabaseHelper.SQL_SMB_SERVER_NAME + "=\"" + obj.name + "\"";
      if (obj.databaseIndex >= 0) {
        SQL += " AND _id!=" + obj.databaseIndex;
      }
      ArrayList<PlayerServer> list = DatabaseHelper.getPlayerServerListBySQL(SQL);
      if (list.size() == 0) {
        boolean found = false;
        for (int i = 0; i < smbServerList.size(); i++) {
          if (smbServerList.get(i) != obj &&
              smbServerList.get(i).name.compareTo(obj.name) == 0) {
            found = true;
            break;
          }
        }
        if (found == false) {
          ret = DatabaseHelper.updateSmbServer(obj);
          if (ret == true) {
            if (obj.databaseIndex == -1) {
              list = DatabaseHelper.getPlayerServerListBySQL(SQL);
              if (list.size() > 0) {
                obj.databaseIndex = list.get(0).databaseIndex;
                list.clear();
              }
            }
            for (int i = 0; i < smbServerList.size(); i++) {
              if (smbServerList.get(i) == obj) {
                found = true;
                break;
              }
            }
            if (found == false) {
              smbServerList.add(obj);
            }
            sendListChangedHandler();
          }
        }
      }
      list.clear();
    }
    return ret;
  }
}
