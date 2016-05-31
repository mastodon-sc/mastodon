package net.trackmate.graph.features;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

final class IntUndoFeatureMap< O > implements UndoFeatureMap< O >
{
	private static final int NO_ENTRY_KEY = -1;

	private final TObjectIntMap< O > featureMap;

	private final int noEntryValue;

	private final TIntIntMap undoMap;

	protected IntUndoFeatureMap( final TObjectIntMap< O > featureMap, final int noEntryValue )
	{
		this.featureMap = featureMap;
		this.noEntryValue = noEntryValue;
		undoMap = new TIntIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, noEntryValue );
	}

	@Override
	public void store( final int undoId, final O obj )
	{
		final int value = featureMap.get( obj );
		if ( value != noEntryValue )
			undoMap.put( undoId, value );
	}

	@Override
	public void retrieve( final int undoId, final O obj )
	{
		final int value = undoMap.get( undoId );
		if ( value != noEntryValue )
			featureMap.put( obj, value );
		else
			featureMap.remove( obj );
	}

	@Override
	public void swap( final int undoId, final O obj )
	{
		final int undoValue = undoMap.get( undoId );
		final int featureValue = featureMap.get( obj );
		if ( featureValue != noEntryValue )
			undoMap.put( undoId, featureValue );
		else
			undoMap.remove( undoId );
		if ( undoValue != noEntryValue )
			featureMap.put( obj, undoValue );
		else
			featureMap.remove( obj );

	}

	@Override
	public void clear( final int undoId )
	{
		undoMap.remove( undoId );
	}
}
