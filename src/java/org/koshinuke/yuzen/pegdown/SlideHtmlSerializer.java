package org.koshinuke.yuzen.pegdown;

import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.eclipse.jgit.util.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.TextNode;

/**
 * @author taichi
 */
public class SlideHtmlSerializer extends ToHtmlSerializer {

	int pages = 0;

	String title;

	public SlideHtmlSerializer() {
		super(new LinkRenderer());
	}

	public SlideHtmlSerializer(LinkRenderer linkRenderer) {
		super(linkRenderer);
	}

	@Override
	public String toHtml(RootNode astRoot) {
		checkArgNotNull(astRoot, "astRoot");
		this.printer.print("<section>");
		astRoot.accept(this);
		this.printer.print("</section>");
		return this.printer.getString();
	}

	@Override
	public void visit(HeaderNode node) {
		int level = node.getLevel();
		if (level < 3 && 0 < this.pages++) {
			this.printer.print("</section><section>");
		}
		this.printTag(node, "h" + node.getLevel());
	}

	@Override
	public void visit(TextNode node) {
		if (this.pages < 2 && StringUtils.isEmptyOrNull(this.title)) {
			this.title = node.getText();
		}
		super.visit(node);
	}

	public int getPages() {
		return this.pages;
	}
}