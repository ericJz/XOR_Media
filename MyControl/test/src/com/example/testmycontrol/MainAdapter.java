package com.example.testmycontrol;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class MainAdapter extends BaseAdapter {
  private ArrayList<ButtonCommand> mData = null;
  private Context mContext = null;

  public MainAdapter(Context context, ArrayList<ButtonCommand> data) {
    mContext = context;
    mData = data;
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
  public Object getItem(int position) {
    ButtonCommand ret = null;
    if (mData != null) {
      ret = mData.get(position);
    }
    return ret;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @SuppressLint("InflateParams")
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext).inflate(
          R.layout.main_list_item, null);
    }
    Button button = (Button) convertView.findViewById(R.id.button1);
    button.setText(mData.get(position).name);
    button.setOnClickListener(mData.get(position).mOnClick);
    return convertView;
  }

  @Override
  protected void finalize() throws Throwable {
    if (mData != null) {
      mData.clear();
    }
    super.finalize();
  }
}
