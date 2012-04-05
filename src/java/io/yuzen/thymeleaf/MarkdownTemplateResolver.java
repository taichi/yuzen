package io.yuzen.thymeleaf;

import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.templatemode.TemplateModeHandler;
import org.thymeleaf.templateresolver.TemplateResolver;
import org.thymeleaf.templatewriter.XhtmlHtml5TemplateWriter;

/**
 * @author taichi
 */
public class MarkdownTemplateResolver extends TemplateResolver {

	public static final String TEMPLATE_MODE = "MARKDOWN";

	public static final TemplateModeHandler MARKDOWN;

	private final static int MAX_PARSERS_POOL_SIZE = 24;
	static {
		// see. org.thymeleaf.templatemode.StandardTemplateModeHandlers
		final int availableProcessors = Runtime.getRuntime()
				.availableProcessors();
		final int poolSize = Math.min(
				availableProcessors <= 2 ? availableProcessors
						: availableProcessors - 1, MAX_PARSERS_POOL_SIZE);
		MARKDOWN = new TemplateModeHandler(TEMPLATE_MODE, new MarkdownParser(
				poolSize), new XhtmlHtml5TemplateWriter());
	}

	public MarkdownTemplateResolver(IResourceResolver resourceResolver) {
		this.setSuffix(".md");
		this.setResourceResolver(resourceResolver);
	}

	@Override
	protected String computeTemplateMode(
			TemplateProcessingParameters templateProcessingParameters) {
		return TEMPLATE_MODE;
	}
}
