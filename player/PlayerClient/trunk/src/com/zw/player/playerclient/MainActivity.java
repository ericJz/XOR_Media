package com.zw.player.playerclient;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.IMainActivityInterface;
import com.xormedia.mylib.SingleActivityPage;
import com.xormedia.mylib.SingleActivityPageManager;
import com.zw.player.playerclient.service.PlayerServer;
import com.zw.player.playerclient.service.PlayerServerList;

public class MainActivity extends Activity implements IMainActivityInterface {
  private static Logger Log = Logger.getLogger(MainActivity.class);
  private InputMethodManager imm = null;
  // rotating loading
  private FrameLayout rotatingLoadingFrameLayout = null;
  private ImageView rotatingLoadingImageView = null;
  private Animation rotatingLoadingAnim = null;

  private String serverIPAddress = null;
  public PlayerServer playerServer = null;
  private RelativeLayout titleViewRoot_rl = null;
  private TextView pageTitleViewTitle_tv = null;
  private Switch pageTitleViewSwitch_s = null;
  private RelativeLayout tabViewRoot_rl = null;
  private RelativeLayout tabViewButton0_rl = null;
  private RelativeLayout tabViewButton1_rl = null;
  private RelativeLayout tabViewButton2_rl = null;
  private RelativeLayout tabViewButton3_rl = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.info("onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    // rotating loading
    rotatingLoadingFrameLayout = (FrameLayout) findViewById(R.id.rotatingLoadingFrameLayout);
    rotatingLoadingImageView = (ImageView) findViewById(R.id.rotatingLoadingImageView);
    rotatingLoadingAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotating_loading_anim);
    rotatingLoadingFrameLayout.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {

        return true;
      }
    });

    titleViewRoot_rl = (RelativeLayout) findViewById(R.id.titleView);
    pageTitleViewSwitch_s = (Switch) findViewById(R.id.pageTitleViewSwitch_s);
    pageTitleViewSwitch_s.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked == true && playerServer != null &&
            playerServer.isConnected() == false) {
          playerServer.connect();
          showRotatingLoadingLayout();
        } else if (isChecked == false && playerServer != null &&
            playerServer.isConnected() == true) {
          playerServer.shutdown();
          showRotatingLoadingLayout();
        }
      }
    });
    pageTitleViewTitle_tv = (TextView) findViewById(R.id.pageTitleViewTitle_tv);
    tabViewRoot_rl = (RelativeLayout) findViewById(R.id.tabView);
    tabViewButton0_rl = (RelativeLayout) findViewById(R.id.tabViewButton0_rl);
    tabViewButton0_rl.setClickable(true);
    tabViewButton0_rl.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        v.setClickable(false);
        if (tabViewButton0_rl.isSelected() == false) {
          openPlayListPage(serverIPAddress);
        }
        v.setClickable(true);
      }
    });
    tabViewButton1_rl = (RelativeLayout) findViewById(R.id.tabViewButton1_rl);
    tabViewButton1_rl.setClickable(true);
    tabViewButton1_rl.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        v.setClickable(false);
        if (tabViewButton1_rl.isSelected() == false) {
          openSongsListPage(serverIPAddress);
        }
        v.setClickable(true);
      }
    });
    tabViewButton2_rl = (RelativeLayout) findViewById(R.id.tabViewButton2_rl);
    tabViewButton2_rl.setClickable(true);
    tabViewButton2_rl.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        v.setClickable(false);
        if (tabViewButton2_rl.isSelected() == false) {
          openModifyServerPage(serverIPAddress);
        }
        v.setClickable(true);
      }
    });
    tabViewButton3_rl = (RelativeLayout) findViewById(R.id.tabViewButton3_rl);
    tabViewButton3_rl.setClickable(true);
    tabViewButton3_rl.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        v.setClickable(false);
        if (tabViewButton3_rl.isSelected() == false) {
          openPlayServerSettingsPage(serverIPAddress);
        }
        v.setClickable(true);
      }
    });

    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
      @Override
      public void run() {
        SingleActivityPageManager currPageManager = new SingleActivityPageManager(getApplicationContext(), MainActivity.class.getName(), MainActivity.this,
            MainActivity.class.getName(), false);
        currPageManager.clearAllPage();
        SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), PlayServerGridPage.class.getName());
        myPage.setFragment(PlayServerGridPage.class.getName(), "R.id.mainFrameLayout");
        myPage.setIsBack(true);
        myPage.setIsHomePage(true);
        myPage.setPageParameter(null);
        myPage.open();
      }
    }, 500);

  }

  private Handler connectServerHandler = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      if (playerServer != null && playerServer.isConnected() == true) {
        if (pageTitleViewSwitch_s != null) {
          pageTitleViewSwitch_s.setChecked(true);
        }
      } else {
        if (pageTitleViewSwitch_s != null) {
          pageTitleViewSwitch_s.setChecked(false);
        }
      }

      SingleActivityPageManager manager = ActivityPageManager.getSingleActivityPageManagerByName(MainActivity.class.getName());
      if (manager != null) {
        SingleActivityPage currMyPage = manager.getCurrentPageLink();
        if (currMyPage != null && currMyPage.getPageName() != null) {
          String pageName = currMyPage.getPageName();
          if (pageName != null && pageName.compareTo(PlayServerSettingsPage.class.getName()) == 0) {
            PlayServerSettingsPage curFragment = (PlayServerSettingsPage)
                currMyPage.getFragment(PlayServerSettingsPage.class.getName(), "R.id.mainFrameLayout");
            if (curFragment != null && curFragment.playerSettingsPortArrow_tv != null) {
              if (pageTitleViewSwitch_s.isChecked() == true) {
                curFragment.playerSettingsPortArrow_tv.setVisibility(View.GONE);
              } else {
                curFragment.playerSettingsPortArrow_tv.setVisibility(View.VISIBLE);
              }
            }
          } else if (pageName != null && pageName.compareTo(PlayListPage.class.getName()) == 0) {
            if (playerServer != null && playerServer.isConnected() == true) {
              playerServer.getPlayList();
            }
          }
        }
      }
      hiddenRotatingLoadingLayout();
      return false;
    }
  });

  public void showTitleView(String _serverIPAddress) {
    titleViewRoot_rl.setVisibility(View.VISIBLE);
    if (serverIPAddress == null || (_serverIPAddress != null && _serverIPAddress.length() > 0 && _serverIPAddress.compareTo(serverIPAddress) != 0)) {
      serverIPAddress = _serverIPAddress;
      for (int i = 0; i < PlayerServerList.list.size(); i++) {
        if (PlayerServerList.list.get(i).serverIPAddress.compareTo(serverIPAddress) == 0) {
          playerServer = PlayerServerList.list.get(i);
          playerServer.setConnectHandler(connectServerHandler);
          break;
        }
      }

      if (playerServer != null) {
        if (playerServer.serverName != null) {
          pageTitleViewTitle_tv.setText(playerServer.serverName);
        }
        if (playerServer.isConnected() == true) {
          pageTitleViewSwitch_s.setChecked(true);
        } else {
          pageTitleViewSwitch_s.setChecked(false);
        }
      }
    }
  }

  public void hideTitleView() {
    titleViewRoot_rl.setVisibility(View.GONE);
    pageTitleViewTitle_tv.setText(null);
    playerServer = null;
    serverIPAddress = null;
  }

  public void setTabViewButtonSelected(int selectIndex) {
    tabViewButton0_rl.setSelected(false);
    tabViewButton1_rl.setSelected(false);
    tabViewButton2_rl.setSelected(false);
    tabViewButton3_rl.setSelected(false);
    if (selectIndex == 0) {
      tabViewButton0_rl.setSelected(true);
    } else if (selectIndex == 1) {
      tabViewButton1_rl.setSelected(true);
    } else if (selectIndex == 2) {
      tabViewButton2_rl.setSelected(true);
    } else if (selectIndex == 3) {
      tabViewButton3_rl.setSelected(true);
    }
  }

  public void showTabView() {
    tabViewRoot_rl.setVisibility(View.VISIBLE);
  }

  public void hideTabView() {
    tabViewRoot_rl.setVisibility(View.GONE);
  }

  public void openPlayServerSettingsPage(String _serverIPAddress) {
    showTitleView(_serverIPAddress);
    showTabView();
    setTabViewButtonSelected(3);
    SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), PlayServerSettingsPage.class.getName());
    myPage.setFragment(PlayServerSettingsPage.class.getName(), "R.id.mainFrameLayout");
    myPage.setIsBack(false);
    myPage.setPageParameter(null);
    myPage.open();
  }

  public void openModifyServerPage(String _serverIPAddress) {
    showTitleView(_serverIPAddress);
    showTabView();
    setTabViewButtonSelected(2);
    SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), ModifyServerPage.class.getName());
    myPage.setFragment(ModifyServerPage.class.getName(), "R.id.mainFrameLayout");
    myPage.setIsBack(false);
    myPage.setPageParameter(null);
    myPage.open();
  }

  public void openSongsListPage(String _serverIPAddress) {
    showTitleView(_serverIPAddress);
    showTabView();
    setTabViewButtonSelected(1);
    SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), SongsListPage.class.getName());
    myPage.setFragment(SongsListPage.class.getName(), "R.id.mainFrameLayout");
    myPage.setIsBack(false);
    myPage.setPageParameter(null);
    myPage.open();
  }

  public void openPlayListPage(String _serverIPAddress) {
    showTitleView(_serverIPAddress);
    showTabView();
    setTabViewButtonSelected(0);
    SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), PlayListPage.class.getName());
    myPage.setFragment(PlayListPage.class.getName(), "R.id.mainFrameLayout");
    myPage.setIsBack(false);
    myPage.setPageParameter(null);
    myPage.open();
  }

  public void showRotatingLoadingLayout() {
    if (rotatingLoadingFrameLayout.getVisibility() == View.GONE) {
      rotatingLoadingFrameLayout.setVisibility(View.VISIBLE);
      rotatingLoadingImageView.clearAnimation();
      rotatingLoadingImageView.startAnimation(rotatingLoadingAnim);
    }
  }

  public void hiddenRotatingLoadingLayout() {
    if (rotatingLoadingFrameLayout.getVisibility() == View.VISIBLE) {
      rotatingLoadingFrameLayout.setVisibility(View.GONE);
      rotatingLoadingImageView.clearAnimation();
    }
  }

  @Override
  public boolean isShowRotatingLoadingLayout() {
    boolean ret = false;
    if (rotatingLoadingFrameLayout.getVisibility() == View.VISIBLE) {
      ret = true;
    }
    return ret;
  }

  public void hideSoftKeyboard() {
    if (imm != null && getCurrentFocus() != null) {
      imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
  }

  @Override
  public void onBackPressed() {
    Log.info("onBackPressed");
    SingleActivityPageManager manager = ActivityPageManager.getSingleActivityPageManagerByName(MainActivity.class.getName());
    if (manager != null) {
      SingleActivityPage currMyPage = manager.getCurrentPageLink();
      if (currMyPage != null && currMyPage.getPageName() != null) {
        String pageName = currMyPage.getPageName();
        if (pageName.compareTo(PlayServerGridPage.class.getName()) == 0) {
          PlayServerGridPage curFragment = (PlayServerGridPage)
              currMyPage.getFragment(PlayServerGridPage.class.getName(), "R.id.mainFrameLayout");
          if (curFragment.isShowManuallyAddMusicPlayer() == true) {
            curFragment.hideManuallyAddMusicPlayer();
          } else {
            manager.back();
          }
        } else if (pageName.compareTo(PlayListPage.class.getName()) == 0) {
          PlayListPage curFragment = (PlayListPage)
              currMyPage.getFragment(PlayListPage.class.getName(), "R.id.mainFrameLayout");
          curFragment.back();
        } else if (pageName.compareTo(PlayServerSettingsPage.class.getName()) == 0) {
          manager.back();
        } else if (pageName.compareTo(ModifyServerPage.class.getName()) == 0) {
          ModifyServerPage curFragment = (ModifyServerPage)
              currMyPage.getFragment(ModifyServerPage.class.getName(), "R.id.mainFrameLayout");
          curFragment.back();
        } else if (pageName.compareTo(SmbFileListPage.class.getName()) == 0) {
          openModifyServerPage(serverIPAddress);
        } else if (pageName.compareTo(SongsListPage.class.getName()) == 0) {
          SongsListPage curFragment = (SongsListPage)
              currMyPage.getFragment(SongsListPage.class.getName(), "R.id.mainFrameLayout");
          curFragment.back();
        }
      }
    }
  }

  @Override
  protected void onPause() {
    Log.info("onPause");
    super.onPause();
  }

  @Override
  protected void onResume() {
    Log.info("onResume");
    super.onResume();
  }

  @Override
  public void finish() {
    Log.info("finish");
    super.finish();
  }

  @Override
  protected void onStop() {
    Log.info("onStop");
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    Log.info("onDestroy");
    super.onDestroy();
  }

}
