package io.yuzen.thymeleaf;

import static org.junit.Assert.assertEquals;
import io.yuzen.util.ClassUtil;

import java.io.StringWriter;
import java.net.URL;

import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.resourceresolver.ClassLoaderResourceResolver;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * @author taichi
 */
public class MarkdownTemplateResolverTest {

	static final String PKG = ClassUtil
			.getPackagePath(MarkdownTemplateResolverTest.class) + "/";

	@Test
	public void test() throws Exception {
		ClassLoaderResourceResolver crr = new ClassLoaderResourceResolver();
		MarkdownTemplateResolver md = new MarkdownTemplateResolver(crr);
		md.setPrefix(PKG);
		TemplateEngine te = new TemplateEngine();
		te.addTemplateModeHandler(MarkdownTemplateResolver.MARKDOWN);
		te.addTemplateResolver(md);

		Context c = new Context();

		StringWriter w = new StringWriter();
		te.process("MarkdownTemplateResolverTest", c, w);
		w.flush();
		String actual = w.toString();

		URL url = Resources.getResource(PKG
				+ "MarkdownTemplateResolverTest.expected");
		String expected = Resources.toString(url, Charsets.UTF_8).replace("\r",
				"");
		assertEquals(expected.length(), actual.length());
		assertEquals(expected, w.toString());
	}
}
