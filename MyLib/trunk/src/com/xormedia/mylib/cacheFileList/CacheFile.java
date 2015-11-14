package com.xormedia.mylib.cacheFileList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.os.Handler;
import android.os.Message;

import com.xormedia.mylib.ConfigureLog4J;
import com.xormedia.mylib.xhr;
import com.xormedia.mylib.xhr.xhrParameter;
import com.xormedia.mylib.xhr.xhrResponse;

public class CacheFile {
  private static Logger Log = Logger.getLogger(CacheFile.class);
  private final static int KEEP_FILE_COUNT = 2;// 保留过去文件个数
  private final static int PREFETCH_FILE_COUNT = 2;// 预取多文件个数

  private ArrayList<MyFile> mFileArray = new ArrayList<MyFile>();
  private WeakReference<Handler> mWeakHadReadyHandler = null;
  private WeakReference<Handler> mWeakDownLoadProcessHandler = null;
  private File rootFolder = null;

  private boolean isRunThread = true;
  private DownloadThread mDownloadThread = null;

  private static final byte mHexhars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  private static String convertURL2Filename(String url) {
    String retval = null;
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(url.getBytes("UTF-8"));
      MessageDigest hash = MessageDigest.getInstance("SHA1");
      byte[] buffer = new byte[1024];
      int length = 0;
      while ((length = inputStream.read(buffer)) != -1)
        hash.update(buffer, 0, length);
      byte[] data = hash.digest();
      if (data != null) {
        length = data.length;
        if (length > 0) {
          StringBuilder buf = new StringBuilder(2 * length);
          for (int i = 0; i < length; i++) {
            int v = data[i] & 0xff;
            buf.append((char) mHexhars[v >> 4]);
            buf.append((char) mHexhars[v & 0xf]);
          }
          retval = buf.toString();
        }
      }
    } catch (Exception e) {
      retval = null;
    }
    if (url.lastIndexOf(".") > 0) {
      String[] tmp = url.split("/");
      if (tmp != null && tmp.length > 0 && tmp[tmp.length - 1].lastIndexOf(".") > 0) {
        retval += tmp[tmp.length - 1].substring(tmp[tmp.length - 1].lastIndexOf("."));
      }
    }
    return retval;
  }

  public CacheFile(String storagePath) {
    if (storagePath != null) {
      rootFolder = new File(storagePath);
      if (rootFolder.exists() == false) {
        rootFolder.mkdirs();
      } else {
        emptyRootFolder();
      }
    }
  }

  private void emptyRootFolder() {
    if (rootFolder != null && rootFolder.exists() == true) {
      File[] files = rootFolder.listFiles();
      if (files != null && files.length > 0) {
        for (int i = 0; i < files.length; i++) {
          files[i].delete();
        }
      }
    }
  }

  /**
   * 加载文件下载是否完成返回handler
   * 
   * @param hadReadyHandler
   *          msg.what:0表示下载成功，1表示下载失败 ;msg.obj:当前下载的文件
   */
  public void setHadReadyHandler(Handler hadReadyHandler) {
    if (mWeakHadReadyHandler == null) {
      if (hadReadyHandler != null) {
        mWeakHadReadyHandler = new WeakReference<Handler>(hadReadyHandler);
      }
    } else {
      synchronized (mWeakHadReadyHandler) {
        if (hadReadyHandler != null) {
          mWeakHadReadyHandler = new WeakReference<Handler>(hadReadyHandler);
        } else {
          mWeakHadReadyHandler = null;
        }
      }
    }
  }

  private void sendHadReadyHandler(int what, MyFile file) {
    if (mWeakHadReadyHandler != null) {
      synchronized (mWeakHadReadyHandler) {
        if (mWeakHadReadyHandler != null) {
          Handler handler = mWeakHadReadyHandler.get();
          if (handler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.obj = file;
            handler.sendMessage(msg);
          } else {
            mWeakHadReadyHandler = null;
          }
        }
      }
    }
  }

  /**
   * 加载下载文件进度返回handler
   * 
   * @param downLoadProcessHandler
   *          msg.what:下载进度 ;msg.obj:当前下载的文件
   */
  public void setDownLoadProcessHandler(Handler downLoadProcessHandler) {
    if (mWeakDownLoadProcessHandler == null) {
      if (downLoadProcessHandler != null) {
        mWeakDownLoadProcessHandler = new WeakReference<Handler>(downLoadProcessHandler);
      }
    } else {
      synchronized (mWeakDownLoadProcessHandler) {
        if (downLoadProcessHandler != null) {
          mWeakDownLoadProcessHandler = new WeakReference<Handler>(downLoadProcessHandler);
        } else {
          mWeakDownLoadProcessHandler = null;
        }
      }
    }
  }

  private void sendDownLoadProcessHandler(int _progress, MyFile file) {
    if (mWeakDownLoadProcessHandler != null) {
      synchronized (mWeakDownLoadProcessHandler) {
        if (mWeakDownLoadProcessHandler != null) {
          Handler handler = mWeakDownLoadProcessHandler.get();
          if (handler != null) {
            Message msg = new Message();
            msg.what = _progress;
            if (file != null) {
              file.downloadProgress = _progress;
              Log.info("cachefile 162");
            }
            msg.obj = file;
            handler.sendMessage(msg);
          } else {
            mWeakDownLoadProcessHandler = null;
          }
        }
      }
    }
  }

  /**
   * 设置文件列表
   * 
   * @param _fileArray
   */
  public void setFileList(ArrayList<MyFile> _fileList) {
    if (_fileList != null && _fileList.size() > 0) {
      synchronized (mFileArray) {
        synchronized (downloadFiles) {
          stopCurrentDownload(false);
          downloadFiles.clear();
        }
        // emptyRootFolder();
        mFileArray.clear();
        mFileArray.addAll(_fileList);
      }
    }
  }

  private ArrayList<MyFile> downloadFiles = new ArrayList<MyFile>();

  /**
   * 开始准备第index个文件，当上一个任务未完成，停止上一个任务后，进行新任务。
   * 
   * @param index
   * @param callback
   *          通知文件准备完毕回调函数
   */
  public void startTask(int index) {
    synchronized (mFileArray) {
      if (index >= 0 && mFileArray.size() > index && rootFolder != null && rootFolder.exists() == true) {
        int startIndex = index - KEEP_FILE_COUNT;
        if (startIndex < 0) {
          startIndex = 0;
        }
        int endIndex = index + PREFETCH_FILE_COUNT;
        if (endIndex >= mFileArray.size()) {
          endIndex = mFileArray.size() - 1;
        }
        ArrayList<MyFile> needDownload = new ArrayList<MyFile>();
        ArrayList<String> tmpNames = new ArrayList<String>();
        for (int i = startIndex; i <= endIndex; i++) {
          MyFile file = mFileArray.get(i);
          if (file.storageName == null && file.fileURL != null) {
            file.storageName = convertURL2Filename(file.fileURL);
          }
          if (file.storageName != null) {
            tmpNames.add(file.storageName);
            if (i >= index) {
              needDownload.add(file);
            }
          }
        }
        File[] files = rootFolder.listFiles();
        if (files != null && files.length > 0) {
          for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".xhrTemp") == false) {
              boolean found = false;
              int j = 0;
              for (j = 0; j < tmpNames.size(); j++) {
                if (files[i].getName().compareTo(tmpNames.get(j)) == 0) {
                  found = true;
                  break;
                }
              }
              if (found == true) {
                if (j >= (index - startIndex)) {
                  sendHadReadyHandler(0, mFileArray.get(startIndex + j));
                  int tmp = needDownload.indexOf(mFileArray.get(startIndex + j));
                  if (tmp >= 0) {
                    needDownload.remove(tmp);
                  }
                }
              } else {
                files[i].delete();
              }
            }
          }
        }
        tmpNames.clear();

        synchronized (downloadFiles) {

          if (currentDownLoadFile != null) {
            synchronized (currentDownLoadFile) {
              if (currentDownLoadFile != null) {
                if (currentDownLoadFile.fileURL.compareTo(needDownload.get(0).fileURL) != 0) {
                  currentDownLoadFile.downloadIsStop.isStop = true;
                } else {
                  needDownload.remove(0);
                }
              }
            }
          }
          downloadFiles.clear();
          downloadFiles.addAll(needDownload);
          needDownload.clear();

          if (mDownloadThread == null) {
            mDownloadThread = new DownloadThread();
            mDownloadThread.start();
          }
        }
      }
    }
  }

  private MyFile currentDownLoadFile = null;

  private class DownloadThread extends Thread {
    @Override
    public void run() {
      super.run();
      try {
        while (isRunThread == true) {
          synchronized (downloadFiles) {
            if (downloadFiles.size() > 0) {
              File file = new File(rootFolder.getAbsolutePath(), downloadFiles.get(0).storageName);
              if (file.exists() == false) {
                currentDownLoadFile = downloadFiles.get(0);
              }
              downloadFiles.remove(0);
            }
          }
          if (currentDownLoadFile != null) {
            xhrResponse response = null;
            if (currentDownLoadFile.downloadIsStop.isStop == false) {
              xhr.xhrParameter param = new xhr.xhrParameter();
              param.url = currentDownLoadFile.fileURL;
              param.saveType = xhr.SAVE_TYPE_LOCAL_FILE;
              param.savePath = rootFolder.getAbsolutePath();
              param.saveFileName = currentDownLoadFile.storageName;
              response = xhr.requestToServer(param, new xhr.xhrProgress() {

                @Override
                public void progress(long _progress, xhrParameter param) {

                  if (param != null && param.url != null && param.url.length() > 0
                      && currentDownLoadFile != null
                      && currentDownLoadFile.fileURL.compareTo(param.url) == 0) {
                    sendDownLoadProcessHandler((int) (_progress * 100 / currentDownLoadFile.fileSize), currentDownLoadFile);
                  }
                }
              }, currentDownLoadFile.downloadIsStop);
            }
            synchronized (currentDownLoadFile) {
              if (response != null && response.code == 200) {
                sendHadReadyHandler(0, currentDownLoadFile);
              } else {
                sendHadReadyHandler(1, currentDownLoadFile);
              }
              currentDownLoadFile.downloadIsStop.isStop = false;
              currentDownLoadFile.notifyAll();
              currentDownLoadFile = null;
            }
          }
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      } catch (Exception e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } finally {
        if (currentDownLoadFile != null) {
          synchronized (currentDownLoadFile) {
            if (currentDownLoadFile != null) {
              currentDownLoadFile.downloadIsStop.isStop = false;
              currentDownLoadFile.notifyAll();
              currentDownLoadFile = null;
            }
          }
        }
        mDownloadThread = null;
      }
    }
  }

  /**
   * 停止上一次任務
   */
  private void stopCurrentDownload(boolean needWait) {
    if (currentDownLoadFile != null) {
      synchronized (currentDownLoadFile) {
        if (currentDownLoadFile != null) {
          currentDownLoadFile.downloadIsStop.isStop = true;
          if (needWait == true) {
            try {
              currentDownLoadFile.wait();
            } catch (InterruptedException e) {
              ConfigureLog4J.printStackTrace(e, Log);
            }
          }
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    isRunThread = false;
    synchronized (downloadFiles) {
      stopCurrentDownload(true);
      downloadFiles.clear();
    }
    if (mFileArray != null) {
      mFileArray.clear();
    }

    super.finalize();
  }

}
