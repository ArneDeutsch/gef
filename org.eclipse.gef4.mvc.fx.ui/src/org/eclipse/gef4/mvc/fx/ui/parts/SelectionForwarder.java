/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.ui.parts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.viewer.IViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class SelectionForwarder<VR>
		implements PropertyChangeListener, ISelectionChangedListener {

	private final ISelectionProvider selectionProvider;
	private final IViewer<VR> viewer;
	private final SelectionModel<VR> selectionModel;

	public SelectionForwarder(final ISelectionProvider selectionProvider,
			IViewer<VR> viewer) {
		if (viewer == null) {
			throw new IllegalArgumentException(
					"The given IViewer<VR> may not be null.");
		}
		this.selectionProvider = selectionProvider;
		this.viewer = viewer;
		this.selectionModel = viewer
				.<SelectionModel<VR>> getAdapter(SelectionModel.class);

		// register listeners
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(this);
		}
		if (selectionModel != null) {
			selectionModel.addPropertyChangeListener(this);
		}
	}

	public void dispose() {
		// unregister listeners
		if (this.selectionProvider != null) {
			this.selectionProvider.removeSelectionChangedListener(this);
		}
		if (selectionModel != null) {
			selectionModel.removePropertyChangeListener(this);
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (SelectionModel.SELECTION_PROPERTY.equals(event.getPropertyName())) {
			// forward selection changes to selection provider (in case
			// there is any)
			if (event.getNewValue() == null) {
				selectionProvider.setSelection(StructuredSelection.EMPTY);
			} else {
				// extract content elements of selected parts
				@SuppressWarnings("unchecked")
				List<IContentPart<VR, ? extends VR>> selectedParts = (List<IContentPart<VR, ? extends VR>>) event
						.getNewValue();
				List<Object> selectedContentElements = new ArrayList<Object>(
						selectedParts.size());
				for (IContentPart<VR, ? extends VR> cp : selectedParts) {
					selectedContentElements.add(cp.getContent());
				}
				// set the content elements as the new selection on the
				// selection provider
				// TODO: verify no events are fired when the same selection is
				// set again
				selectionProvider.setSelection(
						new StructuredSelection(selectedContentElements));
			}
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			if (sel.isEmpty()) {
				selectionModel.select(Collections
						.<IContentPart<VR, ? extends VR>> emptyList());
			} else {
				// find the content parts associated with the selection
				Object[] selected = sel.toArray();
				List<IContentPart<VR, ? extends VR>> parts = new ArrayList<IContentPart<VR, ? extends VR>>(
						selected.length);
				for (Object content : selected) {
					IContentPart<VR, ? extends VR> part = viewer
							.getContentPartMap().get(content);
					if (part != null) {
						parts.add(part);
					}
				}
				// set the content parts as the new selection on the
				// SelectionModel
				if (!selectionModel.getSelected().equals(parts)) {
					selectionModel.select(parts);
				}
			}
		}
	}

}
