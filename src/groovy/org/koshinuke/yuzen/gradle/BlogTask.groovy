package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.internal.ConventionTask
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
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

	static Logger LOG = Logging.getLogger(BlogTask)

	@InputFiles
	FileTree contents

	@Input
	String templatePrefix

	String templateSuffix

	@OutputDirectory
	File destinationDir

	BlogTask() {
		this.group = BasePlugin.BUILD_GROUP
		def ypc = project.convention.getByType(YuzenPluginConvention)
		this.contents = ypc.contents
		this.templatePrefix = "$ypc.templatePrefix/$ypc.blog.dirName/"
		this.templateSuffix = ypc.templateSuffix
		this.destinationDir = project.file("$ypc.destinationDir/$ypc.blog.dirName")
	}

	@TaskAction
	def generate() {
		def te = makeEngine()
		// process main contents
		this.contents.visit([
					visitDir : {
					},
					visitFile : {
						def path = FileUtil.removeExtension(it.path)
						def ln = it.relativePath.lastName
						def ext = Files.getFileExtension(ln)
						if(ext == 'md') {
							processTemplate(te, it.relativePath, path)
						} else {
							if(ext ==~ /[Mm][Aa][Rr][Kk][Dd][Oo][Ww][Nn]|[Mm][Dd]/) {
								BlogTask.LOG.warn("extension of $ext is not supported. rename $it.path to ${path}.md")
							}
							it.copyTo(project.file("$destinationDir/$it.path"))
						}
					}
				] as FileVisitor)
	}

	def processTemplate(te, rel, path) {
		def template = rel.lastName
		if(1 < rel.segments.length) {
			template = rel.segments[0]
		}
		template = FileUtil.removeExtension(template)
		def blog = project.extensions.getByType(BlogPluginExtension)
		def c = new Context()
		c.setVariable('blog', blog)
		c.setVariable('content',[path: path, date: new Date()])
		def dir = project.file("$destinationDir/$path")
		dir.mkdirs()
		def html = project.file("$dir/index.html")
		html.withWriter("UTF-8") {
			te.process(template, c, it)
		}
	}

	def makeEngine() {
		def rr = new FileResourceResolver()
		def md = new MarkdownTemplateResolver(rr)
		md.prefix = "$contents.dir.path/"

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
