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
 * Note: Parts of this interface have been transferred from org.eclipse.gef.editparts.AbstractEditPart and org.eclipse.gef.editparts.AbstractGraphicalEditPart.
 * 
 *******************************************************************************/
package org.eclipse.gef4.mvc.parts;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.gef4.mvc.IActivatable;
import org.eclipse.gef4.mvc.bindings.IAdaptable;
import org.eclipse.gef4.mvc.viewer.IVisualViewer;

/**
 * 
 * @author anyssen
 * 
 * @param <V>
 */
public abstract class AbstractVisualPart<V> implements IVisualPart<V> {

	/**
	 * This flag is set during {@link #activate()}, and reset on
	 * {@link #deactivate()}
	 */
	protected static final int FLAG_ACTIVE = 1;

	/**
	 * The left-most bit that is reserved by this class for setting flags.
	 * Subclasses may define additional flags starting at
	 * <code>(MAX_FLAG << 1)</code>.
	 */
	protected static final int MAX_FLAG = FLAG_ACTIVE;

	private int flags;

	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private Map<Class<?>, Object> partBounds;

	private IVisualPart<V> parent;
	private List<IVisualPart<V>> children;

	private List<IVisualPart<V>> anchoreds;
	private List<IVisualPart<V>> anchorages;

	private boolean refreshFromModel = true;

	/**
	 * Activates this {@link IVisualPart}, which in turn activates its policies
	 * and children. Subclasses should <em>extend</em> this method if they need
	 * to register listeners to the content. Activation indicates that the
	 * {@link IVisualPart} is realized in an {@link IVisualViewer}.
	 * <code>deactivate()</code> is the inverse, and is eventually called on all
	 * {@link IVisualPart}s.
	 * 
	 * @see #deactivate()
	 */
	public void activate() {
		setFlag(FLAG_ACTIVE, true);

		if (partBounds != null) {
			for (Object b : partBounds.values()) {
				if (b instanceof IActivatable) {
					((IActivatable) b).activate();
				}
			}
		}

		List<IVisualPart<V>> c = getChildren();
		for (int i = 0; i < c.size(); i++)
			c.get(i).activate();
	}

	@Override
	public void addChild(IVisualPart<V> child) {
		addChild(child, getChildren().size());
	}

	public void addChild(IVisualPart<V> child, int index) {
		Assert.isNotNull(child);
		addChildWithoutNotify(child, index);

		child.setParent(this);
		addChildVisual(child, index);

		child.refreshVisual();

		if (isActive())
			child.activate();
	}

	@Override
	public void addChildren(List<? extends IVisualPart<V>> children) {
		for (IVisualPart<V> child : children) {
			addChild(child);
		}
	}

	@Override
	public void removeChildren(List<? extends IVisualPart<V>> children) {
		for (IVisualPart<V> child : children) {
			removeChild(child);
		}
	}

	/**
	 * Performs the addition of the child's <i>visual</i> to this
	 * {@link IVisualPart}'s visual.
	 * 
	 * @param child
	 *            The {@link IVisualPart} being added
	 * @param index
	 *            The child's position
	 * @see #addChild(IVisualPart, int)
	 */
	// TODO: make concrete, passing over the visual container to the child (as
	// in case of anchoreds)
	protected abstract void addChildVisual(IVisualPart<V> child, int index);

	private void addChildWithoutNotify(IVisualPart<V> child, int index) {
		if (children == null)
			children = new ArrayList<IVisualPart<V>>(2);
		children.add(index, child);
	}

	public IRootPart<V> getRoot() {
		if (getParent() != null) {
			return getParent().getRoot();
		}
		if (getAnchorages().size() > 0) {
			return getAnchorages().get(0).getRoot();
		}
		return null;
	}

	/**
	 * Deactivates this {@link IVisualPart}, and in turn deactivates its
	 * policies and children. Subclasses should <em>extend</em> this method to
	 * remove any listeners established in {@link #activate()}
	 * 
	 * @see #activate()
	 */
	public void deactivate() {
		List<IVisualPart<V>> c = getChildren();
		for (int i = 0; i < c.size(); i++)
			c.get(i).deactivate();

		if (partBounds != null) {
			for (Object b : partBounds.values()) {
				if (b instanceof IActivatable) {
					((IActivatable) b).deactivate();
				}
			}
		}

		setFlag(FLAG_ACTIVE, false);
	}

	public List<IVisualPart<V>> getChildren() {
		if (children == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(children);
	}

	/**
	 * Returns the boolean value of the given flag. Specifically, returns
	 * <code>true</code> if the bitwise AND of the specified flag and the
	 * internal flags field is non-zero.
	 * 
	 * @param flag
	 *            Bitmask indicating which flag to return
	 * @return the requested flag's value
	 * @see #setFlag(int,boolean)
	 */
	protected final boolean getFlag(int flag) {
		return (flags & flag) != 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> key) {
		if (partBounds == null) {
			return null;
		}
		return (T) partBounds.get(key);
	}

	protected IVisualViewer<V> getViewer() {
		IRootPart<V> root = getRoot();
		if (root == null) {
			return null;
		}
		return root.getViewer();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void setAdapter(Class<T> key, T adapter) {
		if (partBounds == null) {
			partBounds = new HashMap<Class<?>, Object>();
		}
		partBounds.put(key, adapter);
		if(adapter instanceof IAdaptable.Bound){
			((IAdaptable.Bound<IVisualPart<V>>)adapter).setAdaptable(this);
		}
		if (isActive() && adapter instanceof IActivatable) {
			((IActivatable) adapter).activate();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> void setAdapter(T adapter) {
		setAdapter((Class<T>)adapter.getClass(), adapter);
		
	}

	/**
	 * @return <code>true</code> if this {@link IVisualPart} is active.
	 */
	@Override
	public boolean isActive() {
		return getFlag(FLAG_ACTIVE);
	}

	@Override
	public boolean isRefreshVisual() {
		return refreshFromModel;
	}

	@Override
	public void setRefreshVisual(boolean refreshFromModel) {
		this.refreshFromModel = refreshFromModel;
	}

	/**
	 * Refreshes this {@link IVisualPart}'s <i>visuals</i>. This method does
	 * nothing by default. Subclasses may override.
	 */
	public abstract void refreshVisual();

	public void removeChild(IVisualPart<V> child) {
		Assert.isNotNull(child);
		int index = getChildren().indexOf(child);
		if (index < 0)
			return;
		if (isActive())
			child.deactivate();

		child.setParent(null);
		removeChildVisual(child);
		removeChildWithoutNotify(child);
	}

	/**
	 * Removes the child's visual from this {@link IVisualPart}'s visual.
	 * 
	 * @param child
	 *            the child {@link IVisualPart}
	 */
	protected abstract void removeChildVisual(IVisualPart<V> child);

	private void removeChildWithoutNotify(IVisualPart<V> child) {
		children.remove(child);
		if (children.size() == 0) {
			children = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void unsetAdapter(Class<T> key) {
		if (partBounds == null)
			return;
		Object bounded = partBounds.remove(key);
		if (bounded != null) {
			if (bounded instanceof IActivatable) {
				((IActivatable) bounded).deactivate();
			}
			if(bounded instanceof IAdaptable.Bound){
				((IAdaptable.Bound<IVisualPart<V>>)bounded).setAdaptable(null);
			}
		}
		if (partBounds.size() == 0) {
			partBounds = null;
		}	
	}

	/**
	 * Moves a child {@link IVisualPart} into a lower index than it currently
	 * occupies.
	 * 
	 * @param child
	 *            the child {@link IVisualPart} being reordered
	 * @param index
	 *            new index for the child
	 */
	public void reorderChild(IVisualPart<V> child, int index) {
		removeChildVisual(child);
		removeChildWithoutNotify(child);
		addChildWithoutNotify(child, index);
		addChildVisual(child, index);
	}

	/**
	 * Sets the value of the specified flag. Flag values are declared as static
	 * constants. Subclasses may define additional constants above
	 * {@link #MAX_FLAG}.
	 * 
	 * @param flag
	 *            Flag being set
	 * @param value
	 *            Value of the flag to be set
	 * @see #getFlag(int)
	 */
	protected final void setFlag(int flag, boolean value) {
		if (value)
			flags |= flag;
		else
			flags &= ~flag;
	}

	protected void registerAtVisualPartMap() {
		getViewer().getVisualPartMap().put(getVisual(), this);
	}

	protected void unregisterFromVisualPartMap() {
		getViewer().getVisualPartMap().remove(getVisual());
	}

	/**
	 * Sets the parent {@link IVisualPart}.
	 */
	public void setParent(IVisualPart<V> parent) {
		if (this.parent == parent)
			return;

		IVisualPart<V> oldParent = this.parent;

		// unregister if we have no (remaining) link to the viewer
		if (this.parent != null) {
			if (parent == null && anchorages == null) {
				unregister();
			}
		}

		this.parent = parent;

		// if we obtain a link to the viewer (via parent) then register visuals
		if (this.parent != null && anchorages == null) {
			register();
		}

		pcs.firePropertyChange(PARENT_PROPERTY, oldParent, parent);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public IVisualPart<V> getParent() {
		return parent;
	}

	@Override
	public void addAnchored(IVisualPart<V> anchored) {
		if (anchoreds == null) {
			anchoreds = new ArrayList<IVisualPart<V>>();
		}
		anchoreds.add(anchored);

		anchored.addAnchorage(this);
		attachAnchoredVisual(anchored);

		anchored.refreshVisual();
	}

	@Override
	public void addAnchoreds(List<? extends IVisualPart<V>> anchoreds) {
		for (IVisualPart<V> anchored : anchoreds) {
			addAnchored(anchored);
		}
	}

	@Override
	public void removeAnchoreds(List<? extends IVisualPart<V>> anchoreds) {
		for (IVisualPart<V> anchored : anchoreds) {
			removeAnchored(anchored);
		}
	}

	protected void attachAnchoredVisual(IVisualPart<V> anchored) {
		anchored.attachVisualToAnchorageVisual(this, getVisual());
	}

	@Override
	public void removeAnchored(IVisualPart<V> anchored) {
		anchored.removeAnchorage(this);
		detachAnchoredVisual(anchored);

		anchoreds.remove(anchored);
		if (anchoreds.size() == 0) {
			anchoreds = null;
		}
	}

	protected void detachAnchoredVisual(IVisualPart<V> anchored) {
		anchored.detachVisualFromAnchorageVisual(this, getVisual());
	}

	@Override
	public List<IVisualPart<V>> getAnchoreds() {
		if (anchoreds == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(anchoreds);
	}

	@Override
	public void addAnchorage(IVisualPart<V> anchorage) {
		if (anchorage == null) {
			throw new IllegalArgumentException("Anchorage may not be null.");
		}

		if (anchorages == null) {
			anchorages = new ArrayList<IVisualPart<V>>();
		}
		anchorages.add(anchorage);

		// if we obtain a link to the viewer (via anchorage) then register
		// visuals
		if (parent == null) {
			if (anchorages.size() == 1) {
				register();
			}
		}
	}

	/**
	 * Called when a link to the Viewer is obtained.
	 */
	protected void register() {
		registerAtVisualPartMap();
	}

	// counterpart to setParent(null) in case of hierarchy
	@Override
	public void removeAnchorage(IVisualPart<V> anchorage) {
		if (anchorage == null) {
			throw new IllegalArgumentException("Anchorage may not be null.");
		}
		if (anchorages == null || !anchorages.contains(anchorage)) {
			throw new IllegalArgumentException("Anchorage has to be contained.");
		}

		if (parent == null) {
			if (anchorages.size() == 1) {
				unregister();
			}
		}
		anchorages.remove(anchorage);
		if (anchorages.size() == 0) {
			anchorages = null;
		}
	}

	/**
	 * Called when the link to the Viewer is lost.
	 */
	protected void unregister() {
		unregisterFromVisualPartMap();
	}

	@Override
	public List<IVisualPart<V>> getAnchorages() {
		if (anchorages == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(anchorages);
	}

}
