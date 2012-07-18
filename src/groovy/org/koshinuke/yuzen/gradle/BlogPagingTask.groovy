package org.koshinuke.yuzen.gradle



import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.util.FileUtil;
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.resourceresolver.FileResourceResolver
import org.thymeleaf.templatemode.StandardTemplateModeHandlers
import org.thymeleaf.templateresolver.TemplateResolver

/**
 * @author taichi
 */
class BlogPagingTask extends ConventionTask implements ContentsTask {

	@InputFiles
	ConfigurableFileTree contents

	@Input
	String templatePrefix

	@Input
	String templateSuffix

	@OutputDirectory
	File destinationDir

	@Input
	String pagingPrefix

	def entryDirName

	def entriesFilter = { include "$entryDirName/**/*.md" }

	BlogPagingTask() {
		this.group = BasePlugin.BUILD_GROUP
	}

	@TaskAction
	def generate() {
		def entries = []
		def tree = ConfigureUtil.configure(getEntriesFilter(), getContents())
		tree.visit([
					visitDir : {
					},
					visitFile : { entries.add it }
				] as FileVisitor)

		entries.sort project.blog.getContentsComparator()

		def engine = makeEngine()
		def pages = entries.collate(project.blog.pagingUnitSize)
		pages.eachWithIndex { v, i ->
			def page = new Page(v.collect { new Content(it) }, i, this.getPagingPrefix(), pages.size)
			processTemplate(engine, page, i)
		}
	}

	def processTemplate(engine, page, index) {
		def c = new Context()
		c.setVariables(project.properties)
		c.setVariable("page", page)

		def path = ""
		if(0 < index) {
			path = getPagingPrefix() + "/" + (index + 1)
		}

		def html = new File(this.getDestinationDir(), "$path/index.html")
		def relative = ""
		if(0 < index) {
			def htmlPath = html.parentFile.toPath()
			def relPath = htmlPath.relativize(getDestinationDir().toPath())
			relative = FileUtil.slashify(relPath) + "/"
		}
		c.setVariable('relative', relative)
		html.parentFile.mkdirs()
		html.withWriter("UTF-8") {
			engine.process('page', c, it)
		}
		return html
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
