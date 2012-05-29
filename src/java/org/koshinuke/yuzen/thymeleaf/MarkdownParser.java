package org.koshinuke.yuzen.thymeleaf;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Document;
import org.thymeleaf.dom.NestableNode;
import org.thymeleaf.dom.Node;
import org.thymeleaf.templateparser.ITemplateParser;
import org.thymeleaf.templateparser.xmlsax.XhtmlAndHtml5NonValidatingSAXTemplateParser;

import com.google.common.io.CharStreams;

/**
 * @author taichi
 */
public class MarkdownParser implements ITemplateParser {

	protected XhtmlAndHtml5NonValidatingSAXTemplateParser delegate;
	protected PegDownProcessor pegDownProcessor;

	public MarkdownParser(int poolSize) {
		this.delegate = new XhtmlAndHtml5NonValidatingSAXTemplateParser(
				poolSize);
		this.pegDownProcessor = new PegDownProcessor(Extensions.ALL);
	}

	@Override
	public Document parseTemplate(Configuration configuration,
			String documentName, Reader source) {
		try {
			String md = CharStreams.toString(source);
			String html = this.toHtml(md);
			Document doc = this.delegate.parseTemplate(configuration,
					documentName, new StringReader("<!DOCTYPE html><div>"
							+ html + "</div>"));
			NestableNode node = (NestableNode) doc.getFirstChild();
			// remove trick nodes.
			doc.setChildren(node.getChildren());
			return doc;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	protected String toHtml(String src) {
		return this.pegDownProcessor.markdownToHtml(src);
	}

	@Override
	public List<Node> parseFragment(Configuration configuration, String fragment) {
		String html = this.toHtml(fragment);
		return this.delegate.parseFragment(configuration, html);
	}

}
