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

	public FeatureProjectionSpec( final String projectionName )
	{
		this( projectionName, Dimension.NONE );
	}

	public FeatureProjectionSpec( final String projectionName, final Dimension dimension )
	{
		this.projectionName = projectionName;
		this.projectionDimension = dimension;
	}

	public String getKey()
	{
		return projectionName;
	}

	/**
	 * Generates a feature projection key based on this specification name and
	 * on the specified source indices.
	 *
	 * @param sourceIndices
	 *            the source indices.
	 * @return they feature projection key.
	 */
	public String projectionKey( final int... sourceIndices )
	{
		StringBuilder sb = new StringBuilder( projectionName );
		for ( int sourceIndex : sourceIndices )
		{
			sb.append( " ch" );
			sb.append( sourceIndex );
		}
		return sb.toString();
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
