package org.openlca.app.preferencepages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.openlca.app.M;
import org.openlca.app.rcp.RcpActivator;
import org.openlca.app.util.Dialog;
import org.openlca.app.util.UI;
import org.openlca.ilcd.io.Authentication;
import org.openlca.ilcd.io.NetworkClient;
import org.openlca.util.Strings;

public class IoPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String ID = "org.openlca.io.IoPreferencePage";
	private final List<FieldEditor> editors = new ArrayList<>();

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		UI.gridLayout(comp, 1);
		UI.gridData(comp, true, true);
		createIlcdNetworkContents(comp);
		UI.horizontalSeparator(comp);
		createIlcdOtherContents(comp);
		return comp;
	}

	private void createIlcdNetworkContents(Composite parent) {
		Group section = new Group(parent, SWT.SHADOW_OUT);
		section.setText(M.ILCDNetworkSettings);
		StringFieldEditor urlEditor = new StringFieldEditor(
				IoPreference.ILCD_URL, M.URL, section);
		addField(urlEditor);
		StringFieldEditor userEditor = new StringFieldEditor(
				IoPreference.ILCD_USER, M.User, section);
		addField(userEditor);
		StringFieldEditor passwordEditor = new StringFieldEditor(
				IoPreference.ILCD_PASSWORD, M.Password, section);
		passwordEditor.getTextControl(section).setEchoChar('*');
		addField(passwordEditor);
		UI.gridLayout(section, 2);
		UI.gridData(section, true, false);
	}

	private void createIlcdOtherContents(Composite parent) {
		Group section = new Group(parent, SWT.SHADOW_OUT);
		section.setText(M.ILCDOtherSettings);
		ComboFieldEditor langEditor = new ComboFieldEditor(
				IoPreference.ILCD_LANG, M.Language, getLanguages(),
				section);
		addField(langEditor);
		UI.gridLayout(section, 2);
		UI.gridData(section, true, false);
	}

	private String[][] getLanguages() {
		Locale displayLang = new Locale(Language.getApplicationLanguage()
				.getCode());
		Locale[] all = Locale.getAvailableLocales();
		List<String[]> namesAndValues = new ArrayList<>();
		Set<String> added = new HashSet<>();
		for (int i = 0; i < all.length; i++) {
			String lang = all[i].getLanguage();
			if (added.contains(lang))
				continue;
			String[] nameAndValue = new String[2];
			nameAndValue[0] = all[i].getDisplayLanguage(displayLang);
			nameAndValue[1] = lang;
			namesAndValues.add(nameAndValue);
			added.add(lang);
		}
		Collections.sort(namesAndValues, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return Strings.compare(o1[0], o2[0]);
			}
		});
		return namesAndValues.toArray(new String[namesAndValues.size()][]);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getApplyButton().setText(M.Test);
		setImageDescriptor(RcpActivator.imageDescriptorFromPlugin(
				RcpActivator.PLUGIN_ID, "icons/network16.png"));
	}

	@Override
	protected void performApply() {
		super.performApply();
		String url = IoPreference.getIlcdUrl();
		String user = IoPreference.getIlcdUser();
		String password = IoPreference.getIlcdPassword();
		NetworkClient client = new NetworkClient(url, user, password);
		testConnection(client);
	}

	@Override
	public boolean performOk() {
		for (FieldEditor editor : editors)
			editor.store();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		IoPreference.reset();
		for (FieldEditor editor : editors)
			editor.load();
	}

	private void testConnection(NetworkClient client) {
		try {
			client.connect();
			checkAuthentication(client.getAuthentication());
		} catch (Exception e) {
			Dialog.showError(getShell(), M.ILCD_CONNECTION_FAILED_MSG
					+ " (" + e.getMessage() + ")");
		}
	}

	private void checkAuthentication(Authentication auth) {
		if (!auth.isAuthenticated())
			Dialog.showError(getShell(), M.ILCD_AUTHENTICATION_FAILED_MSG);
		else if (!auth.isReadAllowed() || !auth.isExportAllowed())
			Dialog.showWarning(getShell(), M.ILCD_NO_READ_OR_WRITE_ACCESS_MSG);
		else
			Dialog.showInfo(getShell(), M.ILCD_CONNECTION_WORKS_MSG);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(RcpActivator.getDefault().getPreferenceStore());
	}

	public static final void open(Shell shell) {
		PreferencesUtil.createPreferenceDialogOn(shell, IoPreferencePage.ID,
				null, null).open();
	}

	private void addField(FieldEditor editor) {
		editors.add(editor);
		editor.setPage(this);
		editor.setPreferenceStore(getPreferenceStore());
		editor.load();
	}
}