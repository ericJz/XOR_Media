package com.example.testmycontrol;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.example.testmycontrol.draglayout.TestDragLayoutActivity;
import com.example.testmycontrol.floatview.TestFloatViewActivity;
import com.example.testmycontrol.shake.TestShakeActivity;
import com.example.testmycontrol.virtualkeyboard.TestVirtualKeyboardActivity;
import com.xormedia.mylib.AlarmCallBackReceiver;
import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.Pinyin4j;
import com.xormedia.mylib.StopWatch;

public class MainActivity extends Activity {
  private ListView mListView = null;
  private ArrayList<ButtonCommand> data = new ArrayList<ButtonCommand>();
  private MainAdapter adapter = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    new ConfigureLog4J(getApplicationContext());
    new StopWatch();
    new Pinyin4j();
    new AlarmCallBackReceiver(getApplicationContext());

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mListView = (ListView) findViewById(R.id.listView1);
    data.clear();
    ButtonCommand MyListViewTest = new ButtonCommand();
    MyListViewTest.name = "摇一摇测试";
    MyListViewTest.mOnClick = new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), TestShakeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
      }
    };
    data.add(MyListViewTest);
    MyListViewTest = new ButtonCommand();
    MyListViewTest.name = "可拖動的View";
    MyListViewTest.mOnClick = new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), TestFloatViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
      }
    };
    data.add(MyListViewTest);
    MyListViewTest = new ButtonCommand();
    MyListViewTest.name = "可拖動的layout";
    MyListViewTest.mOnClick = new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), TestDragLayoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
      }
    };
    data.add(MyListViewTest);
    MyListViewTest = new ButtonCommand();
    MyListViewTest.name = "虛擬鍵盤";
    MyListViewTest.mOnClick = new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), TestVirtualKeyboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
      }
    };
    data.add(MyListViewTest);

    adapter = new MainAdapter(getApplicationContext(), data);
    mListView.setAdapter(adapter);

  }
}
