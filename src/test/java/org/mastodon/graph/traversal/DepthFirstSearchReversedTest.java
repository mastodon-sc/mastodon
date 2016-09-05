package org.mastodon.graph.traversal;

import static org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass.BACK;
import static org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass.CROSS;
import static org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass.TREE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.EdgeClass;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.graph.traversal.GraphsForTests.GraphTestBundle;
import org.mastodon.graph.traversal.GraphsForTests.TraversalTester;

/**
 * We assume that for unsorted search, child vertices are returned in the order
 * they are added to the graph. If they are not, this test will fail, but it
 * does not necessary means it is incorrect
 */
public class DepthFirstSearchReversedTest
{

	@Test
	public void testForkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.forkPoolObjects();

		final TestVertex first = bundle.vertices.get( 1 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final List< TestVertex > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		
		final List< TestVertex > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );

		final List< TestEdge > edges = RefCollections.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
						expectedVertices.iterator(),
						processedVertices.iterator(),
						edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testForkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.forkStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>();
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>();
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectEdge< Integer > > edges = new ArrayList<>();
		edges.add( bundle.edges.get( 0 ) );
		
		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
						expectedVertices.iterator(),
						processedVertices.iterator(),
						edges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testLoopPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.loopPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );

		final List< TestVertex > expectedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedvertices.add( bundle.vertices.get( 0 ) );
		expectedvertices.add( bundle.vertices.get( 6 ) );
		expectedvertices.add( bundle.vertices.get( 5 ) );
		expectedvertices.add( bundle.vertices.get( 4 ) );
		expectedvertices.add( bundle.vertices.get( 3 ) );
		expectedvertices.add( bundle.vertices.get( 2 ) );
		expectedvertices.add( bundle.vertices.get( 1 ) );

		final List< TestVertex > processedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedvertices.add( bundle.vertices.get( 1 ) );
		processedvertices.add( bundle.vertices.get( 2 ) );
		processedvertices.add( bundle.vertices.get( 3 ) );
		processedvertices.add( bundle.vertices.get( 4 ) );
		processedvertices.add( bundle.vertices.get( 5 ) );
		processedvertices.add( bundle.vertices.get( 6 ) );
		processedvertices.add( bundle.vertices.get( 0 ) );

		final List< TestEdge > edges = RefCollections.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 6 ) );
		edges.add( bundle.edges.get( 5 ) );
		edges.add( bundle.edges.get( 4 ) );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
						expectedvertices.iterator(),
						processedvertices.iterator(), 
						edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testLoopStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.loopStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );

		final List< ObjectVertex< Integer > > expectedvertices = new ArrayList<>( 7 );
		expectedvertices.add( bundle.vertices.get( 0 ) );
		expectedvertices.add( bundle.vertices.get( 6 ) );
		expectedvertices.add( bundle.vertices.get( 5 ) );
		expectedvertices.add( bundle.vertices.get( 4 ) );
		expectedvertices.add( bundle.vertices.get( 3 ) );
		expectedvertices.add( bundle.vertices.get( 2 ) );
		expectedvertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedvertices = new ArrayList<>( 7 );
		processedvertices.add( bundle.vertices.get( 1 ) );
		processedvertices.add( bundle.vertices.get( 2 ) );
		processedvertices.add( bundle.vertices.get( 3 ) );
		processedvertices.add( bundle.vertices.get( 4 ) );
		processedvertices.add( bundle.vertices.get( 5 ) );
		processedvertices.add( bundle.vertices.get( 6 ) );
		processedvertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > edges = RefCollections.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 6 ) );
		edges.add( bundle.edges.get( 5 ) );
		edges.add( bundle.edges.get( 4 ) );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
						expectedvertices.iterator(), 
						processedvertices.iterator(), 
						edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExamplePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.wpExamplePoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< TestVertex > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );

		final List< TestEdge > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExampleStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.wpExampleStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 ); // E
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );

		final List< ObjectEdge< Integer > > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleEdgePoolObjects();

		final TestVertex first = bundle.vertices.get( 1 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< TestVertex > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );

		final List< TestEdge > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgeStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleEdgeStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectEdge< Integer > > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLinePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.straightLinePoolObjects();

		final TestVertex first = bundle.vertices.get( 4 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE } );

		final List< TestVertex > expectedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedvertices.add( bundle.vertices.get( 4 ) );
		expectedvertices.add( bundle.vertices.get( 3 ) );
		expectedvertices.add( bundle.vertices.get( 2 ) );
		expectedvertices.add( bundle.vertices.get( 1 ) );
		expectedvertices.add( bundle.vertices.get( 0 ) );

		final List< TestVertex > processedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedvertices.add( bundle.vertices.get( 0 ) );
		processedvertices.add( bundle.vertices.get( 1 ) );
		processedvertices.add( bundle.vertices.get( 2 ) );
		processedvertices.add( bundle.vertices.get( 3 ) );
		processedvertices.add( bundle.vertices.get( 4 ) );

		final List< TestEdge > edges = RefCollections.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
						expectedvertices.iterator(),
						processedvertices.iterator(),
						edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLineStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.straightLineStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE } );

		final List< ObjectVertex< Integer > > expectedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedvertices.add( bundle.vertices.get( 4 ) );
		expectedvertices.add( bundle.vertices.get( 3 ) );
		expectedvertices.add( bundle.vertices.get( 2 ) );
		expectedvertices.add( bundle.vertices.get( 1 ) );
		expectedvertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedvertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedvertices.add( bundle.vertices.get( 0 ) );
		processedvertices.add( bundle.vertices.get( 1 ) );
		processedvertices.add( bundle.vertices.get( 2 ) );
		processedvertices.add( bundle.vertices.get( 3 ) );
		processedvertices.add( bundle.vertices.get( 4 ) );

		final List< ObjectEdge< Integer > > edges = RefCollections.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
						expectedvertices.iterator(),
						processedvertices.iterator(),
						edges.iterator(),
						edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.twoComponentsPoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< TestVertex > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );

		final List< TestEdge > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.twoComponentsStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 ); // E
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = RefCollections.createRefList( bundle.graph.vertices() );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 5 ) );
		processedVertices.add( bundle.vertices.get( 4 ) );

		final List< ObjectEdge< Integer > > expectedEdges = RefCollections.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleVertexPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestEdge > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< TestVertex, TestEdge, DepthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleVertexStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > dfs = new DepthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, DepthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		dfs.setTraversalListener( traversalTester );
		dfs.start( first );
		traversalTester.searchDone();
	}
}
