package org.koshinuke.yuzen.gradle

import java.io.File
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.file.DefaultFileTreeElement;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.ProgressLogger
import org.gradle.logging.ProgressLoggerFactory
import org.koshinuke.yuzen.file.PathEventListener
import org.koshinuke.yuzen.file.PathSentinel

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

	def bootServer() {
		this.sentinel = startSentinel()
		this.server = new Server(this.port)
		this.server.stopAtShutdown = true
		Resource.setDefaultUseCaches(false)
		def users = new ResourceHandler()
		users.setBaseResource(Resource.newResource(this.rootDir))

		def yuzens = new ResourceHandler()
		yuzens.setBaseResource(Resource.newClassPathResource(this.templatePrefix))

		def hl = new HandlerList()
		hl.setHandlers([
			users,
			yuzens,
			new DefaultHandler()
		]
		as Handler[])
		this.server.setHandler(hl)
		this.server.start()
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
		this.sentinel.shutdown()
		this.server.stop()
	}

	def startSentinel() {
		PathSentinel sentinel = new PathSentinel()
		eachContents {
			sentinel.watchTree(it.contentsDir.toPath())
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
		this.dependsOn.findAll { it instanceof ContentsTask }.each closure
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
				if(path.startsWith(it.contentsDir.toPath())) {
					closure(it)
				}
			}
		}
	}

	def delete(path) {
		handle(path) {
			it.deleteFile(to(path))
		}
	}
}
