package net.trackmate.model;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractIdVertexPool;
import net.trackmate.graph.Graph;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool.Factory;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class SpotCollection implements Iterable< Spot >
{
	private static final Factory< ByteMappedElement > poolFactory = SingleArrayMemPool.factory( ByteMappedElementArray.factory );

	final AbstractIdVertexPool< Spot, Link, ByteMappedElement > spotPool;

	final AbstractEdgePool< Link, Spot, ByteMappedElement > linkPool;

	private final Graph< Spot, Link > graph;

	final AdditionalFeatures additionalSpotFeatures;

	final AdditionalFeatures additionalEdgeFeatures;

	final PoolObject.Factory< Spot, ByteMappedElement > spotFactory = new PoolObject.Factory< Spot, ByteMappedElement >()
	{
		@Override
		public int getSizeInBytes()
		{
			return Spot.SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptyRef()
		{
			return new Spot( spotPool, additionalSpotFeatures );
		}

		@Override
		public Factory< ByteMappedElement > getMemPoolFactory()
		{
			return poolFactory;
		}
	};

	final PoolObject.Factory< Link, ByteMappedElement > edgeFactory = new PoolObject.Factory< Link, ByteMappedElement >()
	{
		@Override
		public int getSizeInBytes()
		{
			return Link.SIZE_IN_BYTES;
		}

		@Override
		public Link createEmptyRef()
		{
			return new Link( linkPool, additionalEdgeFeatures );
		}

		@Override
		public Factory< ByteMappedElement > getMemPoolFactory()
		{
			return poolFactory;
		}
	};

	public SpotCollection()
	{
		this( 10000 );
	}

	public SpotCollection( final int initialCapacity )
	{
		spotPool = new AbstractIdVertexPool< Spot, Link, ByteMappedElement >( initialCapacity, spotFactory );
		linkPool = new AbstractEdgePool< Link, Spot, ByteMappedElement >( initialCapacity, edgeFactory, spotPool );
		spotPool.linkEdgePool( linkPool );
		additionalSpotFeatures = new AdditionalFeatures( initialCapacity );
		additionalEdgeFeatures = new AdditionalFeatures( initialCapacity );
		graph = GraphImp.create( spotPool, linkPool );
	}

	public Graph< Spot, Link > getGraph()
	{
		return graph;
	}

	public int numSpots()
	{
		return spotPool.size();
	}

	public int numLinks()
	{
		return linkPool.size();
	}

	public void clear()
	{
		spotPool.clear();
		linkPool.clear();
	}

	public Spot createEmptySpotRef()
	{
		return spotPool.createRef();
	}

	public Spot createSpot()
	{
		return spotPool.create( spotPool.createRef() );
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

	public void removeSpot( final Spot spot )
	{
		spotPool.delete( spot );
	}

	public void removeSpot( final int ID )
	{
		final Spot tmp = spotPool.createRef();
		getSpot( ID, tmp );
		removeSpot( tmp );
		spotPool.releaseRef( tmp );
	}

	public Link createEmptyLinkRef()
	{
		return linkPool.createRef();
	}

	public Link getLink( final Spot source, final Spot target )
	{
		return linkPool.getEdge( source, target, linkPool.createRef() );
	}

	// garbage-free version
	public Link getLink( final Spot source, final Spot target, final Link link )
	{
		return linkPool.getEdge( source, target, link );
	}

	public Link addLink( final Spot source, final Spot target )
	{
		return linkPool.addEdge( source, target, linkPool.createRef() );
	}

	// garbage-free version
	public Link addLink( final Spot source, final Spot target, final Link edge )
	{
		return linkPool.addEdge( source, target, edge );
	}

	@Override
	public Iterator< Spot > iterator()
	{
		return spotPool.iterator( createEmptySpotRef() );
	}

	public Iterable< Link > links()
	{
		return linkPool;
	}

	public Spot getTmpSpotRef()
	{
		return spotPool.createRef();
	}

	public void releaseTmpSpotRef( final Spot spot )
	{
		spotPool.releaseRef( spot );
	}

	public void releaseTmpSpotRef( final Spot... spots )
	{
		for ( final Spot spot : spots )
			spotPool.releaseRef( spot );
	}

	public Link getTmpLinkRef()
	{
		return linkPool.createRef();
	}

	public void releaseTmpLinkRef( final Link link )
	{
		linkPool.releaseRef( link );
	}

	public void releaseTmpLinkRef( final Link... links )
	{
		for ( final Link link : links )
			linkPool.releaseRef( link );
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "SpotCollection {\n" );
		sb.append( "  spots = {\n" );
		for ( final Spot spot : this )
			sb.append( "    " + spot + "\n" );
		sb.append( "  },\n" );
		sb.append( "  links = {\n" );
		for ( final Link link : links() )
			sb.append( "    " + link + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}

	//// Adding Spots to Frames ////

	/** The frame by frame list of spot this object wrap. */
	private final ConcurrentSkipListMap< Integer, Set< Spot > > content = new ConcurrentSkipListMap< Integer, Set< Spot > >();

	public void addSpotTo( final Spot spot, final int frame )
	{
		Set< Spot > spots = content.get( frame );
		if ( null == spots )
		{
			spots = new SpotSet( this );
			content.put( frame, spots );
		}
		spots.add( spot );
		spot.setFrame( frame );;
		spot.setVisibility( true );
	}

	public NavigableSet< Integer > keySet()
	{
		return content.keySet();
	}

	public Set< Spot > getAll( final int frame )
	{
		return content.get( frame );
	}
}
