package com.zw.player.playerserver.service;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.socket.MulticastSocketMessage;
import com.xormedia.socket.MyMulticastSocketServer;
import com.xormedia.socket.SocketTaskExecution.MyMulticastSocketCallback;

public class MulticastSocketServer {
  private static Logger Log = Logger.getLogger(MulticastSocketServer.class);
  public static MyMulticastSocketServer multicastSocketServer;

  public static String FIND_SERVER = "Search Player Server.";
  public static String I_AM_SERVER = "I'm Player Server!";

  public static int MULTICAST_PORT = 8123;
  public static int CLIENT_MULTICAST_PORT = 8124;


  public static void start() {
    if (multicastSocketServer == null) {
      multicastSocketServer = new MyMulticastSocketServer(MULTICAST_PORT, new MyMulticastSocketCallback() {
        @Override
        public void onProcess(int _multicastPort, MulticastSocketMessage msg) {
          if (_multicastPort == MULTICAST_PORT && msg != null && msg.isBroadMessage == true && msg.content != null && msg.content.length() > 0) {
            try {
              JSONObject obj = new JSONObject(msg.content);
              if (obj.has("msg") == true && obj.getString("msg").compareTo(FIND_SERVER) == 0) {
                JSONObject _obj = new JSONObject();
                _obj.put("msg", I_AM_SERVER);
                _obj.put("deviceName", android.os.Build.MODEL);
                _obj.put("deviceBrand", android.os.Build.BRAND);
                _obj.put("socketPort", SocketServer.SOCKET_PORT);
                MulticastSocketMessage _msg = new MulticastSocketMessage(CLIENT_MULTICAST_PORT, _obj.toString());
                multicastSocketServer.sendMessage(_msg);
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
            _obj.put("msg", I_AM_SERVER);
            _obj.put("deviceName", android.os.Build.MODEL);
            _obj.put("deviceBrand", android.os.Build.BRAND);
            _obj.put("socketPort", SocketServer.SOCKET_PORT);
            MulticastSocketMessage _msg = new MulticastSocketMessage(CLIENT_MULTICAST_PORT, _obj.toString());
            multicastSocketServer.sendMessage(_msg);
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }

        @Override
        public void onClose(int _multicastPort) {

        }
      });
    } else {
      if (multicastSocketServer.isConnected() == false) {
        multicastSocketServer.start();
      }
    }
  }

  public static void stop() {
    if (multicastSocketServer != null) {
      multicastSocketServer.close();
    }
  }
}
