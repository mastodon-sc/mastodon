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

public class ModelGraph extends GraphImp< ModelGraph.SpotPool, ModelGraph.LinkPool, Spot, Link, ByteMappedElement >
{
	public ModelGraph()
	{
		this( 10000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );
	}

	static class SpotPool extends AbstractVertexPool< Spot, Link, ByteMappedElement >
	{
		public SpotPool( final int initialCapacity )
		{
			this( initialCapacity, new SpotFactory() );
		}

		private SpotPool( final int initialCapacity, final SpotFactory f )
		{
			super( initialCapacity, f );
			f.spotPool = this;
		}

		private static class SpotFactory implements PoolObject.Factory< Spot, ByteMappedElement >
		{
			private SpotPool spotPool;

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
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	static class LinkPool extends AbstractEdgePool< Link, Spot, ByteMappedElement >
	{
		public LinkPool( final int initialCapacity, final SpotPool vertexPool )
		{
			this( initialCapacity, new SpotEdgeFactory(), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		private LinkPool( final int initialCapacity, final SpotEdgeFactory f, final SpotPool vertexPool )
		{
			super( initialCapacity, f, vertexPool );
			f.linkPool = this;
		}

		private static class SpotEdgeFactory implements PoolObject.Factory< Link, ByteMappedElement >
		{
			private LinkPool linkPool;

			@Override
			public int getSizeInBytes()
			{
				return Link.SIZE_IN_BYTES;
			}

			@Override
			public Link createEmptyRef()
			{
				return new Link( linkPool );
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

	public Iterable< Spot > vertices()
	{
		return vertexPool;
	}

	public Iterable< Link > links()
	{
		return edgePool;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "SpotCollection {\n" );
		sb.append( "  spots = {\n" );
		for ( final Spot spot : vertices() )
			sb.append( "    " + spot + "\n" );
		sb.append( "  },\n" );
		sb.append( "  links = {\n" );
		for ( final Link link : links() )
			sb.append( "    " + link + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}

	protected SpotPool getVertexPool()
	{
		return vertexPool;
	}

	protected LinkPool getLinkPool()
	{
		return edgePool;
	}

	public GraphIdBimap< Spot, Link > getIdBimap()
	{
		return new GraphIdBimap< Spot, Link >(
				new PoolObjectIdBimap< Spot >( vertexPool ),
				new PoolObjectIdBimap< Link >( edgePool ) );
	}

	public static void main( final String[] args )
	{
		final ModelGraph graph = new ModelGraph();
		System.out.println( graph );
		System.out.println( "done" );
	}
}
