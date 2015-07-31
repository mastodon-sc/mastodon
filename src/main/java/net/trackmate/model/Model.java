package net.trackmate.model;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.imglib2.realtransform.AffineTransform3D;

public class Model
{
	private final ModelGraph graph;

	private final Map< Integer, SpotSet > timepointToSpots;

	public Model( final ModelGraph spotCollection )
	{
		this.graph = spotCollection;
		timepointToSpots = new HashMap< Integer, SpotSet >();
	}

	public SpotSet getSpots( final int timepoint )
	{
		SpotSet spots = timepointToSpots.get( timepoint );
		if ( null == spots )
		{
			spots = new SpotSet( graph );
			timepointToSpots.put( timepoint, spots );
		}
		return spots;
	}

	public SortedSet< Integer > frames()
	{
		return new TreeSet< Integer >( timepointToSpots.keySet() );
	}

	public ModelGraph getGraph()
	{
		return graph;
	}

	public Spot createSpot( final int timepoint, final AffineTransform3D transform, final double nu, final double[] m, final double[] W, final Spot ref )
	{
		final Spot spot = graph.addVertex( ref );
		spot.init( timepoint, transform, nu, m, W );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Spot createSpot( final int timepoint, final double x, final double y, final double z, final double radius, final Spot ref )
	{
		final Spot spot = graph.addVertex( ref );
		spot.init( timepoint, x, y, z, radius );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Spot createSpot( final int timepoint, final double[] pos, final double[][] cov, final Spot ref )
	{
		final Spot spot = graph.addVertex( ref );
		spot.init( timepoint, pos, cov );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Link createLink( final Spot source, final Spot target)
	{
		return graph.addEdge( source, target );
	}

	public Link createLink( final Spot source, final Spot target, final Link ref )
	{
		return graph.addEdge( source, target, ref );
	}

}
