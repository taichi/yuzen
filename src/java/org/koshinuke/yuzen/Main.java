package org.koshinuke.yuzen;

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
		Server server = new Server(8081);

		ResourceHandler rh = new ResourceHandler();
		rh.setResourceBase("templates/blog");
		ResourceHandler b = new ResourceHandler();
		b.setResourceBase("build");

		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[] { b, rh, new DefaultHandler() });

		server.setHandler(hl);

		server.start();
		server.join();
	}
}
