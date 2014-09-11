package pietzsch;

import java.util.Iterator;

import pietzsch.mappedelementpool.ByteMappedElement;
import pietzsch.mappedelementpool.ByteMappedElementArray;
import pietzsch.mappedelementpool.Pool.Factory;
import pietzsch.mappedelementpool.SingleArrayPool;
import pietzsch.spots.AbstractEdgePool;
import pietzsch.spots.AbstractSpotPool;

public class SpotCollection implements Iterable< Spot >
{
	private static final Factory< ByteMappedElement > poolFactory = SingleArrayPool.factory( ByteMappedElementArray.factory );

	// TODO: make package private. this is just for debugging
	public final AbstractSpotPool< Spot, ByteMappedElement, Edge > spotPool;

	final AbstractEdgePool< Edge, ByteMappedElement > edgePool;

	public SpotCollection( final int initialCapacity )
	{
		spotPool = new AbstractSpotPool< Spot, ByteMappedElement, Edge >( initialCapacity, Spot.factory, poolFactory );
		edgePool = new AbstractEdgePool< Edge, ByteMappedElement >( initialCapacity, Edge.factory, poolFactory );
		spotPool.linkEdgePool( edgePool );
	}

	public void clear()
	{
		spotPool.clear();
		edgePool.clear();
	}

	public Spot createEmptySpotRef()
	{
		return spotPool.createEmptySpotRef();
	}

	public Spot createSpot()
	{
		return spotPool.create();
	}

	// garbage-free version
	public void createSpot( final Spot spot )
	{
		spotPool.create( spot );
	}

	public Spot createSpot( final int ID )
	{
		return spotPool.create( ID );
	}

	// garbage-free version
	public void createSpot( final int ID, final Spot spot )
	{
		spotPool.create( ID, spot );
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
		edgePool.releaseAllLinkedEdges( spot, spotPool );
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
		return edgePool.createEmptyEdgeRef();
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

	// TODO: remove(?) this is just for debugging.
	public Iterable< Edge > edges()
	{
		return edgePool;
	}
}
