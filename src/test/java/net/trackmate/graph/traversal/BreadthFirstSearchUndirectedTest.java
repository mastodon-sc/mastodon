package net.trackmate.graph.traversal;

import static net.trackmate.graph.traversal.GraphSearch.EdgeClass.CROSS;
import static net.trackmate.graph.traversal.GraphSearch.EdgeClass.TREE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectVertex;
import net.trackmate.graph.traversal.GraphSearch.EdgeClass;
import net.trackmate.graph.traversal.GraphsForTests.GraphTestBundle;
import net.trackmate.graph.traversal.GraphsForTests.TraversalTester;

import org.junit.Test;

/**
 * We assume that for unsorted search, child vertices are returned in the order
 * they are added to the graph. If they are not, this test will fail, but it
 * does not necessary means it is incorrect
 */
public class BreadthFirstSearchUndirectedTest
{

	@Test
	public void testForkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.forkPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester = new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(

				bundle.vertices.iterator(),
				bundle.vertices.iterator(),
				bundle.edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testForkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.forkStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
				bundle.vertices.iterator(),
				bundle.vertices.iterator(),
				bundle.edges.iterator(),
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
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = new ArrayList< TestVertex >( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );

		final List< TestEdge > expectedEdges = new ArrayList< TestEdge >( 7 );
		expectedEdges.add( bundle.edges.get( 6 ) );
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(
				expectedVertices.iterator(),
				expectedVertices.iterator(),
				expectedEdges.iterator(),
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
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList< ObjectVertex< Integer > >( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList< ObjectEdge< Integer > >( 7 );
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 6 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
				expectedVertices.iterator(),
				expectedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testExamplePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.wpExamplePoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 6 )
		} );
		final List< TestVertex > processedVertices = expectedVertices;
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 6 ),
				bundle.edges.get( 5 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(
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

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList< ObjectVertex< Integer > >( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );

		final List< ObjectVertex< Integer > > processedVertices = expectedVertices;

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList< ObjectEdge<Integer> >(7);
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 6 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSimpleCounterEx()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.diamondPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester = new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(

				bundle.vertices.iterator(),
				bundle.vertices.iterator(),
				bundle.edges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
//		bfs.setTraversalListener( GraphsForTests.traversalPrinter( bundle.graph ) );
		bfs.start( first );
		traversalTester.searchDone();
	}

	@Test
	public void testSingleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleEdgePoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 )
		} );
		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 )
		} );
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(
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

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList< ObjectVertex< Integer > >( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList< ObjectVertex< Integer > >( 2 );
		processedVertices.add( bundle.vertices.get( 0 ) );
		processedVertices.add( bundle.vertices.get( 1 ) );

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList< ObjectEdge< Integer > >( 1 );
		expectedEdges.add( bundle.edges.get( 0 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
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

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = bundle.vertices;
		final List< TestVertex > processedVertices = bundle.vertices;
		final List< TestEdge > expectedEdges = bundle.edges;

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(
				expectedVertices.iterator(),
				processedVertices.iterator(),
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

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = bundle.vertices;

		final List< ObjectVertex< Integer > > processedVertices = bundle.vertices;

		final List< ObjectEdge< Integer > > expectedEdges = bundle.edges;

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
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

		final TestVertex first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 ),
				bundle.vertices.get( 1 ),
				bundle.vertices.get( 2 ),
				bundle.vertices.get( 4 ),
				bundle.vertices.get( 3 ),
				bundle.vertices.get( 5 ),
				bundle.vertices.get( 6 )
		} );
		final List< TestVertex > processedVertices = expectedVertices;
		final List< TestEdge > expectedEdges = Arrays.asList( new TestEdge[] {
				bundle.edges.get( 0 ),
				bundle.edges.get( 1 ),
				bundle.edges.get( 2 ),
				bundle.edges.get( 3 ),
				bundle.edges.get( 4 ),
				bundle.edges.get( 6 ),
				bundle.edges.get( 5 )
		} );
		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester =
				new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(

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

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList< ObjectVertex< Integer > >( 7 );
		expectedVertices.add( bundle.vertices.get( 0 ) );
		expectedVertices.add( bundle.vertices.get( 1 ) );
		expectedVertices.add( bundle.vertices.get( 2 ) );
		expectedVertices.add( bundle.vertices.get( 4 ) );
		expectedVertices.add( bundle.vertices.get( 3 ) );
		expectedVertices.add( bundle.vertices.get( 5 ) );
		expectedVertices.add( bundle.vertices.get( 6 ) );

		final List< ObjectVertex< Integer > > processedVertices = expectedVertices;

		final List< ObjectEdge< Integer > > expectedEdges = new ArrayList< ObjectEdge< Integer > >( 7 );
		expectedEdges.add( bundle.edges.get( 0 ) );
		expectedEdges.add( bundle.edges.get( 1 ) );
		expectedEdges.add( bundle.edges.get( 2 ) );
		expectedEdges.add( bundle.edges.get( 3 ) );
		expectedEdges.add( bundle.edges.get( 4 ) );
		expectedEdges.add( bundle.edges.get( 6 ) );
		expectedEdges.add( bundle.edges.get( 5 ) );

		final List< EdgeClass > edgeClass = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(

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
		final BreadthFirstSearch< TestVertex, TestEdge > bfs = new BreadthFirstSearch< TestVertex, TestEdge >( bundle.graph, false );

		final List< TestVertex > expectedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestVertex > processedVertices = Arrays.asList( new TestVertex[] {
				bundle.vertices.get( 0 )
		} );

		final List< TestEdge > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > > traversalTester = new TraversalTester< TestVertex, TestEdge, BreadthFirstSearch< TestVertex, TestEdge > >(

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
		final BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >> bfs = new BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>( bundle.graph, false );

		final List< ObjectVertex< Integer > > expectedVertices = new ArrayList< ObjectVertex< Integer > >( 2 );
		expectedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectVertex< Integer > > processedVertices = new ArrayList< ObjectVertex< Integer > >( 2 );
		processedVertices.add( bundle.vertices.get( 0 ) );

		final List< ObjectEdge< Integer > > expectedEdges = Collections.emptyList();

		final List< EdgeClass > edgeClass = Collections.emptyList();

		final TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>> traversalTester =
				new TraversalTester< ObjectVertex< Integer >, ObjectEdge< Integer >, BreadthFirstSearch< ObjectVertex< Integer >, ObjectEdge< Integer >>>(
				expectedVertices.iterator(),
				processedVertices.iterator(),
				expectedEdges.iterator(),
				edgeClass.iterator() );

		bfs.setTraversalListener( traversalTester );
		bfs.start( first );
		traversalTester.searchDone();
	}
}
