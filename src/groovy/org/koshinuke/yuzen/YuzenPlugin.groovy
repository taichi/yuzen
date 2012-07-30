package org.koshinuke.yuzen

import org.eclipse.jgit.util.StringUtils;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.koshinuke.yuzen.github.GitHubModel
import org.koshinuke.yuzen.gradle.BlogArchiveTask
import org.koshinuke.yuzen.gradle.BlogPagingTask
import org.koshinuke.yuzen.gradle.BlogModel
import org.koshinuke.yuzen.gradle.ContentsTask
import org.koshinuke.yuzen.gradle.DefaultContentsTask
import org.koshinuke.yuzen.gradle.FeedTask
import org.koshinuke.yuzen.gradle.InitTemplateTask
import org.koshinuke.yuzen.gradle.LessCompile
import org.koshinuke.yuzen.gradle.NewEntryTask
import org.koshinuke.yuzen.gradle.PublishTask
import org.koshinuke.yuzen.gradle.SlideTask
import org.koshinuke.yuzen.gradle.StartServerTask
import org.koshinuke.yuzen.util.FileUtil



/**
 * @author taichi
 */
class YuzenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.plugins.apply(BasePlugin)

		YuzenPluginConvention ypc = project.convention.create('yuzen', YuzenPluginConvention, project)
		project.extensions.create('github', GitHubModel, project)
		project.extensions.create('blog', BlogModel, project)

		addRule(project, 'start', 'start Jetty of a task.') { base, name, task ->
			def newone = project.tasks.add name, StartServerTask
			newone.dependsOn task
			newone.rootDir = task.destinationDir
			newone.templatePrefix = task.templatePrefix
		}

		addCopyBootstrapTask(project, ypc)
		addBlogTasks(project, ypc)
		addSlideTasks(project, ypc)
		addSiteTasks(project, ypc)

		project.tasks.withType(ContentsTask) {
			it.conventionMapping.with {
				contents = {
					project.fileTree ypc.contentsDir, {}
				}
				templatePrefix = { ypc.templatePrefix }
				templateSuffix = { ypc.templateSuffix }
				destinationDir = { ypc.destinationDir }
			}
		}

		def publish = project.tasks.add 'publish', PublishTask
		publish.conventionMapping.rootDir = { ypc.destinationDir }
		publish.description = "publish contents to server."
	}

	def addRule(Project project, String prefix, String desc, Closure closure) {
		project.tasks.addRule "Pattern: ${prefix}<TaskName>: $desc", { String name ->
			if(name.startsWith(prefix) == false) {
				return
			}
			def base = name.replace prefix, ""
			def task = project.tasks.withType(ContentsTask).find {
				base.equalsIgnoreCase(it.name)
			}
			if(task != null) {
				closure(base, name, task)
			}
		}
	}

	def addCopyBootstrapTask(Project project, YuzenPluginConvention ypc) {
		Copy copyBootstrap = project.tasks.add 'copyBootstrap', Copy
		copyBootstrap.description = 'copy Bootstrap from archive'
		def tree = getPluginArchive(project)
		copyBootstrap.into ypc.templatePrefix
		copyBootstrap.from tree.matching({ include "less/**" })

		copyBootstrap.doLast {
			def tmp = copyBootstrap.getTemporaryDir()
			['less', 'js', 'img'].each { name ->
				project.copy {
					from tree.matching({ include "bootstrap/$name/**" })
					into tmp
				}
				project.copy {
					from "$tmp/bootstrap/$name"
					into "$ypc.templatePrefix/$name"
				}
			}
		}
	}

	def addInitTask(Project project, YuzenPluginConvention ypc, name) {
		def newone = project.tasks.add "init$name", InitTemplateTask
		newone.templateName = name
		newone.conventionMapping.destinationDir = { project.file("$ypc.templatePrefix") }
		newone.description = "init $name Template"
		return newone
	}

	def addBlogTasks(Project project, YuzenPluginConvention ypc) {
		Task init = addInitTask(project, ypc, 'blog')
		init.dependsOn 'copyBootstrap'

		NewEntryTask profile = project.tasks.add 'makeProfile', NewEntryTask
		profile.title = 'Profile'
		profile.newEntry = new File(ypc.contentsDir, 'profile.md')
		profile.description = 'make default profile page.'
		init.dependsOn profile

		def blog = project.tasks.add 'blog', DefaultContentsTask
		blog.description = "make static blog"
		addLessTask(project, blog, ypc)
		addCopyTask(project, blog, ypc, 'js')

		def paging = project.tasks.add 'paging', BlogPagingTask
		paging.description = "make static blog pagination files"
		paging.conventionMapping.entryDirName = { ypc.entryDirName }
		paging.conventionMapping.pagingPrefix = { ypc.pagingPrefix }

		blog.dependsOn paging

		def post = project.tasks.add 'post', NewEntryTask
		post.conventionMapping.title = {
			getInput(project, 'title', 'newEntry')
		}
		post.conventionMapping.newEntry = {
			def name = String.format("${ypc.entryDirName}/${ypc.entryPattern}", new Date(), post.getTitle())
			return new File(ypc.contentsDir, name)
		}
		post.description = "make new post. example -> gradlew post -Ptitle=HelloWorld"

		def page = project.tasks.add 'page', NewEntryTask
		page.conventionMapping.with {
			title = {
				def n = page.getNewEntry().name
				return FileUtil.removeExtension(n)
			}
			newEntry = {
				def name = getInput(project, "pagename", "newPage.md")
				return new File(ypc.contentsDir, name)
			}
		}
		page.description = "make new page. example -> gradlew page -Ppagename=pages/profile.md"

		def archives = project.tasks.add 'archives', BlogArchiveTask
		archives.description = 'make blog archives'
		archives.conventionMapping.entryDirName = { ypc.entryDirName }
		blog.dependsOn archives

		def feed = project.tasks.add 'blogFeed', FeedTask
		feed.description "make blog syndication feed"
		feed.model = project.blog.feed
		feed.conventionMapping.with {
			feedType = { feed.model.feedType }
			syndicationURI = { feed.model.syndicationURI }
			title = {
				def s = feed.model.title
				if(StringUtils.isEmptyOrNull(s)) {
					return project.blog.title
				}
				return s
			}
			author = { feed.model.author }
			contents = {
				project.fileTree ypc.contentsDir, {}
			}
			destinationFile = { new File(ypc.destinationDir, "${feed.model.feedType}.xml") }
		}
		blog.dependsOn feed
	}

	def getInput(Project project, key, defaultValue) {
		if(project.hasProperty(key)) {
			return project.property(key)
		}
		return defaultValue
	}

	def addLessTask(Project project, Task task, YuzenPluginConvention ypc) {
		def newone = project.tasks.add "${task.name}less", LessCompile
		newone.description = "compile less to css"
		newone.compress true
		newone.source project.file("$ypc.templatePrefix/less/main.less")
		newone.destinationDir project.file("$ypc.destinationDir/css")
		task.dependsOn newone
	}

	def addSlideTasks(Project project, YuzenPluginConvention ypc) {
		Task init = addInitTask(project, ypc, 'slide')
		NewEntryTask index = project.tasks.add 'defaultSlide', NewEntryTask
		index.title = 'Slide Title'
		index.newEntry = new File(ypc.contentsDir, 'index.md')
		index.description = 'default slide page'
		init.dependsOn index

		def slide = project.tasks.add 'slide', SlideTask
		slide.description = "make html slide"
		addCopyTask(project, slide, ypc, 'js')
		addCopyTask(project, slide, ypc, 'css')
		addCopyTask(project, slide, ypc, 'assets')
	}

	def addSiteTasks(Project project, YuzenPluginConvention ypc) {
		Task init = addInitTask(project, ypc, 'site')
		init.dependsOn 'copyBootstrap'

		def site = project.tasks.add 'site', DefaultContentsTask
		site.description = "make project site"
		addLessTask(project, site, ypc)
		addCopyTask(project, site, ypc, 'js')
	}

	def addCopyTask(Project project, Task task, YuzenPluginConvention ypc, String resource) {
		def newone = project.tasks.add "$task.name$resource", Copy
		newone.from "$ypc.templatePrefix/$resource"
		newone.into "$ypc.destinationDir/$resource"
		newone.description = "copy $resource from template"
		task.dependsOn newone
	}

	def static getPluginArchive(Project project) {
		def pluginURL = YuzenPlugin.protectionDomain.codeSource.location.toExternalForm()
		project.zipTree(pluginURL)
	}
}
