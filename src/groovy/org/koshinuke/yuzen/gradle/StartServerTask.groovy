package org.koshinuke.yuzen.gradle

import java.io.File
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.file.DefaultFileTreeElement;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.ProgressLogger
import org.gradle.logging.ProgressLoggerFactory
import org.koshinuke.yuzen.YuzenPluginConvention;
import org.koshinuke.yuzen.file.PathEventListener
import org.koshinuke.yuzen.file.PathSentinel
import org.koshinuke.yuzen.reload.PaththroughHandler
import org.koshinuke.yuzen.util.WebSocketUtil;

/**
 * @author taichi
 */
class StartServerTask extends ConventionTask {

	int port = 8080

	@InputDirectory
	File rootDir

	String templatePrefix

	PathSentinel sentinel

	Server server

	URI serverURI
	WebSocketClientFactory factory
	WebSocketClient client

	def bootServer() {
		this.sentinel = startSentinel()
		this.server = new Server(this.port)
		this.server.stopAtShutdown = true
		Resource.setDefaultUseCaches(false)

		def rl = []
		rl.add Resource.newResource(this.rootDir)
		File f = new File(this.templatePrefix)
		if(f.exists()) {
			rl.add Resource.newResource(this.templatePrefix)
		}

		def yuzens = new ResourceHandler()
		yuzens.baseResource = new ResourceCollection( rl as Resource[])
		def ws = new PaththroughHandler()
		this.server.handler = ws

		def hl = new HandlerList()
		hl.setHandlers([
			yuzens,
			new DefaultHandler()
		]
		as Handler[])
		ws.handler = hl
		this.server.start()

		this.serverURI = toServerURI(server)
		this.factory = new WebSocketClientFactory()
		this.factory.start()
		this.client = this.factory.newWebSocketClient()
	}

	def toServerURI(Server s) {
		Connector c = s.getConnectors()[0]
		def host = c.host
		host = host == null ? 'localhost' : host
		new URI("ws://$host:$c.localPort")
	}

	@TaskAction
	def startServer() {
		ProgressLoggerFactory factory = this.services.get(ProgressLoggerFactory)
		ProgressLogger plogger = factory.newOperation(StartServerTask)
		plogger.setDescription("Start Jetty8")
		def version = Server.version
		plogger.setShortDescription("Starting $version")
		plogger.started()
		try {
			bootServer()
			plogger.progress("Running $version at localhost:$port on $rootDir")
			this.server.join()
		} finally {
			plogger.completed()
		}
	}

	def stopServer() {
		this.factory.stop()
		this.sentinel.shutdown()
		this.server.stop()
	}

	def startSentinel() {
		PathSentinel sentinel = new PathSentinel()
		eachContents {
			sentinel.watchTree(it.contents.dir.toPath())
		}
		sentinel.register([
					overflowed : { logger.warn("overflowed ...") },
					created : {
						logger.info("created {}", it.path)
						make(it.path)
					},
					modified : {
						logger.info("modified {}", it.path)
						make(it.path)
					},
					deleted : {
						logger.info("deleted {}", it.path)
						delete(it.path)
					}
				] as PathEventListener)
		sentinel.startUp()
		addShutdownHook { sentinel.shutdown() }
		return sentinel
	}

	def eachContents(Closure closure) {
		this.dependsOn.findAll { it instanceof WatchableTask }.each closure
	}

	def to(Path path) {
		YuzenPluginConvention ypc = this.project.convention.getByType(YuzenPluginConvention)
		File root = ypc.contentsDir
		def segments = root.toPath().relativize(path).toFile().getPath().split(Pattern.quote(File.separator))
		def rel = new RelativePath(Files.isDirectory(path) == false, segments)
		return new DefaultFileTreeElement(path.toFile().absoluteFile, rel)
	}

	def make(path) {
		handle(path) {
			it.processFile(to(path))
		}
	}

	def handle(path, Closure closure) {
		if(Files.isDirectory(path) == false) {
			eachContents {
				def parent = it.contents.dir.toPath()
				if(path.startsWith(parent)) {
					closure(it)
					publish(parent.relativize(path))
				}
			}
		}
	}

	def publish(path) {
		Future<Connection> future = this.client.open(this.serverURI, [
					onOpen: {
						logger.debug('onOpen {}', it)
					},
					onClose: { c, m ->
					},
					onMessage : {
						logger.debug('onMessage {}', it)
					}
				] as WebSocket.OnTextMessage)
		Connection c = future.get(1, TimeUnit.SECONDS)
		try {
			c.sendMessage(path.toString().replace(File.separatorChar, '/' as char))
		} finally {
			WebSocketUtil.close(c)
		}
	}

	def delete(path) {
		handle(path) {
			it.deleteFile(to(path))
		}
	}
}
