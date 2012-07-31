package org.koshinuke.yuzen.gradle

import org.gradle.api.Project;

/**
 * @author taichi
 */
class SiteModel {
	final Project project

	String head = 'css'

	SiteModel(Project project) {
		this.project = project
	}
}
