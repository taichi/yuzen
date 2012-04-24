package org.koshinuke.yuzen.thymeleaf;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * @author taichi
 */
public class YuzenDialect extends AbstractDialect {

	final TemplateResolver markdownResolver;

	public YuzenDialect(TemplateResolver md) {
		this.markdownResolver = md;
	}

	@Override
	public String getPrefix() {
		return "yz";
	}

	@Override
	public boolean isLenient() {
		return false;
	}

	@Override
	public Set<IProcessor> getProcessors() {
		final Set<IProcessor> processors = new HashSet<IProcessor>();
		processors.add(new MarkdownProcessor(this.markdownResolver));
		return processors;
	}
}
