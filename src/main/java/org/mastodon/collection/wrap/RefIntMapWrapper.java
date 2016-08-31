package org.mastodon.collection.wrap;

import org.mastodon.collection.RefIntMap;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

public class RefIntMapWrapper< K > extends TObjectIntHashMap< K > implements RefIntMap< K >
{
	/*
	 * This is not exactly a wrapper, since TROVE provides the right mother
	 * class, but we stick to the naming convention.
	 */

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	public RefIntMapWrapper( final int noEntryValue, final int initialCapacity )
	{
		super( initialCapacity, DEFAULT_LOAD_FACTOR, noEntryValue );
	}

	public RefIntMapWrapper( final int noEntryValue )
	{
		this( noEntryValue, Constants.DEFAULT_CAPACITY );
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
	public boolean forEachKey( final TObjectProcedure< ? super K > procedure, final K ref )
	{
		return forEachKey( procedure );
	}

	@Override
	public boolean forEachEntry( final TObjectIntProcedure< ? super K > procedure, final K ref )
	{
		return forEachEntry( procedure );
	}

	@Override
	public boolean retainEntries( final TObjectIntProcedure< ? super K > procedure, final K ref )
	{
		return retainEntries( procedure );
	}

}
