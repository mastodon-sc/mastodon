package org.mastodon.kdtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import net.imglib2.RealLocalizable;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.kdtree.KDTree;
import org.mastodon.kdtree.KDTreeNode;
import org.mastodon.kdtree.NearestNeighborSearchOnKDTree;
import org.mastodon.kdtree.NearestValidNeighborSearchOnKDTree;
import org.mastodon.kdtree.RealLocalizableVertices.MyVertex;
import org.mastodon.kdtree.RealLocalizableVertices.MyVertexPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.DoubleMappedElement;
import org.mastodon.pool.SingleArrayMemPool;

public class KDTreeTest
{
	final int numDataVertices = 10000;

	final int numInvalidDataVertices = 1000;

	final int numTestVertices = 100;

	final double minCoordinateValue = -5.0;

	final double maxCoordinateValue = 5.0;

	MyVertexPool vertexPool;

	RefArrayList< MyVertex > dataVertices;

	RefArrayList< MyVertex > testVertices;

	RefSetImp< MyVertex > invalidDataVertices;

	@Before
	public void createDataVertices()
	{
		vertexPool = new MyVertexPool( numDataVertices + numTestVertices );
		dataVertices = new RefArrayList< MyVertex >( vertexPool, numDataVertices );
		testVertices = new RefArrayList< MyVertex >( vertexPool, numTestVertices );
		invalidDataVertices = new RefSetImp< MyVertex >( vertexPool, numInvalidDataVertices );

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
			map.get( invalid ).setValid( false );
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
			map.get( invalid ).setValid( false );
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
