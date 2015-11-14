package com.xormedia.mylib.fontsize;

import android.content.Context;
import android.text.TextPaint;
import android.util.Log;

public class TestUtils {
  public static int display_width;
  public static int display_height;
  private double textview_width;
  private double textview_height;

  private String TAG = "testtag";

  private Context context;

  private double pixel_width = 2;
  private double pixel_height = 2.5;

  private float fill_display_width = 720;
  private float fill_display_height = 1280;

  private int base_text_width = 12;
  private int base_text_height = 18;

  private int base_size = 6;
  private int number;

  public TestUtils(Context context, String string) {
    this.context = context;
    this.number = (int) getLength(string);
  }

  private double getLength(String s) {
    double valueLength = 0;
    String chinese = "[\u4e00-\u9fa5]";
    for (int i = 0; i < s.length(); i++) {
      String temp = s.substring(i, i + 1);
      if (temp.matches(chinese)) {
        valueLength += 1;
      } else {
        valueLength += 0.5;
      }
    }
    return Math.ceil(valueLength);
  }

  public float rePaint(int comparePixel, String string) {
    double textHeight_pixel = (comparePixel - base_size) * pixel_height + base_text_height;
    double textWidth_pixel = ((comparePixel - base_size) * pixel_width + base_text_width) * number;
    textview_width = display_width * textWidth_pixel / fill_display_width;
    textview_height = display_height * textHeight_pixel / fill_display_height;
    return getTextSize(string);
  }

  private float getTextSize(String string) {
    int base_pixel = base_size;
    TextPaint newPaint = new TextPaint();
    float newPaintWidth = measure(newPaint, base_pixel, string);
    while (newPaintWidth <= textview_width) {
      base_pixel++;
      newPaintWidth = measure(newPaint, base_pixel, string);
    }
    base_pixel--;
    return base_pixel;
  }
  
  private float measure(TextPaint newPaint, int pixel, String string){
    float textSize = context.getResources().getDisplayMetrics().scaledDensity * pixel;
    newPaint.setTextSize(textSize);
    return newPaint.measureText(string);
  }

}
