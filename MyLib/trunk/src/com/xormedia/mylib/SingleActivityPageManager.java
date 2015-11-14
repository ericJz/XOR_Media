package com.xormedia.mylib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class SingleActivityPageManager {
  private static Logger Log = Logger.getLogger(SingleActivityPageManager.class);
  protected boolean noPageIsExistApp = false;
  protected WeakReference<Activity> mWeakActivity = null;
  protected IApplication mApp = null;
  protected Context mContext = null;
  protected String mActivityClassName = null;
  private static MySysData mFragmentaData;
  public String mIdentificationName = null;
  protected Long lastOperationTime = 0l;
  protected long sleepTime = 300;

  protected final ArrayList<SingleActivityPage> mPageList = new ArrayList<SingleActivityPage>();

  /**
   * 
   * @param _context
   *          当前上下文
   * @param _identificationName
   *          标识此对象的名称
   * @param _activity
   *          此页面管理运行在的Activity对象
   * @param _activityClassName
   *          此页面管理运行在的Activity对象的类名
   * @param useHistroy
   *          是否使用历史页面。
   */

  public SingleActivityPageManager(Context _context, String _identificationName, Activity _activity, String _activityClassName, boolean useHistroy) {
    Log.info("SingleActivityPageManager(" + _context + "," + _identificationName + "," + _activity
        + "," + _activityClassName + "," + useHistroy + ")***Enter!");
    if (_activity != null && _context != null && _activityClassName != null && _activityClassName.length() > 0
        && _identificationName != null && _identificationName.length() > 0) {
      mContext = _context;
      mWeakActivity = new WeakReference<Activity>(_activity);
      mActivityClassName = _activityClassName;
      mIdentificationName = _identificationName;
      if (mFragmentaData == null) {
        mFragmentaData = new MySysData(mContext, SingleActivityPageManager.class.getName());
      }
      if (useHistroy == true) {
        String myFragmentaData = mFragmentaData.getString(mActivityClassName, null);
        if (myFragmentaData != null && myFragmentaData.length() > 0) {
          try {
            JSONArray tmp = new JSONArray(myFragmentaData);
            if (tmp != null && tmp.length() > 0) {
              for (int i = 0; i < tmp.length(); i++) {
                mPageList.add(new SingleActivityPage(tmp.getJSONObject(i)));
              }
            }
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
      else {
        setPageData();
      }
      ActivityPageManager.addSingleActivityPageManager(this);
    }
    Log.info("SingleActivityPageManager(" + _context + "," + _identificationName + "," + _activity
        + "," + _activityClassName + "," + useHistroy + ")***Leave!");
  }

  @Override
  protected void finalize() throws Throwable {
    if (mPageList != null) {
      mPageList.clear();
    }
    super.finalize();
  }

  public void setNoPageIsExistApp(boolean _noPageIsExistApp, IApplication _app) {
    Log.info("setNoPageIsExistApp(" + _noPageIsExistApp + "," + _app + ")***Enter!");
    noPageIsExistApp = _noPageIsExistApp;
    mApp = _app;
    Log.info("setNoPageIsExistApp(" + _noPageIsExistApp + "," + _app + ")***Leave!");
  }

  protected void setPageData() {
    if (mFragmentaData != null && mActivityClassName != null && mActivityClassName.length() > 0) {
      JSONArray arry = new JSONArray();
      for (int i = 0; i < mPageList.size(); i++) {
        arry.put(mPageList.get(i).toJSONObject());
      }
      mFragmentaData.putString(mActivityClassName, arry.toString());
    }
  }

  public void back() {
    Log.info("back(" + mIdentificationName + ")***Enter!mPageList.size()=" + mPageList.size());
    SingleActivityPage page = null;
    if (mPageList.size() > 1) {
      int i = mPageList.size() - 2;
      while (i >= 0) {
        if (mPageList.get(i).getIsBack() == true) {
          page = mPageList.get(i);
          break;
        }
        else {
          mPageList.get(i).isLinkToPageManager = false;
          for (int j = 0; j < mPageList.get(i).mMyFragments.size(); j++) {
            mPageList.get(i).mMyFragments.get(j).mFragment = null;
          }
          mPageList.remove(i);
          i--;
        }
      }
    }
    if (page != null) {
      mPageList.get(mPageList.size() - 1).isLinkToPageManager = false;
      for (int j = 0; j < mPageList.get(mPageList.size() - 1).mMyFragments.size(); j++) {
        mPageList.get(mPageList.size() - 1).mMyFragments.get(j).mFragment = null;
      }
      mPageList.remove(mPageList.size() - 1);
      page._back();
      setPageData();
    }
    else {
      setPageData();
      for (int i = 0; i < mPageList.size(); i++) {
        mPageList.get(i).isLinkToPageManager = false;
        for (int j = 0; j < mPageList.get(i).mMyFragments.size(); j++) {
          mPageList.get(i).mMyFragments.get(j).mFragment = null;
        }
      }
      mPageList.clear();
      if (mWeakActivity != null) {
        Activity activity = mWeakActivity.get();
        if (activity != null) {
          Log.info("back(" + mIdentificationName + ")***activity.finish()");
          activity.finish();
        }
        mWeakActivity = null;
      }
      ActivityPageManager.deleteSingleActivityPageManager(this);
      if (noPageIsExistApp == true) {
        if (mApp != null) {
          Log.info("back(" + mIdentificationName + ")***mApp.quitApp()");
          mApp.quitApp();
        }
        else {
          Log.info("back(" + mIdentificationName + ")***System.exit(0)");
          System.exit(0);
        }
      }
    }
    Log.info("back(" + mIdentificationName + ")***Leave!mPageList.size()=" + mPageList.size());
  }

  protected boolean push(SingleActivityPage page) {
    Log.info("push(" + mIdentificationName + ")***Enter!mPageList.size()=" + mPageList.size() + ";pageName=" + page.getPageName());
    boolean ret = false;
    if (page != null) {
      int i = 0;
      while (i < mPageList.size()) {
        if (mPageList.get(i).getIsBack() == false) {
          mPageList.remove(i);
        }
        else {
          for (int j = 0; j < mPageList.get(i).mMyFragments.size(); j++) {
            mPageList.get(i).mMyFragments.get(j).mFragment = null;
          }
          i++;
        }
      }
      mPageList.add(page);
      page.isLinkToPageManager = true;
      ret = true;
      setPageData();
    }
    Log.info("push(" + mIdentificationName + ")***Leave!mPageList.size()=" + mPageList.size() + ";pageName=" + page.getPageName());
    return ret;
  }

  public void clearAllPage() {
    Log.info("clearAllPage(" + mIdentificationName + ")***Enter!mPageList.size()=" + mPageList.size());
    if (mPageList != null) {
      mPageList.clear();
    }
    Log.info("clearAllPage(" + mIdentificationName + ")***Leave!mPageList.size()=" + mPageList.size());
  }

  public SingleActivityPage getCurrentPageLink() {
    Log.info("getCurrentPageLink(" + mIdentificationName + ")***Enter!mPageList.size()=" + mPageList.size());
    SingleActivityPage ret = null;
    if (mPageList.size() > 0) {
      ret = mPageList.get(mPageList.size() - 1);
    }
    Log.info("getCurrentPageLink(" + mIdentificationName + ")***Leave!mPageList.size()=" + mPageList.size() + ";pageName="
        + (ret != null ? ret.getPageName() : "null"));
    return ret;
  }

  public SingleActivityPage getPrevPageLink() {
    Log.info("getPrevPageLink(" + mIdentificationName + ")***Enter!mPageList.size()=" + mPageList.size());
    SingleActivityPage ret = null;
    if (mPageList.size() > 1) {
      ret = mPageList.get(mPageList.size() - 2);
    }
    Log.info("getPrevPageLink(" + mIdentificationName + ")***Leave!mPageList.size()=" + mPageList.size() + ";pageName="
        + (ret != null ? ret.getPageName() : "null"));
    return ret;
  }

  public boolean isAppOnForeground(String packageName, boolean isMoveTaskToFront) {
    boolean ret = false;
    // String packageName = mContext.getPackageName();
    Log.info("isAppOnForeground:packageName=" + packageName);
    ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses != null) {
      for (RunningAppProcessInfo appProcess : appProcesses) {
        if (appProcess.processName.equals(packageName)) {
          Log.info("isAppOnForeground:appProcess.processName=" + appProcess.processName);
          Log.info("isAppOnForeground:appProcess.importance=" + appProcess.importance);
          if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
            ret = true;
          } else if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND
              || appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
              || appProcess.importance == RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE
              || appProcess.importance == RunningAppProcessInfo.IMPORTANCE_SERVICE) {
            if (isMoveTaskToFront == false) {
              ret = true;
            } else {
              List<RunningTaskInfo> tasks = activityManager.getRunningTasks(50);
              for (RunningTaskInfo task : tasks) {
                if (task.baseActivity.getPackageName().compareTo(packageName) == 0) {
                  activityManager.moveTaskToFront(task.id, 0);
                  ret = true;
                  break;
                }
              }
            }
          }
          break;
        }
      }
    }
    return ret;
  }
}
