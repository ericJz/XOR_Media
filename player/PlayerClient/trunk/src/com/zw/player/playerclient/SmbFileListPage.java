package com.zw.player.playerclient;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyToast;
import com.xormedia.mylib.SingleActivityPage;
import com.xormedia.mylib.SingleActivityPageManager;
import com.xormedia.mylib.smb.MySmb;
import com.xormedia.mylib.smb.MySmbFile;
import com.xormedia.mylib.smb.MySmbServer;
import com.zw.player.playerclient.adapter.SmbFileListAdapter;
import com.zw.player.playerclient.adapter.SongsAdapter;
import com.zw.player.playerclient.service.SmbServerList;
import com.zw.player.playerclient.service.Songs;
import com.zw.player.playerclient.service.SongsList;

@SuppressLint("HandlerLeak")
public class SmbFileListPage extends Fragment {
  private static Logger Log = Logger.getLogger(SmbFileListPage.class);

  private Context mContext = null;
  private TextView tv_back = null;
  private ImageView imv_more = null;
  private LinearLayout ll_path = null;
  private LinearLayout ll_more = null;
  private LinearLayout ll_newsong = null;
  private LinearLayout ll_oldsongs = null;

  private RelativeLayout rl_more = null;
  private ListView ls_smb = null;
  private ImageView imv_close = null;
  private TextView tv_addnewplayerlist = null;
  private TextView tv_addoldplayerlist = null;
  private TextView tv_selectall = null;
  private TextView tv_refresh = null;
  private EditText et_songs = null;
  private TextView tv_newcancle = null;
  private TextView tv_newadd = null;
  private TextView tv_oldcancle = null;
  private TextView tv_oldadd = null;

  private SmbFileListAdapter adapter = null;
  private ArrayList<MySmbFile> mListData = new ArrayList<MySmbFile>();
  private ArrayList<MySmbFile> showSmbFiles = new ArrayList<MySmbFile>();
  private MySmbServer current_SmbServer = null;
  private ArrayList<Songs> playerSongslist = new ArrayList<Songs>();
  private MainActivity main = null;
  private ListView ls_songs = null;
  private SongsAdapter songsAdapter = null;
  private ArrayList<Songs> mDataSongs = new ArrayList<Songs>();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = container.getContext();
    main = (MainActivity) getActivity();
    View mView = inflater.inflate(R.layout.smbfile_list, container, false);
    getParam();
    initView(mView);
    showPath();
    return mView;
  }

  private void getParam() {
    SingleActivityPageManager manager = ActivityPageManager.getSingleActivityPageManagerByName(MainActivity.class.getName());
    if (manager != null) {
      SingleActivityPage currMyPage = manager.getCurrentPageLink();
      if (currMyPage != null) {
        JSONObject jo = currMyPage.getPageParameter();
        if (jo != null) {
          try {
            if (jo.has("smbserver_index") == true && jo.isNull("smbserver_index") == false) {
              current_SmbServer = SmbServerList.smbServerList.get(jo.getInt("smbserver_index"));
            }
          } catch (JSONException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
    }
    if (current_SmbServer != null) {
      mListData.addAll(current_SmbServer.getList());
    }

  }

  private void showOrHideTabView() {
    if (rl_more.getVisibility() == View.VISIBLE || ll_newsong.getVisibility() == View.VISIBLE || ll_oldsongs.getVisibility() == View.VISIBLE) {
      main.hideTabView();
    } else {
      main.showTabView();
    }
  }
  
  private void initView(View _view) {
    tv_back = (TextView) _view.findViewById(R.id.tv_sfl_back);
    tv_back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        main.openModifyServerPage(main.playerServer.serverIPAddress);
      }
    });
    imv_more = (ImageView) _view.findViewById(R.id.imv_sfl_more);
    imv_more.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.VISIBLE);
        ll_more.setVisibility(View.VISIBLE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    ll_path = (LinearLayout) _view.findViewById(R.id.ll_sfl_path);

    imv_close = (ImageView) _view.findViewById(R.id.imv_sfl_close);
    imv_close.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    tv_addnewplayerlist = (TextView) _view.findViewById(R.id.tv_sfl_addsongs);
    tv_addnewplayerlist.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.VISIBLE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.VISIBLE);
        ll_oldsongs.setVisibility(View.GONE);
        et_songs.setText(null);
        showOrHideTabView();
      }
    });

    tv_addoldplayerlist = (TextView) _view.findViewById(R.id.tv_sfl_addoldsongs);
    tv_addoldplayerlist.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.VISIBLE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.VISIBLE);

        if (mDataSongs != null) {
          mDataSongs.clear();
        } else {
          mDataSongs = new ArrayList<Songs>();
        }
        mDataSongs.addAll(SongsList.SongsList);
        songsAdapter.removeAllSelect();
        songsAdapter.notifyDataSetChanged();
        showOrHideTabView();
      }
    });

    tv_selectall = (TextView) _view.findViewById(R.id.tv_sfl_all);
    tv_selectall.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        ArrayList<String> tmp = new ArrayList<String>();
        for (int i = 0; i < mListData.size(); i++) {
          tmp.add(mListData.get(i).fileName);
        }
        adapter.setSelectList(tmp);
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    tv_refresh = (TextView) _view.findViewById(R.id.tv_sfl_refresh);
    tv_refresh.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (showSmbFiles != null && showSmbFiles.size() > 0) {
          adapter.removeAllSelect();
          MySmbFile tmp_smb = showSmbFiles.get(showSmbFiles.size() -1);
          tmp_smb.getListOnline(0, getSubHandler);
        }
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    rl_more = (RelativeLayout) _view.findViewById(R.id.rl_sfl_more);
    rl_more.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (main != null) {
          main.hideSoftKeyboard();
        }
      }
    });

    ll_more = (LinearLayout) _view.findViewById(R.id.ll_sfl_more);
    ll_newsong = (LinearLayout) _view.findViewById(R.id.ll_sfl_newsongs);
    ll_oldsongs = (LinearLayout) _view.findViewById(R.id.ll_sfl_oldsongs);
    et_songs = (EditText) _view.findViewById(R.id.et_sfl_songsname);
    tv_newcancle = (TextView) _view.findViewById(R.id.tv_sfl_cancle);
    tv_newcancle.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });
    tv_newadd = (TextView) _view.findViewById(R.id.tv_sfl_adddone);
    tv_newadd.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (et_songs != null && et_songs.getText() != null) {
          if (playerSongslist != null) {
            playerSongslist.clear();
          }
          String songs_name = et_songs.getText().toString();
          if (songs_name != null) {
            Songs new_songs = new Songs(songs_name);
            playerSongslist.add(new_songs);
          }
          ArrayList<MySmbFile> tmp = adapter.getSeleteList();
          MySmb.getLeaves(tmp, false, getLeavesHandler);
        }
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    tv_oldcancle = (TextView) _view.findViewById(R.id.tv_sfl_oldcancle);
    tv_oldcancle.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_more.setVisibility(View.GONE);
        ll_newsong.setVisibility(View.GONE);
        ll_oldsongs.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    tv_oldadd = (TextView) _view.findViewById(R.id.tv_sfl_oldadddone);
    tv_oldadd.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        if (songsAdapter != null) {
          ArrayList<Songs> selectTmp = songsAdapter.getSeleteList();
          if (playerSongslist != null) {
            playerSongslist.clear();
          } else {
            playerSongslist = new ArrayList<Songs>();
          }
          if (selectTmp != null) {
            ArrayList<MySmbFile> tmp = adapter.getSeleteList();
            MySmb.getLeaves(tmp, false, getLeavesHandler);
          }
        }
        showOrHideTabView();
      }
    });

    adapter = new SmbFileListAdapter(mContext, mListData, false, "fileName", null);
    ls_smb = (ListView) _view.findViewById(R.id.ls_sfl_list);
    ls_smb.setAdapter(adapter);

    ls_smb.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MySmbFile tmp_smb = (MySmbFile) parent.getItemAtPosition(position);
        if (tmp_smb != null && tmp_smb.isFolder == true) {
          if (mListData != null) {
            if (main != null && !main.isShowRotatingLoadingLayout()) {
              main.showRotatingLoadingLayout();
            }
            showSmbFiles.add(tmp_smb);
            tmp_smb.getListOnline(0, getSubHandler);
            showPath();
          }
        }
      }
    });

    ls_songs = (ListView) _view.findViewById(R.id.ls_sfl_songs);
    if (mDataSongs != null) {
      mDataSongs.clear();
    } else {
      mDataSongs = new ArrayList<Songs>();
    }
    mDataSongs.addAll(SongsList.SongsList);
    songsAdapter = new SongsAdapter(mContext, mDataSongs, false, "name", null);
    ls_songs.setAdapter(songsAdapter);
  }

  private Handler getSubHandler = new Handler() {
    @SuppressWarnings("unchecked")
    public void handleMessage(Message msg) {
      if (msg != null) {
        if (mListData != null) {
          mListData.clear();
        }
        if (msg.obj != null) {
          mListData.addAll((ArrayList<MySmbFile>) msg.obj);
        }
        adapter.notifyDataSetChanged();
      }
      if (main != null) {
        main.hiddenRotatingLoadingLayout();
      }
    };
  };

  private void showPath() {
    ll_path.removeAllViews();
    ll_path.removeAllViewsInLayout();
    View view_root = LayoutInflater.from(mContext).inflate(R.layout.smbfilepath, null);
    TextView tv_path_root = (TextView) view_root.findViewById(R.id.tv_sfp_path);
    tv_path_root.setText(current_SmbServer.rootPath);
    tv_path_root.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (mListData != null) {
          mListData.clear();
        }
        if (showSmbFiles != null) {
          showSmbFiles.clear();
        }
        showPath();
        mListData.addAll(current_SmbServer.getList());
        adapter.notifyDataSetChanged();
      }
    });
    ll_path.addView(view_root);
    for (int i = 0; i < showSmbFiles.size(); i++) {
      View view = LayoutInflater.from(mContext).inflate(R.layout.smbfilepath, null);
      TextView tv_path = (TextView) view.findViewById(R.id.tv_sfp_path);
      tv_path.setText(showSmbFiles.get(i).fileName);
      tv_path.setOnClickListener(showPathClick(showSmbFiles.get(i)));
      ll_path.addView(view);
    }
  }

  private OnClickListener showPathClick(final MySmbFile _mySmbFile) {
    return new OnClickListener() {

      @Override
      public void onClick(View v) {
        boolean found = false;
        int i = 0;
        while (i < showSmbFiles.size()) {
          if (showSmbFiles.get(i).filePath.compareTo(_mySmbFile.filePath) == 0) {
            found = true;
            i++;
          }
          if (found == true) {
            if (i < showSmbFiles.size()) {
              showSmbFiles.remove(i);
            }
          } else {
            i++;
          }
        }

        showPath();
        if (mListData != null) {
          mListData.clear();
        }
        mListData.addAll(_mySmbFile.getList());
        adapter.notifyDataSetChanged();

      }
    };
  }

  private Handler getLeavesHandler = new Handler() {
    public void handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        for (int i = 0; i < playerSongslist.size(); i++) {
          Songs mSongs = playerSongslist.get(i);
          boolean ret = mSongs.addFiles((JSONArray) msg.obj);
          if (ret == false) {
            MyToast.show("加入失败", Toast.LENGTH_LONG);
          }
        }
      }
      rl_more.setVisibility(View.GONE);
    };
  };

  @Override
  public void onPause() {
    super.onPause();
    if (main != null) {
      main.hiddenRotatingLoadingLayout();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

}
