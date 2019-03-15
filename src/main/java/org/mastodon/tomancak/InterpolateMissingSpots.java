package org.mastodon.tomancak;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.imglib2.util.LinAlgHelpers;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

import static net.imglib2.util.LinAlgHelpers.rows;

public class InterpolateMissingSpots
{
	public static void interpolate( final Model model )
	{
		new InterpolateMissingSpots( model ).interpolate();
	}

	private final Model model;
	private final ModelGraph graph;

	private final Spot vref1;
	private final Spot vref2;
	private final Spot vref3;

	private final Link eref1;

	private double[] pos0 = new double[ 3 ];
	private double[] pos1 = new double[ 3 ];
	private double[] pos = new double[ 3 ];

	private double[][] cov0 = new double[ 3 ][ 3 ];
	private double[][] cov = new double[ 3 ][ 3 ];

	private InterpolateMissingSpots( final Model model )
	{
		this.model = model;
		this.graph = model.getGraph();
		vref1 = graph.vertexRef();
		vref2 = graph.vertexRef();
		vref3 = graph.vertexRef();
		eref1 = graph.edgeRef();
	}

	private void interpolate()
	{
		final ReentrantReadWriteLock lock = graph.getLock();
		lock.writeLock().lock();
		try
		{
			RefList< Link > edgesToInterpolate = RefCollections.createRefList( graph.edges() );
			for ( Link edge : graph.edges() )
			{
				final Spot from = edge.getSource( vref1 );
				final Spot to = edge.getTarget( vref2 );
				if ( to.getTimepoint() - from.getTimepoint() > 1 )
					edgesToInterpolate.add( edge );
			}

			if ( !edgesToInterpolate.isEmpty() )
			{
				edgesToInterpolate.forEach( this::interpolateEdge );
				model.setUndoPoint();
				graph.notifyGraphChanged();
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	private void interpolateEdge( Link edge )
	{
		final Spot from = edge.getSource( vref1 );
		final Spot to = edge.getTarget( vref2 );
		graph.remove( edge );

		final int t0 = from.getTimepoint();
		final int t1 = to.getTimepoint();

		final int steps = t1 - t0;
		if( steps >= 2 )
		{
			from.localize( pos0 );
			to.localize( pos1 );
			from.getCovariance( cov0 );
			final double radiusRatio = Math.sqrt( to.getBoundingSphereRadiusSquared() / from.getBoundingSphereRadiusSquared() );

			Spot previous = from;
			for ( int s = 1; s < steps; ++s )
			{
				final double ratio = ( double ) s / steps;

				final int t = t0 + s;
				lerp( pos0, pos1, ratio, pos );
				final double scale = ( 1.0 - ratio ) + ratio * radiusRatio;
				LinAlgHelpers.scale( cov0, scale * scale, cov );

				final Spot current = graph.addVertex( vref3 ).init( t, pos, cov );
				graph.addEdge( previous, current, eref1 ).init();
				previous = vref1.refTo( current );
			}
			graph.addEdge( previous, to, eref1 ).init();
		}
	}


	// TODO: Use imglib2 LinAlgHelpers.lerp() when released
	/**
	 * set c = ( 1 - t ) * a + t * b, where a, b are vectors and t is scalar. Dimensions of a, b, and c
	 * must match. In place interpolation (c==a or c==b) is allowed.
	 */
	private static void lerp( final double[] a, final double[] b, final double t, final double[] c )
	{
		assert rows( a ) == rows( b );
		assert rows( a ) == rows( c );

		final int rows = rows( a );

		for ( int i = 0; i < rows; ++i )
			c[ i ] = ( 1.0 - t ) * a[ i ] + t * b[ i ];
	}




	public static final String basepath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/";

	public static final String[] paths = {
			basepath + "1.SimView2_20130315_Mastodon_Automat-segm-t0-t300",
			basepath + "2.SimView2_20130315_Mastodon_MHT",
			basepath + "3.Pavel manual",
			basepath + "4.Vlado_TrackingPlatynereis",
			basepath + "5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG"
	};

	public static void main( String[] args ) throws IOException
	{
		final String path = paths[ 4 ];
		System.out.println( "path = " + path );
		final Dataset ds = new Dataset( path );
		InterpolateMissingSpots.interpolate( ds.model() );
		System.out.println( "done" );
	}
}
