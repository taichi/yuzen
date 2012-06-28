package org.koshinuke.yuzen.pegdown;

import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.RootNode;

/**
 * @author taichi
 */
public class SlideHtmlSerializer extends ToHtmlSerializer {

	int pages = 0;

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
		if (0 < this.pages++ && level < 3) {
			this.printer.print("</section><section>");
		}
		this.printTag(node, "h" + node.getLevel());
	}

	public int getPages() {
		return this.pages;
	}
}