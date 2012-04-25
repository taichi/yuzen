package org.koshinuke.yuzen.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin;

/**
 * @author taichi
 */
class YuzenPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.plugins.apply(BasePlugin)

		YuzenPluginConvention ypc = project.convention.create("yuzen", YuzenPluginConvention, project)
		project.extensions.create("blog", BlogPluginExtension, project)

		addServerRule project
		project.tasks.add 'blog', BlogTask
	}

	protected void addServerRule(Project project) {
		def prefix = 'start'
		project.tasks.addRule "Pattern: ${prefix}<TaskName>: start Jetty of a task.", { String name ->
			if(name.startsWith(prefix) == false) {
				return
			}
			def base = name.replace prefix, ""
			def task = project.tasks.withType(ContentsTask).find {
				base.equalsIgnoreCase(it.name)
			}
			if(task != null) {
				def st = project.tasks.add name, StartServerTask
				st.dependsOn task
				st.rootDir = task.destinationDir
				st.templatePrefix = task.templatePrefix
			}
		}
	}
}
