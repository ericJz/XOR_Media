package com.zw.player.playerclient.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.xormedia.mylib.handler.WeakHandler;
import com.xormedia.socket.MySocket;
import com.xormedia.socket.MySocketClient;
import com.xormedia.socket.SocketMessage;
import com.xormedia.socket.SocketTaskExecution.MySocketCallback;

public class PlayerServer {
  private static Logger Log = Logger.getLogger(PlayerServer.class);
  private MySocketClient socket = null;
  public String serverIPAddress = null;
  public String serverName = null;
  public String deviceName = null;
  public String deviceBrand = null;
  public int socketPort = 8001;
  protected int databaseIndex = -1;
  private ArrayList<MyFile> fileList = new ArrayList<MyFile>();
  public PlayFile currentPlayFile = null;

  private WeakHandler currentPlayFileChangedHandler = new WeakHandler();

  public void setCurrentPlayFileChangedHandler(Handler handler) {
    currentPlayFileChangedHandler.setHandler(handler);
  }

  private WeakHandler currentPlayFileStatusChangedHandler = new WeakHandler();

  public void setCurrentPlayFileStatusChangedHandler(Handler handler) {
    currentPlayFileStatusChangedHandler.setHandler(handler);
  }

  private WeakHandler currentPlayFileDownloadProgressHandler = new WeakHandler();

  public void setCurrentPlayFileDownloadProcessHandler(Handler handler) {
    currentPlayFileDownloadProgressHandler.setHandler(handler);
  }

  private WeakHandler playlistChangedHandler = new WeakHandler();

  public void setPlaylistChangedHandler(Handler handler) {
    playlistChangedHandler.setHandler(handler);
  }

  private WeakHandler connectHandler = new WeakHandler();

  public void setConnectHandler(Handler handler) {
    connectHandler.setHandler(handler);
  }

  public boolean isConnected() {
    boolean ret = false;
    if (socket != null && socket.isConnected() == true) {
      ret = true;
    }
    return ret;
  }

  public PlayerServer(String _serverIPAddress, String _deviceName, String _deviceBrand, int _socketPort) {
    serverIPAddress = _serverIPAddress;
    deviceName = _deviceName;
    deviceBrand = _deviceBrand;
    serverName = deviceBrand + "-" + deviceName;
    socketPort = _socketPort;
  }

  public PlayerServer(String _serverIPAddress, String _serverName, int _socketPort) {
    serverIPAddress = _serverIPAddress;
    serverName = _serverName;
    socketPort = _socketPort;
  }

  public void connect() {
    if (serverIPAddress != null && serverIPAddress.length() > 0) {
      if (socket == null) {
        socket = new MySocketClient(serverIPAddress, socketPort, new MySocketCallback() {

          @Override
          public void onProcess(MySocket channel, SocketMessage msg) {
            if (msg != null && msg.type.compareTo(SocketMessage.TYPE_ANNOUNCE) == 0 && msg.content != null) {
              try {
                if (msg.content.has("CMD") == true) {
                  if (msg.content.getString("CMD").compareTo("playlistChanged") == 0) {
                    if (msg.content.has("playlist") == true) {
                      _playlistChanged(msg.content.getJSONArray("playlist"));
                    }
                  } else if (msg.content.getString("CMD").compareTo("currentPlayFileChanged") == 0) {
                    if (msg.content.has("playFile") == true) {
                      _currentPlayFileChanged(msg.content.getJSONObject("playFile"));
                    }
                  } else if (msg.content.getString("CMD").compareTo("currentStatusChanged") == 0) {
                    if (msg.content.has("playFile") == true) {
                      _currentPlayFileStatusChanged(msg.content.getJSONObject("playFile"));
                    }
                  } else if (msg.content.getString("CMD").compareTo("currentPlayFileDownload") == 0) {
                    if (msg.content.has("playFile") == true) {
                      _currentPlayFileDownloadProgress(msg.content.getJSONObject("playFile"));
                    }
                  }
                }
              } catch (JSONException e) {
                ConfigureLog4J.printStackTrace(e, Log);
              }
            }
          }

          @Override
          public void onConnectOK(MySocket channel) {
            Message msg = new Message();
            msg.what = 0;
            connectHandler.sendMessge(msg);
          }

          @Override
          public void onClose(MySocket channel, String reason) {
            Message msg = new Message();
            msg.what = 1;
            msg.obj = reason;
            connectHandler.sendMessge(msg);
          }
        });
      } else {
        if (socket.isConnected() == false) {
          socket.start();
        }
      }
    }
  }

  public boolean sendPlayList(Songs playlist, Handler handler) {
    boolean ret = false;
    if (playlist != null && isConnected() == true) {
      try {
        JSONArray array = new JSONArray();
        for (int i = 0; i < playlist.files.size(); i++) {
          array.put(playlist.files.get(i).toJSONObject());
        }
        SocketMessage msg = new SocketMessage();
        msg.type = SocketMessage.TYPE_REQUEST;
        msg.content = new JSONObject();
        msg.content.put("CMD", "newPlaylist");
        msg.content.put("playlist", array);
        if (handler != null) {
          msg.UIHandler = new WeakReference<Handler>(handler);
        }
        ret = socket.sendMsg(msg);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public ArrayList<MyFile> getUIList() {
    ArrayList<MyFile> ret = new ArrayList<MyFile>();
    synchronized (fileList) {
      ret.addAll(fileList);
    }
    return ret;
  }

  private Handler getPlayListHandler = new Handler(Looper.getMainLooper(), new Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (msg.what == 0 && msg.obj != null) {
        SocketMessage msg1 = (SocketMessage) msg.obj;
        if (msg1.response != null && msg1.response.content != null) {
          try {
            if (msg1.response.content.has("playlist") == true) {
              _playlistChanged(msg1.response.content.getJSONArray("playlist"));
            }
            if (msg1.response.content.has("playFile") == true) {
              _currentPlayFileChanged(msg1.response.content.getJSONObject("playFile"));
            }
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
      return false;
    }
  });

  public boolean getPlayList() {
    boolean ret = false;
    if (socket != null && socket.isConnected()) {
      try {
        SocketMessage msg = new SocketMessage();
        msg.type = SocketMessage.TYPE_REQUEST;
        msg.content = new JSONObject();
        msg.content.put("CMD", "getPlaylist");
        if (getPlayListHandler != null) {
          msg.UIHandler = new WeakReference<Handler>(getPlayListHandler);
        }
        ret = socket.sendMsg(msg);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  private void _playlistChanged(JSONArray array) {
    if (array != null) {
      synchronized (fileList) {
        fileList.clear();
        try {
          for (int i = 0; i < array.length(); i++) {
            MyFile tmp = new MyFile(array.getJSONObject(i));
            fileList.add(tmp);
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      Message msg = new Message();
      msg.what = 0;
      msg.obj = getUIList();
      playlistChangedHandler.sendMessge(msg);
    }
  }

  private void _currentPlayFileChanged(JSONObject obj) {
    if (obj != null) {
      synchronized (fileList) {
        currentPlayFile = new PlayFile(obj);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = currentPlayFile;
        currentPlayFileChangedHandler.sendMessge(msg);
      }
    }
  }

  private void _currentPlayFileStatusChanged(JSONObject obj) {
    if (obj != null) {
      synchronized (fileList) {
        currentPlayFile = new PlayFile(obj);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = currentPlayFile;
        currentPlayFileStatusChangedHandler.sendMessge(msg);
      }
    }
  }

  private void _currentPlayFileDownloadProgress(JSONObject obj) {
    if (obj != null) {
      synchronized (fileList) {
        currentPlayFile = new PlayFile(obj);
        Message msg = new Message();
        msg.what = 0;
        msg.obj = currentPlayFile;
        currentPlayFileDownloadProgressHandler.sendMessge(msg);
      }
    }
  }

  public boolean sendPlay(MyFile file, int index, Handler handler) {
    boolean ret = false;
    if (socket != null && socket.isConnected()) {
      try {
        SocketMessage msg = new SocketMessage();
        msg.type = SocketMessage.TYPE_REQUEST;
        msg.content = new JSONObject();
        msg.content.put("CMD", "Play");
        msg.content.put("playFile", file.toJSONObject());
        msg.content.put("index", index);
        if (handler != null) {
          msg.UIHandler = new WeakReference<Handler>(handler);
        }
        ret = socket.sendMsg(msg);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public boolean sendPause(Handler handler) {
    boolean ret = false;
    if (socket != null && socket.isConnected()) {
      try {
        SocketMessage msg = new SocketMessage();
        msg.type = SocketMessage.TYPE_REQUEST;
        msg.content = new JSONObject();
        msg.content.put("CMD", "Pause");
        if (handler != null) {
          msg.UIHandler = new WeakReference<Handler>(handler);
        }
        ret = socket.sendMsg(msg);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public void shutdown() {
    if (socket != null && socket.isConnected()) {
      socket.close();
    }
    synchronized (fileList) {
      fileList.clear();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (socket != null && socket.isConnected() == false) {
      socket.close();
      socket = null;
    }
    super.finalize();
  }
}
