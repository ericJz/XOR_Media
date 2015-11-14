package com.example.testmycontrol.floatview;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.testmycontrol.R;
import com.xormedia.mycontrol.floatview.FloatViewTouchListener;
import com.xormedia.mylib.MyToast;

public class HomePage extends Fragment {
  private static Logger Log = Logger.getLogger(HomePage.class);

  private Context mContext = null;

  private ImageView homeShakeButton_iv = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    Activity main = getActivity();
    if (main != null) {
      if (main.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
        main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      }
    }
    mContext = container.getContext();

    View mView = inflater.inflate(R.layout.home, container, false);

    homeShakeButton_iv = (ImageView) mView.findViewById(R.id.homeShakeButton_iv);
    homeShakeButton_iv.setOnTouchListener(new FloatViewTouchListener(mContext, (View) homeShakeButton_iv.getParent()));
    homeShakeButton_iv.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        MyToast.show("onClick", 1000);
      }
    });

    return mView;
  }

  @Override
  public void onResume() {
    Log.info("onResume");
    super.onResume();
  }

  @Override
  public void onPause() {
    Log.info("onPause");
    super.onPause();
  }

  @Override
  public void onDestroy() {
    Log.info("onDestroy");
    super.onDestroy();
  }
}