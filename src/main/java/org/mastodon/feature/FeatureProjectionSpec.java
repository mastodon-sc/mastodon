package org.mastodon.feature;

import java.util.function.Function;

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

	/**
	 * The projection multiplicity.
	 */
	public final Multiplicity multiplicity;

	public static enum Multiplicity
	{
		/**
		 * For projections that do not have multiplicity.
		 */
		SINGLE( 0, ( indices ) -> "" ),
		/**
		 * Indicate projections that have one value per source present in the
		 * model.
		 */
		ON_SOURCES( 1, ( indices ) -> " ch" + ( indices[ 0 ] + 1 ) ),
		/**
		 * Indicate projections that have one value per source pair.
		 */
		ON_SOURCE_PAIRS( 2, ( indices ) -> " ch" + ( indices[ 0 ] + 1 ) + " ch" + ( indices[ 1 ] + 1 ) );

		private final int requiredNArgs;

		private final Function< int[], String > suffixGenerator;

		private Multiplicity( final int requiredNArgs, final Function< int[], String > suffixGenerator )
		{
			this.requiredNArgs = requiredNArgs;
			this.suffixGenerator = suffixGenerator;
		}

		public String makeSuffix( final int[] sourceIndices )
		{
			if ( sourceIndices.length < requiredNArgs )
				throw new IllegalArgumentException( "At least " + requiredNArgs
						+ " are required to build a name with feature projection multiplicity "
						+ this + ". Got " + sourceIndices.length + "." );

			return suffixGenerator.apply( sourceIndices );
		}
	}

	private FeatureProjectionSpec( final String projectionName, final Dimension dimension, final Multiplicity multiplicity )
	{
		this.projectionName = projectionName;
		this.projectionDimension = dimension;
		this.multiplicity = multiplicity;
	}

	/**
	 * Generates a feature projection key based on this specification name and
	 * on the specified source indices.
	 *
	 * @param sourceIndices
	 *            the source indices
	 * @return they feature projection key.
	 * @throws IllegalArgumentException
	 *             if there is not enough source indices specified for the
	 *             multiplicity of this projection specification. For instance
	 *             the {@link Multiplicity#ON_SOURCES} requires the
	 *             specification of at least one source index.
	 */
	public String projectionKey( final int... sourceIndices )
	{
		return projectionName + multiplicity.makeSuffix( sourceIndices );
	}

	/**
	 * Creates a feature projection specification for a projection without
	 * multiplicity. This signals a projection that has one value regardless of
	 * the number of sources.
	 *
	 * @param projectionName
	 *            the projection name.
	 * @param dimension
	 *            the feature projection dimension.
	 * @return a new feature projection specification.
	 */
	public static FeatureProjectionSpec standard( final String projectionName, final Dimension dimension )
	{
		return new FeatureProjectionSpec( projectionName, dimension, Multiplicity.SINGLE );
	}

	/**
	 * Creates a feature projection specification for a projection with source
	 * multiplicity. This signals a projection that can have one value per
	 * source.
	 *
	 * @param projectionName
	 *            the projection name.
	 * @param dimension
	 *            the feature projection dimension.
	 * @return a new feature projection specification.
	 */
	public static FeatureProjectionSpec onSources( final String projectionName, final Dimension dimension )
	{
		return new FeatureProjectionSpec( projectionName, dimension, Multiplicity.ON_SOURCES );
	}

	/**
	 * Creates a feature projection specification for a projection with
	 * source-pair multiplicity. This signals a projection that can have one
	 * value per source pair combination.
	 *
	 * @param projectionName
	 *            the projection name.
	 * @param dimension
	 *            the feature projection dimension.
	 * @return a new feature projection specification.
	 */
	public static FeatureProjectionSpec onSourcePairs( final String projectionName, final Dimension dimension )
	{
		return new FeatureProjectionSpec( projectionName, dimension, Multiplicity.ON_SOURCE_PAIRS );
	}

	@Override
	public String toString()
	{
		return "\"" + projectionName + "\" (dimension = " + projectionDimension + ", multiplicity = " + multiplicity + ")";
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
		return multiplicity == that.multiplicity;
	}

	@Override
	public int hashCode()
	{
		return projectionName.hashCode();
	}
}
