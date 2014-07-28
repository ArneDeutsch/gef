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
package org.eclipse.gef4.fx.anchors;

import javafx.scene.Node;

import org.eclipse.gef4.geometry.planar.Point;

/**
 * A {@link FXStaticAnchor} provides a static position per anchor link.
 * 
 * @author mwienand
 * 
 */
public class FXStaticAnchor extends AbstractFXAnchor {

	/**
	 * Creates an "empty" static anchor, i.e. no positions are stored, yet.
	 */
	public FXStaticAnchor() {
		super(null);
	}

	public FXStaticAnchor(AnchorKey key, Point position) {
		this(null, key, position);
	}

	public FXStaticAnchor(Node anchorage, AnchorKey key, Point position) {
		super(anchorage);
		attach(key);
		positionProperty().put(key, position);
	}

	@Override
	protected void recomputePositions(Node anchored) {
		// nothing to compute (*static* anchor)
	}

	public void setPosition(AnchorKey key, Point position) {
		positionProperty().put(key, position);
	}

}
