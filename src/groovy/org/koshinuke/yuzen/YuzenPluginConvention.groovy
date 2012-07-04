package org.koshinuke.yuzen


import org.gradle.api.Project

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	String contentsDirName = '_contents'
	String templatePrefix = '_templates'
	String templateSuffix = ".html"
	String destinationDirName = 'yuzen'

	String pagingPrefix = 'page'
	String entryDirName = 'entry'
	String entryPattern = '%1$tY/%1$tm/%1$td/%2$s.md'

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