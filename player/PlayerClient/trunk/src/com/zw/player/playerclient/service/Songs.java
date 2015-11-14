package com.zw.player.playerclient.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.smb.MySmbFile;

public class Songs {
  private static Logger Log = Logger.getLogger(Songs.class);
  public static final String ATTR_SONGS_NAME = "name";
  public static final String ATTR_SONGS_FILES = "files";

  public String name = null;
  public ArrayList<MySmbFile> files = new ArrayList<MySmbFile>();

  public Songs(String _name) {
    name = _name;
  }

  public ArrayList<MySmbFile> getList() {
    if (files.size() == 0) {
      files = DatabaseHelper.getSongsFileList(name);
    }
    return files;
  }

  public boolean addFiles(JSONArray objs) {
    boolean ret = false;
    ArrayList<MySmbFile> filelist = new ArrayList<MySmbFile>();
    if (objs != null && objs.length() > 0) {
      try {
        for (int i = 0; i < objs.length(); i++) {
          JSONObject obj = objs.getJSONObject(i);
          if (obj != null) {
            MySmbFile file = new MySmbFile(obj);
            filelist.add(file);
          }
        }
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    if (filelist.size() > 0) {
      getList();

      for (int i = 0; i < filelist.size(); i++) {
        boolean found = false;
        for (int j = 0; j < files.size(); j++) {
          if (files.get(j).fileName.compareTo(filelist.get(i).fileName) == 0
              && files.get(j).filePath.compareTo(filelist.get(i).filePath) == 0) {
            found = true;
            break;
          }
        }
        if (found == false) {
          files.add(filelist.get(i));
        }
      }

      ret = DatabaseHelper.updateSongs(Songs.this);
      if (ret == true) {
        if (SongsList.SongsList.indexOf(Songs.this) < 0) {
          SongsList.SongsList.add(Songs.this);
        }
      }
    }
    return ret;
  }

  public boolean deleteFiles(ArrayList<MySmbFile> filelist) {
    boolean ret = false;
    if (filelist.size() > 0) {
      getList();
      for (int i = 0; i < filelist.size(); i++) {
        for (int j = 0; j < files.size(); j++) {
          if (files.get(j).fileName.compareTo(filelist.get(i).fileName) == 0
              && files.get(j).filePath.compareTo(filelist.get(i).filePath) == 0) {
            files.remove(j);
            break;
          }
        }
      }
      ret = DatabaseHelper.updateSongs(Songs.this);
      getList();
      if (files.size() == 0) {
        SongsList.SongsList.remove(Songs.this);
      }
    }
    return ret;
  }

  public boolean delete() {
    boolean ret = false;
    String sql = "DELETE FROM " + DatabaseHelper.SQL_SONGS_LIST_TABLE + " WHERE " + DatabaseHelper.SQL_SONGS_LIST_NAME + "=\"" + name + "\"";
    ret = DatabaseHelper.execSQL(sql);
    if (ret == true) {
      SongsList.SongsList.remove(Songs.this);
    }
    return ret;
  }

  public boolean rename(String _name) {
    boolean ret = false;
    String sql = "UPDATE " + DatabaseHelper.SQL_SONGS_LIST_TABLE + " SET " + DatabaseHelper.SQL_SONGS_LIST_NAME + "=\"" + _name + "\" WHERE "
        + DatabaseHelper.SQL_SONGS_LIST_NAME + "=\"" + name + "\"";
    ret = DatabaseHelper.execSQL(sql);
    if (ret == true) {
      name = _name;
    }
    return ret;
  }

  @Override
  protected void finalize() throws Throwable {
    if (files != null) {
      files.clear();
    }
    super.finalize();
  }

}
