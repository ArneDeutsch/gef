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
package org.eclipse.gef4.mvc.fx.tools;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import org.eclipse.gef4.mvc.fx.parts.FXPartUtils;
import org.eclipse.gef4.mvc.fx.viewer.IFXViewer;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IRootPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.tools.AbstractSelectionTool;

// TODO: use drag tool?
public class FXSelectionTool extends AbstractSelectionTool<Node> {

	private final EventHandler<MouseEvent> pressedHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent event) {
			IVisualPart<Node> targetPart = FXPartUtils.getEventTargetPart(
					getDomain().getViewer(), event);
			if (targetPart == null) {
				return;
			}

			boolean append = event.isControlDown();
			if (targetPart instanceof IRootPart) {
				select(null, append);
			} else if (targetPart instanceof IContentPart) {
				select((IContentPart<Node>) targetPart, append);
			} else {
				// IGNORE
				// throw new IllegalArgumentException(
				// "This tool only supports IRootVisualPart and IContentPart targets");
			}
		}
	};

	@Override
	protected void registerListeners() {
		((IFXViewer) getDomain().getViewer()).getScene().addEventHandler(
				MouseEvent.MOUSE_PRESSED, pressedHandler);
	};

	@Override
	protected void unregisterListeners() {
		((IFXViewer) getDomain().getViewer()).getScene().removeEventHandler(
				MouseEvent.MOUSE_PRESSED, pressedHandler);
	}

}
