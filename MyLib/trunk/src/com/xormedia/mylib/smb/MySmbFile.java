package com.xormedia.mylib.smb;

import java.net.MalformedURLException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyThread;
import com.xormedia.mylib.MyThread.myRunable;

public class MySmbFile {
  private static Logger Log = Logger.getLogger(MySmbFile.class);
  public static final String META_FILENAME = "fileName";
  public static final String META_ISFOLDER = "isFolder";
  public static final String META_FILEPATH = "filePath";
  public static final String META_FILESIZE = "fileSize";
  public static final String META_SUBTREE = "subTree";

  public SmbFile smbFile = null;
  public String fileName = null;
  public boolean isFolder = false;
  public String filePath = null;
  public long fileSize = 0l;

  private ArrayList<MySmbFile> subTree = new ArrayList<MySmbFile>();
  private ArrayList<MySmbFile> uiList = new ArrayList<MySmbFile>();

  public MySmbFile(String _fileName, String _filePath, boolean _isFolder, long _fileSize) {
    try {
      fileName = _fileName;
      isFolder = _isFolder;
      filePath = _filePath;
      fileSize = _fileSize;

      smbFile = new SmbFile(toSmbURL());
    } catch (MalformedURLException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  public MySmbFile(JSONObject obj) {
    try {
      if (obj != null) {
        if (obj.has(META_FILENAME) == true) {
          fileName = obj.getString(META_FILENAME);
        }
        if (obj.has(META_ISFOLDER) == true) {
          isFolder = obj.getBoolean(META_ISFOLDER);
        }
        if (obj.has(META_FILEPATH) == true) {
          filePath = obj.getString(META_FILEPATH);
        }
        if (obj.has(META_FILESIZE) == true) {
          fileSize = obj.getLong(META_FILESIZE);
        }
        smbFile = new SmbFile(toSmbURL());
        if (obj.has(META_SUBTREE) == true && obj.isNull(META_SUBTREE) == false) {
          JSONArray sub = obj.getJSONArray(META_SUBTREE);
          synchronized (subTree) {
            subTree.clear();
            for (int i = 0; i < sub.length(); i++) {
              subTree.add(new MySmbFile(sub.getJSONObject(i)));
            }
          }
        }
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (MalformedURLException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  public MySmbFile(SmbFile _smbFile, String _filePath) {
    try {
      if (_smbFile != null && _smbFile.exists() == true && _filePath != null && _filePath.length() > 0) {
        smbFile = _smbFile;
        fileName = smbFile.getName();
        if (fileName.endsWith("/") == true) {
          fileName = fileName.substring(0, fileName.length() - 1);
        }
        isFolder = smbFile.isDirectory();
        filePath = _filePath;
        if (isFolder == false) {
          fileSize = smbFile.length();
        }
      }
    } catch (SmbException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  /**
   * 按照深度需求获取下层文件树。
   * 
   * @param deep
   *          当<0时，会获取到低；当=0时，将获取本层次的子节点；当>0时，将获取对应层深度的节点组成树结构。
   */
  public void getListOnlineInThread(int deep) {
    try {
      if (smbFile != null && smbFile.isFile() == false) {
        synchronized (subTree) {
          subTree.clear();
          SmbFile[] files = smbFile.listFiles();
          for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
              MySmbFile file = new MySmbFile(files[i], toSmbURL());
              if (files[i].isFile() == false && deep != 0) {
                file.getListOnlineInThread(deep - 1);
              }
              subTree.add(file);
            }
          }
        }
      }
    } catch (SmbException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  private MyThread _getListOnline = null;

  private class _myRunable implements myRunable {
    int deep = -1;

    public _myRunable(int _deep) {
      deep = _deep;
    }

    @Override
    public void run(Message msg) {
      getListOnlineInThread(deep);
      msg.obj = getList();
      _getListOnline = null;
    }
  }

  public String toSmbURL() {
    String ret = filePath + fileName;
    if (isFolder == true && ret.endsWith("/") == false) {
      ret += "/";
    }
    return ret;
  }

  public boolean getListOnline(int deep, Handler handler) {
    boolean ret = false;
    if (_getListOnline == null) {
      _getListOnline = new MyThread(new _myRunable(deep));
      _getListOnline.start(handler);
      ret = true;
    }
    return ret;
  }

  public ArrayList<MySmbFile> getList() {
    synchronized (subTree) {
      uiList.clear();
      uiList.addAll(subTree);
    }
    return uiList;
  }

  public JSONObject toJSONObject() {
    JSONObject ret = new JSONObject();
    try {
      ret.put(META_ISFOLDER, isFolder);
      if (fileName != null) {
        ret.put(META_FILENAME, fileName);
      }
      if (isFolder == false) {
        ret.put(META_FILESIZE, fileSize);
      }
      if (filePath != null) {
        ret.put(META_FILEPATH, filePath);
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  public JSONObject getTreeListJSONObjectInThread() {
    JSONObject ret = toJSONObject();
    try {
      if (isFolder == true) {
        if (subTree.size() == 0) {
          getListOnlineInThread(0);
        }
        JSONArray subNodes = new JSONArray();
        for (int i = 0; i < subTree.size(); i++) {
          subNodes.put(subTree.get(i).getTreeListJSONObjectInThread());
        }
        ret.put(META_SUBTREE, subNodes);
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  public JSONArray getLeavesJSONObjectInThread(boolean isOnlyFile) {
    JSONArray ret = new JSONArray();
    try {
      if (isFolder == true) {
        if (subTree.size() == 0) {
          getListOnlineInThread(0);
        }
        if (subTree.size() > 0) {
          for (int i = 0; i < subTree.size(); i++) {
            JSONArray subNodes = subTree.get(i).getLeavesJSONObjectInThread(isOnlyFile);
            for (int j = 0; j < subNodes.length(); j++) {
              ret.put(subNodes.getJSONObject(j));
            }
          }
        } else if (isOnlyFile == false) {
          ret.put(toJSONObject());
        }
      } else {
        ret.put(toJSONObject());
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  @Override
  protected void finalize() throws Throwable {
    if (subTree != null) {
      subTree.clear();
    }
    if (uiList != null) {
      uiList.clear();
    }
    super.finalize();
  }
}
