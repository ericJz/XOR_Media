package com.zw.player.playerclient;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONException;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyToast;
import com.xormedia.mylib.SingleActivityPageManager;
import com.xormedia.mylib.smb.MySmbFile;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshListView;
import com.xormedia.socket.SocketMessage;
import com.zw.player.playerclient.adapter.SongsAdapter;
import com.zw.player.playerclient.adapter.SongsFileListAdapter;
import com.zw.player.playerclient.service.Songs;
import com.zw.player.playerclient.service.SongsList;

public class SongsListPage extends Fragment {
  private static Logger Log = Logger.getLogger(SongsListPage.class);

  private Context mContext = null;
  private MainActivity mainActivity = null;

  private TextView songsName_tv = null;

  private PullToRefreshListView songsList_prlv = null;
  private SongsAdapter mSongsAdapter = null;

  private PullToRefreshListView songsFileList_prlv = null;
  private SongsFileListAdapter mSongsFileListAdapter = null;
  private ArrayList<MySmbFile> files = new ArrayList<MySmbFile>();
  private Songs currentSongs = null;

  private RelativeLayout moreOperating_rl = null;
  private LinearLayout renameListName_ll = null;
  private LinearLayout sendToCurrentPlaying_ll = null;

  private RelativeLayout renameLibraryList_rl = null;
  private EditText listName_et = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.info("onCreateView");
    super.onCreateView(inflater, container, savedInstanceState);
    mContext = container.getContext();
    mainActivity = (MainActivity) getActivity();

    View mView = inflater.inflate(R.layout.songs_list_page, container, false);
    new SongsList();
    initOptionView(mView);
    initListView(mView);

    return mView;
  }

  private void initOptionView(View mView) {
    ImageView more_iv = (ImageView) mView.findViewById(R.id.more_iv);
    more_iv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.VISIBLE);
        if (songsList_prlv.getVisibility() == View.VISIBLE) {
          renameListName_ll.setVisibility(View.GONE);
          sendToCurrentPlaying_ll.setVisibility(View.GONE);
        } else if (songsFileList_prlv.getVisibility() == View.VISIBLE) {
          renameListName_ll.setVisibility(View.VISIBLE);
          sendToCurrentPlaying_ll.setVisibility(View.VISIBLE);
        }
        v.setClickable(true);
      }
    });
    songsName_tv = (TextView) mView.findViewById(R.id.songsName_tv);
    songsName_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        back();
      }
    });

    moreOperating_rl = (RelativeLayout) mView.findViewById(R.id.moreOperating_rl);
    moreOperating_rl.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return true;
      }
    });
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
        if (songsList_prlv.getVisibility() == View.VISIBLE) {
          ArrayList<Songs> tmpList = mSongsAdapter.getSeleteList();
          if (tmpList != null && tmpList.size() > 0) {
            moreOperating_rl.setVisibility(View.GONE);
            for (int i = 0; i < tmpList.size(); i++) {
              tmpList.get(i).delete();
            }
            mSongsAdapter.removeAllSelect();
            mSongsAdapter.notifyDataSetChanged();
          } else {
            MyToast.show("请选择曲库", Toast.LENGTH_SHORT);
          }
        } else if (songsFileList_prlv.getVisibility() == View.VISIBLE) {
          ArrayList<MySmbFile> tmpList = mSongsFileListAdapter.getSeleteList();
          if (tmpList != null && tmpList.size() > 0 && currentSongs != null) {
            moreOperating_rl.setVisibility(View.GONE);
            mSongsFileListAdapter.removeAllSelect();
            currentSongs.deleteFiles(tmpList);
            currentSongs.getList();
            if (currentSongs.files != null && currentSongs.files.size() > 0) {
              files.clear();
              files.addAll(currentSongs.files);
              mSongsFileListAdapter.notifyDataSetChanged();
            } else {
              back();
              mSongsAdapter.notifyDataSetChanged();
            }
          } else {
            MyToast.show("请选择", Toast.LENGTH_SHORT);
          }
        }
        v.setClickable(true);
      }
    });
    renameListName_ll = (LinearLayout) mView.findViewById(R.id.renameListName_ll);
    renameListName_ll.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        moreOperating_rl.setVisibility(View.GONE);
        renameLibraryList_rl.setVisibility(View.VISIBLE);
        v.setClickable(true);
      }
    });
    sendToCurrentPlaying_ll = (LinearLayout) mView.findViewById(R.id.sendToCurrentPlaying_ll);
    sendToCurrentPlaying_ll.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        if (mainActivity.playerServer != null && mainActivity.playerServer.isConnected() == true) {
          mainActivity.showRotatingLoadingLayout();
          mainActivity.playerServer.sendPlayList(currentSongs, sendPlayListHandler);
        } else {
          MyToast.show("请连接播放器", Toast.LENGTH_SHORT);
        }
        moreOperating_rl.setVisibility(View.GONE);
        v.setClickable(true);
      }
    });

    renameLibraryList_rl = (RelativeLayout) mView.findViewById(R.id.renameLibraryList_rl);
    renameLibraryList_rl.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return true;
      }
    });
    listName_et = (EditText) mView.findViewById(R.id.listName_et);
    TextView cancel_tv = (TextView) mView.findViewById(R.id.cancel_tv);
    cancel_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        renameLibraryList_rl.setVisibility(View.GONE);
        listName_et.setText(null);
        v.setClickable(true);
      }
    });
    TextView define_tv = (TextView) mView.findViewById(R.id.define_tv);
    define_tv.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        v.setClickable(false);
        String name = listName_et.getText().toString().trim();
        if (name != null && name.length() > 0) {
          if (currentSongs != null) {
            currentSongs.rename(name);
            songsName_tv.setText(name);
          }
          renameLibraryList_rl.setVisibility(View.GONE);
          listName_et.setText(null);
        } else {
          MyToast.show("请输入列表名称", Toast.LENGTH_SHORT);
        }
        v.setClickable(true);
      }
    });
  }

  private Handler sendPlayListHandler = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      mainActivity.hiddenRotatingLoadingLayout();
      boolean isChengGong = false;
      if (msg != null && msg.obj != null && msg.what == 0) {
        SocketMessage tmp = (SocketMessage) msg.obj;
        try {
          if (tmp.response.content.getString("result").compareTo("OK") == 0) {
            isChengGong = true;
            MyToast.show("发送成功", Toast.LENGTH_SHORT);
          }
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      if (isChengGong == false) {
        MyToast.show("发送失败", Toast.LENGTH_SHORT);
      }
      return false;
    }
  });

  private void initListView(View mView) {
    songsList_prlv = (PullToRefreshListView) mView.findViewById(R.id.songsList_prlv);
    songsList_prlv.setMode(Mode.DISABLED);
    songsList_prlv.setScrollingWhileRefreshingEnabled(true);
    mSongsAdapter = new SongsAdapter(mContext, SongsList.SongsList, false, "name", null);
    songsList_prlv.setAdapter(mSongsAdapter);
    songsList_prlv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent != null && parent.getItemAtPosition(position) != null) {
          currentSongs = (Songs) parent.getItemAtPosition(position);
          currentSongs.getList();
          if (currentSongs != null && currentSongs.files != null) {
            songsName_tv.setVisibility(View.VISIBLE);
            songsName_tv.setText(currentSongs.name);
            songsList_prlv.setVisibility(View.GONE);
            songsFileList_prlv.setVisibility(View.VISIBLE);
            files.clear();
            files.addAll(currentSongs.files);
            mSongsFileListAdapter.notifyDataSetChanged();
            songsFileList_prlv.onRefreshComplete();
          }
        }
      }
    });

    songsFileList_prlv = (PullToRefreshListView) mView.findViewById(R.id.songsFileList_prlv);
    songsFileList_prlv.setMode(Mode.DISABLED);
    songsFileList_prlv.setScrollingWhileRefreshingEnabled(true);
    mSongsFileListAdapter = new SongsFileListAdapter(mContext, files, false, "fileName", null);
    songsFileList_prlv.setAdapter(mSongsFileListAdapter);
    songsFileList_prlv.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent != null && parent.getItemAtPosition(position) != null) {
          MySmbFile itemData = (MySmbFile) parent.getItemAtPosition(position);
          if (itemData != null && itemData.fileName != null) {
            MyToast.show(itemData.fileName, Toast.LENGTH_SHORT);
          }
        }
      }
    });
    songsFileList_prlv.setVisibility(View.GONE);
  }

  public void back() {
    if (moreOperating_rl.getVisibility() == View.VISIBLE) {
      moreOperating_rl.setVisibility(View.GONE);
    } else if (renameLibraryList_rl.getVisibility() == View.VISIBLE) {
      renameLibraryList_rl.setVisibility(View.GONE);
    } else if (songsFileList_prlv.getVisibility() == View.VISIBLE) {
      currentSongs = null;
      files.clear();
      songsFileList_prlv.setVisibility(View.GONE);
      songsList_prlv.setVisibility(View.VISIBLE);
      mSongsAdapter.notifyDataSetChanged();
      songsName_tv.setVisibility(View.GONE);
      songsName_tv.setText(null);
    } else {
      SingleActivityPageManager manager = ActivityPageManager.getSingleActivityPageManagerByName(MainActivity.class.getName());
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
    mainActivity.hideSoftKeyboard();
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
