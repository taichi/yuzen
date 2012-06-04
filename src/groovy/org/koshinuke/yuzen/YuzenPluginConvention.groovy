package org.koshinuke.yuzen


import org.gradle.api.Project;

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	String contentsDirName = '_contents'
	String templatePrefix = '_templates'
	String templateSuffix = ".html"
	String destinationDirName = 'yuzen'

	YuzenPluginConvention(Project project) {
		this.project = project
	}

	File getContentsDir() {
		this.project.file("$project.projectDir/$contentsDirName")
	}

	File getDestinationDir() {
		this.project.file("$project.buildDir/$destinationDirName")
	}
}