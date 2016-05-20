package net.trackmate.revised.model.mamut;

import net.trackmate.graph.io.RawGraphIO.Serializer;
import net.trackmate.undo.UndoSerializer;

class ModelSerializer implements Serializer< Spot, Link >, UndoSerializer< Spot, Link >
{
	private final int VERTEX_NUM_BYTES = Spot.SIZE_IN_BYTES - Spot.X_OFFSET;
	private final int VERTEX_DATA_START = Spot.X_OFFSET;

	@Override
	public int getVertexNumBytes()
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
	public void notifyVertexAdded( final Spot vertex )
	{
		vertex.notifyVertexAdded();
	}

	@Override
	public int getEdgeNumBytes()
	{
		return 0;
	}

	@Override
	public void getBytes( final Link edge, final byte[] bytes )
	{}

	@Override
	public void setBytes( final Link edge, final byte[] bytes )
	{}

	private ModelSerializer()
	{}

	private static ModelSerializer instance = new ModelSerializer();

	public static ModelSerializer getInstance()
	{
		return instance;
	}

	@Override
	public void notifyEdgeAdded( final Link edge )
	{
		edge.notifyEdgeAdded();
	}
}
