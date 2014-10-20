package net.trackmate.kdtree;

import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;

import java.util.ArrayList;
import java.util.List;
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
		protected static final int Z_OFFSET = Y_OFFSET + DOUBLE_SIZE;
		protected static final int SIZE_IN_BYTES = Z_OFFSET + DOUBLE_SIZE;

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

		public double getZ()
		{
			return access.getDouble( Z_OFFSET );
		}

		public void setZ( final double z )
		{
			access.putDouble( z, Z_OFFSET );
		}

		// === RealLocalizable ===

		@Override
		public int numDimensions()
		{
			return 3;
		}

		@Override
		public void localize( final float[] position )
		{
			position[ 0 ] = ( float ) getX();
			position[ 1 ] = ( float ) getY();
			position[ 2 ] = ( float ) getZ();
		}

		@Override
		public void localize( final double[] position )
		{
			position[ 0 ] = getX();
			position[ 1 ] = getY();
			position[ 2 ] = getZ();
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return ( float ) getDoublePosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return ( d == 0 ) ? getX() : ( ( d == 1 ) ? getY() : getZ() );
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
			vertex.setZ( p[ 2 ] ); // TODO: building kdtree is very slow if all vertices have z=0. fix this.
			vertices.add( vertex );
		}

		final long start = System.currentTimeMillis();
		final KDTree< MyVertex, ByteMappedElement > kdtree = KDTree.kdtree( vertices, vertexPool );
		final NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement > nnsearch = new NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement >( kdtree );
		final long kdSetupTime = System.currentTimeMillis() - start;
		System.out.println( "kdtree setup took: " + ( kdSetupTime ) + " ms." );
		System.out.println( String.format( "Total memory used: %.1f MB", ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}

	private static MyVertex findNearestNeighborExhaustive( final List< MyVertex > points, final MyVertex nearest, final RealPoint t )
	{
		float minDistance = Float.MAX_VALUE;

		final int n = t.numDimensions();
		final float[] tpos = new float[ n ];
		final float[] ppos = new float[ n ];
		t.localize( tpos );

		for ( final MyVertex p : points )
		{
			p.localize( ppos );
			float dist = 0;
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

	protected static boolean testNearestNeighborSearch( final int numPoints, final int numTests, final double min, final double max )
	{
		final MyVertexPool vertexPool = new MyVertexPool( numPoints );
		final PoolObjectList< MyVertex, ByteMappedElement > vertices = new PoolObjectList< KDTreeExample.MyVertex, ByteMappedElement >( vertexPool, numPoints );
		final MyVertex vertex = vertexPool.createRef();
		final int numDimensions = vertex.numDimensions();

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
			vertex.setZ( p[ 2 ] ); // TODO: building kdtree is very slow if all vertices have z=0. fix this.
			vertices.add( vertex );
		}

		long start = System.currentTimeMillis();
		final KDTree< MyVertex, ByteMappedElement > kdtree = KDTree.kdtree( vertices, vertexPool );
		kdtree.reorder();
		kdtree.createDoubles();
//		kdtree.createSOA();
//		final NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement > kd = new NearestNeighborSearchOnKDTree< MyVertex, ByteMappedElement >( kdtree );
//		final NNSOA< MyVertex, ByteMappedElement > kd = new NNSOA< MyVertex, ByteMappedElement >( kdtree );
		final NNDoubles< MyVertex, ByteMappedElement > kd = new NNDoubles< MyVertex, ByteMappedElement >( kdtree );
		final long kdSetupTime = System.currentTimeMillis() - start;
//		System.out.println( "kdtree setup took: " + ( kdSetupTime ) + " ms." );
//		System.out.println( "rootIndex = " + kdtree.rootIndex );

		start = System.currentTimeMillis();
		final ArrayList< RealPoint > testpoints = new ArrayList< RealPoint >();
		for ( int i = 0; i < numTests; ++i )
		{
			for ( int d = 0; d < numDimensions; ++d )
				p[ d ] = rnd.nextDouble() * 2 * size + min - size / 2;

			final RealPoint t = new RealPoint( p );
			testpoints.add( t );
		}

//		for ( final RealPoint t : testpoints )
//		{
//			kd.search( t );
//			final RealLocalizable nnKdtree = kd.getSampler().get();
//			final RealLocalizable nnExhaustive = findNearestNeighborExhaustive( vertices, vertex, t );
//
//			boolean equal = true;
//			for ( int d = 0; d < numDimensions; ++d )
//				if ( nnKdtree.getDoublePosition( d ) != nnExhaustive.getDoublePosition( d ) )
//					equal = false;
//			if ( !equal )
//			{
//				System.out.println( "Nearest neighbor to: " + t );
//				System.out.println( "KD-Tree says: " + nnKdtree + " " + Util.printCoordinates( nnKdtree ) );
//				System.out.println( "Exhaustive says: " + nnExhaustive + " " + Util.printCoordinates( nnExhaustive ) );
//				return false;
//			}
//		}
//		final long compareTime = System.currentTimeMillis() - start;
//		System.out.println( "comparison (kdtree <-> exhaustive) search took: " + ( compareTime ) + " ms." );

		start = System.currentTimeMillis();
		for ( final RealPoint t : testpoints )
		{
			kd.search( t );
			final RealLocalizable nnKdtree = kd.getSampler().get();
			nnKdtree.getClass();
		}
		final long kdTime = System.currentTimeMillis() - start;
		System.out.println( "kdtree search took: " + ( kdTime ) + " ms." );
//		System.out.println( "kdtree all together took: " + ( kdSetupTime + kdTime ) + " ms." );

//		start = System.currentTimeMillis();
//		for ( final RealPoint t : testpoints )
//		{
//			final RealLocalizable nnExhaustive = findNearestNeighborExhaustive( vertices, vertex, t );
//			nnExhaustive.getClass();
//		}
//		final long exhaustiveTime = System.currentTimeMillis() - start;
//		System.out.println( "exhaustive search took: " + ( exhaustiveTime ) + " ms." );

		return true;
	}

	protected static void testBuildImgLib2( final int numPoints, final double min, final double max )
	{
		final int numDimensions = new MyVertexPool( 1 ).createRef().numDimensions();

		final ArrayList< RealPoint > points = new ArrayList< RealPoint >();
		final Random rnd = new Random( 435435435 );

		final double[] p = new double[ numDimensions ];

		final double size = ( max - min );

		for ( int i = 0; i < numPoints; ++i )
		{
			for ( int d = 0; d < numDimensions; ++d )
				p[ d ] = rnd.nextDouble() * size + min;

			final RealPoint t = new RealPoint( p );
			points.add( t );
		}

		final long start = System.currentTimeMillis();
		final net.imglib2.collection.KDTree< RealPoint > kdTree = new net.imglib2.collection.KDTree< RealPoint >( points, points );
		final net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree< RealPoint > kd = new net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree< RealPoint >( kdTree );
		final long kdSetupTime = System.currentTimeMillis() - start;
		System.out.println( "kdtree setup took: " + ( kdSetupTime ) + " ms. (imglib)" );
		System.out.println( String.format( "Total memory used: %.1f MB", ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / 1e6d ) );
	}

	public static void main( final String[] args )
	{
		for ( int i = 0; i < 20; ++i )
		{
			if ( KDTreeExample.testNearestNeighborSearch( 100000, 1000, -5, 5 ) );
//				System.out.println( "Nearest neighbor test successful\n" );
		}

//		KDTreeExample.testBuild( 1000000, -5, 5 );
//		KDTreeExample.testNearestNeighborSearch( 100000, 1000, -5, 5 );
//		KDTreeExample.testBuildImgLib2( 1000000, -5, 5 );
	}
}
