package com.zw.player.playerserver;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xormedia.mycontrolfortv.list.FocusAndSelectedAdapter;
import com.xormedia.mylib.fontsize.DisplayUtil;


public class LrcAdapter extends FocusAndSelectedAdapter<String>{
	private LayoutInflater mInflater = null;
	
	public LrcAdapter(Context _context, ArrayList<String> _data){
		super(_context,_data);	
		this.mInflater = LayoutInflater.from(_context);    
	}
	
	public LrcAdapter(Context _context, ArrayList<String> _data, String _identifyAttrName){
		super(_context,_data,_identifyAttrName);	
		this.mInflater = LayoutInflater.from(_context);
	}
	
	  @Override
      public String getItem(int position) {
		  String line=null;
		  if(mData != null && mData.size() > 0){
			  line = mData.get(position);
		  }		  
          return line;
      }
	  
	  @Override
	  public long getItemId(int position) {
		  return position;
	  }	  
	
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent){
		ViewHolder holder = null;
		  if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.lrc_line_layout, null);
            holder.lrcs = (TextView)convertView.findViewById(R.id.myline);
            convertView.setTag(holder);
        }else{
     	  holder = (ViewHolder)convertView.getTag();   
        }
		  String myLine = getItem(position);
		  if(myLine != null){
			  holder.lrcs.setText(myLine);
		  }
		return convertView;
	}

	  public class ViewHolder
	  {
	      public TextView lrcs;
	  }
	
}

