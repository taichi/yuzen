package org.koshinuke.yuzen.gradle

import org.gradle.api.Project;

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	final BlogTaskConvention blog = new BlogTaskConvention()

	String destinationDirName = 'yuzen'

	YuzenPluginConvention(Project project) {
		this.project = project
	}

	File getArticlesDir() {
		this.project.fileResolver.withBaseDir(this.project.projectDir).resolve(blog.articlesDirName)
	}
	File getDestinationDir() {
		this.project.fileResolver.withBaseDir(this.project.buildDir).resolve(destinationDirName)
	}
}

class BlogTaskConvention {
	String articlesDirName = '_articles'
	String dirName = 'blog'
}
