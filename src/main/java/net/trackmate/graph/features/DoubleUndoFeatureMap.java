package net.trackmate.graph.features;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import net.trackmate.graph.UndoFeatureMap;

public final class DoubleUndoFeatureMap< K > implements UndoFeatureMap< K >
{
	private static final int NO_ENTRY_KEY = -1;

	private final TObjectDoubleMap< K > featureMap;

	private final double noEntryValue;

	private final TIntDoubleMap undoMap;

	protected DoubleUndoFeatureMap( final TObjectDoubleMap< K > featureMap, final double noEntryValue )
	{
		this.featureMap = featureMap;
		this.noEntryValue = noEntryValue;
		undoMap = new TIntDoubleHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, noEntryValue );
	}

	@Override
	public void store( final int undoId, final K obj )
	{
		final double value = featureMap.get( obj );
		if ( value != noEntryValue )
			undoMap.put( undoId, value );
	}

	@Override
	public void retrieve( final int undoId, final K obj )
	{
		final double value = undoMap.get( undoId );
		if ( value != noEntryValue )
			featureMap.put( obj, value );
		else
			featureMap.remove( obj );
	}

	@Override
	public void swap( final int undoId, final K obj )
	{
		final double undoValue = undoMap.get( undoId );
		final double featureValue = featureMap.get( obj );
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