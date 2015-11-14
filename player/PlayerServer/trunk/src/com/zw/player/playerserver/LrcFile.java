package com.zw.player.playerserver;

import java.util.ArrayList;

public class LrcFile {
	
	private String lrcLine = null;
	private	float lrcTime = 0;
	private ArrayList<String> lrcs;
	private float[] times;
	
	public LrcFile(String _lrcLine, float _lrcTime){
		lrcLine = _lrcLine;
		lrcTime = _lrcTime;
	}

//	public float getLrcTime(){
//		return lrcTime;
//	}
//	
//	public String getLrcLine(){
//		return lrcLine;
//	}
//	
//	public void setLrcTime(float time){
//		lrcTime = time;
//	}
//	
//	public void setLrcLine(String line){
//		lrcLine = line;
//	}
	
	public void lrcTreatment(ArrayList<String> arr){
		//把歌词和时间分离，赋给数组 lrs 和 times
	}
	
}
