package com.xormedia.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.socket.SocketTaskExecution.MySocketServerCallback;
import com.xormedia.socket.SocketTaskExecution.Task;
import com.xormedia.socket.SocketTaskExecution.TaskType;

public class MySocketServer {
  private static Logger Log = Logger.getLogger(MySocketServer.class);
  private static final int BUFFER_SIZE = 8192;
  public static long CONNECT_TIMEOUT = 5l * 1000l;
  /** * 连接通道 */
  private ServerSocketChannel serverSocketChannel;

  /** 发送缓冲区 */
  private final ByteBuffer sendBuf;

  /** 接收缓冲区 */
  private final ByteBuffer receiveBuf;
  /** 端口选择器 */
  private Selector selector;
  /** 线程是否结束的标志 */
  private AtomicBoolean shutdown;
  /** 本地监听端口 */
  public int ListenPort = 0;

  private long connectTime = 0l;

  private SocketTaskExecution _SocketTaskExecution = null;
  private ArrayList<MySocket> channels = new ArrayList<MySocket>();
  private Thread _thread = null;
  private Thread _thread2 = null;

  public MySocketServer(int _ListenPort, MySocketServerCallback mySocketServerCallback) {
    // 初始化缓冲区
    ListenPort = _ListenPort;
    sendBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
    receiveBuf = ByteBuffer.allocateDirect(BUFFER_SIZE);
    shutdown = new AtomicBoolean(true);
    _SocketTaskExecution = new SocketTaskExecution(mySocketServerCallback);
    _SocketTaskExecution.start();
    start();
  }

  public void sendMsg(SocketMessage msg) {
    if (msg != null) {
      if (channels.size() > 0) {
        synchronized (channels) {
          for (int i = 0; i < channels.size(); i++) {
            channels.get(i).sendMsg(msg);
          }
        }
      }
    }
  }

  public void setMySocketServerCallback(MySocketServerCallback mySocketServerCallback) {
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setMySocketServerCallback(mySocketServerCallback);
    }
  }

  private void startup() {
    try {
      if (selector == null || selector.isOpen() == false) {
        selector = Selector.open();
      }
      // 打开通道
      Log.info("開始建立監聽非廣播消息，監聽端口：" + ListenPort);
      serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      serverSocketChannel.socket().bind(new InetSocketAddress(ListenPort)); // 绑定端口
      if (channels.size() > 0) {
        synchronized (channels) {
          for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).isConnected() == true) {
              channels.get(i).channel.register(selector, SelectionKey.OP_READ);
            }
          }
        }
      }

      Task task = new Task();
      task.type = TaskType.onServerConnectOK;
      task.listenPort = ListenPort;
      if (_SocketTaskExecution != null) {
        _SocketTaskExecution.setTask(task);
      }
    } catch (IOException e) {
      shutdown();
      ConfigureLog4J.printStackTrace(e, Log);
    }
    connectTime = System.currentTimeMillis();
  }

  public boolean isOpen() {
    boolean ret = false;
    if (serverSocketChannel != null && serverSocketChannel.isOpen()
        && selector != null && selector.isOpen() && shutdown.get() == false) {
      ret = true;
    }
    else {
      Log.info(ListenPort + "端口无监听");
    }
    return ret;
  }

  private Runnable runSend = new Runnable() {

    @Override
    public void run() {
      while (!shutdown.get()) {
        if (isOpen()) {
          if (channels.size() > 0) {
            synchronized (channels) {
              int i = 0;
              while (i < channels.size()) {
                if (channels.get(i).isConnected() == true) {
                  if (channels.get(i).readyClose == true
                      || (System.currentTimeMillis() - channels.get(i).lastRecieveMsgTime) > (MySocket.HEART_BEAT_TIMEOUT + (30l * 1000l))) {
                    channels.get(i).shutdown(sendBuf, _SocketTaskExecution);
                    channels.remove(i);
                  } else {
                    if (channels.get(i).send(sendBuf) == false) {
                      channels.get(i).shutdown(sendBuf, _SocketTaskExecution);
                    }
                    i++;
                  }
                } else {
                  channels.get(i).shutdown(sendBuf, _SocketTaskExecution);
                  channels.remove(i);
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
      _thread2 = null;
    }
  };

  private Runnable run = new Runnable() {

    @Override
    public void run() {
      while (!shutdown.get()) {
        if (!isOpen()) {
          if (System.currentTimeMillis() - connectTime > CONNECT_TIMEOUT) {
            startup();
          }
        } else {
          try {
           // Log.info("正在監聽非廣播消息，監聽端口：" + ListenPort);
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iter = keys.iterator();
            while (iter.hasNext()) {
              SelectionKey key = iter.next();
              iter.remove();
              if (key.isAcceptable()) {// 新的连接
                SocketChannel channel = serverSocketChannel.accept();
                // 设置非阻塞模式
                channel.configureBlocking(false);
                channel.register(selector, SelectionKey.OP_READ);
                Log.info("收到一個新連接客戶端。" + channel.socket().getInetAddress().getHostAddress());
                synchronized (channels) {
                  boolean found = false;
                  for (int i = 0; i < channels.size(); i++) {
                    if (channels.get(i).channel.equals(channel) == true) {
                      found = true;
                      break;
                    }
                  }
                  if (found == false) {
                    MySocket _socket = new MySocket(channel);
                    channels.add(_socket);
                    Task task = new Task();
                    task.type = TaskType.onConnectOK;
                    task.channel = _socket;
                    if (_SocketTaskExecution != null) {
                      _SocketTaskExecution.setTask(task);
                    }
                  }
                }
              }
              else if (key.isReadable()) {// 可读
                SocketChannel channel = (SocketChannel) key.channel();
                MySocket myChannel = null;
                synchronized (channels) {
                  for (int i = 0; i < channels.size(); i++) {
                    if (channels.get(i).channel.equals(channel) == true) {
                      myChannel = channels.get(i);
                      break;
                    }
                  }
                  if (myChannel == null) {
                    myChannel = new MySocket(channel);
                    channels.add(myChannel);
                  }
                }
                if (myChannel.recieve(receiveBuf, _SocketTaskExecution) == true) {
                  synchronized (channels) {
                    for (int i = 0; i < channels.size(); i++) {
                      if (channels.get(i).channel.equals(channel) == true) {
                        channels.get(i).shutdown(sendBuf, _SocketTaskExecution);
                        channels.remove(i);
                        break;
                      }
                    }
                  }
                }
              }
            }
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }catch (ClosedSelectorException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      shutdown();
      Task task = new Task();
      task.type = TaskType.onServerClose;
      task.listenPort = ListenPort;
      if (_SocketTaskExecution != null) {
        _SocketTaskExecution.setTask(task);
      }
      _thread = null;
    }
  };

  private void shutdown() {
    shutdown.set(true);
    if (serverSocketChannel != null) {
      try {
        serverSocketChannel.close();
        Log.info(ListenPort + "端口監聽关闭成功");
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } finally {
        serverSocketChannel = null;
      }
    }
    if (channels.size() > 0) {
      for (int i = 0; i < channels.size(); i++) {
        if (channels.get(i) != null) {
          channels.get(i).shutdown(sendBuf, _SocketTaskExecution);
        }
      }
      channels.clear();
    }
    if (selector != null && selector.isOpen() == true) {
      try {
        selector.close();
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
      while (_thread2 != null) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      if (_thread == null && _thread2 == null) {
        _thread = new Thread(run);
        _thread2 = new Thread(runSend);
        shutdown.set(false);
        _thread.start();
        _thread2.start();
      }
    }
  }

  public void closeChannel(MySocket channel) {
    if (channel != null && channel.toIPAddress != null)
      synchronized (channels) {
        for (int i = 0; i < channels.size(); i++) {
          if (channels.get(i).toIPAddress.compareTo(channel.toIPAddress) == 0) {
            channels.get(i).readyClose = true;
            break;
          }
        }
      }
  }

  protected void stopTask() {
    shutdown();
  }

  public void start() {
    Task task = new Task();
    task.socketServer = MySocketServer.this;
    task.type = TaskType.startThread;
    if (_SocketTaskExecution != null) {
      _SocketTaskExecution.setTask(task);
    }
  }

  public void close() {
    Task task = new Task();
    task.socketServer = MySocketServer.this;
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
    channels.clear();
    super.finalize();
  }

}
