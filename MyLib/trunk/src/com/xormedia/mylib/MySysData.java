package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class MySysData {
  private static Logger Log = Logger.getLogger(MySysData.class);

  public final static String MODE_DEFAULT_DATA = "default_data";
  public final static String MODE_USER_DATA = "user_data";
  private final static String VERSION_CODE = "default_data_version_code";
  public final static String MODE_PAGE_MANAGER = "page_manager_data";
  

  private SharedPreferences mSp = null;

  // private String mode = null;

  public MySysData(Context context, String mode) {
    if (context != null && mode != null) {
      // this.mode = mode;
      mSp = context.getSharedPreferences(mode, Context.MODE_PRIVATE);
      if (mode.compareTo(MODE_DEFAULT_DATA) == 0) {
        try {
          int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
          int sp_versionCode = mSp.getInt(VERSION_CODE, 0);
          if (versionCode != sp_versionCode) {
            mSp.edit().clear().commit();
            mSp.edit().putInt(VERSION_CODE, versionCode).commit();
          }
        } catch (NameNotFoundException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
  }

  public boolean clear() {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().clear().commit();
    }
    Log.info("clear() return " + ret);
    return ret;
  }

  public boolean putInt(String key, int value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putInt(key, value).commit();
    }
    Log.info("putInt() return " + ret);
    return ret;
  }

  public boolean putLong(String key, long value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putLong(key, value).commit();
    }
    Log.info("putLong() return " + ret);
    return ret;
  }

  public boolean putDouble(String key, Double value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putString(key, Double.toString(value)).commit();
    }
    Log.info("putDouble() return " + ret);
    return ret;
  }

  public boolean putFloat(String key, float value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putFloat(key, value).commit();
    }
    Log.info("putFloat() return " + ret);
    return ret;
  }

  public boolean putString(String key, String value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putString(key, value).commit();
    }
    Log.info("putString() return " + ret);
    return ret;
  }

  public boolean putBoolean(String key, boolean value) {
    boolean ret = false;
    if (mSp != null) {
      ret = mSp.edit().putBoolean(key, value).commit();
    }
    Log.info("putBoolean() return " + ret);
    return ret;
  }

  public String getString(String key, String defaultValue) {
    String ret = defaultValue;
    if (mSp != null) {
      ret = mSp.getString(key, defaultValue);
    }
    Log.info("getString() return " + ret);
    return ret;
  }

  public int getInt(String key, int defaultValue) {
    int ret = defaultValue;
    if (mSp != null) {
      ret = mSp.getInt(key, defaultValue);
    }
    Log.info("getInt() return " + ret);
    return ret;
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    boolean ret = defaultValue;
    if (mSp != null) {
      ret = mSp.getBoolean(key, defaultValue);
    }
    Log.info("getBoolean() return " + ret);
    return ret;
  }

  public long getLong(String key, long defaultValue) {
    long ret = defaultValue;
    if (mSp != null) {
      ret = mSp.getLong(key, defaultValue);
    }
    Log.info("getLong() return " + ret);
    return ret;
  }

  public double getDouble(String key, double defaultValue) {
    double ret = defaultValue;
    if (mSp != null) {
      ret = Double.parseDouble(mSp.getString(key, Double.toString(defaultValue)));
    }
    Log.info("getDouble() return " + ret);
    return ret;
  }

  public float getFloat(String key, float defaultValue) {
    float ret = defaultValue;
    if (mSp != null) {
      ret = mSp.getFloat(key, defaultValue);
    }
    Log.info("getFloat() return " + ret);
    return ret;
  }

}
