package com.xormedia.mylib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

@SuppressLint("SimpleDateFormat")
public class MyUseCamera {
  private static Logger Log = Logger.getLogger(MyUseCamera.class);

  public final static int MODE_ZHAO_PIAN = 1;
  public final static int MODE_SHI_PING = 2;
  public final static int MODE_CAPTURE = 3;
  private static int curMode = MODE_ZHAO_PIAN;
  public final static int FLAG_PAI_SHE = 1;
  public final static int FLAG_XUAN_QU = 2;
  /**
   * 当前是选取还是拍摄 1：拍摄，2：选取
   */
  public static int curStatus = -1;
  public final static int REQUEST_CODE_PAI_SHE = 1;
  public final static int REQUEST_CODE_XUAN_QU = 2;
  public final static int REQUEST_CODE_CAPTURE = 3;

  private static Activity mActivity = null;
  public static String mSavePath = null;

  /**
   * @param _mode
   *          照片：1；视频：2
   * @param _flag
   *          拍摄：1；手机相册中选取：2
   */
  public static void useCamera(int _mode, int _flag, Activity _activity) {
    if (_activity == null) {
      Log.info("useCamera Activity is null");
      return;
    } else {
      mActivity = _activity;
      Intent intent = null;
      if (_flag == FLAG_PAI_SHE) {
        curStatus = FLAG_PAI_SHE;
        String filePath = "";
        if (checkSDCard()) {
          filePath = Environment.getExternalStorageDirectory().toString();
        } else {
          filePath = mActivity.getCacheDir().getAbsolutePath();
        }
        if (_mode == MODE_ZHAO_PIAN) {
          curMode = MODE_ZHAO_PIAN;
          filePath += "/" + mActivity.getPackageName() + "/picture";
        } else if (_mode == MODE_SHI_PING) {
          curMode = MODE_SHI_PING;
          filePath += "/" + mActivity.getPackageName() + "/video";
        }
        File tmpFolder = new File(filePath);
        if (tmpFolder.exists() == false) {
          tmpFolder.mkdirs();
        }

        String currentFileName = new SimpleDateFormat("yyyyMMddHHmmssS").format(new Date());
        File out = null;

        if (_mode == MODE_ZHAO_PIAN) {
          currentFileName += ".jpg";
          intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

          out = new File(tmpFolder.getAbsoluteFile() + "/" + currentFileName);
          if (out.exists() == false) {
            try {
              out.createNewFile();
            } catch (IOException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        } else if (_mode == MODE_SHI_PING) {
          currentFileName += ".mp4";
          intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
          out = new File(tmpFolder.getAbsoluteFile() + "/" + currentFileName);
          if (out.exists() == false) {
            try {
              out.createNewFile();
            } catch (IOException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        }

        if (intent != null) {
          Uri uri = Uri.fromFile(out);
          intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
          if (_mode == MODE_SHI_PING) {
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
          }
          mSavePath = tmpFolder.getAbsoluteFile() + "/" + currentFileName;
          mActivity.startActivityForResult(intent, REQUEST_CODE_PAI_SHE);
        }
      } else if (_flag == FLAG_XUAN_QU) {
        curStatus = FLAG_XUAN_QU;
        intent = new Intent(Intent.ACTION_PICK);
        if (_mode == MODE_ZHAO_PIAN) {
          curMode = MODE_ZHAO_PIAN;
          intent.setType("image/*");
        } else if (_mode == MODE_SHI_PING) {
          curMode = MODE_SHI_PING;
          intent.setType("video/*");
        }
        mActivity.startActivityForResult(intent, REQUEST_CODE_XUAN_QU);
      }
    }
  }

  private static Uri imageUri = null;
/**
 * 拍照或选取照片之后，调用系统相机的裁剪功能
 * @param _imageUri
 */
  public static void useCameraCapture(Uri _imageUri) {
    if (mActivity != null && _imageUri != null) {
      imageUri = _imageUri;
      curMode = MODE_CAPTURE;
      Intent intent = new Intent("com.android.camera.action.CROP");
      intent.setDataAndType(imageUri, "image/*");
      intent.putExtra("crop", "true");
      intent.putExtra("aspectX", 2);
      intent.putExtra("aspectY", 2);
      intent.putExtra("outputX", 150);
      intent.putExtra("outputY", 150);
      intent.putExtra("scale", true);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
      intent.putExtra("return-data", false);
      intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
      intent.putExtra("noFaceDetection", true);
      mActivity.startActivityForResult(intent, REQUEST_CODE_CAPTURE);
    }
  }

  /**
   * 检验SDcard状态
   * 
   * @return boolean
   */
  public static boolean checkSDCard() {
    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 接口
   * 
   * @see 拍照后，返回照片或视频的路径接口
   */
  public interface MyUseCameraResultInterface {
    /**
     * 
     * @param _myUseCameraResultPath
     *          照片或视频的路径
     * @param _mode
     *          1=照片、2=视频
     */
    public void myUseCameraResultPath(String _myUseCameraResultPath, int _mode);
  }

  /**
   * 接口变量
   * 
   * @see 拍照后，返回照片或视频的路径接口变量
   */
  private static MyUseCameraResultInterface myUseCameraResultPath = null;

  /**
   * 事件监听
   * 
   * @see 拍照后，返回照片或视频的路径事件监听
   */
  public static void setMyUseCameraResultInterfaceListener(MyUseCameraResultInterface _myUseCameraResultPath) {
    myUseCameraResultPath = _myUseCameraResultPath;
  }

  public static void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
    switch (_requestCode) {
      case REQUEST_CODE_PAI_SHE:
        if (_resultCode == Activity.RESULT_OK) {
          Log.info("拍摄 path = " + mSavePath);
          Log.info("拍摄 curMode = " + curMode);
          if (mSavePath != null && mSavePath.length() > 0) {
            File mFile = new File(mSavePath);
            if (mFile != null && mFile.getName() != null) {
              myUseCameraResultPath.myUseCameraResultPath(mSavePath, curMode);
            }
          }
          mSavePath = null;
        }
        break;
      case REQUEST_CODE_XUAN_QU:
        if (_resultCode == Activity.RESULT_OK) {
          if (_data != null && _data.getData() != null) {
            Uri selectUri = _data.getData();
            String[] filePathColumns = { MediaStore.Images.Media.DATA };
            Cursor c = mActivity.getContentResolver().query(selectUri, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String selectPath = c.getString(columnIndex);
            c.close();
            Log.info("选取 path = " + selectPath);
            Log.info("拍摄 curMode = " + curMode);
            if (selectPath != null && selectPath.length() > 0) {
              File mFile = new File(selectPath);
              if (mFile != null && mFile.getName() != null) {
                myUseCameraResultPath.myUseCameraResultPath(selectPath, curMode);
              }
            }
          }
        }
        break;
      case REQUEST_CODE_CAPTURE:
        if (_resultCode == Activity.RESULT_OK) {
          if (imageUri != null) {
            myUseCameraResultPath.myUseCameraResultPath(imageUri.getPath(), curMode);
          }
        }
        break;
    }
  }

  /**
   * 旋转缩略图方向
   * 
   * @param _file
   */
  public static void turnBitmapNewFile(File _file) {
    ExifInterface exif = null;
    int digree = 0;
    if (_file != null) {
      try {
        exif = new ExifInterface(_file.getAbsolutePath());
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
        exif = null;
      }
      if (exif != null) {
        // 读取图片中相机方向信息
        int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        // 计算旋转角度
        switch (ori) {
          case ExifInterface.ORIENTATION_ROTATE_90:
            digree = 90;
            break;
          case ExifInterface.ORIENTATION_ROTATE_180:
            digree = 180;
            break;
          case ExifInterface.ORIENTATION_ROTATE_270:
            digree = 270;
            break;
          default:
            digree = 0;
            break;
        }
      }

      MyBitmap myBitmap = new MyBitmap(_file, 80);
      if (myBitmap != null && myBitmap.mBitmap != null) {
        if (digree != 0) {
          // 旋转图片
          Matrix m = new Matrix();
          m.postRotate(digree);
          myBitmap.mBitmap = Bitmap
              .createBitmap(myBitmap.mBitmap, 0, 0, myBitmap.mBitmap.getWidth(), myBitmap.mBitmap.getHeight(), m, true);
        }
        if (myBitmap.mBitmap != null) {
          File f = new File(_file.getAbsolutePath());
          try {
            f.createNewFile();
          } catch (IOException e1) {
            ConfigureLog4J.printStackTrace(e1, Log);
          }
          FileOutputStream fOut = null;
          try {
            fOut = new FileOutputStream(f);
          } catch (FileNotFoundException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
          myBitmap.mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
          try {
            fOut.flush();
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
          try {
            fOut.close();
          } catch (IOException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
    }
  }

  /**
   * 删除文件
   * 
   * @param _file
   * @return 0：删除成功，-1：删除失败
   */
  public static int deleteFile(File _file) {
    int ret = -1;
    if (_file != null && _file.exists() == true && _file.isFile() == true && curStatus == FLAG_PAI_SHE) {
      boolean isSucceed = _file.delete();
      if (isSucceed == true) {
        ret = 0;
      }
    }
    return ret;
  }

}
