package org.koshinuke.yuzen.gradle

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
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.TemplateResolver;
import com.google.common.io.*;

/**
 * @author taichi
 */
class BlogTask extends ConventionTask implements ContentsTask {

	@InputDirectory
	File contentsDir

	@Input
	String templatePrefix

	String templateSuffix

	@OutputDirectory
	File destinationDir

	BlogTask() {
		this.group = BasePlugin.BUILD_GROUP
		def ypc = project.convention.getByType(YuzenPluginConvention)
		this.contentsDir = ypc.contentsDir
		this.templatePrefix = "$ypc.templatePrefix/$ypc.blog.dirName/"
		this.templateSuffix = ypc.templateSuffix
		this.destinationDir = project.file("$ypc.destinationDir/$ypc.blog.dirName")
	}

	@TaskAction
	def generate() {
		def te = makeEngine()
		// process main contents
		def ypc = project.convention.getByType(YuzenPluginConvention)
		this.project.fileTree(this.contentsDir, ypc.blog.contentsFilter).visit([
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
		this.project.file("$destinationDir/$path/index.html")
	}

	def calcFileOutput(file) {
		this.project.file("$destinationDir/$file.path")
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
		def blog = project.extensions.getByType(BlogPluginExtension)
		def c = new Context()
		c.setVariable('blog', blog)
		c.setVariable('content',[path: path, date: new Date()])
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
		md.prefix = "$contentsDir/"

		def r = new TemplateResolver()
		r.resourceResolver = rr
		r.templateMode = 'HTML5'
		r.prefix = this.templatePrefix
		r.suffix = this.templateSuffix

		def cr = new ClassLoaderTemplateResolver()
		cr.templateMode = 'HTML5'
		cr.prefix = this.templatePrefix
		cr.suffix = this.templateSuffix

		def te = new TemplateEngine()
		te.addTemplateModeHandler(StandardTemplateModeHandlers.HTML5)
		te.addTemplateModeHandler(MarkdownTemplateResolver.MARKDOWN)
		te.addTemplateResolver(md)
		te.addTemplateResolver(r)
		te.addTemplateResolver(cr)
		te.addDialect(new YuzenDialect(md))
		return te
	}
}
