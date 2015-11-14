package com.xormedia.mylib.media;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

import com.xormedia.mylib.ConfigureLog4J;

public class AudioPlayer {
  private static Logger Log = Logger.getLogger(AudioPlayer.class);

  public static final int MEDIA_PLAYER_STATUS_STOP = 0;// 调用reset()方法后，进入idle
  public static final int MEDIA_PLAYER_STATUS_PREPARING = 1;// 正在准备资源
  public static final int MEDIA_PLAYER_STATUS_PREPARED = 2;// 准备完成
  public static final int MEDIA_PLAYER_STATUS_PLAY = 3;// 正在播放
  public static final int MEDIA_PLAYER_STATUS_PAUSE = 4;// 调用pause()方法后，进入pause
  public static MediaPlayer mMediaPlayer;
  public static WeakReference<IPlayCallBack> mUserCallbackFunc;
  public static Context mContext;
  public static int mediaPlayerStatus;
  public static int willOption = 0;
  public static boolean inSeek = false;
  public static String dataSourceURL;

  public static void createMediaPlayer() {
    if (mMediaPlayer == null) {
      inSeek = false;
      mediaPlayerStatus = MEDIA_PLAYER_STATUS_STOP;
      mMediaPlayer = new MediaPlayer();
      synchronized (mMediaPlayer) {
        mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {

          @Override
          public void onPrepared(MediaPlayer mp) {
            mediaPlayerStatus = MEDIA_PLAYER_STATUS_PREPARED;
            if (mMediaPlayer != null) {
              synchronized (mMediaPlayer) {
                if (mMediaPlayer != null && mediaPlayerStatus >= MEDIA_PLAYER_STATUS_PREPARED && inSeek == false) {
                  if (willOption == MEDIA_PLAYER_STATUS_PAUSE) {
                    mMediaPlayer.pause();
                    mediaPlayerStatus = MEDIA_PLAYER_STATUS_PAUSE;
                  } else if (willOption == MEDIA_PLAYER_STATUS_PLAY) {
                    mMediaPlayer.start();
                    mediaPlayerStatus = MEDIA_PLAYER_STATUS_PLAY;
                  }
                  willOption = 0;
                }
              }
            }
            if (mUserCallbackFunc != null) {
              synchronized (mUserCallbackFunc) {
                if (mUserCallbackFunc != null) {
                  IPlayCallBack tmp = mUserCallbackFunc.get();
                  if (tmp != null) {
                    tmp.playPrepared(mp);
                  } else {
                    mUserCallbackFunc = null;
                  }
                }
              }
            }
          }
        });
        mMediaPlayer.setOnErrorListener(new OnErrorListener() {

          @Override
          public boolean onError(MediaPlayer mp, int what, int extra) {
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
            Log.info("errMsg=" + errMsg);
            stop();
            if (mUserCallbackFunc != null) {
              synchronized (mUserCallbackFunc) {
                if (mUserCallbackFunc != null) {
                  IPlayCallBack tmp = mUserCallbackFunc.get();
                  if (tmp != null) {
                    tmp.playError(mp, what, extra, errMsg);
                  } else {
                    mUserCallbackFunc = null;
                  }
                }
              }
            }
            return false;
          }
        });
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

          @Override
          public void onCompletion(MediaPlayer mp) {
            mMediaPlayer.seekTo(0);
            inSeek = true;
            willOption = MEDIA_PLAYER_STATUS_PAUSE;
            if (mUserCallbackFunc != null) {
              synchronized (mUserCallbackFunc) {
                if (mUserCallbackFunc != null) {
                  IPlayCallBack tmp = mUserCallbackFunc.get();
                  if (tmp != null) {
                    tmp.playFinish(mp);
                  } else {
                    mUserCallbackFunc = null;
                  }
                }
              }
            }
          }
        });
        mMediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {

          @Override
          public void onSeekComplete(MediaPlayer mp) {
            inSeek = false;
            if (mMediaPlayer != null) {
              synchronized (mMediaPlayer) {
                if (mMediaPlayer != null && mediaPlayerStatus >= MEDIA_PLAYER_STATUS_PREPARED) {
                  if (willOption == MEDIA_PLAYER_STATUS_PAUSE) {
                    mMediaPlayer.pause();
                    mediaPlayerStatus = MEDIA_PLAYER_STATUS_PAUSE;
                  } else if (willOption == MEDIA_PLAYER_STATUS_PLAY) {
                    mediaPlayerStatus = MEDIA_PLAYER_STATUS_PLAY;
                  }
                  willOption = 0;
                }
              }
            }
          }
        });
      }
    }
  }

  public AudioPlayer(Context context) {
    mContext = context;
  }

  public static void setUserCallbackFunc(IPlayCallBack _playCallBack) {
    if (mUserCallbackFunc == null) {
      if (_playCallBack != null) {
        mUserCallbackFunc = new WeakReference<IPlayCallBack>(_playCallBack);
      }
    } else {
      synchronized (mUserCallbackFunc) {
        if (_playCallBack != null) {
          mUserCallbackFunc = new WeakReference<IPlayCallBack>(_playCallBack);
        } else {
          mUserCallbackFunc = null;
        }
      }
    }
  }

  public static boolean setDataSource(String url, boolean isLooping) {
    boolean ret = false;
    if (url != null && url.length() > 0) {
      stop();
      createMediaPlayer();
      synchronized (mMediaPlayer) {
        dataSourceURL = url;
        try {
          mMediaPlayer.setDataSource(url);
          // mMediaPlayer.setLooping(isLooping);
          mMediaPlayer.prepareAsync();
          mediaPlayerStatus = MEDIA_PLAYER_STATUS_PREPARING;
          ret = true;
        } catch (IllegalArgumentException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } catch (SecurityException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } catch (IllegalStateException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
        if (ret == false) {
          mMediaPlayer.reset();
        }
      }
    }
    return ret;
  }

  public static boolean play(String url, boolean isLooping) {
    boolean ret = setDataSource(url, isLooping);
    if (ret == true) {
      ret = play();
    }
    return ret;
  }

  public static boolean pause(String url, boolean isLooping) {
    boolean ret = setDataSource(url, isLooping);
    if (ret == true) {
      ret = pause();
    }
    return ret;
  }

  public static boolean play() {
    boolean ret = false;
    if (mMediaPlayer != null) {
      synchronized (mMediaPlayer) {
        if (mMediaPlayer != null && mediaPlayerStatus > 0) {
          if (mediaPlayerStatus >= MEDIA_PLAYER_STATUS_PREPARED && inSeek == false) {
            mMediaPlayer.start();
            mediaPlayerStatus = MEDIA_PLAYER_STATUS_PLAY;
          } else {
            willOption = MEDIA_PLAYER_STATUS_PLAY;
          }
        }
      }
    }
    return ret;
  }

  public static boolean pause() {
    boolean ret = false;
    if (mMediaPlayer != null) {
      synchronized (mMediaPlayer) {
        if (mMediaPlayer != null && mediaPlayerStatus > 0) {
          if (mediaPlayerStatus >= MEDIA_PLAYER_STATUS_PREPARED) {
            mMediaPlayer.pause();
            mediaPlayerStatus = MEDIA_PLAYER_STATUS_PAUSE;
          } else {
            willOption = MEDIA_PLAYER_STATUS_PAUSE;
          }
        }
      }
    }
    return ret;
  }

  public static boolean stop() {
    boolean ret = false;
    if (mMediaPlayer != null) {
      synchronized (mMediaPlayer) {
        if (mediaPlayerStatus > 0) {
          mMediaPlayer.stop();
          mMediaPlayer.release();
        }
        mMediaPlayer = null;
      }
    }
    inSeek = false;
    dataSourceURL = null;
    mediaPlayerStatus = MEDIA_PLAYER_STATUS_STOP;
    willOption = 0;
    return ret;
  }

  public static int getCurrentPosition() {
    int ret = 0;
    if (mMediaPlayer != null) {
      synchronized (mMediaPlayer) {
        if (mMediaPlayer != null) {
          ret = mMediaPlayer.getCurrentPosition();
        }
      }
    }
    return ret;
  }

  public static int getDuration() {
    int ret = 0;
    if (mMediaPlayer != null) {
      synchronized (mMediaPlayer) {
        if (mMediaPlayer != null) {
          ret = mMediaPlayer.getDuration();
        }
      }
    }
    return ret;
  }
}
