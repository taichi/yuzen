package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction

/**
 * @author taichi
 */
class BlogTask extends ConventionTask {

	@InputDirectory
	File articlesDir

	@OutputDirectory
	File destinationDir

	BlogTask() {
		this.group = BasePlugin.BUILD_GROUP
		def ypc = project.convention.getByType(YuzenPluginConvention)
		this.articlesDir = ypc.articlesDir
		this.destinationDir = project.file("$ypc.destinationDir/$ypc.blog.dirName")
	}

	@TaskAction
	def generate() {
		println this.articlesDir
		println this.destinationDir
	}
}
