package com.zw.player.playerserver.service;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import  android.util.Log;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyRunLastHandler;
import com.xormedia.mylib.MyToast;
import com.xormedia.mylib.StoragePathHelper;
import com.xormedia.mylib.cacheFileList.CacheFile;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.xormedia.mylib.handler.WeakHandler;
import com.xormedia.mylib.media.AudioPlayer;
import com.xormedia.mylib.media.IPlayCallBack;
import com.xormedia.socket.SocketMessage;

public class MyPlayer {
  private static Logger Log = Logger.getLogger(MyPlayer.class);
  public static final String STATUS_PLAYING = "playing";
  public static final String STATUS_PAUSE = "pause";
  public static String cacheFolder;
  protected static ArrayList<MyFile> fileList;
  private static CacheFile cacheFilelist;
  private static IPlayCallBack audioPlayerCallBack;
  private static Handler CacheFileHadReadyHandler;
  private static MyRunLastHandler CacheFileDownLoadProcessHandler;
  private static int currentIndex;
  private static String currentStatus;
  private static Thread _thread;
  private static ArrayList<task> tasks;
  /** 线程是否结束的标志 */
  private static AtomicBoolean shutdown;
  private static WeakHandler fileListChangedHandler;
  private static WeakHandler currentPlayFileChangedHandler;
  private static WeakHandler currentStatusChangedHandler;
  private static WeakHandler currentPlayFileDownloadHandler;
  //private static Handler seekbarHandler;

  public static void setFileListChangedHandler(Handler handler) {
    fileListChangedHandler.setHandler(handler);
  }
  
  public static void setCurrentPlayFileChangedHandler(Handler handler) {
    currentPlayFileChangedHandler.setHandler(handler);
  }

  public static void setCurrentStatusChangedHandler(Handler handler) {
    currentStatusChangedHandler.setHandler(handler);
  }

  public static void setCurrentPlayFileDownloadHandler(Handler handler) {
    currentPlayFileDownloadHandler.setHandler(handler);
  }

  
  private static enum taskType {
    none, setPlayList, play, pause, stop
  }

  public static ArrayList<MyFile> getNameList() {
	  ArrayList<MyFile> songs = new ArrayList<MyFile>();
	  synchronized(fileList) {
		  songs.addAll(fileList);
	  }	  
	  return songs;
  }
  
  public static String getCurrentStatus(){
	  return currentStatus;
  }
  
  public static JSONArray getPlaylistToJSONArray() {
    JSONArray array = new JSONArray();
    synchronized (fileList) {
      for (int i = 0; i < fileList.size(); i++) {
        array.put(fileList.get(i).toJSONObject());
      }
    }
    return array;
  }

  public static JSONObject getCurrentPlayFileToJSONObject() {
    JSONObject obj = null;
    synchronized (fileList) {
      if (currentIndex >= 0 && fileList.size() > currentIndex) {
        try {
          obj = fileList.get(currentIndex).toJSONObject();
          obj.put("index", currentIndex);
          obj.put("playStatus", currentStatus);
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
    return obj;
  }

  private static class task {
    public taskType type = taskType.none;
    public long songName = 0l;
    public JSONObject playFile = null;
    public int playFileIndex = -1;
  }

  public MyPlayer(Context context) {
    if (shutdown == null) {
      shutdown = new AtomicBoolean(false);
    }

    if (tasks == null) {
      tasks = new ArrayList<MyPlayer.task>();
    }
    if (fileList == null) {
      fileList = new ArrayList<MyFile>();
    }
    if (_thread == null) {
      _thread = new Thread(run);
      _thread.start();
    }
    if (fileListChangedHandler == null) {
      fileListChangedHandler = new WeakHandler();
    }  
    if (currentPlayFileChangedHandler == null) {
      currentPlayFileChangedHandler = new WeakHandler();
    }
    if (currentStatusChangedHandler == null) {
      currentStatusChangedHandler = new WeakHandler();
    }
    if (currentPlayFileDownloadHandler == null) {
      currentPlayFileDownloadHandler = new WeakHandler();
    }
    DatabaseHelper.getSongList(DatabaseHelper.getCurrentSongsName());
    if (CacheFileHadReadyHandler == null) {
      CacheFileHadReadyHandler = new Handler(Looper.getMainLooper(), new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
          if (msg != null && msg.obj != null && currentIndex >= 0) {
            synchronized (fileList) {
              if (currentStatus.compareTo(STATUS_PLAYING) == 0) {
                MyFile file = (MyFile) msg.obj;
                if (file.storageName != null && fileList.get(currentIndex).fileURL.compareTo(file.fileURL) == 0) {
                  if (msg.what == 0) {
                    fileList.get(currentIndex).downloadProgress = 100;
                    _currentPlayFileDownload_InSynFileList();
                    String url = cacheFolder + "/" + file.storageName;
                    if (AudioPlayer.dataSourceURL != null && url.compareTo(AudioPlayer.dataSourceURL) == 0) {
                      AudioPlayer.play();
                    } else {
                      AudioPlayer.play(url, false);
                    }
                  } else {
                    _playNext_InSynFileList();
                  }
                }
              }
            }
          }
          return false;
        }
      });
    }

    if (CacheFileDownLoadProcessHandler == null) {
      CacheFileDownLoadProcessHandler = new MyRunLastHandler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
          if (msg != null && msg.obj != null) {
            synchronized (fileList) {
              MyFile file = (MyFile) msg.obj;
              if (file.fileURL != null && currentIndex >= 0 && file.fileURL.compareTo(fileList.get(currentIndex).fileURL) == 0) {
                fileList.get(currentIndex).downloadProgress = msg.what;
                Log.info(198);
                _currentPlayFileDownload_InSynFileList();
              }
            }
          }
          return false;
        }
      });
    }

    if (cacheFilelist == null) {
      cacheFolder = StoragePathHelper.getRootFilePath() + "/" + context.getPackageName() + "/cacheFile";
      cacheFilelist = new CacheFile(cacheFolder);
      cacheFilelist.setDownLoadProcessHandler(CacheFileDownLoadProcessHandler);
      cacheFilelist.setHadReadyHandler(CacheFileHadReadyHandler);

    }
    if (audioPlayerCallBack == null) {
      audioPlayerCallBack = new IPlayCallBack() {

        @Override
        public void playPrepared(MediaPlayer mp) {
          // TODO Auto-generated method stub

        }

        @Override
        public void playFinish(MediaPlayer mp) {
          //MyToast.show("MediaPlayer playFinish!", Toast.LENGTH_LONG);
          synchronized (fileList) {
            if (currentStatus.compareTo(STATUS_PLAYING) == 0) {
              _playNext_InSynFileList();
            }
          }
        }

        @Override
        public void playError(MediaPlayer mp, int what, int extra, String errorMsg) {
          MyToast.show("MediaPlayer Error:" + errorMsg, Toast.LENGTH_LONG);
          synchronized (fileList) {
            if (currentStatus.compareTo(STATUS_PLAYING) == 0) {
              _playNext_InSynFileList();
            }
          }
        }
      };

      AudioPlayer.setUserCallbackFunc(audioPlayerCallBack);
    }

    if (fileList.size() > 0) {
      cacheFilelist.setFileList(fileList);
      currentIndex = 0;
      currentStatus = STATUS_PAUSE;
      pause();
    } else {
      currentIndex = -1;
      currentStatus = STATUS_PAUSE;
    }
  }

  private static void _playNext_InSynFileList() {
    int index = 0;
    if (currentIndex < fileList.size() - 1) {
      index = currentIndex + 1;
    }
    JSONObject obj = fileList.get(index).toJSONObject();
    play(obj, index);
  }

  private static void _currentPlayFileChanged_InSynFileList() {
    try {
      SocketMessage rmsg = new SocketMessage();
      rmsg.type = SocketMessage.TYPE_ANNOUNCE;
      rmsg.content = new JSONObject();
      rmsg.content.put("CMD", "currentPlayFileChanged");
      JSONObject obj = fileList.get(currentIndex).toJSONObject();
      obj.put("index", currentIndex);
      obj.put("playStatus", currentStatus);
      rmsg.content.put("playFile", obj);
      SocketServer.sendMessage(rmsg);
      Message msg = new Message();
      msg.obj = fileList.get(currentIndex);
      currentPlayFileChangedHandler.sendMessge(msg);
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  private static void _currentStatusChanged_InSynFileList() {
    try {
      SocketMessage rmsg = new SocketMessage();
      rmsg.type = SocketMessage.TYPE_ANNOUNCE;
      rmsg.content = new JSONObject();
      rmsg.content.put("CMD", "currentStatusChanged");
      JSONObject obj = fileList.get(currentIndex).toJSONObject();
      obj.put("index", currentIndex);
      obj.put("playStatus", currentStatus);
      rmsg.content.put("playFile", obj);
      SocketServer.sendMessage(rmsg);
      Message msg = new Message();
      msg.obj = fileList.get(currentIndex);
      currentStatusChangedHandler.sendMessge(msg);
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  private static void _currentPlayFileDownload_InSynFileList() {
    try {
      SocketMessage rmsg = new SocketMessage();
      rmsg.type = SocketMessage.TYPE_ANNOUNCE;
      rmsg.content = new JSONObject();
      rmsg.content.put("CMD", "currentPlayFileDownload");
      JSONObject obj = fileList.get(currentIndex).toJSONObject();
      obj.put("index", currentIndex);
      obj.put("playStatus", currentStatus);
      rmsg.content.put("playFile", obj);
      SocketServer.sendMessage(rmsg);
      Message msg = new Message();
      msg.obj = fileList.get(currentIndex);
      currentPlayFileDownloadHandler.sendMessge(msg);
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  private static void setPlayListInThread(task _task) {
    if (_task.songName == 0) {
      _task.songName = DatabaseHelper.getCurrentSongsName();
    }
    if (_task.songName > 0) {
      synchronized (fileList) {
        currentStatus = STATUS_PAUSE;
        AudioPlayer.pause();
        DatabaseHelper.getSongList(_task.songName);
        cacheFilelist.setFileList(fileList);
        currentIndex = 0;
        try {
          SocketMessage rmsg = new SocketMessage();
          rmsg.type = SocketMessage.TYPE_ANNOUNCE;
          rmsg.content = new JSONObject();
          rmsg.content.put("CMD", "playlistChanged");
          JSONArray array = new JSONArray();
          for (int i = 0; i < fileList.size(); i++) {
            array.put(fileList.get(i).toJSONObject());
          }
          rmsg.content.put("playlist", array);
          SocketServer.sendMessage(rmsg);
          fileListChangedHandler.sendMessge(new Message());
          _currentPlayFileChanged_InSynFileList();
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      pauseInThread();
    }
  }

  private static void playInThread(task _task) {
    if (_task.playFile != null && _task.playFileIndex >= 0) {
      synchronized (fileList) {
        int index = getFile(_task.playFile, _task.playFileIndex);
        boolean found = false;
        if (currentIndex == index) {
          if (fileList.get(currentIndex).storageName != null) {
            String url = cacheFolder + "/" + fileList.get(currentIndex).storageName;
            if (AudioPlayer.dataSourceURL != null && url.compareTo(AudioPlayer.dataSourceURL) == 0) {
              AudioPlayer.play();
              if (currentStatus.compareTo(STATUS_PLAYING) != 0) {
                currentStatus = STATUS_PLAYING;
                _currentStatusChanged_InSynFileList();
              }
              found = true;
            }
          }
        }
        if (found == false) {
          pauseInThread();
          String tmp = currentStatus;
          currentStatus = STATUS_PLAYING;
          if (currentIndex != index) {
            currentIndex = index;
            _currentPlayFileChanged_InSynFileList();
          }
          if (tmp.compareTo(STATUS_PLAYING) != 0) {
            _currentStatusChanged_InSynFileList();
          }

          fileList.get(currentIndex).downloadProgress = 0;
          Log.info("388");
          _currentPlayFileDownload_InSynFileList();
          cacheFilelist.startTask(currentIndex);
        }
      }
    }
  }

  private static void pauseInThread() {
    synchronized (fileList) {
      AudioPlayer.pause();
      if (currentStatus.compareTo(STATUS_PAUSE) != 0) {
        currentStatus = STATUS_PAUSE;
        _currentStatusChanged_InSynFileList();
      }
      boolean found = false;
      if (fileList.get(currentIndex).storageName != null) {
        String url = cacheFolder + "/" + fileList.get(currentIndex).storageName;
        if (AudioPlayer.dataSourceURL != null && url.compareTo(AudioPlayer.dataSourceURL) == 0) {
          found = true;
        }
      }
      if (found == false) {
        cacheFilelist.startTask(currentIndex);
        //fileList.get(currentIndex).downloadProgress = 0;
        _currentPlayFileDownload_InSynFileList();
      }
    }
  }

  
  private static Runnable run = new Runnable() {
    @Override
    public void run() {
      while (!shutdown.get()) {
        synchronized (tasks) {
          if (tasks.size() > 0) {
            task _task = tasks.get(0);
            if (_task.type == taskType.setPlayList) {
              setPlayListInThread(_task);
            } else if (_task.type == taskType.play) {
              playInThread(_task);
              //seekbarHandler.sendEmptyMessage(0); 
            } else if (_task.type == taskType.pause) {
              synchronized (fileList) {
                pauseInThread();
              }
            }
            tasks.clear();
          }
        }

        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
  };

  public static void setPlayList(long SongsName) {
    task _task = new task();
    _task.type = taskType.setPlayList;
    _task.songName = SongsName;
    synchronized (tasks) {
      tasks.clear();
      tasks.add(_task);
    }
  }

  private static int getFile(JSONObject obj, int index) {
    int ret = -1;
    if (obj != null) {
      MyFile file = new MyFile(obj);
      if (file.fileName != null && file.fileURL != null) {
        if (index >= 0 && fileList.size() > index) {
          if (fileList.get(index).fileName != null && fileList.get(index).fileURL != null
              && fileList.get(index).fileName.compareTo(file.fileName) == 0
              && fileList.get(index).fileURL.compareTo(file.fileURL) == 0) {
            ret = index;
          }
        }
        if (ret == -1) {
          for (int i = 0; i < fileList.size(); i++) {
            if (fileList.get(i).fileName != null && fileList.get(i).fileURL != null
                && fileList.get(i).fileName.compareTo(file.fileName) == 0
                && fileList.get(i).fileURL.compareTo(file.fileURL) == 0) {
              ret = i;
              break;
            }
          }
        }
      }
    }
    return ret;
  }

  public static void play(JSONObject obj, int index) {
    task _task = new task();
    _task.type = taskType.play;
    _task.playFile = obj;
    _task.playFileIndex = index;
    synchronized (tasks) {
      tasks.clear();
      tasks.add(_task);
    }
  }

  public static void play() {
    synchronized (fileList) {
      if (fileList.size() > 0 && currentIndex >= 0) {
        JSONObject obj = fileList.get(currentIndex).toJSONObject();
        play(obj, currentIndex);
      }
    }
  }
  
  public static void play(int index){
	  synchronized (fileList) {
	  //判断index是否存在
		if(index>=0 && index<fileList.size()){  
	  JSONObject obj = fileList.get(index).toJSONObject();
	  play(obj,index);}
		else{
			
		}		
	  }
  }
  
  
  public static void pause() {
    task _task = new task();
    _task.type = taskType.pause;
    synchronized (tasks) {
      tasks.clear();
      tasks.add(_task);
    }
  }
}
