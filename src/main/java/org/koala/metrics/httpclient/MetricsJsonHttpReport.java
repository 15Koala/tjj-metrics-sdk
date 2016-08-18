package org.koala.metrics.httpclient;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.koala.metrics.core.AdMonitor;
import org.koala.metrics.core.Reporter;
import org.koala.metrics.core.Scene;
import org.koala.metrics.utils.TimeUtil;

public class MetricsJsonHttpReport {

	 //private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	 private final Timer executor = new Timer(true);
	 private long lastSendTimeForReporter = System.currentTimeMillis();
	 	 
	 public void reportPerMin(String url) {
		 report(60, url);
	 }
	 /*
	  * Old function cannot be set as a deamon thread
	  *
	 public  void report(long period, TimeUnit unit, final String url) {
		 executor.scheduleAtFixedRate(new Runnable() {
			
			public void run() {
				try {
					HttpResult result = metricsJsonHttp.execute(url);
					logger.info(result.toString());
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		}, period, period, unit);
	 }
	 */
	 
	public void report(long period, final String url) {

		executor.scheduleAtFixedRate(new TimerTask() {

			public void run() {
				try {
					execute(url);// send metrics data
				} catch (Exception e) {

				}
			}
		}, period * 1000, period * 1000);
	}
	
	
	public void reportBulk(final String url_prefix) {
		long period = 5;// 5轮训一下
		executor.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
					executeReport(url_prefix, false);// send metrics data
				} catch (Exception e) {

				}
			}
		}, period * 1000, period * 1000);
	}
	 
	private String getUrl(String appid, String url_prefix) {
			String dateStr = TimeUtil.getCurDatetime("yyyyMM");
			StringBuilder url = new StringBuilder()
					.append(url_prefix)
					.append("admonitor_")
					.append(dateStr)
					.append("/")
					.append(appid);
			return url.toString();
	}
	
	private String getBulkUrl(String appid, String url_prefix) {
		String dateStr = TimeUtil.getCurDatetime("yyyyMM");
		StringBuilder url = new StringBuilder()
				.append(url_prefix)
				.append("admonitor_")
				.append(dateStr)
				.append("/")
				.append(appid)
				.append("/_bulk");
		return url.toString();
	}
	 
	 public void execute(String url) {
		 // 获取度量数据，调用http client
		 Map<String,Scene> scenes = AdMonitor.sceneMap();
		 for(String appId:scenes.keySet()) {
			 String json = scenes.get(appId).getFormatedMetric();
			 String tmpUrl = getUrl(appId , url);
			 HttpClient.post(tmpUrl, json);
		 }
	 }
	 
	 // 发送reporter的数据
	 public void executeReport(String url, boolean must) {
		 Map<String,Reporter> reporters = AdMonitor.reportMap();
		 for(String appId:reporters.keySet()) {
			 Reporter r = reporters.get(appId);
			 
			 if(r.size() <= 0) continue;

			 if( must || reporters.size() > Reporter.MAX_SIZE || System.currentTimeMillis() - lastSendTimeForReporter > Reporter.MAX_MILLI_SEC ) {
				 String json = r.pop();
				 int cnt = 0;
				String msg = HttpClient.post(getBulkUrl(appId,url),json);
				 while("error".equals(msg)) {
					 cnt ++;
					 if(cnt > 2 ) break;
					 try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				 }
				 lastSendTimeForReporter = System.currentTimeMillis();
			 }
		 }
	 }

}
