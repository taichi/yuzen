package org.koshinuke.yuzen.gradle

import static org.junit.Assert.*

import org.junit.Test
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.resourceresolver.FileResourceResolver
import org.thymeleaf.templatemode.StandardTemplateModeHandlers
import org.thymeleaf.templateresolver.TemplateResolver

/**
 * @author taichi
 */
class PageTest {

	def make(index, pageSize) {
		new Page([], index, "page", pageSize)
	}

	@Test
	void dots() {
		assert "dots_right" == make(3,10).pagination
		assert "dots_twice" == make(5,10).pagination
		assert "dots_left" == make(7,10).pagination
	}

	@Test
	void dots_right_pages() {
		4.times {
			Page p = make(it, 10)
			assert 2..5 == p.pages
			assert it + 1 == p.current
		}
	}

	@Test
	void dots_left_pages() {
		(7..9).each {
			Page p = make(it, 10)
			assert 6..10 == p.pages
			assert it + 1 == p.current
		}
	}

	@Test
	void dots_twice_pages() {
		assert 3..7 == make(4, 10).pages
		assert 4..8 == make(5, 10).pages
	}

	@Test
	void template_small() {
		assertTemplate(make(3, 9), "PageTest_small")
	}

	@Test
	void template_right() {
		assertTemplate(make(3, 10), "PageTest_right")
	}

	@Test
	void template_left() {
		assertTemplate(make(8, 10), "PageTest_left")
	}

	@Test
	void template_twice() {
		assertTemplate(make(5, 10), "PageTest_twice")
	}

	def testDataDir = new File("test/groovy/org/koshinuke/yuzen/gradle")

	def assertTemplate(page, en) {
		def s = processTemplate("PageTest", page)
		def expected = new XmlParser().parse(new File(testDataDir, "${en}.html"))
		def actual = new XmlParser().parseText(s)
		assert expected.toString() == actual.toString()
	}

	def processTemplate(template, page) {
		def files = [
			new File("src/resources/_templates/blog"),
			testDataDir
		]
		def engine = new TemplateEngine()
		engine.addTemplateModeHandler(StandardTemplateModeHandlers.HTML5)
		files.each {
			assert it.exists()
			def r = new TemplateResolver()
			r.resourceResolver = new FileResourceResolver()
			r.templateMode = 'HTML5'
			r.prefix = it.toURI().path
			r.suffix = ".html"
			engine.addTemplateResolver(r)
		}

		def c = new Context()
		StringWriter sw = new StringWriter()
		c.setVariable("page", page)
		c.setVariable('relative', "../../")
		engine.process(template, c, sw)
		return sw.toString()
	}
}

