package net.trackmate.revised.undo;

import java.util.ArrayList;

import gnu.trove.map.TIntObjectArrayMap;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

public class UndoableEditList<
			V extends Vertex< E >,
			E extends Edge< V > >
		extends Pool< UndoableEditRef< V, E >, ByteMappedElement >
{
	protected final UndoDataStack dataStack;

	protected final ArrayList< UndoableEdit > nonRefEdits;

	/**
	 * Index in {@link #edits} where the next {@link UndoableEdit} is to be
	 * recorded. (This is not simply the end of the list because of Redo ...)
	 */
	protected int nextEditIndex;

	public UndoableEditList( final int initialCapacity )
	{
		this( initialCapacity, new Factory<>() );
	}

	private UndoableEditList( final int initialCapacity, final Factory< V, E > f )
	{
		super( initialCapacity, f );
		f.pool = this;

		dataStack = new UndoDataStack( 1024 * 1024 * 32 );
		nonRefEdits = new ArrayList<>();
	}

	public void setUndoPoint()
	{
		final UndoableEditRef< V, E > ref = createRef();
		if ( nextEditIndex > 0 )
			get( nextEditIndex - 1, ref ).setUndoPoint( true );
		releaseRef( ref );
	}

	public void undo()
	{
		final UndoableEditRef< V, E > ref = createRef();
		boolean first = true;
		for ( int i = nextEditIndex - 1; i >= 0; --i )
		{
			final UndoableEdit edit = get( i, ref );
			if ( edit.isUndoPoint() && !first )
				break;
			edit.undo();
			--nextEditIndex;
			first = false;
		}
		releaseRef( ref );
	}

	public void redo()
	{
		final UndoableEditRef< V, E > ref = createRef();
		for ( int i = nextEditIndex; i < size(); ++i )
		{
			final UndoableEdit edit = get( i, ref );
			edit.redo();
			++nextEditIndex;
			if ( edit.isUndoPoint() )
				break;
		}
		releaseRef( ref );
	}

	private UndoableEditRef< V, E > get( final int index, final UndoableEditRef< V, E > ref )
	{
		super.getByInternalPoolIndex( index, ref );
		return ref;
	}

	@Override
	public UndoableEditRef< V, E > create( final UndoableEditRef< V, E > ref )
	{
		if ( nextEditIndex < size() )
			clearFromIndex( nextEditIndex, ref );
		super.create( ref );
		++nextEditIndex;
		return ref;
	}

	private void clearFromIndex( final int fromIndex, final UndoableEditRef< V, E > ref )
	{
		for ( int i = super.size() - 1; i >= fromIndex; --i )
		{
			getByInternalPoolIndex( i, ref );
			ref.clear();
			deleteByInternalPoolIndex( i );
		}
	}

	@Override
	protected void deleteByInternalPoolIndex( final int index )
	{
		super.deleteByInternalPoolIndex( index );
	}

	private static class Factory<
				V extends Vertex< E >,
				E extends Edge< V > >
			implements PoolObject.Factory< UndoableEditRef< V, E >, ByteMappedElement >
	{
		private UndoableEditList< V, E > pool;

		@Override
		public int getSizeInBytes()
		{
			return UndoableEditRef.SIZE_IN_BYTES;
		}

		@Override
		public UndoableEditRef< V, E > createEmptyRef()
		{
			return new UndoableEditRef< V, E >(	pool );
		}

		@Override
		public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
		{
			return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
		}
	};

	/**
	 * An {@link UndoableEdit} that can be cleared.
	 * This is used to remove non-ref edits from {@link UndoableEditList#nonRefEdits}.
	 */
	public interface ClearableUndoableEdit extends UndoableEdit
	{
		public void clear();
	}

	/**
	 * Represents a specific sub-type of {@link ClearableUndoableEdit},
	 * identified by a unique index.
	 *
	 * @param <T>
	 *            the {@link ClearableUndoableEdit} type.
	 */
	public interface UndoableEditType< V extends Vertex< E >, E extends Edge< V >, T extends ClearableUndoableEdit >
	{
		/**
		 * Get the unique index associated to {@code T}.
		 *
		 * @return the unique index associated to T.
		 */
		public int typeIndex();

		/**
		 * Create a {@link ClearableUndoableEdit} of type {@code T}.
		 *
		 * @param ref
		 *            the {@link UndoableEditRef} that will use the created
		 *            {@code T}.
		 * @return a new {@code T}.
		 */
		public T createInstance( final UndoableEditRef< V, E > ref );
	}

	/**
	 * Abstract base class for {@link ClearableUndoableEdit}s that has an
	 * {@link UndoableEditRef} to which it forwards. {@link #clear()} does
	 * nothing.
	 */
	protected abstract class AbstractClearableUndoableEdit implements ClearableUndoableEdit
	{
		protected final UndoableEditRef< V, E > ref;

		protected final byte typeIndex;

		protected AbstractClearableUndoableEdit( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			this.ref = ref;
			this.typeIndex = ( byte ) typeIndex;
		}

		public void init()
		{
			ref.setIsUndoPointField( false );
			ref.setTypeIndex( typeIndex );
		}

		@Override
		public boolean isUndoPoint()
		{
			return ref.getIsUndoPointField();
		}

		@Override
		public void setUndoPoint( final boolean isUndoPoint )
		{
			ref.setIsUndoPointField( isUndoPoint );
		}

		@Override
		public void clear()
		{}
	}

	private final int idgen = 0;

	private final TIntObjectArrayMap< UndoableEditType< V, E, ? > > undoableEditTypes = new TIntObjectArrayMap<>();

	/**
	 * Abstract base class for the {@link UndoableEditType}s of this {@link UndoableEditList}.
	 *
	 * @param <T>
	 *            the {@link AbstractClearableUndoableEdit} type.
	 */
	protected abstract class UndoableEditTypeImp< T extends AbstractClearableUndoableEdit > implements UndoableEditType< V, E, T >
	{
		private final int typeIndex;

		public UndoableEditTypeImp()
		{
			typeIndex = idgen;
			undoableEditTypes.put( typeIndex, this );
		}

		@Override
		public int typeIndex()
		{
			return typeIndex;
		}

		@Override
		public abstract T createInstance( final UndoableEditRef< V, E > ref );
	}

	UndoableEditType< V, E, ? > getUndoableEditType( final byte typeIndex )
	{
		return undoableEditTypes.get( typeIndex );
	}

	/*
	 * =========================================================================
	 *
	 *                        recording specific edits
	 *
	 * =========================================================================
	 */

	public void recordOther( final UndoableEdit undoableEdit )
	{
		final UndoableEditRef< V, E > ref = createRef();
		create( ref ).getEdit( other ).init( undoableEdit );
		releaseRef( ref );
	}

	protected final OtherType other = new OtherType();

	protected class OtherType extends UndoableEditTypeImp< Other >
	{
		@Override
		public Other createInstance( final UndoableEditRef< V, E > ref )
		{
			return new Other( ref, typeIndex() );
		}
	}

	private class Other extends AbstractClearableUndoableEdit
	{
		Other( final UndoableEditRef< V, E > ref, final int typeIndex )
		{
			super( ref, typeIndex );
		}

		public void init( final UndoableEdit edit )
		{
			super.init();
			ref.setDataIndex( nonRefEdits.size() );
			nonRefEdits.add( edit );
		}

		@Override
		public void redo()
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).redo();
		}

		@Override
		public void undo()
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).undo();
		}

		@Override
		public void setUndoPoint( final boolean isUndoPoint )
		{
			nonRefEdits.get( ( int ) ref.getDataIndex() ).setUndoPoint( isUndoPoint );
		}

		@Override
		public boolean isUndoPoint()
		{
			return nonRefEdits.get( ( int ) ref.getDataIndex() ).isUndoPoint();
		}

		@Override
		public void clear()
		{
			// THIS ONLY WORKS BECAUSE OF HOW clearFromIndex() IS IMPLEMENTED!
			nonRefEdits.remove( nonRefEdits.size() - 1 );
		}
	}
}
