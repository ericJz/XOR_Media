package com.xormedia.mylib.fontsize;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtil {
  /**
   * 将px值转换为dip值，保证尺寸大小不变
   * 
   * @param pxValue
   *          传入图片像素
   */
  private static int designWidth;
  private static int designHeight;
  private static Context context;

  public DisplayUtil(Context _context, int _designWidth, int _designHeight) {
    context = _context;
    designWidth = _designWidth;
    designHeight = _designHeight;
  }

  public static int px2dip(float pxValue) {
    int ret = 0;
    if (context != null) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      float scale = dm.density;
      ret = (int) (pxValue / scale + 0.5f);
    }
    return ret;
  }

  /**
   * 将dip值转换为px值，保证尺寸大小不变
   * 
   * @param dipValue
   *          传入屏幕尺寸值
   */
  public static float dip2px(float dipValue) {
    float ret = 0;
    if (context != null) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      float scale = dm.density;
      ret = dipValue * scale + 0.5f;
    }
    return ret;
  }

  /**
   * 将px值转换为sp值，保证文字大小不变
   * 
   * @param pxValue
   *          传入图片像素
   */
  public static float px2sp(float pxValue) {
    float ret = 0;
    if (context != null) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      float display_width = dm.widthPixels;
      float display_height = dm.heightPixels;
      int rootWidth = designWidth;
      if (display_width > display_height) {
        rootWidth = Math.max(designHeight, designWidth);
      } else {
        rootWidth = Math.min(designHeight, designWidth);
      }
      float fontScale = dm.scaledDensity;
      float div = rootWidth / display_width;
      pxValue = pxValue / div;
      ret = pxValue / fontScale + 0.5f;
    }
    return ret;
  }

  public static float fixPx2sp(float pxValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    // float div = (float) (720.0/display_width);
    // Log.i("==============div", div+"");
    // pxValue = pxValue/fontScale;
    // Log.i("==============", fontScale+"");
    // Log.i("==============", (pxValue / fontScale + 0.5f)+"");
    float baseDis = dip2px(720);
    // Log.i("==============baseDis", baseDis+"");
    // float nowDis = 480/fontScale;
    // Log.i("==============nowDis", nowDis+"");
    // float value = baseDis*pxValue/fontScale/nowDis;
    // Log.i("==============value", (value / fontScale + 0.5f)+"");
    pxValue = pxValue * 480 / 720;
    return pxValue / fontScale + 0.5f;
    // return pxValue / fontScale;
  }

  /**
   * 将sp值转换为px值，保证文字大小不变
   * 
   * @param spValue
   *          传入字体尺寸值
   */
  public static int sp2px(float spValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }
}
