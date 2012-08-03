package org.koshinuke.yuzen.gradle

import groovy.lang.Closure

import org.gradle.api.Project;
import org.gradle.util.ConfigureUtil

/**
 * @author taichi
 */
class SiteModel {
	final Project project

	String head = 'css'

	def FeedModel feed

	SiteModel(Project project) {
		this.project = project
		this.feed = new FeedModel()
	}

	def feed(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, getFeed())
	}
}
