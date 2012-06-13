package org.koshinuke.yuzen

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy;
import org.koshinuke.yuzen.github.GitHubPluginExtension
import org.koshinuke.yuzen.gradle.BlogPagingTask
import org.koshinuke.yuzen.gradle.BlogPluginExtension;
import org.koshinuke.yuzen.gradle.DefaultContentsTask;
import org.koshinuke.yuzen.gradle.ContentsTask;
import org.koshinuke.yuzen.gradle.InitTemplateTask;
import org.koshinuke.yuzen.gradle.LessCompile;
import org.koshinuke.yuzen.gradle.StartServerTask;



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

		addRule(project, 'start', 'start Jetty of a task.') { name, task ->
			def newone = project.tasks.add name, StartServerTask
			newone.dependsOn task
			newone.rootDir = task.destinationDir
			newone.templatePrefix = task.templatePrefix
		}

		def blog = project.tasks.add 'blog', DefaultContentsTask
		addRule(project, 'init', 'init Template') { name, task ->
			def newone = project.tasks.add name, InitTemplateTask
			newone.templateName = blog.name
			newone.conventionMapping.destinationDir = { project.file("$ypc.templatePrefix") }
		}
		blog.description = "make static blog"
		blog.dependsOn 'lessBlog', 'jsBlog'

		addRule(project, 'less', "compile less to css") { name, task ->
			def newone = project.tasks.add name, LessCompile
			newone.compress true
			newone.source project.file("$task.templatePrefix/less/main.less")
			newone.destinationDir project.file("$task.destinationDir/css")
		}

		addRule(project, 'js', 'copy js from template') { name, task ->
			def newone = project.tasks.add name, Copy
			newone.from "$task.templatePrefix/js"
			newone.into "$task.destinationDir/js"
		}

		def paging = project.tasks.add 'paging', BlogPagingTask
		paging.description = "make static blog pagination files"

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
				closure(name, task)
			}
		}
	}
}
