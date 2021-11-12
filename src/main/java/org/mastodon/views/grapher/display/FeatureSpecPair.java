package org.mastodon.views.grapher.display;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;

/**
 * Data class representing a selection of a feature plus a projection in said
 * feature, plus possibly a selection of the source indices in case the
 * multiplicity is not SINGLE.
 * 
 * @author Jean-Yves Tinevez
 */
public class FeatureSpecPair implements Comparable< FeatureSpecPair >
{

	final FeatureSpec< ?, ? > featureSpec;

	final FeatureProjectionSpec projectionSpec;

	private final int c1;

	private final int c2;

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps )
	{
		assert f.getMultiplicity() == Multiplicity.SINGLE;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = -1;
		this.c2 = -1;
	}

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1 )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCES;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = -1;
	}

	public FeatureSpecPair( final FeatureSpec< ?, ? > f, final FeatureProjectionSpec ps, final int c1, final int c2 )
	{
		assert f.getMultiplicity() == Multiplicity.ON_SOURCE_PAIRS;
		this.featureSpec = f;
		this.projectionSpec = ps;
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public int compareTo( final FeatureSpecPair o )
	{
		if ( featureSpec == null || projectionSpec == null )
			return 1;

		final int c = featureSpec.getKey().compareTo( o.featureSpec.getKey() );
		if ( c != 0 )
			return c;

		return projectionKey().toString().compareTo( o.projectionKey().toString() );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj instanceof FeatureSpecPair ) && compareTo( ( FeatureSpecPair ) obj ) == 0;
	}

	@Override
	public String toString()
	{
		if ( featureSpec == null || projectionSpec == null )
			return "";

		final int size = featureSpec.getProjectionSpecs().size();
		if ( featureSpec.getMultiplicity().equals( Multiplicity.SINGLE ) && size == 1 )
			return featureSpec.getKey();

		return featureSpec.getKey() + " - " + projectionKey();
	}

	public FeatureProjectionKey projectionKey()
	{
		final Multiplicity multiplicity = featureSpec.getMultiplicity();
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
		return key;
	}

	/**
	 * Returns the feature projection found in the specified feature model, and
	 * that matches the specifications of this instance. If the projection
	 * cannot be found in the feature model, returns <code>null</code>.
	 * 
	 * @param <O>
	 *            the type of objects the projection is defined on.
	 * @param featureModel
	 *            the feature model.
	 * @return the feature projection or <code>null</code>.
	 */
	public < O > FeatureProjection< O > getProjection( final FeatureModel featureModel )
	{
		final Feature< ? > feature = featureModel.getFeature( featureSpec );
		if ( null == feature )
			return null;

		final FeatureProjectionKey key = projectionKey();
		@SuppressWarnings( "unchecked" )
		final FeatureProjection< O > projection = ( FeatureProjection< O > ) feature.project( key );
		return projection;
	}
}
