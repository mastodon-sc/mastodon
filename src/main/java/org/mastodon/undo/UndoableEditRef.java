package org.mastodon.undo;

import static org.mastodon.pool.ByteUtils.BOOLEAN_SIZE;
import static org.mastodon.pool.ByteUtils.BYTE_SIZE;
import static org.mastodon.pool.ByteUtils.LONG_SIZE;

import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.undo.UndoableEditList.UndoableEditType;

import gnu.trove.map.TIntObjectArrayMap;

public final class UndoableEditRef
		extends PoolObject< UndoableEditRef, ByteMappedElement >
		implements UndoableEdit
{
	private static final int IS_UNDO_POINT_OFFSET = 0;
	private static final int TYPE_INDEX_OFFSET = IS_UNDO_POINT_OFFSET + BOOLEAN_SIZE;
	private static final int DATA_INDEX_OFFSET = TYPE_INDEX_OFFSET + BYTE_SIZE;
	static final int SIZE_IN_BYTES = DATA_INDEX_OFFSET + LONG_SIZE;

	private final UndoableEditList pool;

	protected UndoableEditRef( final UndoableEditList pool )
	{
		super( pool );
		this.pool = pool;
	}

	@Override
	public void undo()
	{
		getEdit().undo();
	}

	@Override
	public void redo()
	{
		getEdit().redo();
	}

	@Override
	public boolean isUndoPoint()
	{
		return getEdit().isUndoPoint();
	}

	@Override
	public void setUndoPoint( final boolean isUndoPoint )
	{
		getEdit().setUndoPoint( isUndoPoint );
	}

	@Override
	protected void setToUninitializedState()
	{}

	@Override
	public void clear()
	{
		getEdit().clear();
	}

	protected byte getTypeIndex()
	{
		return access.getByte( TYPE_INDEX_OFFSET );
	}

	protected void setTypeIndex( final byte id )
	{
		access.putByte( id, TYPE_INDEX_OFFSET );
	}

	public long getDataIndex()
	{
		return access.getLong( DATA_INDEX_OFFSET );
	}

	public void setDataIndex( final long id )
	{
		access.putLong( id, DATA_INDEX_OFFSET );
	}

	public void setIsUndoPointField( final boolean isUndoPoint )
	{
		access.putBoolean( isUndoPoint, IS_UNDO_POINT_OFFSET );
	}

	public boolean getIsUndoPointField()
	{
		return access.getBoolean( IS_UNDO_POINT_OFFSET );
	}

	private final TIntObjectArrayMap< UndoableEdit > editTypes = new TIntObjectArrayMap<>();

	public < T extends UndoableEdit > T getEdit( final UndoableEditType< T > type )
	{
		@SuppressWarnings( "unchecked" )
		T edit = ( T ) editTypes.get( type.typeIndex() );
		if ( edit == null )
		{
			edit = type.createInstance( this );
			editTypes.put( type.typeIndex(), edit );
		}
		return edit;
	}

	private UndoableEdit getEdit()
	{
		final UndoableEdit edit = editTypes.get( getTypeIndex() );
		if ( edit == null )
			return getEdit( pool.getUndoableEditType( getTypeIndex() ) );
		return edit;
	}
}
