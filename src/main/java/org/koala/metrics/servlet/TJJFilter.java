package org.koala.metrics.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.codahale.metrics.Timer;
import org.koala.metrics.core.Scene;
import org.koala.metrics.core.AdMonitor;

public class TJJFilter implements Filter {

	private String appId;
	private String hostName;
	private Scene TJJRequestSce;
	
	private String filterName;
	public TJJFilter() {

	}



	private static class StatusExposingServletResponse extends
			HttpServletResponseWrapper {
		private int httpStatus = 200;

		public StatusExposingServletResponse(HttpServletResponse response) {
			super(response);
		}

		public void sendError(int sc) throws IOException {
			this.httpStatus = sc;
			super.sendError(sc);
		}

		public void sendError(int sc, String msg) throws IOException {
			this.httpStatus = sc;
			super.sendError(sc, msg);
		}

		public void setStatus(int sc) {
			this.httpStatus = sc;
			super.setStatus(sc);
		}

		public int getStatus() {
			return this.httpStatus;
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		appId = filterConfig.getInitParameter("appId");
		hostName = filterConfig.getInitParameter("hostName");
		if (hostName != null && !hostName.equals("")) {
			AdMonitor.setHostName(hostName);
		}
		TJJRequestSce = AdMonitor.getScene(appId);
		filterName = filterConfig.getFilterName();
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		StatusExposingServletResponse wrappedResponse = new StatusExposingServletResponse(
				(HttpServletResponse) response);
		Timer.Context context = TJJRequestSce.timer(filterName + "_request");
		try {
			chain.doFilter(request, wrappedResponse);
		} finally {
			context.stop();
			TJJRequestSce.meter(filterName + "_requestCode_" + wrappedResponse.getStatus());
		}
	}

	public void destroy() {

	}

}
