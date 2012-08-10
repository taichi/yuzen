package org.koshinuke.yuzen.gradle

import org.gradle.api.file.FileTreeElement
import org.koshinuke.yuzen.util.FileUtil
import org.pegdown.Extensions
import org.pegdown.PegDownProcessor
import org.thymeleaf.Standards

import com.google.common.io.CharStreams










/**
 * @author taichi
 */
class Content {

	static final DOCTYPE = '<!DOCTYPE html [\n' + getEntities() + ']>'

	def String url
	def String title
	def Date timestamp
	def String summary
	def File rawfile

	static String getEntities() {
		[
			Standards.ENTITIES_LATIN_1_FOR_XHTML_DOC_TYPE_RESOLUTION_ENTRY,
			Standards.ENTITIES_SPECIAL_FOR_XHTML_DOC_TYPE_RESOLUTION_ENTRY,
			Standards.ENTITIES_SYMBOLS_FOR_XHTML_DOC_TYPE_RESOLUTION_ENTRY
		]*.createInputSource()*.getByteStream()*.withReader { CharStreams.toString(it) }
		.join('\n')
	}

	Content(FileTreeElement file) {
		this.timestamp = new Date(file.lastModified)

		this.url = file.relativePath.segments.collect {
			URLEncoder.encode(it, 'UTF-8')
		}.join('/').replaceAll(/([iI][nN][dD][eE][xX])?\.md$/, "")

		this.rawfile = file.file

		PegDownProcessor md = new PegDownProcessor(Extensions.ALL)
		def txt = md.markdownToHtml(file.file.text)
		// see. org.pegdown.ToHtmlSerializer.visit(SimpleNode)
		def html = new XmlParser().parseText("$DOCTYPE<div>$txt</div>")

		def title = html.depthFirst().find {
			it.name() ==~ /h\d/
		}?.depthFirst().find { it.text() }

		if(title == null) {
			this.title = FileUtil.removeExtension(file.name)
			this.summary = toHtml(html.children()[0])
		} else {
			this.title = title.text()
			def siblings = title.parent().children()
			def index = siblings.indexOf(title)
			if((index + 1) < siblings.size()) {
				this.summary = toHtml(siblings.get(index + 1))
			}
		}
	}

	def toHtml(node) {
		StringWriter sw = new StringWriter()
		XmlNodePrinter nw = new XmlNodePrinter(new PrintWriter(sw))
		nw.print(node)
		return sw.toString()
	}
}
