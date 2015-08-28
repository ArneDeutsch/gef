/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API & implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.zest.fx.ui.jface;

import org.eclipse.gef4.fx.nodes.IFXDecoration;

public interface IEdgeDecorationProvider {

	public IFXDecoration getSourceDecoration(Object contentSourceNode, Object contentTargetNode);

	public IFXDecoration getTargetDecoration(Object contentSourceNode, Object contentTargetNode);

}
