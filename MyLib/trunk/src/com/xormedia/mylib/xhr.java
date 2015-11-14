package com.xormedia.mylib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.xormedia.mylib.smb.MySmb;

public class xhr extends Service {
  private static String BOUNDARY = "---------------------------290552136312398";
  public static String POST = "POST";
  public static String GET = "GET";
  public static String DELETE = "DELETE";
  public static String PUT = "PUT";
  public static int SAVE_TYPE_TEXT = 0;
  public static int SAVE_TYPE_LOCAL_FILE = 2;
  private static String cookie = null;
  private static ArrayList<xhr.request> requestQueue = new ArrayList<xhr.request>();
  private static xhrThread Thread1 = null;
  public static boolean useFakeData = false;
  private static xhrFakeData mFakeData = null;
  public static String mLocalIPAddress;
  public static final int CONNECT_TYPE_NULL = 0;
  public static final int CONNECT_TYPE_MOBILE = 1;
  public static final int CONNECT_TYPE_WIFI = 2;
  public static final int CONNECT_TYPE_OTHER = 3;
  public static int mConnectType = CONNECT_TYPE_NULL;

  private static Logger Log = Logger.getLogger(xhr.class);

  private static BroadcastReceiver connectionReceiver = new BroadcastReceiver() {
    @SuppressLint("InlinedApi")
    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
      NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      NetworkInfo activeInfo = connectMgr.getActiveNetworkInfo();
      if ((mobNetInfo != null && mobNetInfo.isConnected()) ||
          (wifiNetInfo != null && wifiNetInfo.isConnected()) ||
          (activeInfo != null && activeInfo.isAvailable())) {
        if (mobNetInfo != null && mobNetInfo.isConnected()) {
          mConnectType = CONNECT_TYPE_MOBILE;
        } else if (wifiNetInfo != null && wifiNetInfo.isConnected()) {
          mConnectType = CONNECT_TYPE_WIFI;
        } else if (activeInfo != null && activeInfo.isAvailable()) {
          mConnectType = CONNECT_TYPE_OTHER;
        }
        mLocalIPAddress = MySmb.getLocIPAddress(mConnectType);
      } else {
        mLocalIPAddress = null;
        mConnectType = CONNECT_TYPE_NULL;
        // MyToast.show("网络无连接，请检查您的网络！", Toast.LENGTH_LONG);
      }
      Log.info("mobile:" + (mobNetInfo != null ? mobNetInfo.isConnected() : "null")
          + ";" + "wifi:" + (wifiNetInfo != null ? wifiNetInfo.isConnected() : "null")
          + ";" + "active:" + (activeInfo != null ? activeInfo.getTypeName() : "null"));
    }
  };

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate() {
    super.onCreate();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(connectionReceiver, intentFilter);
    ContentResolver resolver = getApplicationContext().getContentResolver();
    int value = Settings.System.getInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
    if (Settings.System.WIFI_SLEEP_POLICY_NEVER != value) {
      Settings.System.putInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER);
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_NOT_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(connectionReceiver);
  }

  private static Context mContext = null;

  public static boolean networkConnect() {
    boolean ret = false;
    if (mContext != null) {
      ConnectivityManager connectMgr = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
      NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
      NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      NetworkInfo activeInfo = connectMgr.getActiveNetworkInfo();

      if ((mobNetInfo != null && mobNetInfo.isConnected()) ||
          (wifiNetInfo != null && wifiNetInfo.isConnected()) ||
          (activeInfo != null && activeInfo.isAvailable())) {
        ret = true;
      } else {
        // ret = false;
        ret = true;
      }
    }
    return ret;
  }

  public static void start(Context _context) {
    if (_context != null) {
      Log.info("xhr::start()");
      mContext = _context;
      new MyToast(mContext);
      Intent intent = new Intent(mContext, xhr.class);
      mContext.startService(intent);
    }
  }

  public static void setFakeData(xhrFakeData fake) {
    if (fake != null) {
      mFakeData = fake;
    }
  }

  public static void setUseFakeData(boolean b) {
    useFakeData = b;
  }

  public static interface xhrFakeData {
    public xhrResponse getResponse(xhrParameter param);
  }

  public static interface xhrCallBack {
    public void load(xhrResponse response, xhr.request param);

    public void error(xhrResponse response, xhr.request param);
  }

  public static interface xhrProgress {
    public void progress(long _progress, xhrParameter param);
  }

  public static class xhrResponse {
    public int code = 0;
    public String result = "";
    public long contentLength = 0;
  }

  public static class xhrParameter {
    public String url = "";
    public ArrayList<Map<String, String>> requestHeaders = null;
    public String method = GET;
    public int connectTimeout = 5000;
    public int readTimeout = 15000;
    public int saveType = SAVE_TYPE_TEXT;
    public String savePath = "";
    public String saveFileName = "";
    public boolean async = true;
    public JSONObject putData = null;
    public MyRandomAccessFile putFile = null;
    public MyBitmap putBitmap = null;
  }

  private static class xhrThread extends Thread {
    private request request = null;
    public boolean isExist = false;

    public xhrThread() {
    }

    public xhrThread(request req) {
      request = req;
    }

    @Override
    public void run() {
      Log.info("Enter: xhrThread::run");
      while (isExist == false) {
        while (request != null || requestQueue.size() > 0) {
          request req = null;
          if (request != null) {
            req = request;
            request = null;
            isExist = true;
          } else {
            synchronized (requestQueue) {
              if (requestQueue.size() > 0) {
                req = requestQueue.remove(0);
              }
            }
          }
          if (req != null && req.requestParameter != null) {
            if (req.isStop == false) {
              req.response = requestToServer(req.requestParameter);
            }
            if (req.mHandler != null && req.isStop == false) {
              if (req.mWeakReferenceView == null
                  || (req.mWeakReferenceView != null && req.mWeakReferenceView
                      .get() != null)) {
                Message msg = new Message();
                req.mHandler.sendMessage(msg);
              }
            }
            else if (req.mHandler == null) {
              synchronized (req.result) {
                req.result.notifyAll();
              }
            }
          }
          req.connIsFinish = true;
        }
      }
      Log.info("Leave: xhrThread::run]");
    }
  }

  public static xhrResponse requestToServer(xhrParameter param) {
    return requestToServer(param, null);
  }

  public static xhrResponse requestToServer(xhrParameter param, xhrProgress progressCallBack) {
    return requestToServer(param, progressCallBack, new isStop(false));
  }

  public static class isStop {
    public boolean isStop = false;

    public isStop(boolean _isStop) {
      isStop = _isStop;
    }
  }

  private static xhrResponse requestToServerByHTTP(xhrParameter param, File saveFile
      , xhrProgress progressCallBack, isStop _isStop, xhrResponse response) {
    long tmpTime = 0;
    RandomAccessFile tmpFile = null;
    BufferedReader in = null;
    BufferedInputStream inn = null;
    long tmpFileLength = 0;
    String url = new String(param.url);
    try {
      if (param.putData != null && param.method == GET) {
        if (url.indexOf("?") < 0) {
          url += "?";
        } else {
          url += "&";
        }

        JSONArray names = param.putData.names();
        for (int i = 0; i < names.length(); i++) {
          if (i > 0) {
            url += "&";
          }
          url += URLEncoder.encode(names.getString(i),
              "UTF-8")
              + "="
              + URLEncoder.encode(param.putData
                  .getString(names.getString(i)),
                  "UTF-8");
        }
      }
      tmpTime = new Date().getTime();
      Log.info("xhr.requestToServer::HTTP Request Start!\nmethod:"
          + param.method + ";\nURL:" + url);
      URL realUrl = new URL(url);
      // 打开和URL之间的连接
      HttpURLConnection conn = (HttpURLConnection) realUrl
          .openConnection();
      conn.setRequestMethod(param.method);
      // 设置HTTP请求头属性
      if (cookie != null) {
        conn.setRequestProperty("Cookie", cookie);
      }
      boolean isFormData = false;
      if (param.requestHeaders != null) {
        for (int i = 0; i < param.requestHeaders.size(); i++) {
          if (param.requestHeaders.get(i).get("field")
              .compareTo("Content-Type") == 0
              && param.requestHeaders.get(i).get("value")
                  .compareTo("multipart/form-data") == 0) {
            isFormData = true;
            conn.setRequestProperty(param.requestHeaders
                .get(i).get("field"),
                param.requestHeaders.get(i)
                    .get("value")
                    + "; boundary="
                    + BOUNDARY);
          } else {
            conn.setRequestProperty(param.requestHeaders
                .get(i).get("field"),
                param.requestHeaders.get(i)
                    .get("value"));
          }
        }
      } else {
        conn.setRequestProperty("accept",
            "application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // conn.setRequestProperty("connection", "close");
        conn.setRequestProperty("connection", "Keep-Alive");
      }
      if (param.saveType == SAVE_TYPE_LOCAL_FILE) {
        conn.setRequestProperty("accept",
            "application/octet-stream");
        // conn.setRequestProperty("Content-Type",
        // "application/octet-stream");
        tmpFile = new RandomAccessFile(
            saveFile.getAbsolutePath() + ".xhrTemp", "rw");
        tmpFileLength = tmpFile.length();
        Log.info("xhr.requestToServer::tmpFile.length()="
            + tmpFile.length());
        if (tmpFileLength > 1000 * 1000) {
          tmpFileLength -= (1000 * 1000);
          conn.setRequestProperty("Range", "bytes "
              + (tmpFileLength) + "-");
        } else {
          tmpFileLength = 0;
          // conn.setRequestProperty("Range", "bytes 0-");
        }
        Log.info("xhr.requestToServer::start realfileLength="
            + (tmpFileLength));
        conn.setChunkedStreamingMode(0);
      }
      if (param.putFile != null) {
        conn.setRequestProperty("Content-Length",
            param.putFile.getSize() + "");
        conn.setRequestProperty("Content-Range",
            "bytes "
                + param.putFile.getPos()
                + "-"
                + (param.putFile.getPos()
                    + param.putFile.getSize() - 1)
                + "/" + param.putFile.getFileLength());
      }
      Log.info("xhr.requestToServer::Request Properties="
          + conn.getRequestProperties().toString());
      conn.setConnectTimeout(param.connectTimeout);
      conn.setReadTimeout(param.readTimeout);
      Log.info("xhr.requestToServer::connectTimeout="
          + param.connectTimeout + "\nreadTimeout="
          + param.readTimeout);
      if (param.method == GET
          || param.method == DELETE
          || (param.putData == null
              && param.putBitmap == null && param.putFile == null)) {
        // 建立实际的连接
        conn.connect();
      } else {
        // 发送POST或PUT请求必须设置如下两行
        conn.setDoOutput(true);
        conn.setDoInput(true);
        if (param.putData != null) {
          // 获取URLConnection对象对应的输出流
          PrintWriter out = new PrintWriter(conn.getOutputStream());
          if (isFormData == true) {
            String putData = "--";
            putData += BOUNDARY;
            JSONArray names = param.putData.names();
            String operationStr = null;
            String payloadStr = null;
            for (int i = 0; i < names.length(); i++) {
              String tmp = "\r\nContent-Disposition: form-data; name=\""
                  + names.getString(i)
                  + "\"\r\n\r\n"
                  + param.putData.getString(names
                      .getString(i))
                  + "\r\n--"
                  + BOUNDARY;
              if (names.getString(i).compareTo(
                  "operation") == 0) {
                operationStr = tmp;
              } else if (names.getString(i).compareTo(
                  "payload") == 0) {
                payloadStr = tmp;
              } else {
                putData += tmp;
              }
            }
            if (operationStr != null) {
              putData += operationStr;
            }
            if (payloadStr != null) {
              putData += payloadStr;
            }
            putData += "--\r\n";
            Log.info("xhr.requestToServer::putData:\n"
                + putData);
            // 发送请求参数
            out.print(putData);
          } else {
            if (param.putData.toString().indexOf("\"@") > 0) {
              JSONArray array = param.putData.names();
              String paramStr = "";
              JSONObject obj1 = new JSONObject();
              for (int i = 0; i < array.length(); i++) {
                if (array.getString(i).indexOf("@") == 0) {
                  obj1.put(array.getString(i),
                      param.putData.get(array
                          .getString(i)));
                }
              }
              paramStr = obj1.toString();
              paramStr = paramStr.substring(0,
                  paramStr.length() - 1);
              obj1 = new JSONObject();
              for (int i = 0; i < array.length(); i++) {
                if (array.getString(i).indexOf("@") != 0) {
                  obj1.put(array.getString(i),
                      param.putData.get(array
                          .getString(i)));
                }
              }

              if (obj1.toString().length() > 2 && paramStr.length() > 1) {
                paramStr += ",";
              }
              paramStr += obj1.toString().substring(1);

              Log.info("xhr.requestToServer::putData:\n"
                  + paramStr);
              // 发送请求参数
              out.print(paramStr);
            } else {
              Log.info("xhr.requestToServer::putData:\n"
                  + param.putData.toString());
              // 发送请求参数
              out.print(param.putData.toString());
            }
          }
          // flush输出流的缓冲
          out.flush();
          out.close();
        }
        if (param.putFile != null) {
          conn.setChunkedStreamingMode(0);
          BufferedOutputStream outputStream = new BufferedOutputStream(
              conn.getOutputStream());
          byte[] buffer = param.putFile.readBuffer();
          while (buffer != null) {
            outputStream.write(buffer);
            outputStream.flush();
            buffer = null;
            buffer = param.putFile.readBuffer();
          }
          outputStream.close();
        }
        if (param.putBitmap != null
            && param.putBitmap.mBitmap != null) {
          DataOutputStream dos = new DataOutputStream(
              conn.getOutputStream());
          param.putBitmap.mBitmap.compress(
              Bitmap.CompressFormat.JPEG,
              param.putBitmap.quality, dos);
          dos.flush();
          dos.close();
          param.putBitmap.destroyBitmap();
        }
      }
      response.code = conn.getResponseCode();
      Map<String, List<String>> tmpMap = conn.getHeaderFields();
      List<String> tmpList = tmpMap.get("Set-Cookie");
      String tmpCookie = null;
      if (tmpList != null) {
        tmpCookie = tmpList.get(0);
      }
      if (tmpCookie != null) {
        tmpCookie = tmpCookie.substring(0,
            tmpCookie.indexOf(";"));
      }
      if (tmpCookie != null) {
        cookie = tmpCookie;
        Log.info("xhr.requestToServer::Set-Cookie=" + cookie);
      }
      tmpList = tmpMap.get("Content-Length");
      response.contentLength = (tmpList != null) ? Long
          .parseLong(tmpList.get(0)) : 0;
      Log.info("xhr.requestToServer::Content-Length="
          + response.contentLength);
      if (conn.getErrorStream() != null) {
        in = new BufferedReader(new InputStreamReader(
            conn.getErrorStream()));
        String line;
        while ((line = in.readLine()) != null) {
          response.result += "\n" + line;
        }
      } else if (conn.getInputStream() != null) {
        if (param.saveType == SAVE_TYPE_TEXT) {
          // 定义BufferedReader输入流来读取URL的响应
          in = new BufferedReader(new InputStreamReader(
              conn.getInputStream()));
          char[] buffer = new char[100 * 1024];
          int bufferRead = in.read(buffer);

          while (bufferRead > 0) {
            response.result += new String(buffer, 0, bufferRead);
            bufferRead = in.read(buffer);
          }
        } else if (param.saveType == SAVE_TYPE_LOCAL_FILE) {
          inn = new BufferedInputStream(conn.getInputStream());
          int bufferSize = Math.min(inn.available(),
              1024 * 1024 * 8);
          Log.info("xhr.requestToServer::bufferSize[max]="
              + bufferSize);
          if (bufferSize == 0) {
            bufferSize = 1024 * 1024 * 8;
          }
          byte[] buffer = new byte[bufferSize];
          int bytesRead = inn.read(buffer);
          long tmp1FileLength = 0;
          if (progressCallBack != null) {
            progressCallBack.progress(0, param);
          }
          while (_isStop != null && _isStop.isStop == false && bytesRead > 0 && response.contentLength > 0) {
            tmpFile.seek(tmpFileLength);
            tmpFile.write(buffer, 0, bytesRead);
            tmpFileLength += bytesRead;
            tmp1FileLength += bytesRead;
            Log.info("xhr.requestToServer::bytesRead=" + bytesRead);
            Log.info("xhr.requestToServer::tmpFileLength=   " + tmpFileLength);
            Log.info("xhr.requestToServer::tmpFile.length()=" + tmpFile.length());
            bytesRead = inn.read(buffer);
            if (progressCallBack != null) {
              progressCallBack.progress(tmpFileLength, param);
            }
          }
          Log.info("xhr.requestToServer::end File.length()="
              + tmpFile.length());
          Log.info("xhr.requestToServer::tmp1FileLength="
              + tmp1FileLength);
          inn.close();
          inn = null;
          tmpFile.close();
          tmpFile = null;
          if (response.contentLength == 0
              || response.contentLength == tmp1FileLength) {
            File tmp = new File(saveFile.getAbsolutePath()
                + ".xhrTemp");
            if (tmp.exists() == true
                && saveFile.exists() == false) {
              if (progressCallBack != null) {
                progressCallBack.progress(100, param);
              }
              tmp.renameTo(saveFile);
            }
          }
        }
      }

    } catch (MalformedURLException e) {
      response.result = "new URL Error(" + url + "):"
          + e.toString() + "\n" + e.getLocalizedMessage()
          + "\n" + e.getMessage();
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (ProtocolException e) {
      response.result = "conn.setRequestMethod Error(" + url
          + "):" + e.toString();
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (IOException e) {
      response.result = "connect Error(" + url + "):"
          + e.toString() + "\n" + e.getLocalizedMessage()
          + "\n" + e.getMessage();
      if (e.getMessage() != null && e.getMessage().length() > 0) {
        if (e.getMessage().indexOf("authentication challenge") >= 0
            && response.code == 0) {
          response.code = 401;
        } else if (e.getMessage().indexOf("timeout") >= 0
            && response.code == 0) {
          MyToast.show("网络不给力！", Toast.LENGTH_SHORT);
        }
      }
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (JSONException e) {
      response.result = "putData Error("
          + url
          + "):"
          + e.toString()
          + (param.putData != null ? "\n"
              + param.putData.toString() : "") + "\n"
          + e.getLocalizedMessage() + "\n" + e.getMessage();
      ConfigureLog4J.printStackTrace(e, Log);
    }
    // 使用finally块来关闭输入流
    finally {
      try {
        if (in != null) {
          in.close();
        }
        if (inn != null) {
          inn.close();
        }
        if (tmpFile != null) {
          tmpFile.close();
        }
      } catch (IOException ex) {
        Log.info("file close Error(" + url + "):"
            + ex.toString() + "\n"
            + ex.getLocalizedMessage() + "\n"
            + ex.getMessage());
        ConfigureLog4J.printStackTrace(ex, Log);
      }
    }
    tmpTime = new Date().getTime() - tmpTime;
    Log.info("xhr.requestToServer:: HTTP Request End!\nmethed:"
        + param.method + "\nURL:" + url + "\nTime consuming="
        + tmpTime + "\nresponse.code:"
        + String.valueOf(response.code)
        + "\nresponse.result.length:"
        + response.result.getBytes().length + "B\nresponse.result:");
    if (response.result.length() > 1024) {
      Log.info(response.result.substring(0, 1024));
    } else {
      Log.info(response.result);
    }

    // for (int i = 0; i < response.result.length(); i += 2048) {
    // Log.info("["
    // + i
    // + "]"
    // + response.result
    // .substring(
    // i,
    // ((i + 2048) > (response.result
    // .length())) ? (response.result
    // .length()) : (i + 2048)));
    // }

    return response;
  }

  private static xhrResponse requestToServerBySMB(xhrParameter param, File saveFile
      , xhrProgress progressCallBack, isStop _isStop, xhrResponse response) {
    long tmpTime = 0;
    RandomAccessFile tmpFile = null;
    // BufferedInputStream inn = null;
    SmbFileInputStream inn = null;
    long tmpFileLength = 0;
    SmbFile mSmbFile = null;
    String url = new String(param.url);
    try {
      tmpTime = new Date().getTime();
      Log.info("xhr.requestToServer::SMB Request Start!\nURL:" + url);
      mSmbFile = new SmbFile(url);
      if (mSmbFile.exists() == true && mSmbFile.length() > 0) {
        if (param.saveType == SAVE_TYPE_LOCAL_FILE) {
          tmpFile = new RandomAccessFile(
              saveFile.getAbsolutePath() + ".xhrTemp", "rw");
        }
        response.contentLength = mSmbFile.length();
        Log.info("xhr.requestToServer::Content-Length="
            + response.contentLength);

        // inn = new BufferedInputStream(mSmbFile.getInputStream());
        inn = new SmbFileInputStream(url);
        long bufferSize = Math.min((1024l * 1024l * 1l), response.contentLength);
        Log.info("xhr.requestToServer::bufferSize[max]="
            + bufferSize);
        byte[] buffer = new byte[(int) bufferSize];
        int bytesRead = inn.read(buffer);
        if (progressCallBack != null) {
          progressCallBack.progress(0, param);
        }
        while (_isStop != null && _isStop.isStop == false && bytesRead > 0 && response.contentLength > 0) {
          tmpFile.seek(tmpFileLength);
          tmpFile.write(buffer, 0, bytesRead);
          tmpFileLength += bytesRead;
          Log.info("xhr.requestToServer::bytesRead=" + bytesRead);
          Log.info("xhr.requestToServer::tmpFileLength=   " + tmpFileLength);
          Log.info("xhr.requestToServer::tmpFile.length()=" + tmpFile.length());
          if (progressCallBack != null) {
            progressCallBack.progress(tmpFileLength, param);
          }

          if (tmpFileLength < response.contentLength) {
            int tmp = (int) (response.contentLength - tmpFileLength);
            if (tmp < buffer.length) {
              buffer = new byte[tmp];
            }
            bytesRead = inn.read(buffer);
          } else {
            break;
          }
        }
        Log.info("xhr.requestToServer::end File.length()="
            + tmpFile.length());
        inn.close();
        inn = null;
        tmpFile.close();
        tmpFile = null;
        if (response.contentLength == tmpFileLength) {
          response.code = 200;
          File tmp = new File(saveFile.getAbsolutePath()
              + ".xhrTemp");
          if (tmp.exists() == true
              && saveFile.exists() == false) {
            if (progressCallBack != null) {
              progressCallBack.progress(tmp.length(), param);
            }
            tmp.renameTo(saveFile);
          }
        } else {
          File tmp = new File(saveFile.getAbsolutePath()
              + ".xhrTemp");
          if (tmp.exists() == true) {
            tmp.delete();
          }
        }
      }
    } catch (MalformedURLException e) {
      response.result = "new smbFile Error(" + url + "):"
          + e.toString() + "\n" + e.getLocalizedMessage()
          + "\n" + e.getMessage();
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (IOException e) {
      response.result = "connect Error(" + url + "):"
          + e.toString() + "\n" + e.getLocalizedMessage()
          + "\n" + e.getMessage();
      if (e.getMessage() != null && e.getMessage().length() > 0) {
        if (e.getMessage().indexOf("authentication challenge") >= 0
            && response.code == 0) {
          response.code = 401;
        } else if (e.getMessage().indexOf("timeout") >= 0
            && response.code == 0) {
          MyToast.show("网络不给力！", Toast.LENGTH_SHORT);
        }
      }
      ConfigureLog4J.printStackTrace(e, Log);
    }
    // 使用finally块来关闭输入流
    finally {
      try {
        if (inn != null) {
          inn.close();
        }
        if (tmpFile != null) {
          tmpFile.close();
        }
      } catch (IOException ex) {
        Log.info("file close Error(" + url + "):"
            + ex.toString() + "\n"
            + ex.getLocalizedMessage() + "\n"
            + ex.getMessage());
        ConfigureLog4J.printStackTrace(ex, Log);
      }

      tmpTime = new Date().getTime() - tmpTime;
      Log.info("xhr.requestToServer:: HTTP Request End!\nmethed:"
          + param.method + "\nURL:" + url + "\nTime consuming="
          + tmpTime + "\nresponse.code:"
          + String.valueOf(response.code)
          + "\nresponse.contentLength"
          + response.contentLength);

    }
    return response;
  }

  @SuppressLint("DefaultLocale")
  public static xhrResponse requestToServer(xhrParameter param,
      xhrProgress progressCallBack, isStop _isStop) {
    Log.info("Enter: xhr::requestToServer");
    xhrResponse response = null;
    if (useFakeData == true && mFakeData != null) {
      response = mFakeData.getResponse(param);
    }
    if (response == null) {
      response = new xhrResponse();
    }
    if (response.code == 0 && networkConnect() == true) {
      File saveFile = null;
      if (param != null && param.saveType == SAVE_TYPE_LOCAL_FILE
          && param.savePath != null && param.url != null) {
        if (param.saveFileName == null
            || param.saveFileName.length() == 0) {
          if (param.url.lastIndexOf("?") >= 0) {
            param.saveFileName = param.url.substring(0,
                param.url.lastIndexOf("?"));
          } else {
            param.saveFileName = param.url;
          }
          if (param.saveFileName.lastIndexOf("/") >= 0) {
            param.saveFileName = param.saveFileName
                .substring(param.saveFileName.lastIndexOf("/") + 1);
          }
        }
        if (param.saveFileName.length() > 0) {
          saveFile = new File(param.savePath, param.saveFileName);
          if (saveFile != null && saveFile.exists() == true) {
            saveFile.delete();
          }
        }
      }
      if (param != null
          && param.url != null
          && param.url.length() > 0) {
        String tmp = param.url.substring(0, 7);
        tmp = tmp.toLowerCase();
        if (tmp.startsWith("http://") == true) {
          response = requestToServerByHTTP(param, saveFile, progressCallBack, _isStop, response);
        } else if (tmp.startsWith("smb://") == true) {
          response = requestToServerBySMB(param, saveFile, progressCallBack, _isStop, response);
        }
      }
    }
    Log.info("Leave: xhr::requestToServer");
    return response;
  }

  public static class request {
    public xhrParameter requestParameter = null;
    public xhrCallBack responseCallback = null;
    public xhrResponse response = null;
    public Handler mHandler = null;
    public boolean connIsFinish = false;
    public boolean isStop = false;
    public WeakReference<View> mWeakReferenceView = null;

    public static class waitResult {
      public boolean result = false;
    }

    public waitResult result = null;

    public request(xhrParameter param) {
      requestParameter = param;
      result = new waitResult();
    }

    public request(xhrParameter param, xhrCallBack callback) {
      requestParameter = param;
      responseCallback = callback;
      result = new waitResult();
    }

    public request(xhrParameter param, xhrCallBack callback, View view) {
      requestParameter = param;
      responseCallback = callback;
      result = new waitResult();
      if (view != null) {
        mWeakReferenceView = new WeakReference<View>(view);
      }
    }

    private void callResponseCallback() {
      if (responseCallback != null
          && response != null
          && (mWeakReferenceView == null || (mWeakReferenceView != null && mWeakReferenceView
              .get() != null))) {
        if (response.code >= 200 && response.code < 300) {
          responseCallback.load(response, this);
        } else {
          responseCallback.error(response, this);
        }
      }
    }

    @SuppressLint("HandlerLeak")
    public void start() {
      isStop = false;
      connIsFinish = false;
      if (requestParameter != null) {
        if (requestParameter.async == false) {
          mHandler = null;
          synchronized (result) {
            result.result = false;
            new xhrThread(this).start();
            try {
              result.wait(requestParameter.connectTimeout);
            } catch (InterruptedException e1) {
              ConfigureLog4J.printStackTrace(e1, Log);
            }
            callResponseCallback();
          }
        } else {
          if (responseCallback != null && mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper()) {
              @Override
              public void handleMessage(Message msg) {
                callResponseCallback();
              }
            };
          }
          synchronized (requestQueue) {
            requestQueue.add(this);
          }
          if (Thread1 == null) {
            Thread1 = new xhrThread();
            Thread1.start();
          }
        }
      }
    }

    public void stop() {
      isStop = true;
    }
  }

  public static void stopStaticThread() {
    if (Thread1 != null) {
      Thread1.isExist = true;
      Thread1 = null;
      Log.info("xhr.stopStaticThread::Thread1 is stop.");
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

}
