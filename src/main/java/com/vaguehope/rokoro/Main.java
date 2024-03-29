package com.vaguehope.rokoro;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.cometd.Channel;
import org.cometd.Client;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.mortbay.cometd.continuation.ContinuationCometdServlet;

public class Main {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	protected static final Logger logger = Logger.getLogger(Main.class.getName());
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static void main (String[] args) throws Exception {
		// Servlet container.
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/");
		
		// CometD.
		ContinuationCometdServlet cometdServlet = new ContinuationCometdServlet();
		servletHandler.addServlet(new ServletHolder(cometdServlet), "/cometd/*");
		
		// Static files on classpath.
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		String webrootClass = Main.class.getPackage().getName().replace('.', '/') + "/webroot";
		String webrootResource = Main.class.getClassLoader().getResource(webrootClass).toExternalForm();
		resourceHandler.setResourceBase(webrootResource);
		
		// Prepare final handler.
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, servletHandler });
		
		// Listening connector.
		String portString = System.getenv("PORT");
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(30000); // 30 seconds.
		connector.setAcceptors(2);
		connector.setStatsOn(false);
		connector.setLowResourcesConnections(1000);
		connector.setLowResourcesMaxIdleTime(5000); // 5 seconds.
		connector.setPort(Integer.parseInt(portString));
		
		// Start server.
		Server server = new Server();
		server.setHandler(handlers);
		server.addConnector(connector);
		server.start();
		
		// This must be done after server is started getBayeux() is null before then.
		cometdServlet.getBayeux().setTimeout(20000); // set long-poll timeout to 20 seconds.
		
		// Demo server interaction.
		final Client client = cometdServlet.getBayeux().newClient("server");
		final Channel ch = cometdServlet.getBayeux().getChannel("/hello", true);
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run () {
				Map<String, Object> data = new HashMap<String, Object>();
				String msg = "server[" + new Date() + "]";
				data.put("greeting", msg);
				ch.publish(client, data, null);
				System.out.println("send msg: " + msg);
			}
		}, 10000, 60000);
		
		// Wait for server thread.
		logger.info("Server ready.");
		server.join();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
