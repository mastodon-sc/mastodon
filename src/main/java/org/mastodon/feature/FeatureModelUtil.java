package org.mastodon.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;

public class FeatureModelUtil
{
	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	public static boolean isValid( final FeatureModel fm, final FeatureColorMode mode, final Class< ? > vertexClass, final Class< ? > edgeClass )
	{
		if ( mode.getVertexColorMode() != VertexColorMode.NONE )
		{
			final Class< ? > target = mode.getVertexColorMode() == VertexColorMode.VERTEX
					? vertexClass
					: edgeClass;
			if ( null == getFeatureProjection( fm, mode.getVertexFeatureProjection(), target ) )
				return false;
		}

		if ( mode.getEdgeColorMode() != EdgeColorMode.NONE )
		{
			final Class< ? > target = mode.getEdgeColorMode() == EdgeColorMode.EDGE
					? edgeClass
					: vertexClass;
			if ( null == getFeatureProjection( fm, mode.getEdgeFeatureProjection(), target ) )
				return false;
		}

		return true;
	}

	public static FeatureProjection< ? > getFeatureProjection( final FeatureModel featureModel, final String[] keys )
	{
		return getFeatureProjection( featureModel, getFeatureSpec( featureModel, keys[ 0 ] ), parseProjectionName( keys[ 1 ] ) );
	}

	public static < T > FeatureProjection< T > getFeatureProjection( final FeatureModel featureModel, final String[] keys, final Class< T > target )
	{
		return getFeatureProjection( featureModel, getFeatureSpec( featureModel, keys[ 0 ], target ), parseProjectionName( keys[ 1 ] ) );
	}

	public static < T > FeatureProjection< T > getFeatureProjection( final FeatureModel featureModel, final FeatureSpec< ?, T > featureKey, final FeatureProjectionKey projectionKey )
	{
		@SuppressWarnings( "unchecked" )
		final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( featureKey );
		if ( feature == null )
			return null;
		return feature.project( projectionKey );
	}

	private static final Pattern SOURCE_PATTERN = Pattern.compile( "^(?<name>.*) ch(?<digit1>\\d+)\\z" );

	private static final Pattern SOURCE_PAIR_PATTERN = Pattern.compile( "^(?<name>.*) ch(?<digit1>\\d+) ch(?<digit2>\\d+)\\z" );

	/**
	 * Split projection key string into projection and source indices, and
	 * create {@link FeatureProjectionKey}
	 */
	public static FeatureProjectionKey parseProjectionName( final String name )
	{
		Matcher matcher = SOURCE_PAIR_PATTERN.matcher( name );
		if ( matcher.find() )
		{
			final String projectionName = matcher.group( "name" );
			final String digits1 = matcher.group( "digit1" );
			final String digits2 = matcher.group( "digit2" );
			return key( new FeatureProjectionSpec( projectionName ),
					Integer.parseInt( digits1 ) - 1,
					Integer.parseInt( digits2 ) - 1 );
		}
		matcher = SOURCE_PATTERN.matcher( name );
		if ( matcher.find() )
		{
			final String projectionName = matcher.group( "name" );
			final String digits1 = matcher.group( "digit1" );
			return key( new FeatureProjectionSpec( projectionName ),
					Integer.parseInt( digits1 ) - 1 );
		}
		return key( new FeatureProjectionSpec( name ) );
	}

	/**
	 * Get FeatureSpec with given {@code featureKey} (if defined in
	 * {@code FeatureModel})
	 */
	public static FeatureSpec< ?, ? > getFeatureSpec( final FeatureModel featureModel, final String featureKey )
	{
		return featureModel.getFeatureSpecs().stream()
				.filter( fs -> fs.getKey().equals( featureKey ) )
				.findFirst()
				.orElse( null );
	}

	/**
	 * Get FeatureSpec with given {@code featureKey} and target {@code clazz}
	 * (if defined in {@code FeatureModel})
	 */
	public static < T > FeatureSpec< ?, T > getFeatureSpec( final FeatureModel featureModel, final String featureKey, final Class< T > clazz )
	{
		@SuppressWarnings( "unchecked" )
		final FeatureSpec< ?, T > spec = ( FeatureSpec< ?, T > ) featureModel.getFeatureSpecs().stream()
				.filter( fs -> fs.getKey().equals( featureKey ) )
				.filter( fs -> fs.getTargetClass().isAssignableFrom( clazz ) )
				.findFirst()
				.orElse( null );
		return spec;
	}
}
