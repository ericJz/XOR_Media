package com.xormedia.mylib;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

public class MyAudioRecord {
  private static Logger Log = Logger.getLogger(MyAudioRecord.class);
  public static MediaRecorder mRecorder = null;
  public static File mRecAudioFile = null;
  public static Long startTime = null;

  public static class VoiceFile {
    public File mFile = null;
    public int fileLength = 0;
  }

  /**
   * 检验SDcard状态
   * 
   * @return boolean
   */
  public static boolean checkSDCard() {
    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
      return true;
    } else {
      return false;
    }
  }

  @SuppressLint("SimpleDateFormat")
  public static boolean startRecording(Context _context, String _fileNamePrefixes) {
    boolean ret = false;
    if (mRecorder != null) {
      try {
        mRecorder.stop();
        mRecorder.release();
      } catch (IllegalStateException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (RuntimeException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
      mRecorder = null;
      mRecAudioFile = null;
    }
    String filePath;
    if (checkSDCard()) {
      filePath = Environment.getExternalStorageDirectory().toString();
    } else {
      filePath = _context.getCacheDir().getAbsolutePath();
    }
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssS");
    String name = _fileNamePrefixes + "_" + format.format(new Date());

    filePath += File.separator + _context.getPackageName() + "/audio";

    File folder = new File(filePath);
    if (folder.exists() == false) {
      folder.mkdirs();
    }
    try {
      mRecAudioFile = File.createTempFile(name, ".caf", folder);
    } catch (IOException e) {
      ConfigureLog4J.printStackTrace(e, Log);
      Log.error("AudioRecord::mRec", e);
    }

    // instance
    mRecorder = new MediaRecorder();
    // 设置麦克风
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    // 输出文件格式
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    // 输出文件路径
    mRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
    // 音频文件编码
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    Log.info("AudioRecord::输出文件路径:" + mRecAudioFile.getAbsolutePath());

    // 准备--开始
    try {
      mRecorder.prepare();
      mRecorder.start();
      startTime = System.currentTimeMillis();
      ret = true;
    } catch (IllegalStateException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (RuntimeException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    } catch (IOException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  public static VoiceFile stopRecording() {
    Log.info("stopRecording()***Enter!");
    VoiceFile tmpFile = null;
    if (mRecorder != null) {
      tmpFile = new VoiceFile();
      tmpFile.mFile = mRecAudioFile;
      if (startTime != null) {
        tmpFile.fileLength = (int) (System.currentTimeMillis() - startTime) / 1000;
        startTime = null;
      }
      try {
        mRecorder.stop();
        mRecorder.release();
      } catch (IllegalStateException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (RuntimeException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
      mRecorder = null;
      mRecAudioFile = null;
    }
    Log.info("stopRecording()***Leave!");
    return tmpFile;
  }

  /**
   * 删除文件
   * 
   * @param _file
   * @return 0：删除成功，-1：删除失败
   */
  public static int deleteFile(File _file) {
    int ret = -1;
    if (_file != null && _file.exists() == true && _file.isFile() == true) {
      boolean isSucceed = _file.delete();
      if (isSucceed == true) {
        ret = 0;
      }
    }
    return ret;
  }

}
