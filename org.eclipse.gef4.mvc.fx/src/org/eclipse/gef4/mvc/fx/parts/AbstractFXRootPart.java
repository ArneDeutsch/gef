/*******************************************************************************
 * Copyright (c) 2014 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.parts;

import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.parts.AbstractRootPart;
import org.eclipse.gef4.mvc.parts.IRootPart;
import org.eclipse.gef4.mvc.viewer.IViewer;

import javafx.scene.Node;

/**
 * Abstract base implementation for a JavaFX-specific {@link IRootPart}.
 *
 * @author anyssen
 *
 * @param <N>
 *            The visual {@link Node} used by this {@link AbstractFXRootPart}.
 */
public abstract class AbstractFXRootPart<N extends Node>
		extends AbstractRootPart<Node, N> {

	/**
	 * Constructs a new {@link AbstractFXRootPart}.
	 */
	public AbstractFXRootPart() {
		super();
	}

	@Override
	public FXViewer getViewer() {
		return (FXViewer) super.getViewer();
	}

	@Override
	public void setAdaptable(IViewer<Node> viewer) {
		IViewer<Node> oldViewer = getViewer();
		if (oldViewer != null && viewer != oldViewer) {
			unregister(oldViewer);
		}
		super.setAdaptable(viewer);
		if (viewer != null && viewer != oldViewer) {
			register(viewer);
		}
	}

}