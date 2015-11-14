package com.xormedia.mylib;

import org.apache.log4j.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

public class ChangeBitmap {
  private static Logger Log = Logger.getLogger(ChangeBitmap.class);
  private static final int IMAGE_SIZE_LOW = 1 * 1024 * 1024 / 4;
  private static final int IMAGE_SIZE_BIG = 2 * 1024 * 1024;

  public static Bitmap toLowSizeBitmap(String fileName) {
    Bitmap bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    // true那么将不返回实际的bitmap对象,不给其分配内存空间但是可以得到一些解码边界信息即图片大小等信息
    options.inJustDecodeBounds = true;
    bitmap = BitmapFactory.decodeFile(fileName, options);

    options.inSampleSize = 1;
    for (int imagesize = options.outWidth * options.outHeight * 4; imagesize / (options.inSampleSize * options.inSampleSize) >= IMAGE_SIZE_LOW; options.inSampleSize *= 2)
      ;

    options.inJustDecodeBounds = false;
    options.inInputShareable = true;
    options.inPurgeable = true;
    options.inScaled = true;
    options.inPreferredConfig = Bitmap.Config.RGB_565;

    bitmap = BitmapFactory.decodeFile(fileName, options);
    return bitmap;
  }

  public static Bitmap toBigSizeBitmap(String fileName) {
    Bitmap bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    // true那么将不返回实际的bitmap对象,不给其分配内存空间但是可以得到一些解码边界信息即图片大小等信息
    options.inJustDecodeBounds = true;
    bitmap = BitmapFactory.decodeFile(fileName, options);

    options.inSampleSize = 1;
    for (int imagesize = options.outWidth * options.outHeight * 4; imagesize / (options.inSampleSize * options.inSampleSize) >= IMAGE_SIZE_BIG; options.inSampleSize *= 2)
      ;

    options.inJustDecodeBounds = false;
    options.inInputShareable = true;
    options.inPurgeable = true;
    options.inScaled = true;
    options.inPreferredConfig = Bitmap.Config.RGB_565;

    bitmap = BitmapFactory.decodeFile(fileName, options);
    return bitmap;
  }

  public static Bitmap toFitImageBitmap(String fileName, int width, int height) {
    Bitmap bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    // true那么将不返回实际的bitmap对象,不给其分配内存空间但是可以得到一些解码边界信息即图片大小等信息
    options.inJustDecodeBounds = true;
    bitmap = BitmapFactory.decodeFile(fileName, options);
    if (options.outHeight * options.outWidth > width * height && options.outHeight * options.outHeight > 1920 * 1080) {
      options.inSampleSize = (options.outHeight * options.outWidth) / (1920 * 1080)
          + (((options.outHeight * options.outWidth) / (1920 * 1080) > 0) ? 1 : 0);
      options.inInputShareable = true;
      options.inPurgeable = true;
      options.inScaled = true;
      options.inPreferredConfig = Bitmap.Config.RGB_565;
      options.inJustDecodeBounds = false;
      bitmap = null;
      try {
        bitmap = BitmapFactory.decodeFile(fileName, options);
      } catch (OutOfMemoryError e) {
        ConfigureLog4J.printStackTrace(e, Log);
        System.gc();
        bitmap = null;
        try {
          bitmap = BitmapFactory.decodeFile(fileName, options);
        } catch (OutOfMemoryError e2) {
          ConfigureLog4J.printStackTrace(e2, Log);
          bitmap = null;
        }
      }
    } else {
      options.inPreferredConfig = Bitmap.Config.RGB_565;
      options.inJustDecodeBounds = false;
      bitmap = BitmapFactory.decodeFile(fileName, options);
    }

    if (bitmap != null && bitmap.getWidth() * bitmap.getHeight() > width * height) {
      double tmp = bitmap.getHeight();
      tmp = tmp / bitmap.getWidth();
      double tmp1 = tmp;
      tmp1 = (width * height) / tmp1;
      tmp1 = Math.sqrt(tmp1);
      int outWidth = (int) tmp1;
      tmp1 = tmp1 * tmp;
      int outHeight = (int) tmp1;
      Log.info("toFitImageBitmap::" + outWidth + ";" + outHeight);

      Bitmap orgbitmap = bitmap;
      bitmap = null;
      bitmap = ThumbnailUtils.extractThumbnail(orgbitmap, outWidth, outHeight);
    }
    return bitmap;
  }

  public static int calculateInSampleSize(int srcWidth, int srcHeight, int reqWidth, int reqHeight) {
    int inSampleSize = 1;

    if (srcWidth < srcHeight) {
      int tmp = srcWidth;
      srcWidth = srcHeight;
      srcHeight = tmp;
    }
    if (reqWidth < reqHeight) {
      int tmp = reqWidth;
      reqWidth = reqHeight;
      reqHeight = tmp;
    }

    if (srcHeight > reqHeight || srcWidth > reqWidth) {
      float scaleW = (float) srcWidth / (float) reqWidth;
      float scaleH = (float) srcHeight / (float) reqHeight;

      double sample = scaleW > scaleH ? scaleW : scaleH;

      // 只能是2的次幂
      if (sample < 3)
        inSampleSize = (int) sample;
      else if (sample < 6.5)
        inSampleSize = 4;
      else if (sample < 8)
        inSampleSize = 8;
      else
        inSampleSize = (int) sample;

    }
    return inSampleSize;
  }

  public static Bitmap toOriginalBitmap(String fileName) {
    Bitmap bitmap = null;
    bitmap = BitmapFactory.decodeFile(fileName);
    return bitmap;
  }

}
