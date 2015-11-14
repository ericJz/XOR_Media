package com.xormedia.mylib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoMarqueeTextView extends TextView {

  public AutoMarqueeTextView(Context context) {
    super(context);
  }

  public AutoMarqueeTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public boolean isFocused() {
    return true;
  }
}