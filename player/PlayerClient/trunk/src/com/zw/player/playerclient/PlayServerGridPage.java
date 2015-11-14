package com.zw.player.playerclient;

import org.apache.log4j.Logger;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.MyToast;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshGridView;
import com.zw.player.playerclient.adapter.PlayServerGridAdapter;
import com.zw.player.playerclient.service.PlayerServer;
import com.zw.player.playerclient.service.PlayerServerList;

public class PlayServerGridPage extends Fragment {
  private static Logger Log = Logger.getLogger(PlayServerGridPage.class);

  private Context mContext = null;
  private PullToRefreshGridView playServerGrid_prgv = null;
  private PlayServerGridAdapter mPlayServerGridAdapter = null;
  private RelativeLayout moreOperating_rl = null;
  private RelativeLayout manuallyAddMusicPlayer_rl = null;
  private TextView title_tv = null;
  private EditText playerName_et = null;
  private EditText playerIPAddress_et = null;
  private EditText playerPort_et = null;
  private TextView delete_tv = null;
  private TextView define_tv = null;
  private View deleteLine_v = null;
  private PlayerServer currPlayerServer = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.info("onCreateView");
    super.onCreateView(inflater, container, savedInstanceState);
    mContext = container.getContext();
    ((MainActivity) getActivity()).hideTitleView();
    ((MainActivity) getActivity()).hideTabView();
    PlayerServerList.searchPlayerServer(connectedHandler);
    View mView = inflater.inflate(R.layout.play_server_grid_page, container, false);
    playServerGrid_prgv = (PullToRefreshGridView) mView.findViewById(R.id.playServerGrid_prgv);
    playServerGrid_prgv.setMode(Mode.DISABLED);
    playServerGrid_prgv.setScrollingWhileRefreshingEnabled(true);
    mPlayServerGridAdapter = new PlayServerGridAdapter(mContext);
    playServerGrid_prgv.setAdapter(mPlayServerGridAdapter);
    playServerGrid_prgv.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent != null) {
          PlayerServer itemData = (PlayerServer) parent.getItemAtPosition(position);
          if (itemData != null && itemData.serverIPAddress != null) {
            ((MainActivity) getActivity()).openSongsListPage(itemData.serverIPAddress);
          }
        }
      }
    });
    playServerGrid_prgv.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent != null) {
          PlayerServer itemData = (PlayerServer) parent.getItemAtPosition(position);
          if (itemData != null && itemData.isConnected() == false) {
            showManuallyAddMusicPlayer(itemData);
          }
        }
        return true;
      }
    });

    ImageView more_iv = (ImageView) mView.findViewById(R.id.more_iv);
    more_iv.setClickable(true);
    more_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.VISIBLE);
        v.setClickable(true);
      }
    });
    moreOperating_rl = (RelativeLayout) mView.findViewById(R.id.moreOperating_rl);
    ImageView closeMoreOperating_iv = (ImageView) mView.findViewById(R.id.closeMoreOperating_iv);
    closeMoreOperating_iv.setClickable(true);
    closeMoreOperating_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.GONE);
        v.setClickable(true);
      }
    });
    TextView manuallyAddMusicPlayer_tv = (TextView) mView.findViewById(R.id.manuallyAddMusicPlayer_tv);
    manuallyAddMusicPlayer_tv.setClickable(true);
    manuallyAddMusicPlayer_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.GONE);
        showManuallyAddMusicPlayer(null);
        v.setClickable(true);
      }
    });
    TextView scanOnlineMusicPlayer_tv = (TextView) mView.findViewById(R.id.scanOnlineMusicPlayer_tv);
    scanOnlineMusicPlayer_tv.setClickable(true);
    scanOnlineMusicPlayer_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.GONE);
        PlayerServerList.searchPlayerServer(null);
        v.setClickable(true);
      }
    });

    manuallyAddMusicPlayer_rl = (RelativeLayout) mView.findViewById(R.id.manuallyAddMusicPlayer_rl);
    title_tv = (TextView) mView.findViewById(R.id.title_tv);
    playerName_et = (EditText) mView.findViewById(R.id.playerName_et);
    playerIPAddress_et = (EditText) mView.findViewById(R.id.playerIPAddress_et);
    playerPort_et = (EditText) mView.findViewById(R.id.playerPort_et);
    delete_tv = (TextView) mView.findViewById(R.id.delete_tv);
    delete_tv.setClickable(true);
    delete_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        hideManuallyAddMusicPlayer();
        if (currPlayerServer != null) {
          if (PlayerServerList.deletePlayerServer(currPlayerServer) == false) {
            MyToast.show("删除播放器失败", Toast.LENGTH_SHORT);
          }
        }
        v.setClickable(true);
      }
    });
    deleteLine_v = (View) mView.findViewById(R.id.deleteLine_v);
    TextView cancel_tv = (TextView) mView.findViewById(R.id.cancel_tv);
    cancel_tv.setClickable(true);
    cancel_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        hideManuallyAddMusicPlayer();
        v.setClickable(true);
      }
    });
    define_tv = (TextView) mView.findViewById(R.id.define_tv);
    define_tv.setClickable(true);
    define_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (playerName_et.getText() != null
            && playerName_et.getText().toString().length() > 0
            && playerIPAddress_et.getText() != null
            && playerIPAddress_et.getText().toString().length() > 0
            && playerPort_et.getText() != null
            && playerPort_et.getText().toString().length() > 0) {
          hideManuallyAddMusicPlayer();
          if (currPlayerServer != null) {
            currPlayerServer.serverName = playerName_et.getText().toString();
            currPlayerServer.serverIPAddress = playerIPAddress_et.getText().toString();
            currPlayerServer.socketPort = Integer.valueOf(playerPort_et.getText().toString());
            if (PlayerServerList.updatePlayerServer(currPlayerServer) == false) {
              MyToast.show("更新播放器失败", Toast.LENGTH_SHORT);
            }
          } else {
            PlayerServer mPlayerServer = new PlayerServer(playerIPAddress_et.getText().toString()
                , playerName_et.getText().toString()
                , Integer.valueOf(playerPort_et.getText().toString()));
            if (PlayerServerList.updatePlayerServer(mPlayerServer) == false) {
              MyToast.show("更新播放器失败", Toast.LENGTH_SHORT);
            }
          }
        } else {
          MyToast.show("播放器信息填写完整", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });

    return mView;
  }

  private Handler connectedHandler = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      Log.info("connectedHandler Callback");
      if (mPlayServerGridAdapter != null) {
        mPlayServerGridAdapter.notifyDataSetChanged();
      }
      if (playServerGrid_prgv != null) {
        playServerGrid_prgv.onRefreshComplete();
      }
      return false;
    }
  });

  /**
   * 显示手动添加音乐播放器
   * 
   * @param _name
   *          播放器名称
   * @param _ip
   *          播放器IP地址
   * @param _port
   *          播放器监听端口
   */
  private void showManuallyAddMusicPlayer(PlayerServer _playerServer) {
    currPlayerServer = _playerServer;
    if (currPlayerServer != null) {
      title_tv.setText(R.string.edit_music_player);
      playerName_et.setText(currPlayerServer.serverName);
      playerIPAddress_et.setText(currPlayerServer.serverIPAddress);
      playerPort_et.setText(String.valueOf(currPlayerServer.socketPort));
      define_tv.setText(R.string.save_txt);
      delete_tv.setVisibility(View.VISIBLE);
      deleteLine_v.setVisibility(View.VISIBLE);
    } else {
      title_tv.setText(R.string.add_music_player);
      playerName_et.setText(null);
      playerIPAddress_et.setText(null);
      playerPort_et.setText(null);
      define_tv.setText(R.string.add_txt);
      delete_tv.setVisibility(View.GONE);
      deleteLine_v.setVisibility(View.GONE);
    }
    manuallyAddMusicPlayer_rl.setVisibility(View.VISIBLE);
  }

  public void hideManuallyAddMusicPlayer() {
    manuallyAddMusicPlayer_rl.setVisibility(View.GONE);
  }

  public boolean isShowManuallyAddMusicPlayer() {
    boolean ret = false;
    if (manuallyAddMusicPlayer_rl.getVisibility() == View.VISIBLE) {
      ret = true;
    }
    return ret;
  }

  @Override
  public void onResume() {
    Log.info("onResume");
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.info("onPause");
    super.onPause();
  }

  @Override
  public void onDestroy() {
    Log.info("onDestroy");
    super.onDestroy();
  }
}
