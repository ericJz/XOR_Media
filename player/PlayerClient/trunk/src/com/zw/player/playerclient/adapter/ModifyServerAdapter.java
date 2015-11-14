package com.zw.player.playerclient.adapter;

import java.util.ArrayList;

import com.xormedia.mylib.smb.MySmbServer;
import com.zw.player.playerclient.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ModifyServerAdapter extends BaseAdapter {

  private Context mContext = null;
  private ArrayList<MySmbServer> mData = null;
  
  public ModifyServerAdapter(Context _context, ArrayList<MySmbServer> _MySmbServers) {
    this.mContext = _context;
    this.mData = _MySmbServers;                 
  }
  
  @Override
  public int getCount() {
    int ret = 0;
    if (mData != null) {
      ret = mData.size();
    }
    return ret;
  }

  @Override
  public MySmbServer getItem(int position) {
    MySmbServer ret = null;
    if (mData != null && mData.size() > 0) {
      ret = mData.get(position);
    }
    return ret;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder = null;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_modifyserver, null);
      viewHolder.tv_ip = (TextView) convertView.findViewById(R.id.tv_ams_ip);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    
    MySmbServer mySmbServer= mData.get(position);
    if (mySmbServer != null) {
      viewHolder.tv_ip.setText(mySmbServer.name);
    }
    
    return convertView;
  }

  class ViewHolder{
    public TextView tv_ip = null;
  }
}
