package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.listenable.ListenableGraph;

import org.junit.Before;
import org.junit.Test;

public class ConnectedComponentsDynamicDefaultTest
{

	private ListenableGraph< TestVertex, TestEdge > graph;

	private List< TestVertex > vertices;

	private List< TestEdge > edges;

	private ConnectedComponentsDynamicDefault< TestVertex, TestEdge > ccd;

	@Before
	public void setUp() throws Exception
	{
		final TestGraph g = new TestGraph( 30 );
		vertices = new ArrayList< TestVertex >( 30 );
		edges = new ArrayList< TestEdge >();

		// 3-branches star component.
		{
			final TestVertex A = g.addVertex().init( 0 );
			final TestVertex B = g.addVertex().init( 1 );
			final TestVertex C = g.addVertex().init( 2 );
			final TestVertex D = g.addVertex().init( 3 );
			final TestVertex E = g.addVertex().init( 4 );
			final TestVertex F = g.addVertex().init( 5 );
			final TestVertex G = g.addVertex().init( 6 );
			vertices.add( A );
			vertices.add( B );
			vertices.add( C );
			vertices.add( D );
			vertices.add( E );
			vertices.add( F );
			vertices.add( G );
			g.addEdge( A, B );
			final TestEdge eBC = g.addEdge( B, C );
			final TestEdge eCD = g.addEdge( C, D );
			final TestEdge eCF = g.addEdge( C, F );
			g.addEdge( D, E );
			g.addEdge( F, G );
			edges.add( eBC ); // 0
			edges.add( eCD ); // 1
			edges.add( eCF ); // 2
		}

		// Straight line.
		{
			final TestVertex H = g.addVertex().init( 7 );
			final TestVertex I = g.addVertex().init( 8 );
			final TestVertex J = g.addVertex().init( 9 );
			final TestVertex K = g.addVertex().init( 10 );
			final TestVertex L = g.addVertex().init( 11 );
			vertices.add( H );
			vertices.add( I );
			vertices.add( J );
			vertices.add( K );
			vertices.add( L );
			final TestEdge eHI = g.addEdge( H, I );
			final TestEdge eIJ = g.addEdge( I, J );
			g.addEdge( J, K );
			final TestEdge eKL = g.addEdge( K, L );
			edges.add( eIJ ); // 3
			edges.add( eKL ); // 4
			edges.add( eHI ); // 5
		}

		// Single vertex.
		{
			final TestVertex M = g.addVertex().init( 12 );
			vertices.add( M );
		}

		// Single edge.
		{
			final TestVertex N = g.addVertex().init( 13 );
			final TestVertex O = g.addVertex().init( 14 );
			vertices.add( N );
			vertices.add( O );
			final TestEdge eNO = g.addEdge( N, O );
			edges.add( eNO ); // 6
		}

		// Loop.
		{
			final TestVertex P = g.addVertex().init( 15 );
			final TestVertex Q = g.addVertex().init( 16 );
			final TestVertex R = g.addVertex().init( 17 );
			final TestVertex S = g.addVertex().init( 18 );
			final TestVertex T = g.addVertex().init( 19 );
			vertices.add( P );
			vertices.add( Q );
			vertices.add( R );
			vertices.add( S );
			vertices.add( T );
			final TestEdge ePQ = g.addEdge( P, Q );
			g.addEdge( Q, R );
			g.addEdge( R, S );
			g.addEdge( S, T );
			final TestEdge eTP = g.addEdge( T, P );
			edges.add( ePQ ); // 7
			edges.add( eTP ); // 8
		}

		// Butterfly.
		{
			final TestVertex U = g.addVertex().init( 20 );
			final TestVertex V = g.addVertex().init( 21 );
			final TestVertex W = g.addVertex().init( 22 );
			final TestVertex X = g.addVertex().init( 23 );
			final TestVertex Y = g.addVertex().init( 24 );
			final TestVertex Z = g.addVertex().init( 25 );
			final TestVertex AA = g.addVertex().init( 26 );
			final TestVertex AB = g.addVertex().init( 27 );
			final TestVertex AC = g.addVertex().init( 28 );
			vertices.add( U );
			vertices.add( V );
			vertices.add( W );
			vertices.add( X );
			vertices.add( Y );
			vertices.add( Z );
			vertices.add( AA );
			vertices.add( AB );
			vertices.add( AC );
			final TestEdge eUV = g.addEdge( U, V );
			g.addEdge( V, W );
			final TestEdge eUX = g.addEdge( U, X );
			g.addEdge( X, Y );
			g.addEdge( W, Y );
			final TestEdge eUZ = g.addEdge( U, Z );
			g.addEdge( Z, AA );
			g.addEdge( AA, AB );
			final TestEdge eUAC = g.addEdge( U, AC );
			g.addEdge( AB, AC );
			edges.add( eUV );
			edges.add( eUX );
			edges.add( eUZ );
			edges.add( eUAC );
		}

		this.graph = new ListenableGraph< TestVertex, TestEdge >( g );
		this.ccd = new ConnectedComponentsDynamicDefault< TestVertex, TestEdge >( graph );
	}

	@Test
	public void testSingleRemoval()
	{
		/*
		 * Test initial connected components.
		 */
		{
			final int nComponents = ccd.nComponents();
			assertEquals( "Did not find the expected initial number of connected components.", 5, nComponents );

			// The single vertex should not show up.
			final TestVertex M = vertices.get( 12 );
			final int idOfM = ccd.idOf( M );
			assertEquals( "A single vertex should not be present in the connected components set.", -1, idOfM );

			// Other components.
			final int[] verticesIndex = new int[] { 0, 7, 13, 15, 20 };
			final int[] expectedSizes = new int[] { 7, 5, 2, 5, 9 };
			for ( int i = 0; i < expectedSizes.length; i++ )
			{
				testConnectedComponentSize( vertices.get( verticesIndex[ i ] ), expectedSizes[ i ] );
			}
		}
		
		/*
		 * Let's break things.
		 */

		/*
		 * Easy first: remove an edge in the straight line.
		 */
		
		{
			final int previouSize = ccd.nComponents();
			final TestEdge eIJ = edges.get( 3 );
			graph.beginUpdate();
			graph.remove( eIJ );
			assertEquals( "Dynamic connected components should not know of the change before endUpdate().", previouSize, ccd.nComponents() );
			graph.endUpdate();

			assertEquals( "Removing start central edge should generate 2 new components.", previouSize + 1, ccd.nComponents() );
			final TestVertex H = vertices.get( 7 );
			testConnectedComponentSize( H, 2 );
			final TestVertex L = vertices.get( 11 );
			testConnectedComponentSize( L, 3 );
		}

		/*
		 * Evil: Remove the last edge of a 3-vertices component.
		 */

		{
			final int previouSize = ccd.nComponents();
			final TestEdge eKL = edges.get( 4 );
			graph.beginUpdate();
			graph.remove( eKL );
			graph.endUpdate();

			assertEquals( "Removing last edge should not generate a new component.", previouSize, ccd.nComponents() );
			final TestVertex K = vertices.get( 10 );
			testConnectedComponentSize( K, 2 );
			testConnectedComponentContent( K, new int[] { 9, 10 } );
			// Lonely vertex get deleted.
			final TestVertex L = vertices.get( 11 );
			final int idOfL = ccd.idOf( L );
			assertEquals( "A single vertex should not be present in the connected components set.", -1, idOfL );
		}

		/*
		 * Remove the middle of the star CC.
		 */
		{
			final int previouSize = ccd.nComponents();
			final TestVertex C = vertices.get( 2 );
			graph.beginUpdate();
			graph.remove( C );
			// Since we did not endUpdate, ccd should not know about this.
			assertEquals( "Dynamic connected components should not know of the change before endUpdate().", previouSize, ccd.nComponents() );
			// endUpdate
			graph.endUpdate();

			final int idOfC = ccd.idOf( C );
			assertEquals( "A deleted vertex should not be present in the connected components set.", -1, idOfC );

			assertEquals( "Removing star central vertex should generate 3 new components.", previouSize + 2, ccd.nComponents() );
			final int[] verticesIndex = new int[] { 0, 4, 6 };
			final int[] expectedSizes = new int[] { 2, 2, 2 };
			final int[][] contents = new int[][] {
					{ 0, 1 },
					{ 3, 4 },
					{ 5, 6 }
			};
			for ( int i = 0; i < expectedSizes.length; i++ )
			{
				testConnectedComponentSize( vertices.get( verticesIndex[ i ] ), expectedSizes[ i ] );
				testConnectedComponentContent( vertices.get( verticesIndex[ i ] ), contents[ i ] );
			}
		}

		/*
		 * Remove the only edge of a 2-vertices component.
		 */
		{
			final int previouSize = ccd.nComponents();
			final TestEdge eNO = edges.get( 6 );
			graph.beginUpdate();
			graph.remove( eNO );
			graph.endUpdate();

			assertEquals( "Removing the only edge of a CC should delete a component.", previouSize - 1, ccd.nComponents() );
			final TestVertex N = vertices.get( 13 );
			final int idOfN = ccd.idOf( N );
			assertEquals( "A single vertex should not be present in the connected components set.", -1, idOfN );
			final TestVertex O = vertices.get( 14 );
			final int idOfO = ccd.idOf( O );
			assertEquals( "A single vertex should not be present in the connected components set.", -1, idOfO );
		}

		/*
		 * Remove the edge of a loop.
		 */
		{
			final int previouSize = ccd.nComponents();
			final TestEdge ePQ = edges.get( 7 );
			graph.beginUpdate();
			graph.remove( ePQ );
			graph.endUpdate();

			assertEquals( "Removing an edge in a loop CC should not change anything.", previouSize, ccd.nComponents() );
			final TestVertex P = vertices.get( 15 );
			testConnectedComponentSize( P, 5 );
		}

		/*
		 * The butterfly case.
		 */
		{
			final int previouSize = ccd.nComponents();
			final TestVertex U = vertices.get( 20 );
			graph.beginUpdate();
			graph.remove( U );
			// endUpdate
			graph.endUpdate();

			final int idOfU = ccd.idOf( U );
			assertEquals( "A deleted vertex should not be present in the connected components set.", -1, idOfU );

			assertEquals( "Removing butterfly central vertex should generate 2 new components.", previouSize + 1, ccd.nComponents() );
			final int[] verticesIndex = new int[] { 21, 28 };
			final int[] expectedSizes = new int[] { 4, 4 };
			final int[][] contents = new int[][] {
					{ 21, 22, 23, 24 },
					{ 25, 26, 27, 28 }
			};
			for ( int i = 0; i < expectedSizes.length; i++ )
			{
				testConnectedComponentSize( vertices.get( verticesIndex[ i ] ), expectedSizes[ i ] );
				testConnectedComponentContent( vertices.get( verticesIndex[ i ] ), contents[ i ] );
			}
		}
	}

	private final void testConnectedComponentSize( final TestVertex vertex, final int expectedSize )
	{
		final int id = ccd.idOf( vertex );
		assertTrue( "The component spanning from vertex " + vertex + " should be a CC.", id >= 0 );
		final int size = ccd.size( id );
		assertEquals( "The component spanning from vertex " + vertex + " with id " + id + " does not have the expected size.", expectedSize, size );
	}

	private final void testConnectedComponentContent( final TestVertex vertex, final int[] content )
	{
		final int id = ccd.idOf( vertex );
		assertTrue( "The component spanning from vertex " + vertex + " should be a CC.", id >= 0 );

		final RefSet< TestVertex > cc = ccd.get( id );
		for ( final int vid : content )
		{
			final TestVertex v = vertices.get( vid );
			assertTrue( "Connected component for vertex " + vertex + " with id " + id + " does not contain expected vertex " + v, cc.contains( v ) );
		}

	}

}
