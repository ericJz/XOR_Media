package com.zw.player.playerserver;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xormedia.mycontrolfortv.list.FocusAndSelectedAdapter;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.xormedia.mylib.fontsize.DisplayUtil;


public class SongListAdapter extends FocusAndSelectedAdapter<MyFile>{
	public static ArrayList<String> iconStatus = new ArrayList<String>();	
	private LayoutInflater mInflater = null;
	private final int maxSong = 100;
	
	public SongListAdapter(Context _context, ArrayList<MyFile> _data){
		super(_context,_data);	
		this.mInflater = LayoutInflater.from(_context);
	    for(int i=0;i<maxSong;i++){
	    	iconStatus.add("no");
	    }	    
	}
	
	public SongListAdapter(Context _context, ArrayList<MyFile> _data, String _identifyAttrName){
		super(_context,_data,_identifyAttrName);	
		this.mInflater = LayoutInflater.from(_context);
	    for(int i=0;i<maxSong;i++){
	    	iconStatus.add("no");
	    }	
	}
	
	  @Override
      public MyFile getItem(int position) {
		  MyFile ret=null;
		  if(mData != null && mData.size() > 0){
			  ret = mData.get(position);
		  }		  
          return ret;
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
            convertView = mInflater.inflate(R.layout.item_layout, null);
            holder.playIcon = (ImageView)convertView.findViewById(R.id.playIcon);
            holder.song = (TextView)convertView.findViewById(R.id.song);
           // holder.song.setTextSize(DisplayUtil.sp2px(20));
            convertView.setTag(holder);
        }else{
     	  holder = (ViewHolder)convertView.getTag();   
        }
          float size = DisplayUtil.sp2px((float)20);

		  MyFile myFile = getItem(position);
		  if(myFile != null){
			  holder.song.setText(myFile.fileName);
		  }
		  if(isFocus(position)){
			  holder.song.setTextColor(0xff333399);
		  }else{
			  holder.song.setTextColor(android.graphics.Color.WHITE);
		  }	
		  
		  holder.playIcon.setImageResource(R.drawable.playmusic);
			
		  if(iconStatus.get(position).compareTo("yes")==0){
			  holder.playIcon.setVisibility(View.VISIBLE);
		  }else{
			  holder.playIcon.setVisibility(View.INVISIBLE);			  
		  }

		  
		return convertView;
	}

	  public class ViewHolder
	  {
	      public ImageView playIcon;
	      public TextView song;
	  }
	
}
