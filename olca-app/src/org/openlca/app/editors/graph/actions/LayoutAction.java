package org.openlca.app.editors.graph.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.osgi.util.NLS;
import org.openlca.app.M;
import org.openlca.app.editors.graph.GraphEditor;
import org.openlca.app.editors.graph.edit.GraphEditPart;
import org.openlca.app.rcp.images.Icon;

import static org.openlca.app.editors.graph.requests.GraphRequestConstants.REQ_LAYOUT;

public class LayoutAction extends WorkbenchPartAction {

	private final GraphEditor editor;

	public LayoutAction(GraphEditor part) {
		super(part);
		editor = part;
		setText(NLS.bind(M.LayoutAs, M.Tree));
		setId(ActionIds.LAYOUT_TREE);
		setImageDescriptor(Icon.LAYOUT.descriptor());
	}

	@Override
	public void run() {
		execute(getCommand());
	}

	@Override
	protected boolean calculateEnabled() {
		var command = getCommand();
		if (command == null)
			return false;
		return command.canExecute();
	}

	private Command getCommand() {
		var request = new Request(REQ_LAYOUT);
		var viewer = (GraphicalViewer) getWorkbenchPart().getAdapter(
			GraphicalViewer.class);
		var registry = viewer.getEditPartRegistry();
		var graphEditPart = (GraphEditPart) registry.get(editor.getModel());
		return graphEditPart.getCommand(request);
	}

}
