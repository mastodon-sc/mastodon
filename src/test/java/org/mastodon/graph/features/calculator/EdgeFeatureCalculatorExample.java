package org.mastodon.graph.features.calculator;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.mastodon.collection.RefSet;
import org.mastodon.collection.RefCollections;
import org.mastodon.graph.ListenableGraph;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.features.DoubleFeature;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;

import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.real.DoubleType;

public class EdgeFeatureCalculatorExample
{

	private static final DoubleFeature< Link > DISP = new DoubleFeature< >( "displacement", Double.NaN );

	public static void compute( final Model model )
	{
		final ListenableGraph< Spot, Link > graph = model.getGraph();

		/*
		 * 1. Compute and store displacement for all links.
		 */

		final Spot tmp1 = graph.vertexRef();
		final Spot tmp2 = graph.vertexRef();

		final double[] s = new double[ 3 ];
		final double[] t = new double[ 3 ];

		for ( final Link link : graph.edges() )
		{
			link.getSource( tmp1 ).localize( s );
			link.getTarget( tmp2 ).localize( t );

			double dr2 = 0.;
			for ( int d = 0; d < 3; d++ )
			{
				final double dx = s[ d ] - t[ d ];
				dr2 = dx * dx;
			}

			link.feature( DISP ).set( Math.sqrt( dr2 ) );
		}

		/*
		 * Compute mean displacement for each track.
		 */

		// Find roots.

		final RefSet< Spot > roots = RefCollections.createRefSet( graph.vertices() );
		for ( final Spot spot :graph.vertices() )
		{
			if (spot.incomingEdges().isEmpty())
				roots.add(spot);
		}

		// Collect links for each root.
		final DepthFirstSearch< Spot, Link > linkCollector = new DepthFirstSearch<>( graph, SearchDirection.UNDIRECTED );


		for ( final Spot root : roots )
		{
			final DoubleType dr = new DoubleType( 0 );
			final DoubleType tmp = new DoubleType( 0 );
			final LongType n = new LongType( 0 );

			linkCollector.setTraversalListener( new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
			{

				@Override
				public void processVertexLate( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
				{}

				@Override
				public void processVertexEarly( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
				{}

				@Override
				public void processEdge( final Link edge, final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
				{
					tmp.set( edge.feature( DISP ).getDouble() );
					dr.add( tmp );
					n.inc();
				}
			} );

			linkCollector.start( root );
			if ( linkCollector.wasAborted() )
				System.out.println( "Early abortion of iterating through track " + root.getLabel() + "." );
			else
				System.out.println( String.format( Locale.US, "For root %s, mean displacement = %.3f (N = %d).", root.getLabel(), ( dr.get() / n.get() ), n.get() ) );
		}
	}

	public static void main( final String[] args ) throws IOException
	{
		long start = System.currentTimeMillis();
		final String modelFile = "samples/model_revised.raw";
		final Model model = new Model();
		System.out.print( "Loading data: " + modelFile + "... " );
		model.loadRaw( new File( modelFile ) );
		long end = System.currentTimeMillis();
		System.out.println( "Done in " + ( end - start ) + " ms." );

		start = System.currentTimeMillis();
		compute( model );
		end = System.currentTimeMillis();
		System.out.println( "Calculation done in " + ( end - start ) + " ms." );

	}
}
