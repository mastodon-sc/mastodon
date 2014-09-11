package net.trackmate.model.abstractmodel;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.util.HashMap;

public class AdditionalFeatures
{
	public static class Feature
	{
		public double value;

		public boolean exists;
	}

	private final int initialCapacity;

	private final HashMap< String, TIntDoubleMap > featureMaps;

	public AdditionalFeatures( final int initialCapacity )
	{
		this.initialCapacity = initialCapacity;
		featureMaps = new HashMap< String, TIntDoubleMap >();
	}

	public void putFeature( final String feature, final double value, final int index )
	{
		TIntDoubleMap featureMap = featureMaps.get( feature );
		if ( featureMap == null )
		{
			featureMap = new TIntDoubleHashMap( initialCapacity );
			featureMaps.put( feature, featureMap );
		}
		featureMap.put( index, value );
	}

	public Feature getFeature( final String feature, final int index, final Feature value )
	{
		final TIntDoubleMap featureMap = featureMaps.get( feature );
		if ( featureMap == null )
			value.exists = false;
		else
		{
			value.value = featureMap.get( index );
			if ( value.value == featureMap.getNoEntryValue() )
				value.exists = featureMap.containsKey( index );
			else
				value.exists = true;
		}
		return value;
	}

	public Double getFeature( final String feature, final int index )
	{
		final Feature value = getFeature( feature, index, new Feature() );
		return value.exists ? value.value : null;
	}
}
