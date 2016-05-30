package net.trackmate.graph.features;

import gnu.trove.map.TObjectIntMap;
import net.trackmate.graph.FeatureValue;

/**
 * TODO
 *
 * @param <O>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class IntFeatureValue< O > implements FeatureValue< Integer >
{
	private final TObjectIntMap< O > featureMap;

	private final O object;

	private final NotifyFeatureValueChange notify;

	protected IntFeatureValue( final TObjectIntMap< O > featureMap, final O object, final NotifyFeatureValueChange notify )
	{
		this.featureMap = featureMap;
		this.object = object;
		this.notify = notify;
	}

	@Override
	public void set( final Integer value )
	{
		notify.notifyBeforeFeatureChange();
		if ( value == null )
			featureMap.remove( object );
		else
			featureMap.put( object, value.intValue() );
	}

	public void set( final int value )
	{
		notify.notifyBeforeFeatureChange();
		featureMap.put( object, value );
	}

	@Override
	public void remove()
	{
		notify.notifyBeforeFeatureChange();
		featureMap.remove( object );
	}

	@Override
	public Integer get()
	{
		final int i = getInt();
		return ( i == featureMap.getNoEntryValue() ) ? null : i;
	}

	public int getInt()
	{
		return featureMap.get( object );
	}

	@Override
	public boolean isSet()
	{
		return featureMap.containsKey( object );
	}
}
