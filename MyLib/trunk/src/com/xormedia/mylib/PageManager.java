package com.xormedia.mylib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

public class PageManager {
  private static boolean noFragmentIsExistApp = true;
  private static Logger Log = Logger.getLogger(PageManager.class);
  private static boolean _screenIsON = true;

  public static boolean screenIsON() {
    if (mContext != null) {
      PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
      _screenIsON = pm.isScreenOn();
    }
    return _screenIsON;
  }

  private final BroadcastReceiver receiver = new BroadcastReceiver() {

    @Override
    public void onReceive(final Context context, final Intent intent) {
      String action = intent.getAction();
      Log.info("PageManager:receiver:onReceive() action = " + action);
      if (action.compareTo(Intent.ACTION_USER_PRESENT) == 0) {
        // if (action.compareTo(Intent.ACTION_SCREEN_ON) == 0) {
        _screenIsON = true;
        if (PageManager.getCurrentPageLink() != null) {
          PageManager.getCurrentPageLink().commit();
        }
      } else if (action.compareTo(Intent.ACTION_SCREEN_OFF) == 0) {
        _screenIsON = false;
      }
    }
  };

  public static Context mContext = null;
  public static IApplication mApp = null;
  public static ActivityManager activityManager = null;
  public static MySysData pageData = null;

  private void setContext(Context context) {
    if (context != null) {
      final IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      // filter.addAction(Intent.ACTION_SCREEN_ON);
      filter.addAction(Intent.ACTION_USER_PRESENT);
      context.registerReceiver(receiver, filter);
      activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

      mContext = context;
      pageData = new MySysData(context, MySysData.MODE_PAGE_MANAGER);
      String tmp1 = pageData.getString("pages", "");
      if (tmp1.length() > 0) {
        try {
          JSONArray tmp = new JSONArray(tmp1);
          if (tmp != null && tmp.length() > 0) {
            for (int i = 0; i < tmp.length(); i++) {
              mPageList.add(new MyPage(tmp.getJSONObject(i)));
            }
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      pageData.clear();
    }
  }

  public PageManager(Context context) {
    setContext(context);
  }

  public PageManager(Context context, IApplication app) {
    mApp = app;
    setContext(context);
  }

  public static void savePageManager() {
    if (mPageList != null && mPageList.size() > 0 && pageData != null) {
      JSONArray tmp = new JSONArray();
      for (int i = 0; i < mPageList.size(); i++) {
        tmp.put(mPageList.get(i).toJSONObject());
      }
      pageData.putString("pages", tmp.toString());
    }
  }

  // public static class myFragment extends Fragment {
  // public String name = null;
  // public Bundle args = null;
  //
  // public Fragment getFragment(Context context) {
  // return Fragment.instantiate(context, name, args);
  // }
  // }

  public static interface myFragment {
    public String getName();

    public Fragment getFragment();

    public int getDrawLayoutId();
  }

  public static ArrayList<myFragment> myFragments = new ArrayList<PageManager.myFragment>();

  public static void registerMyFragment(PageManager.myFragment _myFragment) {
    if (_myFragment != null) {
      boolean find = false;
      int index = 0;
      for (int i = 0; i < myFragments.size(); i++) {
        if (myFragments.get(i).getName().compareTo(_myFragment.getName()) == 0) {
          find = true;
          index = i;
        }
      }
      if (find == false) {
        myFragments.add(_myFragment);
      } else {
        myFragments.set(index, _myFragment);
      }
    }
  }

  public static boolean isAppOnForeground(String packageName, boolean isMoveTaskToFront) {
    boolean ret = false;
    // String packageName = mContext.getPackageName();
    Log.info("isAppOnForeground:packageName=" + packageName);
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

  public static void setNoFragmentIsExistApp(boolean bol) {
    noFragmentIsExistApp = bol;
  }

  public static boolean getNoFragmentIsExistApp() {
    return noFragmentIsExistApp;
  }

  public static final ArrayList<MyPage> mPageList = new ArrayList<PageManager.MyPage>();
  private static WeakReference<Activity> mActivity = null;
  private static String backIntentActionName = null;
  private static String backIntentPackageName = null;
  private static String currentIntentActionName = null;
  private static String ATTR_BACK_APPLICATION_ACTION = "backApplication";
  private static String ATTR_BACK_APPLICATION_PACKAGE_NAME = "backApplicationPackageName";
  private static String ATTR_PARAMETER = "parameter";

  public static JSONObject setActivity(Activity activity) {
    JSONObject ret = null;
    if (activity != null) {
      mActivity = new WeakReference<Activity>(activity);
      if (currentIntentActionName == null && activity.getIntent().getAction() != null
          && activity.getIntent().getAction().compareTo(Intent.ACTION_MAIN) != 0) {
        currentIntentActionName = activity.getIntent().getAction();
        Log.info("currentIntentActionName=" + activity.getIntent().getAction());
      }
      if (activity.getIntent() != null && activity.getIntent().getStringExtra(ATTR_BACK_APPLICATION_ACTION) != null) {
        backIntentActionName = activity.getIntent().getStringExtra(ATTR_BACK_APPLICATION_ACTION);
        Log.info("backIntentActionName=" + backIntentActionName);
      }
      if (activity.getIntent() != null && activity.getIntent().getStringExtra(ATTR_BACK_APPLICATION_PACKAGE_NAME) != null) {
        backIntentPackageName = activity.getIntent().getStringExtra(ATTR_BACK_APPLICATION_PACKAGE_NAME);
        Log.info("backIntentPackageName=" + backIntentPackageName);
      }
      if (activity.getIntent() != null && activity.getIntent().getStringExtra(ATTR_PARAMETER) != null) {
        String parameter = activity.getIntent().getStringExtra(ATTR_PARAMETER);
        Log.info("parameter=" + parameter);
        if (parameter != null && parameter.length() > 0) {
          try {
            ret = new JSONObject(parameter);
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
    }
    return ret;
  }

  public static JSONObject setActivity(Activity activity, MyPage firstPage) {
    JSONObject ret = null;
    if (activity != null) {
      ret = setActivity(activity);
      if (mPageList.size() > 0) {
        MyPage page = mPageList.get(mPageList.size() - 1);
        page.open();
      } else {
        if (firstPage != null)
          firstPage.open();
      }
    }
    return ret;
  }

  public static JSONObject setActivity(Activity activity, String intentActionName) {
    JSONObject ret = null;
    if (activity != null) {
      if (intentActionName != null) {
        currentIntentActionName = intentActionName;
      }
      ret = setActivity(activity);
    }
    return ret;
  }

  public static JSONObject setActivity(Activity activity, MyPage firstPage, String intentActionName) {
    JSONObject ret = null;
    if (activity != null) {
      if (intentActionName != null) {
        currentIntentActionName = intentActionName;
      }
      ret = setActivity(activity, firstPage);
    }
    return ret;
  }

  public static boolean clearPageList(JSONObject jObject) {
    boolean ret = false;
    if (PageManager.mActivity != null && PageManager.mActivity.get() != null) {
      Activity _activity = PageManager.mActivity.get();
      mPageList.clear();
      if (jObject != null) {
        Intent _intent = new Intent();
        _intent.putExtra(ATTR_PARAMETER, jObject.toString());
        _activity.setResult(Activity.RESULT_OK, _intent);
      } else {
        _activity.setResult(Activity.RESULT_CANCELED);
      }
      _activity.finish();
      PageManager.mActivity = null;
      if (noFragmentIsExistApp == true) {
        if (mApp != null) {
          mApp.quitApp();
        } else {
          System.exit(0);
        }
      }
      ret = true;
    }
    return ret;
  }

  public static int ACTIVITY_FOR_RESULT_CODE = 49523;

  public static JSONObject onActivityResult(int requestCode, int resultCode, Intent data) {
    JSONObject ret = null;
    if (requestCode == ACTIVITY_FOR_RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
      if (data.getStringExtra(ATTR_PARAMETER) != null && data.getStringExtra(ATTR_PARAMETER).length() > 0) {
        try {
          ret = new JSONObject(data.getStringExtra(ATTR_PARAMETER));
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
    return ret;
  }

  public static void openApplication(String action, JSONObject parameter, boolean needBackToCurrentApplication) {
    if (action != null && PageManager.mActivity != null && PageManager.mActivity.get() != null) {
      Activity _activity = PageManager.mActivity.get();
      // action = MediaStore.ACTION_IMAGE_CAPTURE;
      Intent _intent = new Intent(action);
      _intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      PackageManager pm = _activity.getPackageManager();
      List<ResolveInfo> list = pm.queryIntentActivities(_intent, 0);
      if (list.size() > 0) {
        if (needBackToCurrentApplication == true && currentIntentActionName != null) {
          _intent.putExtra(ATTR_BACK_APPLICATION_ACTION, currentIntentActionName);
          _intent.putExtra(ATTR_BACK_APPLICATION_PACKAGE_NAME, _activity.getPackageName());
        }
        if (parameter == null) {
          parameter = new JSONObject();
        }
        _intent.putExtra(ATTR_PARAMETER, parameter.toString());
        if (needBackToCurrentApplication == false) {
          _activity.startActivity(_intent);
          clearPageList(null);
        } else {
          _activity.startActivityForResult(_intent, ACTIVITY_FOR_RESULT_CODE);
        }
      }
    }
  }

  public static void backApplication(JSONObject parameter) {
    if (backIntentActionName != null && backIntentPackageName != null && PageManager.mActivity != null
        && PageManager.mActivity.get() != null) {
      Activity _activity = PageManager.mActivity.get();
      if (isAppOnForeground(backIntentPackageName, false) == true) {
        if (parameter == null) {
          parameter = new JSONObject();
        }
        clearPageList(parameter);
      } else {
        Intent _intent = new Intent(backIntentActionName);
        _intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PackageManager pm = _activity.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(_intent, 0);
        if (list.size() > 0) {
          if (parameter == null) {
            parameter = new JSONObject();
          }
          _intent.putExtra(ATTR_PARAMETER, parameter.toString());
          _activity.startActivity(_intent);
          clearPageList(null);
        }
      }
    } else {
      clearPageList(null);
    }
  }

  public static class MyPage {
    public static String ATTR_FRAGMENT_NAME = "fragment_name";
    public static String ATTR_DRAW_LAYOUT_ID = "draw_layout_id";
    public static String ATTR_IS_BACK = "is_back";
    public static String ATTR_USE_ANIMATIONS = "use_animations";
    public static String ATTR_JSON_PARAMETER = "json_parameter";

    public Fragment mFragment = null;
    public String fragmentName = null;
    public int drawLayoutId = 0;
    public boolean isBack = true;
    public boolean useAnimations = true;
    public Map<Object, Object> param = null;
    public JSONObject JSONParam = null;
    private boolean needCommit = true;
    private FragmentTransaction ft = null;

    public MyPage() {
    }

    public MyPage(JSONObject obj) {
      if (obj != null) {
        try {
          if (obj.has(ATTR_FRAGMENT_NAME) == true && obj.isNull(ATTR_FRAGMENT_NAME) == false) {
            fragmentName = obj.getString(ATTR_FRAGMENT_NAME);
          }
          // if (obj.has(ATTR_DRAW_LAYOUT_ID) == true) {
          // drawLayoutId = obj.getInt(ATTR_DRAW_LAYOUT_ID);
          // }
          if (obj.has(ATTR_IS_BACK) == true) {
            isBack = obj.getBoolean(ATTR_IS_BACK);
          }
          if (obj.has(ATTR_USE_ANIMATIONS) == true) {
            useAnimations = obj.getBoolean(ATTR_USE_ANIMATIONS);
          }
          if (obj.has(ATTR_JSON_PARAMETER) == true && obj.isNull(ATTR_JSON_PARAMETER) == false) {
            JSONParam = obj.getJSONObject(ATTR_JSON_PARAMETER);
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }

    public JSONObject toJSONObject() {
      JSONObject ret = new JSONObject();
      try {
        ret.put(ATTR_FRAGMENT_NAME, fragmentName);
        // ret.put(ATTR_DRAW_LAYOUT_ID, drawLayoutId);
        ret.put(ATTR_IS_BACK, isBack);
        ret.put(ATTR_USE_ANIMATIONS, useAnimations);
        ret.put(ATTR_JSON_PARAMETER, JSONParam);
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
      return ret;
    }

    public synchronized void open() {
      synchronized (lastOperationTime) {
        if ((lastOperationTime + sleepTime) < new Date().getTime()) {
          if (mFragment == null && fragmentName != null && PageManager.myFragments != null) {
            for (int i = 0; i < PageManager.myFragments.size(); i++) {
              if (PageManager.myFragments.get(i) != null && PageManager.myFragments.get(i).getName() != null
                  && PageManager.myFragments.get(i).getName().compareTo(fragmentName) == 0) {
                mFragment = PageManager.myFragments.get(i).getFragment();
                drawLayoutId = PageManager.myFragments.get(i).getDrawLayoutId();
                break;
              }
            }
          }
          if (mFragment != null && drawLayoutId != 0) {
            needCommit = true;
            commit();
            push(this);
          }
          lastOperationTime = new Date().getTime();
        }
      }
    }

    public synchronized void gotoPage() {
      synchronized (lastOperationTime) {
        if ((lastOperationTime + sleepTime) < new Date().getTime()) {
          if (mFragment == null && fragmentName != null && PageManager.myFragments != null) {
            for (int i = 0; i < PageManager.myFragments.size(); i++) {
              if (PageManager.myFragments.get(i) != null && PageManager.myFragments.get(i).getName() != null
                  && PageManager.myFragments.get(i).getName().compareTo(fragmentName) == 0) {
                mFragment = PageManager.myFragments.get(i).getFragment();
                drawLayoutId = PageManager.myFragments.get(i).getDrawLayoutId();
                break;
              }
            }
          }
          if (mFragment != null && drawLayoutId != 0) {
            needCommit = true;
            commit();
            if (mPageList.size() > 0) {
              boolean found = false;
              int i = 0;
              while (i < mPageList.size()) {
                if (mPageList.get(i).fragmentName.compareTo(fragmentName) == 0) {
                  found = true;
                }
                if (found == true) {
                  mPageList.remove(i);
                } else {
                  i++;
                }
              }
            }
            push(this);
          }
          lastOperationTime = new Date().getTime();
        }
      }
    }

    private boolean _back(final JSONObject parameter) {
      boolean ret = true;
      if (mFragment == null && fragmentName != null && PageManager.myFragments != null) {
        for (int i = 0; i < PageManager.myFragments.size(); i++) {
          if (PageManager.myFragments.get(i) != null && PageManager.myFragments.get(i).getName() != null
              && PageManager.myFragments.get(i).getName().compareTo(fragmentName) == 0) {
            mFragment = PageManager.myFragments.get(i).getFragment();
            drawLayoutId = PageManager.myFragments.get(i).getDrawLayoutId();
            break;
          }
        }
      }
      if (mFragment != null && drawLayoutId != 0) {
        if (parameter != null) {
          JSONParam = parameter;
        }
        needCommit = true;
        commit();
      }
      return ret;
    }

    public synchronized boolean commit() {
      boolean ret = false;
      boolean useCommitAllowingStateLoss = false;
      if (needCommit == true && PageManager.screenIsON() == true && PageManager.mActivity != null && PageManager.mActivity.get() != null) {
        if (PageManager.isAppOnForeground(PageManager.mActivity.get().getPackageName(), true) == false) {
          Intent ootStartIntent = new Intent(mContext, PageManager.mActivity.get().getClass());
          ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
              | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
          mContext.startActivity(ootStartIntent);
          useCommitAllowingStateLoss = true;
        }
        if (ft != null) {
          ft = null;
        }
        FragmentManager fm = PageManager.mActivity.get().getFragmentManager();
        ft = fm.beginTransaction();
        if (useAnimations == true) {
          // ft.setCustomAnimations(R.anim.objectleft_in,
          // R.anim.objectleft_out);
          ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        }

        ft.replace(drawLayoutId, mFragment);
        if (useCommitAllowingStateLoss == true) {
          Log.info("commitAllowingStateLoss");
          ft.commitAllowingStateLoss();
        }
        else {
          Log.info("commit");
          ft.commitAllowingStateLoss();
        }
        needCommit = false;
        ret = true;
      }
      return ret;
    }

    @Override
    protected void finalize() throws Throwable {
      mFragment = null;
      if (param != null) {
        param.clear();
      }
      param = null;
      super.finalize();
    }
  }

  public static MyPage getCurrentPageLink() {
    MyPage ret = null;
    if (mPageList.size() > 0) {
      ret = mPageList.get(mPageList.size() - 1);
    }
    return ret;
  }

  public static MyPage getBackPageLink() {
    MyPage ret = null;
    if (mPageList.size() > 1) {
      int i = mPageList.size() - 2;
      while (i >= 0) {
        if (mPageList.get(i).isBack == true) {
          ret = mPageList.get(i);
          break;
        }
        else {
          i--;
        }
      }
    }
    return ret;
  }

  public static boolean push(MyPage page) {
    boolean ret = false;
    if (page != null) {
      if (mPageList.size() > 0 && mPageList.get(mPageList.size() - 1).isBack == false) {
        mPageList.remove(mPageList.size() - 1);
      }
      for (int i = 0; i < mPageList.size(); i++) {
        mPageList.get(i).mFragment = null;
      }
      mPageList.add(page);
      ret = true;
    }
    return ret;
  }

  public static void back() {
    back(null);
  }

  private static Long lastOperationTime = 0l;
  private static long sleepTime = 300;

  public static void back(final JSONObject parameter) {
    synchronized (lastOperationTime) {
      if ((lastOperationTime + sleepTime) < new Date().getTime()) {
        MyPage page = null;
        if (mPageList.size() > 1) {
          int i = mPageList.size() - 2;
          while (i >= 0) {
            if (mPageList.get(i).isBack == true) {
              page = mPageList.get(i);
              break;
            }
            else {
              mPageList.remove(i);
              i--;
            }
          }
          if (page != null) {
            page._back(parameter);
            if (mPageList.get(mPageList.size() - 1).mFragment != null) {
              mPageList.get(mPageList.size() - 1).mFragment = null;
            }
            mPageList.set(mPageList.size() - 1, null);
            mPageList.remove(mPageList.size() - 1);
          }
          else {
            backApplication(parameter);
          }
        } else {
          backApplication(parameter);
        }
        lastOperationTime = new Date().getTime();
      } else {
        new Handler(Looper.getMainLooper()).postAtTime(new Runnable() {
          @Override
          public void run() {
            MyPage page = null;
            if (mPageList.size() > 1) {
              int i = mPageList.size() - 2;
              while (i >= 0) {
                if (mPageList.get(i).isBack == true) {
                  page = mPageList.get(i);
                  break;
                }
                else {
                  mPageList.remove(i);
                  i--;
                }
              }
              if (page != null) {
                page._back(parameter);
                if (mPageList.get(mPageList.size() - 1).mFragment != null) {
                  mPageList.get(mPageList.size() - 1).mFragment = null;
                }
                mPageList.set(mPageList.size() - 1, null);
                mPageList.remove(mPageList.size() - 1);
              }
              else {
                backApplication(parameter);
              }
            } else {
              backApplication(parameter);
            }
          }
        }, lastOperationTime + sleepTime);
        lastOperationTime += sleepTime + 100;
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    // mPageList.clear();
    super.finalize();
  }
}
