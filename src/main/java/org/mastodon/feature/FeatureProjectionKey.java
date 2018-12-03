package org.mastodon.feature;

import java.util.Arrays;

/**
 * Links a {@link FeatureProjectionSpec} to source indices (according to the
 * features {@link Multiplicity}).
 * <p>
 * Can be used to {@link Feature#project(FeatureProjectionKey) retrieve} a projection from a {@link Feature}.
 */
public final class FeatureProjectionKey
{
	/**
	 * Construct a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#SINGLE}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec )
	{
		return new FeatureProjectionKey( spec );
	}

	/**
	 * Constructs a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#ON_SOURCES}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @param i0
	 *            the source index.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec, final int i0 )
	{
		return new FeatureProjectionKey( spec, i0 );
	}

	/**
	 * Constructs a {@link FeatureProjectionKey} with
	 * {@link Multiplicity#ON_SOURCE_PAIRS}.
	 * 
	 * @param spec
	 *            the feature projection spec.
	 * @param i0
	 *            the first source index.
	 * @param i1
	 *            the second source index.
	 * @return a new {@link FeatureProjectionKey}.
	 */
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec, final int i0, final int i1 )
	{
		return new FeatureProjectionKey( spec, i0, i1 );
	}

	private final FeatureProjectionSpec spec;

	private final int[] sourceIndices;

	private FeatureProjectionKey( final FeatureProjectionSpec spec, final int... sourceIndices )
	{
		this.spec = spec;
		this.sourceIndices = ( sourceIndices == null ) ? new int[ 0 ] : sourceIndices;
	}

	public FeatureProjectionSpec getSpec()
	{
		return spec;
	}

	public int[] getSourceIndices()
	{
		return sourceIndices;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder( spec.getKey() );
		for ( int sourceIndex : sourceIndices )
		{
			sb.append( " ch" );
			sb.append( sourceIndex );
		}
		return sb.toString();
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o instanceof FeatureProjectionKey )
		{
			final FeatureProjectionKey key = ( FeatureProjectionKey ) o;
			return spec.equals( key.spec ) && Arrays.equals( sourceIndices, key.sourceIndices );
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int result = spec.hashCode();
		result = 31 * result + Arrays.hashCode( sourceIndices );
		return result;
	}
}
