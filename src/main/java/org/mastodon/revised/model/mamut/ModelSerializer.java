package org.mastodon.revised.model.mamut;

import org.mastodon.graph.io.GraphSerializer;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.pool.PoolObjectAttributeSerializer;

class ModelSerializer implements GraphSerializer< Spot, Link >
{
	private ModelSerializer()
	{}

	private static ModelSerializer instance = new ModelSerializer();

	public static ModelSerializer getInstance()
	{
		return instance;
	}

	private final SpotSerializer vertexSerializer = new SpotSerializer();

	private final LinkSerializer edgeSerializer = new LinkSerializer();

	@Override
	public SpotSerializer getVertexSerializer()
	{
		return vertexSerializer;
	}

	@Override
	public LinkSerializer getEdgeSerializer()
	{
		return edgeSerializer;
	}


	static class SpotSerializer extends PoolObjectAttributeSerializer< Spot >
	{
		public SpotSerializer()
		{
			super(
					AbstractVertexPool.layout.getSizeInBytes(),
					SpotPool.layout.getSizeInBytes() - AbstractVertexPool.layout.getSizeInBytes() );
		}

		@Override
		public void notifySet( final Spot vertex )
		{
			vertex.notifyVertexAdded();
		}
	}

	static class LinkSerializer extends PoolObjectAttributeSerializer< Link >
	{
		public LinkSerializer()
		{
			super(
					AbstractEdgePool.layout.getSizeInBytes(),
					LinkPool.layout.getSizeInBytes() - AbstractEdgePool.layout.getSizeInBytes() );
		}

		@Override
		public void notifySet( final Link edge )
		{
			edge.notifyEdgeAdded();
		}
	}
}
