package org.koshinuke.yuzen.thymeleaf;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;
import org.koshinuke.yuzen.util.ClassUtil;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.resourceresolver.ClassLoaderResourceResolver;
import org.thymeleaf.templatemode.StandardTemplateModeHandlers;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * @author taichi
 */
public class MarkdownProcessorTest {

	static final String PKG = ClassUtil
			.getPackagePath(MarkdownProcessorTest.class) + "/";

	@Test
	public void test() throws Exception {
		ClassLoaderResourceResolver crr = new ClassLoaderResourceResolver();
		MarkdownTemplateResolver md = new MarkdownTemplateResolver(crr);
		md.setPrefix(PKG);

		TemplateResolver r = new TemplateResolver();
		r.setResourceResolver(crr);
		r.setTemplateMode("HTML5");
		r.setPrefix(PKG);
		r.setSuffix(".html");

		TemplateEngine te = new TemplateEngine();
		te.setTemplateModeHandlers(StandardTemplateModeHandlers.ALL_TEMPLATE_MODE_HANDLERS);
		te.addTemplateModeHandler(MarkdownTemplateResolver.MARKDOWN);
		te.addTemplateResolver(md);
		te.addTemplateResolver(r);
		te.addDialect(new YuzenDialect());

		Context c = new Context();
		c.setVariable("md", "mdp");

		StringWriter w = new StringWriter();
		te.process("MarkdownProcessorTest", c, w);
		w.flush();
		String actual = w.toString();
		URL url = Resources.getResource(PKG
				+ "MarkdownProcessorTest.html.expected");
		String expected = Resources.toString(url, Charsets.UTF_8).replace("\r",
				"");
		assertEquals(expected.length(), actual.length());
		assertEquals(expected, w.toString());
	}

}
