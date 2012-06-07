package org.koshinuke.yuzen.gradle

import groovy.lang.Closure;

import java.io.File;

import org.gradle.api.file.FileTreeElement
import org.gradle.api.file.FileVisitor;
import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.*;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.thymeleaf.MarkdownTemplateResolver
import org.koshinuke.yuzen.thymeleaf.YuzenDialect;
import org.koshinuke.yuzen.util.FileUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.resourceresolver.FileResourceResolver;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.TemplateResolver;
import com.google.common.io.*;

/**
 * @author taichi
 */
class DefaultContentsTask extends ConventionTask implements ContentsTask {

	@InputDirectory
	File contentsDir

	@Input
	String templatePrefix

	@Input
	String templateSuffix

	@OutputDirectory
	File destinationDir

	/**
	 * The given closure is used to configure the filter.A {@link org.gradle.api.tasks.util.PatternFilterable} is
	 * passed to the closure as it's delegate
	 */
	Closure contentsFilter = { exclude "fragments/**" }

	DefaultContentsTask() {
		this.group = BasePlugin.BUILD_GROUP
	}

	@TaskAction
	def generate() {
		def te = makeEngine()
		// process main contents
		this.project.fileTree(this.getContentsDir(), this.getContentsFilter()).visit([
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
		new File(this.getDestinationDir(), "$path/index.html")
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
		html.parentFile.mkdirs()
		html.withWriter("UTF-8") {
			engine.process(template, c, it)
		}
		return html
	}

	def makeEngine() {
		def rr = new FileResourceResolver()
		def md = new MarkdownTemplateResolver(rr)
		md.prefix = this.getContentsDir().toURI().path

		def r = new TemplateResolver()
		r.resourceResolver = rr
		r.templateMode = 'HTML5'
		r.prefix = this.project.file(this.getTemplatePrefix()).toURI().path
		r.suffix = this.getTemplateSuffix()

		def te = new TemplateEngine()
		te.addTemplateModeHandler(StandardTemplateModeHandlers.HTML5)
		te.addTemplateModeHandler(MarkdownTemplateResolver.MARKDOWN)
		te.addTemplateResolver(r)
		te.addTemplateResolver(md)
		te.addDialect(new YuzenDialect(md))
		return te
	}
}
