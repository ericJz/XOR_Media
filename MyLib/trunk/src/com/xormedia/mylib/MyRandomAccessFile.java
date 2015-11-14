package com.xormedia.mylib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

public class MyRandomAccessFile {
  private static Logger Log = Logger.getLogger(MyRandomAccessFile.class);

  private static int BUFFER_SIZE = 10 * 1024;
  private String filePath = null;
  private long pos = 0;
  private long size = 0;
  private long nPos = 0;
  private RandomAccessFile oSavedFile = null;

  public boolean exists() {
    return new File(filePath).exists();
  }

  public long getPos() {
    return pos;
  }

  public long getSize() {
    return size;
  }

  public String getFileName() {
    String ret = null;
    if (oSavedFile != null) {
      ret = filePath;
    }
    return ret;
  }

  public long getFileLength() {
    long ret = 0;
    if (oSavedFile != null) {
      try {
        ret = oSavedFile.length();
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public MyRandomAccessFile(String fileName) {
    if (fileName != null && fileName.length() > 0) {
      filePath = fileName;
      try {
        oSavedFile = new RandomAccessFile(filePath, "rw");
      } catch (FileNotFoundException e) {
        ConfigureLog4J.printStackTrace(e, Log);
        oSavedFile = null;
      }
      if (oSavedFile != null) {
        try {
          oSavedFile.seek(pos);
          size = oSavedFile.length() - pos;
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
  }

  public MyRandomAccessFile(String fileName, long pos) {
    if (fileName != null && fileName.length() > 0 && pos >= 0) {
      filePath = fileName;
      try {
        oSavedFile = new RandomAccessFile(fileName, "r");
      } catch (FileNotFoundException e) {
        ConfigureLog4J.printStackTrace(e, Log);
        oSavedFile = null;
      }
      this.pos = pos;
      this.nPos = pos;
      if (oSavedFile != null) {
        try {
          oSavedFile.seek(pos);
          size = oSavedFile.length() - pos;
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }
  }

  public MyRandomAccessFile(String fileName, long pos, long size) {
    if (fileName != null && fileName.length() > 0 && pos >= 0 && size > 0) {
      filePath = fileName;
      try {
        oSavedFile = new RandomAccessFile(fileName, "r");
      } catch (FileNotFoundException e) {
        ConfigureLog4J.printStackTrace(e, Log);
        oSavedFile = null;
      }
      this.pos = pos;
      this.nPos = pos;
      if (oSavedFile != null) {
        try {
          oSavedFile.seek(pos);
          size = oSavedFile.length() - pos;
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
      if (this.size > size) {
        this.size = size;
      }
    }
  }

  public boolean setPosition(long pos, long size) {
    boolean ret = false;
    if (oSavedFile != null && pos >= 0 && size > 0) {
      try {
        if (pos < oSavedFile.length()) {
          oSavedFile.seek(pos);
          this.pos = pos;
          nPos = pos;
          this.size = size;
          if (oSavedFile.length() - nPos < size) {
            this.size = oSavedFile.length() - nPos;
          }
          ret = true;
        }
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public long getCurrentPosition() {
    return nPos;
  }

  public byte[] readBuffer() {
    byte[] buffer = null;
    if (oSavedFile != null) {
      int bufferSize = BUFFER_SIZE;
      if (size - (nPos - pos) < BUFFER_SIZE) {
        bufferSize = (int) (size - (nPos - pos));
      }
      if (bufferSize > 0) {
        buffer = new byte[bufferSize];
      }
      if (buffer != null) {
        try {
          int ret = oSavedFile.read(buffer);
          if (ret != -1) {
            nPos += bufferSize;
          }
        } catch (IOException e) {
          ConfigureLog4J.printStackTrace(e, Log);
          buffer = null;
        }
      }
    }
    return buffer;
  }

  public void writeBuffer(byte[] buffer, int byteOffset, int byteCount) {
    try {
      oSavedFile.write(buffer, byteOffset, byteCount);
    } catch (IOException e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
  }

  public void close() {
    if (oSavedFile != null) {
      try {
        oSavedFile.close();
        oSavedFile = null;
      } catch (IOException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

}
