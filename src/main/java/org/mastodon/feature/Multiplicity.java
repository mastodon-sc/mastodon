package org.mastodon.feature;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static final Pattern SOURCE_PATTERN = Pattern.compile( " ch(?<digit1>\\d+)" );

	private static final Pattern SOURCE_PAIR_PATTERN = Pattern.compile( " ch(?<digit1>\\d+) ch(?<digit2>\\d+)" );

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

	/**
	 * Returns the name of a projection with this multiplicity from the specified
	 * key. For instance: "Mean ch1" with a multiplicity of {@link #ON_SOURCES}
	 * would return "Mean".
	 * 
	 * @param key
	 *                the projection key.
	 * @return the projection name.
	 */
	public String nameFromKey( final String key )
	{
		switch ( this )
		{
		case SINGLE:
			return key;
		case ON_SOURCES:
			final int l1 = key.lastIndexOf( " ch" );
			return key.substring( 0, l1 );
		case ON_SOURCE_PAIRS:
			final Matcher matcher = SOURCE_PAIR_PATTERN.matcher( key );
			if ( matcher.find() )
			{
				final int l2 = matcher.start();
				return key.substring( 0, l2 );
			}
			else
			{
				return key;
			}
		default:
			throw new IllegalArgumentException( "Unsupported multiplicity :" + this );
		}
	}

	/**
	 * Returns the indices stored in the specified projection key, according to this
	 * multiplicity. Warning, we assume that the key indices are 1-based, but the
	 * indices themselves are 0-based. For instance "Ratio ch3 ch9" will return
	 * <code>[2, 8]</code>. An empty array is returned if a match could not be
	 * found.
	 * 
	 * @param key
	 *                the projection key.
	 * @return the indices array.
	 */
	public int[] indicesFromKey( final String key )
	{
		switch ( this )
		{
		case SINGLE:
			return new int[] {};
		case ON_SOURCES:
			final Matcher matcher1 = SOURCE_PATTERN.matcher( key );
			if ( matcher1.find() )
			{
				final String digits = matcher1.group( "digit1" );
				return new int[] { Integer.parseInt( digits ) - 1 };
			}
			else
			{
				return new int[] {};
			}
		case ON_SOURCE_PAIRS:
			final Matcher matcher2 = SOURCE_PAIR_PATTERN.matcher( key );
			if ( matcher2.find() )
			{
				final String digits1 = matcher2.group( "digit1" );
				final String digits2 = matcher2.group( "digit2" );
				return new int[] { Integer.parseInt( digits1 ) - 1, Integer.parseInt( digits2 ) - 1 };
			}
			else
			{
				return new int[] {};
			}
		default:
			throw new IllegalArgumentException( "Unsupported multiplicity :" + this );
		}
	}
}
