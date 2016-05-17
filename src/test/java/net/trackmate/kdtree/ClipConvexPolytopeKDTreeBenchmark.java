package net.trackmate.kdtree;

import java.util.Random;

import net.imglib2.util.BenchmarkHelper;
import net.imglib2.util.LinAlgHelpers;
import net.trackmate.collection.RefList;
import net.trackmate.collection.ref.RefArrayList;
import net.trackmate.pool.DoubleMappedElement;

public class ClipConvexPolytopeKDTreeBenchmark
{
	public static void main( final String[] args )
	{
		final int w = 400;
		final int h = 400;
		final int nPoints = 100000;
		final Random rand = new Random( 123124 );

		final RealPointPool pool = new RealPointPool( 2, nPoints );
		final RealPoint pRef = pool.createRef();
		final RefList< RealPoint > points = new RefArrayList< RealPoint >( pool, nPoints );
		for ( int i = 0; i < nPoints; ++i )
		{
			final long x = rand.nextInt( w );
			final long y = rand.nextInt( h );
			points.add( pool.create( pRef ).init( x, y ) );
		}

		final double[][] planes = new double[ 5 ][ 3 ]; // unit normal x, y; d

		double[] plane = planes[ 0 ];
		plane[ 0 ] = 1;
		plane[ 1 ] = 1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = 230;

		plane = planes[ 1 ];
		plane[ 0 ] = -1;
		plane[ 1 ] = 1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -30;

		plane = planes[ 2 ];
		plane[ 0 ] = 0.1;
		plane[ 1 ] = -1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -230;

		plane = planes[ 3 ];
		plane[ 0 ] = -0.5;
		plane[ 1 ] = -1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -290;

		plane = planes[ 4 ];
		plane[ 0 ] = -1;
		plane[ 1 ] = 0.1;
		LinAlgHelpers.scale( plane, 1.0 / LinAlgHelpers.length( plane ), plane );
		plane[ 2 ] = -200;

		System.out.println( "partitioning list of points:" );
		BenchmarkHelper.benchmarkAndPrint( 20, false, new Runnable()
		{
			@Override
			public void run()
			{
				for ( int i = 0; i < 500; ++i )
				{
					final RefList< RealPoint >[] insideoutside = getInsidePoints( points, planes, pool );
					if ( insideoutside[ 0 ].size() > 1000000 )
						System.out.println( "bla" );
				}
			}
		} );

		System.out.println( "partitioning kdtree of points:" );
		final KDTree< RealPoint, DoubleMappedElement > kdtree = KDTree.kdtree( points, pool );
		final ClipConvexPolytopeKDTree< RealPoint, DoubleMappedElement > clipper = new ClipConvexPolytopeKDTree< RealPoint, DoubleMappedElement >( kdtree );
		BenchmarkHelper.benchmarkAndPrint( 20, false, new Runnable()
		{
			@Override
			public void run()
			{
				for ( int i = 0; i < 500; ++i )
				{
					clipper.clip( planes );
				}
			}
		} );
	}

	@SuppressWarnings( "unchecked" )
	static RefList< RealPoint >[] getInsidePoints( final RefList< RealPoint > points, final double[][] planes, final RealPointPool pool )
	{
		final int nPlanes = planes.length;
		final int n = points.get( 0 ).numDimensions();
		final RefList< RealPoint > inside = new RefArrayList< RealPoint >( pool );
		final RefList< RealPoint > outside = new RefArrayList< RealPoint >( pool );
		A: for ( final RealPoint p : points )
		{
			for ( int i = 0; i < nPlanes; ++i )
			{
				final double[] plane = planes[ i ];
				double dot = 0;
				for ( int d = 0; d < n; ++d )
					dot += p.getDoublePosition( d ) * plane[ d ];
				if ( dot < plane[ n ] )
				{
					outside.add( p );
					continue A;
				}
			}
			inside.add( p );
		}
		return new RefList[] { inside, outside };
	}
}
