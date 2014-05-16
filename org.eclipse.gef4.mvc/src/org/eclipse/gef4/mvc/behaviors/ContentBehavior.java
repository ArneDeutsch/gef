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
 * Note: Parts of this class have been transferred from org.eclipse.gef.editparts.AbstractEditPart.
 *     
 *******************************************************************************/
package org.eclipse.gef4.mvc.behaviors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef4.mvc.models.IContentModel;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IContentPartFactory;
import org.eclipse.gef4.mvc.parts.IVisualPart;
import org.eclipse.gef4.mvc.parts.PartUtils;

public class ContentBehavior<V> extends AbstractBehavior<V> implements
		PropertyChangeListener {

	@Override
	public void activate() {
		super.activate();
		if (getHost() == getHost().getRoot()) {
			getHost().getRoot().getViewer().getContentModel()
					.addPropertyChangeListener(this);
			;
		} else {
			getHost().addPropertyChangeListener(this);
		}
	}

	@Override
	public void deactivate() {
		if (getHost() == getHost().getRoot()) {
			getHost().getRoot().getViewer().getContentModel()
					.removePropertyChangeListener(this);
			;
		} else {
			getHost().removePropertyChangeListener(this);
		}
		super.deactivate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (IContentModel.CONTENTS_PROPERTY.equals(event.getPropertyName())) {
			synchronizeContentChildren((List<Object>) event.getNewValue());
		} else if (IContentPart.CONTENT_PROPERTY
				.equals(event.getPropertyName())) {
			synchronizeContentChildren(((IContentPart<V>) getHost())
					.getContentChildren());
			synchronizeContentAnchored(((IContentPart<V>) getHost())
					.getContentAnchored());
		}
	}

	/**
	 * Updates the host {@link IVisualPart}'s children {@link IContentPart}s (see
	 * {@link IVisualPart#getChildren()}) so that it is in sync with the set of
	 * content children that is passed in.
	 */
	@SuppressWarnings("unchecked")
	public void synchronizeContentChildren(List<Object> contentChildren) {
		// only synchronize ContentPart children

		int i;
		IContentPart<V> editPart;
		Object model;

		List<IContentPart<V>> contentPartChildren = PartUtils.filterParts(
				getHost().getChildren(), IContentPart.class);
		int size = contentPartChildren.size();
		Map<Object, IContentPart<V>> modelToEditPart = Collections.emptyMap();
		if (size > 0) {
			modelToEditPart = new HashMap<Object, IContentPart<V>>(size);
			for (i = 0; i < size; i++) {
				editPart = (IContentPart<V>) contentPartChildren.get(i);
				modelToEditPart.put(editPart.getContent(), editPart);
			}
		}

		List<Object> modelObjects = contentChildren;
		for (i = 0; i < modelObjects.size(); i++) {
			model = modelObjects.get(i);

			// Do a quick check to see if editPart[i] == model[i]
			if (i < size && contentPartChildren.get(i).getContent() == model)
				continue;

			// Look to see if the EditPart is already around but in the
			// wrong location
			editPart = (IContentPart<V>) modelToEditPart.get(model);

			if (editPart != null) {
				// TODO: this is wrong, it has to take into consideration the
				// visual parts in between
				getHost().reorderChild(editPart, i);
			} else {
				// An EditPart for this model doesn't exist yet. Create and
				// insert one.
				editPart = findOrCreatePartFor(model);
				getHost().addChild(editPart, i);
			}
		}

		// remove the remaining EditParts
		contentPartChildren = PartUtils.filterParts(getHost().getChildren(),
				IContentPart.class);
		size = contentPartChildren.size();
		if (i < size) {
			List<IContentPart<V>> trash = new ArrayList<IContentPart<V>>(size
					- i);
			for (; i < size; i++)
				trash.add(contentPartChildren.get(i));
			for (i = 0; i < trash.size(); i++) {
				IContentPart<V> ep = trash.get(i);
				getHost().removeChild(ep);
				disposeIfObsolete(ep);
			}
		}
	}

	protected IContentPart<V> findOrCreatePartFor(Object model) {
		Map<Object, IContentPart<V>> contentPartMap = getHost().getRoot()
				.getViewer().getContentPartMap();
		if (contentPartMap.containsKey(model)) {
			return contentPartMap.get(model);
		} else {
			IContentPartFactory<V> contentPartFactory = getHost().getRoot()
					.getViewer().getContentPartFactory();
			IContentPart<V> contentPart = contentPartFactory.createContentPart(
					model, this, Collections.emptyMap());
			contentPart.setContent(model);
			return contentPart;
		}
	}

	/**
	 * Updates the host {@link IVisualPart}'s anchored {@link IContentPart}s
	 * (see {@link IVisualPart#getAnchoreds()}) so that it is in sync with the
	 * set of content anchored that is passed in.
	 */
	@SuppressWarnings("unchecked")
	public void synchronizeContentAnchored(List<Object> contentAnchored) {
		int i;
		IContentPart<V> editPart;
		Object model;

		List<IContentPart<V>> anchored = PartUtils.filterParts(getHost()
				.getAnchoreds(), IContentPart.class);
		int size = anchored.size();
		Map<Object, IContentPart<V>> modelToEditPart = Collections.emptyMap();
		if (size > 0) {
			modelToEditPart = new HashMap<Object, IContentPart<V>>(size);
			for (i = 0; i < size; i++) {
				editPart = (IContentPart<V>) anchored.get(i);
				modelToEditPart.put(editPart.getContent(), editPart);
			}
		}

		for (i = 0; i < contentAnchored.size(); i++) {
			model = contentAnchored.get(i);

			// Do a quick check to see if editPart[i] == model[i]
			if (i < anchored.size() && anchored.get(i).getContent() == model)
				continue;

			// Look to see if the EditPart is already around but in the
			// wrong location
			editPart = (IContentPart<V>) modelToEditPart.get(model);

			if (editPart != null) {
				// TODO: this is wrong, it has to take into consideration the
				// visual parts in between
				// reorderChild(editPart, i);
			} else {
				// An EditPart for this model doesn't exist yet. Create and
				// insert one.
				editPart = findOrCreatePartFor(model);
				// what if it does not exist??
				getHost().addAnchored(editPart);
			}
		}

		// remove the remaining EditParts
		anchored = PartUtils.filterParts(getHost().getAnchoreds(),
				IContentPart.class);
		size = anchored.size();
		if (i < size) {
			List<IContentPart<V>> trash = new ArrayList<IContentPart<V>>(size
					- i);
			for (; i < size; i++)
				trash.add(anchored.get(i));
			for (i = 0; i < trash.size(); i++) {
				IContentPart<V> ep = trash.get(i);
				getHost().removeAnchored(ep);
				disposeIfObsolete(ep);
			}
		}
	}

	protected void disposeIfObsolete(IContentPart<V> contentPart) {
		if (contentPart.getParent() == null
				&& contentPart.getAnchorages().isEmpty()) {
			contentPart.setContent(null);
		}
	}

}
