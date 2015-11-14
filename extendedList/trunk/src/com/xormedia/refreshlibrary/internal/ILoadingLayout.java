package com.xormedia.refreshlibrary.internal;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
/**
 * 用来显示文本
 * @author guorui.zhang
 *
 */
public interface ILoadingLayout {

	public void setLastUpdatedLabel(CharSequence label);

	public void setLoadingDrawable(Drawable drawable);

	public void setRefreshPullLabel(CharSequence pullLabel);

    public void setRefreshRefreshingLabel(CharSequence refreshingLabel);

    public void setRefreshReleaseLabel(CharSequence releaseLabel);

    public void setGetMorePullLabel(CharSequence pullLabel);

    public void setGetMoreRefreshingLabel(CharSequence refreshingLabel);

    public void setGetMoreReleaseLabel(CharSequence releaseLabel);

	public void setTextTypeface(Typeface tf);

}
