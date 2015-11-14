package com.xormedia.mylib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.WindowManager;

public class MyTTS {
  private static Logger Log = Logger.getLogger(MyTTS.class);

  public static final int ERROR = TextToSpeech.ERROR;
  public static final int SUCCESS = TextToSpeech.SUCCESS;
  private static TextToSpeech mTts;
  private static Context mContext;
  private static String AppTitle = "";

  public MyTTS(Context context, String appTitle) {
    mContext = context;
    AppTitle = appTitle;
    setTTS();
  }

  public static int speak(String text, int queueMode, HashMap<String, String> params) {
    int ret = ERROR;
    if (mTts != null) {
      ret = mTts.speak(text, queueMode, params);
    }
    return ret;
  }

  private static void showNoticeDialog() {
    // 构造对话框
    if (mContext != null) {
      AlertDialog.Builder builder = new Builder(mContext);
      builder.setTitle("TTS引擎检查");
      builder.setMessage(AppTitle + "应用,必须安装TTS引擎才能正常工作。稍后将为您安装，安装完毕后请再次进入应用。谢谢！");
      // 更新
      builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
          try {
            InputStream is = mContext.getAssets().open("tts.apk");
            String fileName = "____tts.apk";
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
            if (file.exists() == false) {
              file.createNewFile();
              FileOutputStream fos = new FileOutputStream(file);
              byte[] temp = new byte[1024];
              int i = 0;
              while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
              }
              fos.close();
              is.close();
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(
                Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + fileName),
                "application/vnd.android.package-archive");
            mContext.startActivity(intent);
            System.exit(0);
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      });

      Dialog noticeDialog = builder.create();
      noticeDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
      noticeDialog.setCanceledOnTouchOutside(false);
      noticeDialog.show();
    }
  }

  private static void setTTS() {
    if (mContext != null) {
      mTts = new TextToSpeech(mContext, new OnInitListener() {
        @Override
        public void onInit(int status) {
          boolean find = false;

          if (status == TextToSpeech.SUCCESS) {
            List<EngineInfo> egs = mTts.getEngines();
            for (int i = 0; i < egs.size(); i++) {
              if (egs.get(i).name != null && egs.get(i).name.compareTo("com.iflytek.tts") == 0) {
                find = true;
                break;
              }
            }
          }

          if (status == TextToSpeech.ERROR || find == false) {
            new Handler(Looper.getMainLooper()).postAtFrontOfQueue(new Runnable() {

              @Override
              public void run() {
                showNoticeDialog();
              }
            });
          } else {
            mTts.setLanguage(Locale.CHINESE);
          }

        }
      }, "com.iflytek.tts");
    }
  }
}
