package com.xormedia.mylib;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class HaveSelectBaseAdapter<T> extends BaseAdapter {
  private static Logger Log = Logger.getLogger(HaveSelectBaseAdapter.class);
  public Context mContext = null;
  public String identifyAttrName = null;
  public Field identifyAttrField = null;
  public boolean singleSelect = true; // true:单选，false：多选
  public ArrayList<String> seleteList = null;
  public ArrayList<T> mData = null;

  public HaveSelectBaseAdapter(Context _context, ArrayList<T> _data) {
    mContext = _context;
    mData = _data;
  }

  public HaveSelectBaseAdapter(Context _context, ArrayList<T> _data,
      boolean _singleSelect, String _identifyAttrName, ArrayList<String> _seleteList) {
    mContext = _context;
    mData = _data;
    singleSelect = _singleSelect;
    if (_seleteList != null) {
      seleteList = new ArrayList<String>();
      seleteList.addAll(_seleteList);
    }
    if (_identifyAttrName != null) {
      identifyAttrName = _identifyAttrName;
      if (seleteList == null) {
        seleteList = new ArrayList<String>();
      }
    }
  }

  public void setSelectList(ArrayList<String> _seleteList) {
    if (_seleteList != null && _seleteList != seleteList) {
      seleteList.clear();
      seleteList.addAll(_seleteList);
      this.notifyDataSetChanged();
    }
  }

  public void addSelect(String identifyAttrValue) {
    if (seleteList != null && identifyAttrValue != null) {
      if (singleSelect == true) {
        seleteList.clear();
        seleteList.add(identifyAttrValue);
      }
      else {
        boolean found = false;
        for (int i = 0; i < seleteList.size(); i++) {
          if (identifyAttrValue.compareTo(seleteList.get(i)) == 0) {
            found = true;
            break;
          }
        }
        if (found == false) {
          seleteList.add(identifyAttrValue);
        }
      }
    }
  }

  public void removeSelect(String identifyAttrValue) {
    if (seleteList != null && identifyAttrValue != null) {
      int i = 0;
      while (i < seleteList.size()) {
        if (identifyAttrValue.compareTo(seleteList.get(i)) == 0) {
          seleteList.remove(i);
        }
        else {
          i++;
        }
      }
    }
  }

  public void removeAllSelect() {
    if (seleteList != null) {
      seleteList.clear();
    }
  }

  public boolean isSelect(int position) {
    boolean ret = false;
    if (seleteList != null && identifyAttrName != null && mData != null && seleteList.size() > 0 && mData.size() > position
        && mData.get(position) != null) {
      try {
        if (identifyAttrField == null) {
          Class<?> c = mData.get(position).getClass();
          while (c != null) {
            try {
              identifyAttrField = c.getDeclaredField(identifyAttrName);
            } catch (NoSuchFieldException e) {
            }
            if (identifyAttrField != null) {
              break;
            }
            else {
              c = c.getSuperclass();
            }
          }
        }
        String strTmp = null;
        if (identifyAttrField != null) {
          strTmp = identifyAttrField.get(mData.get(position)).toString();
        }
        if (strTmp != null) {
          for (int i = 0; i < seleteList.size(); i++) {
            if (strTmp.compareTo(seleteList.get(i)) == 0) {
              ret = true;
              break;
            }
          }
        }
      } catch (IllegalArgumentException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      } catch (IllegalAccessException e) {
        ConfigureLog4J.printStackTrace(e, Log);
      }
    }
    return ret;
  }

  public ArrayList<T> getSeleteList() {
    ArrayList<T> ret = new ArrayList<T>();
    if (seleteList != null && identifyAttrName != null && mData != null && seleteList.size() > 0 && mData.size() > 0) {
      for (int j = 0; j < mData.size(); j++) {
        if (isSelect(j) == true) {
          ret.add(mData.get(j));
        }
      }
    }
    return ret;
  }

  @Override
  protected void finalize() throws Throwable {
    if (seleteList != null) {
      seleteList.clear();
    }
    super.finalize();
  }

}
