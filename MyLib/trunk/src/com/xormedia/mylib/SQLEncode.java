package com.xormedia.mylib;

public class SQLEncode {
  // 需要转换的字符
  private static final String[] replaceValues1 = { "\'", "\"" };
  // 需要转换的字符
  private static final String[] replaceValues2 = { "\'\'", "\"\"" };

  public static String encode(String tmp) {
    String ret = tmp;
    if (ret != null && ret.length() > 0) {
      for (int i = 0; i < replaceValues1.length; i++) {
        ret = ret.replace(replaceValues1[i], replaceValues2[i]);
      }
    }
    return ret;
  }

  // 需要转换的字符
  private static final String[] replaceValuesJSON1 = { "\'" };
  // 需要转换的字符
  private static final String[] replaceValuesJSON2 = { "\'\'" };

  public static String encodeJSONString(String tmp) {
    String ret = tmp;
    if (ret != null && ret.length() > 0) {
      for (int i = 0; i < replaceValuesJSON1.length; i++) {
        ret = ret.replace(replaceValuesJSON1[i], replaceValuesJSON2[i]);
      }
    }
    return ret;
  }

}
