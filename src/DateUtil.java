import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateUtil {
   public static final int PERIOD_TYPE_YEAR = 0;

   public static final int PERIOD_TYPE_MONTH = 1;

   public static final int PERIOD_TYPE_HALFMONTH = 2;

   public static final int PERIOD_TYPE_WEEK = 3;

   public static final int MICROSECONDS_OF_HOUR = 3600000;

   public static final String[] DATEREG_ARRAY = {"(\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d.*)",
      "(\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d.*)", "(\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d.*)",
      "(\\d\\d\\d\\d-\\d\\d-\\d\\d.*)", "(\\d\\d\\d\\d\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d.*)",
      "(\\d\\d\\d\\d\\d\\d\\d\\d\\s\\d\\d:\\d\\d.*)", "(\\d\\d\\d\\d\\d\\d\\d\\d\\s\\d\\d.*)",
      "(\\d\\d\\d\\d\\d\\d\\d\\d.*)", "(\\d\\d\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d.*)",
      "(\\d\\d\\d\\d\\d\\d\\s\\d\\d:\\d\\d.*)", "(\\d\\d\\d\\d\\d\\d\\s\\d\\d.*)", "(\\d\\d\\d\\d\\d\\d.*)",
      "(\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d.*)", "(\\d\\d\\d\\d\\s\\d\\d:\\d\\d.*)", "(\\d\\d\\d\\d\\s\\d\\d.*)",
      "(\\d\\d\\d\\d.*)"};

   public static final int MICROSECONDS_OF_DAY = MICROSECONDS_OF_HOUR * 24;

   public static final long MICROSECONDS_OF_YEAR = 31622400000L;
   
   public static final String FORMAT_1 = "yyyy-MM-dd";

   public static final String FORMAT_2 = "MM-dd-yy";

   public static final String FORMAT_3 = "MM/dd/yy";

   public static final String FORMAT_4 = "MM/dd/yyyy";

   public static final String FORMAT_5 = "dd-MM-yy";

   public static final String FORMAT_6 = "dd/MM/yy";

   public static final String FORMAT_7 = "dd/MM/yyyy";

   public static final String SQL_FORMATE = "yyyy-MM-dd";

   public static final String SQL_FULL_FORMATE = "yyyy-MM-dd HH:mm:ss.SSS";

   public static final String SQL_MINI_FORMATE = "yyyy-MM-dd HH:mm";

   public static final int DURATION_DAY = 24 * 60;

   public static final int DURATION_HOUR = 60;
   
   public static boolean isEmpty(Object srcStr) {
      
      return nvl(srcStr, "").trim().length() == 0 || nvl(srcStr, "").equals("null");
   }
   
   public static String nvl(Object src, String alt) {
      if (src == null) {
         return alt;
      } else {
         return nvl(src.toString(), alt);
      }
   }
   
   public static String nvl(String srcStr, String objStr) {
      if (srcStr == null || 0 == srcStr.trim().length() || "null".equalsIgnoreCase(srcStr.trim())) {
         return objStr;
      } else {
         return srcStr;
      }
   }
   
   public static Date parse(String dateStr) {
      
      return parse(dateStr, FORMAT_1);
   }
         
   public static Date parse(String dateStr, String format) {
      if (isEmpty(dateStr)) {
         return null;
      }
      SimpleDateFormat simpleDateParser = new SimpleDateFormat(FORMAT_1, Locale.US);
      simpleDateParser.set2DigitYearStart(Timestamp.valueOf("2000-01-01 00:00:00.000000000"));
      simpleDateParser.setLenient(false);
      simpleDateParser.applyPattern(format);
      return simpleDateParser.parse(dateStr, new ParsePosition(0));
   }
   
   public static long compareTo(Date date1, Date date2) {
      return compareTo(date1, date2, Calendar.DATE);
   }

   public static long compareTo(Date date1, Date date2, int field) {
      if (date1 == null) {
         return Integer.MIN_VALUE;
      } else if (date2 == null) {
         return Integer.MAX_VALUE;
      } else if (date1 == date2 || date1.equals(date2)) {
         return 0;
      } else {
         if (field == Calendar.HOUR || field == Calendar.HOUR_OF_DAY) {
            return getTimeDelta(trimDate(date1, Calendar.HOUR_OF_DAY), trimDate(date2,
               Calendar.HOUR_OF_DAY)) /
               MICROSECONDS_OF_HOUR;
         } else if (field == Calendar.DATE) {
            return getTimeDelta(trimDate(date1), trimDate(date2)) / MICROSECONDS_OF_DAY;
         } else if (field == Calendar.MONTH) {
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();
            if (date1.before(date2)) {
               fromCal.setTime(date1);
               toCal.setTime(date2);
            } else {
               fromCal.setTime(date2);
               toCal.setTime(date1);
            }
            int fromYear = fromCal.get(Calendar.YEAR);
            int fromMonth = fromCal.get(Calendar.MONTH);
            int toYear = toCal.get(Calendar.YEAR);
            int toMonth = toCal.get(Calendar.MONTH);

            long months = (toYear - fromYear + 1) * 12 - fromMonth - (12 - toMonth);
            if (date1.before(date2)) {
               return -months;
            } else {
               return months;
            }
         } else if (field == Calendar.YEAR) {
            return getDateFieldValue(date1, Calendar.YEAR) - getDateFieldValue(date2, Calendar.YEAR);
         } else if (field == Calendar.MINUTE) {
            return getTimeDelta(date1, date2) / (1000 * 60);
         } else if (field == Calendar.DAY_OF_WEEK) {
            date1 = DateUtil.getNextDate(date1, Calendar.DATE, 1 - DateUtil.getDateFieldValue(date1,
               Calendar.DAY_OF_WEEK));
            date2 = DateUtil.getNextDate(date2, Calendar.DATE, 1 - DateUtil.getDateFieldValue(date2,
               Calendar.DAY_OF_WEEK));
            return getTimeDelta(trimDate(date1), trimDate(date2)) / (MICROSECONDS_OF_DAY * 7);
         } else if (field == Calendar.DAY_OF_YEAR) {
            return date1.getYear() - date2.getYear();
         } else {
            return date1.compareTo(date2);
         }
      }
   }
   
   public static long getTimeDelta(Date date1, Date date2) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date1);

      Calendar calendar2 = Calendar.getInstance();
      calendar2.setTime(date2);

      long date1Time = calendar.getTimeInMillis() + calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
      long date2Time = calendar2.getTimeInMillis() + calendar2.getTimeZone().getOffset(calendar2.getTimeInMillis());

      long offset = date1Time - date2Time;
      return offset;
   }
   
   public static int getDateFieldValue(Date date, int field) {
      Calendar cl = Calendar.getInstance();
      cl.setTime(date);
      return cl.get(field);
   }
   
   public static Date getNextDate(Date date, int type, int duration) {
      if (date == null) {
         return null;
      }

      java.util.Calendar calendar = java.util.Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(type, duration);

      return new Timestamp(calendar.getTimeInMillis());
   }
   
   public static Date trimDate(Date date) {
      return trimDate(date, Calendar.DATE);
   }

   /**
    * Tries to trim date,
    * 
    * @param date
    * @return
    */
   public static Date trimDate(Date date, int type) {
      if (date == null) {
         return null;
      }
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date.getTime());
      switch (type) {
      case Calendar.YEAR:
         cal.set(Calendar.MONTH, 0);
      case Calendar.MONTH:
         cal.set(Calendar.DATE, 0);
      case Calendar.DATE:
         cal.set(Calendar.HOUR_OF_DAY, 0);
      case Calendar.HOUR_OF_DAY:
         break;
      case Calendar.HOUR:
         break;
      case Calendar.MINUTE:
         break;
      default:
         cal.set(Calendar.HOUR_OF_DAY, 0);
      }

      if (Calendar.MINUTE != type) {
         cal.set(Calendar.MINUTE, 0);
      }
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);

      return new Timestamp(cal.getTimeInMillis());
   }
}
