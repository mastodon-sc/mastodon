package org.mastodon.revised.model.mamut;

import org.mastodon.graph.io.RawGraphIO.ObjectSerializer;
import org.mastodon.graph.io.RawGraphIO.Serializer;
import org.mastodon.pool.PoolObjectAttributeSerializer;
import org.mastodon.undo.attributes.AttributeSerializer;

class ModelSerializer implements Serializer< Spot, Link >
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

	static class SpotSerializer extends PoolObjectAttributeSerializer< Spot > implements ObjectSerializer< Spot >
	{
		public SpotSerializer( )
		{
			super( Spot.X_OFFSET, Spot.SIZE_IN_BYTES - Spot.X_OFFSET );
		}

//		private final int VERTEX_NUM_BYTES = Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
//		private final int VERTEX_DATA_START = Spot.X_OFFSET;
//
//		@Override
//		public int getNumBytes()
//		{
//			return VERTEX_NUM_BYTES;
//		}
//
//		@Override
//		public void getBytes( final Spot vertex, final byte[] bytes )
//		{
//			vertex.getAccess().getBytes( bytes, 0, VERTEX_NUM_BYTES, VERTEX_DATA_START );
//		}
//
//		@Override
//		public void setBytes( final Spot vertex, final byte[] bytes )
//		{
//			vertex.getAccess().putBytes( bytes, 0, VERTEX_NUM_BYTES, VERTEX_DATA_START );
//		}

		@Override
		public void notifyAdded( final Spot vertex )
		{
			vertex.notifyVertexAdded();
		}

		@Override
		public void notifySet( final Spot vertex )
		{
			vertex.notifyVertexAdded();
		}
	}

	static class LinkSerializer implements ObjectSerializer< Link >, AttributeSerializer< Link >
	{
		@Override
		public int getNumBytes()
		{
			return 0;
		}

		@Override
		public void getBytes( final Link edge, final byte[] bytes )
		{}

		@Override
		public void setBytes( final Link edge, final byte[] bytes )
		{}

		@Override
		public void notifyAdded( final Link edge )
		{
			edge.notifyEdgeAdded();
		}

		@Override
		public void notifySet( final Link edge )
		{
			edge.notifyEdgeAdded();
		}
	}
}
