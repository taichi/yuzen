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

		configureBlog project, ypc
	}

	void configureBlog(Project project, YuzenPluginConvention ypc) {
		def blog = project.tasks.add 'blog', BlogTask
		blog.group = BasePlugin.BUILD_GROUP
		blog.destinationDir = project.file("$ypc.docsDir/blog")
	}
}
