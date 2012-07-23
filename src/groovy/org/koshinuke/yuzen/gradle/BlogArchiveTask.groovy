package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.resourceresolver.FileResourceResolver
import org.thymeleaf.templatemode.StandardTemplateModeHandlers
import org.thymeleaf.templateresolver.TemplateResolver

/**
 * @author taichi
 */
class BlogArchiveTask extends ConventionTask implements ContentsTask {

	@InputFiles
	ConfigurableFileTree contents

	@Input
	String templatePrefix

	@Input
	String templateSuffix

	@OutputDirectory
	File destinationDir

	def entryDirName

	def entriesFilter = { include "$entryDirName/**/*.md" }


	BlogArchiveTask() {
		this.group = BasePlugin.BUILD_GROUP
	}

	@TaskAction
	def makeArchives() {
		def contents = []
		def tree = ConfigureUtil.configure(getEntriesFilter(), getContents())
		tree.visit([
					visitDir : {
					},
					visitFile : {
						contents.add new Content(it)
					}
				] as FileVisitor)
		Map groups = contents.sort { l, r ->
			r.timestamp <=> l.timestamp
		}.groupBy {
			it.timestamp.format('yyyy-MM')
		}
		processTemplate(groups)
	}

	def processTemplate(Map groups) {
		def engine = makeEngine()
		def c = new Context()
		c.setVariables(project.properties)
		c.setVariable('contents', groups)
		List months = new ArrayList(groups.keySet())
		months.sort(Collections.reverseOrder())
		c.setVariable('months', months)
		c.setVariable('relative', '../')
		def html = new File(this.getDestinationDir(), 'archives/index.html')
		html.parentFile.mkdirs()
		html.withWriter("UTF-8") {
			engine.process('archive', c, it)
		}
	}

	def makeEngine() {
		def r = new TemplateResolver()
		r.resourceResolver = new FileResourceResolver()
		r.templateMode = 'HTML5'
		r.prefix = this.project.file(this.getTemplatePrefix()).toURI().path
		r.suffix = this.getTemplateSuffix()

		def te = new TemplateEngine()
		te.addTemplateModeHandler(StandardTemplateModeHandlers.HTML5)
		te.addTemplateResolver(r)
		return te
	}
}
