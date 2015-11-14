package com.example.testsmb;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.smb.MySmb;
import com.xormedia.mylib.smb.MySmbServer;

public class MainActivity extends Activity {
  public TextView txtView = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    new ConfigureLog4J(getApplicationContext());
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    txtView = (TextView) findViewById(R.id.textView1);
    findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        MySmb.scanSmbServerList(handler);
      }
    });

  }

  public Handler handler = new Handler(new Callback() {

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
      if (msg != null && msg.obj != null) {
        ArrayList<MySmbServer> list = (ArrayList<MySmbServer>) msg.obj;
        if (list != null && list.size() > 0) {
          String tmp = "";
          for (int i = 0; i < list.size(); i++) {
            tmp += list.get(i).name + "\n";
          }
          txtView.setText(tmp);
        }
      }
      return false;
    }
  });
}
