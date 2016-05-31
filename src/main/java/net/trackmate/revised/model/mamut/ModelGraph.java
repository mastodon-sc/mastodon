package net.trackmate.revised.model.mamut;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.features.unify.Features;
import net.trackmate.graph.ref.AbstractListenableEdgePool;
import net.trackmate.pool.ByteMappedElement;
import net.trackmate.pool.ByteMappedElementArray;
import net.trackmate.pool.MemPool;
import net.trackmate.pool.PoolObject;
import net.trackmate.pool.SingleArrayMemPool;
import net.trackmate.revised.model.AbstractModelGraph;
import net.trackmate.revised.model.AbstractSpotPool;

public class ModelGraph extends AbstractModelGraph< ModelGraph, SpotPool, LinkPool, Spot, Link, ByteMappedElement >
{
	public ModelGraph()
	{
		this( 1000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );
	}

	Features< Spot > vertexFeatures()
	{
		return vertexFeatures;
	}

	Features< Link > edgeFeatures()
	{
		return edgeFeatures;
	}

	GraphIdBimap< Spot, Link > idmap()
	{
		return idmap;
	}

	@Override
	protected void notifyGraphChanged()
	{
		super.notifyGraphChanged();
	}
}

class SpotPool extends AbstractSpotPool< Spot, Link, ByteMappedElement, ModelGraph >
{
	SpotPool( final int initialCapacity )
	{
		this( initialCapacity, new SpotFactory() );
	}

	private SpotPool( final int initialCapacity, final SpotFactory f )
	{
		super( initialCapacity, f );
		f.vertexPool = this;
	}

	private static class SpotFactory implements PoolObject.Factory< Spot, ByteMappedElement >
	{
		private SpotPool vertexPool;

		@Override
		public int getSizeInBytes()
		{
			return Spot.SIZE_IN_BYTES;
		}

		@Override
		public Spot createEmptyRef()
		{
			return new Spot( vertexPool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Spot > getRefClass()
		{
			return Spot.class;
		}
	};
}

class LinkPool extends AbstractListenableEdgePool< Link, Spot, ByteMappedElement >
{
	LinkPool( final int initialCapacity, final SpotPool vertexPool )
	{
		this( initialCapacity, new LinkFactory(), vertexPool );
	}

	private LinkPool( final int initialCapacity, final LinkPool.LinkFactory f, final SpotPool vertexPool )
	{
		super( initialCapacity, f, vertexPool );
		f.edgePool = this;
	}

	private static class LinkFactory implements PoolObject.Factory< Link, ByteMappedElement >
	{
		private LinkPool edgePool;

		@Override
		public int getSizeInBytes()
		{
			return Link.SIZE_IN_BYTES;
		}

		@Override
		public Link createEmptyRef()
		{
			return new Link( edgePool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}

		@Override
		public Class< Link > getRefClass()
		{
			return Link.class;
		}
	};
}
