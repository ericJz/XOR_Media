package com.example.testmycontrol.shake;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testmycontrol.R;
import com.xormedia.mylib.shake.ShakeListener;
import com.xormedia.mylib.shake.ShakeListener.OnShakeListener;

public class TestShakeActivity extends Activity {
  private static Logger Log = Logger.getLogger(TestShakeActivity.class);
  private ShakeListener mShakeListener = null;
  private MediaPlayer mediaPlayer = null;
  private TextView mTextView = null;
  private int times = 0;
  private ImageView image1 = null;
  private Animation operatingAnim = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_shake);
    mTextView = (TextView) findViewById(R.id.textview1);
    mTextView.setText("您摇了0次");
    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.a);
    image1 = (ImageView) findViewById(R.id.imageView1);
    operatingAnim = AnimationUtils.loadAnimation(this, R.anim.shakehand);
    // LinearInterpolator lin = new LinearInterpolator(); // 动画匀速运行效果
    AccelerateInterpolator acc = new AccelerateInterpolator(10);// 动画加速运行效果
    operatingAnim.setInterpolator(acc);
    mShakeListener = new ShakeListener(this);
    mShakeListener.setOnShakeListener(new OnShakeListener() {

      @Override
      public void onShake() {
        mShakeListener.stop();
        Log.info("摇一摇成功啦！");
        image1.clearAnimation();
        image1.startAnimation(operatingAnim);
        times++;
        mTextView.setText("您摇了" + times + "次");
        mediaPlayer.start();

        new Handler().postDelayed(new Runnable() {

          @Override
          public void run() {
            mShakeListener.start();
          }
        }, 3000);
      }
    });
  }

  @Override
  protected void onPause() {
    mShakeListener.stop();
    mShakeListener = null;
    mediaPlayer.stop();
    mediaPlayer.release();
    times = 0;
    finish();
    super.onPause();
  }
}
