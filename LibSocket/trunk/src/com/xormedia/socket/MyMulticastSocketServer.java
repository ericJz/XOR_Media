package com.xormedia.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.xhr;
import com.xormedia.socket.SocketTaskExecution.MyMulticastSocketCallback;
import com.xormedia.socket.SocketTaskExecution.Task;
import com.xormedia.socket.SocketTaskExecution.TaskType;

public class MyMulticastSocketServer {
  private static Logger Log = Logger.getLogger(MyMulticastSocketServer.class);
  public static String MULTICAST_HOST_ADDRESS = "239.0.44.44";
  private static final int BUFFER_SIZE = 1024;
  public static long CONNECT_TIMEOUT = 15l * 1000l;

  public static interface receiveMessageProcess {
    public void onProcess(int _multicastPort, MulticastSocketMessage msg);
  }

  /** 接收缓冲区 */
  private byte receiveBuf[];
  private DatagramPacket dp;
  private MulticastSocket multicSocket = null;
  private int multicastPort = 0;
  private AtomicBoolean shutdown;
  private long connectTime = 0l;
  private boolean isConnected = false;

  private Thread _thread = null;
  private SocketTaskExecution _SocketTaskExecution = null;

  public MyMulticastSocketServer(int _multicastPort, MyMulticastSocketCallback _MyMulticastSocketCallback) {
    multicastPort = _multicastPort;
    receiveBuf = new byte[BUFFER_SIZE];
    shutdown = new AtomicBoolean(true);
    _SocketTaskExecution = new SocketTaskExecution(_MyMulticastSocketCallback);
    _SocketTaskExecution.start();
    start();
  }

  private void startup() {
    try {
      Log.info("建立監聽廣播消息，監聽端口：" + multicastPort);
      isConnected = false;
      if (xhr.mConnectType >= xhr.CONNECT_TYPE_WIFI) {
        if (multicSocket != null) {
          multicSocket.close();
        }
        multicSocket = new MulticastSocket(multicastPort);
        if (multicSocket != null) {
          multicSocket.setLoopbackMode(true);
          multicSocket.setTimeToLive(64);
          InetAddress monitorAddress = InetAddress.getByName(MULTICAST_HOST_ADDRESS);
          multicSocket.joinGroup(monitorAddress);
        }
      } else {
        isConnected = false;
        Log.info("建立監聽廣播消息失败！監聽端口：" + multicastPort);
      }
      connectTime = System.currentTimeMillis();
    } catch (IOException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  private Runnable run = new Runnable() {

    @Override
    public void run() {

      while (!shutdown.get()) {
        if (multicSocket != null) {
          Log.info("isConnected:" + multicSocket.isConnected() + ";isBound:" + multicSocket.isBound() + ":isClose:" + multicSocket.isClosed());
        }
        if (!isConnected()) {
          if (System.currentTimeMillis() - connectTime > CONNECT_TIMEOUT) {
            startup();
          }
        } else {
          if (isConnected == false) {
            Task task = new Task();
            task.type = TaskType.onConnectOK;
            task.multicastPort = multicastPort;
            if (_SocketTaskExecution != null) {
              _SocketTaskExecution.setTask(task);
            }
            isConnected = true;
          }
          Log.info("正在監聽廣播消息，監聽端口：" + multicastPort + ";監聽IPAddress:" + MULTICAST_HOST_ADDRESS);
          try {
            dp = new DatagramPacket(receiveBuf, BUFFER_SIZE);
            multicSocket.receive(dp);
            Log.info("接收到来自[" + dp.getAddress().getHostAddress() + "]消息：" + new String(dp.getData(), 0, dp.getLength()));
            if (dp.getLength() > 0) {
              MulticastSocketMessage bMsg = new MulticastSocketMessage(multicastPort, dp);
              if (bMsg.isBroadMessage == true) {
                Task task = new Task();
                task.type = TaskType.onProcess;
                task.multicastPort = multicastPort;
                task.multicastMsg = bMsg;
                if (_SocketTaskExecution != null) {
                  _SocketTaskExecution.setTask(task);
                }
              }
            }
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
      shutdown();
      Log.info("解除監聽廣播消息，監聽端口：" + multicastPort);
      _thread = null;
    }
  };

  private void shutdown() {
    shutdown.set(true);
    if (isConnected()) {
      try {
        multicSocket.leaveGroup(InetAddress.getByName(MULTICAST_HOST_ADDRESS));
      } catch (UnknownHostException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
      multicSocket.close();
      isConnected = false;
      multicSocket = null;
    }
    Task task = new Task();
    task.type = TaskType.onClose;
    task.multicastPort = multicastPort;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
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
    task.multicastSocketServer = MyMulticastSocketServer.this;
    task.type = TaskType.startThread;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
    }
  }

  public void close() {
    Task task = new Task();
    task.multicastSocketServer = MyMulticastSocketServer.this;
    task.type = TaskType.stopThread;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
    }
  }

  public boolean sendMessage(final MulticastSocketMessage msg) {
    boolean ret = false;
    if (msg != null && msg.isBroadMessage == true && isConnected()) {
      new Thread(new Runnable() {

        @Override
        public void run() {
          if (isConnected()) {
            try {
              multicSocket.send(msg.getDatagramPacket(MULTICAST_HOST_ADDRESS));
              Thread.sleep(1000);
              multicSocket.send(msg.getDatagramPacket(MULTICAST_HOST_ADDRESS));
              Log.info("广播消息发送成功");
            } catch (IOException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            } catch (InterruptedException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        }
      }).start();
    }
    return ret;
  }

  public boolean isConnected() {
    boolean ret = false;
    if (multicSocket != null && multicSocket.isClosed() == false && multicSocket.isBound() == true
        && xhr.mConnectType >= xhr.CONNECT_TYPE_WIFI) {
      ret = true;
    }
    return ret;
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
