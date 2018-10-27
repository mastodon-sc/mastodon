package org.mastodon.feature;

import java.util.function.Function;

public enum Multiplicity
{
	/**
	 * For projections that do not have multiplicity.
	 */
	SINGLE( 0, ( indices ) -> "" ),
	/**
	 * Indicate projections that have one value per source present in the model.
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
