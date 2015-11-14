package com.zw.player.playerserver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
//import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xormedia.mycontrolfortv.list.MyListView;
import com.xormedia.mylib.cacheFileList.MyFile;
import com.xormedia.mylib.handler.WeakHandler;
import com.xormedia.mylib.media.AudioPlayer;
import com.xormedia.socket.MySocket;
import com.xormedia.socket.SocketMessage;
import com.zw.player.playerserver.service.MyPlayer;
import com.zw.player.playerserver.service.SocketServer;

public class MainActivity extends Activity {
  private static Logger Log = Logger.getLogger(MainActivity.class);
  private TextView logTxt = null;
  private boolean isClicked = true;
  private boolean isLrc = false;
  private MyListView mlv;
  private TextView lrctext;
  private ArrayList<MyFile> arr;
  private ImageView imbutton;
  private ImageView button_next;
  private ImageView button_previous;
  private ImageView button_switch;
  private SongListAdapter  sla;
  private TextView progress = null;
  private int songIndex = 0;
  private int buttonIndex = 1;
  private static int currentIndex = 0;
  private SeekbarView seekbarview;
  private ImageView cd = null;
  private Animation rot = null;
  private int xiazaiprogress = 0;
  private String mysong = "怒放的生命";
  private String singer = "汪峰";
  private ScrollView lrcView;
  private boolean switchIndex = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    logTxt = (TextView) findViewById(R.id.log);
    if (SocketServer.newChannelConnect == null) {
      SocketServer.newChannelConnect = new WeakHandler(newChannelConnectHandler);
    } else {
      SocketServer.newChannelConnect.setHandler(newChannelConnectHandler);
    }
    if (SocketServer.channelClose == null) {
      SocketServer.channelClose = new WeakHandler(channelCloseHandler);
    } else {
      SocketServer.channelClose.setHandler(channelCloseHandler);
    }
    if (SocketServer.processHandler == null) {
      SocketServer.processHandler = new WeakHandler(processHandler);
    } else {
      SocketServer.processHandler.setHandler(processHandler);
    }
    MyPlayer.setFileListChangedHandler(filelistChangedHander);
    MyPlayer.setCurrentPlayFileChangedHandler(currentSongIconHandler);
    MyPlayer.setCurrentStatusChangedHandler(iconStatusHandler);
    MyPlayer.setCurrentPlayFileDownloadHandler(currentSongDownloadHandler);
        
    lrcView = (ScrollView)findViewById(R.id.lrcview);
    lrctext = (TextView)findViewById(R.id.lrctext);
    imbutton = (ImageView)findViewById(R.id.play);
    button_switch = (ImageView)findViewById(R.id.haha);
    button_next = (ImageView)findViewById(R.id.mynext);
    button_next.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v){
    		if((currentIndex+1)<MyPlayer.getNameList().size()){
    			currentIndex+=1;
    			MyPlayer.play(currentIndex);
    		}
    	  }
      });
    
    button_previous = (ImageView)findViewById(R.id.last);
    button_previous.setOnClickListener(new OnClickListener() {
    	@Override
    	public void onClick(View v){
    		if(currentIndex>0){
    			currentIndex-=1;
    			MyPlayer.play(currentIndex);
    		}    		
    	  }
      });
      
    imbutton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
    	if(isClicked){  
    		MyPlayer.play();
    	}else{
    		MyPlayer.pause();
    	}
      }
    });
    
    button_switch.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!isLrc){
				mlv.setVisibility(View.GONE);
				lrcView.setVisibility(View.VISIBLE);
			}else{
				lrcView.setVisibility(View.GONE);
				mlv.setVisibility(View.VISIBLE);
			}
			isLrc = !isLrc;
		}
	});
    
    arr = MyPlayer.getNameList();

    progress = (TextView)findViewById(R.id.progress);

    
    seekbarview = (SeekbarView)findViewById(R.id.myseekbar);
    seekbarview.setColors(0x00cccccc, 0x33bfbfbf);
    changeSeekbar();
        
    mlv = (MyListView)findViewById(R.id.mylistview);
    sla = new SongListAdapter(this,arr,"fileName");
    mlv.setAdapter(sla);
    mlv.setFocusItem(MyPlayer.getNameList().get(songIndex).fileName,true);
    cd = (ImageView)findViewById(R.id.cd);
    rot = AnimationUtils.loadAnimation(this, R.anim.rotate); 
    LinearInterpolator lin = new LinearInterpolator(); 
    rot.setInterpolator(lin);  
     
    new Thread(lrcTask).start();
  }
   
  public boolean onKeyDown(int keyCode, KeyEvent event){
	  
	  if(switchIndex){
		  button_switch.setBackgroundResource(R.drawable.switchfocus);
		  if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER||keyCode == KeyEvent.KEYCODE_ENTER){
			  button_switch.performClick();
		  }
		  if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			  switchIndex = false;
			  button_switch.setBackgroundResource(R.drawable.lrcswitch);
			  songIndex = 0;
			  mlv.setFocusItem(MyPlayer.getNameList().get(0).fileName,true);
		  }
	  }else{
		  button_switch.setBackgroundResource(R.drawable.lrcswitch);
		if(songIndex < MyPlayer.getNameList().size()){
			if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
				imbutton.setBackgroundResource(R.drawable.mypause);
			}else{
				imbutton.setBackgroundResource(R.drawable.myplay);
				}
			button_previous.setBackgroundResource(R.drawable.myprevious);
			button_next.setBackgroundResource(R.drawable.mynext);
	  	//按向下键
	  	if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			  button_switch.setBackgroundResource(R.drawable.lrcswitch);
	  		  switchIndex = false;
			  songIndex += 1;
			  if(songIndex>=MyPlayer.getNameList().size()){
				mlv.lostFocusItem(true);
				if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
					imbutton.setBackgroundResource(R.drawable.mypausefocus);
				}else{
					imbutton.setBackgroundResource(R.drawable.myplayfocus);
				}
			  }else{
			  mlv.lostFocusItem(true);
			  mlv.setFocusItem(MyPlayer.getNameList().get(songIndex).fileName,true);	  		
			  }
	  	}
	  	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER||keyCode == KeyEvent.KEYCODE_ENTER){
			  currentIndex = songIndex;
			  MyPlayer.play(currentIndex);
	  	}
	  	//按向上键
	  	if(keyCode == KeyEvent.KEYCODE_DPAD_UP && songIndex>=0){
			  songIndex -= 1;			  
			  if(songIndex>=0){
			  mlv.lostFocusItem(true);
			  if(songIndex<MyPlayer.getNameList().size()){
				  mlv.setFocusItem(MyPlayer.getNameList().get(songIndex).fileName,true);		
			  	}
			  }else{
				  mlv.lostFocusItem(true);
				  button_switch.setBackgroundResource(R.drawable.switchfocus);
				  switchIndex = true;
			  }
	  	}
		}else{
	  		//left
	  		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT && buttonIndex > 0){
	  			buttonIndex -= 1;
	  		}
	  		//right
	  		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && buttonIndex <2){
	  			buttonIndex += 1;
	  		}

	  		if(keyCode == 19){
	  			songIndex -= 1;
	  			buttonIndex = 1;
  				button_previous.setBackgroundResource(R.drawable.myprevious);
  				button_next.setBackgroundResource(R.drawable.mynext);	
//	  			if(songIndex == (MyPlayer.getNameList().size()-1)){
	  				mlv.setFocusItem(MyPlayer.getNameList().get(songIndex).fileName,true);
	  				if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
	  					imbutton.setBackgroundResource(R.drawable.mypause);
	  				}else{
	  					imbutton.setBackgroundResource(R.drawable.myplay);
	  					}
  				
//	  			}
	  		}
	  		//ok
	  		if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER||keyCode == KeyEvent.KEYCODE_ENTER){
	  			switch(buttonIndex){
	  			case 0: button_previous.performClick();
	  					break;
	  			case 1: imbutton.performClick();
	  					break;
	  			case 2: button_next.performClick();
	  					break;
	  			default:Toast toastButton = Toast.makeText(MainActivity.this, "buttonIndex "+buttonIndex, 2000);	
	  					toastButton.show();
	  					break;		
	  			}
	  		}
	  		
	  		if(buttonIndex == 1 && songIndex != (MyPlayer.getNameList().size()-1)){
	  			if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
	  				imbutton.setBackgroundResource(R.drawable.mypausefocus);
	  			}else{
	  				imbutton.setBackgroundResource(R.drawable.myplayfocus);
	  			}
				button_previous.setBackgroundResource(R.drawable.myprevious);
				button_next.setBackgroundResource(R.drawable.mynext);
	  		}
	  		if(buttonIndex == 0){
	  			button_previous.setBackgroundResource(R.drawable.mypreviousfocus);
	  			if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
	  				imbutton.setBackgroundResource(R.drawable.mypause);
	  			}else{
	  				imbutton.setBackgroundResource(R.drawable.myplay);
	  			}
				button_next.setBackgroundResource(R.drawable.mynext);
	  		}
	  		if(buttonIndex == 2){
	  			button_next.setBackgroundResource(R.drawable.mynextfocus);
	  			if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
	  				imbutton.setBackgroundResource(R.drawable.mypause);
	  			}else{
	  				imbutton.setBackgroundResource(R.drawable.myplay);
	  			}
				button_previous.setBackgroundResource(R.drawable.myprevious);
	  		}
	  	}
	  }	
	  return super.onKeyDown(keyCode, event);    
  }

  
  private Handler filelistChangedHander = new Handler(new Callback() {

    @Override
    public boolean handleMessage(Message msg) {   
	 	ArrayList<MyFile> newarr = MyPlayer.getNameList();
	    arr.clear();
	    arr.addAll(newarr);
	    newarr.clear();
	    //mMyAdapter.notifyDataSetChanged();    
	    sla.notifyDataSetChanged();
    	return false;
    }
  });

  private Handler currentSongIconHandler = new Handler(new Callback() {

	    @Override
	    public boolean handleMessage(Message msg) {
	    	
	    	if (msg != null && msg.obj != null) {
  					MyFile song = (MyFile) msg.obj;	
	    		for(int i=0;i<arr.size();i++){
	    			if(arr.get(i).fileName.equals(song.fileName)){
	    				SongListAdapter.iconStatus.set(i, "yes");
	    				
//	    				int index = song.fileName.indexOf("-");
//	    				int songLength = song.fileName.length();
//	    				if(index >= 0 && (songLength-4)>=(index+1)){
//	    				singer = song.fileName.substring(0, index);
//	    				mysong = song.fileName.substring(index+1, songLength-4);
//	    				}
//
//	    				new Thread(lrcTask).start();
	    				
	    			}else{
	    				SongListAdapter.iconStatus.set(i, "no");
	    			}
	    		}
	    		Toast mytoast = Toast.makeText(MainActivity.this, song.fileName+" 正在播放", 2000);	
	    		mytoast.show();
	    	}
	    	sla.notifyDataSetChanged();
	      return false;
	    }
	  }); 
  
  private Handler currentSongDownloadHandler = new Handler(new Callback(){
	  @Override
	  public boolean handleMessage(Message msg) {
		  if (msg != null && msg.obj != null) {
			  MyFile song = (MyFile) msg.obj;
			  progress.setText(song.fileName+" "+song.downloadProgress);	
			  if(song.downloadProgress!=100){

				  seekbarview.setPlayColor(0x00cccccc,0x00cccccc);
				  seekbarview.setColors(0x00cccccc, 0x33bfbfbf);
				  xiazaiprogress = song.downloadProgress;
				  float a = (float)xiazaiprogress;
				  float b = 100;
				  float ratio = a/b;
				  
				  seekbarview.setProgressNotInUiThread(ratio, xiazaiprogress);
				  				  
			  }else{
				  seekbarview.setProgressNotInUiThread(1, xiazaiprogress);
				  seekbarview.setPlayColor(0xff6699ff,0x00cccccc);
				  xiazaiprogress = 100;}

		  }
		  return false;		
	  }
  });
    
  private Handler iconStatusHandler = new Handler(new Callback(){
	  @Override
	  public boolean handleMessage(Message msg){
		  if (msg != null && msg.obj != null) {
			  if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PLAYING)==0){
				  if(songIndex>=MyPlayer.getNameList().size() && buttonIndex == 1){
					  imbutton.setBackgroundResource(R.drawable.mypausefocus);
				  }else{
					  imbutton.setBackgroundResource(R.drawable.mypause);
				  }
				  isClicked = false;
				  MyFile song = (MyFile) msg.obj;
				  if(song.fileName.compareTo(arr.get(0).fileName)==0){
					  SongListAdapter.iconStatus.set(0, "yes");
				  }
				  cd.startAnimation(rot);
			  }else if(MyPlayer.getCurrentStatus().compareTo(MyPlayer.STATUS_PAUSE)==0)
			  {
				  if(songIndex>=MyPlayer.getNameList().size() && buttonIndex == 1){
					  imbutton.setBackgroundResource(R.drawable.myplayfocus);
				  }else{
					  imbutton.setBackgroundResource(R.drawable.myplayfocus);
				  }
				  isClicked = true;
				  cd.clearAnimation();
			  }
		  }
		  sla.notifyDataSetChanged();
		  return false;
	  }
  });
  
  private Handler processHandler = new Handler(new Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        SocketMessage msg1 = (SocketMessage) msg.obj;
        //logTxt.setText(logTxt.getText() + "\n接收" + msg1.toIP + "第[" + msg1.CSeq + "]条请求返回：" + msg1.content.toString());
      }
      return false;
    }
  });

  private Handler newChannelConnectHandler = new Handler(new Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        MySocket socket = (MySocket) msg.obj;
        //logTxt.setText(logTxt.getText() + "\n" + socket.toIPAddress + "连接！");
      }
      return false;
    }
  });

  private Handler channelCloseHandler = new Handler(new Callback() {

    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        MySocket socket = (MySocket) msg.obj;
        //logTxt.setText(logTxt.getText() + "\n" + socket.toIPAddress + "断开！");
      }
      return false;
    }
  });
  
  public void changeSeekbar(){
	  new Handler().postDelayed(new Runnable() {
		
		@Override
		public void run() {
			if(xiazaiprogress==100){
	    	  float position = AudioPlayer.getCurrentPosition();
	    	  float time = AudioPlayer.getDuration();
	    	  //int max = seekbar.getMax();
	    	  if(time!=0){
	    		  float ratio = position/time;
	    		  seekbarview.setProgressNotInUiThread(ratio,position,time); 
	    	  }else{
	    		  seekbarview.setProgressNotInUiThread(0,0,0);
	    	  }   
			}
	    	  changeSeekbar();
		}
	}, 1000);
  }	
  
   
  Handler lrcHandler = new Handler(){
	  @Override
	  public void handleMessage(Message msg){
		  if (msg != null && msg.obj != null){
			  ArrayList<String> list = (ArrayList<String>)msg.obj;
			  String lrc = "";
			  for(int i =0;i<list.size();i++){
				  lrc = lrc+list.get(i)+"\n";
			  }
			  lrctext.setText(lrc);
			  Toast toastlrc = Toast.makeText(MainActivity.this, "lrc downloaded", 2000);	
			  toastlrc.show();
		  }else{
			  Toast toastlrc = Toast.makeText(MainActivity.this, "歌词加载失败", 2000);	
			  toastlrc.show();
		  }
	  }  
  }; 
  
  Runnable lrcTask = new Runnable() {  
	  
	    @Override  
	    public void run() {  
	        // TODO  
	        // 在这里进行 http request.网络请求相关操作  
	        Message msg = new Message();  

	        try {
				mysong = URLEncoder.encode(mysong.trim(), "utf-8");
				singer = URLEncoder.encode(singer.trim(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        SearchLRC search = new SearchLRC(mysong,singer); 
	        ArrayList<String> result = search.fetchLyric(); 
	        msg.obj = result;
	        lrcHandler.sendMessage(msg);  
	    }  
	};  
	 
}
