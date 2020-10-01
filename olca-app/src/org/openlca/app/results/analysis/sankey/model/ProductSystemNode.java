package org.openlca.app.results.analysis.sankey.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionRouter;
import org.openlca.app.results.analysis.sankey.SankeyDiagram;
import org.openlca.core.model.ProductSystem;

public class ProductSystemNode {

	public final ProductSystem productSystem;
	public final SankeyDiagram editor;
	public final List<ProcessNode> processNodes = new ArrayList<>();

	public ProductSystemNode(
			ProductSystem productSystem,
			SankeyDiagram editor) {
		this.productSystem = productSystem;
		this.editor = editor;
	}

	public void setRouted(boolean enabled) {
		var router = ConnectionRouter.NULL;
		if (enabled)
			router = LinkPart.ROUTER;
		for (var node : processNodes) {
			var pNode = node;
			for (var link : pNode.links) {
				link.figure.setConnectionRouter(router);
			}
		}
	}
}
