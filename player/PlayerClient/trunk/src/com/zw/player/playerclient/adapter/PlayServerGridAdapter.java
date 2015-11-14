package com.zw.player.playerclient.adapter;

import org.apache.log4j.Logger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zw.player.playerclient.R;
import com.zw.player.playerclient.service.PlayerServer;
import com.zw.player.playerclient.service.PlayerServerList;

public class PlayServerGridAdapter extends BaseAdapter {
  private static Logger Log = Logger.getLogger(PlayServerGridAdapter.class);
  private Context mContext = null;

  public PlayServerGridAdapter(Context _context) {
    mContext = _context;
  }

  @Override
  public int getCount() {
    int ret = 0;
    if (PlayerServerList.list != null && PlayerServerList.list.size() > 0) {
      ret = PlayerServerList.list.size();
    }
    return ret;
  }

  @Override
  public PlayerServer getItem(int position) {
    PlayerServer contact = null;
    if (PlayerServerList.list != null && PlayerServerList.list.size() > 0) {
      contact = PlayerServerList.list.get(position);
    }
    return contact;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Log.info("getView");
    ViewHolder holder = null;
    if (convertView == null) {
      holder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.play_server_grid_item, null);
      holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
      holder.status_tv = (TextView) convertView.findViewById(R.id.status_tv);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    holder.name_tv.setText(null);
    holder.status_tv.setText(null);

    PlayerServer item = PlayerServerList.list.get(position);
    if (item != null) {
      if (item.serverName != null && item.serverName.length() > 0) {
        holder.name_tv.setText(item.serverName);
      }
      if (item.isConnected() == true) {
        holder.status_tv.setText("已连接");
      } else {
        holder.status_tv.setText("未连接");
      }
    }

    return convertView;
  }

  class ViewHolder {
    private TextView name_tv;
    private TextView status_tv;
  }

}
