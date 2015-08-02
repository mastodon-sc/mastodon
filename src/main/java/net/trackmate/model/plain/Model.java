package net.trackmate.model.plain;

import java.util.HashMap;
import java.util.NavigableSet;
import java.util.TreeSet;

import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool.Factory;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.model.Link;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.ModelGraph.SpotFactoryI;
import net.trackmate.model.ModelGraph.SpotPool;
import net.trackmate.model.SpotSet;

public class Model
{


	private final HashMap< Integer, SpotSet< Spot >> timepointToSpots;

	private final ModelGraph< Spot > graph;

	public Model()
	{
		this( 10000 );
	}

	public Model( final int initialCapacity )
	{
		this.graph = new ModelGraph< Spot >( new SpotFactory(), initialCapacity );
		this.timepointToSpots = new HashMap< Integer, SpotSet< Spot > >();
	}

	public static class SpotFactory implements SpotFactoryI< Spot >
	{

		private AbstractVertexPool< Spot, Link< Spot >, ByteMappedElement > spotPool;

		@Override
		public int getSizeInBytes()
		{
			return Spot.SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptyRef()
		{
			return new Spot( spotPool );
		}

		@Override
		public Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public void setSpotPool( final SpotPool< Spot > spotPool )
		{
			this.spotPool = spotPool;
		}

	}

	public ModelGraph< Spot > getGraph()
	{
		return graph;
	}

	public NavigableSet< Integer > timepoints()
	{
		return new TreeSet< Integer >( timepointToSpots.keySet() );
	}

	public SpotSet< Spot > getSpots( final int timepoint )
	{
		SpotSet< Spot > spots = timepointToSpots.get( timepoint );
		if ( null == spots )
		{
			spots = new SpotSet< Spot >( graph );
			timepointToSpots.put( timepoint, spots );
		}
		return spots;
	}

	public Spot createSpot( final int timepointId, final double x, final double y, final double z, final double radius, final Spot ref )
	{
		final Spot spot = graph.addVertex( ref ).init( timepointId, x, y, z, radius );
		getSpots( timepointId ).add( spot );
		return spot;
	}

	public Link< Spot > createLink( final Spot source, final Spot target )
	{
		return graph.addEdge( source, target );
	}

	public Link< Spot > createLink( final Spot source, final Spot target, final Link< Spot > ref )
	{
		return graph.addEdge( source, target, ref );
	}
}
