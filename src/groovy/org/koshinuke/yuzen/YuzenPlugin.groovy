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
import org.koshinuke.yuzen.gradle.SlideTask
import org.koshinuke.yuzen.gradle.StartServerTask



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

		addRule(project, 'init', 'init Template') { base, name, task ->
			def newone = project.tasks.add name, InitTemplateTask
			newone.templateName = base
			newone.conventionMapping.destinationDir = { project.file("$ypc.templatePrefix") }
		}

		addBlogTasks(project)
		addSlideTasks(project)

		project.tasks.withType(ContentsTask) {
			it.conventionMapping.contents = {
				project.fileTree ypc.contentsDir, {}
			}
			it.conventionMapping.templatePrefix = { ypc.templatePrefix }
			it.conventionMapping.templateSuffix = { ypc.templateSuffix }
			it.conventionMapping.destinationDir = { ypc.destinationDir }
		}
	}

	def addBlogTasks(Project project) {
		def blog = project.tasks.add 'blog', DefaultContentsTask
		blog.description = "make static blog"
		addLessTask(project, blog)
		addCopyTask(project, blog, 'js')

		def paging = project.tasks.add 'paging', BlogPagingTask
		paging.description = "make static blog pagination files"
		blog.dependsOn paging
	}

	def addSlideTasks(Project project) {
		def slide = project.tasks.add 'slide', SlideTask
		slide.description = "make html slide"
		addCopyTask(project, slide, 'js')
		addCopyTask(project, slide, 'css')
		addCopyTask(project, slide, 'assets')
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

	def addLessTask(Project project, Task task) {
		def newone = project.tasks.add "${task.name}less", LessCompile
		newone.description = "compile less to css"
		newone.compress true
		newone.source project.file("$task.templatePrefix/less/main.less")
		newone.destinationDir project.file("$task.destinationDir/css")
		task.dependsOn newone
	}

	def addCopyTask(Project project, Task task, String resource) {
		def newone = project.tasks.add "$task.name$resource", Copy
		newone.from "$task.templatePrefix/$resource"
		newone.into "$task.destinationDir/$resource"
		newone.description = "copy $resource from template"
		task.dependsOn newone
	}
}
