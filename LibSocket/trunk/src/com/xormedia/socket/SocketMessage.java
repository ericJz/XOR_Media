package com.xormedia.socket;

import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;

public class SocketMessage {
  private static Logger Log = Logger.getLogger(SocketMessage.class);
  public final static String TYPE_REQUEST = "Request";
  public final static String TYPE_RESPONSE = "Response";
  public final static String TYPE_ANNOUNCE = "Announce";
  public WeakReference<Handler> UIHandler = null;
  public String fromIP = null;
  public String toIP = null;
  public int toPort = 0;
  public String type = null;
  public long CSeq = 0;
  public long timestamp = 0l;
  public JSONObject content = null;
  public SocketMessage response = null;
  public long requestTimeout = 10 * 1000l;

  public SocketMessage() {
  }

  public void sendResponse() {
    if (UIHandler != null && response != null) {
      Handler handler = UIHandler.get();
      if (handler != null) {
        Message msg = new Message();
        msg.obj = this;
        if (this.response != null) {
          msg.what = 0;
        } else {
          msg.what = 1;
        }
        handler.sendMessage(msg);
      }
    }
  }

  public SocketMessage(String strMsg) {
    if (strMsg != null && strMsg.length() > 0) {
      int tmp = strMsg.indexOf("-ZWSM/1.0 Start-\r\n");
      if (tmp >= 0) {
        strMsg = strMsg.substring(tmp + new String("-ZWSM/1.0 Start-\r\n").length());
        if (strMsg.startsWith("From-IP: ") == true) {
          strMsg = strMsg.substring(new String("From-IP: ").length());
          int index = strMsg.indexOf("\r\n");
          if (index >= 0) {
            fromIP = strMsg.substring(0, index);
          }
          strMsg = strMsg.substring(index + new String("\r\n").length());
          if (strMsg.startsWith("To-IP: ") == true) {
            strMsg = strMsg.substring(new String("To-IP: ").length());
            index = strMsg.indexOf("\r\n");
            if (index >= 0) {
              toIP = strMsg.substring(0, index);
            }
            strMsg = strMsg.substring(index + new String("\r\n").length());
            if (strMsg.startsWith("To-Port: ") == true) {
              strMsg = strMsg.substring(new String("To-Port: ").length());
              index = strMsg.indexOf("\r\n");
              if (index >= 0) {
                toPort = Integer.parseInt(strMsg.substring(0, index));
              }
              strMsg = strMsg.substring(index + new String("\r\n").length());
              if (strMsg.startsWith("Type: ") == true) {
                strMsg = strMsg.substring(new String("Type: ").length());
                index = strMsg.indexOf("\r\n");
                if (index >= 0) {
                  type = strMsg.substring(0, index);
                }
                strMsg = strMsg.substring(index + new String("\r\n").length());
                if (strMsg.startsWith("CSeq: ") == true) {
                  strMsg = strMsg.substring(new String("CSeq: ").length());
                  index = strMsg.indexOf("\r\n");
                  if (index >= 0) {
                    CSeq = Integer.parseInt(strMsg.substring(0, index));
                  }
                  strMsg = strMsg.substring(index + new String("\r\n").length());
                  if (strMsg.startsWith("Timestamp: ") == true) {
                    strMsg = strMsg.substring(new String("Timestamp: ").length());
                    index = strMsg.indexOf("\r\n");
                    if (index >= 0) {
                      timestamp = Long.parseLong(strMsg.substring(0, index));
                    }
                    strMsg = strMsg.substring(index + new String("\r\n").length());
                    if (strMsg.startsWith("Content-Length: ") == true) {
                      int contentLength = 0;
                      strMsg = strMsg.substring(new String("Content-Length: ").length());
                      index = strMsg.indexOf("\r\n");
                      if (index >= 0) {
                        contentLength = Integer.parseInt(strMsg.substring(0, index));
                      }
                      strMsg = strMsg.substring(index + new String("\r\n").length());
                      if (contentLength > 0 && strMsg.startsWith("Content: ") == true) {
                        strMsg = strMsg.substring(new String("Content: ").length());
                        String _content = strMsg.substring(0, contentLength);
                        if (_content != null && _content.length() > 0) {
                          try {
                            content = new JSONObject(_content);
                          } catch (JSONException e) {
                            ConfigureLog4J.printStackTrace(e, Log);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    if (timestamp == 0) {
      timestamp = System.currentTimeMillis();
    }
    String ret = "\r\n-ZWSM/1.0 Start-\r\n"
        + "From-IP: " + fromIP + "\r\n"
        + "To-IP: " + toIP + "\r\n"
        + "To-Port: " + toPort + "\r\n"
        + "Type: " + type + "\r\n"
        + "CSeq: " + CSeq + "\r\n"
        + "Timestamp: " + timestamp + "\r\n";
    if (content == null) {
      ret += "Content-Length: 0\r\n";
    } else {
      ret += "Content-Length: " + content.toString().length() + "\r\n"
          + "Content: " + content.toString() + "\r\n";
    }
    ret += "-ZWSM/1.0 End-\r\n-";
    return ret;
  }
}
