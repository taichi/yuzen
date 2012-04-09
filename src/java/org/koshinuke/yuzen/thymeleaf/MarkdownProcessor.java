package org.koshinuke.yuzen.thymeleaf;

import java.util.Collections;
import java.util.List;

import org.thymeleaf.Arguments;
import org.thymeleaf.Template;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

/**
 * @author taichi
 */
public class MarkdownProcessor extends AbstractChildrenModifierAttrProcessor {

	public MarkdownProcessor() {
		super(MarkdownTemplateResolver.TEMPLATE_MODE.toLowerCase());
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
			final TemplateProcessingParameters tpp = new TemplateProcessingParameters(
					arguments.getConfiguration(), v.toString(),
					arguments.getContext());
			final Template t = arguments.getTemplateRepository().getTemplate(
					tpp);
			return t.getDocument().getChildren();
		}
		return Collections.emptyList();
	}
}
