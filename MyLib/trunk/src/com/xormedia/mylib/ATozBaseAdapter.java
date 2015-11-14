package com.xormedia.mylib;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.SectionIndexer;

@SuppressLint("DefaultLocale")
public abstract class ATozBaseAdapter<T> extends HaveSelectBaseAdapter<T> implements SectionIndexer {
  private static Logger Log = Logger.getLogger(ATozBaseAdapter.class);
  public String mPyFieldName = null;
  public Field mPyField = null;
  private static String a2z = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

  public ATozBaseAdapter(Context _context, ArrayList<T> _data, String pyFieldName) {
    super(_context, _data);
    mPyFieldName = pyFieldName;
  }

  public ATozBaseAdapter(Context _context, ArrayList<T> _data,
      boolean _singleSelect, String _identifyAttrName, ArrayList<String> _seleteList, String pyFieldName) {
    super(_context, _data, _singleSelect, _identifyAttrName, _seleteList);
    mPyFieldName = pyFieldName;
  }

  /**
   * 返回是否显示A-z，且显示的内容
   * 
   * @param position
   * @return null：不显示；非null为显示，且显示返回值。
   */
  public String getShowAToz(int position) {
    String ret = null;
    if (mData != null && mData.size() > position && mData.get(position) != null && mPyFieldName != null && mPyFieldName.length() > 0) {
      try {
        if (mPyField == null) {
          Class<?> c = mData.get(position).getClass();
          while (c != null) {
            try {
              mPyField = c.getDeclaredField(mPyFieldName);
            } catch (NoSuchFieldException e) {
            }
            if (mPyField != null) {
              break;
            }
            else {
              c = c.getSuperclass();
            }
          }
        }
        String strTmp = null;
        if (mPyField != null) {
          strTmp = (String) mPyField.get(mData.get(position));
        }
        String firstChar = "#";
        if (strTmp != null && strTmp.length() > 0 && a2z.contains(strTmp.substring(0, 1))) {
          firstChar = strTmp.toUpperCase().substring(0, 1);
        }
        if (position == 0) {
          ret = firstChar;
        } else {
          String strTmp1 = null;
          if (mPyField != null) {
            strTmp1 = (String) mPyField.get(mData.get(position - 1));
          }
          String preFirstChar = "#";
          if (strTmp1 != null && strTmp1.length() > 0 && a2z.contains(strTmp1.substring(0, 1))) {
            preFirstChar = strTmp1.toUpperCase().substring(0, 1);
          }
          if (firstChar.compareTo(preFirstChar) != 0) {
            ret = firstChar;
          }
        }
      } catch (IllegalAccessException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  @Override
  public int getPositionForSection(int section) {
    int ret = -1;
    if (section == 100000000) {
      ret = 0;
    } else {
      if (mData != null) {
        try {
          for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i) != null) {
              if (mPyField == null) {
                Class<?> c = mData.get(i).getClass();
                while (c != null) {
                  try {
                    mPyField = c.getDeclaredField(mPyFieldName);
                  } catch (NoSuchFieldException e) {
                  }
                  if (mPyField != null) {
                    break;
                  }
                  else {
                    c = c.getSuperclass();
                  }
                }
              }
              String strTmp = null;
              if (mPyField != null) {
                strTmp = (String) mPyField.get(mData.get(i));
              }
              char firstChar = '#';
              if (strTmp != null && strTmp.length() > 0 && a2z.contains(strTmp.substring(0, 1))) {
                firstChar = strTmp.toUpperCase().charAt(0);
              }
              if (firstChar == section) {
                ret = i;
                break;
              }
            }
          }
        } catch (IllegalAccessException e) {
          ConfigureLog4J.printStackTrace(e, Log);
        } 
      }
    }
    return ret;
  }

}
