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
 * Note: Parts of this interface have been transferred from org.eclipse.gef.EditPart
 * 
 *******************************************************************************/
package org.eclipse.gef4.mvc.parts;

import java.util.List;

import org.eclipse.gef4.mvc.IActivatable;
import org.eclipse.gef4.mvc.IPropertyChangeSupport;
import org.eclipse.gef4.mvc.bindings.IAdaptable;

public interface IVisualPart<V> extends IActivatable, IAdaptable, IPropertyChangeSupport {

	// TODO: add others
	public static final String PARENT_PROPERTY = "parent";

	/**
	 * Returns the {@link IRootPart}. This method should only be called
	 * internally or by helpers such as edit policies. The root can be used to
	 * get the viewer.
	 * 
	 * @return <code>null</code> or the {@link IRootPart}
	 */
	public IRootPart<V> getRoot();

	public abstract V getVisual();

	public boolean isRefreshVisual();

	/**
	 * While interacting with IVisualParts the visual representation should not
	 * be updated to correspond with its model. To suppress
	 * {@link #refreshVisual()} from doing so, you can set this flag to
	 * <code>false</code>.
	 * 
	 * @param refreshVisual
	 */
	public void setRefreshVisual(boolean refreshVisual);

	public void refreshVisual();

	public void setParent(IVisualPart<V> parent);

	public IVisualPart<V> getParent();

	public List<IVisualPart<V>> getChildren();

	public void removeChild(IVisualPart<V> child);

	public void removeChildren(List<? extends IVisualPart<V>> children);

	public void addChild(IVisualPart<V> child, int index);

	public void addChild(IVisualPart<V> child);

	public void addChildren(List<? extends IVisualPart<V>> children);

	public void reorderChild(IVisualPart<V> child, int index);

	// public void addVisualToParentVisual(IVisualPart<V> parent, V
	// parentVisual);
	//
	// public void removeVisualFromParentVisual(IVisualPart<V> parent, V
	// parentVisual);

	public void addAnchored(IVisualPart<V> anchored);

	// TODO: add by index and reordering of anchored

	public void addAnchoreds(List<? extends IVisualPart<V>> anchoreds);

	public void removeAnchored(IVisualPart<V> anchored);

	public void removeAnchoreds(List<? extends IVisualPart<V>> anchoreds);

	public List<IVisualPart<V>> getAnchoreds();

	public void addAnchorage(IVisualPart<V> anchorage);

	public void removeAnchorage(IVisualPart<V> anchorage);

	public List<IVisualPart<V>> getAnchorages();

	// anchorage visual may not be the visual of the anchorage itself!
	public void attachVisualToAnchorageVisual(IVisualPart<V> anchorage,
			V anchorageVisual);

	public void detachVisualFromAnchorageVisual(IVisualPart<V> anchorage,
			V anchorageVisual);
}