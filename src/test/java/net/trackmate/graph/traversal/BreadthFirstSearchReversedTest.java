package net.trackmate.graph.traversal;

import static net.trackmate.graph.algorithm.traversal.GraphSearch.EdgeClass.BACK;
import static net.trackmate.graph.algorithm.traversal.GraphSearch.EdgeClass.CROSS;
import static net.trackmate.graph.algorithm.traversal.GraphSearch.EdgeClass.TREE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import net.trackmate.collection.util.CollectionUtils;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.algorithm.traversal.BreadthFirstSearch;
import net.trackmate.graph.algorithm.traversal.GraphSearch.EdgeClass;
import net.trackmate.graph.algorithm.traversal.GraphSearch.SearchDirection;
import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectVertex;
import net.trackmate.graph.traversal.GraphsForTests.GraphTestBundle;
import net.trackmate.graph.traversal.GraphsForTests.TraversalTester;

/**
 * We assume that for unsorted search, child vertices are returned in the order
 * they are added to the graph. If they are not, this test will fail, but it
 * does not necessary means it is incorrect
 */
public class BreadthFirstSearchReversedTest
{

	@Test
	public void testForkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.forkPoolObjects();

		final TestVertex first = bundle.vertices.get( 1 ); // B
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final List< TestVertex > vertices = CollectionUtils.createRefList( bundle.graph.vertices() );
		vertices.add( bundle.vertices.get( 1 ) );
		vertices.add( bundle.vertices.get( 0 ) );

		final List< TestEdge> edges = CollectionUtils.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
						vertices.iterator(), 
						vertices.iterator(), 
						edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testForkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.forkStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );// B
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final List< ObjectVertex< Integer > > vertices = new ArrayList<>();
		vertices.add( bundle.vertices.get( 1 ) );
		vertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > edges = new ArrayList<>();
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
						vertices.iterator(), 
						vertices.iterator(), 
						edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testLoopPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.loopPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );

		final List< TestVertex > vertices = CollectionUtils.createRefList( bundle.graph.vertices() );
		vertices.add( bundle.vertices.get( 0 ) );
		vertices.add( bundle.vertices.get( 6 ) );
		vertices.add( bundle.vertices.get( 5 ) );
		vertices.add( bundle.vertices.get( 4 ) );
		vertices.add( bundle.vertices.get( 3 ) );
		vertices.add( bundle.vertices.get( 2 ) );
		vertices.add( bundle.vertices.get( 1 ) );

		final List< TestEdge > edges = CollectionUtils.createRefList( bundle.graph.edges() );
		edges.add( bundle.edges.get( 6 ) );
		edges.add( bundle.edges.get( 5 ) );
		edges.add( bundle.edges.get( 4 ) );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge >> traversalTester =
				new TraversalTester<>(
						vertices.iterator(), 
						vertices.iterator(), 
						edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testLoopStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.loopStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, BACK } );

		final List< ObjectVertex< Integer > > vertices = new ArrayList<>();
		vertices.add( bundle.vertices.get( 0 ) );
		vertices.add( bundle.vertices.get( 6 ) );
		vertices.add( bundle.vertices.get( 5 ) );
		vertices.add( bundle.vertices.get( 4 ) );
		vertices.add( bundle.vertices.get( 3 ) );
		vertices.add( bundle.vertices.get( 2 ) );
		vertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectEdge< Integer > > edges = new ArrayList<>();
		edges.add( bundle.edges.get( 6 ) );
		edges.add( bundle.edges.get( 5 ) );
		edges.add( bundle.edges.get( 4 ) );
		edges.add( bundle.edges.get( 3 ) );
		edges.add( bundle.edges.get( 2 ) );
		edges.add( bundle.edges.get( 1 ) );
		edges.add( bundle.edges.get( 0 ) );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
						vertices.iterator(), vertices.iterator(), edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExamplePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.wpExamplePoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 1 )
		} );
		final List< TestVertex > processedVertices = expectedVertices;
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 2 ),
				bundle.edges.get( 5 ), 
				bundle.edges.get( 4 ),
				bundle.edges.get( 0 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExampleStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.wpExampleStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 7 );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = expectedVertices;

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 7 );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleEdgePoolObjects();

		final TestVertex first = bundle.vertices.get( 1 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 0 )
		} );
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgeStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleEdgeStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 2 );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 1 ) );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 1 );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLinePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.straightLinePoolObjects();

		final TestVertex first = bundle.vertices.get( 3 ); // D
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = CollectionUtils.createRefList( bundle.graph.vertices() );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< TestEdge > expectedEdges = CollectionUtils.createRefList( bundle.graph.edges() );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge >> traversalTester =
				new TraversalTester<>(
						expectedVertices.iterator(),
						expectedVertices.iterator(),
						expectedEdges.iterator(),
						edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testStraightLineStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.straightLineStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 3 ); // D
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>();
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>();
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
						expectedVertices.iterator(),
						expectedVertices.iterator(),
						expectedEdges.iterator(),
						edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.twoComponentsPoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 1 )
		} );
		final List< TestVertex > processedVertices = expectedVertices;
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 2 ),
				bundle.edges.get( 5 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 0 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testTwoComponentsStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.twoComponentsStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 7 );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = expectedVertices;

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList<>( 7 );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleVertexPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestEdge > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleVertexStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > > bfs = new BreadthFirstSearch<>( bundle.graph, SearchDirection.REVERSED );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList<>( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList<>( 2 );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer > >> traversalTester =
				new TraversalTester<>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}
}
