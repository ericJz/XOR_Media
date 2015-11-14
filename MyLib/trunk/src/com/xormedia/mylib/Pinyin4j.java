package com.xormedia.mylib;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class Pinyin4j {
  private static Logger Log = Logger.getLogger(Pinyin4j.class);
  private static HanyuPinyinOutputFormat hanYuPinOutputFormat;

  /**
   * INITIAL 首字母
   * FULL 全拼
   */
  public static enum Mode {
    INITIAL, FULL
  }
  public Pinyin4j() {
    if (hanYuPinOutputFormat == null) {
      hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

      // 输出设置，大小写，音标方式等
      hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
      hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
      hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }
    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          PinyinHelper.toHanyuPinyinStringArray(new String("好").toCharArray()[0], hanYuPinOutputFormat);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
          ConfigureLog4J.printStackTrace(e, Log);
        }
      }
    }).start();
  }

  @SuppressLint("DefaultLocale")
  public static String getPinyinEx(String name, Mode mode) {
    String namePinyin = "";
    boolean havePinyin = false;
    if (hanYuPinOutputFormat == null) {
      hanYuPinOutputFormat = new HanyuPinyinOutputFormat();

      // 输出设置，大小写，音标方式等
      hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
      hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
      hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
    }
    if (name != null && name.length() > 0) {
      for (int i = 0; i < name.length(); i++) {
        String tmp = name.substring(i, i + 1);
        if ((tmp.compareTo("a") >= 0 && tmp.compareTo("z") <= 0) ||
            (tmp.compareTo("A") >= 0 && tmp.compareTo("Z") <= 0) ||
            (tmp.compareTo("0") >= 0 && tmp.compareTo("9") <= 0)) {
          namePinyin += tmp.toUpperCase();
        }
        else if (tmp.compareTo(" ") != 0) {
          try {
            String[] pinyin = PinyinHelper.toHanyuPinyinStringArray(tmp.toCharArray()[0], hanYuPinOutputFormat);
            if (pinyin == null) {
              namePinyin += tmp;
            } else {
              havePinyin = true;
              // if (namePinyin.endsWith(",") == false) {
              // namePinyin += ",";
              // }
              // namePinyin += pinyin[0] + tmp + ",";
              if (mode.equals(Mode.INITIAL)) {
                namePinyin += pinyin[0].substring(0, 1);
              } else if (mode.equals(Mode.FULL)) {
                namePinyin += pinyin[0];
              }
            }
          } catch (BadHanyuPinyinOutputFormatCombination e) {
            ConfigureLog4J.printStackTrace(e, Log);
          }
        }
      }
    }
    // if (namePinyin.startsWith(",") == true) {
    // namePinyin = namePinyin.substring(1);
    // }
    // if (namePinyin.endsWith(",") == true) {
    // namePinyin = namePinyin.substring(0, namePinyin.length() - 1);
    // }
    if (havePinyin == true) {
      namePinyin += "|" + name;
    }
    return namePinyin;
  }

  /**
   * 获取拼音
   */
//  public static String getPinyin(String src, int a) {
//    if (src != null && !src.trim().equalsIgnoreCase("")) {
//      char[] srcChar;
//      srcChar = src.toCharArray();
//      // 汉语拼音格式输出类
//      HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
//
//      // 输出设置，大小写，音标方式等
//      hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//      hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//      hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
//
//      String[][] temp = new String[src.length()][];
//      for (int i = 0; i < srcChar.length; i++) {
//        char c = srcChar[i];
//        // 是中文或者a-z或者A-Z转换拼音(我的需求，是保留中文或者a-z或者A-Z)
//        if (String.valueOf(c).matches("[\\u4E00-\\u9FA5]+")) {
//          try {
//            temp[i] = PinyinHelper.toHanyuPinyinStringArray(srcChar[i], hanYuPinOutputFormat);
//          } catch (BadHanyuPinyinOutputFormatCombination e) {
//            ConfigureLog4J.printStackTrace(e, Log);
//          }
//        } else /*
//                * if (((int) c >= 65 && (int) c <= 90) || ((int) c >= 97 &&
//                * (int) c <= 122))
//                */{
//          temp[i] = new String[] { String.valueOf(srcChar[i]) };
//        }
//        // else {
//        // temp[i] = new String[] { "" };
//        // }
//      }
//      String[] pingyinArray = Exchange(temp);
//      return pingyinArray[0];
//    }
//    return "";
//  }

  /**
   * 递归
   */
//  public static String[] Exchange(String[][] strJaggedArray) {
//    String[][] temp = DoExchange(strJaggedArray);
//    return temp[0];
//  }

  /**
   * 递归
   */
//  private static String[][] DoExchange(String[][] strJaggedArray) {
//    int len = strJaggedArray.length;
//    if (len >= 2) {
//      int len1 = strJaggedArray[0].length;
//      int len2 = strJaggedArray[1].length;
//      int newlen = len1 * len2;
//      String[] temp = new String[newlen];
//      int Index = 0;
//      for (int i = 0; i < len1; i++) {
//        for (int j = 0; j < len2; j++) {
//          temp[Index] = strJaggedArray[0][i] + strJaggedArray[1][j];
//          Index++;
//        }
//      }
//      String[][] newArray = new String[len - 1][];
//      for (int i = 2; i < len; i++) {
//        newArray[i - 1] = strJaggedArray[i];
//      }
//      newArray[0] = temp;
//      return DoExchange(newArray);
//    } else {
//      return strJaggedArray;
//    }
//  }

  /**
   * 判断字符串 str 是否是英文
   * 
   * @param str
   * @return true str是英文 false str是中文
   */
  // public static boolean isEn(String str) {
  // boolean ret = false;
  // if (str != null && str.length() > 0) {
  // ret = true;
  // for (int j = 0; j < str.length(); j++) {
  // int i = (int) str.charAt(j);
  // if (!((i >= 65 && i <= 90) || (i >= 97) && i <= 122)) {
  // ret = false;
  // break;
  // }
  // }
  // }
  // return ret;
  // }
}
