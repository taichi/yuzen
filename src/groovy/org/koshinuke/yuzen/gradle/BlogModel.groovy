package org.koshinuke.yuzen.gradle







import org.eclipse.jgit.util.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.file.FileVisitor;
import org.gradle.util.ConfigureUtil
import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

/**
 * @author taichi
 */
class BlogModel {

	final Project project

	String title

	String subtitle

	String profile

	String head = 'css'

	boolean autoload = false

	def recentPostsSize = 5
	def recentPosts = null

	def pagingUnitSize = 5

	def contentsComparator = { l, r ->
		r.lastModified <=> l.lastModified
	}

	def FeedModel feed

	BlogModel(Project project) {
		this.project = project
		this.feed = new FeedModel()
	}

	def hasProfile() {
		StringUtils.isEmptyOrNull(this.profile) == false
	}

	def makeProfile() {
		if(hasProfile()) {
			PegDownProcessor md = new PegDownProcessor(Extensions.ALL)
			return md.markdownToHtml(this.profile)
		}
		return ""
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

	def feed(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, getFeed())
	}
}
