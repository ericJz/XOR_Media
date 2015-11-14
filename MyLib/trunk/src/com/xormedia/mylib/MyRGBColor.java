package com.xormedia.mylib;

public class MyRGBColor {
  public int red = 255;
  public int green = 255;
  public int blue = 255;
  public int alpha = 255;

  // 0xAARRGGBB
  public MyRGBColor(int color) {
    red = color & 0x00FF0000;
    if (red != 0) {
      String _red = Integer.toHexString(red);
      if (_red.length() == 6) {
        _red = _red.substring(0, 2);
      }
      else if (_red.length() == 5) {
        _red = _red.substring(0, 1);
      }
      red = Integer.parseInt(_red, 16);
    }
    green = color & 0x0000FF00;
    if (green != 0) {
      String _green = Integer.toHexString(green);
      if (_green.length() == 4) {
        _green = _green.substring(0, 2);
      }
      else if (_green.length() == 3) {
        _green = _green.substring(0, 1);
      }
      green = Integer.parseInt(_green, 16);
    }
    blue = color & 0x000000FF;
    if (blue != 0) {
      String _blue = Integer.toHexString(blue);
      blue = Integer.parseInt(_blue, 16);
    }
    alpha = color & 0xFF000000;
    if (alpha != 0) {
      String _alpha = Integer.toHexString(alpha);
      if (_alpha.length() == 8) {
        _alpha = _alpha.substring(0, 2);
      }
      else if (_alpha.length() == 7) {
        _alpha = _alpha.substring(0, 1);
      }
      alpha = Integer.parseInt(_alpha, 16);
    }
  }
}
