package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class MyWebChromeClient extends WebChromeClient {
  private static Logger Log = Logger.getLogger(MyWebChromeClient.class);

  private ProgressBar mProgressBar = null;

  public MyWebChromeClient(ProgressBar _progressBar) {
    Log.info("MyWebChromeClient");
    mProgressBar = _progressBar;
  }

  @Override
  public void onProgressChanged(WebView view, int newProgress) {
    Log.info("new Progress = "+newProgress);
    if (mProgressBar != null) {
      if (newProgress == 100) {
        mProgressBar.setVisibility(View.GONE);
      } else {
        if (mProgressBar.getVisibility() == View.GONE) {
          mProgressBar.setVisibility(View.VISIBLE);
        }
        mProgressBar.setProgress(newProgress);
      }
    }

    super.onProgressChanged(view, newProgress);
  }

}
