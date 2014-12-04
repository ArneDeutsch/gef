/*******************************************************************************
 * Copyright (c) 2009-2010 Mateusz Matela and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Mateusz Matela - initial API and implementation
 *               Ian Bull
 ******************************************************************************/
package org.eclipse.gef4.zest.core.widgets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef4.layout.LayoutPropertiesHelper;
import org.eclipse.gef4.layout.interfaces.LayoutContext;
import org.eclipse.gef4.layout.interfaces.NodeLayout;
import org.eclipse.gef4.zest.core.widgets.internal.GraphLabel;
import org.eclipse.gef4.zest.core.widgets.internal.ZestRootLayer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * A subgraph that for each unexpanded node in a graph adds a label showing
 * number of pruned successors (as unexpanded node is considered a node for
 * which {@link GraphWidget#canExpand(GraphNode)} returns true AND
 * {@link GraphWidget#canCollapse(GraphNode)} returns false). It doesn't matter
 * which subgraph a node is pruned to, so the factory for this subgraph uses one
 * instance for whole layout context.
 */
class PrunedSuccessorsSubgraph extends DefaultSubgraph {

	private class LabelAncestorListener extends AncestorListener.Stub {
		private final IFigure originalFigure;
		private IFigure fisheyeFigure;

		public LabelAncestorListener(IFigure originalFigure,
				IFigure fisheyeFigure) {
			this.originalFigure = originalFigure;
			this.fisheyeFigure = fisheyeFigure;
		}

		public void ancestorRemoved(IFigure ancestor) {
			if (fisheyeFigure != null) {
				final GraphLabel label = nodeFigureToLabel.get(fisheyeFigure);
				if (label == null) {
					return;
				}
				nodeFigureToLabel.remove(fisheyeFigure);
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						label.removeAncestorListener(LabelAncestorListener.this);
					}
				});
				fisheyeFigure.removeFigureListener(nodeFigureListener);
				originalFigure.addFigureListener(nodeFigureListener);
				labelToAncestorListener.remove(label);
				fisheyeFigure = null;
				addLabelForFigure(originalFigure, label);
				refreshLabelBounds(originalFigure, label);
			}
		}
	}

	private final FigureListener nodeFigureListener = new FigureListener() {
		public void figureMoved(IFigure source) {
			GraphLabel label = nodeFigureToLabel.get(source);
			if (label != null) {
				refreshLabelBounds(source, label);
			}
		}
	};

	private final FisheyeListener fisheyeListener = new FisheyeListener() {

		public void fisheyeReplaced(GraphWidget graph,
				IFigure oldFisheyeFigure, IFigure newFisheyeFigure) {
			oldFisheyeFigure.removeFigureListener(nodeFigureListener);
			newFisheyeFigure.addFigureListener(nodeFigureListener);
			GraphLabel label = nodeFigureToLabel.remove(oldFisheyeFigure);
			nodeFigureToLabel.put(newFisheyeFigure, label);

			LabelAncestorListener ancestorListener = labelToAncestorListener
					.get(label);
			ancestorListener.fisheyeFigure = null;
			addLabelForFigure(newFisheyeFigure, label);
			ancestorListener.fisheyeFigure = newFisheyeFigure;
			refreshLabelBounds(newFisheyeFigure, label);
		}

		public void fisheyeRemoved(GraphWidget graph, IFigure originalFigure,
				IFigure fisheyeFigure) {
			// do nothing - labelAncestorListener will take care of cleaning
			// up
		}

		public void fisheyeAdded(GraphWidget graph, IFigure originalFigure,
				IFigure fisheyeFigure) {
			originalFigure.removeFigureListener(nodeFigureListener);
			fisheyeFigure.addFigureListener(nodeFigureListener);
			GraphLabel label = nodeFigureToLabel.get(originalFigure);
			if (label == null) {
				return;
			}
			nodeFigureToLabel.put(fisheyeFigure, label);
			refreshLabelBounds(fisheyeFigure, label);
			addLabelForFigure(fisheyeFigure, label);
			LabelAncestorListener labelAncestorListener = new LabelAncestorListener(
					originalFigure, fisheyeFigure);
			label.addAncestorListener(labelAncestorListener);
			labelToAncestorListener.put(label, labelAncestorListener);
		}
	};

	/**
	 * Maps from figures of nodes to labels showing number of nodes hidden
	 * successors
	 */
	private HashMap<IFigure, GraphLabel> nodeFigureToLabel = new HashMap<IFigure, GraphLabel>();

	private HashMap<GraphLabel, LabelAncestorListener> labelToAncestorListener = new HashMap<GraphLabel, LabelAncestorListener>();

	protected PrunedSuccessorsSubgraph(LayoutContext context2) {
		super(context2);
		context.container.getGraph().addFisheyeListener(fisheyeListener);
	}

	public void addNodes(NodeLayout[] nodes) {
		super.addNodes(nodes);
		HashSet<NodeLayout> nodesToUpdate = new HashSet<NodeLayout>();
		for (int i = 0; i < nodes.length; i++) {
			nodesToUpdate
					.addAll(Arrays.asList(nodes[i].getPredecessingNodes()));
		}
		for (Iterator<NodeLayout> iterator = nodesToUpdate.iterator(); iterator
				.hasNext();) {
			InternalNodeLayout nodeToUpdate = (InternalNodeLayout) iterator
					.next();
			updateNodeLabel(nodeToUpdate);
		}

	}

	public void removeNodes(NodeLayout[] nodes) {
		super.removeNodes(nodes);
		HashSet<NodeLayout> nodesToUpdate = new HashSet<NodeLayout>();
		for (int i = 0; i < nodes.length; i++) {
			nodesToUpdate
					.addAll(Arrays.asList(nodes[i].getPredecessingNodes()));
			if (((InternalNodeLayout) nodes[i]).isDisposed()) {
				removeFigureForNode((InternalNodeLayout) nodes[i]);
			} else {
				nodesToUpdate.add(nodes[i]);
			}
		}
		for (Iterator<NodeLayout> iterator = nodesToUpdate.iterator(); iterator
				.hasNext();) {
			InternalNodeLayout predecessor = (InternalNodeLayout) iterator
					.next();
			updateNodeLabel(predecessor);
		}
	}

	private void addLabelForFigure(IFigure figure, GraphLabel label) {
		IFigure parent = figure.getParent();
		if (parent instanceof ZestRootLayer) {
			((ZestRootLayer) parent).addDecoration(figure, label);
		} else {
			if (parent.getChildren().contains(label)) {
				parent.remove(label);
			}
			int index = parent.getChildren().indexOf(figure);
			parent.add(label, index + 1);
		}
	}

	private void refreshLabelBounds(IFigure figure, GraphLabel label) {
		Rectangle figureBounds = figure.getBounds();
		if (figureBounds.width * figureBounds.height > 0) {
			label.setText(label.getText()); // hack: resets label's size
			Dimension labelSize = label.getSize();
			labelSize.expand(-6, -4);
			Point anchorPoint = figure.getBounds().getBottomRight();
			anchorPoint.x -= labelSize.width / 2;
			anchorPoint.y -= labelSize.height / 2;
			Rectangle bounds = new Rectangle(anchorPoint, labelSize);
			label.setBounds(bounds);
			label.getParent().setConstraint(label, bounds);
		} else {
			label.getParent().setConstraint(label,
					new Rectangle(figureBounds.x, figureBounds.y, 0, 0));
			label.setBounds(new Rectangle(figureBounds.x, figureBounds.y, 0, 0));
		}
	}

	void updateNodeLabel(InternalNodeLayout internalNode) {
		if (internalNode.isDisposed()) {
			return;
		}
		IFigure figure = internalNode.getNode().getFigure();
		GraphLabel label = nodeFigureToLabel.get(figure);
		IFigure fisheye = getFisheyeFigure(figure);
		if (fisheye != null) {
			figure = fisheye;
		}
		if (label == null) {
			label = new GraphLabel(false);
			label.setForegroundColor(ColorConstants.white);
			label.setBackgroundColor(ColorConstants.red);
			FontData fontData = Display.getDefault().getSystemFont()
					.getFontData()[0];
			fontData.setHeight(6);
			label.setFont(new Font(Display.getCurrent(), fontData));
			figure.addFigureListener(nodeFigureListener);
			addLabelForFigure(figure, label);
			nodeFigureToLabel.put(figure, label);
		}

		GraphNode graphNode = internalNode.getNode();
		if (!graphNode.getGraphWidget().canExpand(graphNode)
				|| graphNode.getGraphWidget().canCollapse(graphNode)
				|| internalNode.isPruned()) {
			label.setVisible(false);
		} else {
			NodeLayout[] successors = internalNode.getSuccessingNodes();
			int numberOfHiddenSuccessors = 0;
			for (int i = 0; i < successors.length; i++) {
				if (LayoutPropertiesHelper.isPruned(successors[i])) {
					numberOfHiddenSuccessors++;
				}
			}
			String labelText = numberOfHiddenSuccessors > 0 ? ""
					+ numberOfHiddenSuccessors : "";
			if (!labelText.equals(label.getText())) {
				label.setText(labelText);
			}
			label.setVisible(true);
		}

		refreshLabelBounds(figure, label);
	}

	private IFigure getFisheyeFigure(IFigure originalFigure) {
		// a node has a fisheye if and only if its label has an AncestorListener
		GraphLabel label = nodeFigureToLabel.get(originalFigure);
		LabelAncestorListener ancestorListener = labelToAncestorListener
				.get(label);
		if (ancestorListener != null) {
			return ancestorListener.fisheyeFigure;
		}
		return null;
	}

	private void removeFigureForNode(InternalNodeLayout internalNode) {
		IFigure figure = internalNode.getNode().getFigure();
		GraphLabel label = nodeFigureToLabel.get(figure);
		if (label != null && label.getParent() != null) {
			label.getParent().remove(label);
		}
		nodeFigureToLabel.remove(figure);
	}
}
