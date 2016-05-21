package net.trackmate.collection.wrap;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectProcedure;
import net.trackmate.collection.RefDoubleMap;

public class RefDoubleMapWrapper< K > extends TObjectDoubleHashMap< K > implements RefDoubleMap< K >
{
	/*
	 * This is not exactly a wrapper, since TROVE provides the right mother
	 * class, but we stick to the naming convention.
	 */

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	public RefDoubleMapWrapper( final double noEntryValue, final int initialCapacity )
	{
		super( initialCapacity, DEFAULT_LOAD_FACTOR, noEntryValue );
	}

	public RefDoubleMapWrapper( final double noEntryValue )
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
	public boolean forEachEntry( final TObjectDoubleProcedure< ? super K > procedure, final K ref )
	{
		return forEachEntry( procedure );
	}

	@Override
	public boolean retainEntries( final TObjectDoubleProcedure< ? super K > procedure, final K ref )
	{
		return retainEntries( procedure );
	}

}
