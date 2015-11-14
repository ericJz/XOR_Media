package com.xormedia.mylib.media;

import android.media.MediaPlayer;

public interface IPlayCallBack {
  /**
   * 播放到尾时回调方法
   */
  public void playFinish(MediaPlayer mp);

  /**
   * 播放音频出错时回调方法
   * 
   * @param _errMsg
   *          错误信息，String
   */
  public void playError(MediaPlayer mp, int what, int extra,String errorMsg);

  /**
   * 资源准备完成，开始播放之前(start()之前)回调方法
   */
  public void playPrepared(MediaPlayer mp);
}
