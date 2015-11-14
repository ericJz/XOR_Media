package com.example.testmycontrol.virtualkeyboard;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.example.testmycontrol.R;

public class TestVirtualKeyboardActivity extends Activity {
  private static Logger Log = Logger.getLogger(TestVirtualKeyboardActivity.class);
  private TextView text1 = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_virtual_keyboard);
    text1 = (TextView) findViewById(R.id.textView1);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    Log.info("onKeyDown(keyCode = " + keyCode + ") Enter！");
    String tmp = (text1.getText() != null && text1.getText().toString() != null) ? text1.getText().toString() : "";
    tmp += keyCode + ";";
    text1.setText(tmp);
    return false;
  }

  @Override
  protected void onPause() {
    finish();
    super.onPause();
  }
}
