package org.koala.metrics.core;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.koala.metrics.httpclient.MetricsJsonHttpReport;

public class TJJMonitor {
	
	private static Map<String, Scene> Scenes;
	private static Map<String, Reporter> Reporters;
	
	private static String hostName = null;
	private static String url_prefix = "http://elasticsearch:9000/";
	private static MetricsJsonHttpReport report;
	static {
		hostName = getHostName();
		Scenes = new ConcurrentHashMap<String, Scene>();
		Reporters = new ConcurrentHashMap<String, Reporter>();
		report = new MetricsJsonHttpReport();
		report.reportPerMin(url_prefix);
		report.reportBulk(url_prefix);//定时
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				report.execute(url_prefix);
				report.executeReport(url_prefix, true);
			}
		});
	}
	
	public static void setHostName(String host_name) {
		hostName = host_name;
	}
	
	public static Scene getScene(Integer appId) {
		return getScene(String.valueOf(appId));
	}
	
	public static Scene getScene(String appId) {
		Scene scene = Scenes.get(appId);
		if( scene == null ) {
			scene = new Scene(appId);
			Scenes.put(appId, scene);
		}
		return scene;
	}
	
	public static Reporter getReporter(String appId) {
		Reporter report = Reporters.get(appId);
		if( report == null ) {
			report = new Reporter(appId);
			Reporters.put(appId, report);
		}
		return report;
	}
	
	public static Map<String, Scene> sceneMap() {
		return Scenes;
	}
	
	public static Map<String, Reporter> reportMap() {
		return Reporters;
	}
	
	public static String getHostName() {
		if (hostName == null) {
			//hostName = InetAddress.getLocalHost().getHostAddress().toString();
			hostName = ManagementFactory.getRuntimeMXBean().getName();
			if(hostName.indexOf("@")>=0) {
				hostName = hostName.split("@")[1];
			}
		}
		return hostName;
	}
}


