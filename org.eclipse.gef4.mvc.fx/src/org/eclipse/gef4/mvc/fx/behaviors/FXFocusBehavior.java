/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.behaviors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javafx.scene.Node;

import org.eclipse.gef4.mvc.behaviors.AbstractBehavior;
import org.eclipse.gef4.mvc.models.IFocusModel;

public class FXFocusBehavior extends AbstractBehavior<Node> implements
		PropertyChangeListener {

	public FXFocusBehavior() {
	}

	@Override
	public void activate() {
		super.activate();
		getHost().getRoot().getViewer().getFocusModel()
				.addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getHost().getRoot().getViewer().getFocusModel()
				.removePropertyChangeListener(this);
		super.deactivate();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (IFocusModel.VIEWER_FOCUS_PROPERTY.equals(evt.getPropertyName())) {
			// viewer focus changed
		} else if (IFocusModel.FOCUS_PROPERTY.equals(evt.getPropertyName())) {
			if (evt.getNewValue() == getHost()) {
				getHost().getVisual().requestFocus();
			}
		}
	}

}
