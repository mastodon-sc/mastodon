package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefSet;

import org.junit.Before;
import org.junit.Test;

public class ConnectedComponentsTest
{

	private TestGraph graph;

	private int nVertices;

	private int idLoop;

	private int idTree;

	private TestVertex R00;

	private TestVertex E;

	@Before
	public void setUp() throws Exception
	{
		graph = new TestGraph();

		/*
		 * 1. Linear branch.
		 */

		final int idLinearBranch = 0;
		final TestVertex A = graph.addVertex().init( idLinearBranch );
		final TestVertex B = graph.addVertex().init( idLinearBranch );
		final TestVertex C = graph.addVertex().init( idLinearBranch );
		final TestVertex D = graph.addVertex().init( idLinearBranch );
		graph.addEdge( A, B );
		graph.addEdge( B, C );
		graph.addEdge( C, D );

		/*
		 * 2. Loop.
		 */

		idLoop = 1;
		E = graph.addVertex().init( idLoop );
		final TestVertex F = graph.addVertex().init( idLoop );
		final TestVertex G = graph.addVertex().init( idLoop );
		final TestVertex H = graph.addVertex().init( idLoop );
		graph.addEdge( E, F );
		graph.addEdge( F, G );
		graph.addEdge( G, H );
		graph.addEdge( H, E );

		/*
		 * 3. Tree.
		 */

		idTree = 2;
		final TestVertex R0 = graph.addVertex().init( idTree );
		R00 = graph.addVertex().init( idTree );
		final TestVertex R01 = graph.addVertex().init( idTree );
		final TestVertex R000 = graph.addVertex().init( idTree );
		final TestVertex R001 = graph.addVertex().init( idTree );
		final TestVertex R010 = graph.addVertex().init( idTree );
		final TestVertex R011 = graph.addVertex().init( idTree );
		graph.addEdge( R0, R00 );
		graph.addEdge( R0, R01 );
		graph.addEdge( R00, R000 );
		graph.addEdge( R00, R001 );
		graph.addEdge( R01, R010 );
		graph.addEdge( R01, R011 );

		/*
		 * 4. Single.
		 */

		final int idSingle = 3;
		graph.addVertex().init( idSingle );

		/*
		 * 5. Double.
		 */

		final int idDouble = 4;
		final TestVertex I1 = graph.addVertex().init( idDouble );
		final TestVertex I2 = graph.addVertex().init( idDouble );
		graph.addEdge( I1, I2 );

		/*
		 * 6. Random.
		 */

		final Random ran = new Random( 1l );
		final int nv = 50 + ran.nextInt( 100 );
		final int nExtraEdges = 20 + ran.nextInt( 50 );
		final RefList< TestVertex > vList = graph.createVertexList( nv );
		final int idRandom = 5;

		final TestVertex previous = graph.addVertex().init( idRandom );
		vList.add( previous );
		for ( int i = 1; i < nv; i++ )
		{
			final TestVertex current = graph.addVertex().init( idRandom );
			vList.add( current );
			// At least a linear branch to ensure they are all connected.
			graph.addEdge( previous, current );
		}

		for ( int i = 0; i < nExtraEdges; i++ )
		{
			final int iSource = ran.nextInt( vList.size() );
			final int iTarget = ran.nextInt( vList.size() );
			if ( iSource == iTarget )
			{
				continue;
			}

			final TestVertex source = vList.get( iSource );
			final TestVertex target = vList.get( iTarget );
			graph.addEdge( source, target );
		}

		/*
		 * Count vertices
		 */

		final Iterator< TestVertex > vertexIterator = graph.vertexIterator();
		int graphCounter = 0;
		while ( vertexIterator.hasNext() )
		{
			vertexIterator.next();
			graphCounter++;
		}
		nVertices = graphCounter;
	}

	@Test
	public void testBehavior()
	{
		final ConnectedComponents< TestVertex, TestEdge > cc = new ConnectedComponents< TestVertex, TestEdge >( graph );
		final Set< RefSet< TestVertex >> components = cc.get();

		final TIntHashSet componentIds = new TIntHashSet( components.size() );

		int counter = 0;
		for ( final RefSet< TestVertex > refSet : components )
		{
			final Iterator< TestVertex > it = refSet.iterator();
			final TestVertex previous = it.next();
			counter++;
			final int currentId = previous.getId();

			assertFalse( "The component with ID = " + currentId + " is disjoint: it belongs to two different sets.", componentIds.contains( currentId ) );

			componentIds.add( currentId );
			while ( it.hasNext() )
			{
				counter++;
				final TestVertex current = it.next();
				final int id = current.getId();
				assertEquals( "Found an undesired vertex in a connected components.", currentId, id );
			}
		}

		/*
		 * Check that we did not forget any vertex.
		 */
		assertEquals( "Connected components do not span the whole graph.", nVertices, counter );

		/*
		 * Link two components and reuse the same algo instance.
		 */

		graph.addEdge( E, R00 );
		final Set< RefSet< TestVertex >> nCC = cc.get();

		counter = 0;
		componentIds.clear();
		for ( final RefSet< TestVertex > refSet : nCC )
		{
			final Iterator< TestVertex > it = refSet.iterator();
			final TestVertex previous = it.next();
			counter++;
			final int currentId = previous.getId();
			assertFalse( "The component with ID = " + currentId + " is disjoint: it belongs to two different sets.", componentIds.contains( currentId ) );

			componentIds.add( currentId );
			while ( it.hasNext() )
			{
				counter++;
				final TestVertex current = it.next();
				final int id = current.getId();
				componentIds.add( id );

				if ( id != idLoop && id != idTree )
				{
					assertEquals( "Found an undesired vertex in a connected components.", currentId, id );
				}
			}
		}
		assertEquals( "Connected components do not span the whole graph.", nVertices, counter );

	}

}
