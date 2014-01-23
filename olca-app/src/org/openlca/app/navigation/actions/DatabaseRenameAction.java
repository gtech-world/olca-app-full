package org.openlca.app.navigation.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.openlca.app.Messages;
import org.openlca.app.db.Database;
import org.openlca.app.db.DerbyConfiguration;
import org.openlca.app.db.IDatabaseConfiguration;
import org.openlca.app.navigation.DatabaseElement;
import org.openlca.app.navigation.INavigationElement;
import org.openlca.app.navigation.Navigator;
import org.openlca.app.resources.ImageType;
import org.openlca.app.util.Editors;
import org.openlca.app.util.UI;
import org.openlca.core.database.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class DatabaseRenameAction extends Action implements INavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private DerbyConfiguration config;

	public DatabaseRenameAction() {
		setText(Messages.Rename);
		setImageDescriptor(ImageType.CHANGE_ICON.getDescriptor());
	}

	@Override
	public boolean accept(INavigationElement<?> element) {
		if (!(element instanceof DatabaseElement))
			return false;
		DatabaseElement dbElement = (DatabaseElement) element;
		IDatabaseConfiguration config = dbElement.getContent();
		if (!(config instanceof DerbyConfiguration))
			return false;
		else {
			this.config = (DerbyConfiguration) config;
			return true;
		}
	}

	@Override
	public boolean accept(List<INavigationElement<?>> elements) {
		return false;
	}

	@Override
	public void run() {
		if (config == null)
			return;
		InputDialog dialog = new InputDialog(UI.shell(),
				Messages.Rename,
				Messages.PleaseEnterNewName,
				config.getName(), null);
		if (dialog.open() != Window.OK)
			return;
		String newName = dialog.getValue();
		File newDbFolder = new File(config.getFolder(), newName);
		if (!DbUtils.isValidName(newName) || newDbFolder.exists()) {
			org.openlca.app.util.Error.showBox("The given name is not a valid" +
					" database name or the database already exists.");
			return;
		}
		doRename(newName);
	}

	private void doRename(String newName) {
		boolean isActive = Database.isActive(config);
		try {
			if (isActive) {
				Editors.closeAll();
				Database.close();
			}
			File oldDbFolder = new File(config.getFolder(), config.getName());
			File newDbFolder = new File(config.getFolder(), newName);
			boolean success = oldDbFolder.renameTo(newDbFolder);
			if (!success) {
				log.error("failed to rename folder");
				return;
			}
			Database.remove(config);
			config.setName(newName);
			Database.register(config);
			if (isActive)
				Database.activate(config);
			Navigator.refresh();
		} catch (Exception e) {
			log.error("failed to rename database", e);
		}
	}
}
