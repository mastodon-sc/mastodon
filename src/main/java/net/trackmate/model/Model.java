package net.trackmate.model;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraphWrapper;

public class Model
{
	private final Map< Integer, SpotSet > timepointToSpots;

	public final ListenableGraphWrapper< Spot, Link, ModelGraph > listenableGraph;

	public Model( final ModelGraph graph )
	{
		listenableGraph = ListenableGraphWrapper.wrap( graph );
		timepointToSpots = new HashMap< Integer, SpotSet >();
	}

	public SpotSet getSpots( final int timepoint )
	{
		SpotSet spots = timepointToSpots.get( timepoint );
		if ( null == spots )
		{
			spots = new SpotSet( listenableGraph.getGraph() );
			timepointToSpots.put( timepoint, spots );
		}
		return spots;
	}

	public SortedSet< Integer > frames()
	{
		return new TreeSet< Integer >( timepointToSpots.keySet() );
	}

	// TODO should not be public
	public ModelGraph getGraph()
	{
		return listenableGraph.getGraph();
	}

	public Spot createSpot( final int timepoint, final AffineTransform3D transform, final double nu, final double[] m, final double[] W, final Spot ref )
	{
		final Spot spot = listenableGraph.addVertex( ref );
		spot.init( timepoint, transform, nu, m, W );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Spot createSpot( final int timepoint, final double x, final double y, final double z, final double radius, final Spot ref )
	{
		final Spot spot = listenableGraph.addVertex( ref );
		spot.init( timepoint, x, y, z, radius );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Spot createSpot( final int timepoint, final double[] pos, final double[][] cov, final Spot ref )
	{
		final Spot spot = listenableGraph.addVertex( ref );
		spot.init( timepoint, pos, cov );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Link createLink( final Spot source, final Spot target)
	{
		return listenableGraph.addEdge( source, target );
	}

	public Link createLink( final Spot source, final Spot target, final Link ref )
	{
		return listenableGraph.addEdge( source, target, ref );
	}

	public boolean addGraphListener( final GraphListener< Spot, Link > listener )
	{
		return listenableGraph.addGraphListener( listener );
	}

}
