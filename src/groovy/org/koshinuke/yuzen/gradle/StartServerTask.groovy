package org.koshinuke.yuzen.gradle

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

/**
 * @author taichi
 */
class StartServerTask extends ConventionTask {

	int port = 8080

	File rootDir

	String templatePrefix

	@TaskAction
	def bootServer() {
		Server server = new Server(port)
		server.stopAtShutdown = true
		def users = new ResourceHandler()
		users.setBaseResource(Resource.newResource(rootDir))

		def yuzens = new ResourceHandler()
		yuzens.setBaseResource(Resource.newClassPathResource(templatePrefix))

		def hl = new HandlerList()
		hl.setHandlers([
			users,
			yuzens,
			new DefaultHandler()
		]
		as Handler[])
		server.setHandler(hl)
		server.start()
		server.join()
	}
}
