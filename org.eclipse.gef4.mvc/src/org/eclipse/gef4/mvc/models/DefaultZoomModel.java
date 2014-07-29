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
package org.eclipse.gef4.mvc.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 
 * @author mwienand
 * 
 */
public class DefaultZoomModel implements IZoomModel {

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private double zoom = IZoomModel.DEFAULT_ZOOM_FACTOR;

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public double getZoomFactor() {
		return zoom;
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void setZoomFactor(double zoomFactor) {
		if (zoomFactor <= 0) {
			throw new IllegalArgumentException(
					"Expected: Positive double value. Given: <" + zoomFactor
							+ ">.");
		}
		double oldZoom = zoom;
		zoom = zoomFactor;
		pcs.firePropertyChange(ZOOM_FACTOR_PROPERTY, oldZoom, zoom);
	}

}
