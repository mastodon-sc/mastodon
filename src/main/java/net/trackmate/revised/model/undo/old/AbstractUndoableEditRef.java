package net.trackmate.revised.model.undo.old;

import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.mempool.ByteMappedElement;

public abstract class AbstractUndoableEditRef< O extends AbstractUndoableEditRef< O > >
	extends PoolObject< O, ByteMappedElement >
	implements UndoableEdit
{
	private final UndoableEditPool< O > pool;

	protected AbstractUndoableEditRef( final UndoableEditPool< O > pool )
	{
		super( pool );
		this.pool = pool;
	}

	@Override
	protected void setToUninitializedState()
	{}

	void clear()
	{
		pool.deleteByInternalPoolIndex( getInternalPoolIndex() );
	}

	public static class UndoableEditPool< O extends PoolObject< O, ByteMappedElement > > extends Pool< O, ByteMappedElement >
	{
		public UndoableEditPool(
				final int initialCapacity,
				final PoolObject.Factory< O, ByteMappedElement > objFactory )
		{
			super( initialCapacity, objFactory );
		}

		@Override
		protected void deleteByInternalPoolIndex( final int index )
		{
			super.deleteByInternalPoolIndex( index );
		}
	}
}
