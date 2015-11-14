package com.xormedia.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.socket.SocketTaskExecution.MySocketCallback;
import com.xormedia.socket.SocketTaskExecution.Task;
import com.xormedia.socket.SocketTaskExecution.TaskType;

public class MySocketClient {
  private static Logger Log = Logger.getLogger(MySocketClient.class);
  private static final int BUFFER_SIZE = 8192;
  /** * 连接通道 */
  private MySocket _socket;

  /** 发送缓冲区 */
  private final ByteBuffer sendBuf;

  /** 接收缓冲区 */
  private final ByteBuffer receiveBuf;

  /** 端口选择器 */
  private Selector selector;
  /** 线程是否结束的标志 */
  private AtomicBoolean shutdown;

  private int connectRetryTimes = 0;

  private SocketTaskExecution _SocketTaskExecution = null;
  private Thread _thread = null;

  public MySocketClient(String serverIP, int serverPort, MySocketCallback mySocketCallback) {
    // 初始化缓冲区
    sendBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
    receiveBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
    InetSocketAddress serverAddress = new InetSocketAddress(serverIP, serverPort);
    _socket = new MySocket(serverAddress);
    shutdown = new AtomicBoolean(true);
    _SocketTaskExecution = new SocketTaskExecution(mySocketCallback);
    _SocketTaskExecution.start();
    start();
  }

  public void setMySocketCallback(MySocketCallback mySocketCallback) {
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setMySocketCallback(mySocketCallback);
    }
  }

  public boolean isConnected() {
    boolean ret = false;
    if (_socket != null && _socket.isConnected() && shutdown.get() == false) {
      ret = true;
      connectRetryTimes = 0;
    }
    return ret;
  }

  private Runnable run = new Runnable() {

    @Override
    public void run() {
      if (_socket != null) {
        selector = _socket.connectChannel(selector);
        while (!shutdown.get()) {
          try {
            if (selector != null && selector.isOpen() == true) {
              selector.select();
              Set<SelectionKey> keys = selector.selectedKeys();
              Iterator<SelectionKey> iter = keys.iterator();
              while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isConnectable()) {// 连接成功&正常
                  _socket.channel.finishConnect();
                  _socket.fromIPAddress = _socket.channel.socket().getLocalAddress().getHostAddress();
                  Log.info("LocalAddress[" + _socket.channel.socket().getLocalAddress().getHostAddress() + "]");
                  Log.info("建立与[" + _socket.channel.socket().getInetAddress().getHostAddress() + "]通讯通道成功!");
                  Task task = new Task();
                  task.type = TaskType.onConnectOK;
                  task.channel = _socket;
                  if (_SocketTaskExecution != null) {
                    _SocketTaskExecution.setTask(task);
                  }
                } else if (key.isReadable() && isConnected() == true) {// 可读
                  if (_socket.recieve(receiveBuf, _SocketTaskExecution) == true) {
                    shutdown.set(true);
                    if (isConnected() == false) {
                      Task task = new Task();
                      task.type = TaskType.onClose;
                      task.reason = "接收数据失败！";
                      task.channel = _socket;
                      if (_SocketTaskExecution != null) {
                        _SocketTaskExecution.setTask(task);
                      }
                    }
                  }
                }
                iter.remove();
              }
            }
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
            shutdown.set(true);
            if (isConnected() == false) {
              Task task = new Task();
              task.type = TaskType.onClose;
              task.reason = "连接失败！";
              task.channel = _socket;
              if (_SocketTaskExecution != null) {
                _SocketTaskExecution.setTask(task);
              }
            }
          }
          if (!shutdown.get()) {
            if (!isConnected()) {
              if ((System.currentTimeMillis() - _socket.connectTime) > MySocket.CONNECT_TIMEOUT) {
                if (connectRetryTimes < 2) {
                  selector = _socket.connectChannel(selector);
                  connectRetryTimes++;
                } else {
                  shutdown.set(true);
                  Task task = new Task();
                  task.type = TaskType.onClose;
                  task.reason = "连接失败或者超时！";
                  task.channel = _socket;
                  if (_SocketTaskExecution != null) {
                    _SocketTaskExecution.setTask(task);
                  }
                }
              }
            } else {
              if (_socket.send(sendBuf) == false) {
                shutdown.set(true);
                if (isConnected() == false) {
                  Task task = new Task();
                  task.type = TaskType.onClose;
                  task.reason = "发送数据失败！";
                  task.channel = _socket;
                  if (_SocketTaskExecution != null) {
                    _SocketTaskExecution.setTask(task);
                  }
                }
              }
              if (System.currentTimeMillis() - _socket.lastSendMsgTime > MySocket.HEART_BEAT_TIMEOUT) {
                SocketMessage msg = new SocketMessage();
                msg.type = SocketMessage.TYPE_ANNOUNCE;
                msg.content = new JSONObject();
                try {
                  msg.content.put("CMD", "HeartBeat");
                } catch (JSONException e) {
                  ConfigureLog4J.printStackTrace(e, Log);
                }
                sendMsg(msg);
                if (_socket.send(sendBuf) == false) {
                  shutdown.set(true);
                  if (isConnected() == false) {
                    Task task = new Task();
                    task.type = TaskType.onClose;
                    task.reason = "发送心跳数据失败！";
                    task.channel = _socket;
                    if (_SocketTaskExecution != null) {
                      _SocketTaskExecution.setTask(task);
                    }
                  }
                }
              }
            }
          }
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
      shutdown();
      Log.info("解除与[" + _socket.toIPAddress + "]通讯通道");
      _thread = null;
    }
  };

  public boolean sendMsg(SocketMessage msg) {
    boolean ret = false;
    if (msg != null && _socket != null) {
      ret = _socket.sendMsg(msg);
    }
    return ret;
  }

  private void shutdown() {
    shutdown.set(true);
    if (_socket != null) {
      _socket.shutdown(sendBuf, _SocketTaskExecution);
    }
    if (selector != null) {
      try {
        selector.close();
        selector = null;
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  protected void startTask() {
    if (shutdown.get() == true) {
      while (_thread != null) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      if (_thread == null) {
        _thread = new Thread(run);
        shutdown.set(false);
        _thread.start();
      }
    }
  }

  protected void stopTask() {
    shutdown();
  }

  public void start() {
    Task task = new Task();
    task.socketClient = MySocketClient.this;
    task.type = TaskType.startThread;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
    }
  }

  public void close() {
    Task task = new Task();
    task.socketClient = MySocketClient.this;
    task.type = TaskType.stopThread;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    shutdown.set(true);
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.close();
    }
    super.finalize();
  }
}
