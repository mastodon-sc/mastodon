package net.trackmate.graph.features;

import java.util.Map;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.graph.features.unify.UndoFeatureMap;

final class ObjUndoFeatureMap< O, T > implements UndoFeatureMap< O >
{
	private static final int NO_ENTRY_KEY = -1;

	private final Map< O, T > featureMap;

	private final TIntObjectMap< T > undoMap;

	protected ObjUndoFeatureMap( final Map< O, T > featureMap )
	{
		this.featureMap = featureMap;
		undoMap = new TIntObjectHashMap<>( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY );
	}

	@Override
	public void store( final int undoId, final O obj )
	{
		final T value = featureMap.get( obj );
		if ( value != null )
			undoMap.put( undoId, value );
	}

	@Override
	public void retrieve( final int undoId, final O obj )
	{
		final T value = undoMap.get( undoId );
		if ( value != null )
			featureMap.put( obj, value );
		else
			featureMap.remove( obj );
	}

	@Override
	public void swap( final int undoId, final O obj )
	{
		final T undoValue = undoMap.get( undoId );
		final T featureValue = featureMap.get( obj );
		if ( featureValue != null )
			undoMap.put( undoId, featureValue );
		else
			undoMap.remove( undoId );
		if ( undoValue != null )
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
