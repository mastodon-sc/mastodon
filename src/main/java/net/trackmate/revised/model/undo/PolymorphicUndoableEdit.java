package net.trackmate.revised.model.undo;

import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import gnu.trove.impl.Constants;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;

import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.Ref;
import net.trackmate.graph.RefPool;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.ByteMappedElementArray;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.SingleArrayMemPool;

/**
 * See {@link PolymorphicUndoableEditList}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class PolymorphicUndoableEdit
	extends PoolObject< PolymorphicUndoableEdit, ByteMappedElement >
	implements UndoableEdit
{
	private static final int LIST_INDEX_OFFSET = 0;
	private static final int TYPE_INDEX_OFFSET = LIST_INDEX_OFFSET + INT_SIZE;
	private static final int SIZE_IN_BYTES = TYPE_INDEX_OFFSET + INT_SIZE;

	private final ArrayList< Type< ? > > types;

	private final TObjectIntMap< Class< ? > > classToTypeIndex;

	private final ArrayList< UndoableEdit > nonRefEdits;

	PolymorphicUndoableEdit(
			final Pool< PolymorphicUndoableEdit, ByteMappedElement > pool,
			final ArrayList< Type< ? > > types,
			final TObjectIntMap< Class< ? > > classToTypeIndex,
			final ArrayList< UndoableEdit > nonRefEdits )
	{
		super( pool );
		this.types = types;
		this.classToTypeIndex = classToTypeIndex;
		this.nonRefEdits = nonRefEdits;
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public < T extends UndoableEdit > PolymorphicUndoableEdit init( final T e )
	{
		if ( e instanceof AbstractUndoableEditRef )
			initRef( ( AbstractUndoableEditRef ) e );
		else
		{
			nonRefEdits.add( e );
			setTypeIndex( -1 );
			setListIndex( nonRefEdits.size() - 1 );
		}
		return this;
	}

	private < T extends AbstractUndoableEditRef< T > > PolymorphicUndoableEdit initRef( final T e )
	{
		int i = classToTypeIndex.get( e.getClass() );
		if ( i < 0 )
			i = classToTypeIndex.get( UndoableEdit.class );
		@SuppressWarnings( "unchecked" )
		final Type< T > type = ( Type< T > ) types.get( i );
		type.edits.add( e );
		setTypeIndex( i );
		setListIndex( type.edits.size() - 1 );
		return this;
	}

	@Override
	public void undo()
	{
		final int ti = getTypeIndex();
		if ( ti < 0 )
			nonRefEdits.get( getListIndex() ).undo();
		else
			types.get( ti ).undo( getListIndex() );
	}

	@Override
	public void redo()
	{
		final int ti = getTypeIndex();
		if ( ti < 0 )
			nonRefEdits.get( getListIndex() ).redo();
		else
			types.get( ti ).redo( getListIndex() );
	}

	@Override
	public boolean isUndoPoint()
	{
		final int ti = getTypeIndex();
		if ( ti < 0 )
			return nonRefEdits.get( getListIndex() ).isUndoPoint();
		else
			return types.get( ti ).isUndoPoint( getListIndex() );
	}

	@Override
	public void setUndoPoint( final boolean isUndoPoint )
	{
		final int ti = getTypeIndex();
		if ( ti < 0 )
			nonRefEdits.get( getListIndex() ).setUndoPoint( isUndoPoint );
		else
			types.get( ti ).setUndoPoint( getListIndex(), isUndoPoint );
	}

	private int getTypeIndex()
	{
		return access.getInt( TYPE_INDEX_OFFSET );
	}

	private void setTypeIndex( final int typeIndex )
	{
		access.putInt( typeIndex, TYPE_INDEX_OFFSET );
	}

	private int getListIndex()
	{
		return access.getInt( LIST_INDEX_OFFSET );
	}

	private void setListIndex( final int listIndex )
	{
		access.putInt( listIndex, LIST_INDEX_OFFSET );
	}

	@Override
	protected void setToUninitializedState()
	{}

	private void clear()
	{
		final int ti = getTypeIndex();
		if ( ti < 0 )
			// THIS IS A HACK THAT ONLY WORKS BECAUSE OF HOW clearFromIndex() IS IMPLEMENTED!
			nonRefEdits.remove( nonRefEdits.size() - 1 );
		else
			types.get( ti ).clear( getListIndex() );
	}

	/**
	 * A list of {@link PolymorphicUndoableEdit} stored in a {@link RefPool}.
	 * <p>
	 * Each edit in the pool wraps a {@link UndoableEdit} of polymorphic type.
	 * If wrapped edits are {@link Ref}s themselves, a {@link RefPool} for each
	 * type must be {@link #registerPool(RefPool) registered} with the
	 * {@link PolymorphicUndoableEditList}.
	 * <p>
	 * To append a new edit to the end of the list, call
	 * {@link #create(PolymorphicUndoableEdit)} and then
	 * {@link PolymorphicUndoableEdit#init(UndoableEdit)} with the edit that
	 * should be wrapped.
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static class PolymorphicUndoableEditList extends Pool< PolymorphicUndoableEdit, ByteMappedElement >
	{
		private final ArrayList< Type< ? > > types;

		private final TObjectIntMap< Class< ? > > classToTypeIndex;

		private final ArrayList< UndoableEdit > nonRefEdits;

		public PolymorphicUndoableEditList( final int initialCapacity )
		{
			this( initialCapacity, new Factory() );
		}

		public < T extends AbstractUndoableEditRef< T > > void registerPool( final RefPool< T > pool )
		{
			final Type< ? > type = new Type<>( pool );
			types.add( type );
			classToTypeIndex.put( type.clazz, types.size() - 1 );
		}

		@Override
		public PolymorphicUndoableEdit create( final PolymorphicUndoableEdit ref )
		{
			return super.create( ref );
		}

		public PolymorphicUndoableEdit get( final int index, final PolymorphicUndoableEdit ref )
		{
			super.getByInternalPoolIndex( index, ref );
			return ref;
		}

		public void clearFromIndex( final int fromIndex )
		{
			final PolymorphicUndoableEdit ref = createRef();
			for ( int i = super.size() - 1; i >= fromIndex; --i )
			{
				getByInternalPoolIndex( i, ref );
				ref.clear();
				deleteByInternalPoolIndex( i );
			}
			releaseRef( ref );
		}

		private PolymorphicUndoableEditList( final int initialCapacity, final Factory f )
		{
			super( initialCapacity, f );
			f.pool = this;
			types = f.types;
			classToTypeIndex = f.classToTypeIndex;
			nonRefEdits = f.nonRefEdits;
		}

		private static class Factory implements PoolObject.Factory< PolymorphicUndoableEdit, ByteMappedElement >
		{
			private PolymorphicUndoableEditList pool;

			private final ArrayList< Type< ? > > types = new ArrayList<>();

			private final TObjectIntMap< Class< ? > > classToTypeIndex = new TObjectIntHashMap<>( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1 );

			private final ArrayList< UndoableEdit > nonRefEdits = new ArrayList<>();

			@Override
			public int getSizeInBytes()
			{
				return PolymorphicUndoableEdit.SIZE_IN_BYTES;
			}

			@Override
			public PolymorphicUndoableEdit createEmptyRef()
			{
				return new PolymorphicUndoableEdit( pool, types, classToTypeIndex, nonRefEdits  );
			}

			@Override
			public MemPool.Factory< ByteMappedElement > getMemPoolFactory()
			{
				return SingleArrayMemPool.factory( ByteMappedElementArray.factory );
			}
		};
	}

	static class Type< T extends AbstractUndoableEditRef< T > >
	{
		private final RefPool< T > pool;
		private final RefList< T > edits;
		private final Class< ? > clazz;

		Type( final RefPool< T > pool )
		{
			this.pool = pool;
			this.edits = new PoolObjectList< T >( pool );
			final T ref = edits.createRef();
			clazz = ref.getClass();
			edits.releaseRef( ref );
		}

		void undo( final int index )
		{
			final T ref = pool.createRef();
			edits.get( index, ref ).undo();
			pool.releaseRef( ref );
		}

		void redo( final int index )
		{
			final T ref = pool.createRef();
			edits.get( index, ref ).redo();
			pool.releaseRef( ref );
		}

		boolean isUndoPoint( final int index )
		{
			final T ref = pool.createRef();
			final boolean b = edits.get( index, ref ).isUndoPoint();
			pool.releaseRef( ref );
			return b;
		}

		void setUndoPoint( final int index, final boolean isUndoPoint )
		{
			final T ref = pool.createRef();
			edits.get( index, ref ).setUndoPoint( isUndoPoint );
			pool.releaseRef( ref );
		}

		void clear( final int index )
		{
			final T ref = pool.createRef();
			edits.get( index, ref ).clear();
			pool.releaseRef( ref );
		}
	}
}
