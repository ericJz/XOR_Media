package com.xormedia.mylib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.MyThread.myRunable;

public class MySocketMsg {
  private static Logger Log = Logger.getLogger(MySocketMsg.class);
  public static String ATTR_SEND_TIME = "sendTime";
  public static String ATTR_CONTENT = "content";
  public static String ATTR_TO_IPADDRESS = "toIPAddress";
  public static String ATTR_TO_PORT = "toPort";
  public static String ATTR_FROM_IPADDRESS = "fromIPAddress";
  public String toIPAddress = null;
  public int toPort = 0;
  public String fromIPAddress = null;
  public boolean sendOK = false;
  public long sendTime = 0l;
  public String content = null;

  public static String getLocalHostIp() {
    String ipaddress = "";
    try {
      Enumeration<NetworkInterface> en = NetworkInterface
          .getNetworkInterfaces();
      // 遍历所用的网络接口
      while (en.hasMoreElements()) {
        NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
        Enumeration<InetAddress> inet = nif.getInetAddresses();
        // 遍历每一个接口绑定的所有ip
        while (inet.hasMoreElements()) {
          InetAddress ip = inet.nextElement();
          if (!ip.isLoopbackAddress()
              && InetAddressUtils.isIPv4Address(ip
                  .getHostAddress())) {
            return ipaddress = "本机的ip是" + "：" + ip.getHostAddress();
          }
        }
      }
    } catch (SocketException e) {
      Log.info("获取本地ip地址失败");
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ipaddress;
  }

  public MySocketMsg(String _toIPAddress, int _toPort, String _content) {
    toIPAddress = _toIPAddress;
    toPort = _toPort;
    fromIPAddress = getLocalHostIp();
    content = _content;
  }

  public MySocketMsg(JSONObject obj) {
    if (obj != null) {
      if (obj.has(ATTR_FROM_IPADDRESS) == true) {
        fromIPAddress = obj.optString(ATTR_FROM_IPADDRESS);
      }
      if (obj.has(ATTR_TO_IPADDRESS) == true) {
        toIPAddress = obj.optString(ATTR_TO_IPADDRESS);
      }
      if (obj.has(ATTR_TO_PORT) == true) {
        toPort = obj.optInt(ATTR_TO_PORT);
      }
      if (obj.has(ATTR_SEND_TIME) == true) {
        sendTime = obj.optLong(ATTR_SEND_TIME);
      }
      if (obj.has(ATTR_CONTENT) == true) {
        content = obj.optString(ATTR_CONTENT);
      }
      sendOK = true;
    }
  }

  public JSONObject toJSONObject() {
    JSONObject ret = new JSONObject();
    try {
      ret.put(ATTR_SEND_TIME, sendTime);
      if (content != null) {
        ret.put(ATTR_CONTENT, content);
      }
      if (toIPAddress != null) {
        ret.put(ATTR_TO_IPADDRESS, toIPAddress);
      }
      ret.put(ATTR_TO_PORT, toPort);
      if (fromIPAddress != null) {
        ret.put(ATTR_FROM_IPADDRESS, fromIPAddress);
      }
      if (content != null) {
        ret.put(ATTR_CONTENT, content);
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  public boolean send(Handler handler, boolean isInUserThread) {
    boolean ret = false;
    if (isInUserThread == true) {
      ret = sendInThread();
    } else {
      new MyThread(new myRunable() {

        @Override
        public void run(Message msg) {
          if (sendInThread()) {
            msg.what = 0;
          } else {
            msg.what = 1;
          }
        }
      }).start(handler);
    }
    return ret;
  }

  public boolean sendInThread() {
    boolean ret = false;
    if (toIPAddress != null && toPort > 0 && content != null && content.length() > 0) {
      BufferedReader in = null;
      Socket socket = null;
      OutputStream outputStream = null;
      try {
        sendTime = System.nanoTime();
        socket = new Socket(toIPAddress, toPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        outputStream = socket.getOutputStream();
        byte[] tmp = toJSONObject().toString().getBytes();
        Integer tmpLen = tmp.length;
        if (tmpLen > 0) {
          outputStream.write(tmpLen.byteValue());
          outputStream.flush();
          outputStream.write(tmp);
          outputStream.flush();
        }
        int loop = 0;
        String str = "";
        while (!socket.isClosed() && loop < 5) {
          loop++;
          String line;
          while (!socket.isClosed() && (line = in.readLine()) != null) {
            str += line;
          }
          if (str.length() > 0) {
            if (str.compareTo("200") == 0) {
              ret = true;
              break;
            }
          }
          Thread.sleep(1000);
        }
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (InterruptedException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } finally {
        try {
          if (outputStream != null) {
            outputStream.close();
          }
          if (in != null) {
            in.close();
          }
          if (socket != null) {
            socket.close();
          }
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
    return ret;
  }
}
