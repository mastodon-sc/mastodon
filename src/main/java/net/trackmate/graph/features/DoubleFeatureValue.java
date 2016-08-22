package net.trackmate.graph.features;

import gnu.trove.map.TObjectDoubleMap;
import net.trackmate.graph.FeatureValue;

/**
 * Feature value for scalar, floating-point numbers based on
 * <code>double.</code>
 *
 * @param <O>
 *            type of object to which the feature should be attached.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class DoubleFeatureValue< O > implements FeatureValue< Double >
{
	private final TObjectDoubleMap< O > featureMap;

	private final O object;

	private final NotifyFeatureValueChange notify;

	protected DoubleFeatureValue( final TObjectDoubleMap< O > featureMap, final O object, final NotifyFeatureValueChange notify )
	{
		this.featureMap = featureMap;
		this.object = object;
		this.notify = notify;
	}

	@Override
	public void set( final Double value )
	{
		notify.notifyBeforeFeatureChange();
		if ( value == null )
			featureMap.remove( object );
		else
			featureMap.put( object, value.intValue() );
	}

	public void set( final double value )
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
	public Double get()
	{
		final double d = getDouble();
		return ( d == featureMap.getNoEntryValue() ) ? null : d;
	}

	public double getDouble()
	{
		return featureMap.get( object );
	}

	@Override
	public boolean isSet()
	{
		return featureMap.containsKey( object );
	}
}
