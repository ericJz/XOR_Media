package com.xormedia.mylib;

import java.io.File;

import org.apache.log4j.Logger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class MyBitmap {
  public int quality = 100;
  public Bitmap mBitmap = null;
  private static Logger Log = Logger.getLogger(MyBitmap.class);

  public MyBitmap(Bitmap bitmap) {
    if (bitmap != null) {
      mBitmap = bitmap;
    }
  }

  public MyBitmap(Bitmap bitmap, int saveJPEGQuality) {
    if (bitmap != null) {
      mBitmap = bitmap;
    }
    if (saveJPEGQuality > 0 && saveJPEGQuality <= 100) {
      quality = saveJPEGQuality;
    }
  }

  public MyBitmap(File file) {
    file2Bitmap(file);
  }

  public MyBitmap(File file, int saveJPEGQuality) {
    file2Bitmap(file);
    if (saveJPEGQuality > 0 && saveJPEGQuality <= 100) {
      quality = saveJPEGQuality;
    }
  }

  private void file2Bitmap(File file) {
    if (file != null && file.exists() == true) {
      if (MediaFile.isVideoFileType(file.getName()) == true) {
        mBitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
      } else {
        BitmapFactory.Options options = new BitmapFactory.Options();
        // true那么将不返回实际的bitmap对象,不给其分配内存空间但是可以得到一些解码边界信息即图片大小等信息
        options.inJustDecodeBounds = true;
        mBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (options.outHeight * options.outHeight > 1920 * 1080) {
          options.inSampleSize = (options.outHeight * options.outWidth) / (1920 * 1080)
              + (((options.outHeight * options.outWidth) / (1920 * 1080) > 0) ? 1 : 0);
          options.inInputShareable = true;
          options.inPurgeable = true;
          options.inScaled = true;
          options.inPreferredConfig = Bitmap.Config.RGB_565;
          options.inJustDecodeBounds = false;
          mBitmap = null;
          try {
            mBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            tmpLength = mBitmap.getByteCount();
            Log.info("888-mBitmap create " + tmpLength + " file=" + file.getName());
          } catch (OutOfMemoryError e) {
            ConfigureLog4J.printStackTrace(e, Log);
            mBitmap = null;
          }
        } else {
          options.inPreferredConfig = Bitmap.Config.RGB_565;
          options.inJustDecodeBounds = false;
          mBitmap = null;
          try {
            mBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            tmpLength = mBitmap.getByteCount();
            Log.info("888-mBitmap create " + tmpLength + " file=" + file.getName());
          } catch (OutOfMemoryError e) {
            ConfigureLog4J.printStackTrace(e, Log);
            mBitmap = null;
          }
        }
      }
    }
  }

  private long tmpLength = 0l;

  public boolean isEmpty() {
    boolean ret = true;
    if (mBitmap != null && mBitmap.getByteCount() > 0) {
      ret = false;
    }
    return ret;
  }

  public int getBitmapByteCount() {
    int ret = 0;
    if (mBitmap != null) {
      ret = mBitmap.getByteCount();
      if (mBitmap.getRowBytes() > 10000 || mBitmap.getHeight() > 10000) {
        ret = 0;
      }
    }
    return ret;
  }

  public Bitmap getLimitSize(long size) {
    if (mBitmap != null && mBitmap.getWidth() * mBitmap.getHeight() > size) {
      double tmp = mBitmap.getHeight();
      tmp = tmp / mBitmap.getWidth();
      double tmp1 = tmp;
      tmp1 = (size) / tmp1;
      tmp1 = Math.sqrt(tmp1);
      int outWidth = (int) tmp1;
      tmp1 = tmp1 * tmp;
      int outHeight = (int) tmp1;
      Log.info("getLimitSize::" + outWidth + ";" + outHeight);

      float tmp2 = outWidth;
      tmp2 = tmp2 / mBitmap.getWidth();
      Log.info("MyBitmap::" + tmp2 + "");
      Matrix matrix = new Matrix();
      matrix.postScale(tmp2, tmp2);
      mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
      // Bitmap orgbitmap = mBitmap;
      // mBitmap = null;
      // mBitmap = ThumbnailUtils.extractThumbnail(orgbitmap, outWidth,
      // outHeight);
    }
    return mBitmap;
  }

  public Bitmap getScopeCenter(int targetWidth, int targetHeight) {
    if (targetWidth == targetHeight && mBitmap != null) {
      int width = mBitmap.getWidth();
      int height = mBitmap.getHeight();

      if (width > targetWidth && height > targetWidth) {
        int tmp = Math.min(width, height);
        float tmp1 = targetWidth;
        tmp1 = tmp1 / tmp;
        Matrix matrix = new Matrix();
        matrix.postScale(tmp1, tmp1);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
        width = mBitmap.getWidth();
        height = mBitmap.getHeight();
      }

      if (width != height) {
        if (width > height) {
          mBitmap = Bitmap.createBitmap(mBitmap, (width - height) / 2, 0, height, height);
        } else {
          mBitmap = Bitmap.createBitmap(mBitmap, 0, (height - width) / 2, width, width);
        }
      }
    }
    if (targetHeight != targetWidth) {
      int width = mBitmap.getWidth();
      int height = mBitmap.getHeight();
      float tmpScale = targetWidth;
      tmpScale = tmpScale / targetHeight;
      float tmp1Scale = width;
      tmp1Scale = tmp1Scale / height;

      if (tmpScale > tmp1Scale) {
        float h = (targetHeight * width) / targetWidth;
        mBitmap = Bitmap.createBitmap(mBitmap, 0, (int) (height - h) / 2, width, (int) h);
      } else {
        float w = (targetWidth * height) / targetHeight;
        mBitmap = Bitmap.createBitmap(mBitmap, (int) (width - w) / 2, 0, (int) w, height);
      }

      if (mBitmap != null) {
        Log.info("MyBitmap::" + mBitmap.getWidth() + ":" + mBitmap.getHeight());
        float tmp1 = targetWidth;
        tmp1 = tmp1 / mBitmap.getWidth();
        Log.info("MyBitmap::" + tmp1 + "");
        Matrix matrix = new Matrix();
        matrix.postScale(tmp1, tmp1);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
      }

    }
    return mBitmap;
  }

  public void destroyBitmap() {
    if (mBitmap != null) {// && mBitmap.isRecycled() == false) {
      Log.info("888-mBitmap destroy tmpLength=" + tmpLength + ";" + mBitmap.getByteCount() + ";" + mBitmap.getRowBytes() + ";" + mBitmap.getHeight());
      // mBitmap.recycle();
      mBitmap = null;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    destroyBitmap();
    super.finalize();
  }

}
