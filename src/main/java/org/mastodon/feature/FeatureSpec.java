package org.mastodon.feature;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.scijava.plugin.SciJavaPlugin;

/**
 * Specification for a feature {@code F}.
 *
 * @param <F>
 *            the concrete feature type.
 * @param <T>
 *            the target type (<i>e.g.</i> Spot).
 */
public abstract class FeatureSpec< F extends Feature< T >, T > implements SciJavaPlugin
{
	private final String key;

	private final HashSet< FeatureProjectionSpec > projectionSpecs;

	private final Class< F > featureClass;

	private final Class< T > targetClass;

	private final String info;

	/**
	 * The feature multiplicity.
	 */
	private final Multiplicity multiplicity;

	protected FeatureSpec(
			final String key,
			final String info,
			final Class< F > featureClass,
			final Class< T > targetClass,
			final Multiplicity multiplicity,
			final FeatureProjectionSpec... projectionSpecs )
	{
		this.key = key;
		this.info = info;
		this.featureClass = featureClass;
		this.targetClass = targetClass;
		this.multiplicity = multiplicity;
		this.projectionSpecs = new HashSet<>( Arrays.asList( projectionSpecs ) );
	}

	/**
	 * Get an info string describing the feature (to be displayed in UI, for
	 * example).
	 *
	 * @return info string.
	 */
	public String getInfo()
	{
		return info;
	}

	public String getKey()
	{
		return key;
	}

	public Set< FeatureProjectionSpec > getProjectionSpecs()
	{
		return projectionSpecs;
	}

	public Class< F > getFeatureClass()
	{
		return featureClass;
	}

	public Class< T > getTargetClass()
	{
		return targetClass;
	}

	public Multiplicity getMultiplicity()
	{
		return multiplicity;
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

		// Don't test for feature projection.
		return key.equals( that.key )
				&& featureClass.equals( that.featureClass )
				&& targetClass.equals( that.targetClass );
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	/**
	 * Returns <code>true</code> when a feature should be selected by default in
	 * a user interface. It is a good idea override the default implementation,
	 * that returns <code>true</code>, when a feature computer is known to take
	 * very long to compute.
	 *
	 * @return whether a feature should be selected by default in an user
	 *         interface.
	 */
	public boolean isDefaultSelected()
	{
		return true;
	}
}
