package org.mastodon.revised.ui.coloring.feature.revised;

import org.mastodon.feature.Multiplicity;

public final class FeatureProjectionId
{
	final private String featureKey;

	final private String projectionKey;

	final private int i0;

	final private int i1;

	public FeatureProjectionId( final String featureKey, final String projectionKey )
	{
		this( featureKey, projectionKey, -1, -1 );
	}

	public FeatureProjectionId( final String featureKey, final String projectionKey, final int i0 )
	{
		this( featureKey, projectionKey, i0, -1 );
	}

	public FeatureProjectionId( final String featureKey, final String projectionKey, final int i0, final int i1 )
	{
		this.featureKey = featureKey;
		this.projectionKey = projectionKey;
		this.i0 = i0;
		this.i1 = i1;
	}

	public String getFeatureKey()
	{
		return featureKey;
	}

	public String getProjectionKey()
	{
		return projectionKey;
	}

	public int getI0()
	{
		return i0;
	}

	public int getI1()
	{
		return i1;
	}

	public Multiplicity getMultiplicity()
	{
		if ( i0 < 0 )
			return Multiplicity.SINGLE;
		else if ( i1 < 0 )
			return Multiplicity.ON_SOURCES;
		else
			return Multiplicity.ON_SOURCE_PAIRS;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "FeatureProjectionId{" );
		sb.append( "featureKey='" ).append( featureKey ).append( '\'' );
		sb.append( ", projectionKey='" ).append( projectionKey ).append( '\'' );
		sb.append( ", multiplicity=" ).append( getMultiplicity() );
		sb.append( ", i0=" ).append( i0 );
		sb.append( ", i1=" ).append( i1 );
		sb.append( '}' );
		return sb.toString();
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( !( o instanceof FeatureProjectionId ) )
			return false;

		final FeatureProjectionId that = ( FeatureProjectionId ) o;

		if ( i0 >= 0 && that.i0 >= 0 && i0 != that.i0 )
			return false;
		if ( i1 >= 0 && that.i1 >= 0 && i1 != that.i1 )
			return false;
		if ( !featureKey.equals( that.featureKey ) )
			return false;
		return projectionKey.equals( that.projectionKey );
	}

	@Override
	public int hashCode()
	{
		int result = featureKey.hashCode();
		result = 31 * result + projectionKey.hashCode();
		result = 31 * result + ( i0 >= 0 ? i0 : -1 );
		result = 31 * result + ( i1 >= 0 ? i1 : -1 );
		return result;
	}
}
