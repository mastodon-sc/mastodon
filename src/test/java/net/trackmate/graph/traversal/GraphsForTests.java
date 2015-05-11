package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;
import net.trackmate.graph.traversal.GraphSearch.EdgeClass;

public class GraphsForTests
{
	public static final class TraversalTester< V extends Vertex< E >, E extends Edge< V >, T extends GraphSearch< T, V, E > > implements SearchListener< V, E, T >
	{

		private final Iterator< V > expectedDiscoveredVertexIterator;

		private final Iterator< EdgeClass > expectedEdgeClassIterator;

		final Iterator< V > expectedProcessedVertexIterator;

		private final Iterator< E > expectedEdgeIterator;

		public TraversalTester( final Iterator< V > expectedDiscoveredVertexIterator, final Iterator< V > expectedProcessedVertexIterator, final Iterator< E > expectedEdgeIterator, final Iterator< EdgeClass > expectedEdgeClassIterator )
		{
			this.expectedDiscoveredVertexIterator = expectedDiscoveredVertexIterator;
			this.expectedProcessedVertexIterator = expectedProcessedVertexIterator;
			this.expectedEdgeIterator = expectedEdgeIterator;
			this.expectedEdgeClassIterator = expectedEdgeClassIterator;
		}

		@Override
		public void processVertexLate( final V vertex, final T search )
		{
			assertEquals( "Did not finish processing vertex in expected order during search.", expectedProcessedVertexIterator.next(), vertex );
		}

		@Override
		public void processVertexEarly( final V vertex, final T search )
		{
			assertEquals( "Did not discover the expected vertex sequence during search.", expectedDiscoveredVertexIterator.next(), vertex );
		}

		@Override
		public void processEdge( final E edge, final V from, final V to, final T search )
		{
			assertEquals( "Did not cross the expected edge sequence during search.", expectedEdgeIterator.next(), edge );

			final EdgeClass eclass = search.edgeClass( from, to );
			assertEquals( "The edge " + edge + " traversed  from " + from + " to " + to + " has an unexpected class in the search.", expectedEdgeClassIterator.next(), eclass );
		}

		public void searchDone()
		{
			assertFalse( "Did not discover all the expected vertices.", expectedDiscoveredVertexIterator.hasNext() );
			assertFalse( "Did not finish processing all the expected vertices.", expectedProcessedVertexIterator.hasNext() );
			assertFalse( "Did not cross all the expected edges.", expectedEdgeIterator.hasNext() );
			assertFalse( "Did not assess all edge classes.", expectedEdgeClassIterator.hasNext() );
		}
	}

	public static final < V extends Vertex< E >, E extends Edge< V >, T extends GraphSearch< T, V, E > > SearchListener< V, E, T > traversalPrinter( final Graph< V, E > graph )
	{
		return new SearchListener< V, E, T >()
		{
			@Override
			public void processVertexLate( final V vertex, final T search )
			{
				System.out.println( " - Finished processing " + vertex );
			}

			@Override
			public void processVertexEarly( final V vertex, final T search )
			{
				System.out.println( " - Discovered " + vertex );
			}

			@Override
			public void processEdge( final E edge, final V from, final V to, final T search )
			{
				System.out.println( " - Crossing " + edge + " from " + from + " to " + to + ". Edge class = " + search.edgeClass( from, to ) );
			}
		};
	}

	public static final GraphTestBundle< TestVertex, TestEdge > straightLinePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		final TestVertex C = graph.addVertex().init( 3 );
		final TestVertex D = graph.addVertex().init( 4 );
		final TestVertex E = graph.addVertex().init( 5 );
		final TestVertex F = graph.addVertex().init( 6 );
		final TestVertex G = graph.addVertex().init( 7 );
		bundle.vertices = new ArrayList< TestVertex >( 7 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );
		bundle.vertices.add( D );
		bundle.vertices.add( E );
		bundle.vertices.add( F );
		bundle.vertices.add( G );

		final TestEdge eAB = graph.addEdge( A, B );
		final TestEdge eBC = graph.addEdge( B, C );
		final TestEdge eCD = graph.addEdge( C, D );
		final TestEdge eDE = graph.addEdge( D, E );
		final TestEdge eEF = graph.addEdge( E, F );
		final TestEdge eFG = graph.addEdge( F, G );
		bundle.edges = new ArrayList< TestEdge >( 6 );
		bundle.edges.add( eAB );
		bundle.edges.add( eBC );
		bundle.edges.add( eCD );
		bundle.edges.add( eDE );
		bundle.edges.add( eEF );
		bundle.edges.add( eFG );

		bundle.name = "Straight line pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > > straightLineStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = new GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > >();

		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		bundle.graph = graph;

		final ObjectVertex< Integer > A = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > B = graph.addVertex().init( 2 );
		final ObjectVertex< Integer > C = graph.addVertex().init( 3 );
		final ObjectVertex< Integer > D = graph.addVertex().init( 4 );
		final ObjectVertex< Integer > E = graph.addVertex().init( 5 );
		final ObjectVertex< Integer > F = graph.addVertex().init( 6 );
		final ObjectVertex< Integer > G = graph.addVertex().init( 7 );
		bundle.vertices = new ArrayList< ObjectVertex< Integer > >( 7 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );
		bundle.vertices.add( D );
		bundle.vertices.add( E );
		bundle.vertices.add( F );
		bundle.vertices.add( G );

		final ObjectEdge< Integer > eAB = graph.addEdge( A, B );
		final ObjectEdge< Integer > eBC = graph.addEdge( B, C );
		final ObjectEdge< Integer > eCD = graph.addEdge( C, D );
		final ObjectEdge< Integer > eDE = graph.addEdge( D, E );
		final ObjectEdge< Integer > eEF = graph.addEdge( E, F );
		final ObjectEdge< Integer > eFG = graph.addEdge( F, G );
		bundle.edges = new ArrayList< ObjectEdge< Integer > >( 6 );
		bundle.edges.add( eAB );
		bundle.edges.add( eBC );
		bundle.edges.add( eCD );
		bundle.edges.add( eDE );
		bundle.edges.add( eEF );
		bundle.edges.add( eFG );

		bundle.name = "Straight line standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > loopPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = straightLinePoolObjects();
		final TestEdge edge = bundle.graph.addEdge( bundle.vertices.get( 6 ), bundle.vertices.get( 0 ) );
		bundle.edges.add( edge );

		bundle.name = "Loop pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > > loopStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > > bundle = straightLineStdObjects();
		bundle.edges.add( bundle.graph.addEdge( bundle.vertices.get( 6 ), bundle.vertices.get( 0 ) ) );

		bundle.name = "Loop standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > wpExamplePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		final TestVertex C = graph.addVertex().init( 3 );
		final TestVertex D = graph.addVertex().init( 4 );
		final TestVertex E = graph.addVertex().init( 5 );
		final TestVertex F = graph.addVertex().init( 6 );
		final TestVertex G = graph.addVertex().init( 7 );
		bundle.vertices = new ArrayList< TestVertex >( 7 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );
		bundle.vertices.add( D );
		bundle.vertices.add( E );
		bundle.vertices.add( F );
		bundle.vertices.add( G );

		final TestEdge eAB = graph.addEdge( A, B ); // 0
		final TestEdge eAC = graph.addEdge( A, C ); // 1
		final TestEdge eAE = graph.addEdge( A, E ); // 2
		final TestEdge eBD = graph.addEdge( B, D ); // 3
		final TestEdge eBF = graph.addEdge( B, F ); // 4
		final TestEdge eFE = graph.addEdge( F, E ); // 5
		final TestEdge eCG = graph.addEdge( C, G ); // 6
		bundle.edges = new ArrayList< TestEdge >( 7 );
		bundle.edges.add( eAB ); // 0
		bundle.edges.add( eAC ); // 1
		bundle.edges.add( eAE ); // 2
		bundle.edges.add( eBD ); // 3
		bundle.edges.add( eBF ); // 4
		bundle.edges.add( eFE ); // 5
		bundle.edges.add( eCG ); // 6

		bundle.name = "General example pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > > wpExampleStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = new GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > >();

		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		bundle.graph = graph;

		final ObjectVertex< Integer > A = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > B = graph.addVertex().init( 2 );
		final ObjectVertex< Integer > C = graph.addVertex().init( 3 );
		final ObjectVertex< Integer > D = graph.addVertex().init( 4 );
		final ObjectVertex< Integer > E = graph.addVertex().init( 5 );
		final ObjectVertex< Integer > F = graph.addVertex().init( 6 );
		final ObjectVertex< Integer > G = graph.addVertex().init( 7 );
		bundle.vertices = new ArrayList< ObjectVertex< Integer > >( 7 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );
		bundle.vertices.add( D );
		bundle.vertices.add( E );
		bundle.vertices.add( F );
		bundle.vertices.add( G );

		final ObjectEdge< Integer > eAB = graph.addEdge( A, B );
		final ObjectEdge< Integer > eAC = graph.addEdge( A, C );
		final ObjectEdge< Integer > eAE = graph.addEdge( A, E );
		final ObjectEdge< Integer > eBD = graph.addEdge( B, D );
		final ObjectEdge< Integer > eBF = graph.addEdge( B, F );
		final ObjectEdge< Integer > eFE = graph.addEdge( F, E );
		final ObjectEdge< Integer > eCG = graph.addEdge( C, G );
		bundle.edges = new ArrayList< ObjectEdge< Integer > >( 7 );
		bundle.edges.add( eAB );
		bundle.edges.add( eAC );
		bundle.edges.add( eAE );
		bundle.edges.add( eBD );
		bundle.edges.add( eBF );
		bundle.edges.add( eFE );
		bundle.edges.add( eCG );

		bundle.name = "General example standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > singleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		bundle.vertices = Arrays.asList( new TestVertex[] { A } );

		bundle.edges = Collections.emptyList();

		bundle.name = "Single vertex pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> singleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = new GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > >();

		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		bundle.graph = graph;

		final ObjectVertex< Integer > A = graph.addVertex().init( 1 );
		bundle.vertices = new ArrayList< ObjectVertex< Integer > >( 1 );
		bundle.vertices.add( A );

		bundle.edges = Collections.emptyList();
		bundle.name = "Single vertex standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > singleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		bundle.vertices = Arrays.asList( new TestVertex[] { A, B } );

		final TestEdge eAB = graph.addEdge( A, B );
		bundle.edges = Arrays.asList( new TestEdge[] { eAB } );

		bundle.name = "Single edge pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> singleEdgeStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = new GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > >();

		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		bundle.graph = graph;

		final ObjectVertex< Integer > A = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > B = graph.addVertex().init( 2 );
		bundle.vertices = new ArrayList< ObjectVertex< Integer > >( 2 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );

		final ObjectEdge< Integer > eAB = graph.addEdge( A, B );
		bundle.edges = new ArrayList< ObjectEdge< Integer > >( 1 );
		bundle.edges.add( eAB );

		bundle.name = "Single edge standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > forkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		final TestVertex C = graph.addVertex().init( 3 );
		bundle.vertices = Arrays.asList( new TestVertex[] { A, B, C } );

		final TestEdge eAB = graph.addEdge( A, B );
		final TestEdge eAC = graph.addEdge( A, C );
		bundle.edges = Arrays.asList( new TestEdge[] { eAB, eAC } );

		bundle.name = "Fork pool objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > diamondPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = new GraphTestBundle< TestVertex, TestEdge >();

		final TestGraph graph = new TestGraph();
		bundle.graph = graph;

		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		final TestVertex C = graph.addVertex().init( 3 );
		final TestVertex D = graph.addVertex().init( 4 );
		bundle.vertices = Arrays.asList( new TestVertex[] { A, B, C, D } );

		final TestEdge eAB = graph.addEdge( A, B );
		final TestEdge eAC = graph.addEdge( A, C );
		final TestEdge eBD = graph.addEdge( B, D );
		final TestEdge eCD = graph.addEdge( C, D );
		bundle.edges = Arrays.asList( new TestEdge[] { eAB, eAC, eBD, eCD } );

		bundle.name = "Diamond pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> forkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = new GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer > >();

		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		bundle.graph = graph;

		final ObjectVertex< Integer > A = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > B = graph.addVertex().init( 2 );
		final ObjectVertex< Integer > C = graph.addVertex().init( 3 );
		bundle.vertices = new ArrayList< ObjectVertex< Integer > >( 3 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );

		final ObjectEdge< Integer > eAB = graph.addEdge( A, B );
		final ObjectEdge< Integer > eAC = graph.addEdge( A, C );
		bundle.edges = new ArrayList< ObjectEdge< Integer > >( 2 );
		bundle.edges.add( eAB );
		bundle.edges.add( eAC );

		bundle.name = "Fork standard objects";
		return bundle;
	}

	public static final GraphTestBundle< TestVertex, TestEdge > twoComponentsPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = wpExamplePoolObjects();

		final TestVertex A = bundle.graph.addVertex().init( 11 );
		final TestVertex B = bundle.graph.addVertex().init( 12 );
		final TestVertex C = bundle.graph.addVertex().init( 13 );
		final TestVertex D = bundle.graph.addVertex().init( 14 );
		final TestVertex E = bundle.graph.addVertex().init( 15 );
		final TestVertex F = bundle.graph.addVertex().init( 16 );
		final TestVertex G = bundle.graph.addVertex().init( 17 );
		bundle.vertices.addAll( Arrays.asList( new TestVertex[] { A, B, C, D, E, F, G } ) );

		final TestEdge eAB = bundle.graph.addEdge( A, B );
		final TestEdge eBC = bundle.graph.addEdge( B, C );
		final TestEdge eCD = bundle.graph.addEdge( C, D );
		final TestEdge eDE = bundle.graph.addEdge( D, E );
		final TestEdge eEF = bundle.graph.addEdge( E, F );
		final TestEdge eFG = bundle.graph.addEdge( F, G );
		bundle.edges.addAll( Arrays.asList( new TestEdge[] { eAB, eBC, eCD, eDE, eEF, eFG } ) );

		bundle.name = "Two components pool objects";
		return bundle;
	}

	public static final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> twoComponentsStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = wpExampleStdObjects();

		final ObjectVertex< Integer > A = bundle.graph.addVertex().init( 11 );
		final ObjectVertex< Integer > B = bundle.graph.addVertex().init( 12 );
		final ObjectVertex< Integer > C = bundle.graph.addVertex().init( 13 );
		final ObjectVertex< Integer > D = bundle.graph.addVertex().init( 14 );
		final ObjectVertex< Integer > E = bundle.graph.addVertex().init( 15 );
		final ObjectVertex< Integer > F = bundle.graph.addVertex().init( 16 );
		final ObjectVertex< Integer > G = bundle.graph.addVertex().init( 17 );
		bundle.vertices.add( A );
		bundle.vertices.add( B );
		bundle.vertices.add( C );
		bundle.vertices.add( D );
		bundle.vertices.add( E );
		bundle.vertices.add( F );
		bundle.vertices.add( G );

		final ObjectEdge< Integer > eAB = bundle.graph.addEdge( A, B );
		final ObjectEdge< Integer > eBC = bundle.graph.addEdge( B, C );
		final ObjectEdge< Integer > eCD = bundle.graph.addEdge( C, D );
		final ObjectEdge< Integer > eDE = bundle.graph.addEdge( D, E );
		final ObjectEdge< Integer > eEF = bundle.graph.addEdge( E, F );
		final ObjectEdge< Integer > eFG = bundle.graph.addEdge( F, G );
		bundle.edges.add( eAB );
		bundle.edges.add( eBC );
		bundle.edges.add( eCD );
		bundle.edges.add( eDE );
		bundle.edges.add( eEF );
		bundle.edges.add( eFG );

		bundle.name = "Two components standard objects";
		return bundle;
	}

	public static class GraphTestBundle< V extends Vertex< E >, E extends Edge< V > >
	{
		public Graph< V, E > graph;

		public List< V > vertices;

		public List< E > edges;

		public String name;
	}

	private GraphsForTests()
	{}
}
