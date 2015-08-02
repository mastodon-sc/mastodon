package net.trackmate.model;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.imglib2.realtransform.AffineTransform3D;

public class Model
{
	private final Map< Integer, SpotSet< SpotCovariance > > timepointToSpots;

	private final ModelGraph< SpotCovariance > graph;

	public Model( final ModelGraph< SpotCovariance > graph )
	{
		this.graph = graph;
		timepointToSpots = new HashMap< Integer, SpotSet< SpotCovariance > >();
	}

	public SpotSet< SpotCovariance > getSpots( final int timepoint )
	{
		SpotSet< SpotCovariance > spots = timepointToSpots.get( timepoint );
		if ( null == spots )
		{
			spots = new SpotSet< SpotCovariance >( graph );
			timepointToSpots.put( timepoint, spots );
		}
		return spots;
	}

	public SortedSet< Integer > frames()
	{
		return new TreeSet< Integer >( timepointToSpots.keySet() );
	}

	// TODO should not be public
	public ModelGraph< SpotCovariance > getGraph()
	{
		return graph;
	}

	public SpotCovariance createSpot( final int timepoint, final AffineTransform3D transform, final double nu, final double[] m, final double[] W, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepoint, transform, nu, m, W );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public SpotCovariance createSpot( final int timepoint, final double x, final double y, final double z, final double radius, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepoint, x, y, z, radius );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public SpotCovariance createSpot( final int timepoint, final double[] pos, final double[][] cov, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepoint, pos, cov );
		getSpots( timepoint ).add( spot );
		return spot;
	}

	public Link< SpotCovariance > createLink( final SpotCovariance source, final SpotCovariance target )
	{
		return graph.addEdge( source, target );
	}

	public Link< SpotCovariance > createLink( final SpotCovariance source, final SpotCovariance target, final Link< SpotCovariance > ref )
	{
		return graph.addEdge( source, target, ref );
	}
}
