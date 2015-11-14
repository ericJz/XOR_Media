package com.xormedia.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.socket.SocketTaskExecution.Task;
import com.xormedia.socket.SocketTaskExecution.TaskType;

public class MySocket {
  private static Logger Log = Logger.getLogger(MySocket.class);
  public static long HEART_BEAT_TIMEOUT = 30l * 1000l;
  public static long CONNECT_TIMEOUT = 5l * 1000l;
  public SocketChannel channel = null;
  public long lastRequestTime = System.currentTimeMillis();
  public long connectTime = 0l;
  public long lastSendMsgTime = System.currentTimeMillis();
  public long lastRecieveMsgTime = System.currentTimeMillis();
  private int CSeq = 0;

  private InetSocketAddress serverAddress = null;
  public String toIPAddress = null;
  public int toPort = 0;
  public String fromIPAddress = null;

  protected boolean readyClose = false;

  private ArrayList<SocketMessage> sendMessages = new ArrayList<SocketMessage>();

  public MySocket(SocketChannel _channel) {
    channel = _channel;
    if (channel != null && channel.socket() != null && channel.socket().getInetAddress() != null) {
      toIPAddress = channel.socket().getInetAddress().getHostAddress();
      toPort = channel.socket().getPort();
      Log.info("LocalAddress[" + channel.socket().getLocalAddress().getHostAddress() + "]");
      fromIPAddress = channel.socket().getLocalAddress().getHostAddress();
    }
  }

  public MySocket(InetSocketAddress _serverAddress) {
    serverAddress = _serverAddress;
    if (serverAddress != null && serverAddress.getAddress() != null) {
      toIPAddress = serverAddress.getAddress().getHostAddress();
      toPort = serverAddress.getPort();
    }
  }

  public Selector connectChannel(Selector selector) {
    if (serverAddress != null && !isConnected()) {
      try {
        // 打开通道
        Log.info("開始建立与[" + serverAddress.toString() + "]通讯通道");
        connectTime = System.currentTimeMillis();
        if (channel != null) {
          try {
            channel.close();
          } catch (IOException e1) {
            ConfigureLog4J.printStackTrace(e1, Log);
          }
          channel = null;
        }
        if (selector != null) {
          try {
            selector.close();
          } catch (IOException e1) {
            ConfigureLog4J.printStackTrace(e1, Log);
          }
          selector = null;
        }
        if (channel == null) {
          channel = SocketChannel.open();
          channel.configureBlocking(false);
        }
        if (selector == null) {
          selector = Selector.open();
          channel.register(selector, SelectionKey.OP_CONNECT
              | SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
        }
        channel.connect(serverAddress);
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (UnresolvedAddressException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (UnsupportedAddressTypeException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (IllegalBlockingModeException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (NoConnectionPendingException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (IllegalSelectorException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (IllegalArgumentException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (CancelledKeyException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return selector;
  }

  public boolean sendMsg(SocketMessage msg) {
    boolean ret = false;
    if (msg != null) {
      synchronized (sendMessages) {
        sendMessages.add(msg);
        ret = true;
      }
    }
    return ret;
  }

  public boolean isConnected() {
    boolean ret = false;
    if (channel != null && channel.isConnected() && channel.isOpen()) {
      ret = true;
    }
    else {
      Log.info("与[" + toIPAddress + "]通讯通道没有连接！");
    }
    return ret;
  }

  public boolean send(ByteBuffer sendBuf) {
    boolean ret = true;
    synchronized (sendMessages) {
      int i = 0;
      while (i < sendMessages.size() && ret == true) {
        SocketMessage currentMsg = sendMessages.get(i);
        currentMsg.fromIP = fromIPAddress;
        currentMsg.toIP = toIPAddress;
        currentMsg.toPort = toPort;
        // 处理发送新命令
        if (currentMsg != null && currentMsg.type != null
            && (currentMsg.type.compareTo(SocketMessage.TYPE_REQUEST) != 0 || currentMsg.CSeq == 0)) {
          if (currentMsg.type.compareTo(SocketMessage.TYPE_REQUEST) == 0) {
            currentMsg.CSeq = ++CSeq;
          }
          String MsgString = currentMsg.toString();
          if (MsgString != null && MsgString.length() > 0) {
            ret = _send(sendBuf, MsgString.getBytes());
            Log.info("发送给[" + toIPAddress + "]消息[" + ret + "]：" + MsgString);
          }
          if (currentMsg.type.compareTo(SocketMessage.TYPE_REQUEST) != 0 && ret == true) {
            sendMessages.remove(i);
          } else {
            i++;
          }
        } else if (currentMsg != null // 处理命令超时
            && currentMsg.CSeq > 0
            && currentMsg.response == null
            && currentMsg.type.compareTo(SocketMessage.TYPE_REQUEST) == 0
            && (currentMsg.timestamp + currentMsg.requestTimeout) < System.currentTimeMillis()) {
          Log.info("超时无回复消息：" + currentMsg.toString());
          currentMsg.sendResponse();
          sendMessages.remove(i);
        } else {
          i++;
        }
      }
    }
    return ret;
  }

  private boolean _send(ByteBuffer sendBuf, byte[] out) {
    boolean ret = false;
    if (out == null || out.length < 1) {
      return ret;
    }
    synchronized (sendBuf) {
      sendBuf.clear();
      sendBuf.put(out);
      sendBuf.flip();
    }
    // 发送出去
    try {
      if (isConnected()) {
        lastSendMsgTime = System.currentTimeMillis();
        int len = channel.write(sendBuf);
        if (len == out.length) {
          ret = true;
        }
      }
    } catch (final IOException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  /**
   * 
   * @param sendBuf
   * @param _SocketMsgTaskExecution
   * @return 返回是否收到closeSocket消息。
   */
  public boolean recieve(ByteBuffer receiveBuf, SocketTaskExecution _SocketMsgTaskExecution) {
    boolean ret = false;
    _recieve(receiveBuf);
    if (recieveString.length() > 0) {
      if (recieveString.indexOf("-ZWSM/1.0 End-\r\n-") > 0) {
        String[] nodes = recieveString.split("-ZWSM/1.0 End-\r\n-");
        int len = nodes.length;
        if (recieveString.endsWith("-ZWSM/1.0 End-\r\n-") == false) {
          recieveString = recieveString.substring(recieveString.lastIndexOf("-ZWSM/1.0 End-\r\n-") + new String("-ZWSM/1.0 End-\r\n-").length());
          len--;
        } else {
          recieveString = "";
        }
        if (nodes != null && len > 0) {
          Log.info("len=" + len);

          for (int i = 0; i < len; i++) {
            // Log.info("nodes[" + i + "]=" + nodes[i]);
            String node = nodes[i].trim();
            SocketMessage msg = new SocketMessage(node);
            if (msg != null && msg.type != null) {
              if (msg.type.compareTo(SocketMessage.TYPE_RESPONSE) == 0) {
                Log.info("收到来自[" + toIPAddress + "]的回复消息:" + (msg.content != null ? msg.content.toString() : ""));
                synchronized (sendMessages) {
                  for (int j = 0; j < sendMessages.size(); j++) {
                    if (sendMessages.get(j).type.compareTo(SocketMessage.TYPE_REQUEST) == 0
                        && sendMessages.get(j).CSeq == msg.CSeq) {
                      sendMessages.get(j).response = msg;
                      sendMessages.get(i).sendResponse();
                      sendMessages.remove(i);
                      break;
                    }
                  }
                }
              } else {
                if (msg.type.compareTo(SocketMessage.TYPE_ANNOUNCE) == 0) {
                  Log.info("收到来自[" + toIPAddress + "]的ANNOUNCE消息:" + (msg.content != null ? msg.content.toString() : ""));
                  try {
                    if (msg.content != null && msg.content.has("CMD") == true
                        && msg.content.getString("CMD").compareTo("CloseSocket") == 0) {
                      ret = true;
                    }
                  } catch (JSONException e) {
                    ConfigureLog4J.printStackTrace(e, Log);
                  }
                } else if (msg.type.compareTo(SocketMessage.TYPE_REQUEST) == 0) {
                  Log.info("收到来自[" + toIPAddress + "]的請求:" + (msg.content != null ? msg.content.toString() : ""));
                }
                Task task = new Task();
                task.type = TaskType.onProcess;
                task.msg = msg;
                task.channel = MySocket.this;
                if (_SocketMsgTaskExecution != null) {
                  _SocketMsgTaskExecution.setTask(task);
                }
              }
            }
          }
        }
      }
    }
    return ret;
  }

  private String recieveString = "";

  private String _recieve(ByteBuffer receiveBuf) {
    int len = 0;
    int readBytes = 0;
    synchronized (receiveBuf) {
      receiveBuf.clear();
      try {
        while (channel != null && (len = channel.read(receiveBuf)) > 0) {
          readBytes += len;
        }
      } catch (Exception e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } finally {
        receiveBuf.flip();
      }
      if (readBytes > 0) {
        lastRecieveMsgTime = System.currentTimeMillis();
        final byte[] tmp = new byte[readBytes];
        receiveBuf.get(tmp);
        recieveString += new String(tmp);
        Log.info("收到来自[" + toIPAddress + "]的内容:" + recieveString);
      }
    }

    return recieveString;
  }

  public void shutdown(ByteBuffer sendBuf, SocketTaskExecution _SocketMsgTaskExecution) {
    if (isConnected() == true) {
      try {
        SocketMessage msg = new SocketMessage();
        msg.type = SocketMessage.TYPE_ANNOUNCE;
        msg.content = new JSONObject();
        try {
          msg.content.put("CMD", "CloseSocket");
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        sendMsg(msg);
        if (send(sendBuf) == true) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
        synchronized (sendMessages) {
          sendMessages.clear();
        }
        if (channel != null) {
          channel.close();
          channel = null;
        }
        Log.info("关闭与[" + toIPAddress + "]通讯通道");
        Task task = new Task();
        task.type = TaskType.onClose;
        task.channel = MySocket.this;
        if (_SocketMsgTaskExecution != null) {
          _SocketMsgTaskExecution.setTask(task);
        }
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    sendMessages.clear();
    super.finalize();
  }

}
