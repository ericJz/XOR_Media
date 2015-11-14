package com.xormedia.myrefreshviewsample;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ScrollView;

import com.xormedia.refreshlibrary.PullToRefreshBase;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshBase.OnRefreshListener;
import com.xormedia.refreshlibrary.PullToRefreshScrollView;

public final class PullToRefreshScrollViewActivity extends Activity {

	PullToRefreshScrollView mPullRefreshScrollView;
	ScrollView mScrollView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ptr_scrollview);

		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
		mPullRefreshScrollView.setMode(Mode.PULL_FROM_START);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				new GetDataTask().execute();
			}
		});

		mScrollView = mPullRefreshScrollView.getRefreshableView();
	}

	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			mPullRefreshScrollView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}

}
