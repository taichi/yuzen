package org.koshinuke.yuzen.thymeleaf;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

/**
 * @author taichi
 */
public class YuzenDialect extends AbstractDialect {

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
		processors.add(new MarkdownProcessor());
		return processors;
	}
}
