package org.mastodon.views.bvv.scene;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.collection.ref.RefIntHashMap;

// (to be reused for Ellipsoids and Cylinders)
public class CompactingPool< O extends ModifiableRef< O > > implements Iterable< O >
{
	private final ModifiableRefPool< O > pool;

	/**
	 * Int value used to declare that the requested key is not in the map.
	 * Negative, so that it cannot be an index in the pool.
	 */
	private static final int NO_ENTRY_KEY = -1;

	/**
	 * Int value used to declare that the requested value is not in the map.
	 * Negative, so that it cannot be an index in the pool.
	 */
	private static final int NO_ENTRY_VALUE = -2;

	// TODO: extract RefIntBimap (?)
	private final IntRefMap< O > keyToObj;
	private final RefIntMap< O > objToKey;

	public CompactingPool( final ModifiableRefPool< O > pool, final int initialCapacity )
	{
		this.pool = pool;
		keyToObj = new IntRefHashMap<>( pool, NO_ENTRY_KEY, initialCapacity );
		objToKey = new RefIntHashMap<>( pool, NO_ENTRY_VALUE, initialCapacity );
	}

	/**
	 * Generates an object reference.
	 *
	 * @return a new, uninitialized, reference object.
	 */
	public O createRef()
	{
		return pool.createRef();
	}

	/**
	 * Releases a previously created reference object.
	 *
	 * @param obj
	 *            the reference object to release.
	 */
	public void releaseRef( final O obj )
	{
		pool.releaseRef( obj );
	}

	public O get( final int key )
	{
		return get( key, pool.createRef() );
	}

	public O get( final int key, final O ref )
	{
		return keyToObj.get( key, ref );
	}

	public O getOrAdd( final int key )
	{
		return getOrAdd( key, pool.createRef() );
	}

	public O getOrAdd( final int key, final O ref )
	{
		O obj = get( key, ref );
		if ( obj == null )
		{
			// create new value
			obj = pool.create( ref );
			keyToObj.put( key, obj );
			objToKey.put( obj, key );
		}
		return obj;
	}

	public int size()
	{
		return pool.size();
	}

	public void remove( final int key )
	{
		final O ref = createRef();
		final O ref2 = createRef();
		try
		{
			final O obj = keyToObj.remove( key, ref );
			if ( obj == null )
				throw new NoSuchElementException();
			if ( pool.getId( obj ) == size() - 1 )
			{
				objToKey.remove( obj );
				pool.delete( obj );
			}
			else
			{
				// swap with last, and remove last
				final O lastObj = pool.getObject( size() - 1, ref2 );
				obj.set( lastObj );
				final int lastKey = objToKey.remove( lastObj );
				objToKey.put( obj, lastKey );
				keyToObj.put( lastKey, obj );
				pool.delete( lastObj );
			}
		}
		finally
		{
			releaseRef( ref );
			releaseRef( ref2 );
		}
	}

	/**
	 * Get key of obj.
	 * If obj is not present (shouldn't happen except for stale refs), {@link #NO_ENTRY_VALUE} is returned.
	 */
	public int keyOf( O obj )
	{
		return objToKey.get( obj );
	}

	@Override
	public Iterator< O > iterator()
	{
		return iterator( createRef() );
	}

	// garbage-free version
	public Iterator< O > iterator( final O obj )
	{
		return pool.iterator( obj );
	}
}
