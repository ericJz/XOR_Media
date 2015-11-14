package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public class PlayAudio {
  private static Logger Log = Logger.getLogger(PlayAudio.class);

  public static final String MEDIA_PLAYER_STATUS_IDLE = "idle";// 调用reset()方法后，进入idle
  public static final String MEDIA_PLAYER_STATUS_INITALIZED = "initialized";// 调用setDataSource()方法后，进入initialized
  public static final String MEDIA_PLAYER_STATUS_PREPARED = "prepared";// 调用prepare()方法后，进入prepared
  public static final String MEDIA_PLAYER_STATUS_STARTED = "started";// 调用start()方法后，进入started
  public static final String MEDIA_PLAYER_STATUS_PAUSE = "pause";// 调用pause()方法后，进入pause
  public static final String MEDIA_PLAYER_STATUS_STOPPED = "stopped";// 调用stop()方法后，进入stopped
  public static final String MEDIA_PLAYER_STATUS_COMPLETED = "completed";// 音频播放完毕时，进入completed。OnCompletionListener返回
  public static final String MEDIA_PLAYER_STATUS_END = "end";// 调用release()方法后，进入end
  public static final String MEDIA_PLAYER_STATUS_ERROR = "error";// 出错时，进入error。OnErrorListener返回
  public static String mediaPlayerStatus = null;

  public static MediaPlayer mMediaPlayer = null;
  public static playCallBack mCcallbackFunc = null;

  /**
   * 调用MediaPlayer，自定义回调接口
   */
  public static interface playCallBack {
    /**
     * 播放到尾时回调方法
     */
    public void playFinish();

    /**
     * 播放音频出错时回调方法
     * 
     * @param _errMsg
     *          错误信息，String
     */
    public void playError(String _errMsg);

    /**
     * 资源准备完成，开始播放之前(start()之前)回调方法
     */
    public void playPrepared();
  }

  /**
   * 开始播放
   * 
   * @param _context
   * @param _url
   *          音频播放路径
   * @param _isLooping
   *          是否循环播放音频。true循环播放，false不循环播放
   * @param _callback
   *          回调函数
   */
  public static void startPlaying(final Context _context, final String _url, final boolean _isLooping, playCallBack _callback) {
    if (_context != null && _url != null && _url.length() > 0 && _callback != null) {
      mCcallbackFunc = _callback;
      try {
        if (mMediaPlayer == null) {
          mMediaPlayer = new MediaPlayer();
        }
        mMediaPlayer.reset();
        mediaPlayerStatus = MEDIA_PLAYER_STATUS_IDLE;
        Uri uri = Uri.parse(_url);
        mMediaPlayer.setDataSource(_context, uri);
        mediaPlayerStatus = MEDIA_PLAYER_STATUS_INITALIZED;
        mMediaPlayer.setLooping(_isLooping);
        mMediaPlayer.prepare();
        mediaPlayerStatus = MEDIA_PLAYER_STATUS_PREPARED;
        mMediaPlayer.start();
        mediaPlayerStatus = MEDIA_PLAYER_STATUS_STARTED;

        mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
          public void onPrepared(MediaPlayer mp) {
            if (mCcallbackFunc != null) {
              mCcallbackFunc.playPrepared();
            }
          }
        });

        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
          public void onCompletion(MediaPlayer mp) {
            mediaPlayerStatus = MEDIA_PLAYER_STATUS_COMPLETED;
            if (mCcallbackFunc != null) {
              mCcallbackFunc.playFinish();
            }
            if (_isLooping == false) {
              stopPlaying();
            }
          }
        });

        mMediaPlayer.setOnErrorListener(new OnErrorListener() {
          public boolean onError(MediaPlayer mp, int what, int extra) {
            boolean ret = false;
            mediaPlayerStatus = MEDIA_PLAYER_STATUS_ERROR;
            String errMsg = null;
            if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
              errMsg = "Unspecified media player error.";
            } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
              errMsg = "Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.";
            } else if (extra == MediaPlayer.MEDIA_ERROR_IO) {
              errMsg = "File or network related operation errors.";
            } else if (extra == MediaPlayer.MEDIA_ERROR_MALFORMED) {
              errMsg = "Bitstream is not conforming to the related coding standard or file spec.";
            } else if (extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED) {
              errMsg = "Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the feature.";
            } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {
              errMsg = "Some operation takes too long to complete, usually more than 3-5 seconds.";
            } else {
              errMsg = "无法播放该音频";
            }
            if (mCcallbackFunc != null && errMsg != null && errMsg.length() > 0) {
              mCcallbackFunc.playError(errMsg);
            }
            stopPlaying();
            return ret;
          }
        });
      } catch (Exception e) {
        ConfigureLog4J.printStackTrace(e, Log);
        if (e != null && e.getMessage() != null && e.getMessage().length() > 0 && mCcallbackFunc != null) {
          mCcallbackFunc.playError(e.getMessage());
        }
        stopPlaying();
      }
    }
  }

  /**
   * 暂停播放
   */
  public static void pausePlaying() {
    if (mMediaPlayer != null && mediaPlayerStatus != null && mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_STARTED) == true) {
      mMediaPlayer.pause();
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_PAUSE;
    }
  }

  /**
   * 继续播放
   */
  public static void continuePlaying() {
    if (mMediaPlayer != null && mediaPlayerStatus != null && mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_PAUSE) == true) {
      mMediaPlayer.start();
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_STARTED;
    }
  }

  /**
   * 重新播放
   */
  public static void againPlaying() {
    if (mMediaPlayer != null
        && mediaPlayerStatus != null
        && (mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_COMPLETED) == true
            || mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_STOPPED) == true || mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_ERROR) == true)) {
      mMediaPlayer.start();
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_STARTED;
    }
  }

  /**
   * 停止播放，释放资源，销毁MediaPlayer
   */
  public static void stopPlaying() {
    if (mMediaPlayer != null && mediaPlayerStatus != null
        && ((mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_STARTED) == true
            || mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_COMPLETED) == true 
            || mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_ERROR) == true)
            || mediaPlayerStatus.equals(MEDIA_PLAYER_STATUS_PAUSE) == true)) {
      mMediaPlayer.stop();
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_STOPPED;
      mMediaPlayer.release();
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_END;
    }
    mMediaPlayer = null;
    mediaPlayerStatus = null;
  }
}
