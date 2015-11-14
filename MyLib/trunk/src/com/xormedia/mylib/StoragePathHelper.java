package com.xormedia.mylib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.os.Environment;

public class StoragePathHelper {
  /**
   * 是否有SD卡
   * 
   * @return
   */
  public static boolean hasSDCard() {
    String status = Environment.getExternalStorageState();
    if (!status.equals(Environment.MEDIA_MOUNTED)) {
      return false;
    }
    return true;
  }

  /**
   * 获取路径
   * 
   * @return
   */
  public static String getRootFilePath() {
    if (hasSDCard()) {
      return getSavePath(getStorageList());// filePath:/sdcard
    } else {
      return Environment.getDataDirectory().getAbsolutePath() + File.separator + "data"; // filePath:
                                                                                         // /data/data
    }
  }

  private static String getSavePath(List<String> storageList) {
    String savePath = null;
    String defaultStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
    if (storageList != null && storageList.size() > 0) {
      for (int i = 0; i < storageList.size(); i++) {
        String currentStorage = storageList.get(i);
        if (!currentStorage.toLowerCase(Locale.ENGLISH).contains("usb") && !currentStorage.equals(defaultStorage)) {
          File file = new File(currentStorage);
          if (file.canRead() && file.canWrite()) {
            savePath = currentStorage;
            break;
          }
        }
      }
    }

    if (savePath == null || "".equals(savePath)) {
      savePath = defaultStorage;
    }
    return savePath;
  }

  private static List<String> getStorageList() {
    List<String> storageList = new ArrayList<String>();
    String defaultStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
    String storageParent = null;
    String[] splits = defaultStorage.split(File.separator);
    if (splits.length > 2) {
      storageParent = defaultStorage.substring(0, defaultStorage.lastIndexOf(File.separator));
      File storageFile = new File(storageParent);
      File[] filelist = storageFile.listFiles();
      if (filelist != null && filelist.length > 0) {
        for (File file : filelist) {
          storageList.add(file.getAbsolutePath());
        }
      }
    }
    return storageList;
  }

}
