package org.koshinuke.yuzen.gradle


import org.gradle.api.Project;

/**
 * @author taichi
 */
class BlogPluginExtension {

	final Project project

	String title

	String subtitle

	BlogPluginExtension(Project project) {
		this.project = project;
	}
}
