package com.xormedia.socket;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.xormedia.mylib.ConfigureLog4J;

public class SocketMsgTaskExecution extends Thread {
  private static Logger Log = Logger.getLogger(SocketMsgTaskExecution.class);
  private AtomicBoolean shutdown;
  private MySocketCallback _MySocketCallback = null;
  private MyMulticastSocketCallback _MyMulticastSocketCallback = null;

  public void setMySocketCallback(MySocketCallback mySocketCallback) {
    if (_MySocketCallback != mySocketCallback) {
      if (_MySocketCallback == null) {
        _MySocketCallback = mySocketCallback;
      } else {
        synchronized (_MySocketCallback) {
          _MySocketCallback = mySocketCallback;
        }
      }
    }
  }

  public void setMyMulticastSocketCallback(MyMulticastSocketCallback myMulticastSocketCallback) {
    if (_MyMulticastSocketCallback != myMulticastSocketCallback) {
      if (_MyMulticastSocketCallback == null) {
        _MyMulticastSocketCallback = myMulticastSocketCallback;
      } else {
        synchronized (_MyMulticastSocketCallback) {
          _MyMulticastSocketCallback = myMulticastSocketCallback;
        }
      }
    }
  }

  public SocketMsgTaskExecution(MySocketCallback MySocketCallback) {
    _MySocketCallback = MySocketCallback;
    shutdown = new AtomicBoolean(false);
  }

  public SocketMsgTaskExecution(MyMulticastSocketCallback myMulticastSocketCallback) {
    _MyMulticastSocketCallback = myMulticastSocketCallback;
    shutdown = new AtomicBoolean(false);
  }

  public void close() {
    shutdown.set(true);
  }

  public static interface MyMulticastSocketCallback {
    public void onProcess(int _multicastPort, MulticastSocketMessage msg);

    public void onConnectOK(int _multicastPort);

    public void onClose(int _multicastPort);
  }

  public static interface MySocketCallback {
    public void onProcess(MySocket channel, SocketMessage msg);

    public void onConnectOK(MySocket channel);

    public void onClose(MySocket channel);
  }

  public static enum TaskType {
    onProcess, onConnectOK, onClose
  }

  public static class Task {
    public TaskType type = null;
    public MySocket channel = null;
    public SocketMessage msg = null;
  }

  private ArrayList<Task> tasks = new ArrayList<Task>();

  public void setTask(Task task) {
    if (task != null) {
      synchronized (tasks) {
        tasks.add(task);
      }
    }
  }

  private Task getTopTask() {
    Task task = null;
    synchronized (tasks) {
      if (tasks.size() > 0) {
        task = tasks.get(0);
        tasks.remove(0);
      }
    }
    return task;
  }

  @Override
  public void run() {
    while (!shutdown.get()) {
      Task task = getTopTask();
      while (task != null) {
        if (_MySocketCallback != null && task.channel != null && task.type != null) {
          synchronized (_MySocketCallback) {
            if (task.type == TaskType.onProcess && task.msg != null) {
              _MySocketCallback.onProcess(task.channel, task.msg);
            } else if (task.type == TaskType.onConnectOK) {
              _MySocketCallback.onConnectOK(task.channel);
            } else if (task.type == TaskType.onClose) {
              _MySocketCallback.onClose(task.channel);
            }
          }
        }
        task = getTopTask();
      }
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (tasks != null) {
      tasks.clear();
    }
    super.finalize();
  }

}
