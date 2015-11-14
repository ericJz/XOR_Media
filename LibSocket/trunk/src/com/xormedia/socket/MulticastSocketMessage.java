package com.xormedia.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.xhr;

public class MulticastSocketMessage {
  private static Logger Log = Logger.getLogger(MulticastSocketMessage.class);
  private static final String DATAGRAMPACKET_BUFF_START_STRING = "\r\nBroadMessage:\r\n";
  public InetAddress fromAddress = null;
  public boolean isBroadMessage = false;
  public String content = null;
  public int multicastPort = 0;

  public MulticastSocketMessage(int _multicastPort, DatagramPacket datagramPacket) {
    multicastPort = _multicastPort;
    if (datagramPacket != null && datagramPacket.getLength() >= DATAGRAMPACKET_BUFF_START_STRING.length()) {
      String tmp = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
      if (tmp.startsWith(DATAGRAMPACKET_BUFF_START_STRING) == true) {
        content = tmp.substring(DATAGRAMPACKET_BUFF_START_STRING.length());
        fromAddress = datagramPacket.getAddress();
        isBroadMessage = true;
        Log.info("接收到来自" + fromAddress.getHostAddress() + "消息：" + content);
      }
    }
  }

  private static MulticastSocket multicastSocketSend;

  public synchronized void send() {
    if (isBroadMessage == true) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            if (multicastSocketSend == null) {
              multicastSocketSend = new MulticastSocket();
              multicastSocketSend.setTimeToLive(4);
            }
            if (multicastSocketSend != null
                && xhr.mConnectType >= xhr.CONNECT_TYPE_WIFI) {
              multicastSocketSend.send(getDatagramPacket(MyMulticastSocketServer.MULTICAST_HOST_ADDRESS));
              Thread.sleep(1000);
              multicastSocketSend.send(getDatagramPacket(MyMulticastSocketServer.MULTICAST_HOST_ADDRESS));
              Thread.sleep(1000);
              multicastSocketSend.send(getDatagramPacket(MyMulticastSocketServer.MULTICAST_HOST_ADDRESS));
              Log.info("广播消息发送成功");
            }
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
            if (multicastSocketSend != null) {
              multicastSocketSend.close();
              multicastSocketSend = null;
            }
          } catch (InterruptedException e) {
            ConfigureLog4J.printStackTrace(e, Log);
            if (multicastSocketSend != null) {
              multicastSocketSend.close();
              multicastSocketSend = null;
            }
          }
        }
      }).start();
    }
  }

  public MulticastSocketMessage(int _multicastPort, String _content) {
    multicastPort = _multicastPort;
    content = _content;
    isBroadMessage = true;
  }

  public DatagramPacket getDatagramPacket(String listenIPAddress) {
    DatagramPacket datagramPacket = null;
    if (isBroadMessage == true) {
      try {
        String tmp = DATAGRAMPACKET_BUFF_START_STRING + (content != null ? content : "");
        byte[] buf = tmp.getBytes();
        datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(listenIPAddress), multicastPort);
        Log.info("消息发送端口号:" + multicastPort + ";内容:" + tmp);
      } catch (UnknownHostException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return datagramPacket;
  }
}
