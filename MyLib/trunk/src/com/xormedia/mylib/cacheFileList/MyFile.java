package com.xormedia.mylib.cacheFileList;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.xhr;
import com.xormedia.mylib.smb.MySmbFile;

public class MyFile {
  private static Logger Log = Logger.getLogger(MySmbFile.class);
  public static final String META_FILENAME = "fileName";
  public static final String META_FILEPATH = "filePath";
  public static final String META_FILESIZE = "fileSize";
  public static final String META_STORAGENAME = "storageName";
  public static final String META_DOWNLOADPROGRESS = "downloadProgress";
  public String fileName = null;
  public String fileURL = null;
  public long fileSize = 0l;
  public String storageName = null;
  public int downloadProgress = 0;
  public xhr.isStop downloadIsStop = new xhr.isStop(false);

  public MyFile(JSONObject obj) {
    try {
      if (obj != null) {
        if (obj.has(META_FILENAME) == true) {
          fileName = obj.getString(META_FILENAME);
        }
        if (obj.has(META_FILEPATH) == true) {
          fileURL = obj.getString(META_FILEPATH);
        }
        if (obj.has(META_FILESIZE) == true) {
          fileSize = obj.getLong(META_FILESIZE);
        }
        if (obj.has(META_STORAGENAME) == true) {
          storageName = obj.getString(META_STORAGENAME);
        }
        if (obj.has(META_DOWNLOADPROGRESS) == true) {
          downloadProgress = obj.getInt(META_DOWNLOADPROGRESS);
        }
      }
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  public MyFile(String _fileName, String _fileURL, long _fileSize) {
    fileName = _fileName;
    fileURL = _fileURL;
    fileSize = _fileSize;
  }

  public JSONObject toJSONObject() {
    JSONObject ret = new JSONObject();
    try {
      if (fileName != null) {
        ret.put(META_FILENAME, fileName);
      }
      ret.put(META_FILESIZE, fileSize);
      if (fileURL != null) {
        ret.put(META_FILEPATH, fileURL);
      }
      if (storageName != null) {
        ret.put(META_STORAGENAME, storageName);
      }
      ret.put(META_DOWNLOADPROGRESS, downloadProgress);
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }
}
