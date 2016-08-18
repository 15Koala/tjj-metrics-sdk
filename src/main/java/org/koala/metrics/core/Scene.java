package org.koala.metrics.core;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedMap;

import org.koala.metrics.utils.TimeUtil;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Scene implements FormatedMetric{
		
	protected String appId;
	private final MetricRegistry metricRegistry = new MetricRegistry();
	
	public Scene(Integer appId) {
		this.appId = appId.toString();
	}
	
	public Scene(String appId) {
		this.appId = appId;
	}
	// meter 
	public void meter(String dimVal) {
		dimVal = new StringBuilder().append("1").append(dimVal).toString();
		metricRegistry.meter(dimVal).mark();
	}


	public void meter(String dimVal, Integer weight){
		dimVal = new StringBuilder().append("1").append(dimVal).toString();
		metricRegistry.meter(dimVal).mark((long)weight);
	}
	// count
	public void counter(String dimVal) {
		dimVal = new StringBuilder().append("4").append(dimVal).toString();
		metricRegistry.counter(dimVal).inc();
	}


	public void counter(String dimVal, Integer weight){
		dimVal = new StringBuilder().append("4").append(dimVal).toString();
		metricRegistry.counter(dimVal).inc((long)weight);
	}
	

	// gauge
	public void gauge(String dimVal, final Collection<?> c){
		dimVal = new StringBuilder().append("0").append(dimVal).toString();
		Gauge<Integer> gauge = new Gauge<Integer>() {
            
            public Integer getValue() {
                return c.size();
            }
        };
		metricRegistry.register(dimVal, gauge);
	}
	
	public void gauge(String dimVal, final Double c){
		dimVal = new StringBuilder().append("0").append(dimVal).toString();
		Gauge<Double> gauge = new Gauge<Double>() {
            
            public Double getValue() {
                return c;
            }
        };
		metricRegistry.register(dimVal, gauge);
	}
	
	public void gauge(String dimVal, final String c){
		dimVal = new StringBuilder().append("0").append(dimVal).toString();
		Gauge<String> gauge = new Gauge<String>() {
            
            public String getValue() {
                return c;
            }
        };
		metricRegistry.register(dimVal, gauge);
	}

	public void gauge(String dimVal, Gauge<?> gauge){
		dimVal = new StringBuilder().append("0").append(dimVal).toString();
		metricRegistry.register(dimVal, gauge);
	}
	
	public void histogram(String dimVal){
		dimVal = new StringBuilder().append("2").append(dimVal).toString();
		metricRegistry.histogram(dimVal).update(1);
	}
	
	public void histogram(String dimVal, Integer weight){
		dimVal = new StringBuilder().append("2").append(dimVal).toString();
		metricRegistry.histogram(dimVal).update(weight);;
	}
	// timer
	public Timer.Context timer(String dimVal){
		dimVal = new StringBuilder().append("3").append(dimVal).toString();
		Timer t  = metricRegistry.timer(dimVal);
		return t.time();
	}
	
	// 计算UV
	public void unique(String dimVal, String key) {
		
	}
		
	// 单个Scene的json
	@SuppressWarnings("rawtypes")
	public String getFormatedMetric() {
		HashMap<String,Object> r = new HashMap<String,Object>();
		r.put("app_id", this.appId);
		r.put("hostname", AdMonitor.getHostName());
		r.put("timestamp", TimeUtil.getCurrentTs());
		r.put("ts", TimeUtil.getCurDatetime("yyyy-MM-dd'T'HH:mm:ss+08:00"));
		SortedMap<String,Gauge> g = metricRegistry.getGauges();
		for(String dim:g.keySet()) {
			r.put(new StringBuilder().append(dim).append("#gauge_value").toString().substring(1), g.get(dim).getValue());
		}
		SortedMap<String,Meter> m = metricRegistry.getMeters();
		for(String dim:m.keySet()) {
			Meter tm = m.get(dim);
			r.put(new StringBuilder().append(dim).append("#meter_count").toString().substring(1), tm.getCount());
			r.put(new StringBuilder().append(dim).append("#meter_m1_rate").toString().substring(1), tm.getOneMinuteRate());
		}
		SortedMap<String,Counter> c = metricRegistry.getCounters();
		for(String dim:c.keySet()) {
			Counter tm = c.get(dim);
			r.put(new StringBuilder().append(dim).append("#counter_count").toString().substring(1), tm.getCount());
		}
		SortedMap<String,Timer> t = metricRegistry.getTimers();
		for(String dim:t.keySet()) {
			Timer tt = t.get(dim);
			r.put(new StringBuilder().append(dim).append("#timer_count").toString().substring(1), tt.getCount());
			r.put(new StringBuilder().append(dim).append("#timer_m1_rate").toString().substring(1), tt.getOneMinuteRate());
			r.put(new StringBuilder().append(dim).append("#timer_p999").toString().substring(1), tt.getSnapshot().get999thPercentile()/1000000.0);
			r.put(new StringBuilder().append(dim).append("#timer_p99").toString().substring(1), tt.getSnapshot().get99thPercentile()/1000000.0);
			r.put(new StringBuilder().append(dim).append("#timer_p98").toString().substring(1), tt.getSnapshot().get98thPercentile()/1000000.0);
			r.put(new StringBuilder().append(dim).append("#timer_p95").toString().substring(1), tt.getSnapshot().get95thPercentile()/1000000.0);
			r.put(new StringBuilder().append(dim).append("#timer_p75").toString().substring(1), tt.getSnapshot().get75thPercentile()/1000000.0);
			r.put(new StringBuilder().append(dim).append("#timer_p50").toString().substring(1), tt.getSnapshot().getMedian()/1000000.0);
		}
		SortedMap<String,Histogram> h = metricRegistry.getHistograms();
		for(String dim:h.keySet()) {
			Histogram th = h.get(dim);
			r.put(new StringBuilder().append(dim).append("#histogram_p999").toString().substring(1), th.getSnapshot().get999thPercentile());
			r.put(new StringBuilder().append(dim).append("#histogram_p99").toString().substring(1), th.getSnapshot().get99thPercentile());
			r.put(new StringBuilder().append(dim).append("#histogram_p98").toString().substring(1), th.getSnapshot().get98thPercentile());
			r.put(new StringBuilder().append(dim).append("#histogram_p95").toString().substring(1), th.getSnapshot().get95thPercentile());
			r.put(new StringBuilder().append(dim).append("#histogram_p75").toString().substring(1), th.getSnapshot().get75thPercentile());
			r.put(new StringBuilder().append(dim).append("#histogram_p50").toString().substring(1), th.getSnapshot().getMedian());
		}
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		try {
			mapper.writeValue(writer, r);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

}
