package org.koshinuke.yuzen.gradle

import org.koshinuke.yuzen.pegdown.SlideHtmlSerializer
import org.parboiled.Parboiled;
import org.pegdown.Extensions
import org.pegdown.Parser;
import org.pegdown.ast.RootNode
import org.thymeleaf.context.Context;

/**
 * @author taichi
 */
class SlideTask extends DefaultContentsTask {

	def processTemplate(engine, file) {
		def c = new Context()
		c.setVariables(project.properties)
		def slide = makeSlide(file)
		c.setVariable('slide', slide)

		def html = calcHtmlOutput(file)
		html.parentFile.mkdirs()
		html.withWriter("UTF-8") {
			engine.process("index", c, it)
		}
		return html
	}

	def makeSlide(file) {
		def src = file.file.text
		def s = new SlideHtmlSerializer()
		def p = Parboiled.createParser(Parser, Extensions.ALL)
		RootNode rn = p.parse(src.toCharArray())
		def sects = s.toHtml(rn)
		[title: s.title, sections:sects]
	}
}
