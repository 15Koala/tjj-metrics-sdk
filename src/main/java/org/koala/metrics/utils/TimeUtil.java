package org.koala.metrics.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * @author qiuwenyi
 *
 */
public class TimeUtil {
		public static String getCurDatetime() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		}
		
		public static String getCurDatetime(String format) {
			return new SimpleDateFormat(format).format(new Date());
		}
		
		public static long getCurrentTs() {
			return new Date().getTime();
		}
		
		public static String getDatetime(String format, Long ts) {
			return new SimpleDateFormat(format).format(ts);
		}
		
}
