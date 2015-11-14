package com.xormedia.myrefreshviewsample;

import java.util.LinkedList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import com.xormedia.refreshlibrary.PullToRefreshBase;
import com.xormedia.refreshlibrary.PullToRefreshBase.OnRefreshListener2;
import com.xormedia.refreshlibrary.PullToRefreshGridView;

public final class PullToRefreshGridActivity extends Activity {

	static final int MENU_SET_MODE = 0;

	private LinkedList<String> mListItems;
	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mGridView;
	private ArrayAdapter<String> mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ptr_grid);

		mPullRefreshGridView = (PullToRefreshGridView) findViewById(R.id.pull_refresh_grid);
		mGridView = mPullRefreshGridView.getRefreshableView();

		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				new GetDataRefreshTask().execute();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				new GetDataGetMoreTask().execute();
			}
		});
		
		//设置无数据页面是否跟随滚动
		mPullRefreshGridView.setScrollEmptyView(true);

		mListItems = new LinkedList<String>();

		TextView tv = new TextView(this);
		tv.setGravity(Gravity.CENTER);
		tv.setText("Empty View, Pull Down/Up to Add Items");
		mPullRefreshGridView.setEmptyView(tv);

		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListItems);
		mGridView.setAdapter(mAdapter);
	}

	private class GetDataGetMoreTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            return mStrings;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mListItems.add("Added after refresh...");
            mAdapter.notifyDataSetChanged();
            mPullRefreshGridView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }
    
    private class GetDataRefreshTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            return mStrings;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mListItems.addFirst("Added after refresh...");
            mAdapter.notifyDataSetChanged();
            mPullRefreshGridView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }



	private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };
}
