package com.xormedia.mylib;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import com.xormedia.mylib.MyTimer.MyTimerRunable;

public class AlarmCallBackReceiver {
  private static Logger Log = Logger.getLogger(AlarmCallBackReceiver.class);
  private static Thread _thread;
  private static Handler _handler;

  private static class myHandler {
    private static Logger Log = Logger.getLogger(myHandler.class);
    public long id = 0;
    private String name = null;
    private MyTimerRunable myTimerRunable = null;
    private Thread _thread;
    public Handler handler;
    private boolean isDelete = false;

    public myHandler(MyTimerRunable _myTimerRunable, String _name) {
      if (_myTimerRunable != null) {
        id = System.nanoTime();
        myTimerRunable = _myTimerRunable;
        name = _name;
        if (_thread == null) {
          _thread = new Thread(new Runnable() {

            @Override
            public void run() {
              Looper.prepare();
              handler = new Handler(Looper.myLooper(), new Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                  if (msg != null && msg.getData() != null && msg.getData().getString("action") != null) {
                    String action = msg.getData().getString("action");
                    isDelete = msg.getData().getBoolean("isDelete");
                    Log.info("myHandler::Handler:handleMessage(" + (name != null ? name : "") + ") action=" + action + ";isDelete="
                        + isDelete);
                    if (msg.what == 0) {
                      if (myTimerRunable != null) {
                        Log.info("myHandler::Handler:handleMessage(" + (name != null ? name : "") + ") run");
                        myTimerRunable.run((Context) msg.obj);
                      }
                    }
                    if (isDelete == true) {
                      Log.info("myHandler::Handler:handleMessage(" + (name != null ? name : "") + ") leave Thread!");
                      handler.getLooper().quit();
                    }
                  }

                  return true;
                }
              });
              Looper.loop();
            }
          }, "MyTimer thread");
          _thread.start();
        }
      }
    }
  }

  private static ArrayList<myHandler> myHandlers;
  private static Context mContext = null;

  public AlarmCallBackReceiver(Context context) {
    mContext = context;
    if (_thread == null) {
      _thread = new Thread(new Runnable() {

        @Override
        public void run() {
          Looper.prepare();
          _handler = new Handler(Looper.myLooper(), new Callback() {

            @Override
            public boolean handleMessage(Message msg) {
              if (msg != null && msg.obj != null && msg.what == 0 && msg.getData() != null) {
                String action = msg.getData().getString("action");
                boolean isDelete = msg.getData().getBoolean("isDelete");
                long id = msg.getData().getLong("timeId");
                Log.info("AlarmCallBackReceiver::Handler:handleMessage() action=" + action + ";isDelete=" + isDelete + ";id=" + id);
                if (id > 0) {
                  if (myHandlers != null) {
                    synchronized (myHandlers) {
                      for (int i = 0; i < myHandlers.size(); i++) {
                        if (myHandlers.get(i).id == id) {
                          if (myHandlers.get(i).handler != null) {
                            Log.info("AlarmCallBackReceiver::Handler:handleMessage() sendMessage(" + myHandlers.get(i).name != null ? myHandlers
                                .get(i).name
                                : "" + ")");
                            Message msg1 = new Message();
                            msg1.what = 0;
                            msg1.setData(msg.getData());
                            myHandlers.get(i).handler.sendMessage(msg1);
                          }
                          if (isDelete == true) {
                            myHandlers.remove(i);
                            try {
                              mContext.unregisterReceiver(AlarmCallBackReceiver.receiver);
                            }
                            catch (Exception e) {
                              ConfigureLog4J.printStackTrace(e, Log);
                            }
                            IntentFilter filter = new IntentFilter();
                            for (int j = 0; j < myHandlers.size(); j++) {
                              String tmp1 = "MyTimer_" + (myHandlers.get(j).name != null ? myHandlers.get(j).name : "") + "|"
                                  + myHandlers.get(j).id;
                              filter.addAction(tmp1);
                              Log.info("AlarmCallBackReceiver::Handler:handleMessage()::filter.addAction:" + tmp1);
                            }
                            mContext.registerReceiver(AlarmCallBackReceiver.receiver, filter);
                          }
                          break;
                        }
                      }
                    }
                  }
                }
              }
              return true;
            }
          });
          Looper.loop();
        }
      }, "MyTimer thread");
      _thread.start();
    }
  }

  public static long setCallBackHandler(MyTimerRunable myTimerRunable, String name) {
    long ret = 0;
    if (myTimerRunable != null) {
      if (myHandlers == null) {
        myHandlers = new ArrayList<AlarmCallBackReceiver.myHandler>();
      }
      synchronized (myHandlers) {
        myHandler tmp = new myHandler(myTimerRunable, name);
        ret = tmp.id;
        myHandlers.add(tmp);
        try {
          mContext.unregisterReceiver(AlarmCallBackReceiver.receiver);
        } catch (IllegalArgumentException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        IntentFilter filter = new IntentFilter();
        for (int i = 0; i < myHandlers.size(); i++) {
          String tmp1 = "MyTimer_" + (myHandlers.get(i).name != null ? myHandlers.get(i).name : "") + "|" + myHandlers.get(i).id;
          filter.addAction(tmp1);
          Log.info("setCallBackHandler::filter.addAction:" + tmp1);
        }
        mContext.registerReceiver(AlarmCallBackReceiver.receiver, filter);
      }
    }
    return ret;
  }

  public static void removeCallBackHandlerById(long id) {
    if (myHandlers != null) {
      synchronized (myHandlers) {
        for (int i = 0; i < myHandlers.size(); i++) {
          if (myHandlers.get(i).id == id) {
            if (myHandlers.get(i).handler != null) {
              Message msg = new Message();
              msg.what = 1;
              Bundle data = new Bundle();
              data.putBoolean("isDelete", true);
              data.putString("action", "delete");
              msg.setData(data);
              myHandlers.get(i).handler.sendMessage(msg);
            }
            myHandlers.remove(i);
            break;
          }
        }
        try {
          mContext.unregisterReceiver(AlarmCallBackReceiver.receiver);
        } catch (Exception e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        IntentFilter filter = new IntentFilter();
        for (int i = 0; i < myHandlers.size(); i++) {
          String tmp1 = "MyTimer_" + (myHandlers.get(i).name != null ? myHandlers.get(i).name : "") + "|" + myHandlers.get(i).id;
          filter.addAction(tmp1);
          Log.info("removeCallBackHandlerById::filter.addAction:" + tmp1);
        }
        mContext.registerReceiver(AlarmCallBackReceiver.receiver, filter);
      }
    }
  }

  public static final BroadcastReceiver receiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.info("AlarmCallBackReceiver:receiver:onReceive() action = " + action);
      Bundle data1 = new Bundle();
      data1.putString("action", action);
      Bundle data = intent.getExtras();
      if (data != null) {
        boolean isDelete = data.getBoolean("isDelete");
        Log.info("AlarmCallBackReceiver:receiver:onReceive() isDelete=" + isDelete);
        data1.putBoolean("isDelete", isDelete);
        long timeId = data.getLong("timeId");
        Log.info("AlarmCallBackReceiver:receiver:onReceive() timeId=" + timeId);
        data1.putLong("timeId", timeId);
        if (timeId > 0) {
          Message msg = new Message();
          msg.what = 0;
          msg.setData(data1);
          msg.obj = context;
          _handler.sendMessage(msg);
        }
      }
    }
  };

}
