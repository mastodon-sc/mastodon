package net.trackmate.model;

import java.util.Iterator;

import net.trackmate.model.abstractmodel.AbstractEdge;
import net.trackmate.model.abstractmodel.AbstractEdgePool;
import net.trackmate.model.abstractmodel.AbstractSpot;
import net.trackmate.model.abstractmodel.AbstractSpotPool;
import net.trackmate.model.abstractmodel.AdditionalFeatures;
import net.trackmate.util.mempool.ByteMappedElement;
import net.trackmate.util.mempool.ByteMappedElementArray;
import net.trackmate.util.mempool.MemPool.Factory;
import net.trackmate.util.mempool.SingleArrayMemPool;

public class SpotCollection implements Iterable< Spot >
{
	private static final Factory< ByteMappedElement > poolFactory = SingleArrayMemPool.factory( ByteMappedElementArray.factory );

	final AbstractSpotPool< Spot, ByteMappedElement, Edge > spotPool;

	final AbstractEdgePool< Edge, ByteMappedElement, Spot > edgePool;

	final AdditionalFeatures additionalSpotFeatures;

	final AdditionalFeatures additionalEdgeFeatures;

	final AbstractSpot.Factory< Spot, ByteMappedElement > spotFactory = new AbstractSpot.Factory< Spot, ByteMappedElement >()
	{
		@Override
		public int getSpotSizeInBytes()
		{
			return Spot.SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptySpotRef()
		{
			return new Spot( spotPool, additionalSpotFeatures );
		}
	};

	final AbstractEdge.Factory< Edge, ByteMappedElement > edgeFactory = new AbstractEdge.Factory< Edge, ByteMappedElement >()
	{
		@Override
		public int getEdgeSizeInBytes()
		{
			return Edge.SIZE_IN_BYTES;
		}

		@Override
		public Edge createEmptyEdgeRef()
		{
			return new Edge( edgePool, additionalEdgeFeatures );
		}
	};


	public SpotCollection()
	{
		this( 10000 );
	}

	public SpotCollection( final int initialCapacity )
	{
		spotPool = new AbstractSpotPool< Spot, ByteMappedElement, Edge >( initialCapacity, spotFactory, poolFactory );
		edgePool = new AbstractEdgePool< Edge, ByteMappedElement, Spot >( initialCapacity, edgeFactory, poolFactory, spotPool );
		spotPool.linkEdgePool( edgePool );
		additionalSpotFeatures = new AdditionalFeatures( initialCapacity );
		additionalEdgeFeatures = new AdditionalFeatures( initialCapacity );
	}

	public int numSpots()
	{
		return spotPool.size();
	}

	public void clear()
	{
		spotPool.clear();
		edgePool.clear();
	}

	public Spot createEmptySpotRef()
	{
		return spotPool.createEmptyRef();
	}

	public Spot createSpot()
	{
		return spotPool.create();
	}

	// garbage-free version
	public Spot createSpot( final Spot spot )
	{
		return spotPool.create( spot );
	}

	public Spot createSpot( final int ID )
	{
		return spotPool.create( ID );
	}

	// garbage-free version
	public Spot createSpot( final int ID, final Spot spot )
	{
		return spotPool.create( ID, spot );
	}

	public Spot getSpot( final int ID )
	{
		return spotPool.get( ID );
	}

	// garbage-free version
	public Spot getSpot( final int ID, final Spot spot )
	{
		return spotPool.get( ID, spot );
	}

	public void releaseSpot( final Spot spot )
	{
		spotPool.release( spot );
	}

	public void releaseSpot( final int ID )
	{
		final Spot tmp = spotPool.getTmpSpotRef();
		getSpot( ID, tmp );
		releaseSpot( tmp );
		spotPool.releaseTmpSpotRef( tmp );
	}

	public Edge createEmptyEdgeRef()
	{
		return edgePool.createEmptyRef();
	}

	public Edge getEdge( final Spot source, final Spot target )
	{
		return edgePool.getEdge( source, target );
	}

	// garbage-free version
	public Edge getEdge( final Spot source, final Spot target, final Edge edge )
	{
		return edgePool.getEdge( source, target, edge );
	}

	public Edge addEdge( final Spot source, final Spot target )
	{
		return edgePool.addEdge( source, target );
	}

	// garbage-free version
	public Edge addEdge( final Spot source, final Spot target, final Edge edge )
	{
		return edgePool.addEdge( source, target, edge );
	}

	@Override
	public Iterator< Spot > iterator()
	{
		return spotPool.iterator( createEmptySpotRef() );
	}

	public Iterable< Edge > edges()
	{
		return edgePool;
	}

	public Spot getTmpSpotRef()
	{
		return spotPool.getTmpSpotRef();
	}

	public void releaseTmpSpotRef( final Spot spot )
	{
		spotPool.releaseTmpSpotRef( spot );
	}

	public Edge getTmpEdgeRef()
	{
		return edgePool.getTmpEdgeRef();
	}

	public void releaseTmpEdgeRef( final Edge edge )
	{
		edgePool.releaseTmpEdgeRef( edge );
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "SpotCollection {\n" );
		sb.append( "  spots = {\n" );
		for ( final Spot spot : this )
			sb.append( "    " + spot + "\n" );
		sb.append( "  },\n" );
		sb.append( "  edges = {\n" );
		for ( final Edge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}
}
