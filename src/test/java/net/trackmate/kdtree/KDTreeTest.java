package net.trackmate.kdtree;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import net.imglib2.RealLocalizable;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.DoubleMappedElement;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

import org.junit.Before;
import org.junit.Test;

public class KDTreeTest
{
	static class MyVertex extends AbstractVertex< MyVertex, MyEdge, ByteMappedElement > implements RealLocalizable
	{
		protected static final int X_OFFSET = AbstractVertex.SIZE_IN_BYTES;

		protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;

		protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;

		protected static final int SIZE_IN_BYTES = Z_OFFSET + DOUBLE_SIZE;

		protected MyVertex( final AbstractVertexPool< MyVertex, ?, ByteMappedElement > pool )
		{
			super( pool );
		}

		private final int n = 3;

		// === subset of RealPositionable ===

		public void setPosition( final RealLocalizable localizable )
		{
			for ( int d = 0; d < n; ++d )
				access.putDouble( localizable.getDoublePosition( d ), X_OFFSET + d * DOUBLE_SIZE );
		}

		public void setPosition( final double[] position )
		{
			for ( int d = 0; d < n; ++d )
				access.putDouble( position[ d ], X_OFFSET + d * DOUBLE_SIZE );
		}

		public void setPosition( final double position, final int d )
		{
			access.putDouble( position, X_OFFSET + d * DOUBLE_SIZE );
		}

		// === RealLocalizable ===

		@Override
		public int numDimensions()
		{
			return n;
		}

		@Override
		public void localize( final float[] position )
		{
			for ( int d = 0; d < n; ++d )
				position[ d ] = ( float ) access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
		}

		@Override
		public void localize( final double[] position )
		{
			for ( int d = 0; d < n; ++d )
				position[ d ] = access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return ( float ) getDoublePosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return access.getDouble( X_OFFSET + d * DOUBLE_SIZE );
		}
	}

	static class MyEdge extends AbstractEdge< MyEdge, MyVertex, ByteMappedElement >
	{
		protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

		protected MyEdge( final AbstractEdgePool< MyEdge, MyVertex, ByteMappedElement > pool )
		{
			super( pool );
		}
	}

	static class MyVertexPool extends AbstractVertexPool< MyVertex, MyEdge, ByteMappedElement >
	{
		public MyVertexPool( final int initialCapacity )
		{
			this( initialCapacity, new MyVertexFactory() );
		}

		private MyVertexPool( final int initialCapacity, final MyVertexFactory f )
		{
			super( initialCapacity, f );
			f.vertexPool = this;
		}

		static class MyVertexFactory implements PoolObject.Factory< MyVertex, ByteMappedElement >
		{
			private MyVertexPool vertexPool;

			@Override
			public int getSizeInBytes()
			{
				return MyVertex.SIZE_IN_BYTES;
			}

			@Override
			public MyVertex createEmptyRef()
			{
				return new MyVertex( vertexPool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	// TODO: make test KDTree vs. exhaustive
	// Reduce MyVertex etc to barest minimum

	final int numDataVertices = 100000;

	final int numTestVertices = 1000;

	final double minCoordinateValue = -5.0;

	final double maxCoordinateValue = 5.0;

	MyVertexPool vertexPool;

	PoolObjectList< MyVertex, ByteMappedElement > dataVertices;

	PoolObjectList< MyVertex, ByteMappedElement > testVertices;

	@Before
	public void createDataVertices()
	{
		vertexPool = new MyVertexPool( numDataVertices + numTestVertices );
		dataVertices = new PoolObjectList< MyVertex, ByteMappedElement >( vertexPool, numDataVertices );
		testVertices = new PoolObjectList< MyVertex, ByteMappedElement >( vertexPool, numTestVertices );

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
				p[ d ] = rnd.nextDouble() * size + minCoordinateValue;
			vertexPool.create( vertex );
			vertex.setPosition( p );
			testVertices.add( vertex );
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
	public void testNearestNeighborSearchDouble()
	{
		final KDTree< MyVertex, DoubleMappedElement > kdtree = KDTree.kdtree( dataVertices, vertexPool );
		kdtree.createDoubles();
		final NNDoubles< MyVertex, DoubleMappedElement > kd = new NNDoubles< MyVertex, DoubleMappedElement >( kdtree );
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
}
