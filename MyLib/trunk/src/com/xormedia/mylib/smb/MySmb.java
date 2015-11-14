package com.xormedia.mylib.smb;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyThread;
import com.xormedia.mylib.MyThread.myRunable;
import com.xormedia.mylib.xhr;

public class MySmb {
  private static Logger Log = Logger.getLogger(MySmb.class);

  /**
   * 返回本机IP地址
   * 
   * @return
   */
  public static String getLocIPAddress(int mode) {
    String locIPAddress = "";
    Enumeration<NetworkInterface> interfacelist;
    try {
      interfacelist = NetworkInterface.getNetworkInterfaces();
      if (interfacelist == null) {
        System.out.println("no networkinterfaces found.");
      } else {
        while (interfacelist.hasMoreElements() && locIPAddress.equals("")) {
          NetworkInterface network = interfacelist.nextElement();
          // 得到某一个网路接口里所有的ip
          Enumeration<InetAddress> ipAddresslist = network.getInetAddresses();
          Log.info("network.getName()=" + network.getName());
          Log.info("network.getDisplayName()=" + network.getDisplayName());
          if (xhr.CONNECT_TYPE_WIFI == mode) {
            if (network.getName() != null && network.getName().indexOf("wlan") < 0) {
              continue;
            }
          }
          if (!ipAddresslist.hasMoreElements()) {
            continue;
          }

          // 遍历某一个网络接口下所有的ipAddress
          while (ipAddresslist.hasMoreElements() && locIPAddress.equals("")) {
            InetAddress ipAddress = ipAddresslist.nextElement();
            Log.info("ipAddress.isSiteLocalAddress=" + ipAddress.isSiteLocalAddress());
            if (ipAddress instanceof Inet4Address
                && !ipAddress.isLoopbackAddress() && ipAddress.getHostAddress() != null
                && !ipAddress.getHostAddress().equals("")) {
              Log.info("ipAddress.getHostAddress=" + ipAddress.getHostAddress());
              locIPAddress = ipAddress.getHostAddress();
            }
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }
    Log.info("getLocIPAddress=" + locIPAddress);
    return locIPAddress;
  }

  private static MyThread scanSmbServerListInThread = new MyThread(new myRunable() {

    @Override
    public void run(Message msg) {
      final ArrayList<MySmbServer> list = new ArrayList<MySmbServer>();
      class pingThread extends Thread {
        int start = 0;
        int end = 0;
        String ipAddress = null;
        boolean isFinish = false;

        @Override
        public void run() {
          super.run();
          for (int i = start; i <= end; i++) {
            MySmbServer server = new MySmbServer("smb://" + ipAddress + "." + i + "/");
            if (server.ping() == true) {
              synchronized (list) {
                list.add(server);
              }
            }
          }
          isFinish = true;
        }
      }
      String ipAddress = getLocIPAddress(xhr.mConnectType);
      if (ipAddress != null) {
        ipAddress = ipAddress.substring(0, ipAddress.lastIndexOf("."));
        Log.info(ipAddress);
        ArrayList<pingThread> pingList = new ArrayList<pingThread>();
        long time = System.currentTimeMillis();
        for (int i = 1; i < 255; i += 1) {
          pingThread ping = new pingThread();
          ping.start = i;
          if (i + 0 > 254) {
            ping.end = 254;
          } else {
            ping.end = i + 0;
          }
          ping.ipAddress = ipAddress;
          pingList.add(ping);
          ping.start();
          try {
            Thread.sleep(5);
          } catch (InterruptedException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
        for (int i = 0; i < pingList.size(); i++) {
          while (pingList.get(i).isFinish == false) {
            int sleepTime = 5000;
            if (System.currentTimeMillis() - time > 5000) {
              sleepTime = 500;
            }
            try {
              Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        }
        Log.info("耗时：" + ((System.currentTimeMillis() - time) / 1000));
        pingList.clear();
      }
      msg.obj = list;
    }
  });


  /**
   * 查找局域網中可以ping得通的ServerList
   * 
   * @param handler
   *          msg.obj=ArrayList<MySmbServer>
   */
  public static void scanSmbServerList(Handler handler) {
    scanSmbServerListInThread.start(handler);
  }

  private static MyThread getLeavesThread;

  private static class getLeavesRun implements myRunable {
    private ArrayList<MySmbFile> list = null;
    private boolean isOnlyFile = false;

    public getLeavesRun(ArrayList<MySmbFile> _list, boolean _isOnlyFile) {
      list = _list;
      isOnlyFile = _isOnlyFile;
    }

    @Override
    public void run(Message msg) {
      synchronized (getLeavesThread) {
        JSONArray arry = new JSONArray();
        try {
          for (int i = 0; i < list.size(); i++) {
            JSONArray subArry = list.get(i).getLeavesJSONObjectInThread(isOnlyFile);
            if (subArry.length() > 0) {
              for (int j = 0; j < subArry.length(); j++) {
                arry.put(subArry.getJSONObject(j));
              }
            }
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        msg.what = 0;
        msg.obj = arry;
        getLeavesThread = null;
      }
    }
  }

  /**
   * 獲取所有最底層的文件
   * 
   * @param list
   *          所要獲取的父節點文件列表
   * @param isOnlyFile
   *          是否只获取文件
   * @param handler
   *          msg.obj= JSONArray
   */
  public static void getLeaves(ArrayList<MySmbFile> list, boolean isOnlyFile, Handler handler) {
    if (getLeavesThread == null) {
      getLeavesThread = new MyThread(new getLeavesRun(list, isOnlyFile));
      synchronized (getLeavesThread) {
        getLeavesThread.start(handler);
      }
    }
  }

  private static MyThread getTreesThread;

  private static class getTreesRun implements myRunable {
    private ArrayList<MySmbFile> list = null;

    public getTreesRun(ArrayList<MySmbFile> _list) {
      list = _list;
    }

    @Override
    public void run(Message msg) {
      synchronized (getTreesThread) {
        JSONArray arry = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
          arry.put(list.get(i).getTreeListJSONObjectInThread());
        }
        msg.what = 0;
        msg.obj = arry;
        getTreesThread = null;
      }
    }
  }

  /**
   * 获取文件列表树结构
   * 
   * @param list
   *          所要获取的文件列表
   * @param handler
   *          msg.obj = JSONArray
   */
  public static void getTrees(ArrayList<MySmbFile> list, Handler handler) {
    if (getTreesThread == null) {
      getTreesThread = new MyThread(new getTreesRun(list));
      synchronized (getTreesThread) {
        getTreesThread.start(handler);
      }
    }
  }
}
