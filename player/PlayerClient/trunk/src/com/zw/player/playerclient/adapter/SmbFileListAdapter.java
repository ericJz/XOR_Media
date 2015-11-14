package com.zw.player.playerclient.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xormedia.mylib.HaveSelectBaseAdapter;
import com.xormedia.mylib.smb.MySmbFile;
import com.zw.player.playerclient.R;

public class SmbFileListAdapter extends HaveSelectBaseAdapter<MySmbFile>{

  public SmbFileListAdapter(Context _context, ArrayList<MySmbFile> _data, boolean _singleSelect, String _identifyAttrName, ArrayList<String> _seleteList) {
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
  public MySmbFile getItem(int position) {
    MySmbFile ret = null;
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
  public View getView( final int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder = null;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.smbfilelist_adapter, null);
      viewHolder.imv_type = (ImageView) convertView.findViewById(R.id.imv_sfla_type);
      viewHolder.imv_select = (ImageView) convertView.findViewById(R.id.imv_sfla_select);
      viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_sfla_filename);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    
    viewHolder.imv_select.setSelected(false);

    final MySmbFile mySmbFile = mData.get(position);
    if (mySmbFile != null) {
      if (mySmbFile.isFolder == true) {
        viewHolder.imv_type.setImageDrawable(mContext.getResources().getDrawable(R.drawable.smb_type_folder));
      } else {
        viewHolder.imv_type.setImageDrawable(mContext.getResources().getDrawable(R.drawable.smb_type_file));
      }
      
      if (isSelect(position)) {
        viewHolder.imv_select.setSelected(true);
      } else {
        viewHolder.imv_select.setSelected(false);
      }
      
      viewHolder.tv_name.setText(mySmbFile.fileName);
      viewHolder.imv_select.setOnClickListener(new OnClickListener() {
        
        @Override
        public void onClick(View v) {
          if (isSelect(position)) {
            removeSelect(mySmbFile.fileName);
            v.setSelected(false);
          } else {
            addSelect(mySmbFile.fileName);
            v.setSelected(true);
          }
        }
      });
    }
    return convertView;
  }

  class ViewHolder{
    public ImageView imv_type = null;
    public ImageView imv_select = null;
    public TextView tv_name = null;
  }
  
  private OnClickListener selectOnClickListener(final ImageView _imv, final int position,final MySmbFile mySmbFile) {
    return new OnClickListener() {
      
      @Override
      public void onClick(View v) {

        
      }
    };
  }
}
