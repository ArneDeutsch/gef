package org.eclipse.gef4.zest.examples.layouts;

import org.eclipse.gef4.layout.LayoutAlgorithm;
import org.eclipse.gef4.layout.PropertiesHelper;
import org.eclipse.gef4.layout.interfaces.EntityLayout;
import org.eclipse.gef4.layout.interfaces.LayoutContext;
import org.eclipse.gef4.zest.core.widgets.GraphConnection;
import org.eclipse.gef4.zest.core.widgets.GraphNode;
import org.eclipse.gef4.zest.core.widgets.GraphWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This snippet shows how to create a custom layout. This layout simply lays the
 * nodes out vertically on the same Y-Axis as they currently have. All the work
 * is done in the applyLayoutInternal Method.
 * 
 * @author irbull
 * 
 */
public class CustomLayout {

	public static void main(String[] args) {
		Display d = new Display();
		Shell shell = new Shell(d);
		shell.setText("Custom Layout Example");
		shell.setLayout(new FillLayout());
		shell.setSize(400, 400);

		GraphWidget g = new GraphWidget(shell, SWT.NONE);

		GraphNode n = new GraphNode(g, SWT.NONE, "Paper");
		GraphNode n2 = new GraphNode(g, SWT.NONE, "Rock");
		GraphNode n3 = new GraphNode(g, SWT.NONE, "Scissors");
		new GraphConnection(g, SWT.NONE, n, n2);
		new GraphConnection(g, SWT.NONE, n2, n3);
		new GraphConnection(g, SWT.NONE, n3, n);

		LayoutAlgorithm layoutAlgorithm = new LayoutAlgorithm() {
			private LayoutContext context;

			public void setLayoutContext(LayoutContext context) {
				this.context = context;
			}

			public LayoutContext getLayoutContext() {
				return context;
			}

			public void applyLayout(boolean clean) {
				EntityLayout[] entitiesToLayout = context.getEntities();
				int totalSteps = entitiesToLayout.length;
				double distance = context.getBounds().getWidth() / totalSteps;
				int xLocation = 0;

				for (int currentStep = 0; currentStep < entitiesToLayout.length; currentStep++) {
					EntityLayout layoutEntity = entitiesToLayout[currentStep];
					PropertiesHelper.setLocation(layoutEntity, xLocation,
							PropertiesHelper.getLocation(layoutEntity).y);
					xLocation += distance;
				}
			}
		};
		g.setLayoutAlgorithm(layoutAlgorithm, true);

		shell.open();
		while (!shell.isDisposed()) {
			while (!d.readAndDispatch()) {
				d.sleep();
			}
		}
	}
}
