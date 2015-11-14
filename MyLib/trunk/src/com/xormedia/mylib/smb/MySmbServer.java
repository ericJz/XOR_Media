package com.xormedia.mylib.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyThread;
import com.xormedia.mylib.MyThread.myRunable;

public class MySmbServer {
  private static Logger Log = Logger.getLogger(MySmbServer.class);
  public final String ATTR_NAME = "name";
  public final String ATTR_ROOTPATH = "rootPath";
  public final String ATTR_USER = "user";
  public final String ATTR_PASSWORD = "password";
  public final String ATTR_DOMAIN = "domain";

  public String name = null;
  public String rootPath = null;
  public String user = null;
  public String password = null;
  public String domain = null;
  private SmbFile smbFile = null;
  public int databaseIndex = -1;

  private ArrayList<MySmbFile> childrenList = new ArrayList<MySmbFile>();
  private ArrayList<MySmbFile> uiList = new ArrayList<MySmbFile>();

  public MySmbServer(String smbURL) {
    parseSmbURL(smbURL);

    try {
      smbFile = new SmbFile(toSmbURL());
    } catch (MalformedURLException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }

  }

  public MySmbServer(String _name, String _domain, String _rootPath, String _user, String _password) {
    name = _name;
    domain = _domain;
    rootPath = _rootPath;
    user = _user;
    password = _password;

    try {
      smbFile = new SmbFile(toSmbURL());
    } catch (MalformedURLException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }

  }

  public MySmbServer(JSONObject obj) {
    if (obj != null) {
      try {
        if (obj.has(ATTR_NAME) == true && obj.isNull(ATTR_NAME) == false) {
          name = obj.getString(ATTR_NAME);
        }
        if (obj.has(ATTR_ROOTPATH) == true && obj.isNull(ATTR_ROOTPATH) == false) {
          rootPath = obj.getString(ATTR_ROOTPATH);
        }
        if (obj.has(ATTR_USER) == true && obj.isNull(ATTR_USER) == false) {
          user = obj.getString(ATTR_USER);
        }
        if (obj.has(ATTR_PASSWORD) == true && obj.isNull(ATTR_PASSWORD) == false) {
          password = obj.getString(ATTR_PASSWORD);
        }
        if (obj.has(ATTR_DOMAIN) == true && obj.isNull(ATTR_DOMAIN) == false) {
          domain = obj.getString(ATTR_DOMAIN);
        }
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
      try {
        smbFile = new SmbFile(toSmbURL());
      } catch (MalformedURLException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  public void setLoginInfo(String _user, String _password, String _domain) {
    domain = _domain;
    user = _user;
    password = _password;

    synchronized (smbFile) {
      try {
        smbFile = new SmbFile(toSmbURL());
      } catch (MalformedURLException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  private MyThread getChildrenInThread = new MyThread(new myRunable() {

    @Override
    public void run(Message msg) {
      try {
        synchronized (smbFile) {
          synchronized (childrenList) {
            childrenList.clear();
            SmbFile[] files = smbFile.listFiles();
            if (files != null && files.length > 0) {
              for (int i = 0; i < files.length; i++) {
                if (files[i] != null) {
                  childrenList.add(new MySmbFile(files[i], toSmbURL()));
                }
              }
            }
          }
          msg.obj = getList();
        }
      } catch (SmbException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  });

  public void getListOnline(Handler handler) {
    getChildrenInThread.start(handler);
  }

  public ArrayList<MySmbFile> getList() {
    synchronized (childrenList) {
      uiList.clear();
      uiList.addAll(childrenList);
    }
    return uiList;
  }

  // smb://domain;username:password@server/share/path/to/file.txt
  public void parseSmbURL(String smbURL) {
    if (smbURL.indexOf("smb://") == 0) {
      String host = smbURL.substring(6);
      if (host.indexOf("/") > 0) {
        host = host.substring(0, host.indexOf("/"));
      }
      if (host.indexOf(";") > 0) {
        domain = host.substring(0, host.indexOf(";"));
        host = host.substring(host.indexOf(";") + 1);
      }
      if (host.indexOf("@") > 0) {
        String auth = host.substring(0, host.indexOf("@"));
        host = host.substring(host.indexOf("@") + 1);
        String[] auths = auth.split(":");
        if (auths.length == 2) {
          user = auths[0];
          password = auths[1];
        }
      }
      if (host.length() > 0) {
        name = host;
      }
      if (smbURL.indexOf(host) > 0) {
        rootPath = smbURL.substring(smbURL.indexOf(host));
      }
    }
  }

  public JSONObject toJSONObject() {
    JSONObject ret = new JSONObject();
    try {
      if (name != null) {
        ret.put(ATTR_NAME, name);
      }
      if (rootPath != null) {
        ret.put(ATTR_ROOTPATH, rootPath);
      }
      if (user != null) {
        ret.put(ATTR_USER, user);
      }
      if (password != null) {
        ret.put(ATTR_PASSWORD, password);
      }
      if (domain != null) {
        ret.put(ATTR_DOMAIN, domain);
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  public String toSmbURL() {
    String smbURL = null;
    smbURL = "smb://";
    if (domain != null && domain.length() > 0) {
      smbURL += domain + ";";
    }
    String auth = "";
    if (user != null && user.length() > 0) {
      auth = user;
    }
    if (password != null && password.length() > 0) {
      auth += ":" + password;
    }
    if (auth.length() > 0) {
      smbURL += auth + "@";
    }
    if (rootPath != null && rootPath.length() > 0) {
      smbURL += rootPath;
    }
    if (smbURL.endsWith("/") == false) {
      smbURL += "/";
    }
    Log.info("smbURL=" + smbURL);
    return smbURL;
  }

  public boolean ping() {
    boolean ret = false;
    if (rootPath != null && rootPath.length() > 0) {
      String tmp = null;
      if (rootPath.indexOf("/") > 0) {
        tmp = rootPath.substring(0, rootPath.indexOf("/"));
      } else {
        tmp = rootPath;
      }
      try {
        Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + tmp);
        if (p.waitFor() == 0) {
          ret = true;
        }
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (InterruptedException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  @Override
  protected void finalize() throws Throwable {
    if (childrenList != null) {
      childrenList.clear();
    }
    if (uiList != null) {
      uiList.clear();
    }
    super.finalize();
  }

}
