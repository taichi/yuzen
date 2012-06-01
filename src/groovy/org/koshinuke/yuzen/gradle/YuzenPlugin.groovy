package org.koshinuke.yuzen.gradle

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Copy;



/**
 * @author taichi
 */
class YuzenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.plugins.apply(BasePlugin)

		YuzenPluginConvention ypc = project.convention.create('yuzen', YuzenPluginConvention, project)
		project.extensions.create('blog', BlogPluginExtension, project)

		addRule(project, 'start', 'start Jetty of a task.') { name, task ->
			def newone = project.tasks.add name, StartServerTask
			newone.dependsOn task
			newone.rootDir = task.destinationDir
			newone.templatePrefix = task.templatePrefix
		}

		def blog = project.tasks.add 'blog', BlogTask
		addRule(project, 'init', 'init Template') { name, task ->
			def newone = project.tasks.add name, InitTemplateTask
			newone.templateName = blog.name
		}
		blog.description = "make static blog"
		blog.dependsOn 'lessBlog', 'jsBlog'

		addRule(project, 'less', "compile less to css") { name, task ->
			def newone = project.tasks.add name, LessCompile
			newone.compress true
			newone.source project.file("$task.templatePrefix/less/main.less")
			newone.destinationDir = project.file("$task.destinationDir/css")
		}

		addRule(project, 'js', 'copy js from template') { name, task ->
			def newone = project.tasks.add name, Copy
			newone.from "$task.templatePrefix/js"
			newone.into "$task.destinationDir/js"
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
