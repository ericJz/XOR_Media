package com.xormedia.refreshlibrary;

import java.util.HashSet;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.xormedia.refreshlibrary.internal.ILoadingLayout;
import com.xormedia.refreshlibrary.internal.LoadingLayout;

public class LoadingLayoutProxy implements ILoadingLayout {

	private final HashSet<LoadingLayout> mLoadingLayouts;

	LoadingLayoutProxy() {
		mLoadingLayouts = new HashSet<LoadingLayout>();
	}

	public void addLayout(LoadingLayout layout) {
		if (null != layout) {
			mLoadingLayouts.add(layout);
		}
	}

	@Override
	public void setLastUpdatedLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setLastUpdatedLabel(label);
		}
	}

	@Override
	public void setLoadingDrawable(Drawable drawable) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setLoadingDrawable(drawable);
		}
	}

	@Override
	public void setRefreshRefreshingLabel(CharSequence refreshingLabel) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setRefreshRefreshingLabel(refreshingLabel);
		}
	}

	@Override
	public void setRefreshPullLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setRefreshPullLabel(label);
		}
	}

	@Override
	public void setRefreshReleaseLabel(CharSequence label) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setRefreshReleaseLabel(label);
		}
	}
	
	@Override
    public void setGetMoreRefreshingLabel(CharSequence refreshingLabel) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setGetMoreRefreshingLabel(refreshingLabel);
        }
    }

    @Override
    public void setGetMorePullLabel(CharSequence label) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setGetMorePullLabel(label);
        }
    }

    @Override
    public void setGetMoreReleaseLabel(CharSequence label) {
        for (LoadingLayout layout : mLoadingLayouts) {
            layout.setGetMoreReleaseLabel(label);
        }
    }

	public void setTextTypeface(Typeface tf) {
		for (LoadingLayout layout : mLoadingLayouts) {
			layout.setTextTypeface(tf);
		}
	}
}
