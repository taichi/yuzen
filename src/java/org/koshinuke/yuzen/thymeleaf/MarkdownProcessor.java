package org.koshinuke.yuzen.thymeleaf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.thymeleaf.Arguments;
import org.thymeleaf.Configuration;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.dom.Document;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.templateresolver.TemplateResolution;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.thymeleaf.util.StringUtils;

/**
 * @author taichi
 */
public class MarkdownProcessor extends AbstractChildrenModifierAttrProcessor {

	TemplateResolver resolver;

	public MarkdownProcessor(TemplateResolver rr) {
		super(MarkdownTemplateResolver.TEMPLATE_MODE.toLowerCase());
		this.resolver = rr;
	}

	@Override
	public int getPrecedence() {
		return 10000;
	}

	@Override
	protected List<Node> getModifiedChildren(Arguments arguments,
			Element element, String attributeName) {
		final String attributeValue = element.getAttributeValue(attributeName);
		final Object v = StandardExpressionProcessor.processExpression(
				arguments, attributeValue);
		if (v != null) {
			Configuration conf = arguments.getConfiguration();
			TemplateProcessingParameters tpp = new TemplateProcessingParameters(
					conf, v.toString(), arguments.getContext());
			TemplateResolution tr = this.resolver.resolveTemplate(tpp);
			IResourceResolver rr = tr.getResourceResolver();
			try (Reader source = this.makeReader(
					rr.getResourceAsStream(tpp, tr.getResourceName()), tr)) {
				Document doc = MarkdownTemplateResolver.MARKDOWN
						.getTemplateParser().parseTemplate(conf,
								tr.getTemplateName(), source);
				doc.precompute(conf);
				return doc.getChildren();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return Collections.emptyList();
	}

	protected Reader makeReader(InputStream in, TemplateResolution tr) {
		final String characterEncoding = tr.getCharacterEncoding();
		Reader reader = null;
		if (StringUtils.isEmpty(characterEncoding) == false) {
			try {
				reader = new InputStreamReader(in, characterEncoding);
			} catch (final UnsupportedEncodingException e) {
				throw new TemplateInputException("Exception parsing document",
						e);
			}
		} else {
			reader = new InputStreamReader(in);
		}
		return reader;
	}
}
