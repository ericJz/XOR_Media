package com.zw.player.playerserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Log;

public class SearchLRC {
	private URL url;
	public static final String DEFAULT_LOCAL = "GB2312"; 
	StringBuffer sb = new StringBuffer(); 
	
	public SearchLRC(String musicName, String singerName) {
		musicName = musicName.replace(' ', '+'); 
		singerName = singerName.replace(' ', '+'); 
		String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" + musicName + "$$" + singerName+"$$$$"; 
		Log.d("test", strUrl); 
		try{
			url = new URL(strUrl); 
		}catch(Exception e1){
			e1.printStackTrace(); 
		}
		BufferedReader br = null; 
		
		String s ; 
		try { 
			InputStreamReader in = new InputStreamReader(url.openStream()); 
			Log.d("the encode is ", in.getEncoding()); 
			br = new BufferedReader(in); 
		} catch (IOException e1) { 
			Log.d("tag", "br is null"); 
		}	
		
		try{
			while ((s = br.readLine()) != null) { 
				sb.append(s + "\r\n"); 
				br.close(); 
			}
		} catch (IOException e) { 
				e.printStackTrace(); 
			}
		}
		
	public ArrayList<String> fetchLyric() { 
		int begin = 0, end = 0, number = 0;// number=0
		String strid = ""; 
		begin = sb.indexOf("<lrcid>"); 
		Log.d("test", "sb = " + sb); 
		if (begin != -1) { 
			end = sb.indexOf("</lrcid>", begin); 
			strid = sb.substring(begin + 7, end); 
			number = Integer.parseInt(strid); 
		}
		String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + number / 100 + "/" + number + ".lrc"; 
		Log.d("test", "geciURL = " + geciURL); 
		//ArrayList gcContent =new ArrayList(); 
		ArrayList<String> gcContent =new ArrayList<String>(); 
		String s = new String(); 
		try { 
			url = new URL(geciURL); 
		}catch (MalformedURLException e2) {
			e2.printStackTrace(); 
		}
		BufferedReader br = null; 
		try { 
			br = new BufferedReader(new InputStreamReader(url.openStream(), "GB2312")); 
		}catch (IOException e1) {
			e1.printStackTrace(); 
		}
		if (br == null) { 
			System.out.print("stream is null"); 			
		}else {
			try{
				while ((s = br.readLine()) != null) { 
//					Sentence sentence = new Sentence(s); 
					gcContent.add(s); 
				}
				br.close(); 
				}catch (IOException e) {
					e.printStackTrace(); 
				}
			}
		return gcContent; 
		}
	}	
