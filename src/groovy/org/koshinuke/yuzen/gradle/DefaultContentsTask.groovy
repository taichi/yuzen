package org.koshinuke.yuzen.gradle


import java.io.File;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.*;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.thymeleaf.MarkdownTemplateResolver
import org.koshinuke.yuzen.thymeleaf.YuzenDialect;
import org.koshinuke.yuzen.util.FileUtil
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.resourceresolver.FileResourceResolver;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.TemplateResolver;
import com.google.common.io.*;

/**
 * @author taichi
 */
class DefaultContentsTask extends ConventionTask implements WatchableTask {

	@InputFiles
	ConfigurableFileTree contents

	@Input
	String templatePrefix

	@Input
	String templateSuffix

	@OutputDirectory
	File destinationDir

	DefaultContentsTask() {
		this.group = BasePlugin.BUILD_GROUP
	}

	@TaskAction
	def generate() {
		def te = makeEngine()
		// process main contents
		this.getContents().visit([
					visitDir : {
					},
					visitFile : { processFile(te, it) }
				] as FileVisitor)
	}

	def processFile(engine, FileTreeElement file) {
		logger.info("processFile $file.path")
		def ext = Files.getFileExtension(file.path)
		def dest
		if(ext == 'md') {
			dest = processTemplate(engine, file)
		} else {
			dest = calcFileOutput(file)
			dest.parentFile.mkdirs()
			file.copyTo(dest)
		}
		return dest
	}

	void processFile(FileTreeElement file) {
		processFile(makeEngine(), file)
	}

	def calcHtmlOutput(file) {
		def path = FileUtil.removeExtension(file.path)
		def child = "$path"
		if(path.endsWith('index') == false) {
			child += '/index'
		}
		child += '.html'
		new File(this.getDestinationDir(), child)
	}

	def calcFileOutput(file) {
		new File(this.getDestinationDir(), "$file.path")
	}

	void deleteFile(FileTreeElement file) {
		def ext = Files.getFileExtension(file.path)
		def target = null
		if(ext == 'md') {
			target = calcHtmlOutput(file)
		} else {
			target = calcFileOutput(file)
		}
		target?.delete()
	}

	def processTemplate(engine, file) {
		def path = FileUtil.removeExtension(file.path)
		def rel = file.relativePath
		def template = rel.lastName
		if(1 < rel.segments.length) {
			template = rel.segments[0]
		}
		template = FileUtil.removeExtension(template)
		def c = new Context()
		c.setVariables(project.properties)
		c.setVariable('content', [path: path, date: new Date(file.lastModified)])
		def html = calcHtmlOutput(file)
		def htmlPath = html.parentFile.toPath()
		def relPath = htmlPath.relativize(getDestinationDir().toPath())
		c.setVariable('relative', FileUtil.slashify(relPath) + "/")

		html.parentFile.mkdirs()
		html.withWriter("UTF-8") {
			engine.process(template, c, it)
		}
		return html
	}

	def makeEngine() {
		def rr = new FileResourceResolver()
		def md = new MarkdownTemplateResolver(rr)
		md.prefix = this.getContents().dir.toURI().path

		def r = new TemplateResolver()
		r.resourceResolver = rr
		r.templateMode = 'HTML5'
		r.prefix = this.project.file(this.getTemplatePrefix()).toURI().path
		r.suffix = this.getTemplateSuffix()

		def te = new TemplateEngine()
		te.addTemplateModeHandler(StandardTemplateModeHandlers.HTML5)
		te.addTemplateModeHandler(MarkdownTemplateResolver.MARKDOWN)
		te.addTemplateResolver(md)
		te.addTemplateResolver(r)
		te.addDialect(new YuzenDialect(md))
		return te
	}
}
