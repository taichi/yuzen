package org.koshinuke.yuzen.gradle

import org.gradle.api.Project

/**
 * @author taichi
 */
class ProjectUtil {

	static def getProperty(Project project, String key) {
		if(project.hasProperty(key)) {
			return project.property(key)
		}
		return null
	}
}
