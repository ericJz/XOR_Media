package com.zw.player.playerclient.service;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.smb.MySmbFile;
import com.xormedia.mylib.smb.MySmbServer;

public class DatabaseHelper {
  private static Logger Log = Logger.getLogger(DatabaseHelper.class);

  private static final String DB_NAME = "data.db";
  private static final int DB_VERSION = 1;
  public static _DatabaseHelper mDatabaseHelper = null;

  public final static String SQL_PLAYER_SERVER_TABLE = "PlayerServer";
  public final static String SQL_PLAYER_SERVER_IPADDRESS = "serverIPAddress";
  public final static int SQL_PLAYER_SERVER_IPADDRESS_INDEX = 1;
  public final static String SQL_PLAYER_SERVER_NAME = "serverName";
  public final static int SQL_PLAYER_SERVER_NAME_INDEX = 2;
  public final static String SQL_PLAYER_SERVER_DEVICENAME = "deviceName";
  public final static int SQL_PLAYER_SERVER_DEVICENAME_INDEX = 3;
  public final static String SQL_PLAYER_SERVER_DEVICEBRAND = "deviceBrand";
  public final static int SQL_PLAYER_SERVER_DEVICEBRAND_INDEX = 4;
  public final static String SQL_PLAYER_SERVER_SOCKETPORT = "socketPort";
  public final static int SQL_PLAYER_SERVER_SOCKETPORT_INDEX = 5;

  public final static String SQL_SMB_SERVER_TABLE = "SmbServer";
  public final static String SQL_SMB_SERVER_NAME = "name";
  public final static int SQL_SMB_SERVER_NAME_INDEX = 1;
  public final static String SQL_SMB_SERVER_ROOTPATH = "rootPath";
  public final static int SQL_SMB_SERVER_ROOTPATH_INDEX = 2;
  public final static String SQL_SMB_SERVER_USER = "user";
  public final static int SQL_SMB_SERVER_USER_INDEX = 3;
  public final static String SQL_SMB_SERVER_PASSWORD = "password";
  public final static int SQL_SMB_SERVER_PASSWORD_INDEX = 4;
  public final static String SQL_SMB_SERVER_DOMAIN = "domain";
  public final static int SQL_SMB_SERVER_DOMAIN_INDEX = 5;

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
        String SQL = "CREATE TABLE " + SQL_PLAYER_SERVER_TABLE
            + " (_id INTEGER PRIMARY KEY autoincrement,"
            + SQL_PLAYER_SERVER_IPADDRESS + " TEXT,"
            + SQL_PLAYER_SERVER_NAME + " TEXT,"
            + SQL_PLAYER_SERVER_DEVICENAME + " TEXT,"
            + SQL_PLAYER_SERVER_DEVICEBRAND + " TEXT,"
            + SQL_PLAYER_SERVER_SOCKETPORT + " TEXT);";
        db.execSQL(SQL);

        SQL = "CREATE TABLE " + SQL_SMB_SERVER_TABLE
            + " (_id INTEGER PRIMARY KEY autoincrement,"
            + SQL_SMB_SERVER_NAME + " TEXT,"
            + SQL_SMB_SERVER_ROOTPATH + " TEXT,"
            + SQL_SMB_SERVER_USER + " TEXT,"
            + SQL_SMB_SERVER_PASSWORD + " TEXT,"
            + SQL_SMB_SERVER_DOMAIN + " TEXT);";
        db.execSQL(SQL);

        SQL = "CREATE TABLE " + SQL_SONGS_LIST_TABLE
            + " (_id INTEGER PRIMARY KEY autoincrement,"
            + SQL_SONGS_LIST_NAME + " TEXT,"
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

  public static synchronized boolean updateSongs(Songs obj) {
    boolean ret = false;
    if (obj != null && mDatabaseHelper != null && obj.name != null) {
      ArrayList<String> sqls = new ArrayList<String>();
      sqls.add("DELETE FROM " + SQL_SONGS_LIST_TABLE + " WHERE " + SQL_SONGS_LIST_NAME + "=\"" + obj.name + "\";");

      for (int i = 0; i < obj.files.size(); i++) {
        String SQL = "insert into " + SQL_SONGS_LIST_TABLE + "("
            + SQL_SONGS_LIST_NAME + ","
            + SQL_SONGS_LIST_FILENAME + ","
            + SQL_SONGS_LIST_FILEPATH + ","
            + SQL_SONGS_LIST_FILESIZE + ")"
            + " values ("
            + "\"" + obj.name + "\","
            + "\"" + obj.files.get(i).fileName + "\","
            + "\"" + obj.files.get(i).filePath + "\","
            + "\'" + obj.files.get(i).fileSize + "\');";
        sqls.add(SQL);
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

  public static synchronized ArrayList<Songs> getSongsList() {
    ArrayList<Songs> ret = new ArrayList<Songs>();
    if (mDatabaseHelper != null) {
      String SQL = "SELECT * FROM " + SQL_SONGS_LIST_TABLE + " GROUP BY " + SQL_SONGS_LIST_NAME;
      Log.info("getSmbServerListBySQL=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
          boolean res = true;
          while (res == true) {
            Songs obj = new Songs(_cursor.getString(SQL_SONGS_LIST_NAME_INDEX));
            ret.add(obj);
            res = _cursor.moveToNext();
          }
        }
        if (_cursor != null) {
          _cursor.close();
        }
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

  public static synchronized ArrayList<MySmbFile> getSongsFileList(String name) {
    ArrayList<MySmbFile> ret = new ArrayList<MySmbFile>();
    if (mDatabaseHelper != null && name != null && name.length() > 0) {
      String SQL = "SELECT * FROM " + SQL_SONGS_LIST_TABLE + " WHERE " + SQL_SONGS_LIST_NAME + "=\"" + name + "\"";
      Log.info("getSmbServerListBySQL=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
          boolean res = true;
          while (res == true) {
            MySmbFile obj = new MySmbFile(_cursor.getString(SQL_SONGS_LIST_FILENAME_INDEX),
                _cursor.getString(SQL_SONGS_LIST_FILEPATH_INDEX), false,
                _cursor.getLong(SQL_SONGS_LIST_FILESIZE_INDEX));
            ret.add(obj);
            res = _cursor.moveToNext();
          }
        }
        if (_cursor != null) {
          _cursor.close();
        }
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

  public static synchronized boolean updateSmbServer(MySmbServer obj) {
    boolean ret = false;
    if (obj != null && mDatabaseHelper != null) {
      ArrayList<String> sqls = new ArrayList<String>();
      sqls.add("DELETE FROM " + SQL_PLAYER_SERVER_TABLE + " WHERE _id=" + obj.databaseIndex + ";");
      String SQL = "insert into " + SQL_SMB_SERVER_TABLE + "("
          + SQL_SMB_SERVER_NAME + ","
          + SQL_SMB_SERVER_ROOTPATH + ","
          + SQL_SMB_SERVER_USER + ","
          + SQL_SMB_SERVER_PASSWORD + ","
          + SQL_SMB_SERVER_DOMAIN + ")"
          + " values ("
          + "\"" + obj.name + "\","
          + "\"" + obj.rootPath + "\","
          + "\"" + obj.user + "\","
          + "\"" + obj.password + "\","
          + "\'" + obj.domain + "\');";
      sqls.add(SQL);

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

  public static synchronized ArrayList<MySmbServer> getSmbServerListBySQL(String SQL) {
    ArrayList<MySmbServer> ret = new ArrayList<MySmbServer>();
    if (SQL != null && mDatabaseHelper != null) {
      Log.info("getSmbServerListBySQL=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        ret = getSmbServerByCursor(_cursor);
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

  private static ArrayList<MySmbServer> getSmbServerByCursor(Cursor _cursor) {
    ArrayList<MySmbServer> ret = new ArrayList<MySmbServer>();
    if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
      boolean res = true;
      while (res == true) {
        MySmbServer obj = new MySmbServer(_cursor.getString(SQL_SMB_SERVER_NAME_INDEX),
            _cursor.getString(SQL_SMB_SERVER_DOMAIN_INDEX),
            _cursor.getString(SQL_SMB_SERVER_ROOTPATH_INDEX),
            _cursor.getString(SQL_SMB_SERVER_USER_INDEX),
            _cursor.getString(SQL_SMB_SERVER_PASSWORD_INDEX));
        obj.databaseIndex = _cursor.getInt(0);
        ret.add(obj);
        res = _cursor.moveToNext();
      }
    }
    if (_cursor != null) {
      _cursor.close();
    }
    return ret;
  }

  public static synchronized boolean updatePlayerServer(PlayerServer obj) {
    boolean ret = false;
    if (obj != null && mDatabaseHelper != null) {
      ArrayList<String> sqls = new ArrayList<String>();
      sqls.add("DELETE FROM " + SQL_PLAYER_SERVER_TABLE + " WHERE _id=" + obj.databaseIndex + ";");
      String SQL = "insert into " + SQL_PLAYER_SERVER_TABLE + "("
          + SQL_PLAYER_SERVER_IPADDRESS + ","
          + SQL_PLAYER_SERVER_NAME + ","
          + SQL_PLAYER_SERVER_DEVICENAME + ","
          + SQL_PLAYER_SERVER_DEVICEBRAND + ","
          + SQL_PLAYER_SERVER_SOCKETPORT + ")"
          + " values ("
          + "\"" + obj.serverIPAddress + "\","
          + "\"" + obj.serverName + "\","
          + "\"" + obj.deviceName + "\","
          + "\"" + obj.deviceBrand + "\","
          + "\'" + obj.socketPort + "\');";
      sqls.add(SQL);

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

  public static synchronized ArrayList<PlayerServer> getPlayerServerListBySQL(String SQL) {
    ArrayList<PlayerServer> ret = new ArrayList<PlayerServer>();
    if (SQL != null && mDatabaseHelper != null) {
      Log.info("getPlayerServerListBySQL=" + SQL);
      synchronized (mDatabaseHelper) {
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor _cursor = database.rawQuery(SQL, null);
        ret = getPlayerServerByCursor(_cursor);
        mDatabaseHelper.close();
      }
    }
    return ret;
  }

  private static ArrayList<PlayerServer> getPlayerServerByCursor(Cursor _cursor) {
    ArrayList<PlayerServer> ret = new ArrayList<PlayerServer>();
    if (_cursor != null && _cursor.getCount() > 0 && _cursor.moveToFirst() == true) {
      boolean res = true;
      while (res == true) {
        PlayerServer obj = new PlayerServer(_cursor.getString(SQL_PLAYER_SERVER_IPADDRESS_INDEX),
            _cursor.getString(SQL_PLAYER_SERVER_NAME_INDEX),
            _cursor.getInt(SQL_PLAYER_SERVER_SOCKETPORT_INDEX));
        obj.databaseIndex = _cursor.getInt(0);
        obj.deviceName = _cursor.getString(SQL_PLAYER_SERVER_DEVICENAME_INDEX);
        obj.deviceBrand = _cursor.getString(SQL_PLAYER_SERVER_DEVICEBRAND_INDEX);
        ret.add(obj);
        res = _cursor.moveToNext();
      }
    }
    if (_cursor != null) {
      _cursor.close();
    }
    return ret;
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
