package org.openlca.app.editors.graphical.action;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.openlca.app.editors.graphical.GraphEditor;

public abstract class EditorAction extends Action implements UpdateAction {

	GraphEditor editor;

	public void setEditor(GraphEditor editor) {
		this.editor = editor;
	}

	protected EditorAction() {
	}

	protected EditorAction(String text, int style) {
		super(text, style);
	}

	@Override
	public void update() {
		setEnabled(accept(editor.getSelection()));
	}

	protected abstract boolean accept(ISelection selection);

}
