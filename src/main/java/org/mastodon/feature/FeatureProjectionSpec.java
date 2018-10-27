package org.mastodon.feature;

/**
 * Specifications for a feature projection.
 */
public class FeatureProjectionSpec
{

	/**
	 * The projection specification name.
	 */
	public final String projectionName;

	public final Dimension projectionDimension;

	public FeatureProjectionSpec( final String projectionName, final Dimension dimension )
	{
		this.projectionName = projectionName;
		this.projectionDimension = dimension;
	}

	/**
	 * Generates a feature projection key based on this specification name and
	 * on the specified source indices.
	 * 
	 * @param multiplicity
	 *            the multiplicity of the feature.
	 * @param sourceIndices
	 *            the source indices.
	 * @return they feature projection key.
	 * @throws IllegalArgumentException
	 *             if there is not enough source indices specified for the
	 *             multiplicity of this projection specification. For instance
	 *             the {@link Multiplicity#ON_SOURCES} requires the
	 *             specification of at least one source index.
	 */
	public String projectionKey( final Multiplicity multiplicity, final int... sourceIndices )
	{
		return projectionName + multiplicity.makeSuffix( sourceIndices );
	}

	@Override
	public String toString()
	{
		return "\"" + projectionName + "\" (dimension = " + projectionDimension + ")";
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( !( o instanceof FeatureProjectionSpec ) )
			return false;

		final FeatureProjectionSpec that = ( FeatureProjectionSpec ) o;

		if ( !projectionName.equals( that.projectionName ) )
			return false;
		if ( projectionDimension != that.projectionDimension )
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return projectionName.hashCode();
	}
}
