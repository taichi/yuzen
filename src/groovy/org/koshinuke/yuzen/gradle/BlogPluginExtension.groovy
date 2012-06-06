package org.koshinuke.yuzen.gradle





import groovy.io.FileType;

import java.nio.file.Path;

import org.gradle.api.Project;
import org.koshinuke.yuzen.YuzenPluginConvention

/**
 * @author taichi
 */
class BlogPluginExtension {

	final Project project

	String title

	String subtitle

	String head = 'css'

	boolean autoload = false

	def recentPostsSize = 5
	def recentPosts = null

	BlogPluginExtension(Project project) {
		this.project = project;
	}

	def getRecentPosts() {
		if(this.recentPosts == null) {
			YuzenPluginConvention ypc = project.convention.getByType(YuzenPluginConvention)
			def recents = []
			ypc.contentsDir.traverse(type: FileType.FILES, nameFilter: ~/.*\.md$/) {
				recents.add it
				if(recentPostsSize < recents.size()) {
					recents.sort { l, r ->
						r.lastModified() <=> l.lastModified()
					}
					recents.remove(recentPostsSize)
				}
			}
			Path root = ypc.contentsDir.toPath()
			this.recentPosts = recents.collect {
				new Content(root, it)
			}
		}
		return this.recentPosts
	}
}
