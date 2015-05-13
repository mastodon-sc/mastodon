package net.trackmate.kdtree;

import java.util.Random;

import net.imglib2.RandomAccess;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.kdtree.HyperPlane;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.position.transform.Round;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.view.Views;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.mempool.DoubleMappedElement;

public class SplitHyperPlaneKDTreeExample
{
	public static void main( final String[] args )
	{
		final int w = 800;
		final int h = 800;
		final int nPoints = 10000;

		// make random 2D Points
		final Random rand = new Random( 123124 );
		final RealPointPool pool = new RealPointPool( 2, nPoints );
		final RealPoint pRef = pool.createRef();
		final RefList< RealPoint > points = new PoolObjectList< RealPoint >( pool, nPoints );
		for ( int i = 0; i < nPoints; ++i )
		{
			final long x = rand.nextInt( w );
			final long y = rand.nextInt( h );
			points.add( pool.create( pRef ).init( x, y ) );
		}

		// split on hyperplane
		final HyperPlane plane = new HyperPlane( 1, 0.5, 600 );
		final KDTree< RealPoint, DoubleMappedElement > kdtree = KDTree.kdtree( points, pool );
		final SplitHyperPlaneKDTree< RealPoint, DoubleMappedElement > split = new SplitHyperPlaneKDTree< RealPoint, DoubleMappedElement >( kdtree );
		split.split( plane );

		// show all points
		final Img< ARGBType > pointsImg = ArrayImgs.argbs( w, h );
		paint( points, pointsImg, new ARGBType( 0x00ff00 ) );
		ImageJFunctions.show( pointsImg );

		// show inside/outside points
		final Img< ARGBType > clipImg = ArrayImgs.argbs( w, h );
		paint( split.getAboveValues(), clipImg, new ARGBType( 0xffff00 ) );
		paint( split.getBelowValues(), clipImg, new ARGBType( 0x0000ff ) );
		ImageJFunctions.show( clipImg );
	}

	static void paint( final Iterable< ? extends RealLocalizable > points, final Img< ARGBType > output, final ARGBType color )
	{
		final int radius = 2;
		final RandomAccess< Neighborhood< ARGBType > > na = new HyperSphereShape( radius ).neighborhoodsRandomAccessible( Views.extendZero( output ) ).randomAccess();
		final Round< RandomAccess< Neighborhood< ARGBType > > > rna = new Round< RandomAccess< Neighborhood< ARGBType > > >( na );
		for ( final RealLocalizable l : points )
		{
			rna.setPosition( l );
			for ( final ARGBType t : na.get() )
				t.set( color );
		}
	}
}
