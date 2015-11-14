package com.xormedia.mylib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;

@SuppressLint("SimpleDateFormat")
public class TimeUtil {
  private static Logger Log = Logger.getLogger(TimeUtil.class);

  /**
   * 从毫秒转换成字符串返回
   * 
   * @param time
   *          毫秒
   * @return 返回格式 = 小时：分钟：秒(00:00:00)
   */
  public static String formatRingingTime(long time) {
    int hours = (int) (time / 3600000);
    int minutes = (int) ((time / 1000 - hours * 3600) / 60);
    int second = (int) (time / 1000 - hours * 3600 - minutes * 60);
    return (hours > 9 ? hours : ("0" + hours)) + " : " + (minutes > 9 ? minutes : ("0" + minutes)) + " : "
        + (second > 9 ? second : ("0" + second));
  }

  /**
   * 从秒转换成字符串返回
   * 
   * @param time
   *          毫秒
   * @return 返回格式 = 小时：分钟：秒(00:00:00)
   */
  public static String formatSecondsTime(long time) {
    int hours = (int) (time/3600);
    int minutes = (int) ((time - hours * 3600) / 60);
    int second = (int) (time - hours * 3600 - minutes * 60);
    return (hours > 9 ? hours : ("0" + hours)) + " : " + (minutes > 9 ? minutes : ("0" + minutes)) + " : "
    + (second > 9 ? second : ("0" + second));
  }
  
  /**
   * 将当前时间转成字符串
   * 
   * @category 格式 = yyyy年MM月dd日+" "+星期几+" "+HH:mm:ss
   * @return 格式 = yyyy年MM月dd日+" "+星期几+" "+HH:mm:ss
   */
  public static String curDataFormatString() {
    String curTime = "";
    Date date = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy年MM月dd日");
    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH:mm:ss");
    curTime = dateFormatDay.format(date) + " " + getWeekDay(c) + " "
        + dateFormatHour.format(date);
    return curTime;
  }

  /**
   * 将当前日期转成字符串
   * 
   * @return 格式 = yyyy-MM-dd
   */
  public static String curDayFormatString() {
    String curTime = "";
    Date date = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    SimpleDateFormat dateFormatDay = new SimpleDateFormat("yyyy-MM-dd");
    curTime = dateFormatDay.format(date);
    return curTime;
  }

  /**
   * 将当前时间转成字符串
   * 
   * @return HH:mm
   */
  public static String curHourFormatString() {
    String curTime = "";
    Date date = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    SimpleDateFormat dateFormatHour = new SimpleDateFormat("HH:mm");
    curTime = dateFormatHour.format(date);
    return curTime;
  }

  /**
   * 如果是今天，返回HH:mm;如果不是今天，返回星期几
   * @param _time
   *          毫秒
   * @return 如果是今天，返回HH:mm;如果不是今天，返回星期几
   */
  public static String formatMillisecondToString(long _time) {
    String ret = null;
    if (_time > 0) {
      SimpleDateFormat yyyyFormat = new SimpleDateFormat("yyyy-MM-dd");
      String curDataStr = yyyyFormat.format(System.currentTimeMillis());
      String flagDataStr = yyyyFormat.format(_time);
      if (curDataStr.equals(flagDataStr) == true) {
        SimpleDateFormat hhmmFormat = new SimpleDateFormat("HH:mm");
        ret = hhmmFormat.format(_time);
      } else {
        Date tmpdate = new Date(_time);
        Calendar tmp_c = Calendar.getInstance();
        tmp_c.setTime(tmpdate);
        int tmp_DayOfWeekInMonth = tmp_c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        System.out.println("tmp_DayOfWeekInMonth = "+tmp_DayOfWeekInMonth);
        int tmp_year = tmp_c.get(Calendar.YEAR);
        int tmp_month = tmp_c.get(Calendar.MONTH);
        
        Date curDate = new Date();
        Calendar cur_c = Calendar.getInstance();
        cur_c.setTime(curDate);
        int cur_DayOfWeekInMonth = cur_c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int cur_year = cur_c.get(Calendar.YEAR);
        int cur_month = cur_c.get(Calendar.MONTH);
        System.out.println("cur_DayOfWeekInMonth = "+cur_DayOfWeekInMonth);

        if ((tmp_year ==cur_year) && (tmp_month == cur_month) && (tmp_DayOfWeekInMonth == cur_DayOfWeekInMonth)) {
          ret = getWeekDay(tmp_c);
        } else {
          ret = flagDataStr;
        }
      }
    }
    return ret;
  }

  /**
   * _time与当前时间差
   * @param _time
   * @return 小于1小时 ret返回单位为分钟，大于1小时小于一天 ret返回单位为小时，大于1天 ret返回单位为天
   */
  public static String formatMillisecond(long _time) {
    String ret = null;
    if (_time > 0) {
      long currtime = System.currentTimeMillis();
      long dif_time = currtime - _time;
      long days = dif_time / (1000 * 60 * 60 * 24);  
      long hours = (dif_time % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);  
      long minutes = (dif_time % (1000 * 60 * 60)) / (1000 * 60);  
      
      if (days == 0) {
        if (hours == 0) {
          if (minutes > 0) {
            ret = minutes+"分钟前";
          } else {
            ret = "刚刚";
          }
        } else if (hours > 0) {
          ret = hours+"小时前";
        }
      } else if (days > 0) {
        ret = days+"天前";
      }
    }
      
    return ret;
    
  }
  /**
   * 从毫秒转换成字符串返回
   * 
   * @param time
   *          毫秒值
   * @return 返回格式 = 年-月-日 （yyyy-MM-dd）
   */
  public static String formatRingingDayTime(long time) {
    Date date = new Date(time);
    SimpleDateFormat formatter;
    formatter = new SimpleDateFormat("yyyy-MM-dd");
    String ctime = formatter.format(date);
    return ctime;
  }

  /**
   * 从毫秒转换成字符串返回
   * 
   * @param time
   *          毫秒值 pattern 返回字符串模式
   * @return 返回格式 = pattern eg. （yyyy-MM-dd）
   */
  public static String formatRingingDayTime(long time, String pattern) {
    Date date = new Date(time);
    SimpleDateFormat formatter;
    formatter = new SimpleDateFormat(pattern);
    String ctime = formatter.format(date);
    return ctime;
  }

  /**
   * 将字符串转换为毫秒值
   * @param time  时间    eg.("2014-10-22 11:55:00")
   * @param pattern 字符串样式 eg.(yyyy-MM-dd HH:mm:ss)
   * @return
   */
  public static long formatStrToMillis(String time, String pattern) {
    long ret = 0L;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat(pattern);
    try {
      date = formatter.parse(time);
    } catch (ParseException e) {
      // TODO 自动生成的 catch 块
      e.printStackTrace();
    }
    if (date != null) {
      ret = date.getTime();
    }
    return ret;
  }
  
  /**
   * 日期格式转换   eg:("yyyy-MM-dd hh:mm:ss" => "yyyyMMddhh:mm:ss")
   * @param time  日期
   * @param pattern1  原格式   eg:（yyyy-MM-dd hh:mm:ss）
   * @param pattern2  转换目标格式  eg:(yyyyMMddhh:mm:ss)
   * @return
   */
  public static String formatPattern(String time, String pattern1, String pattern2){
    String tmp = null;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat(pattern1);
    try {
      date = formatter.parse(time);
    } catch (ParseException e) {
      // TODO: handle exception
    }
    
    SimpleDateFormat formatter2 = new SimpleDateFormat(pattern2);
    tmp = formatter2.format(date);
    return tmp;
  }
  
  /**
   * 比较两个日期顺序
   * @param time1 日期 1
   * @param time2 日期 2
   * @return 0：time1 == time2   大于0:time1>time2(time1在time2之后)
   *         小于0：time1<time2(time1在time2之前);
   */
  public static int timeCompare(String time1, String time2) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date1 = new Date();
    Date date2 = new Date();
    try {
      date1 = formatter.parse(time1);
      date2 = formatter.parse(time2);
    } catch (ParseException e) {
      // TODO 自动生成的 catch 块
      e.printStackTrace();
    }
    
    return date1.compareTo(date2);
  }
  
  /**
   * 比较两个日期顺序
   * @param time1 日期 1
   * @param time2 日期 2
   * @param pattern 时间格式
   * @return 0：time1 == time2   大于0:time1>time2(time1在time2之后)
   *         小于0：time1<time2(time1在time2之前);
   */
  public static int timeCompare(String time1, String time2, String pattern) {
    SimpleDateFormat formatter = new SimpleDateFormat(pattern);
    Date date1 = new Date();
    Date date2 = new Date();
    try {
      date1 = formatter.parse(time1);
      date2 = formatter.parse(time2);
    } catch (ParseException e) {
      // TODO 自动生成的 catch 块
      e.printStackTrace();
    }
    
    return date1.compareTo(date2);
  }
  
  /**
   * 从毫秒转换成字符串返回
   * 
   * @param time
   *          毫秒值
   * @return 返回格式 = 时：分 （hh:mm）
   */
  public static String formatRingingHourTime(long time) {
    Date date = new Date(time);
    SimpleDateFormat formatter;
    formatter = new SimpleDateFormat("HH:mm");
    String ctime = formatter.format(date);
    return ctime;
  }

  /**
   * 获取指定Calendar下是星期几
   * 
   * @param Calendar
   *          c
   * @return
   */
  public static String getWeekDay(Calendar c) {
    String weekDay = "";
    if (c.get(Calendar.DAY_OF_WEEK) == 1) {
      weekDay = "星期天";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 2) {
      weekDay = "星期一";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 3) {
      weekDay = "星期二";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 4) {
      weekDay = "星期三";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 5) {
      weekDay = "星期四";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 6) {
      weekDay = "星期五";
    } else if (c.get(Calendar.DAY_OF_WEEK) == 7) {
      weekDay = "星期六";
    }
    return weekDay;
  }

  /**
   * 距离给定时间是多少 返回字符串 格式=多少秒前、多少分钟前、多少小时前、昨天、前天或yyyy-MM-dd HH:mm:ss
   * 
   * @param _time
   *          毫秒
   * @return 返回字符串 格式=多少秒前、多少分钟前、多少小时前、昨天、前天或yyyy-MM-dd HH:mm:ss
   */
  public static String formatTime(long _time) {
    String ret = "";
    try {
      Long data = _time;
      Long nowDate = new Date().getTime() / 1000;
      data = data / 1000;
      Long tmp = nowDate - data;
      if (tmp <= 0) {
        ret = "刚刚";
      } else if (tmp <= 60) {
        ret = tmp + "秒前";
      } else if (tmp <= 3600) {
        ret = tmp / 60 + "分钟前";
      } else if (tmp <= 3600 * 24) {
        ret = tmp / 3600 + "小时前";
      } else if (tmp > 3600 * 24 && tmp <= 3600 * 48) {
        ret = "昨天";
      } else if (tmp > 3600 * 48 && tmp <= 3600 * 72) {
        ret = "前天";
      } else {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ret = format.format(new Date(data));
      }
    } catch (Exception e) {
      ConfigureLog4J.printStackTrace(e, Log);
    }
    return ret;
  }

  /**
   * 相差多少
   * 
   * @param _bTime
   *          毫秒级别(起始时间)、_eTime 毫秒级别(结束时间)
   * @return x天x小时x分
   */
  public static String formatTimeDifference(long _bTime, long _eTime) {
    String ret = null;
    if (_bTime > 0 && _eTime > 0 && _bTime <= _eTime) {
      long tmp = _eTime - _bTime;
      long days = tmp / (1000 * 60 * 60 * 24);
      long hours = (tmp - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
      long minutes = (tmp - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
      ret = days + "天" + hours + "小时" + minutes + "分";
    }
    return ret;
  }
}
