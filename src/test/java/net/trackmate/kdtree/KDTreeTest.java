package net.trackmate.kdtree;

import static net.trackmate.kdtree.KDTreeNodeFlags.NODE_INVALID_FLAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.DoubleMappedElement;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.kdtree.RealLocalizableVertices.MyVertex;
import net.trackmate.kdtree.RealLocalizableVertices.MyVertexPool;

import org.junit.Before;
import org.junit.Test;

public class KDTreeTest
{
	final int numDataVertices = 10000;

	final int numInvalidDataVertices = 1000;

	final int numTestVertices = 100;

	final double minCoordinateValue = -5.0;

	final double maxCoordinateValue = 5.0;

	MyVertexPool vertexPool;

	PoolObjectList< MyVertex > dataVertices;

	PoolObjectList< MyVertex > testVertices;

	PoolObjectSet< MyVertex > invalidDataVertices;

	@Before
	public void createDataVertices()
	{
		vertexPool = new MyVertexPool( numDataVertices + numTestVertices );
		dataVertices = new PoolObjectList< MyVertex >( vertexPool, numDataVertices );
		testVertices = new PoolObjectList< MyVertex >( vertexPool, numTestVertices );
		invalidDataVertices = new PoolObjectSet< MyVertex >( vertexPool, numInvalidDataVertices );

		final MyVertex vertex = vertexPool.createRef();
		final int n = vertex.numDimensions();
		final double[] p = new double[ n ];
		final double size = ( maxCoordinateValue - minCoordinateValue );
		final Random rnd = new Random( 4379 );
		for ( int i = 0; i < numDataVertices; ++i )
		{
			for ( int d = 0; d < n; ++d )
				p[ d ] = rnd.nextDouble() * size + minCoordinateValue;
			vertexPool.create( vertex );
			vertex.setPosition( p );
			dataVertices.add( vertex );
		}
		for ( int i = 0; i < numTestVertices; ++i )
		{
			for ( int d = 0; d < n; ++d )
				p[ d ] = rnd.nextDouble() * 2 * size + minCoordinateValue - size / 2;
			vertexPool.create( vertex );
			vertex.setPosition( p );
			testVertices.add( vertex );
		}
		for ( int i = 0; i < numInvalidDataVertices; ++i )
		{
			final int j = rnd.nextInt( numDataVertices );
			dataVertices.getQuick( j, vertex );
			invalidDataVertices.add( vertex );
		}
		vertexPool.releaseRef( vertex );
	}

	@Test
	public void testCreateKDTree()
	{
		final KDTree< MyVertex, DoubleMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool );
		assertNotNull( kdtree );
		assertEquals( kdtree.size(), dataVertices.size() );
	}

	/**
	 * Find nearest neighbor by exhaustive search. For verification of KDTree results
	 *
	 * @param nearest
	 *            is returned, referencing the nearest data point to t.
	 * @param t
	 *            query
	 */
	private MyVertex findNearestNeighborExhaustive( final MyVertex nearest, final RealLocalizable t )
	{
		double minDistance = Double.MAX_VALUE;

		final int n = t.numDimensions();
		final double[] tpos = new double[ n ];
		final double[] ppos = new double[ n ];
		t.localize( tpos );

		for ( final MyVertex p : dataVertices )
		{
			p.localize( ppos );
			double dist = 0;
			for ( int i = 0; i < n; ++i )
				dist += ( tpos[ i ] - ppos[ i ] ) * ( tpos[ i ] - ppos[ i ] );
			if ( dist < minDistance )
			{
				minDistance = dist;
				nearest.refTo( p );
			}
		}

		return nearest;
	}

	/**
	 * Find nearest valid neighbor by exhaustive search. For verification of KDTree results
	 *
	 * @param nearest
	 *            is returned, referencing the nearest data point to t.
	 * @param t
	 *            query
	 */
	private MyVertex findNearestValidNeighborExhaustive( final MyVertex nearest, final RealLocalizable t )
	{
		double minDistance = Double.MAX_VALUE;

		final int n = t.numDimensions();
		final double[] tpos = new double[ n ];
		final double[] ppos = new double[ n ];
		t.localize( tpos );

		for ( final MyVertex p : dataVertices )
		{
			if ( invalidDataVertices.contains( p ) )
				continue;

			p.localize( ppos );
			double dist = 0;
			for ( int i = 0; i < n; ++i )
				dist += ( tpos[ i ] - ppos[ i ] ) * ( tpos[ i ] - ppos[ i ] );
			if ( dist < minDistance )
			{
				minDistance = dist;
				nearest.refTo( p );
			}
		}

		return nearest;
	}

	@Test
	public void testNearestNeighborSearch()
	{
		final KDTree< MyVertex, DoubleMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool );
		final NearestNeighborSearchOnKDTree< MyVertex, DoubleMappedElement > kd = new NearestNeighborSearchOnKDTree< MyVertex, DoubleMappedElement >( kdtree );
		final MyVertex nnExhaustive = vertexPool.createRef();
		for ( final RealLocalizable t : testVertices )
		{
			kd.search( t );
			final RealLocalizable nnKdtree = kd.getSampler().get();
			findNearestNeighborExhaustive( nnExhaustive, t );
			assertEquals( nnKdtree, nnExhaustive );
		}
		vertexPool.releaseRef( nnExhaustive );
	}

	@Test
	public void testNearestNeighborSearchBytes()
	{
		final KDTree< MyVertex, ByteMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		final NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement > kd = new NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement >( kdtree );
		final MyVertex nnExhaustive = vertexPool.createRef();
		for ( final RealLocalizable t : testVertices )
		{
			kd.search( t );
			final RealLocalizable nnKdtree = kd.getSampler().get();
			findNearestNeighborExhaustive( nnExhaustive, t );
			assertEquals( nnKdtree, nnExhaustive );
		}
		vertexPool.releaseRef( nnExhaustive );
	}

	@Test
	public void testNearestValidNeighborSearch()
	{
		final KDTree< MyVertex, DoubleMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool );
		final RefRefMap< MyVertex, KDTreeNode< MyVertex, DoubleMappedElement > > map = KDTree.createRefToKDTreeNodeMap( kdtree );
		for ( final MyVertex invalid : invalidDataVertices )
			map.get( invalid ).setFlag( NODE_INVALID_FLAG );
		final NearestValidNeighborSearchOnKDTree< MyVertex, DoubleMappedElement > kd = new NearestValidNeighborSearchOnKDTree< MyVertex, DoubleMappedElement >( kdtree );
		final MyVertex nnExhaustive = vertexPool.createRef();
		for ( final RealLocalizable t : testVertices )
		{
			kd.search( t );
			final RealLocalizable nnKdtree = kd.getSampler().get();
			findNearestValidNeighborExhaustive( nnExhaustive, t );
			assertEquals( nnKdtree, nnExhaustive );
		}
		vertexPool.releaseRef( nnExhaustive );
	}

	@Test
	public void testNearestValidNeighborSearchBytes()
	{
		final KDTree< MyVertex, ByteMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
		final RefRefMap< MyVertex, KDTreeNode< MyVertex, ByteMappedElement > > map = KDTree.createRefToKDTreeNodeMap( kdtree );
		for ( final MyVertex invalid : invalidDataVertices )
			map.get( invalid ).setFlag( NODE_INVALID_FLAG );
		final NearestValidNeighborSearchOnKDTree< MyVertex, ByteMappedElement > kd = new NearestValidNeighborSearchOnKDTree< MyVertex, ByteMappedElement >( kdtree );
		final MyVertex nnExhaustive = vertexPool.createRef();
		for ( final RealLocalizable t : testVertices )
		{
			kd.search( t );
			final RealLocalizable nnKdtree = kd.getSampler().get();
			findNearestValidNeighborExhaustive( nnExhaustive, t );
			assertEquals( nnKdtree, nnExhaustive );
		}
		vertexPool.releaseRef( nnExhaustive );
	}
}
