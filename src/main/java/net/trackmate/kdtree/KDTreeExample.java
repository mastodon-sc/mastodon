package net.trackmate.kdtree;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;

import java.util.ArrayList;
import java.util.Random;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class KDTreeExample
{
	static class MyVertex extends AbstractVertex< MyVertex, MyEdge, ByteMappedElement > implements RealLocalizable
	{
		protected static final int X_OFFSET = AbstractVertex.SIZE_IN_BYTES;
		protected static final int Y_OFFSET = X_OFFSET + DOUBLE_SIZE;
		protected static final int SIZE_IN_BYTES = Y_OFFSET + DOUBLE_SIZE;

		protected MyVertex( final AbstractVertexPool< MyVertex, ?, ByteMappedElement > pool )
		{
			super( pool );
		}

		public double getX()
		{
			return access.getDouble( X_OFFSET );
		}

		public void setX( final double x )
		{
			access.putDouble( x, X_OFFSET );
		}

		public double getY()
		{
			return access.getDouble( Y_OFFSET );
		}

		public void setY( final double y )
		{
			access.putDouble( y, Y_OFFSET );
		}

		// === RealLocalizable ===

		@Override
		public int numDimensions()
		{
			return 2;
		}

		@Override
		public void localize( final float[] position )
		{
			position[ 0 ] = ( float ) getX();
			position[ 1 ] = ( float ) getY();
		}

		@Override
		public void localize( final double[] position )
		{
			position[ 0 ] = getX();
			position[ 1 ] = getY();
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return ( float ) getDoublePosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return ( d == 0 ) ? getX() : getY();
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

	protected static void testBuild( final int numPoints, final double min, final double max )
	{
		final MyVertexPool vertexPool = new MyVertexPool( numPoints );
		final PoolObjectList< MyVertex, ByteMappedElement > vertices = new PoolObjectList< KDTreeExample.MyVertex, ByteMappedElement >( vertexPool, numPoints );
		final MyVertex vertex = vertexPool.createRef();
		final int numDimensions = vertex.numDimensions();

		final ArrayList< RealPoint > points = new ArrayList< RealPoint >();
		final Random rnd = new Random( 435435435 );

		final double[] p = new double[ numDimensions ];

		final double size = ( max - min );

		for ( int i = 0; i < numPoints; ++i )
		{
			for ( int d = 0; d < numDimensions; ++d )
				p[ d ] = rnd.nextDouble() * size + min;

			vertexPool.create( vertex );
			vertex.setX( p[ 0 ] );
			vertex.setY( p[ 1 ] );
			vertices.add( vertex );
		}

		final long start = System.currentTimeMillis();
		final KDTree< MyVertex, ByteMappedElement > kdtree = KDTree.kdtree( vertices, vertexPool );
		final NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement > nnsearch = new NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement >( kdtree );
		final long kdSetupTime = System.currentTimeMillis() - start;
		System.out.println( "kdtree setup took: " + ( kdSetupTime ) + " ms." );
	}

	public static void main( final String[] args )
	{
		for ( int i = 0; i < 100; ++i )
			KDTreeExample.testBuild( 100000, -5, 5 );
	}
}
