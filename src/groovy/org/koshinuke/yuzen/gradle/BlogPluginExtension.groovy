package org.koshinuke.yuzen.gradle



import groovy.io.FileType;

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.gradle.api.Project;
import org.koshinuke.yuzen.util.FileUtil;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

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
			PegDownProcessor md = new PegDownProcessor(Extensions.ALL)
			Path root = ypc.contentsDir.toPath()
			this.recentPosts = recents.collect {
				def segments = root.relativize(it.toPath()).toFile().path.split(Pattern.quote(File.separator))
				def path = segments.collect { URLEncoder.encode(it, 'UTF-8') }.join('/').replace('.md', '')
				[url: '/' + path, title: pickTitle(md, it)]
			}
		}
		return this.recentPosts
	}

	def pickTitle(md, file) {
		def txt = md.markdownToHtml(file.text)
		def html = new XmlParser().parseText("<div>$txt</div>")
		def title = html.depthFirst().find { it.name() ==~ /h\d/ }?.depthFirst().find { it.text() }?.text()
		return title == null ? FileUtil.removeExtension(file.name) : title
	}
}
