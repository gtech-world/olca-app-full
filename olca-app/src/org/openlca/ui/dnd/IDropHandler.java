/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.ui.dnd;

import java.util.List;

import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.ui.viewer.ViewerDropComponent;

/**
 * An interface for handling of dropping model components to a table viewer
 * {@link ViewerDropComponent}
 * 
 * @author Sebastian Greve
 * 
 */
public interface IDropHandler {

	/**
	 * Handles the dropped components
	 * 
	 * @param droppedComponents
	 *            - the components dropped
	 */
	void handleDrop(List<BaseDescriptor> droppedModels);

}
