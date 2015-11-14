package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
  private static Logger Log = Logger.getLogger(MyWebViewClient.class);

  public String activityName = null;
  public MyWebViewClient(String _activityName) {
    activityName = _activityName;
  }

  @Override
  public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    super.onReceivedError(view, errorCode, description, failingUrl);
    Log.info("errorCode = " + errorCode);
  }

  // 控制新的连接在当前WebView中打开
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    Log.info("shouldOverrideUrlLoading url = " + url);
    boolean ret = true;
    if (view != null && url != null) {
      if (url.equals("conf://exit")) {
        if (activityName != null) {
          SingleActivityPageManager manager = ActivityPageManager.getSingleActivityPageManagerByName(activityName);
          if (manager != null) {
            manager.back();
          }
        }
      } else {
        view.loadUrl(url);
      }
    } else {
      ret = super.shouldOverrideUrlLoading(view, url);
    }

    return ret;
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    Log.info("MyWebViewClient onPageFinished url = " + url);
    super.onPageFinished(view, url);
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    Log.info("MyWebViewClient onPageStarted url = " + url);
    super.onPageStarted(view, url, favicon);
  }

}
