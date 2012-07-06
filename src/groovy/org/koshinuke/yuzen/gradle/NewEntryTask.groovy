package org.koshinuke.yuzen.gradle

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.Markers;

/**
 * @author taichi
 */
class NewEntryTask extends ConventionTask {

	@Input
	def String title

	@OutputFile
	def File newEntry

	@TaskAction
	def generate() {
		def f = getNewEntry()
		if(f.exists()) {
			logger.warn Markers.HELP, "$newEntry.path is already exists.."
		} else {
			f.parentFile.mkdirs()
			f.text = "* $title \n"
		}
	}
}
