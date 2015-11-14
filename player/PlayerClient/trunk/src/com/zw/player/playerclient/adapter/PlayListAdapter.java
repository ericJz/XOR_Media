package com.zw.player.playerclient.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xormedia.mylib.HaveSelectBaseAdapter;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.zw.player.playerclient.R;
import com.zw.player.playerclient.service.PlayFile;

public class PlayListAdapter extends HaveSelectBaseAdapter<MyFile> {
  private PlayFile currentPlayFile = null;

  public PlayListAdapter(Context _context, ArrayList<MyFile> _data, boolean _singleSelect, String _identifyAttrName, ArrayList<String> _seleteList) {
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
  public MyFile getItem(int position) {
    MyFile ret = null;
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
      convertView = LayoutInflater.from(mContext).inflate(R.layout.play_list_item, null);
      viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
      viewHolder.selectBox_iv = (ImageView) convertView.findViewById(R.id.selectBox_iv);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    viewHolder.selectBox_iv.setSelected(false);
    viewHolder.name_tv.setText(null);
    viewHolder.name_tv.setCompoundDrawables(null, null, null, null);

    MyFile myFile = mData.get(position);
    if (myFile != null) {
      viewHolder.name_tv.setText(myFile.fileName);
      if (currentPlayFile != null && currentPlayFile.index == position) {
        Drawable drawableRight = mContext.getResources().getDrawable(R.drawable.play_list_item_playstatus_icon);
        drawableRight.setBounds(0, 0, drawableRight.getMinimumWidth(), drawableRight.getMinimumHeight());
        Drawable drawableLeft = null;
        if (currentPlayFile.playStatus != null && currentPlayFile.playStatus.length() > 0) {
          if (currentPlayFile.playStatus.compareTo(PlayFile.STATUS_PLAYING) == 0) {
            drawableLeft = mContext.getResources().getDrawable(R.drawable.play_icon);
            drawableLeft.setBounds(0, 0, drawableLeft.getMinimumWidth(), drawableLeft.getMinimumHeight());
          } else if (currentPlayFile.playStatus.compareTo(PlayFile.STATUS_PAUSE) == 0) {
            drawableLeft = mContext.getResources().getDrawable(R.drawable.pause_icon);
            drawableLeft.setBounds(0, 0, drawableLeft.getMinimumWidth(), drawableLeft.getMinimumHeight());
          }
        }
        viewHolder.name_tv.setCompoundDrawables(drawableLeft, null, drawableRight, null);
      }
      if (isSelect(position)) {
        viewHolder.selectBox_iv.setSelected(true);
      } else {
        viewHolder.selectBox_iv.setSelected(false);
      }
      viewHolder.selectBox_iv.setOnClickListener(selectOnClickListener(viewHolder.selectBox_iv, myFile));
    }

    return convertView;
  }

  private OnClickListener selectOnClickListener(final ImageView _imv, final MyFile _myFile) {
    return new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (_imv.isSelected()) {
          removeSelect(_myFile.fileName);
          _imv.setSelected(false);
        } else {
          addSelect(_myFile.fileName);
          _imv.setSelected(true);
        }

      }
    };
  };

  class ViewHolder {
    public TextView name_tv = null;
    public ImageView selectBox_iv = null;
  }

  public void setCurrentPlayFile(PlayFile _playFile) {
    currentPlayFile = _playFile;
  }

  public PlayFile getCurrentPlayFile() {
    return currentPlayFile;
  }

}
