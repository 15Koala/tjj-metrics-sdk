package org.koala.metrics.core;

import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.koala.metrics.utils.TimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Reporter implements FormatedMetric{

	private String appId;
	private StringBuilder reportData;
	private Map<String, Object> curMetric;
	private String hostname;
	private String curTs;
	private int curSize = 0;

	public static final int MAX_SIZE = 500;
	public static final int MAX_MILLI_SEC = 60 * 1000;
	
	private Object reportDataLock = new Object();
	private Object curMetricLock = new Object();
	
	public void setHostName(String hostname) {
		this.hostname = hostname;
	}
	/**
	 * 用户画图所用的时间横轴，不一定是现在的时间
	 */
	public void setTs(Long ts) {
		curTs = TimeUtil.getDatetime("yyyy-MM-dd'T'HH:mm:ss+08:00", ts);
	}
	
	public Reporter(String appId) {
		this.appId = appId;
		reportData = new StringBuilder();
		curMetric = new ConcurrentHashMap<String,Object>();
	}
	
	public void add(String key, Object value) {
		synchronized(curMetricLock) {
			curMetric.put(key, value);
		}
	}
	
	public void report() {
		synchronized (reportDataLock) {
			synchronized (curMetricLock) {
				curMetric.put("app_id", this.appId);
				curMetric.put("hostname", hostname == null ? AdMonitor.getHostName() : hostname);
				curMetric.put("timestamp", TimeUtil.getCurrentTs());
				curMetric.put("ts", curTs == null ? TimeUtil.getCurDatetime("yyyy-MM-dd'T'HH:mm:ss+08:00") : curTs);

				ObjectMapper mapper = new ObjectMapper();
				StringWriter writer = new StringWriter();
				try {
					mapper.writeValue(writer, curMetric);
				} catch (Exception e) {
				}

				reportData.append("{\"index\": {}}\n").append(writer.toString()).append("\n");
				curSize++;

				curMetric = new ConcurrentHashMap<String, Object>();
			}
		}
	}

	/**
	 *  delete data
	 */
	public void clear() {
		synchronized(reportDataLock) {
			reportData.delete(0, reportData.length());
			curSize = 0;
		}
	}
	/**
	 * return content and delete data
	 * @return
	 */
	public String pop() {
		String r = null;
		synchronized(reportDataLock) {
			r = reportData.toString();
			reportData.delete(0, reportData.length());
			curSize = 0;
		}
		return r;
	}
	
	public int size() {
		return curSize;
	}
	
	public String getFormatedMetric() {
		return reportData.toString();
	}

}
