package com.vaguehope.rokoro;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cometd.server.CometdServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main extends HttpServlet {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private static final long serialVersionUID = 1978132217845265153L;
	protected static final Logger logger = Logger.getLogger(Main.class.getName());
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final PrintWriter out = resp.getWriter();
		out.println("Empty page desu~");
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public static void main (String[] args) throws Exception {
		ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		servletHandler.setContextPath("/");
		servletHandler.addServlet(new ServletHolder(new Main()), "/api/*");
		servletHandler.addServlet(new ServletHolder(new CometdServlet()), "/cometd/*");
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		String webrootClass = Main.class.getPackage().getName().replace('.', '/') + "/webroot";
		String webrootResource = Main.class.getClassLoader().getResource(webrootClass).toExternalForm();
		resourceHandler.setResourceBase(webrootResource);
		
		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { resourceHandler, servletHandler });
		
		String portString = System.getenv("PORT");
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setMaxIdleTime(29000); // 29 seconds.
		connector.setAcceptors(2);
		connector.setStatsOn(false);
		connector.setLowResourcesConnections(25000);
		connector.setLowResourcesMaxIdleTime(5000);
		connector.setPort(Integer.parseInt(portString));
		
		Server server = new Server();
		server.setHandler(handlers);
		server.addConnector(connector);
		server.start();
		
		logger.info("Server ready.");
		server.join();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
