import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class LogRecycle {
   
   private final static Logger LOG = Logger.getLogger(LogRecycle.class);
   
   protected static Properties prop;
   
   protected static void initProp() {
      try {
         //加载配置文件
         prop = new Properties();
         String absolutePath = System.getProperty("user.dir");
         InputStream in = new FileInputStream(absolutePath + "/config.properties");
         prop.load(in);
         in.close();
         //加载Log4j配置信息
         PropertyConfigurator.configure(absolutePath + "/log4j.properties");
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      initProp();
      batchRecycleLog();
   }
   
   /**
    * 批量删除日志文件
    */
   protected static void batchRecycleLog() {
       LOG.debug("---------recycle log file before " + getDelayLogDays() + " days beging-----");
       String prePath = getRoot();
       if (prePath == null || prePath.trim().length() == 0) {
          return;
       }
       File root = new File(prePath);
       final String rootFileNamePre = getFloderNamePre();
       File[] files = root.listFiles(new FilenameFilter() {

          @Override
          public boolean accept(File dir, String name) {
             if (name.toLowerCase().startsWith(rootFileNamePre.toLowerCase())) {
                return true;
             }
             return false;
          }
       });
       if (files != null && files.length != 0) {
          for (File f : files) {
             String dir = f.getAbsolutePath() + getStuffPath();
             File folder = new File(dir);
             recycleLog(folder);
          }
       } else {
          LOG.warn("------no 8thManage log file found in subDir of " + root.getAbsolutePath() + "-----");
       }
       LOG.debug("---------recycle log file before " + getDelayLogDays() + "days end-----");
   }
   
   /**
    * 回收日志文件
    */
   protected static void recycleLog(File folder) {
      LOG.debug("---------recycleLog for folder [" + folder.getAbsolutePath() + "] start-----------");
      File[] files = folder.listFiles(new FilenameFilter() {

         @Override
         public boolean accept(File dir, String name) {
            String[] ns = name.split("\\.");
            if (ns.length <= 2) {
               return false;
            } else {
               String dt = ns[2];
               Date logDate = DateUtil.parse(dt);
               return afterToday(logDate);
            }
         }
      });
      if (files != null && files.length != 0) {
         for (File f : files) {
            f.delete();
            LOG.debug("---------recycleLog file [" + f.getAbsolutePath() + "] sucessful-----------");
         }
      }
      LOG.debug("---------recycle log file " + (files != null ? files.length : 0) + " sucessful.---------");
      LOG.debug("---------recycleLog for folder [" + folder.getAbsolutePath() + "] end-----------");
   }
   
   /**
    * 
    * @param spaceDays
    * @return
    */
   protected static boolean afterToday(Date logDate) {
      Calendar ca = Calendar.getInstance();
      long days = DateUtil.compareTo(ca.getTime(), logDate);
      if (days > getDelayLogDays()) {
         return true;
      } else {
         return false;
      }
   }
   
   /**
    * 获取需要扫描的路径后缀
    * @return
    */
   protected static String getStuffPath() {
      String cupName = System.getenv("COMPUTERNAME");
      
      return "\\8thManage\\logs\\" + cupName + "\\node1\\";
   }
   
   /**
    * 获取需要扫描的根目录
    * @return
    */
   protected static String getRoot() {
      return prop.getProperty("recycle.path.parent");
   }
   
   /**
    * 8thMange应用根目录名称前缀
    * @return
    */
   protected static String getFloderNamePre() {
      return prop.getProperty("recycle.parent.name.start");
   }
   
   /**
    * 定义需要删除N天前的日志
    * @return
    */
   protected static int getDelayLogDays() {
      String days = prop.getProperty("recycle.delay.days");
      Integer ds =  Integer.valueOf(days);
      return ds == null ? 3 : ds.intValue();
   }

   public static Properties getProp() {
      return prop;
   }

   public static void setProp(Properties prop) {
      LogRecycle.prop = prop;
   }
}
