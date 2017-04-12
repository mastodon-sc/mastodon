package org.mastodon.revised.model.mamut;

import org.mastodon.graph.io.RawGraphIO.ObjectSerializer;
import org.mastodon.graph.io.RawGraphIO.Serializer;
import org.mastodon.revisedundo.attributes.AttributeSerializer;
import org.mastodon.undo.GraphUndoSerializer;

class ModelSerializer implements Serializer< Spot, Link >, GraphUndoSerializer< Spot, Link >
{
	@Override
	public int getVertexNumBytes()
	{
		return vertexSerializer.getNumBytes();
	}

	@Override
	public void getBytes( final Spot vertex, final byte[] bytes )
	{
		vertexSerializer.getBytes( vertex, bytes );
	}

	@Override
	public void setBytes( final Spot vertex, final byte[] bytes )
	{
		vertexSerializer.setBytes( vertex, bytes );
	}

	@Override
	public void notifyVertexAdded( final Spot vertex )
	{
		vertexSerializer.notifyAdded( vertex );
	}

	@Override
	public int getEdgeNumBytes()
	{
		return edgeSerializer.getNumBytes();
	}

	@Override
	public void getBytes( final Link edge, final byte[] bytes )
	{
		edgeSerializer.getBytes( edge, bytes );
	}

	@Override
	public void setBytes( final Link edge, final byte[] bytes )
	{
		edgeSerializer.setBytes( edge, bytes );
	}

	@Override
	public void notifyEdgeAdded( final Link edge )
	{
		edgeSerializer.notifyAdded( edge );
	}

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

	static class SpotSerializer implements ObjectSerializer< Spot >, AttributeSerializer< Spot >
	{
		private final int VERTEX_NUM_BYTES = Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
		private final int VERTEX_DATA_START = Spot.X_OFFSET;

		@Override
		public int getNumBytes()
		{
			return VERTEX_NUM_BYTES;
		}

		@Override
		public void getBytes( final Spot vertex, final byte[] bytes )
		{
			for ( int i = 0; i < VERTEX_NUM_BYTES; ++i )
				bytes[ i ] = vertex.getAccess().getByte( VERTEX_DATA_START + i );
		}

		@Override
		public void setBytes( final Spot vertex, final byte[] bytes )
		{
			for ( int i = 0; i < VERTEX_NUM_BYTES; ++i )
				vertex.getAccess().putByte( bytes[ i ], VERTEX_DATA_START + i );
		}

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
