package org.openlca.app.editors.graphical.model.commands;

import org.eclipse.gef.commands.Command;
import org.openlca.app.M;
import org.openlca.app.editors.graphical.GraphEditor;
import org.openlca.app.editors.graphical.model.Graph;
import org.openlca.app.editors.graphical.model.GraphLink;
import org.openlca.app.editors.graphical.model.Node;
import org.openlca.core.model.ProcessLink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.openlca.app.tools.graphics.model.Side.INPUT;
import static org.openlca.app.tools.graphics.model.Side.OUTPUT;

public class RemoveSupplyChainCommand extends Command {

	private final ArrayList<ProcessLink> providerLinks;
	private final GraphEditor editor;
	private final Graph graph;
	private final Node host;

	//	Product system objects
	private Set<Long> processes = new HashSet<>();
	private Set<ProcessLink> links = new HashSet<>();

	// Graph objects
	private Set<GraphLink> connections = new HashSet<>();
	private Set<Node> nodes = new HashSet<>();
	private Set<Long> isRemoving = new HashSet<>();

	public RemoveSupplyChainCommand(ArrayList<ProcessLink> links, Node node) {
		providerLinks = links;
		host = node;
		editor = host.getGraph().getEditor();
		graph = editor.getModel();
		setLabel(M.RemoveSupplyChain.toLowerCase(Locale.ROOT));
	}

	@Override
	public boolean canExecute() {
		return !host.isOnlyChainingReferenceNode(INPUT);
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public void undo() {
		for (var node : nodes)
			editor.getProductSystem().processes.add(node.descriptor.id);
		graph.addChildren(nodes.stream().toList());
		for (var link : connections) {
			link.reconnect();
			editor.getProductSystem().processLinks.add(link.processLink);
			graph.linkSearch.put(link.processLink);
		}
	}

	@Override
	public void redo() {
		for (var link : providerLinks)
			if (!processes.contains(link.providerId)) {
				// Removing the supply chain if it does not provide to any other
				// recipient.
				var otherLinks = graph.linkSearch
						.getProviderLinks(link.providerId)
						.stream()
						.filter(l -> l.processId != l.providerId) // Self-loop
						.filter(l -> l != link)
						.toList();
				if (otherLinks.isEmpty()) {
					removeChain(link.providerId, INPUT);
					removeProcess(link.providerId);
				}
				removeLink(link);
			}
		if (!processes.isEmpty() || !links.isEmpty()) {
			editor.setDirty();
		}
	}

	private void removeLink(ProcessLink link) {
		links.add(link);
		graph.getProductSystem().processLinks.remove(link);
		graph.linkSearch.remove(link);
		var graphLink = graph.getLink(link);
		if (graphLink != null) {
			connections.add(graphLink);
			graphLink.disconnect();
		}
	}

	private void removeProcess(Long process) {
		processes.add(process);
		graph.getProductSystem().processes.remove(process);
		var node = graph.getNode(process);
		if (node != null) {
			nodes.add(node);
			graph.removeChild(node);
		}
	}

	/**
	 * Recursively remove all the input nodes connected to the given
	 * node.
	 * This method does not remove:
	 * <ul>
	 *   <li>the reference node,</li>
	 *   <li>nodes that are chained to the reference node. </li>
	 * </ul>
	 */
	private void removeChain(Long process, int side) {
		if (isRemoving.contains(process))
			return;
		isRemoving.add(process);

		var links = side == INPUT
				? graph.linkSearch.getConnectionLinks(process)
				: graph.linkSearch.getProviderLinks(process);

		for (var link : links) {
			var thisProcess = side == INPUT
					? link.processId
					: link.providerId;
			var otherProcess = side == INPUT
					? link.providerId
					: link.processId;

			if (thisProcess != process  // wrong link
					|| otherProcess == this.host.descriptor.id)  // double link
				continue;

			var refNode = this.host.getGraph().getReferenceNode();
			if (this.host != refNode
					&& (graph.linkSearch.isChainingReference(
							otherProcess, side, refNode.descriptor.id)
					|| otherProcess == refNode.descriptor.id))
				continue;

			removeLink(link);
			removeChain(otherProcess, INPUT);
			removeChain(otherProcess, OUTPUT);

			var linkFiltered = graph.linkSearch.getLinks(otherProcess).stream()
					.filter(l -> l.processId != l.providerId)
					.toList();
			if (!linkFiltered.isEmpty())
				continue;

			removeProcess(otherProcess);
		}
		isRemoving.remove(process);
	}

}