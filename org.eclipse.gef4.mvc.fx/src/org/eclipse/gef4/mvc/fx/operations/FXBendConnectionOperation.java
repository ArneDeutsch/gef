/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.fx.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef4.fx.anchors.DynamicAnchor;
import org.eclipse.gef4.fx.anchors.IAnchor;
import org.eclipse.gef4.fx.nodes.Connection;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.mvc.operations.ITransactionalOperation;

/**
 * An {@link FXBendConnectionOperation} can be used to manipulate an
 * {@link Connection} in an undo-context.
 *
 * @author mwienand
 *
 */
public class FXBendConnectionOperation extends AbstractOperation
		implements ITransactionalOperation {

	private final Connection connection;
	private final List<IAnchor> initialAnchors;
	private final List<IAnchor> newAnchors;

	/**
	 * Constructs a new operation from the given connection. The lists of old
	 * and new {@link IAnchor}s are initialized based on the connection.
	 *
	 * @param connection
	 *            The {@link Connection} which will be modified by this
	 *            operation.
	 */
	public FXBendConnectionOperation(Connection connection) {
		super("Bend");
		this.connection = connection;
		this.initialAnchors = new ArrayList<>(
				onlyExplicit(connection.getAnchors()));
		this.newAnchors = new ArrayList<>(initialAnchors);
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (connection != null) {
			// update anchors (if needed)
			if (!onlyExplicit(connection.getAnchors()).equals(newAnchors)) {
				connection.setAnchors(newAnchors);
			}
			// pass positions for connected anchors to the connection router
			hintAnchorPositions();
			// route so that the hints are applied
			getConnection().getRouter().route(getConnection());
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns the {@link Connection} which is manipulated by this operation.
	 *
	 * @return The {@link Connection} which is manipulated by this operation.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Returns the index within the Connection's anchors for the given explicit
	 * anchor index.
	 *
	 * @param explicitAnchorIndex
	 *            The explicit anchor index for which to return the connection
	 *            index.
	 * @return The connection's anchor index for the given explicit anchor
	 *         index.
	 */
	public int getConnectionIndex(int explicitAnchorIndex) {
		int explicitCount = -1;

		for (int i = 0; i < getConnection().getPoints().size(); i++) {
			IAnchor a = getConnection().getAnchor(i);
			if (!getConnection().getRouter().isImplicitAnchor(a)) {
				explicitCount++;
			}
			if (explicitCount == explicitAnchorIndex) {
				// found all operation indices
				return i;
			}
		}

		throw new IllegalArgumentException(
				"Cannot determine connection index for operation index "
						+ explicitAnchorIndex + ".");
	}

	/**
	 * Returns the list of {@link IAnchor}s which will replace the connection's
	 * anchors upon undoing.
	 *
	 * @return The list of {@link IAnchor}s which will replace the connection's
	 *         anchors upon undoing.
	 */
	public List<IAnchor> getInitialAnchors() {
		return initialAnchors;
	}

	/**
	 * Returns the list of {@link IAnchor}s which will replace the connection's
	 * anchors upon execution.
	 *
	 * @return The list of {@link IAnchor}s which will replace the connection's
	 *         anchors upon execution.
	 */
	public List<IAnchor> getNewAnchors() {
		return newAnchors;
	}

	/**
	 * Provides the {@link Connection}'s connected start and end
	 * {@link DynamicAnchor}s with a position hint.
	 */
	protected void hintAnchorPositions() {
		List<IAnchor> anchors = getConnection().getAnchors();
		List<IAnchor> explicitAnchors = onlyExplicit(anchors);
		if (connection.getStartAnchor() instanceof DynamicAnchor
				&& explicitAnchors.get(0) == connection.getStartAnchor()) {
			for (int i = 1; i < anchors.size(); i++) {
				if (!getConnection().getRouter()
						.isImplicitAnchor(anchors.get(i))) {
					Point referencePoint = connection.getPoint(i);
					getConnection().getRouter().positionHintsProperty().put(
							getConnection().getStartAnchorKey(),
							referencePoint);
					System.out.println("Set reference point for "
							+ getConnection().getStartAnchorKey() + " to "
							+ referencePoint);
					break;
				}
			}
		}
		if (connection.getEndAnchor() instanceof DynamicAnchor
				&& explicitAnchors.get(explicitAnchors.size() - 1) == connection
						.getEndAnchor()) {
			for (int i = anchors.size() - 2; i >= 0; i--) {
				if (!getConnection().getRouter()
						.isImplicitAnchor(anchors.get(i))) {
					Point referencePoint = connection
							.getPoint(explicitAnchors.size() - 1 - 1);
					getConnection().getRouter().positionHintsProperty().put(
							getConnection().getEndAnchorKey(), referencePoint);
					System.out.println("Set reference point for "
							+ getConnection().getEndAnchorKey() + " to "
							+ referencePoint);
					break;
				}
			}
		}
	}

	@Override
	public boolean isContentRelevant() {
		return true;
	}

	@Override
	public boolean isNoOp() {
		return initialAnchors.equals(newAnchors);
	}

	private List<IAnchor> onlyExplicit(List<IAnchor> anchors) {
		ArrayList<IAnchor> explicit = new ArrayList<>(anchors);
		Iterator<IAnchor> it = explicit.iterator();
		while (it.hasNext()) {
			IAnchor anchor = it.next();
			if (connection.getRouter().isImplicitAnchor(anchor)) {
				it.remove();
			}
		}
		return explicit;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	/**
	 * Sets the list of {@link IAnchor}s which will replace the connection's
	 * anchors upon execution.
	 *
	 * @param newAnchors
	 *            The list of {@link IAnchor}s which will replace the
	 *            connection's anchors upon execution.
	 */
	public void setNewAnchors(List<IAnchor> newAnchors) {
		this.newAnchors.clear();
		this.newAnchors.addAll(onlyExplicit(newAnchors));
	}

	@Override
	public String toString() {
		return "FXBendConnectionOperation";
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (connection != null) {
			// check if we have to update anchors here
			if (!onlyExplicit(connection.getAnchors()).equals(initialAnchors)) {
				connection.setAnchors(initialAnchors);
			}
			// pass positions for connected anchors to the connection router
			hintAnchorPositions();
			// route so that the hints are applied
			getConnection().getRouter().route(getConnection());
		}
		return Status.OK_STATUS;
	}

}