package org.mastodon.revised.ui.coloring.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;

/**
 * Provides mapping from {@link FeatureProjectionId} to
 * {@link FeatureProjection}s declared in a {@link FeatureModel}.
 *
 * @author Tobias Pietzsch
 */
public class ProjectionsFromFeatureModel implements Projections
{
	private final FeatureModel featureModel;

	public ProjectionsFromFeatureModel( final FeatureModel featureModel )
	{
		this.featureModel = featureModel;
	}

	@Override
	public FeatureProjection< ? > getFeatureProjection( final FeatureProjectionId id )
	{
		if ( id == null )
			return null;

		final FeatureSpec< ?, ? > featureSpec = featureModel.getFeatureSpecs().stream()
				.filter( spec -> spec.getKey().equals( id.getFeatureKey() ) )
				.findFirst()
				.orElse( null );
		return getFeatureProjection( id, featureSpec );
	}

	@Override
	public < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, final Class< T > target )
	{
		if ( id == null )
			return null;

		@SuppressWarnings( "unchecked" )
		final FeatureSpec< ?, T > featureSpec = ( FeatureSpec< ?, T > ) featureModel.getFeatureSpecs().stream()
				.filter( spec -> target.isAssignableFrom( spec.getTargetClass() ) )
				.filter( spec -> spec.getKey().equals( id.getFeatureKey() ) )
				.findFirst()
				.orElse( null );
		return getFeatureProjection( id, featureSpec );
	}

	private < T > FeatureProjection< T > getFeatureProjection( final FeatureProjectionId id, final FeatureSpec< ?, T > featureSpec )
	{
		if ( featureSpec == null )
			return null;

		@SuppressWarnings( "unchecked" )
		final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( featureSpec );
		if ( feature == null )
			return null;

		final FeatureProjectionSpec projectionSpec = featureSpec.getProjectionSpecs().stream()
				.filter( spec -> spec.getKey().equals( id.getProjectionKey() ) )
				.findFirst()
				.orElse( null );
		if ( projectionSpec == null )
			return null;

		switch ( id.getMultiplicity() )
		{
		default:
		case SINGLE:
			return feature.project( key( projectionSpec ) );
		case ON_SOURCES:
			return feature.project( key( projectionSpec, id.getI0() ) );
		case ON_SOURCE_PAIRS:
			return feature.project( key( projectionSpec, id.getI0(), id.getI1() ) );
		}
	}
}
