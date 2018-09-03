package org.mastodon.feature;

import java.util.Arrays;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Specification for a feature F.
 *
 * @param <F>
 *            the concrete feature type.
 * @param <T>
 *            the target type (<i>e.g.</i> Spot).
 */
public abstract class FeatureSpec< F extends Feature< T >, T > implements SciJavaPlugin
{
	private final String key;

	private final String[] projections;

	private final Class< F > featureClass;

	private final Class< T > targetClass;

	protected FeatureSpec(
			final String key,
			final Class< F > featureClass,
			final Class< T > targetClass,
			final String... projections )
	{
		this.key = key;
		this.featureClass = featureClass;
		this.targetClass = targetClass;
		this.projections = projections;
	}

	public String getKey()
	{
		return key;
	}

	public String[] getProjections()
	{
		return projections;
	}

	public Class< F > getFeatureClass()
	{
		return featureClass;
	}

	public Class< T > getTargetClass()
	{
		return targetClass;
	}

	@Override
	public String toString()
	{
		return "\"" + getKey() + "\" (feature = " + getFeatureClass() + ", target = " + getTargetClass() + ")";
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( !( o instanceof FeatureSpec ) )
			return false;
		final FeatureSpec< ?, ? > that = ( FeatureSpec< ?, ? > ) o;
		return key.equals( that.key )
				&& Arrays.equals( projections, that.projections )
				&& featureClass.equals( that.featureClass )
				&& targetClass.equals( that.targetClass );
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
