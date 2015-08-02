package net.trackmate.model;

import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.GraphImp;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;
import net.trackmate.trackscheme.GraphIdBimap;
import net.trackmate.trackscheme.PoolObjectIdBimap;

public class ModelGraph< V extends AbstractSpot< V >> extends GraphImp< ModelGraph.SpotPool< V >, ModelGraph.LinkPool< V >, V, Link< V >, ByteMappedElement >
{
	public ModelGraph( final SpotFactoryI< V > spotFactory )
	{
		this( spotFactory, 10000 );
	}

	public ModelGraph( final SpotFactoryI< V > spotFactory, final int initialCapacity )
	{
		super( new LinkPool< V >( initialCapacity, new SpotPool< V >( initialCapacity, spotFactory ) ) );
	}

	public static class SpotPool< V extends AbstractSpot< V >> extends AbstractVertexPool< V, Link< V >, ByteMappedElement >
	{
		private SpotPool( final int initialCapacity, final SpotFactoryI< V > f )
		{
			super( initialCapacity, f );
			f.setSpotPool( this );
		}
	}

	static class LinkPool< V extends AbstractSpot< V > > extends AbstractEdgePool< Link< V >, V, ByteMappedElement >
	{
		public LinkPool( final int initialCapacity, final SpotPool< V > vertexPool )
		{
			this( initialCapacity, new SpotEdgeFactory< V >(), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		private LinkPool( final int initialCapacity, final SpotEdgeFactory< V > f, final SpotPool< V > vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.linkPool = this;
		}

		private static class SpotEdgeFactory< V extends AbstractSpot< V > > implements PoolObject.Factory< Link< V >, ByteMappedElement >
		{
			private LinkPool< V > linkPool;

			@Override
			public int getSizeInBytes()
			{
				return Link.SIZE_IN_BYTES;
			}

			@Override
			public Link< V > createEmptyRef()
			{
				return new Link< V >( linkPool );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	public int numSpots()
	{
		return vertexPool.size();
	}

	public int numLinks()
	{
		return edgePool.size();
	}

	public void clear()
	{
		vertexPool.clear();
		edgePool.clear();
	}

	public Iterable< V > vertices()
	{
		return vertexPool;
	}

	public Iterable< Link< V > > links()
	{
		return edgePool;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "ModelGraph {\n" );
		sb.append( "  spots = {\n" );
		for ( final V spot : vertices() )
			sb.append( "    " + spot + "\n" );
		sb.append( "  },\n" );
		sb.append( "  links = {\n" );
		for ( final Link< V > link : links() )
			sb.append( "    " + link + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}

	public SpotPool< V > getVertexPool()
	{
		return vertexPool;
	}

	protected LinkPool< V > getLinkPool()
	{
		return edgePool;
	}

	public GraphIdBimap< V, Link< V > > getIdBimap()
	{
		return new GraphIdBimap< V, Link< V > >(
				new PoolObjectIdBimap< V >( vertexPool ),
				new PoolObjectIdBimap< Link< V > >( edgePool ) );
	}

	public static interface SpotFactoryI< V extends AbstractSpot< V >> extends PoolObject.Factory< V, ByteMappedElement >
	{
		public void setSpotPool( SpotPool< V > spotPool );
	}
}
