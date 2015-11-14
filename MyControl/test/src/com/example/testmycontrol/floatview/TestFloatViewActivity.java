package com.example.testmycontrol.floatview;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.example.testmycontrol.R;

public class TestFloatViewActivity extends Activity {
  // private static Logger Log = Logger.getLogger(TestFloatViewActivity.class);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_float_view);

    FragmentManager fm = getFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.replace(R.id.fragmentRoot_rl, new HomePage());
    ft.addToBackStack(null);
    ft.commit();

  }

  @Override
  protected void onPause() {
    finish();
    super.onPause();
  }
}
