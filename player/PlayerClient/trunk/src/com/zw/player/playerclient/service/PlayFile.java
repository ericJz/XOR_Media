package com.zw.player.playerclient.service;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.cacheFileList.MyFile;

public class PlayFile extends MyFile {
  private static Logger Log = Logger.getLogger(PlayFile.class);
  public static final String META_STATUS = "playStatus";
  public static final String META_DOWNLOAD_PROGRESS = "downloadProgress";
  public static final String META_INDEX = "index";

  public static final String STATUS_PLAYING = "playing";
  public static final String STATUS_PAUSE = "pause";

  public int index = -1;
  public String playStatus = null;
  public int downloadProgress = 0;

  public PlayFile(JSONObject obj) {
    super(obj);
    if (obj != null) {
      try {
        if (obj.has(META_INDEX) == true) {
          index = obj.getInt(META_INDEX);
        }
        if (obj.has(META_STATUS) == true) {
          playStatus = obj.getString(META_STATUS);
        }
        if (obj.has(META_DOWNLOAD_PROGRESS) == true) {
          downloadProgress = obj.getInt(META_DOWNLOAD_PROGRESS);
        }
      } catch (JSONException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }


}
