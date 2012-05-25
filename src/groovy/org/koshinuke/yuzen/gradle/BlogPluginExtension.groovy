package org.koshinuke.yuzen.gradle


import org.gradle.api.Project;

/**
 * @author taichi
 */
class BlogPluginExtension {

	final Project project

	String title

	String subtitle

	String head = 'less'

	boolean autoload = false

	def recentposts = [] // TODO ...
	BlogPluginExtension(Project project) {
		this.project = project;
	}
}
