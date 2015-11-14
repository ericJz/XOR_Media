package com.xormedia.mylib;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.app.Fragment;

public class ActivityPageManager {
  private static Logger Log = Logger.getLogger(ActivityPageManager.class);

  public static interface IFragment {
    public String getFragmentClassName();

    public Fragment getFragment();

    public int getDrawLayoutId();

    public String getDrawLayoutName();

    public String getActivityClassName();
  }

  private static ArrayList<IFragment> mRegisterFragments;
  private static ArrayList<SingleActivityPageManager> mSingleActivityPageManagers;

  public ActivityPageManager() {
    if (mRegisterFragments == null) {
      mRegisterFragments = new ArrayList<IFragment>();
    }
    if (mSingleActivityPageManagers == null) {
      mSingleActivityPageManagers = new ArrayList<SingleActivityPageManager>();
    }
  }

  public static void addSingleActivityPageManager(SingleActivityPageManager _obj) {
    Log.info("addSingleActivityPageManager(" + _obj != null ? _obj.mIdentificationName : "null" + ")***Enter!");
    if (mSingleActivityPageManagers == null) {
      mSingleActivityPageManagers = new ArrayList<SingleActivityPageManager>();
    }
    if (_obj != null && _obj.mIdentificationName != null && _obj.mIdentificationName.length() > 0) {
      synchronized (mSingleActivityPageManagers) {
        boolean find = false;
        int index = 0;
        for (int i = 0; i < mSingleActivityPageManagers.size(); i++) {
          if (mSingleActivityPageManagers.get(i).mIdentificationName != null &&
              _obj.mIdentificationName != null &&
              mSingleActivityPageManagers.get(i).mIdentificationName.compareTo(_obj.mIdentificationName) == 0) {
            find = true;
            index = i;
            break;
          }
        }
        if (find == false) {
          mSingleActivityPageManagers.add(_obj);
          Log.info("addSingleActivityPageManager add " + _obj.mIdentificationName + " sccess! mSingleActivityPageManagers.size()="
              + mSingleActivityPageManagers.size());
        } else {
          mSingleActivityPageManagers.set(index, _obj);
          Log.info("addSingleActivityPageManager replace " + _obj.mIdentificationName + " sccess! mSingleActivityPageManagers.size()="
              + mSingleActivityPageManagers.size());
        }
      }
    }
    Log.info("addSingleActivityPageManager(" + _obj != null ? _obj.mIdentificationName : "null" + ")***Leave!");
  }

  public static void deleteSingleActivityPageManager(SingleActivityPageManager _obj) {
    Log.info("deleteSingleActivityPageManager(" + _obj != null ? _obj.mIdentificationName : "null" + ")***Enter!");
    if (mSingleActivityPageManagers == null) {
      mSingleActivityPageManagers = new ArrayList<SingleActivityPageManager>();
    }
    if (_obj != null) {
      synchronized (mSingleActivityPageManagers) {
        for (int i = 0; i < mSingleActivityPageManagers.size(); i++) {
          if (mSingleActivityPageManagers.get(i) == _obj) {
            mSingleActivityPageManagers.remove(i);
            Log.info("deleteSingleActivityPageManager delete " + _obj.mIdentificationName + " sccess! mSingleActivityPageManagers.size()="
                + mSingleActivityPageManagers.size());
            break;
          }
        }
      }
    }
    Log.info("deleteSingleActivityPageManager(" + _obj != null ? _obj.mIdentificationName : "null" + ")***Leave!");
  }

  public static SingleActivityPageManager getSingleActivityPageManagerByName(String identificationName) {
    Log.info("getSingleActivityPageManagerByName(" + identificationName + ")***Enter!");
    SingleActivityPageManager ret = null;
    if (mSingleActivityPageManagers == null) {
      mSingleActivityPageManagers = new ArrayList<SingleActivityPageManager>();
    }
    synchronized (mSingleActivityPageManagers) {
      for (int i = 0; i < mSingleActivityPageManagers.size(); i++) {
        if (mSingleActivityPageManagers.get(i).mIdentificationName != null &&
            identificationName != null &&
            mSingleActivityPageManagers.get(i).mIdentificationName.compareTo(identificationName) == 0) {
          ret = mSingleActivityPageManagers.get(i);
          break;
        }
      }
    }
    Log.info("getSingleActivityPageManagerByName(" + identificationName + ")***Leave!return " + (ret != null ? ret.mIdentificationName : "null"));
    return ret;
  }

  public static void registerMyFragment(IFragment _myFragment) {
    Log.info("registerMyFragment(" + _myFragment != null ? _myFragment.getFragmentClassName() : "null" + ")***Enter!");
    if (mRegisterFragments == null) {
      mRegisterFragments = new ArrayList<IFragment>();
    }
    if (_myFragment != null) {
      synchronized (mRegisterFragments) {
        boolean find = false;
        int index = 0;
        for (int i = 0; i < mRegisterFragments.size(); i++) {
          if (mRegisterFragments.get(i).getFragmentClassName() != null &&
              _myFragment.getFragmentClassName() != null &&
              mRegisterFragments.get(i).getFragmentClassName().compareTo(_myFragment.getFragmentClassName()) == 0 &&
              mRegisterFragments.get(i).getDrawLayoutName() != null &&
              _myFragment.getDrawLayoutName() != null &&
              mRegisterFragments.get(i).getDrawLayoutName().compareTo(_myFragment.getDrawLayoutName()) == 0 &&
              mRegisterFragments.get(i).getActivityClassName() != null &&
              _myFragment.getActivityClassName() != null &&
              mRegisterFragments.get(i).getActivityClassName().compareTo(_myFragment.getActivityClassName()) == 0) {
            find = true;
            index = i;
            break;
          }
        }
        if (find == false) {
          mRegisterFragments.add(_myFragment);
          Log.info("registerMyFragment add getFragmentClassName=" + _myFragment.getActivityClassName() + ";getDrawLayoutName="
              + _myFragment.getDrawLayoutName()
              + ";getActivityClassName=" + _myFragment.getActivityClassName() + " is sccess!");
        } else {
          mRegisterFragments.set(index, _myFragment);
          Log.info("registerMyFragment replace getFragmentClassName=" + _myFragment.getActivityClassName() + ";getDrawLayoutName="
              + _myFragment.getDrawLayoutName()
              + ";getActivityClassName=" + _myFragment.getActivityClassName() + " is sccess!");
        }
      }
    }
    Log.info("registerMyFragment(" + _myFragment != null ? _myFragment.getFragmentClassName() : "null" + ")***Leave!");
  }

  public static IFragment getIFragment(String activityClassName, String fragmentClassName, String drawLayoutName) {
    Log.info("getIFragment(" + activityClassName + "," + fragmentClassName + "," + drawLayoutName + ")***Enter!");
    IFragment ret = null;
    if (mRegisterFragments == null) {
      mRegisterFragments = new ArrayList<IFragment>();
    }
    if (activityClassName != null && fragmentClassName != null && drawLayoutName != null) {
      synchronized (mRegisterFragments) {
        for (int i = 0; i < mRegisterFragments.size(); i++) {
          if (mRegisterFragments.get(i).getFragmentClassName() != null &&
              fragmentClassName != null &&
              mRegisterFragments.get(i).getFragmentClassName().compareTo(fragmentClassName) == 0 &&
              mRegisterFragments.get(i).getDrawLayoutName() != null &&
              drawLayoutName != null &&
              mRegisterFragments.get(i).getDrawLayoutName().compareTo(drawLayoutName) == 0 &&
              mRegisterFragments.get(i).getActivityClassName() != null &&
              activityClassName != null &&
              mRegisterFragments.get(i).getActivityClassName().compareTo(activityClassName) == 0) {
            ret = mRegisterFragments.get(i);
            break;
          }
        }
      }
    }
    Log.info("getIFragment(" + activityClassName + "," + fragmentClassName + "," + drawLayoutName + ")***Leave!return is " + ret != null ? ret
        .getFragmentClassName() : "null");
    return ret;
  }
}
