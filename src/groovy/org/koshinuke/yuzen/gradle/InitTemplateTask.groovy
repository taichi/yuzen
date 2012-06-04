package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.koshinuke.yuzen.YuzenPlugin;
import org.koshinuke.yuzen.YuzenPluginConvention;

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
		def ypc = project.convention.getByType(YuzenPluginConvention)
		this.destinationDir = project.file("$ypc.templatePrefix")
	}

	@TaskAction
	def initTemplate() {
		def pluginURL = YuzenPlugin.protectionDomain.codeSource.location.toExternalForm()
		def tmp = "$temporaryDir/yuzen"
		project.copy {
			from project.zipTree(pluginURL).matching { include "_templates/$templateName/**" }
			into tmp
		}
		def dest = this.destinationDir
		project.copy {
			from "$tmp/_templates"
			into dest
		}
	}
}
