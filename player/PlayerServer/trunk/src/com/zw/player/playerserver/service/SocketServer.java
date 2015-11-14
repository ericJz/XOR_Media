package com.zw.player.playerserver.service;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.handler.WeakHandler;
import com.xormedia.socket.MySocket;
import com.xormedia.socket.MySocketServer;
import com.xormedia.socket.SocketMessage;
import com.xormedia.socket.SocketTaskExecution.MySocketServerCallback;

public class SocketServer {
  private static Logger Log = Logger.getLogger(SocketServer.class);
  public static MySocketServer socketServer;
  public static int SOCKET_PORT = 9009;

  public static MySocket socket;

  public static WeakHandler newChannelConnect;
  public static WeakHandler channelClose;
  public static WeakHandler processHandler;

  public static boolean sendMessage(SocketMessage msg) {
    boolean ret = false;
    if (socket != null && socket.isConnected()) {
      ret = socket.sendMsg(msg);
    }
    return ret;
  }

  public static void start() {
    if (socketServer == null) {
      socketServer = new MySocketServer(SOCKET_PORT, new MySocketServerCallback() {

        @Override
        public void onProcess(MySocket channel, SocketMessage msg) {
          if (msg != null && msg.type != null && msg.type.compareTo(SocketMessage.TYPE_REQUEST) == 0 && msg.content != null) {
            if (msg.content.has("CMD") == true) {
              try {
                if (msg.content.getString("CMD").compareTo("newPlaylist") == 0) {
                  boolean ret = false;
                  if (msg.content.has("playlist") == true) {
                    long songsName = System.currentTimeMillis();
                    JSONArray ary = msg.content.getJSONArray("playlist");
                    ret = DatabaseHelper.updateSongs(songsName, ary);
                    if (ret == true) {
                      MyPlayer.setPlayList(songsName);
                    }
                  }
                  SocketMessage rmsg = new SocketMessage();
                  rmsg.type = SocketMessage.TYPE_RESPONSE;
                  rmsg.toIP = channel.toIPAddress;
                  rmsg.CSeq = msg.CSeq;
                  rmsg.content = new JSONObject();
                  rmsg.content.put("CMD", "newPlaylist");
                  if (ret == true) {
                    rmsg.content.put("result", "OK");
                  } else {
                    rmsg.content.put("result", "FAILED");
                  }
                  channel.sendMsg(rmsg);
                  if (processHandler != null) {
                    Message msg1 = new Message();
                    msg1.obj = rmsg;
                    processHandler.sendMessge(msg1);
                  }
                } else if (msg.content.getString("CMD").compareTo("getPlaylist") == 0) {
                  SocketMessage rmsg = new SocketMessage();
                  rmsg.type = SocketMessage.TYPE_RESPONSE;
                  rmsg.toIP = channel.toIPAddress;
                  rmsg.CSeq = msg.CSeq;
                  rmsg.content = new JSONObject();
                  rmsg.content.put("CMD", "getPlaylist");
                  rmsg.content.put("playlist", MyPlayer.getPlaylistToJSONArray());
                  rmsg.content.put("playFile", MyPlayer.getCurrentPlayFileToJSONObject());
                  channel.sendMsg(rmsg);
                  if (processHandler != null) {
                    Message msg1 = new Message();
                    msg1.obj = rmsg;
                    processHandler.sendMessge(msg1);
                  }
                } else if (msg.content.getString("CMD").compareTo("Play") == 0) {
                  boolean ret = false;
                  if (msg.content.has("playFile") == true) {
                    JSONObject obj = msg.content.getJSONObject("playFile");
                    int index = -1;
                    if (msg.content.has("index") == true) {
                      index = msg.content.getInt("index");
                    }
                    MyPlayer.play(obj, index);
                    ret = true;
                  }
                  SocketMessage rmsg = new SocketMessage();
                  rmsg.type = SocketMessage.TYPE_RESPONSE;
                  rmsg.toIP = channel.toIPAddress;
                  rmsg.CSeq = msg.CSeq;
                  rmsg.content = new JSONObject();
                  rmsg.content.put("CMD", "Play");
                  if (ret == true) {
                    rmsg.content.put("result", "OK");
                  } else {
                    rmsg.content.put("result", "FAILED");
                  }
                  channel.sendMsg(rmsg);
                  if (processHandler != null) {
                    Message msg1 = new Message();
                    msg1.obj = rmsg;
                    processHandler.sendMessge(msg1);
                  }
                } else if (msg.content.getString("CMD").compareTo("Pause") == 0) {
                  MyPlayer.pause();
                  SocketMessage rmsg = new SocketMessage();
                  rmsg.type = SocketMessage.TYPE_RESPONSE;
                  rmsg.toIP = channel.toIPAddress;
                  rmsg.CSeq = msg.CSeq;
                  rmsg.content = new JSONObject();
                  rmsg.content.put("CMD", "Pause");
                  rmsg.content.put("result", "OK");
                  channel.sendMsg(rmsg);
                  if (processHandler != null) {
                    Message msg1 = new Message();
                    msg1.obj = rmsg;
                    processHandler.sendMessge(msg1);
                  }
                }
              } catch (JSONException e) {
                ConfigureLog4J.printStackTrace(e, Log);
              }
            }
          }
        }

        @Override
        public void onConnectOK(MySocket channel) {
          if (channel != null) {
            if (socket != null) {
              if (socket.toIPAddress.compareTo(channel.toIPAddress) != 0) {
                socketServer.closeChannel(socket);
                socket = channel;
                if (newChannelConnect != null) {
                  Message msg = new Message();
                  msg.obj = socket;
                  newChannelConnect.sendMessge(msg);
                }
                try {
                  JSONArray ary = MyPlayer.getPlaylistToJSONArray();
                  if (ary != null && ary.length() > 0) {
                    SocketMessage rmsg = new SocketMessage();
                    rmsg.type = SocketMessage.TYPE_ANNOUNCE;
                    rmsg.content = new JSONObject();
                    rmsg.content.put("CMD", "playlistChanged");
                    rmsg.content.put("playlist", ary);
                    socket.sendMsg(rmsg);
                    JSONObject obj = MyPlayer.getCurrentPlayFileToJSONObject();
                    if (obj != null) {
                      SocketMessage rmsg1 = new SocketMessage();
                      rmsg1.type = SocketMessage.TYPE_ANNOUNCE;
                      rmsg1.content = new JSONObject();
                      rmsg1.content.put("CMD", "currentPlayFileChanged");
                      rmsg1.content.put("playFile", obj);
                      socket.sendMsg(rmsg1);
                    }
                  }
                } catch (JSONException e) {
                  ConfigureLog4J.printStackTrace(e, Log);
                }
              }
            } else {
              socket = channel;
              if (newChannelConnect != null) {
                Message msg = new Message();
                msg.obj = socket;
                newChannelConnect.sendMessge(msg);
              }
              try {
                SocketMessage rmsg = new SocketMessage();
                rmsg.type = SocketMessage.TYPE_ANNOUNCE;
                rmsg.content = new JSONObject();
                rmsg.content.put("CMD", "playlistChanged");
                rmsg.content.put("playlist", MyPlayer.getPlaylistToJSONArray());
                socket.sendMsg(rmsg);
                SocketMessage rmsg1 = new SocketMessage();
                rmsg1.type = SocketMessage.TYPE_ANNOUNCE;
                rmsg1.content = new JSONObject();
                rmsg1.content.put("CMD", "currentPlayFileChanged");
                rmsg1.content.put("playFile", MyPlayer.getCurrentPlayFileToJSONObject());
                socket.sendMsg(rmsg1);
              } catch (JSONException e) {
                ConfigureLog4J.printStackTrace(e, Log);
              }
            }
          }
        }

        @Override
        public void onClose(MySocket channel, String reason) {
          if (channel != null && socket != null && socket.toIPAddress.compareTo(channel.toIPAddress) == 0) {
            socket = null;
            if (channelClose != null) {
              Message msg = new Message();
              msg.obj = channel;
              channelClose.sendMessge(msg);
            }
          }
        }

        @Override
        public void onServerConnectOK(int Port) {
          // TODO Auto-generated method stub

        }

        @Override
        public void onServerClose(int Port) {
          // TODO Auto-generated method stub

        }
      });
    } else {
      if (socketServer.isOpen() == true) {
        socketServer.start();
      }
    }
  }

  public static void stop() {
    if (socketServer != null) {
      socketServer.close();
    }
  }
}
