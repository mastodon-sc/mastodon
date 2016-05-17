package net.trackmate.collection.wrap;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import net.trackmate.collection.RefIntMap;

public class RefIntMapWrapper< K > extends TObjectIntHashMap< K > implements RefIntMap< K >
{
	/*
	 * This is not exactly a wrapper, since TROVE provides the right mother
	 * class, but we stick to the naming convention.
	 */

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	public RefIntMapWrapper( final int initialCapacity, final int noEntryValue )
	{
		super( initialCapacity, DEFAULT_LOAD_FACTOR, noEntryValue );
	}

	public RefIntMapWrapper( final int noEntryValue )
	{
		this( Constants.DEFAULT_CAPACITY, noEntryValue );
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
