package com.zw.player.playerserver.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.cacheFileList.MyFile;

public class DatabaseHelper {
  private static Logger Log = Logger.getLogger(DatabaseHelper.class);

  private static final String DB_NAME = "data.db";
  private static final int DB_VERSION = 1;
  public static _DatabaseHelper mDatabaseHelper = null;

  public final static String SQL_SONGS_LIST_TABLE = "SongsList";
  public final static String SQL_SONGS_LIST_NAME = "SongsName";
  public final static int SQL_SONGS_LIST_NAME_INDEX = 1;
  public final static String SQL_SONGS_LIST_FILENAME = "fileName";
  public final static int SQL_SONGS_LIST_FILENAME_INDEX = 2;
  public final static String SQL_SONGS_LIST_FILEPATH = "filePath";
  public final static int SQL_SONGS_LIST_FILEPATH_INDEX = 3;
  public final static String SQL_SONGS_LIST_FILESIZE = "fileSize";
  public final static int SQL_SONGS_LIST_FILESIZE_INDEX = 4;

  private static class _DatabaseHelper extends SQLiteOpenHelper {

    public _DatabaseHelper(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.beginTransaction();
      try {
        String SQL = "CREATE TABLE " + SQL_SONGS_LIST_TABLE
            + " (_id INTEGER PRIMARY KEY autoincrement,"
            + SQL_SONGS_LIST_NAME + " INTEGER,"
            + SQL_SONGS_LIST_FILENAME + " TEXT,"
            + SQL_SONGS_LIST_FILEPATH + " TEXT,"
            + SQL_SONGS_LIST_FILESIZE + " TEXT);";
        db.execSQL(SQL);
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
  }

  public DatabaseHelper(Context context) {
    if (mDatabaseHelper == null) {
      mDatabaseHelper = new _DatabaseHelper(context);
    }
  }

  public static synchronized boolean updateSongs(long curTime, JSONArray files) {
    boolean ret = false;
    if (curTime > 0 && mDatabaseHelper != null && files != null && files.length() > 0) {
      ArrayList<String> sqls = new ArrayList<String>();
      sqls.add("DELETE FROM " + SQL_SONGS_LIST_TABLE + " WHERE " + SQL_SONGS_LIST_NAME + "=" + curTime + ";");

      for (int i = 0; i < files.length(); i++) {
        try {
          JSONObject file = files.getJSONObject(i);
          if (file.has(MyFile.META_FILENAME) == true && file.has(MyFile.META_FILEPATH) == true
              && file.has(MyFile.META_FILESIZE) == true) {

            String SQL = "insert into " + SQL_SONGS_LIST_TABLE + "("
                + SQL_SONGS_LIST_NAME + ","
                + SQL_SONGS_LIST_FILENAME + ","
                + SQL_SONGS_LIST_FILEPATH + ","
                + SQL_SONGS_LIST_FILESIZE + ")"
                + " values ("
                + "\"" + curTime + "\","
                + "\"" + file.getString(MyFile.META_FILENAME) + "\","
                + "\"" + file.getString(MyFile.META_FILEPATH) + file.getString(MyFile.META_FILENAME) + "\","
                + "\"" + file.getString(MyFile.META_FILESIZE) + "\");";
            sqls.add(SQL);
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }

      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
          for (int i = 0; i < sqls.size(); i++) {
            database.execSQL(sqls.get(i));
          }
          database.setTransactionSuccessful();
          if (sqls.size() > 1) {
            ret = true;
          }
        } catch (SQLException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } finally {
          database.endTransaction();
          sqls.clear();
        }
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

  public static synchronized long getCurrentSongsName() {
    long ret = 0l;
    ArrayList<Long> tmps = new ArrayList<Long>();
    if (mDatabaseHelper != null) {
      String SQL = "SELECT " + SQL_SONGS_LIST_NAME + " FROM " + SQL_SONGS_LIST_TABLE + " GROUP BY " + SQL_SONGS_LIST_NAME + " order by " + SQL_SONGS_LIST_NAME
          + " desc";
      Log.info("getFirstSongList=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
          boolean res = true;
          while (res == true) {
            tmps.add(_cursor.getLong(0));
            res = _cursor.moveToNext();
          }
        }
        if (_cursor != null) {
          _cursor.close();
        }
        mDatabaseHelper.close();
      }
      if (tmps.size() > 0) {
        ret = tmps.get(0);
      }
      if (tmps.size() > 1) {
        String tmpNames = tmps.get(1) + "";
        for (int i = 2; i < tmps.size(); i++) {
          tmpNames += "," + tmps.get(i);
        }
        SQL = "delete from " + SQL_SONGS_LIST_TABLE + " where " + SQL_SONGS_LIST_NAME + " in (" + tmpNames + ");";
        synchronized (mDatabaseHelper) {
          SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
          database.beginTransaction();
          try {
            database.execSQL(SQL);
            database.setTransactionSuccessful();
          } catch (SQLException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          } finally {
            database.endTransaction();
          }
          mDatabaseHelper.close();
        }
      }
      tmps.clear();
    }
    return ret;
  }

  public static synchronized void getSongList(long name) {
    if (mDatabaseHelper != null && name > 0 && MyPlayer.fileList != null) {
      MyPlayer.fileList.clear();
      String SQL = "SELECT * FROM " + SQL_SONGS_LIST_TABLE + " WHERE " + SQL_SONGS_LIST_NAME + "=" + name + ";";
      Log.info("getSongList=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
          boolean res = true;
          while (res == true) {
            MyFile obj = new MyFile(_cursor.getString(SQL_SONGS_LIST_FILENAME_INDEX),
                _cursor.getString(SQL_SONGS_LIST_FILEPATH_INDEX),
                _cursor.getLong(SQL_SONGS_LIST_FILESIZE_INDEX));
            MyPlayer.fileList.add(obj);
            res = _cursor.moveToNext();
          }
        }
        if (_cursor != null) {
          _cursor.close();
        }
        mDatabaseHelper.close();
      }
    }
  }

  public static synchronized boolean execSQL(String SQL) {
    boolean ret = false;
    if (SQL != null && SQL.length() > 0 && mDatabaseHelper != null) {
      Log.info("execSQL:SQL=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
          database.execSQL(SQL);
          database.setTransactionSuccessful();
          ret = true;
        } catch (SQLException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } finally {
          database.endTransaction();
        }
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

}
