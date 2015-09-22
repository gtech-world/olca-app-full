package org.openlca.app.editors.parameters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.openlca.app.editors.ModelEditor;
import org.openlca.app.util.Error;
import org.openlca.core.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalParameterEditor extends ModelEditor<Parameter> {

	public static String ID = "editors.parameter";
	private Logger log = LoggerFactory.getLogger(getClass());
	private GlobalParameterInfoPage infoPage;

	public GlobalParameterEditor() {
		super(Parameter.class);
	}

	@Override
	protected void addPages() {
		try {
			addPage(infoPage = new GlobalParameterInfoPage(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (infoPage.hasErrors())
			Error.showBox("#Can not save parameter, because formula contains errors");
		else
			super.doSave(monitor);
	}

}
