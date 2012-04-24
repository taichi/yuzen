package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.thymeleaf.MarkdownTemplateResolver
import org.koshinuke.yuzen.thymeleaf.YuzenDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.resourceresolver.FileResourceResolver;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * @author taichi
 */
class BlogTask extends ConventionTask {

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
		def blog = project.extensions.getByType(BlogPluginExtension)
		// process main contents
		this.contents.visit([
					visitDir : {
					},
					visitFile : {

						RelativePath rel = it.relativePath
						def template = rel.lastName
						if(1 < rel.segments.length) {
							template = rel.segments[0]
						}
						template = removeExtension(template)
						String path = removeExtension(rel.pathString)

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
				] as FileVisitor)
	}

	def removeExtension(String path) {
		def result = path
		def i = path.lastIndexOf('.')
		if(0 < i) {
			result = path.substring(0, i)
		}
		return result
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