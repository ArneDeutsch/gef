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

import javafx.scene.Node;

import org.eclipse.gef4.fx.nodes.FXGeometryNode;
import org.eclipse.gef4.fx.nodes.IFXConnection;
import org.eclipse.gef4.geometry.convert.fx.JavaFX2Geometry;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.mvc.behaviors.AbstractHoverBehavior;

/**
 * 
 * @author anyssen
 * 
 */
public class FXHoverBehavior extends AbstractHoverBehavior<Node> {

	@SuppressWarnings("rawtypes")
	@Override
	protected IGeometry getFeedbackGeometry() {
		Node visual = getHost().getVisual();

		// in case a FXGeometryNode is used, we can return its IGeometry
		if (visual instanceof IFXConnection) {
			Node curveNode = ((IFXConnection) visual).getCurveNode();
			if (curveNode instanceof FXGeometryNode) {
				return ((FXGeometryNode) curveNode).getGeometry();
			}
		} else if (visual instanceof FXGeometryNode) {
			return ((FXGeometryNode) visual).getGeometry();
		}

		return JavaFX2Geometry.toRectangle(visual.getLayoutBounds());
	}

}
