package org.koshinuke.yuzen.gradle

import org.gradle.api.Project;
import org.gradle.api.file.FileTree

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	final BlogTaskConvention blog = new BlogTaskConvention()

	String contentsDirName = '_contents'
	String fragmentsDirName = 'fragments'
	String templatePrefix = 'templates'
	String templateSuffix = ".html"
	String destinationDirName = 'yuzen'

	YuzenPluginConvention(Project project) {
		this.project = project
	}

	FileTree getContents() {
		project.fileTree(contentsDirName) { exclude "$fragmentsDirName/**" }
	}

	File getDestinationDir() {
		this.project.file("$project.buildDir/$destinationDirName")
	}
}

class BlogTaskConvention {
	String dirName = 'blog'
}
