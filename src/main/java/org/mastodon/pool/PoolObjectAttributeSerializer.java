package org.mastodon.pool;

import org.mastodon.undo.attributes.AttributeUndoSerializer;

public class PoolObjectAttributeSerializer< O extends PoolObject< O, ? > > implements AttributeUndoSerializer< O >
{
	private int offset;

	private int length;

	public PoolObjectAttributeSerializer(
			int offset,
			int length )
	{
		this.offset = offset;
		this.length = length;
	}

	@Override
	public int getNumBytes()
	{
		return length;
	}

	@Override
	public void getBytes( O obj, byte[] bytes )
	{
		for ( int i = 0, j = offset; i < length; ++i, ++j )
			bytes[ i ] = obj.access.getByte( j );
	}

	@Override
	public void setBytes( O obj, byte[] bytes )
	{
		for ( int i = 0, j = offset; i < length; ++i, ++j )
			obj.access.putByte( bytes[ i ], j );
	}

	@Override
	public void notifySet( O obj )
	{}
}
