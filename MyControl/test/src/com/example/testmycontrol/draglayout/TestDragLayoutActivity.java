package com.example.testmycontrol.draglayout;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.testmycontrol.R;
import com.xormedia.mycontrol.draglayout.DragLayout;
import com.xormedia.mycontrol.draglayout.DragLayout.dragToTopListener;

public class TestDragLayoutActivity extends Activity {
  private static Logger Log = Logger.getLogger(TestDragLayoutActivity.class);
  private DragLayout mDragLayout = null;
  private ViewGroup header1 = null;
  private View header2 = null;
  private View header3 = null;
  private ArrayList<String> data = new ArrayList<String>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_drag_layout);
    header3 = findViewById(R.id.header3);
    header1 = (ViewGroup) findViewById(R.id.header1);
    header2 = LayoutInflater.from(getApplicationContext()).inflate(
        R.layout.drag_layout_header2, null);
    mDragLayout = (DragLayout) findViewById(R.id.dragLayout);
    mDragLayout.addHeaderView(header2);
    header1.addOnLayoutChangeListener(new OnLayoutChangeListener() {

      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        mDragLayout.canDrag(v.getHeight());
        //v.setVisibility(View.INVISIBLE);
      }
    });

    mDragLayout.setDragToTopListener(new dragToTopListener() {

      @Override
      public void onDragToTop(View v) {
        header1.setVisibility(View.VISIBLE);
        header3.setVisibility(View.INVISIBLE);
      }
    });

    for (int i = 0; i < 30; i++) {
      data.add("test_" + (i + 1));
    }
    mDragLayout.getListView().setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, data));
    mDragLayout.getListView().setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.info("onItemClick");
        Toast.makeText(getApplicationContext(), position + "", Toast.LENGTH_LONG).show();
      }
    });
    findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        header3.setVisibility(View.VISIBLE);
        header1.setVisibility(View.INVISIBLE);
        mDragLayout.resetView();
      }
    });
  }

  @Override
  protected void onResume() {
    Log.info("onResume");
    super.onResume();
  }

  @Override
  protected void finalize() throws Throwable {
    data.clear();
    super.finalize();
  }
}
