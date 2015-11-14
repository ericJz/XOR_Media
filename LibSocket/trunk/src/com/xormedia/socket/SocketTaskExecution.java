package com.xormedia.socket;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.xormedia.mylib.ConfigureLog4J;

public class SocketTaskExecution extends Thread {
  private static Logger Log = Logger.getLogger(SocketTaskExecution.class);
  private AtomicBoolean shutdown;
  private MySocketCallback _MySocketCallback = null;
  private MyMulticastSocketCallback _MyMulticastSocketCallback = null;
  private MySocketServerCallback _MySocketServerCallback = null;

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

  public void setMySocketServerCallback(MySocketServerCallback mySocketServerCallback) {
    if (_MySocketServerCallback != mySocketServerCallback) {
      if (_MySocketServerCallback == null) {
        _MySocketServerCallback = mySocketServerCallback;
      } else {
        synchronized (_MySocketServerCallback) {
          _MySocketServerCallback = mySocketServerCallback;
        }
      }
    }
  }

  public SocketTaskExecution(MySocketCallback MySocketCallback) {
    _MySocketCallback = MySocketCallback;
    shutdown = new AtomicBoolean(false);
  }

  public SocketTaskExecution(MyMulticastSocketCallback myMulticastSocketCallback) {
    _MyMulticastSocketCallback = myMulticastSocketCallback;
    shutdown = new AtomicBoolean(false);
  }

  public SocketTaskExecution(MySocketServerCallback mySocketServerCallback) {
    _MySocketServerCallback = mySocketServerCallback;
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

    public void onClose(MySocket channel, String reason);
  }

  public static interface MySocketServerCallback extends MySocketCallback {

    public void onServerConnectOK(int Port);

    public void onServerClose(int Port);
  }

  public static enum TaskType {
    onProcess, onConnectOK, onClose, startThread, stopThread, onServerConnectOK, onServerClose
  }

  public static class Task {
    public TaskType type = null;
    public MyMulticastSocketServer multicastSocketServer = null;
    public MySocketClient socketClient = null;
    public MySocketServer socketServer = null;
    public MySocket channel = null;
    public SocketMessage msg = null;
    public int multicastPort = 0;
    public int listenPort = 0;
    public MulticastSocketMessage multicastMsg = null;
    public String reason = null;
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
        if (task.type != null) {
          if (task.type == TaskType.startThread) {
            if (task.multicastSocketServer != null) {
              task.multicastSocketServer.startTask();
            } else if (task.socketClient != null) {
              task.socketClient.startTask();
            } else if (task.socketServer != null) {
              task.socketServer.startTask();
            }
          } else if (task.type == TaskType.stopThread) {
            if (task.multicastSocketServer != null) {
              task.multicastSocketServer.stopTask();
            } else if (task.socketClient != null) {
              task.socketClient.stopTask();
            } else if (task.socketServer != null) {
              task.socketServer.stopTask();
            }
          } else {
            if (_MySocketCallback != null && task.channel != null) {
              synchronized (_MySocketCallback) {
                if (task.type == TaskType.onProcess && task.msg != null) {
                  _MySocketCallback.onProcess(task.channel, task.msg);
                } else if (task.type == TaskType.onConnectOK) {
                  _MySocketCallback.onConnectOK(task.channel);
                } else if (task.type == TaskType.onClose) {
                  _MySocketCallback.onClose(task.channel, task.reason);
                }
              }
            } else if (_MyMulticastSocketCallback != null && task.multicastPort > 0) {
              synchronized (_MyMulticastSocketCallback) {
                if (task.type == TaskType.onProcess && task.multicastMsg != null) {
                  _MyMulticastSocketCallback.onProcess(task.multicastPort, task.multicastMsg);
                } else if (task.type == TaskType.onConnectOK) {
                  _MyMulticastSocketCallback.onConnectOK(task.multicastPort);
                } else if (task.type == TaskType.onClose) {
                  _MyMulticastSocketCallback.onClose(task.multicastPort);
                }
              }
            } else if (_MySocketServerCallback != null) {
              synchronized (_MySocketServerCallback) {
                if (task.type == TaskType.onProcess && task.msg != null && task.channel != null) {
                  _MySocketServerCallback.onProcess(task.channel, task.msg);
                } else if (task.type == TaskType.onConnectOK && task.channel != null) {
                  _MySocketServerCallback.onConnectOK(task.channel);
                } else if (task.type == TaskType.onClose && task.channel != null) {
                  _MySocketServerCallback.onClose(task.channel, task.reason);
                } else if (task.type == TaskType.onServerConnectOK && task.listenPort > 0) {
                  _MySocketServerCallback.onServerConnectOK(task.listenPort);
                } else if (task.type == TaskType.onServerClose && task.listenPort > 0) {
                  _MySocketServerCallback.onServerClose(task.listenPort);
                }
              }
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
