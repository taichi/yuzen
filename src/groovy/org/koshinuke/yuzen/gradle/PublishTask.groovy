package org.koshinuke.yuzen.gradle

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.koshinuke.yuzen.Markers

/**
 * @author taichi
 */
class PublishTask extends ConventionTask {

	@InputDirectory
	def File rootDir

	PublishTask() {
		group = BasePlugin.UPLOAD_GROUP
		outputs.upToDateWhen { false }
	}

	@TaskAction
	def publish() {
		def dir = getRootDir()
		logger.info("begin publish $dir")
		def l = project.yuzen.publishers
		if(dir.isDirectory()) {
			project.yuzen.publishers*.publish(dir)
		} else {
			logger.error(Markers.BOUNDARY,"$dir is not a directory.")
		}
	}
}
