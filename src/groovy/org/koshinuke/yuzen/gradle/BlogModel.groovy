package org.koshinuke.yuzen.gradle







import org.gradle.api.Project;
import org.gradle.api.file.FileVisitor;

/**
 * @author taichi
 */
class BlogModel {

	final Project project

	String title

	String subtitle

	String head = 'css'

	boolean autoload = false

	def recentPostsSize = 5
	def recentPosts = null

	def pagingUnitSize = 5

	def contentsComparator = { l, r ->
		r.lastModified <=> l.lastModified
	}

	BlogModel(Project project) {
		this.project = project;
	}

	def getRecentPosts() {
		if(this.recentPosts == null) {
			def recents = []
			project.fileTree (project.yuzen.contentsDir, { include '**/*.md' }).visit([
				visitDir : {
				},
				visitFile : {
					recents.add it
					if(recentPostsSize < recents.size()) {
						recents.sort getContentsComparator()
						recents.remove(recentPostsSize)
					}
				}
			] as FileVisitor)
			recents.sort getContentsComparator()
			this.recentPosts = recents.collect { new Content(it) }
		}
		return this.recentPosts
	}
}
