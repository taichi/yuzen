package org.koshinuke.yuzen.gradle

import org.gradle.api.Project
import org.gradle.util.ConfigureUtil

/**
 * @author taichi
 */
class SiteModel {
	final Project project

	String head = 'css'

	def FeedModel feed

	boolean autoload = false

	SiteModel(Project project) {
		this.project = project
		this.feed = new FeedModel()
	}

	// https://issues.apache.org/jira/browse/OGNL-164
	// OGNLRuntime#findClosestMatchingMethod
	// l.2117 でnullチェックをしていない為起こる例外に対する回避措置。
	public String getFeedType() {
		return this.feed.feedType
	}

	public void setFeedType(String type) {
		this.feed.feedType = type
	}

	def feed(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, getFeed())
	}
}
