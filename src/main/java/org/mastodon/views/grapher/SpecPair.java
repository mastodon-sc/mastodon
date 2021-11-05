package org.mastodon.views.grapher;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;

public class SpecPair implements Comparable< SpecPair >
{

	final FeatureSpec< ?, ? > featureSpec;

	final FeatureProjectionSpec projectionSpec;

	private final int c1;

	private final int c2;

	public SpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps )
	{
		assert f.getMultiplicity() == Multiplicity.SINGLE;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = -1;
		this.c2 = -1;
	}

	public SpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1 )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCES;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = -1;
	}

	public SpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1, final int c2 )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public int compareTo( final SpecPair o )
	{
		if ( featureSpec == null || projectionSpec == null )
			return 1;

		final int c = featureSpec.getKey().compareTo( o.featureSpec.getKey() );
		if ( c != 0 )
			return c;

		return projectionSpec.getKey().compareTo( o.projectionSpec.getKey() );
	}

	@Override
	public String toString()
	{
		if ( featureSpec == null || projectionSpec == null )
			return "";

		final int size = featureSpec.getProjectionSpecs().size();
		if ( size == 1 )
			return featureSpec.getKey();

		return featureSpec.getKey() + " - " + projectionSpec.getKey();
	}

	public < O > FeatureProjection< O > getProjection( final FeatureModel featureModel )
	{
		final Feature< ? > feature = featureModel.getFeature( featureSpec );
		final Multiplicity multiplicity = feature.getSpec().getMultiplicity();
		final FeatureProjectionKey key;
		switch ( multiplicity )
		{
		case ON_SOURCES:
			key = FeatureProjectionKey.key( projectionSpec, c1 );
			break;
		case ON_SOURCE_PAIRS:
			key = FeatureProjectionKey.key( projectionSpec, c1, c2 );
			break;
		case SINGLE:
		default:
			key = FeatureProjectionKey.key( projectionSpec );
			break;
		}
		@SuppressWarnings( "unchecked" )
		final FeatureProjection< O > projection = ( FeatureProjection< O > ) feature.project( key );
		return projection;
	}
}
