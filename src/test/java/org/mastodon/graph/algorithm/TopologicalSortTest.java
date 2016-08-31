package org.mastodon.graph.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefList;
import org.mastodon.collection.util.CollectionUtils;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestGraph;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.algorithm.TopologicalSort;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;

public class TopologicalSortTest
{

	private TestGraph graphRef;

	private TestVertex v10ref;

	private TestVertex v7ref;

	private RefList< TestVertex > verticesRef;

	/*
	 * Pool based
	 */

	@Before
	public void setUpRef()
	{
		// From http://en.wikipedia.org/wiki/Topological_sorting
		// X encodes "level"
		graphRef = new TestGraph();
		verticesRef = CollectionUtils.createRefList( graphRef.vertices() );

		v7ref = graphRef.addVertex().init( 0 );
		final TestVertex v11 = graphRef.addVertex().init( 1 );
		graphRef.addEdge( v7ref, v11 );
		final TestVertex v5 = graphRef.addVertex().init( 2 );
		graphRef.addEdge( v5, v11 );
		final TestVertex v8 = graphRef.addVertex().init( 3 );
		graphRef.addEdge( v7ref, v8 );
		final TestVertex v3 = graphRef.addVertex().init( 4 );
		graphRef.addEdge( v3, v8 );
		final TestVertex v2 = graphRef.addVertex().init( 5 );
		graphRef.addEdge( v11, v2 );
		final TestVertex v9 = graphRef.addVertex().init( 6 );
		graphRef.addEdge( v11, v9 );
		graphRef.addEdge( v8, v9 );
		v10ref = graphRef.addVertex().init( 7 );
		graphRef.addEdge( v3, v10ref );
		graphRef.addEdge( v11, v10ref );

		verticesRef.add( v7ref );
		verticesRef.add( v11 );
		verticesRef.add( v2 );
		verticesRef.add( v3 );
		verticesRef.add( v5 );
		verticesRef.add( v8 );
		verticesRef.add( v9 );
		verticesRef.add( v10ref );
	}

	@After
	public void tearDownRef()
	{
		graphRef = null;
	}

	@Test
	public void testBehaviorRef()
	{
		final TopologicalSort< TestVertex, TestEdge > sort = new TopologicalSort< TestVertex, TestEdge >( graphRef );
		assertFalse( sort.hasFailed() );

		final TestVertex target = graphRef.vertexRef();
		final TestVertex current = graphRef.vertexRef();
		final List< TestVertex > list = sort.get();

		for ( int i = 0; i < list.size(); i++ )
		{
			current.refTo( list.get( i ) );
			for ( final TestEdge e : current.outgoingEdges() )
			{
				e.getTarget( target );
				final boolean dependenceInList = list.subList( 0, i ).contains( target );
				assertTrue( "The dependency of " + current + ", " + target + ", could not be found in the sorted list before him.", dependenceInList );
			}
		}
		graphRef.releaseRef( current );
		graphRef.releaseRef( target );

		assertEquals( "Did not iterate through all the vertices of the graph.", verticesRef.size(), sort.get().size() );
	}

	@Test
	public void testNotDAGRef()
	{
		final TopologicalSort< TestVertex, TestEdge > sort = new TopologicalSort< TestVertex, TestEdge >( graphRef );
		assertFalse( sort.hasFailed() );

		graphRef.addEdge( v10ref, v7ref );
		final TopologicalSort< TestVertex, TestEdge > sort2 = new TopologicalSort< TestVertex, TestEdge >( graphRef );
		assertTrue( sort2.hasFailed() );
	}

	/*
	 * Object based
	 */

	private ObjectGraph< String > graphObj;

	private ObjectVertex< String > v10Obj;

	private ObjectVertex< String > v7Obj;

	private RefList< ObjectVertex< String >> verticesObj;

	@Before
	public void setUpObj()
	{
		// From http://en.wikipedia.org/wiki/Topological_sorting
		graphObj = new ObjectGraph< String >();
		v7Obj = graphObj.addVertex().init( "7" );
		final ObjectVertex< String > v11 = graphObj.addVertex().init( "11" );
		graphObj.addEdge( v7Obj, v11 );
		final ObjectVertex< String > v5 = graphObj.addVertex().init( "5" );
		graphObj.addEdge( v5, v11 );
		final ObjectVertex< String > v8 = graphObj.addVertex().init( "8" );
		graphObj.addEdge( v7Obj, v8 );
		final ObjectVertex< String > v3 = graphObj.addVertex().init( "3" );
		graphObj.addEdge( v3, v8 );
		final ObjectVertex< String > v2 = graphObj.addVertex().init( "2" );
		graphObj.addEdge( v11, v2 );
		final ObjectVertex< String > v9 = graphObj.addVertex().init( "9" );
		graphObj.addEdge( v11, v9 );
		graphObj.addEdge( v8, v9 );
		v10Obj = graphObj.addVertex().init( "10" );
		graphObj.addEdge( v3, v10Obj );
		graphObj.addEdge( v11, v10Obj );

		verticesObj = CollectionUtils.createRefList( graphObj.vertices() );
		verticesObj.add( v9 );
		verticesObj.add( v11 );
		verticesObj.add( v2 );
		verticesObj.add( v3 );
		verticesObj.add( v5 );
		verticesObj.add( v8 );
		verticesObj.add( v10Obj );
		verticesObj.add( v7Obj );
	}

	@After
	public void tearDownObj()
	{
		graphObj = null;
	}

	@Test
	public void testBehaviorObj()
	{
		final TopologicalSort< ObjectVertex< String >, ObjectEdge< String > > sort = new TopologicalSort< ObjectVertex< String >, ObjectEdge< String > >( graphObj );
		assertFalse( sort.hasFailed() );
		final List< ObjectVertex< String >> list = sort.get();

		for ( int i = 0; i < list.size(); i++ )
		{
			final ObjectVertex< String > current = list.get( i );
			for ( final ObjectEdge< String > e : current.outgoingEdges() )
			{
				final ObjectVertex< String > target = e.getTarget();
				final boolean dependenceInList = list.subList( 0, i ).contains( target );
				assertTrue( "The dependency of " + current + ", " + target + ", could not be found in the sorted list before him.", dependenceInList );
			}
		}
		assertEquals( "Did not iterate through all the vertices of the graph.", graphObj.vertices().size(), sort.get().size() );
	}

	@Test
	public void testNotDAGObj()
	{
		final TopologicalSort< ObjectVertex< String >, ObjectEdge< String > > sort = new TopologicalSort< ObjectVertex< String >, ObjectEdge< String > >( graphObj );
		assertFalse( sort.hasFailed() );

		graphObj.addEdge( v10Obj, v7Obj );
		final TopologicalSort< ObjectVertex< String >, ObjectEdge< String > > sort2 = new TopologicalSort< ObjectVertex< String >, ObjectEdge< String > >( graphObj );
		assertTrue( sort2.hasFailed() );
	}

}
