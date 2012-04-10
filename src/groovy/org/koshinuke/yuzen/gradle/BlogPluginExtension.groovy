package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.Project;

/**
 * @author taichi
 */
class BlogPluginExtension {

	String title

	String subtitle

	File profile

	File articles

	final Project project

	BlogPluginExtension(Project project) {
		this.project = project;
	}
}
