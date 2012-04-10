package org.koshinuke.yuzen.gradle

import org.gradle.api.Project;

/**
 * @author taichi
 */
class YuzenPluginConvention {
	final Project project

	String docsDirName

	YuzenPluginConvention(Project project) {
		this.project = project
		this.docsDirName = 'yuzen'
	}

	File getDocsDir() {
		this.project.fileResolver.withBaseDir(this.project.buildDir).resolve(docsDirName)
	}
}
