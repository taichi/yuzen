package org.koshinuke.yuzen.gradle

import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.YuzenPlugin

/**
 * @author taichi
 */
class InitTemplateTask extends ConventionTask {

	@Input
	String templateName

	@OutputDirectory
	File destinationDir

	InitTemplateTask() {
		this.group = BasePlugin.BUILD_GROUP
	}

	def fromTree = {
		// for testing.
		def pluginURL = YuzenPlugin.protectionDomain.codeSource.location.toExternalForm()
		project.zipTree(pluginURL)
	}

	@TaskAction
	def initTemplate() {
		def tmp = "$temporaryDir/yuzen"
		def base = "_templates/$templateName"
		def ft = fromTree()
		project.copy {
			from ft.matching { include "$base/**" }
			into tmp
		}
		def dest = this.getDestinationDir()
		project.copy {
			from "$tmp/$base"
			into dest
		}
	}
}
