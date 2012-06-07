package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.koshinuke.yuzen.YuzenPlugin;

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

	@TaskAction
	def initTemplate() {
		def pluginURL = YuzenPlugin.protectionDomain.codeSource.location.toExternalForm()
		def tmp = "$temporaryDir/yuzen"
		def base = "_templates/$templateName"
		project.copy {
			from project.zipTree(pluginURL).matching { include "$base/**" }
			into tmp
		}
		def dest = this.getDestinationDir()
		project.copy {
			from "$tmp/$base"
			into dest
		}
	}
}
