package io.yuzen;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

/**
 * @author taichi
 */
public class Main {

	public static void main(String[] args) throws Exception {
		Server server = new Server(8080);

		ResourceHandler rh = new ResourceHandler();
		rh.setResourceBase("templates/blog");

		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[] { rh, new DefaultHandler() });

		server.setHandler(hl);

		server.start();
		server.join();
	}
}
