package net.trackmate.graph.listenable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.collection.RefSet;

import org.junit.Before;
import org.junit.Test;

public class ListenableGraphRefTest
{

	private TestVertex root;

	private TestVertex A;

	private TestVertex B;

	private TestVertex A1;

	private TestVertex A2;

	private TestVertex B1;

	private TestVertex B2;

	private TestEdge eRootA;

	private TestEdge eA1;

	private TestEdge eA2;

	private TestEdge eB1;

	private TestEdge eB2;

	private TestVertex solo;

	private ListenableGraph< TestVertex, TestEdge > graph;

	private TestGraph sourceGraph;

	@Before
	public void setUp() throws Exception
	{
		sourceGraph = new TestGraph();
		root = sourceGraph.addVertex().init( 0 );
		A = sourceGraph.addVertex().init( 1 );
		B = sourceGraph.addVertex().init( 2 );
		A1 = sourceGraph.addVertex().init( 3 );
		A2 = sourceGraph.addVertex().init( 4 );
		B1 = sourceGraph.addVertex().init( 5 );
		B2 = sourceGraph.addVertex().init( 6 );
		eRootA = sourceGraph.addEdge( root, A );
		sourceGraph.addEdge( root, B );
		eA1 = sourceGraph.addEdge( A, A1 );
		eA2 = sourceGraph.addEdge( A, A2 );
		eB1 = sourceGraph.addEdge( B, B1 );
		eB2 = sourceGraph.addEdge( B, B2 );
		solo = sourceGraph.addVertex().init( 7 );
		graph = new ListenableGraph< TestVertex, TestEdge >( sourceGraph );
	}

	@Test( expected = RuntimeException.class )
	public void testVertexSoloRemoval()
	{
		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as added. There should not be any.", event.getVertexAdded().isEmpty() );
				assertTrue( "Found edges marked as added. There should not be any.", event.getEdgeAdded().isEmpty() );
				assertTrue( "Found edges marked as removed. There should not be any.", event.getEdgeRemoved().isEmpty() );

				assertEquals( "There should be exactly one vertex marked as removed.", 1l, event.getVertexRemoved().size() );

				final TestVertex actual = event.getVertexRemoved().iterator().next();
				assertEquals( "Vertex marked as removed is not the expected one.", solo, actual );

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );
		
		graph.beginUpdate();
		graph.remove( solo );
		graph.endUpdate();
	}

	@Test( expected = RuntimeException.class )
	public void testVertexRemoval()
	{
		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as added. There should not be any.", event.getVertexAdded().isEmpty() );
				assertTrue( "Found edges marked as added. There should not be any.", event.getEdgeAdded().isEmpty() );

				assertEquals( "There should be exactly one vertex marked as removed.", 1l, event.getVertexRemoved().size() );
				assertEquals( "There should be exactly three edges marked as removed.", 3l, event.getEdgeRemoved().size() );

				final TestVertex actual = event.getVertexRemoved().iterator().next();
				assertEquals( "Vertex marked as removed is not the expected one.", A, actual );
				
				for ( final TestEdge edge : event.getEdgeRemoved() )
				{
					final TestVertex source;
					final TestVertex target;
					if ( edge.equals( eRootA ) )
					{
						source = root;
						target = A;
					}
					else if ( edge.equals( eA1 ) )
					{
						source = A;
						target = A1;
					}
					else if ( edge.equals( eA2 ) )
					{
						source = A;
						target = A2;
					}
					else
					{
						source = null;
						target = null;
						fail( "Unexpected removed edge: " + edge );
					}

					assertEquals( "Edge marked as removed is perturbed. Its source does not match.", source, event.getPreviousEdgeSource( edge ) );
					assertEquals( "Edge marked as removed is perturbed. Its target does not match.", target, event.getPreviousEdgeTarget( edge ) );
				}

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );

		graph.beginUpdate();
		graph.remove( A );
		graph.endUpdate();
	}

	@Test( expected = RuntimeException.class )
	public void testCoumpoundVertexRemoval()
	{
		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as added. There should not be any.", event.getVertexAdded().isEmpty() );
				assertTrue( "Found edges marked as added. There should not be any.", event.getEdgeAdded().isEmpty() );

				assertEquals( "There should be exactly 4 vertices marked as removed.", 4l, event.getVertexRemoved().size() );
				assertEquals( "There should be exactly 4 edges marked as removed.", 4l, event.getEdgeRemoved().size() );

				final RefSet< TestVertex > expecteds = sourceGraph.createVertexSet();
				expecteds.add( A1 );
				expecteds.add( A2 );
				expecteds.add( B1 );
				expecteds.add( B2 );
				
				final Iterator< TestVertex > iterator = event.getVertexRemoved().iterator();
				while ( iterator.hasNext() )
				{
					final TestVertex current = iterator.next();
					final boolean found = expecteds.remove( current );
					assertTrue( "One of the vertices marked as removed was not actually removed.", found );
				}
				assertTrue( "All the actually removed vertices have not been reported by the event.", expecteds.isEmpty() );

				for ( final TestEdge edge : event.getEdgeRemoved() )
				{
					final TestVertex source;
					final TestVertex target;
					if ( edge.equals( eB1 ) )
					{
						source = B;
						target = B1;
					}
					else if ( edge.equals( eB2 ) )
					{
						source = B;
						target = B2;
					}
					else if ( edge.equals( eA1 ) )
					{
						source = A;
						target = A1;
					}
					else if ( edge.equals( eA2 ) )
					{
						source = A;
						target = A2;
					}
					else
					{
						source = null;
						target = null;
						fail( "Unexpected removed edge: " + edge );
					}

					assertEquals( "Edge marked as removed is perturbed. Its source does not match.", source, event.getPreviousEdgeSource( edge ) );
					assertEquals( "Edge marked as removed is perturbed. Its target does not match.", target, event.getPreviousEdgeTarget( edge ) );
				}

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );

		graph.beginUpdate();
		graph.remove( A1 );
		graph.remove( A2 );
		graph.remove( B1 );
		graph.remove( B2 );
		graph.endUpdate();
	}

	@Test( expected = RuntimeException.class )
	public void testVertexAdded()
	{
		final int newId = 10000;
		
		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as removed. There should not be any.", event.getVertexRemoved().isEmpty() );
				assertTrue( "Found edges marked as added. There should not be any.", event.getEdgeAdded().isEmpty() );
				assertTrue( "Found edges marked as removed. There should not be any.", event.getEdgeRemoved().isEmpty() );

				assertEquals( "There should be exactly one vertex marked as added.", 1l, event.getVertexAdded().size() );

				final TestVertex actual = event.getVertexAdded().iterator().next();
				assertEquals( "Vertex marked as added is not the expected one.", newId, actual.getId() );

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );

		graph.beginUpdate();
		graph.addVertex().init( newId );
		graph.endUpdate();
	}

	@Test( expected = RuntimeException.class )
	public void testEdgeAdded()
	{
		final TestVertex source = root;
		final TestVertex target = solo;

		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as removed. There should not be any.", event.getVertexRemoved().isEmpty() );
				assertTrue( "Found vertices marked as added. There should not be any.", event.getVertexAdded().isEmpty() );
				assertTrue( "Found edges marked as removed. There should not be any.", event.getEdgeRemoved().isEmpty() );

				assertEquals( "There should be exactly one edge marked as added.", 1l, event.getEdgeAdded().size() );

				final TestEdge actual = event.getEdgeAdded().iterator().next();
				assertEquals( "Edge marked as added is not the expected one. Its source does not match.", source, actual.getSource() );
				assertEquals( "Edge marked as added is not the expected one. Its target does not match.", target, actual.getTarget() );

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );

		graph.beginUpdate();
		graph.addEdge( source, target );
		graph.endUpdate();
	}

	@Test( expected = RuntimeException.class )
	public void testEdgeRemoved()
	{
		final TestEdge edge = eA1;
		final TestVertex source = A;
		final TestVertex target = A1;

		graph.addGraphListener( new GraphListener< TestVertex, TestEdge >()
		{
			@Override
			public void graphChanged( final GraphChangeEvent< TestVertex, TestEdge > event )
			{
				assertTrue( "Found vertices marked as removed. There should not be any.", event.getVertexRemoved().isEmpty() );
				assertTrue( "Found vertices marked as added. There should not be any.", event.getVertexAdded().isEmpty() );
				assertTrue( "Found edges marked as added. There should not be any.", event.getEdgeAdded().isEmpty() );

				assertEquals( "There should be exactly one edge marked as removed.", 1l, event.getEdgeRemoved().size() );

				final TestEdge actual = event.getEdgeRemoved().iterator().next();
				assertEquals( "Edge marked as removed is not the expected one.", edge, actual );

				/*
				 * To test what were the source and target of an edge, you have
				 * to rely on these two event methods.The #getSource() and
				 * #getTarget() methods of the edge class are not reliable after
				 * the edge has been removed from the graph.
				 */

				assertEquals( "Edge marked as removed is perturbed. Its source does not match.", source, event.getPreviousEdgeSource( actual ) );
				assertEquals( "Edge marked as removed is perturbed. Its target does not match.", target, event.getPreviousEdgeTarget( actual ) );

				// Test that the event has been fired.
				throw new RuntimeException( "The event has been fired as expected." );
			}
		} );

		graph.beginUpdate();
		graph.remove( edge );
		graph.endUpdate();
	}

}
