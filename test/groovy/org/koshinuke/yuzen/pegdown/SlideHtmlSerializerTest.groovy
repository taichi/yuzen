

package org.koshinuke.yuzen.pegdown;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test
import org.parboiled.Parboiled;
import org.pegdown.Extensions;
import org.pegdown.Parser;
import org.pegdown.ast.RootNode;

/**
 * @author taichi
 */
class SlideHtmlSerializerTest {

	Parser parser
	SlideHtmlSerializer target

	@Before
	void setUp() {
		target = new SlideHtmlSerializer()
		parser = Parboiled.createParser(Parser, Extensions.ALL)
	}

	String toHtml(String src) {
		RootNode rn = parser.parse(src.toCharArray())
		target.toHtml(rn)
	}

	@Test
	void oneSection() {
		String actual = "# hogehoge\n"
		String expected = "<section><h1>hogehoge</h1></section>"
		assert expected == toHtml(actual)
		assert "hogehoge" == this.target.title
		assert 1 == this.target.pages
	}

	@Test
	void oneSectionWithSmallHeader() {
		String actual = "# hogehoge\n###mogemoge\n"
		String expected = "<section><h1>hogehoge</h1><h3>mogemoge</h3></section>"
		assert expected == toHtml(actual)
		assert "hogehoge" == this.target.title
		assert 1 == this.target.pages
	}

	@Test
	void twoSection() {
		String actual = "# hogehoge\nmogemoge\n##piropiro\n"
		String expected = "<section><h1>hogehoge</h1><p>mogemoge</p></section>" +
				"<section><h2>piropiro</h2></section>"
		assert expected == toHtml(actual)
		assert "hogehoge" == this.target.title
		assert 2 == this.target.pages
	}

	@Test
	void twoSectionWithList() {
		String actual = "# hogehoge\nmogemoge\n##piropiro\n* aaa \n* bbb \n* ccc\n"
		String expected = """<section><h1>hogehoge</h1><p>mogemoge</p></section><section><h2>piropiro</h2>
<ul>
  <li>aaa</li>
  <li>bbb</li>
  <li>ccc</li>
</ul></section>"""
		assert expected == toHtml(actual)
		assert "hogehoge" == this.target.title
		assert 2 == this.target.pages
	}
}
