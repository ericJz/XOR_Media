package com.zw.player.playerclient;

import android.app.Fragment;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.ActivityPageManager.IFragment;
import com.xormedia.mylib.MyApplication;
import com.zw.player.playerclient.service.DatabaseHelper;
import com.zw.player.playerclient.service.PlayerServerList;
import com.zw.player.playerclient.service.SmbServerList;
import com.zw.player.playerclient.service.SongsList;

public class PlayerClientApplication extends MyApplication {
  private static MulticastLock multicastLock;

  @Override
  public void onCreate() {
    super.onCreate();
    new DatabaseHelper(getApplicationContext());
    new PlayerServerList(getApplicationContext());
    new SmbServerList(getApplicationContext());
    new SongsList();
    registerFragment();
    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    multicastLock = wifiManager
        .createMulticastLock(getPackageName());
    multicastLock.acquire();
  }

  @Override
  public void quitApp() {
    PlayerServerList.close();
    if (multicastLock != null) {
      multicastLock.release();
    }
    System.exit(0);
  }

  private void registerFragment() {
    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new PlayServerGridPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return PlayServerGridPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });
    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new PlayListPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return PlayListPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });
    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new PlayServerSettingsPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return PlayServerSettingsPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });

    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new ModifyServerPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return ModifyServerPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });

    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new SmbFileListPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return SmbFileListPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });

    ActivityPageManager.registerMyFragment(new IFragment() {
      public Fragment getFragment() {
        return new SongsListPage();
      }

      public int getDrawLayoutId() {
        return R.id.mainFrameLayout;
      }

      public String getFragmentClassName() {
        return SongsListPage.class.getName();
      }

      public String getDrawLayoutName() {
        return "R.id.mainFrameLayout";
      }

      public String getActivityClassName() {
        return MainActivity.class.getName();
      }
    });
  }

  @Override
  public void setDefaultSettingValue() {
  }

  @Override
  public void setLibPermission() {
  }

}
