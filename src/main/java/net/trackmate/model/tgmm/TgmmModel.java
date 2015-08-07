package net.trackmate.model.tgmm;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.imglib2.realtransform.AffineTransform3D;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.model.Link;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.ModelGraph.SpotFactoryI;
import net.trackmate.model.ModelGraph.SpotPool;
import net.trackmate.model.SpotSet;

public class TgmmModel
{
	private final Map< Integer, SpotSet< SpotCovariance > > timepointToSpots;

	private final ModelGraph< SpotCovariance > graph;

	public TgmmModel()
	{
		this( new ModelGraph< SpotCovariance >( new SpotCovarianceFactory() ) );
	}

	TgmmModel( final ModelGraph< SpotCovariance > graph )
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

	public SortedSet< Integer > timepoints()
	{
		return new TreeSet< Integer >( timepointToSpots.keySet() );
	}

	public ModelGraph< SpotCovariance > getGraph()
	{
		return graph;
	}

	public SpotCovariance createSpot( final int timepointId, final AffineTransform3D transform, final double nu, final double[] m, final double[] W, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepointId, transform, nu, m, W );
		getSpots( timepointId ).add( spot );
		return spot;
	}

	public SpotCovariance createSpot( final int timepointId, final double[] pos, final double[][] cov, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepointId, pos, cov );
		getSpots( timepointId ).add( spot );
		return spot;
	}

	public SpotCovariance createSpot( final int timepointId, final double[] pos, final double radius, final SpotCovariance ref )
	{
		final SpotCovariance spot = graph.addVertex( ref );
		spot.init( timepointId, pos, radius );
		getSpots( timepointId ).add( spot );
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

	static class SpotCovarianceFactory implements SpotFactoryI< SpotCovariance >
	{
		private SpotPool< SpotCovariance > spotPool;

		@Override
		public int getSizeInBytes()
		{
			return SpotCovariance.SIZE_IN_BYTES;
		}

		@Override
		public SpotCovariance createEmptyRef()
		{
			return new SpotCovariance( spotPool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public void setSpotPool( final SpotPool< SpotCovariance > spotPool )
		{
			this.spotPool = spotPool;
		}
	}
}
