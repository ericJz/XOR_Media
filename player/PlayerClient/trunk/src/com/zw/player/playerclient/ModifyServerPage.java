package com.zw.player.playerclient;

import java.util.ArrayList;

import org.apache.log4j.Logger;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mylib.ActivityPageManager;
import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.MyToast;
import com.xormedia.mylib.SingleActivityPage;
import com.xormedia.mylib.SingleActivityPageManager;
import com.xormedia.mylib.smb.MySmbServer;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshGridView;
import com.zw.player.playerclient.adapter.ModifyServerAdapter;
import com.zw.player.playerclient.service.SmbServerList;

@SuppressLint("HandlerLeak")
public class ModifyServerPage extends Fragment {
  private static Logger Log = Logger.getLogger(ModifyServerPage.class);

  private ImageView imv_more = null;
  private PullToRefreshGridView gdv_servers = null;

  private RelativeLayout rl_more = null;
  private LinearLayout ll_more = null;
  private LinearLayout ll_addSmb = null;
  private LinearLayout ll_editSmb = null;
  private LinearLayout ll_loginSmb = null;

  private EditText et_addIp = null;
  private EditText et_addServerName = null;
  private EditText et_addUserName = null;
  private EditText et_addpsd = null;
  private EditText et_serverName = null;

  private TextView tv_editIp = null;
  private EditText et_editServerName = null;
  private EditText et_editUserName = null;
  private EditText et_editPsd = null;
  private TextView tv_editForgot = null;
  private TextView tv_editSave = null;

  private TextView tv_loginIp = null;
  private EditText et_loginUserName = null;
  private EditText et_loginPsd = null;
  private TextView tv_logincancle = null;
  private TextView tv_login = null;

  private ModifyServerAdapter serverAdapter = null;
  private Context mContext = null;
  private ArrayList<MySmbServer> mData = new ArrayList<MySmbServer>();
  private MySmbServer select_smbServer = null;
  private int smbserver_index = -1;
  private MainActivity main = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.info("onCreateView");
    super.onCreateView(inflater, container, savedInstanceState);
    mContext = container.getContext();
    main = (MainActivity) getActivity();

    View mView = inflater.inflate(R.layout.modify_server_page, container, false);
    initView(mView);
    initGridView(mView);
    return mView;
  }

  private void showOrHideTabView() {
    if (ll_addSmb.getVisibility() == View.VISIBLE || ll_editSmb.getVisibility() == View.VISIBLE
        || ll_loginSmb.getVisibility() == View.VISIBLE) {
      main.hideTabView();
    } else {
      main.showTabView();
    }
  }

  private void initView(View _view) {
    imv_more = (ImageView) _view.findViewById(R.id.imv_msp_more);
    imv_more.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.VISIBLE);
        ll_more.setVisibility(View.VISIBLE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    rl_more = (RelativeLayout) _view.findViewById(R.id.rl_msp_more);
    rl_more.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

      }
    });
    rl_more.setVisibility(View.GONE);
    ImageView imv_closemore = (ImageView) _view.findViewById(R.id.imv_msp_close);
    imv_closemore.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    ll_more = (LinearLayout) _view.findViewById(R.id.ll_msp_more);
    TextView tv_addSmb = (TextView) _view.findViewById(R.id.tv_msp_add);
    tv_addSmb.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        ll_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.VISIBLE);
        showOrHideTabView();
      }
    });

    TextView tv_searchSmb = (TextView) _view.findViewById(R.id.tv_msp_search);
    tv_searchSmb.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (main != null && !main.isShowRotatingLoadingLayout()) {
          main.showRotatingLoadingLayout();
        }
        SmbServerList.searchLocalNetwork();
        rl_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    ll_addSmb = (LinearLayout) _view.findViewById(R.id.ll_msp_addsmb);
    ll_addSmb.setVisibility(View.GONE);
    et_addIp = (EditText) _view.findViewById(R.id.et_msp_ip);
    et_addServerName = (EditText) _view.findViewById(R.id.et_msp_servername);
    et_addUserName = (EditText) _view.findViewById(R.id.et_msp_username);
    et_addpsd = (EditText) _view.findViewById(R.id.et_msp_psd);
    TextView tv_addcancle = (TextView) _view.findViewById(R.id.tv_msp_cancle);
    tv_addcancle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    TextView tv_adddone = (TextView) _view.findViewById(R.id.tv_msp_adddone);
    tv_adddone.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        String ip = null;
        if (et_addIp != null && et_addIp.getText() != null) {
          ip = et_addIp.getText().toString();
        }
        String serverName = null;
        if (et_addServerName != null && et_addServerName.getText() != null) {
          serverName = et_addServerName.getText().toString();
        }
        String userName = null;
        if (et_addUserName != null && et_addUserName.getText() != null) {
          userName = et_addUserName.getText().toString();
        }
        String psd = null;
        if (et_addpsd != null && et_addpsd.getText() != null) {
          psd = et_addpsd.getText().toString();
        }

        if (ip != null && serverName != null && userName != null && psd != null) {
          MySmbServer smbServer_add = new MySmbServer(serverName, null, ip, userName, psd);
          SmbServerList.updateSmbServer(smbServer_add);
        } else {
          MyToast.show("所有字段不能为空！", Toast.LENGTH_LONG);
        }
      }
    });

    ll_editSmb = (LinearLayout) _view.findViewById(R.id.ll_msp_editsmb);
    tv_editIp = (TextView) _view.findViewById(R.id.tv_msp_edit_ip);
    et_editServerName = (EditText) _view.findViewById(R.id.et_msp_edit_servername);
    et_editUserName = (EditText) _view.findViewById(R.id.et_msp_edit_username);
    et_editPsd = (EditText) _view.findViewById(R.id.et_msp_edit_psd);
    tv_editForgot = (TextView) _view.findViewById(R.id.tv_msp_forgot);
    tv_editForgot.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        SmbServerList.deleteSmbServer(select_smbServer);
      }
    });
    tv_editSave = (TextView) _view.findViewById(R.id.tv_msp_save);
    tv_editSave.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String userName = null;
        String psd = null;
        if (et_editServerName != null && et_editServerName.getText().toString() != null) {
          select_smbServer.name = et_editServerName.getText().toString();
        }
        if (et_editUserName != null && et_editUserName.getText().toString() != null) {
          userName = et_editUserName.getText().toString();
        }
        if (et_editPsd != null && et_editPsd.getText().toString() != null) {
          psd = et_editPsd.getText().toString();
        }
        if (userName != null && psd != null) {
          select_smbServer.setLoginInfo(userName, psd, null);
          SmbServerList.updateSmbServer(select_smbServer);
        } else {
          MyToast.show("用户名或密码不能为空！", Toast.LENGTH_LONG);
        }
        rl_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    ll_loginSmb = (LinearLayout) _view.findViewById(R.id.ll_msp_loginsmb);
    tv_loginIp = (TextView) _view.findViewById(R.id.tv_msp_login_ip);
    et_loginUserName = (EditText) _view.findViewById(R.id.et_msp_login_username);
    et_loginPsd = (EditText) _view.findViewById(R.id.et_msp_login_psd);

    tv_logincancle = (TextView) _view.findViewById(R.id.tv_msp_logincancle);
    tv_logincancle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        rl_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.GONE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
      }
    });

    tv_login = (TextView) _view.findViewById(R.id.tv_msp_login);
    tv_login.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String userName = null;
        String psd = null;
        if (et_loginUserName != null && et_loginUserName.getText() != null) {
          userName = et_loginUserName.getText().toString();
        }
        if (et_loginPsd != null && et_loginPsd.getText() != null) {
          psd = et_loginPsd.getText().toString();
        }

        if (userName != null && psd != null) {
          select_smbServer.setLoginInfo(userName, psd, null);
          select_smbServer.getListOnline(getListHandler);
        } else {
          MyToast.show("用户名或密码不能为空！", Toast.LENGTH_LONG);
        }
      }
    });
  }

  private Handler getListHandler = new Handler() {
    public void handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        Log.info("login success");
        JSONObject ret = new JSONObject();
        try {
          ret.put("smbserver_index", smbserver_index);
        } catch (JSONException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        main.showTitleView(main.playerServer.serverIPAddress);
        main.showTabView();
        main.setTabViewButtonSelected(2);
        SingleActivityPage myPage = new SingleActivityPage(MainActivity.class.getName(), SmbFileListPage.class.getName());
        myPage.setFragment(SmbFileListPage.class.getName(), "R.id.mainFrameLayout");
        myPage.setIsBack(false);
        myPage.setPageParameter(ret);
        myPage.open();
      } else {
        Log.info("login fail");
        MyToast.show("登入失败!", Toast.LENGTH_LONG);
      }
      if (main != null) {
        main.hiddenRotatingLoadingLayout();
      }
    };
  };

  private void initGridView(View _view) {
    gdv_servers = (PullToRefreshGridView) _view.findViewById(R.id.gv_msp_serverlist);
    gdv_servers.setMode(Mode.DISABLED);
    gdv_servers.setScrollingWhileRefreshingEnabled(true);
    if (SmbServerList.smbServerList != null) {
      mData.addAll(SmbServerList.smbServerList);
    }
    serverAdapter = new ModifyServerAdapter(mContext, mData);
    gdv_servers.setAdapter(serverAdapter);
    gdv_servers.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.info("ModifyServerPage login");
        smbserver_index = position;
        select_smbServer = (MySmbServer) parent.getItemAtPosition(position);
        if (select_smbServer.user != null && select_smbServer.password != null) {
          if (main != null && !main.isShowRotatingLoadingLayout()) {
            main.showRotatingLoadingLayout();
          }
          select_smbServer.getListOnline(getListHandler);
        } else {
          tv_loginIp.setText(select_smbServer.rootPath);
          rl_more.setVisibility(View.VISIBLE);
          ll_more.setVisibility(View.GONE);
          ll_editSmb.setVisibility(View.GONE);
          ll_loginSmb.setVisibility(View.VISIBLE);
          ll_addSmb.setVisibility(View.GONE);
          showOrHideTabView();
        }
      }
    });

    gdv_servers.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        select_smbServer = null;
        select_smbServer = (MySmbServer) parent.getItemAtPosition(position);
        tv_editIp.setText(select_smbServer.rootPath);
        et_editPsd.setText(select_smbServer.password);
        et_editServerName.setText(select_smbServer.name);
        et_editUserName.setText(select_smbServer.user);
        rl_more.setVisibility(View.VISIBLE);
        ll_more.setVisibility(View.GONE);
        ll_editSmb.setVisibility(View.VISIBLE);
        ll_loginSmb.setVisibility(View.GONE);
        ll_addSmb.setVisibility(View.GONE);
        showOrHideTabView();
        return true;
      }
    });

    SmbServerList.setListChangedHandler(smbServerListHandler);
  }

  private Handler smbServerListHandler = new Handler() {
    public void handleMessage(Message msg) {
      if (mData != null) {
        mData.clear();
      }
      if (SmbServerList.smbServerList != null) {
        mData.addAll(SmbServerList.smbServerList);
      }
      serverAdapter.notifyDataSetChanged();
      if (main != null) {
        main.hiddenRotatingLoadingLayout();
      }
    };
  };

  public void back() {
    if (rl_more != null && rl_more.getVisibility() == View.VISIBLE) {
      rl_more.setVisibility(View.GONE);
      ll_editSmb.setVisibility(View.GONE);
      ll_loginSmb.setVisibility(View.GONE);
      ll_addSmb.setVisibility(View.GONE);
      showOrHideTabView();
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
    super.onPause();
  }

  @Override
  public void onDestroy() {
    Log.info("onDestroy");
    super.onDestroy();
  }
}
