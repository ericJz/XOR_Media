package com.zw.player.playerclient.adapter;

import java.util.ArrayList;

import com.xormedia.mylib.HaveSelectBaseAdapter;
import com.zw.player.playerclient.R;
import com.zw.player.playerclient.service.Songs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SongsAdapter extends HaveSelectBaseAdapter<Songs> {
  
  public SongsAdapter(Context _context, ArrayList<Songs> _data, boolean _singleSelect, String _identifyAttrName, ArrayList<String> _seleteList) {
    super(_context, _data, _singleSelect, _identifyAttrName, _seleteList);
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
  public Songs getItem(int position) {
    Songs ret = null;
    if (mData != null && mData.size() > 0) {
      ret = mData.get(position);
    }
    return ret;
  }

  @Override
  public long getItemId(int position) {
    // TODO 自动生成的方法存根
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder = null;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.songs_list_adapter, null);
      viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_sla_songsname);
      viewHolder.imv_select = (ImageView) convertView.findViewById(R.id.imv_sla_select);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    
    viewHolder.imv_select.setSelected(false);
    
    Songs songs = mData.get(position);
    if (songs != null) {
      viewHolder.tv_name.setText(songs.name);
      if (isSelect(position)) {
        viewHolder.imv_select.setSelected(true);
      } else {
        viewHolder.imv_select.setSelected(false);
      }
      viewHolder.imv_select.setOnClickListener(selectOnClickListener(viewHolder.imv_select, songs));
    }
    
    return convertView;
  }

  private OnClickListener selectOnClickListener(final ImageView _imv, final Songs _songs) {
    return new OnClickListener() {
      
      @Override
      public void onClick(View v) {
        if (_imv.isSelected()) {
          removeSelect(_songs.name);
          _imv.setSelected(false);
        } else {
          addSelect(_songs.name);
          _imv.setSelected(true);
        }
        
      }
    };
  };
  class ViewHolder {
    public TextView tv_name = null;
    public ImageView imv_select = null;
  }
}
