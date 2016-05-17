package net.trackmate.collection.wrap;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.collection.IntRefMap;

public class IntRefMapWrapper< K > extends TIntObjectHashMap< K > implements IntRefMap< K >
{
	/*
	 * This is not exactly a wrapper, since TROVE provides the right mother
	 * class, but we stick to the naming convention.
	 */

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	public IntRefMapWrapper( final int initialCapacity, final int noEntryKey )
	{
		super( initialCapacity, DEFAULT_LOAD_FACTOR, noEntryKey );
	}

	public IntRefMapWrapper( final int noEntryKey )
	{
		this( Constants.DEFAULT_CAPACITY, noEntryKey );
	}

	@Override
	public K createRef()
	{
		return null;
	}

	@Override
	public void releaseRef( final K obj )
	{}

	@Override
	public K get( final int key, final K obj )
	{
		return get( key );
	}

	@Override
	public K put( final int key, final K value, final K obj )
	{
		return put( key, value );
	}

	@Override
	public K putIfAbsent( final int key, final K value, final K obj )
	{
		return put( key, value );
	}

	@Override
	public K remove( final int key, final K obj )
	{
		return remove( key );
	}
}
