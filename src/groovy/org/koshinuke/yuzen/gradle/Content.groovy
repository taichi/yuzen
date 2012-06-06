package org.koshinuke.yuzen.gradle

import java.nio.file.Path;
import java.util.regex.Pattern;

import org.koshinuke.yuzen.util.FileUtil;
import org.pegdown.Extensions
import org.pegdown.PegDownProcessor;



/**
 * @author taichi
 */
class Content {
	def url
	def title
	def timestamp
	def summary

	Content(Path root, File file) {
		this.timestamp = new Date(file.lastModified())

		def segments = root.relativize(file.toPath()).toFile().path.split(Pattern.quote(File.separator))
		this.url = '/' + segments.collect {
			URLEncoder.encode(it, 'UTF-8')
		}.join('/').replace('.md', '')

		PegDownProcessor md = new PegDownProcessor(Extensions.ALL)
		def txt = md.markdownToHtml(file.text)
		def html = new XmlParser().parseText("<div>$txt</div>")

		def title = html.depthFirst().find {
			it.name() ==~ /h\d/
		}?.depthFirst().find { it.text() }

		if(title == null) {
			this.title = FileUtil.removeExtension(file.name)
			this.summary = html.children()[0]
		} else {
			this.title = title.text()
			def siblings = title.parent().children()
			def index = siblings.indexOf(title)
			if((index + 1) < siblings.size()) {
				this.summary = siblings.get(index + 1)
			}
		}
	}
}
