package org.openlca.app.editors.costs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.app.Messages;
import org.openlca.app.editors.InfoSection;
import org.openlca.app.editors.ModelEditor;
import org.openlca.app.editors.ModelPage;
import org.openlca.app.util.UI;
import org.openlca.core.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyEditor extends ModelEditor<Currency> {

	private Logger log = LoggerFactory.getLogger(getClass());

	public CurrencyEditor() {
		super(Currency.class);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private class Page extends ModelPage<Currency> {

		private Page() {
			super(CurrencyEditor.this, "CurrencyPage",
					Messages.GeneralInformation);
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			ScrolledForm form = UI.formHeader(mform, Messages.CostCategory + ":"
					+ getModel().getName());
			FormToolkit toolkit = mform.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			InfoSection infoSection = new InfoSection(getEditor());
			infoSection.render(body, toolkit);
			body.setFocus();
			form.reflow(true);
		}
	}

}
