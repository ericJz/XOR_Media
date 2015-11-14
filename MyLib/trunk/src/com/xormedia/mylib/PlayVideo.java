package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class PlayVideo implements OnBufferingUpdateListener, OnCompletionListener, MediaPlayer.OnPreparedListener, SurfaceHolder.Callback,
    OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnSeekCompleteListener{
  private static Logger Log = Logger.getLogger(PlayVideo.class);

  private int videoWidth;
  private int videoHeight;
  public int videoDuration;
  private MediaPlayer mediaPlayer;
  private SurfaceHolder surfaceHolder;
  private ProgressBar bufferProgressBar;
  private SurfaceView mSurfaceView;
  private Activity mActivity;
  /**
   * @see msg.what=0 播放视频到尾 <br/>
   * @see msg.what=1 播放失败
   * @see msg.what=2 播放开始
   * @see msg.what=3 播放缓冲开始
   * @see msg.what=4 播放缓冲结束
   */
  private Handler mHandler;

  private boolean isCreated = false;
  private String videoUrl = null;

  public PlayVideo(Activity activity, SurfaceView surfaceView, ProgressBar progressBar, Handler handler) {
    mActivity = activity;
    mHandler = handler;
    mSurfaceView = surfaceView;
    surfaceHolder = mSurfaceView.getHolder();
    surfaceHolder.addCallback(this);
    bufferProgressBar = progressBar;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    if (holder != surfaceHolder) {
      surfaceHolder = holder;
      if (mediaPlayer != null) {
        mediaPlayer.setDisplay(surfaceHolder);
      }
    }
    surfaceHolder.setFixedSize(width, height);
  }

  Handler handleProgress = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case 1:
          bufferProgressBar.setVisibility(View.VISIBLE);
          break;
        case 2:
          bufferProgressBar.setVisibility(View.GONE);
          break;
      }
    };
  };

  public void play() {
    if (mediaPlayer != null) {
      mediaPlayer.start();
    } else {
      mHandler.sendEmptyMessage(1);
    }
  }

  public void playUrl(final String _videoUrl) {
    videoUrl = _videoUrl;
    Log.info("videoUrl = " + videoUrl);
    new Thread() {
      @Override
      public void run() {
        try {
          if (!isCreated) {
            Thread.sleep(2000);
          }
          if (mediaPlayer != null) {
            handleProgress.sendEmptyMessage(1);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.setDisplay(mSurfaceView.getHolder());
            mediaPlayer.prepare();// prepare之后自动播放
          }
        } catch (Exception e) {
          ConfigureLog4J.printStackTrace(e, Log);
          if (e != null && e.getMessage() != null && e.getMessage().length() > 0) {
            mHandler.sendEmptyMessage(1);
          }
        }
      }
    }.start();
  }

  public boolean isPlaying() {
    boolean ret = false;
    if (mediaPlayer != null && mediaPlayer.isPlaying() == true) {
      ret = true;
    }
    return ret;
  }

  public void pause() {
    if (mediaPlayer != null && isPlaying()) {
      mediaPlayer.pause();
      handleProgress.sendEmptyMessage(2);
    }
  }

  public void release() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      handleProgress.sendEmptyMessage(2);
    }
  }

  public void stop() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
      handleProgress.sendEmptyMessage(2);
    }
  }

  public void seekTo(int time) {
    if (mediaPlayer != null) {
      mediaPlayer.seekTo(time);
    }
  }

  public int getCurrentPosition() {
    
    return (mediaPlayer == null) ? 0 : mediaPlayer.getCurrentPosition();
  }
  
  public int getDuration() {
    if (videoDuration == 0) {
      videoDuration = mediaPlayer.getDuration();
    }
    return videoDuration;
  }
  
  @Override
  public void surfaceCreated(SurfaceHolder arg0) {
    try {
      if (isCreated == false) {
        mediaPlayer = new MediaPlayer();
        // mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        isCreated = true;
      }
      surfaceHolder = arg0;
      mediaPlayer.setDisplay(surfaceHolder);
    } catch (Exception e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder arg0) {
    // stop();
    pause();
    surfaceHolder = null;
    if (mediaPlayer != null) {
      mediaPlayer.setDisplay(null);
    }
  }

  @Override
  /** 
   * 通过onPrepared播放 
   */
  public void onPrepared(MediaPlayer mp) {
    handleProgress.sendEmptyMessage(2);

    mHandler.sendEmptyMessage(2);
    videoWidth = mediaPlayer.getVideoWidth();
    videoHeight = mediaPlayer.getVideoHeight();

    boolean isM3U8 = false;
    if (videoUrl != null && videoUrl.length() > 0 && videoUrl.lastIndexOf(".") >= 0) {
      String tmp = videoUrl.substring(videoUrl.lastIndexOf(".") + 1);
      if (tmp.equals("m3u8") == true) {
        isM3U8 = true;
      }
    }
    if (isM3U8 == false) {
      if (videoHeight != 0 && videoWidth != 0) {
        mp.start();
      }
    } else {
      mp.start();
    }
    videoDuration = mp.getDuration();
    Log.info("Play Video duration = " + videoDuration);
  }

  @Override
  /**
   * 播放视频到尾
   */
  public void onCompletion(MediaPlayer mediaPlayer) {
    mHandler.sendEmptyMessage(0);
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int bufferingProgress) {

  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    switch (what) {
      case MediaPlayer.MEDIA_INFO_BUFFERING_START:
        handleProgress.sendEmptyMessage(1);
        mHandler.sendEmptyMessage(3);
        break;

      case MediaPlayer.MEDIA_INFO_BUFFERING_END:
        handleProgress.sendEmptyMessage(2);
        mHandler.sendEmptyMessage(4);
        videoDuration = mp.getDuration();
        Log.info("PlayVideo Duration = " + mp.getDuration());
        break;
    }
    return false;
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    String text = "";
    switch (what) {
      case MediaPlayer.MEDIA_ERROR_UNKNOWN:
      case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
        text += "视频文件加载失败";
        break;
    }
    switch (extra) {
      case MediaPlayer.MEDIA_ERROR_IO:
      case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
      case MediaPlayer.MEDIA_ERROR_MALFORMED:
        text += "视频文件加载失败";
        break;
      case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
        text += "视频文件加载超时";
        break;
      default:
        text += "播放失败";
        break;
    }
    handleProgress.sendEmptyMessage(2);
    Toast.makeText(mActivity, text, Toast.LENGTH_LONG).show();
    return false;
  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
   Log.info("playvideo width = "+ width + "height = "+height);
    
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
    Log.info("playvideo onSeekComplete ");
    
  }
}
