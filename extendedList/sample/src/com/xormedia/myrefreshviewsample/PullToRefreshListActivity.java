package com.xormedia.myrefreshviewsample;

import java.util.Arrays;
import java.util.LinkedList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.xormedia.refreshlibrary.PullToRefreshBase;
import com.xormedia.refreshlibrary.PullToRefreshBase.Mode;
import com.xormedia.refreshlibrary.PullToRefreshBase.OnLastItemVisibleListener;
import com.xormedia.refreshlibrary.PullToRefreshBase.OnRefreshListener2;
import com.xormedia.refreshlibrary.PullToRefreshListView;

public final class PullToRefreshListActivity extends Activity {

	static final int MENU_MANUAL_REFRESH = 0;
	static final int MENU_DISABLE_SCROLL = 1;
	static final int MENU_SET_MODE = 2;
	static final int MENU_DEMO = 3;

	private LinkedList<String> mListItems;
	private PullToRefreshListView mPullRefreshListView;
	private ArrayAdapter<String> mAdapter;
	
	private View footerview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ptr_list);
		footerview = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer, null);
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
		/**
		 * 设置listview加载模式:BOTH:上下都可以;PULL_FROM_START:可下拉;PULL_FROM_END:可上拉;DISABLED:上下都不可以
		 */
		mPullRefreshListView.setMode(Mode.BOTH); // 下拉刷新,上拉加载更多
//		mPullRefreshListView.setMode(Mode.PULL_FROM_START); // 下拉刷新
//		mPullRefreshListView.setMode(Mode.PULL_FROM_END); // 上拉加载更多
//		mPullRefreshListView.setMode(Mode.DISABLED); // 不能下拉上拉
		
		/**
		 * 设置当加载时是否可以继续滚动
		 */
		mPullRefreshListView.setScrollingWhileRefreshingEnabled(false);
		
		/**
		 * 设置上拉下拉监听
		 */
		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
		    // 下拉刷新
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataRefreshTask().execute();
            }
            // 上拉加载更多
            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                new GetDataGetMoreTask().execute();
            }
        });

		/**
		 * 设置滑动到最后一个条目监听
		 */
		mPullRefreshListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
			    //加载到最后一个条目后加载更多
			}
		});
		
		/**
		 * 设置下拉刷新和上拉加载的箭头标志
		 */
		mPullRefreshListView.getLoadingLayoutProxy().setLoadingDrawable(getResources().getDrawable(R.drawable.xlistview_arrow));
		
		/**
		 * 设置下拉刷新显示的内容
		 */
		mPullRefreshListView.getLoadingLayoutProxy().setRefreshPullLabel("哈哈,快下拉");
		mPullRefreshListView.getLoadingLayoutProxy().setRefreshRefreshingLabel("刷新啦");
		mPullRefreshListView.getLoadingLayoutProxy().setRefreshReleaseLabel("哈哈,松开吧");
		
		/**
		 * 设置上拉加载显示的内容
		 */
		mPullRefreshListView.getLoadingLayoutProxy().setGetMorePullLabel("哈哈,快拉我");
		mPullRefreshListView.getLoadingLayoutProxy().setGetMoreRefreshingLabel("加载更多啦");
		mPullRefreshListView.getLoadingLayoutProxy().setGetMoreReleaseLabel("哈哈,松开吧");
		
		/**
		 * 获取view中的listview
		 */
		ListView actualListView = mPullRefreshListView.getRefreshableView();
		actualListView.addFooterView(footerview);
		mListItems = new LinkedList<String>();
		mListItems.addAll(Arrays.asList(mStrings));

		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListItems);
		actualListView.setAdapter(mAdapter);
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
		    for (int i = 0; i < 10; i++) {
		        mListItems.add("Added after refresh..." + i);
            }
			mAdapter.notifyDataSetChanged();
			mPullRefreshListView.onRefreshComplete();
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
            for (int i = 10; i > 0; i--) {
                mListItems.addFirst("Added after refresh..." + i);
            }
            mAdapter.notifyDataSetChanged();
            mPullRefreshListView.onRefreshComplete();
            super.onPostExecute(result);
        }
    }

	private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler", "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
			"Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
			"Allgauer Emmentaler" };
}
