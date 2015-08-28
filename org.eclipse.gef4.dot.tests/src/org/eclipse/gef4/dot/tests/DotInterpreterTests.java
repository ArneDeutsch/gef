/*******************************************************************************
 * Copyright (c) 2009, 2015 Fabian Steeg and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg - initial API and implementation (see bug #277380)
 *******************************************************************************/
package org.eclipse.gef4.dot.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef4.dot.DotImport;
import org.eclipse.gef4.dot.DotProperties;
import org.eclipse.gef4.graph.Edge;
import org.eclipse.gef4.graph.Graph;
import org.eclipse.gef4.graph.Node;
import org.eclipse.gef4.internal.dot.DotAst;
import org.eclipse.gef4.internal.dot.DotInterpreter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for dynamic import of DOT to a Zest graph instance.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class DotInterpreterTests {

	private final DotInterpreter interpreter = new DotInterpreter();

	@Test
	public void digraphType() {
		Graph graph = interpreter.interpret(parse("digraph Sample{1;2;1->2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_TYPE_DIRECTED,
				DotProperties.getType(graph));
	}

	@Test
	public void graphType() {
		Graph graph = interpreter.interpret(parse("graph Sample{1;2;1--2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_TYPE_UNDIRECTED,
				DotProperties.getType(graph));
	}

	@Test
	public void nodeDefaultLabel() {
		Graph graph = interpreter.interpret(parse("graph Sample{1}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("1", //$NON-NLS-1$
				DotProperties.getId(graph.getNodes().get(0)));
	}

	@Test
	public void nodeCount() {
		Graph graph = interpreter.interpret(parse("graph Sample{1;2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
	}

	@Test
	public void edgeCount() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{1;2;1->2;2->2;1->1}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(3, graph.getEdges().size());
	}

	@Test
	public void nodeLabel() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{1[label=\"Node1\"];}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("Node1", //$NON-NLS-1$
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void edgeLabel() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{1;2;1->2[label=\"Edge1\"]}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("Edge1", DotProperties.getLabel(graph.getEdges() //$NON-NLS-1$
				.get(0)));
	}

	@Test
	public void edgeStyle() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{1;2;1->2[style=dashed]}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.EDGE_STYLE_DASHED,
				DotProperties.getStyle(graph.getEdges().get(0)));
	}

	@Test
	public void globalEdgeStyle() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{edge[style=dashed];1;2;1->2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.EDGE_STYLE_DASHED,
				DotProperties.getStyle(graph.getEdges().get(0)));
	}

	@Test
	public void globalEdgeLabel() {
		Graph graph = interpreter.interpret(
				parse("graph Sample{edge[label=\"Edge1\"];1;2;1->2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("Edge1", DotProperties.getLabel(graph.getEdges() //$NON-NLS-1$
				.get(0)));
	}

	@Test
	public void globalNodeLabel() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{node[label=\"Node1\"];1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("Node1", //$NON-NLS-1$
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void layoutSpring() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{graph[layout=fdp];1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_FDP,
				DotProperties.getLayout(graph));
	}

	@Test
	public void layoutGrid() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{graph[layout=osage];1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_OSAGE,
				DotProperties.getLayout(graph));
	}

	@Test
	public void layoutRadial() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{graph[layout=twopi];1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_TWOPI,
				DotProperties.getLayout(graph));
	}

	@Test
	public void layoutTree() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{graph[layout=dot];1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_DOT,
				DotProperties.getLayout(graph));
	}

	@Test
	public void layoutHorizontalTreeViaLayout() {
		Graph graph = interpreter.interpret(
				parse("graph Sample{graph[layout=dot];rankdir=LR;1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_DOT,
				DotProperties.getLayout(graph));
		Assert.assertEquals(DotProperties.GRAPH_RANKDIR_LR,
				DotProperties.getRankdir(graph));
	}

	@Test
	public void layoutHorizontalTreeViaAttribute() {
		Graph graph = interpreter
				.interpret(parse("graph Sample{rankdir=LR;1;}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(DotProperties.GRAPH_LAYOUT_DOT,
				DotProperties.getLayout(graph));
		Assert.assertEquals(DotProperties.GRAPH_RANKDIR_LR,
				DotProperties.getRankdir(graph));
	}

	@Test
	public void globalNodeAttributeAdHocNodes() {
		Graph graph = interpreter
				.interpret(parse("graph{node[label=\"TEXT\"];1--2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("TEXT", //$NON-NLS-1$
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void globalEdgeAttributeAdHocNodes() {
		Graph graph = interpreter
				.interpret(parse("graph{edge[label=\"TEXT\"];1--2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("TEXT", DotProperties.getLabel(graph.getEdges() //$NON-NLS-1$
				.get(0)));
	}

	@Test
	public void headerCommentGraph() {
		Graph graph = interpreter
				.interpret(parse("/*A header comment*/\ngraph{1--2}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
		Assert.assertEquals(1, graph.getEdges().size());
	}

	@Test
	public void nodesBeforeEdges() {
		Graph graph = interpreter
				.interpret(parse("graph{1;2;3;4; 1->2;2->3;2->4}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(4, graph.getNodes().size());
		Assert.assertEquals(3, graph.getEdges().size());
	}

	@Test
	public void nodesAfterEdges() {
		Graph graph = interpreter.interpret(
				parse("graph{1->2;2->3;2->4;1[label=\"node\"];2;3;4}")); //$NON-NLS-1$
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(4, graph.getNodes().size());
		Assert.assertEquals(3, graph.getEdges().size());
		Assert.assertEquals("node",
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void useInterpreterTwice() {
		String dot = "graph{1;2;3;4; 1->2;2->3;2->4}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(4, graph.getNodes().size());
		Assert.assertEquals(3, graph.getEdges().size());
	}

	@Test
	public void idsWithQuotes() {
		String dot = "graph{\"node 1\";\"node 2\"}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		List<Node> list = graph.getNodes();
		Assert.assertEquals("node 1", //$NON-NLS-1$
				DotProperties.getId(list.get(0)));
		Assert.assertEquals("node 2", //$NON-NLS-1$
				DotProperties.getId(list.get(1)));
	}

	@Test
	public void escapedQuotes() {
		String dot = "graph{n1[label=\"node \\\"1\\\"\"]}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("node \"1\"", //$NON-NLS-1$
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void fullyQuoted() {
		String dot = "graph{\"n1\";\"n2\";\"n1\"->\"n2\"}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals(2, graph.getNodes().size());
		Assert.assertEquals(1, graph.getEdges().size());
		List<Node> list = graph.getNodes();
		Assert.assertEquals("n1", //$NON-NLS-1$
				DotProperties.getId(list.get(0)));
		Assert.assertEquals("n2", //$NON-NLS-1$
				DotProperties.getId(list.get(1)));
	}

	@Test
	public void labelsWithQuotes() {
		String dot = "graph{n1[label=\"node 1\"];n2[label=\"node 2\"];n1--n2[label=\"edge 1\"]}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		List<Node> list = graph.getNodes();
		Assert.assertEquals("node 1", //$NON-NLS-1$
				DotProperties.getLabel(list.get(0)));
		Assert.assertEquals("node 2", //$NON-NLS-1$
				DotProperties.getLabel(list.get(1)));
		Assert.assertEquals("edge 1",
				DotProperties.getLabel(graph.getEdges().get(0)));
	}

	@Test
	public void newLinesInLabels() {
		String dot = "graph{n1[label=\"node\n1\"]}"; //$NON-NLS-1$
		Graph graph = interpreter.interpret(parse(dot));
		Assert.assertNotNull("Created graph must not be null", graph); //$NON-NLS-1$
		Assert.assertEquals("node\n1", //$NON-NLS-1$
				DotProperties.getLabel(graph.getNodes().get(0)));
	}

	@Test
	public void multiEdgeStatements() {
		Graph graph = new DotImport("digraph{1->2->3->4}").newGraphInstance(); //$NON-NLS-1$
		assertEquals(4, graph.getNodes().size());
		assertEquals(3, graph.getEdges().size());
		/* Each node should be connected to one other, the previous node: */
		List<Node> list = graph.getNodes();
		assertEquals(1, getSourceConnections(list.get(1), graph).size());
		assertEquals(1, getSourceConnections(list.get(1), graph).size());
		assertEquals(1, getSourceConnections(list.get(1), graph).size());
	}

	private List<Edge> getSourceConnections(Node node, Graph graph) {
		List<Edge> result = new ArrayList<Edge>();
		List<Edge> edges = graph.getEdges();
		for (Edge edge : edges)
			if (edge.getTarget().equals(node))
				result.add(edge);
		return result;
	}

	@Test
	/* see http://www.graphviz.org/doc/info/attrs.html#d:style */
	public void edgeStyleInvis() {
		Graph graph = new DotImport("digraph{1->2[style=invis]}") //$NON-NLS-1$
				.newGraphInstance();
		assertEquals(2, graph.getNodes().size());
		assertEquals(1, graph.getEdges().size());
	}

	@Test
	public void otherUnsupportedStyles() {
		Graph graph = interpreter.interpret(parse(
				"graph Sample{node[style=other];edge[style=other];1[style=other];2;1->2[style=other]}")); //$NON-NLS-1$
		assertEquals(2, graph.getNodes().size());
		assertEquals(1, graph.getEdges().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void faultyLayout() {
		interpreter.interpret(parse("graph Sample{graph[layout=cool];1;}")); //$NON-NLS-1$
	}

	private DotAst parse(String dot) {
		return new DotAst(dot);
	}
}
