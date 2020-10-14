package org.mastodon.views.bvv.scene;

import java.util.NoSuchElementException;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.collection.ref.RefIntHashMap;

// (to be reused for Ellipsoids and Cylinders)
public class CompactingPool< O extends ModifiableRef< O > >
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

	// TODO version with reusable ref
	public O get( final int key )
	{
		return keyToObj.get( key );
	}

	// TODO version with reusable ref
	public O getOrAdd( final int key )
	{
		O obj = get( key );
		if ( obj == null )
		{
			// create new value
			obj = pool.create( pool.createRef() );
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
		// TODO reusable refs
		final O obj = keyToObj.remove( key );
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
			final O lastObj = pool.getObject( size() - 1, pool.createRef() );
			obj.set( lastObj );
			final int lastKey = objToKey.remove( lastObj );
			objToKey.put( obj, lastKey );
			keyToObj.put( lastKey, obj );
			pool.delete( lastObj );
		}
	}
}
