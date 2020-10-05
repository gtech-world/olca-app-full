package org.openlca.app.editors.graphical.action;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openlca.app.editors.graphical.action.ChangeAllStateAction.MAXIMIZE;
import static org.openlca.app.editors.graphical.action.ChangeAllStateAction.MINIMIZE;
import static org.openlca.app.editors.graphical.action.HideShowAction.HIDE;
import static org.openlca.app.editors.graphical.action.HideShowAction.SHOW;
import static org.openlca.app.editors.graphical.action.MarkingAction.MARK;
import static org.openlca.app.editors.graphical.action.MarkingAction.UNMARK;
import static org.openlca.app.editors.graphical.action.MassExpansionAction.COLLAPSE;
import static org.openlca.app.editors.graphical.action.MassExpansionAction.EXPAND;
import static org.openlca.app.editors.graphical.action.SearchConnectorsAction.PROVIDER;
import static org.openlca.app.editors.graphical.action.SearchConnectorsAction.RECIPIENTS;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.openlca.app.editors.graphical.GraphEditor;
import org.openlca.app.viewers.Selections;

public class GraphActions {

	private GraphActions() {
	}

	public static <T> T firstSelectedOf(ISelection s, Class<T> type) {
		var obj = Selections.firstOf(s);
		if (obj == null || type == null)
			return null;
		if (type.isInstance(s))
			return type.cast(s);
		if (!(obj instanceof EditPart))
			return null;
		var model = ((EditPart) obj).getModel();
		return type.isInstance(model)
				? type.cast(model)
				: null;
	}

	public static <T> List<T> allSelectedOf(ISelection s, Class<T> type) {
		var objects = Selections.allOf(s);
		if (objects.isEmpty() || type == null)
			return Collections.emptyList();
		return objects.stream()
				.map(obj -> obj instanceof EditPart
						? ((EditPart) obj).getModel()
						: obj)
				.filter(type::isInstance)
				.map(type::cast)
				.collect(Collectors.toList());
	}

	public static BuildSupplyChainAction buildSupplyChain() {
		return new BuildSupplyChainAction();
	}

	public static BuildNextTierAction buildNextTier() {
		return new BuildNextTierAction();
	}

	public static IAction buildSupplyChainMenu(GraphEditor editor) {
		BuildSupplyChainMenuAction action = new BuildSupplyChainMenuAction();
		action.editor = editor;
		return action;
	}

	public static IAction minimizeAll(GraphEditor editor) {
		ChangeAllStateAction action = new ChangeAllStateAction(MINIMIZE);
		action.editor = editor;
		return action;
	}

	public static IAction maximizeAll(GraphEditor editor) {
		ChangeAllStateAction action = new ChangeAllStateAction(MAXIMIZE);
		action.editor = editor;
		return action;
	}

	public static IAction expandAll(GraphEditor editor) {
		MassExpansionAction action = new MassExpansionAction(EXPAND);
		action.editor = editor;
		return action;
	}

	public static IAction collapseAll(GraphEditor editor) {
		MassExpansionAction action = new MassExpansionAction(COLLAPSE);
		action.editor = editor;
		return action;
	}

	public static IAction show(GraphEditor editor, TreeViewer viewer) {
		return new HideShowAction(editor, viewer, SHOW);
	}

	public static IAction hide(GraphEditor editor, TreeViewer viewer) {
		return new HideShowAction(editor, viewer, HIDE);
	}

	public static IAction layoutMenu(GraphEditor editor) {
		LayoutMenuAction action = new LayoutMenuAction();
		action.setEditor(editor);
		return action;
	}

	public static IAction openMiniatureView(GraphEditor editor) {
		OpenMiniatureViewAction action = new OpenMiniatureViewAction();
		action.editor = editor;
		return action;
	}

	public static IAction removeAllConnections(GraphEditor editor) {
		RemoveAllConnectionsAction action = new RemoveAllConnectionsAction();
		action.editor = editor;
		return action;
	}

	public static IAction removeSupplyChain(GraphEditor editor) {
		RemoveSupplyChainAction action = new RemoveSupplyChainAction();
		action.editor = editor;
		return action;
	}

	public static IAction saveImage(GraphEditor editor) {
		SaveImageAction action = new SaveImageAction();
		action.editor = editor;
		return action;
	}

	public static IAction mark(GraphEditor editor) {
		MarkingAction action = new MarkingAction(MARK);
		action.editor = editor;
		return action;
	}

	public static IAction unmark(GraphEditor editor) {
		MarkingAction action = new MarkingAction(UNMARK);
		action.editor = editor;
		return action;
	}

	public static IAction searchProviders(GraphEditor editor) {
		SearchConnectorsAction action = new SearchConnectorsAction(PROVIDER);
		action.editor = editor;
		return action;
	}

	public static IAction searchRecipients(GraphEditor editor) {
		SearchConnectorsAction action = new SearchConnectorsAction(RECIPIENTS);
		action.editor = editor;
		return action;
	}

	public static IAction open(GraphEditor editor) {
		OpenAction action = new OpenAction();
		action.editor = editor;
		return action;
	}

	public static IAction showOutline() {
		return new ShowOutlineAction();
	}

}