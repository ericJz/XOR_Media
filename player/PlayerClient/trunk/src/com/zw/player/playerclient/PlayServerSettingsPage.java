package com.zw.player.playerclient;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.MyToast;
import com.zw.player.playerclient.service.PlayerServerList;

@SuppressLint("HandlerLeak")
public class PlayServerSettingsPage extends Fragment {
  private static Logger Log = Logger.getLogger(PlayServerSettingsPage.class);

  private MainActivity mainActivity = null;
  private TextView playerSettingsPortValue_tv = null;
  public TextView playerSettingsPortArrow_tv = null;
  private RelativeLayout playerSettingsEditRoot_rl = null;
  private EditText playerSettingsEditPlayerPort_et = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.info("onCreateView");
    super.onCreateView(inflater, container, savedInstanceState);
    mainActivity = (MainActivity) getActivity();

    View mView = inflater.inflate(R.layout.play_server_settings_page, container, false);
    TextView playerSettingsNameValue_tv = (TextView) mView.findViewById(R.id.playerSettingsNameValue_tv);
    if (mainActivity.playerServer != null && mainActivity.playerServer.serverName != null) {
      playerSettingsNameValue_tv.setText(mainActivity.playerServer.serverName);
    }
    TextView playerSettingsIPAddressValue_tv = (TextView) mView.findViewById(R.id.playerSettingsIPAddressValue_tv);
    if (mainActivity.playerServer != null && mainActivity.playerServer.serverIPAddress != null) {
      playerSettingsIPAddressValue_tv.setText(mainActivity.playerServer.serverIPAddress);
    }
    playerSettingsPortValue_tv = (TextView) mView.findViewById(R.id.playerSettingsPortValue_tv);
    playerSettingsPortValue_tv.setText(String.valueOf(mainActivity.playerServer.socketPort));
    RelativeLayout playerSettingsPortRoot_rl = (RelativeLayout) mView.findViewById(R.id.playerSettingsPortRoot_rl);
    playerSettingsPortRoot_rl.setClickable(true);
    playerSettingsPortRoot_rl.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == false) {
          playerSettingsEditRoot_rl.setVisibility(View.VISIBLE);
          mainActivity.hideTabView();
        }
        v.setClickable(true);
      }
    });
    playerSettingsPortArrow_tv = (TextView) mView.findViewById(R.id.playerSettingsPortArrow_tv);
    if (mainActivity.playerServer.isConnected() == true) {
      playerSettingsPortArrow_tv.setVisibility(View.GONE);
    } else {
      playerSettingsPortArrow_tv.setVisibility(View.VISIBLE);
    }

    // 编辑设置
    playerSettingsEditRoot_rl = (RelativeLayout) mView.findViewById(R.id.playerSettingsEditRoot_rl);
    playerSettingsEditPlayerPort_et = (EditText) mView.findViewById(R.id.playerSettingsEditPlayerPort_et);
    TextView cancel_tv = (TextView) mView.findViewById(R.id.cancel_tv);
    cancel_tv.setClickable(true);
    cancel_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        playerSettingsEditPlayerPort_et.setText(null);
        playerSettingsEditRoot_rl.setVisibility(View.GONE);
        mainActivity.showTabView();
        v.setClickable(true);
      }
    });
    TextView define_tv = (TextView) mView.findViewById(R.id.define_tv);
    define_tv.setClickable(true);
    define_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (playerSettingsEditPlayerPort_et.getText() != null
            && playerSettingsEditPlayerPort_et.getText().toString().length() > 0) {
          if (mainActivity.playerServer != null) {
            mainActivity.playerServer.socketPort = Integer.valueOf(playerSettingsEditPlayerPort_et.getText().toString());
            if (PlayerServerList.updatePlayerServer(mainActivity.playerServer) == false) {
              MyToast.show("更新播放器监听端口失败", Toast.LENGTH_SHORT);
            } else {
              playerSettingsPortValue_tv.setText(String.valueOf(mainActivity.playerServer.socketPort));
              playerSettingsEditPlayerPort_et.setText(null);
              playerSettingsEditRoot_rl.setVisibility(View.GONE);
            }
          }
        } else {
          MyToast.show("请填写播放器监听端口", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });

    return mView;
  }

  @Override
  public void onResume() {
    Log.info("onResume");
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.info("onPause");
    mainActivity.hiddenRotatingLoadingLayout();
    mainActivity.hideSoftKeyboard();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    Log.info("onDestroy");
    super.onDestroy();
  }

}
