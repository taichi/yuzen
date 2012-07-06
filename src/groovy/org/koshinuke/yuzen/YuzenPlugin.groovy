package org.koshinuke.yuzen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy
import org.koshinuke.yuzen.github.GitHubPluginExtension
import org.koshinuke.yuzen.gradle.BlogPagingTask
import org.koshinuke.yuzen.gradle.BlogPluginExtension
import org.koshinuke.yuzen.gradle.ContentsTask
import org.koshinuke.yuzen.gradle.DefaultContentsTask
import org.koshinuke.yuzen.gradle.InitTemplateTask
import org.koshinuke.yuzen.gradle.LessCompile
import org.koshinuke.yuzen.gradle.NewEntryTask;
import org.koshinuke.yuzen.gradle.SlideTask
import org.koshinuke.yuzen.gradle.StartServerTask
import org.koshinuke.yuzen.util.FileUtil;



/**
 * @author taichi
 */
class YuzenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.plugins.apply(BasePlugin)

		YuzenPluginConvention ypc = project.convention.create('yuzen', YuzenPluginConvention, project)
		project.extensions.create('github', GitHubPluginExtension, project)
		project.extensions.create('blog', BlogPluginExtension, project)

		addRule(project, 'start', 'start Jetty of a task.') { base, name, task ->
			def newone = project.tasks.add name, StartServerTask
			newone.dependsOn task
			newone.rootDir = task.destinationDir
			newone.templatePrefix = task.templatePrefix
		}

		addBlogTasks(project, ypc)
		addSlideTasks(project, ypc)

		project.tasks.withType(ContentsTask) {
			it.conventionMapping.contents = {
				project.fileTree ypc.contentsDir, {}
			}
			it.conventionMapping.templatePrefix = { ypc.templatePrefix }
			it.conventionMapping.templateSuffix = { ypc.templateSuffix }
			it.conventionMapping.destinationDir = { ypc.destinationDir }
		}
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

	def addInitTask(Project project, YuzenPluginConvention ypc, name) {
		def newone = project.tasks.add "init$name", InitTemplateTask
		newone.templateName = name
		newone.conventionMapping.destinationDir = { project.file("$ypc.templatePrefix") }
		newone.description = "init $name Template"
		return newone
	}

	def addBlogTasks(Project project, YuzenPluginConvention ypc) {
		Task init = addInitTask(project, ypc, 'blog')
		NewEntryTask profile = project.tasks.add 'makeProfile', NewEntryTask
		profile.title = 'Profile'
		profile.newEntry = new File(ypc.contentsDir, 'profile.md')
		profile.description = 'make default profile page.'
		profile << {
			logger.info(Markers.HELP, "Write your profile to $profile.newEntry.path")
			logger.info(Markers.HELP, "Then write a new post")
			logger.info(Markers.HELP, "gradlew post -Ptitle=HelloWorld")
		}

		init.doLast { profile.execute() }

		def blog = project.tasks.add 'blog', DefaultContentsTask
		blog.description = "make static blog"
		addLessTask(project, blog)
		addCopyTask(project, blog, 'js')

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
		page.conventionMapping.title = {
			def n = page.getNewEntry().name
			return FileUtil.removeExtension(n)
		}
		page.conventionMapping.newEntry = {
			def name = getInput(project, "pagename", "newPage.md")
			return new File(ypc.contentsDir, name)
		}
		page.description = "make new page. example -> gradlew page -Ppagename=pages/profile.md"
	}

	def getInput(Project project, key, defaultValue) {
		if(project.hasProperty(key)) {
			return project.property(key)
		}
		return defaultValue
	}

	def addLessTask(Project project, Task task) {
		def newone = project.tasks.add "${task.name}less", LessCompile
		newone.description = "compile less to css"
		newone.compress true
		newone.source project.file("$task.templatePrefix/less/main.less")
		newone.destinationDir project.file("$task.destinationDir/css")
		task.dependsOn newone
	}

	def addSlideTasks(Project project, YuzenPluginConvention ypc) {
		Task init = addInitTask(project, ypc, 'slide')
		NewEntryTask index = project.tasks.add 'defaultSlide', NewEntryTask
		index.title = 'Slide Title'
		index.newEntry = new File(ypc.contentsDir, 'index.md')
		index.description = 'default slide page'
		index << {
			logger.info(Markers.HELP, "Write slide to $index.newEntry.path")
		}
		init.doLast { index.execute() }

		def slide = project.tasks.add 'slide', SlideTask
		slide.description = "make html slide"
		addCopyTask(project, slide, 'js')
		addCopyTask(project, slide, 'css')
		addCopyTask(project, slide, 'assets')
	}

	def addCopyTask(Project project, Task task, String resource) {
		def newone = project.tasks.add "$task.name$resource", Copy
		newone.from "$task.templatePrefix/$resource"
		newone.into "$task.destinationDir/$resource"
		newone.description = "copy $resource from template"
		task.dependsOn newone
	}
}
