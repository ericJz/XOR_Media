package com.zw.player.playerclient;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.MyToast;
import com.xormedia.mylib.SingleActivityPageManager;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshListView;
import com.zw.player.playerclient.adapter.PlayListAdapter;
import com.zw.player.playerclient.service.PlayFile;

@SuppressLint("HandlerLeak")
public class PlayListPage extends Fragment {
  private static Logger Log = Logger.getLogger(PlayListPage.class);

  private Context mContext = null;
  private MainActivity mainActivity = null;

  private ImageView playStatus_iv = null;// 播放状态
  private TextView currentPlayName_tv = null;
  private TextView downloadProgress_tv = null;

  private PullToRefreshListView playList_prlv = null;
  private PlayListAdapter mPlayListAdapter = null;
  private ArrayList<MyFile> files = new ArrayList<MyFile>();

  private RelativeLayout moreOperating_rl = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.info("onCreateView");
    super.onCreateView(inflater, container, savedInstanceState);
    mContext = container.getContext();
    mainActivity = (MainActivity) getActivity();

    View mView = inflater.inflate(R.layout.play_list_page, container, false);
    initView(mView);
    getData();

    return mView;
  }

  private void initView(View mView) {
    ImageView previousButton_iv = (ImageView) mView.findViewById(R.id.previousButton_iv);
    previousButton_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity != null && mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          if (mPlayListAdapter != null) {
            int tmpIndex = mPlayListAdapter.getCurrentPlayFile().index;
            if (tmpIndex >= 0 && tmpIndex < files.size()) {
              if (tmpIndex > 0) {
                tmpIndex = tmpIndex - 1;
              } else {
                tmpIndex = files.size() - 1;
              }
              MyFile tmpFile = files.get(tmpIndex);
              if (tmpFile != null) {
                mainActivity.playerServer.sendPlay(tmpFile, tmpIndex, sendPlayHandler);
              }
            }
          }
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });
    ImageView nextButton_iv = (ImageView) mView.findViewById(R.id.nextButton_iv);
    nextButton_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity != null && mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          if (mPlayListAdapter != null) {
            int tmpIndex = mPlayListAdapter.getCurrentPlayFile().index;
            if (tmpIndex >= 0 && tmpIndex < files.size()) {
              if (tmpIndex < files.size() - 1) {
                tmpIndex = tmpIndex + 1;
              } else {
                tmpIndex = 0;
              }
              MyFile tmpFile = files.get(tmpIndex);
              if (tmpFile != null) {
                mainActivity.playerServer.sendPlay(tmpFile, tmpIndex, sendPlayHandler);
              }
            }
          }
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });
    ImageView pauseButton_iv = (ImageView) mView.findViewById(R.id.pauseButton_iv);
    pauseButton_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity != null && mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          if (mPlayListAdapter != null) {
            PlayFile playFile = mPlayListAdapter.getCurrentPlayFile();
            if (playFile != null && playFile.playStatus != null && playFile.playStatus.length() > 0) {
              if (playFile.playStatus.compareTo(PlayFile.STATUS_PLAYING) == 0) {
                mainActivity.playerServer.sendPause(sendPlayHandler);
              }
            }
          }
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });
    ImageView playButton_iv = (ImageView) mView.findViewById(R.id.playButton_iv);
    playButton_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity != null && mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          if (mPlayListAdapter != null) {
            PlayFile playFile = mPlayListAdapter.getCurrentPlayFile();
            if (playFile != null && playFile.playStatus != null && playFile.playStatus.length() > 0) {
              if (playFile.playStatus.compareTo(PlayFile.STATUS_PAUSE) == 0) {
                int tmpIndex = playFile.index;
                if (tmpIndex >= 0 && tmpIndex < files.size()) {
                  MyFile tmpFile = files.get(tmpIndex);
                  if (tmpFile != null) {
                    mainActivity.playerServer.sendPlay(tmpFile, tmpIndex, sendPlayHandler);
                  }
                }
              }
            }
          }
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });

    playStatus_iv = (ImageView) mView.findViewById(R.id.playStatus_iv);
    currentPlayName_tv = (TextView) mView.findViewById(R.id.currentPlayName_tv);
    downloadProgress_tv = (TextView) mView.findViewById(R.id.downloadProgress_tv);

    playList_prlv = (PullToRefreshListView) mView.findViewById(R.id.playList_prlv);
    playList_prlv.setMode(Mode.DISABLED);
    playList_prlv.setScrollingWhileRefreshingEnabled(true);
    mPlayListAdapter = new PlayListAdapter(mContext, files, false, "fileName", null);
    playList_prlv.setAdapter(mPlayListAdapter);
    playList_prlv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity != null && mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          MyFile myFile = null;
          PlayFile playFile = null;
          int tmpIndex = -1;
          if (mPlayListAdapter != null) {
            playFile = mPlayListAdapter.getCurrentPlayFile();
            tmpIndex = playFile.index;
          }
          if ((position - 1) != tmpIndex) {
            tmpIndex = position - 1;
            myFile = files.get(tmpIndex);
            if (myFile != null) {
              mainActivity.playerServer.sendPlay(myFile, tmpIndex, sendPlayHandler);
            }
          } else {
            if (tmpIndex >= 0 && tmpIndex < files.size()) {
              myFile = files.get(tmpIndex);
            }
            if (playFile != null && playFile.playStatus != null && playFile.playStatus.length() > 0) {
              if (playFile.playStatus.compareTo(PlayFile.STATUS_PAUSE) == 0) {
                if (myFile != null) {
                  mainActivity.playerServer.sendPlay(myFile, tmpIndex, sendPlayHandler);
                }
              } else if (playFile.playStatus.compareTo(PlayFile.STATUS_PLAYING) == 0) {
                if (myFile != null) {
                  mainActivity.playerServer.sendPause(sendPlayHandler);
                }
              }
            }
          }
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
      }
    });

    ImageView more_iv = (ImageView) mView.findViewById(R.id.more_iv);
    more_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (moreOperating_rl.getVisibility() == View.VISIBLE) {
          moreOperating_rl.setVisibility(View.GONE);
        } else {
          moreOperating_rl.setVisibility(View.VISIBLE);
        }
        v.setClickable(true);
      }
    });
    moreOperating_rl = (RelativeLayout) mView.findViewById(R.id.moreOperating_rl);
    ImageView closeMoreOperating_iv = (ImageView) mView.findViewById(R.id.closeMoreOperating_iv);
    closeMoreOperating_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.GONE);
        v.setClickable(true);
      }
    });
    LinearLayout deleteSelect_ll = (LinearLayout) mView.findViewById(R.id.deleteSelect_ll);
    deleteSelect_ll.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        MyToast.show("删除所选", Toast.LENGTH_SHORT);
        v.setClickable(true);
      }
    });
    LinearLayout deleteAllList_ll = (LinearLayout) mView.findViewById(R.id.deleteAllList_ll);
    deleteAllList_ll.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        MyToast.show("删除所有", Toast.LENGTH_SHORT);
        v.setClickable(true);
      }
    });
    LinearLayout reacquireList_ll = (LinearLayout) mView.findViewById(R.id.reacquireList_ll);
    reacquireList_ll.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        MyToast.show("重新获取列表", Toast.LENGTH_SHORT);
        v.setClickable(true);
      }
    });
  }

  private void getData() {
    if (mainActivity != null && mainActivity.playerServer != null) {
      mainActivity.playerServer.setPlaylistChangedHandler(playlistChangedHandler);
      mainActivity.playerServer.setCurrentPlayFileChangedHandler(currentPlayFileChangedHandler);
      mainActivity.playerServer.setCurrentPlayFileStatusChangedHandler(currentPlayFileStatusChangedHandler);
      mainActivity.playerServer.setCurrentPlayFileDownloadProcessHandler(currentPlayFileDownloadProcessHandler);
      if (mainActivity.playerServer.isConnected() == true) {
        mainActivity.playerServer.getPlayList();
      } else {
        MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
      }
    }
  }

  private void setCurrentPlayFileUI(PlayFile _playFile) {
    currentPlayName_tv.setText(null);
    playStatus_iv.setImageResource(0);
    downloadProgress_tv.setText(null);
    if (_playFile != null && _playFile.index >= 0 && _playFile.index < files.size()) {
      if (mPlayListAdapter != null) {
        mPlayListAdapter.setCurrentPlayFile(_playFile);
        mPlayListAdapter.notifyDataSetChanged();
      }
      if (_playFile.playStatus != null && _playFile.playStatus.length() > 0) {
        if (_playFile.playStatus.compareTo(PlayFile.STATUS_PLAYING) == 0) {
          playStatus_iv.setImageResource(R.drawable.play_icon);
        } else if (_playFile.playStatus.compareTo(PlayFile.STATUS_PAUSE) == 0) {
          playStatus_iv.setImageResource(R.drawable.pause_icon);
        }
      }
      downloadProgress_tv.setText("(" + _playFile.downloadProgress + ")");
      MyFile myFile = files.get(_playFile.index);
      if (myFile != null) {
        currentPlayName_tv.setText(myFile.fileName);
      }
    }
  }

  private Handler currentPlayFileChangedHandler = new Handler(Looper.getMainLooper(), new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      Log.info("currentPlayFileChangedHandler");
      PlayFile tmp = null;
      if (msg != null && msg.obj != null) {
        tmp = (PlayFile) msg.obj;
      }
      setCurrentPlayFileUI(tmp);
      return false;
    }
  });

  private Handler currentPlayFileStatusChangedHandler = new Handler(Looper.getMainLooper(), new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      Log.info("currentPlayFileStatusChangedHandler");
      PlayFile tmp = null;
      if (msg != null && msg.obj != null) {
        tmp = (PlayFile) msg.obj;
      }
      setCurrentPlayFileUI(tmp);
      return false;
    }
  });

  private Handler playlistChangedHandler = new Handler(Looper.getMainLooper(), new Callback() {
    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
      Log.info("playlistChangedHandler");
      if (msg != null && msg.obj != null) {
        ArrayList<MyFile> fileList = (ArrayList<MyFile>) msg.obj;
        files.clear();
        if (fileList != null && fileList.size() > 0) {
          files.addAll(fileList);
          fileList.clear();
        }
        if (mPlayListAdapter != null) {
          mPlayListAdapter.notifyDataSetChanged();
        }
      }
      return false;
    }
  });

  private Handler currentPlayFileDownloadProcessHandler = new Handler(Looper.getMainLooper(), new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      Log.info("currentPlayFileDownloadProcessHandler");
      PlayFile tmp = null;
      if (msg != null && msg.obj != null) {
        tmp = (PlayFile) msg.obj;
      }
      setCurrentPlayFileUI(tmp);
      return false;
    }
  });

  private Handler sendPlayHandler = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.what == 0) {

      } else {
        MyToast.show("播放失败", Toast.LENGTH_SHORT);
      }
      return false;
    }
  });

  private Handler sendPauseHandler = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.what == 0) {

      } else {
        MyToast.show("暂停失败", Toast.LENGTH_SHORT);
      }
      return false;
    }
  });

  public void back() {
    if (moreOperating_rl.getVisibility() == View.VISIBLE) {
      moreOperating_rl.setVisibility(View.GONE);
    } else {
      SingleActivityPageManager manager =
          ActivityPageManager.getSingleActivityPageManagerByName(MainActivity.class.getName());
      manager.back();
    }
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
    super.onPause();
  }

  @Override
  public void onDestroy() {
    Log.info("onDestroy");
    files.clear();
    super.onDestroy();
  }

}
