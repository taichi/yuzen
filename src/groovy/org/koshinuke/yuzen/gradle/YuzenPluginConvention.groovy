package org.koshinuke.yuzen.gradle

import groovy.lang.Closure;

import org.gradle.api.Project;

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	final BlogTaskConvention blog = new BlogTaskConvention()

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

class BlogTaskConvention {
	String dirName = 'blog'
	/**
	 * The given closure is used to configure the filter.A {@link org.gradle.api.tasks.util.PatternFilterable} is
	 * passed to the closure as it's delegate
	 */
	Closure contentsFilter = { exclude "fragments/**" }
}
