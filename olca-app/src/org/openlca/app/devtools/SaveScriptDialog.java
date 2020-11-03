package org.openlca.app.devtools;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.openlca.app.db.Database;
import org.openlca.app.navigation.Navigator;
import org.openlca.app.rcp.Workspace;
import org.openlca.app.util.Controls;
import org.openlca.app.util.MsgBox;
import org.openlca.app.util.UI;

import com.ibm.icu.text.CharsetDetector;

public class SaveScriptDialog extends FormDialog {

	private final String script;
	private String name;
	private boolean asGlobal = true;

	public static void forImportOf(File file) {
		if (file == null || !file.exists())
			return;
		// for imported scripts we try to detect the
		// encoding. in the openLCA workspace we save
		// everything encoded in utf-8
		try {
			var bytes = Files.readAllBytes(file.toPath());
			var match = new CharsetDetector().setText(bytes).detect();
			var charset = match == null || match.getName() == null
					? Charset.defaultCharset()
					: Charset.forName(match.getName());
			var script = new String(bytes, charset);
			forScriptOf(file.getName(), script);
		} catch (Exception e) {
			MsgBox.error("Failed to read file",
					"Failed to read file " + file
							+ ": " + e.getMessage());
		}
	}

	public static void forScriptOf(String name, String script) {
		var dialog = new SaveScriptDialog(name, script);
		dialog.open();
	}

	private SaveScriptDialog(String name, String script) {
		super(UI.shell());
		this.name = name == null
				? "script.py"
				: name;
		this.script = script;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Save script");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 350);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var tk = mform.getToolkit();
		var body = UI.formBody(mform.getForm(), tk);
		UI.gridLayout(body, 2);
		var text = UI.formText(body, tk, "File name:");
		text.setText(name);
		text.addModifyListener(e -> name = text.getText());

		// add a `save as local` script option when
		// a database is open
		var db = Database.get();
		if (db == null)
			return;

		UI.filler(body, tk);
		var global = tk.createButton(
				body, "As global script", SWT.RADIO);
		global.setSelection(true);
		Controls.onSelect(
				global, e -> asGlobal = global.getSelection());

		UI.filler(body, tk);
		var local = tk.createButton(
				body, "As script in database " + db.getName(),
				SWT.RADIO);
		local.setSelection(false);
		Controls.onSelect(
				local, e -> this.asGlobal = !local.getSelection());
	}

	@Override
	protected void okPressed() {
		if (name.isEmpty()) {
			MsgBox.error("Empty name",
					"An empty name is not allowed");
			return;
		}

		var file = file();
		if (file.exists()) {
			MsgBox.error("Script already exists",
					"The script " + name + " already exists");
			return;
		}

		try {
			Files.writeString(file.toPath(),
					script, StandardCharsets.UTF_8);
			Navigator.refresh();
			super.okPressed();
		} catch (Exception e) {
			MsgBox.error("Failed to save script "
					+ name + ": " + e.getMessage());
		}
	}

	private File file() {
		var db = Database.get();
		if (asGlobal || db == null) {
			var dir = new File(Workspace.getDir(), "scripts");
			if (!dir.exists() && !dir.mkdirs())
				throw new RuntimeException(
						"Could not create `scripts` folder: " + dir);
			return new File(dir, name);
		}

		// jump to the global folder when no database
		// local folder can be created
		var dbDir = db.getFileStorageLocation();
		if (dbDir == null) {
			asGlobal = true;
			return file();
		}
		var dir = new File(dbDir, "scripts");
		if (!dir.exists() && !dir.mkdirs()) {
			asGlobal = true;
			return file();
		}

		return new File(dir, name);
	}
}