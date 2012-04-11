package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction

/**
 * @author taichi
 */
class BlogTask extends ConventionTask {

	@OutputDirectory
	File destinationDir

	@TaskAction
	def generate() {
		println this.destinationDir
	}
}
