package com.xormedia.mylib;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.xormedia.mylib.ActivityPageManager.IFragment;

public class SingleActivityPage {
  private static Logger Log = Logger.getLogger(SingleActivityPage.class);
  private static final String ATTR_FRAGMENT_LIST = "fragmentList";
  private static final String ATTR_FRAGMENT_CLASS_NAME = "fragmentClassName";
  private static final String ATTR_DRAW_LAYOUT_NAME = "drawLayoutName";
  private static final String ATTR_PAGE_MANAGER_NAME = "pageManageName";
  private static final String ATTR_PAGE_PARAMETER = "pageParameter";
  private static final String ATTR_IS_BACK = "isBack";
  private static final String ATTR_IS_HOME_PAGE = "isHomePage";
  private static final String ATTR_PAGE_NAME = "pageName";

  private String mPageName = null;
  protected ArrayList<MyFragment> mMyFragments = new ArrayList<MyFragment>();
  private String mPageManagerName = null;
  private JSONObject mParameter = null;
  private boolean isBack = true;
  private boolean isHomePage = false;
  protected boolean isLinkToPageManager = false;
  protected boolean needCommit = true;

  protected static class MyFragment {
    public String mFragmentClassName = null;
    public Fragment mFragment = null;
    public String mDrawLayoutName = null;
    public int mDrawLayoutId = 0;

    public MyFragment(String _fragmentClassName, String _drawLayoutName) {
      mFragmentClassName = _fragmentClassName;
      mDrawLayoutName = _drawLayoutName;
    }
  }

  public SingleActivityPage(JSONObject obj) {
    isLinkToPageManager = true;
    if (obj != null) {
      try {
        if (obj.has(ATTR_FRAGMENT_LIST) == true && obj.isNull(ATTR_FRAGMENT_LIST) == false) {
          JSONArray objs = obj.getJSONArray(ATTR_FRAGMENT_LIST);
          if (objs != null) {
            for (int i = 0; i < objs.length(); i++) {
              if (objs.getJSONObject(i).has(ATTR_FRAGMENT_CLASS_NAME) == true
                  && objs.getJSONObject(i).isNull(ATTR_FRAGMENT_CLASS_NAME) == false
                  && objs.getJSONObject(i).has(ATTR_DRAW_LAYOUT_NAME) == true
                  && objs.getJSONObject(i).isNull(ATTR_DRAW_LAYOUT_NAME) == false) {
                mMyFragments.add(new MyFragment(objs.getJSONObject(i).getString(ATTR_FRAGMENT_CLASS_NAME),
                    objs.getJSONObject(i).getString(ATTR_DRAW_LAYOUT_NAME)));
              }
            }
          }
        }
        if (obj.has(ATTR_PAGE_MANAGER_NAME) == true && obj.isNull(ATTR_PAGE_MANAGER_NAME) == false) {
          mPageManagerName = obj.getString(ATTR_PAGE_MANAGER_NAME);
        }
        if (obj.has(ATTR_PAGE_PARAMETER) == true && obj.isNull(ATTR_PAGE_PARAMETER) == false) {
          mParameter = obj.getJSONObject(ATTR_PAGE_PARAMETER);
        }
        if (obj.has(ATTR_IS_BACK) == true && obj.isNull(ATTR_IS_BACK) == false) {
          isBack = obj.getBoolean(ATTR_IS_BACK);
        }
        if (obj.has(ATTR_IS_HOME_PAGE) == true && obj.isNull(ATTR_IS_HOME_PAGE) == false) {
          isHomePage = obj.getBoolean(ATTR_IS_HOME_PAGE);
        }
        if (obj.has(ATTR_PAGE_NAME) == true && obj.isNull(ATTR_PAGE_NAME) == false) {
          mPageName = obj.getString(ATTR_PAGE_NAME);
        }
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  public SingleActivityPage(String SingleActivityPageManagerName, String pageName) {
    mPageManagerName = SingleActivityPageManagerName;
    mPageName = pageName;
  }

  public JSONObject toJSONObject() {
    JSONObject ret = new JSONObject();
    try {
      if (mMyFragments != null && mMyFragments.size() > 0) {
        JSONArray objs = new JSONArray();
        for (int i = 0; i < mMyFragments.size(); i++) {
          JSONObject obj = new JSONObject();
          obj.put(ATTR_FRAGMENT_CLASS_NAME, mMyFragments.get(i).mFragmentClassName);
          obj.put(ATTR_DRAW_LAYOUT_NAME, mMyFragments.get(i).mDrawLayoutName);
          objs.put(obj);
        }
        ret.put(ATTR_FRAGMENT_LIST, objs);
      }
      if (mPageManagerName != null) {
        ret.put(ATTR_PAGE_MANAGER_NAME, mPageManagerName);
      }
      if (mParameter != null) {
        ret.put(ATTR_PAGE_PARAMETER, mParameter);
      }
      if (mPageName != null) {
        ret.put(ATTR_PAGE_NAME, mPageName);
      }
      ret.put(ATTR_IS_BACK, isBack);
      ret.put(ATTR_IS_HOME_PAGE, isHomePage);
    } catch (JSONException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  private void setPageData() {
    if (mPageManagerName != null && isLinkToPageManager == true) {
      SingleActivityPageManager obj = ActivityPageManager.getSingleActivityPageManagerByName(mPageManagerName);
      if (obj != null) {
        obj.setPageData();
      }
    }
  }

  public void setFragment(String _fragmentClassName, String _drawLayoutName) {
    if (_fragmentClassName != null && _fragmentClassName.length() > 0
        && _drawLayoutName != null && _drawLayoutName.length() > 0) {
      boolean found = false;
      for (int i = 0; i < mMyFragments.size(); i++) {
        if (mMyFragments.get(i).mFragmentClassName.compareTo(_fragmentClassName) == 0
            && mMyFragments.get(i).mDrawLayoutName.compareTo(_drawLayoutName) == 0) {
          found = true;
          break;
        }
      }
      if (found == false) {
        mMyFragments.add(new MyFragment(_fragmentClassName, _drawLayoutName));
        setPageData();
      }
    }
  }

  public Fragment getFragment(String _fragmentClassName, String _drawLayoutName) {
    Fragment ret = null;
    if (_fragmentClassName != null && _fragmentClassName.length() > 0
        && _drawLayoutName != null && _drawLayoutName.length() > 0) {
      for (int i = 0; i < mMyFragments.size(); i++) {
        if (mMyFragments.get(i).mFragmentClassName.compareTo(_fragmentClassName) == 0
            && mMyFragments.get(i).mDrawLayoutName.compareTo(_drawLayoutName) == 0) {
          ret = mMyFragments.get(i).mFragment;
          break;
        }
      }
    }
    return ret;
  }

  public void setPageParameter(JSONObject obj) {
    if (obj != null) {
      try {
        mParameter = new JSONObject(obj.toString());
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    else {
      mParameter = null;
    }
    setPageData();
  }

  public JSONObject getPageParameter() {
    JSONObject ret = null;
    if (mParameter != null) {
      try {
        ret = new JSONObject(mParameter.toString());
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public void setIsBack(boolean _isBack) {
    isBack = _isBack;
    setPageData();
  }

  public boolean getIsBack() {
    return isBack;
  }

  public void setIsHomePage(boolean _isHomePage) {
    isHomePage = _isHomePage;
    setPageData();
  }

  public boolean getIsHomePage() {
    return isHomePage;
  }

  public String getPageName() {
    return mPageName;
  }

  public boolean gotoPage() {
    Log.info("gotoPage(" + mPageName + ")***Enter!");
    boolean ret = false;
    SingleActivityPageManager obj = ActivityPageManager.getSingleActivityPageManagerByName(mPageManagerName);
    if (obj != null) {
      synchronized (obj.lastOperationTime) {
        if ((obj.lastOperationTime + obj.sleepTime) < System.currentTimeMillis()) {
          if (mMyFragments.size() > 0) {
            if (isHomePage == true) {
              obj.clearAllPage();
            }
            if (obj.mPageList.size() > 0) {
              boolean found = false;
              int i = 0;
              while (i < obj.mPageList.size()) {
                if (obj.mPageList.get(i).mPageName.compareTo(mPageName) == 0) {
                  found = true;
                }
                if (found == true) {
                  obj.mPageList.remove(i);
                } else {
                  i++;
                }
              }
            }
            needCommit = true;
            obj.push(this);
            commit();
          }
          obj.lastOperationTime = System.currentTimeMillis();
        }
      }
    }
    return ret;
  }

  public boolean open() {
    Log.info("open(" + mPageName + ")***Enter!");
    boolean ret = false;
    SingleActivityPageManager obj = ActivityPageManager.getSingleActivityPageManagerByName(mPageManagerName);
    if (obj != null) {
      synchronized (obj.lastOperationTime) {
        if ((obj.lastOperationTime + obj.sleepTime) < System.currentTimeMillis()) {
          if (mMyFragments.size() > 0) {
            if (isHomePage == true) {
              obj.clearAllPage();
            }
            needCommit = true;
            obj.push(this);
            commit();
          }
          obj.lastOperationTime = System.currentTimeMillis();
        }
      }
    }
    return ret;
  }

  protected void _back() {
    SingleActivityPageManager obj = ActivityPageManager.getSingleActivityPageManagerByName(mPageManagerName);
    if (obj != null) {
      synchronized (obj.lastOperationTime) {
        if ((obj.lastOperationTime + obj.sleepTime) < System.currentTimeMillis()) {
          if (mMyFragments.size() > 0) {
            needCommit = true;
            commit();
          }
          obj.lastOperationTime = System.currentTimeMillis();
        }
      }
    }
  }

  public synchronized boolean commit() {
    boolean ret = false;
    if (needCommit == true && mPageManagerName != null && mMyFragments != null && mMyFragments.size() > 0) {
      SingleActivityPageManager obj = ActivityPageManager.getSingleActivityPageManagerByName(mPageManagerName);
      if (obj != null) {
        if (obj.mWeakActivity != null && obj.mActivityClassName != null) {
          Activity activity = obj.mWeakActivity.get();
          if (activity != null) {
            if (obj.isAppOnForeground(activity.getPackageName(), true) == true) {
              FragmentManager fm = activity.getFragmentManager();
              FragmentTransaction ft = fm.beginTransaction();
              ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
              boolean replace = false;
              for (int i = 0; i < mMyFragments.size(); i++) {
                IFragment fragment = ActivityPageManager.getIFragment(obj.mActivityClassName,
                    mMyFragments.get(i).mFragmentClassName, mMyFragments.get(i).mDrawLayoutName);
                if (fragment != null) {
                  mMyFragments.get(i).mFragment = fragment.getFragment();
                  mMyFragments.get(i).mDrawLayoutId = fragment.getDrawLayoutId();
                  if (mMyFragments.get(i).mFragment != null && mMyFragments.get(i).mDrawLayoutId != 0) {
                    ft.replace(mMyFragments.get(i).mDrawLayoutId, mMyFragments.get(i).mFragment);
                    replace = true;
                  }
                }
              }
              if (replace == true) {
                ft.commitAllowingStateLoss();
              }
              needCommit = false;
              ret = true;
            }
          }
        }
      }
    }
    return ret;
  }
}
