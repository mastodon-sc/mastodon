package org.mastodon.feature;

import java.util.Arrays;

public final class FeatureProjectionKey
{
	public static FeatureProjectionKey key( final FeatureProjectionSpec spec, final int... sourceIndices )
	{
		return new FeatureProjectionKey( spec, sourceIndices );
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

	@Override
	public String toString()
	{
		return spec.projectionKey( sourceIndices );
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
