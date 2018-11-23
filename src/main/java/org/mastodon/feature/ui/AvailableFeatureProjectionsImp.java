package org.mastodon.feature.ui;

import static org.mastodon.revised.ui.coloring.feature.TargetType.VERTEX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.feature.Multiplicity;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.revised.ui.coloring.feature.TargetType;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Lists the features and projections (as their respective ({@code String}
 * keys), as well as available source indices (for features with multiplicity).
 *
 * This is an implementation of {@link AvailableFeatureProjections} that can be
 * populated with
 * <ul>
 * <li>{@link FeatureSpec}s from the {@link FeatureSpecsService} and the
 * {@link FeatureModel} (via {@link #add(FeatureSpec)})</li>
 * <li>{@link FeatureProjectionId}s of feature projections defined in existing
 * FeatureColorModes (via {@link #add(FeatureProjectionId)})</li>
 * <li>the number of sources from the current model (via
 * {@link #setMinNumSources(int)})</li>
 * </ul>
 *
 * @author Tobias Pietzsch
 */
public class AvailableFeatureProjectionsImp implements AvailableFeatureProjections
{
	static class FeatureProperties
	{
		final Multiplicity multiplicity;

		final Set< String > projectionKeys;

		public FeatureProperties( final Multiplicity multiplicity )
		{
			this.multiplicity = multiplicity;
			this.projectionKeys = new LinkedHashSet<>();
		}

		@Override
		public String toString()
		{
			final StringBuffer sb = new StringBuffer( "FeatureProperties{" );
			sb.append( "multiplicity=" ).append( multiplicity );
			sb.append( ", projectionKeys=" ).append( projectionKeys );
			sb.append( '}' );
			return sb.toString();
		}
	}

	private final Map< String, FeatureProperties > vertexFeatures = new LinkedHashMap<>();

	private final Map< String, FeatureProperties > edgeFeatures = new LinkedHashMap<>();

	// all available source indices (from model or existing color modes)
	private final TIntList sourceIndices = new TIntArrayList();

	private final Class< ? > vertexClass;

	private final Class< ? > edgeClass;

	public AvailableFeatureProjectionsImp( final Class< ? > vertexClass, final Class< ? > edgeClass )
	{
		this.vertexClass = vertexClass;
		this.edgeClass = edgeClass;
	}

	@Override
	public TIntList getSourceIndices()
	{
		return sourceIndices;
	}

	@Override
	public Collection< String > featureKeys( final TargetType targetType )
	{
		return features( targetType ).keySet();
	}

	@Override
	public Collection< String > projectionKeys( final TargetType targetType, final String featureKey )
	{
		final FeatureProperties p = features( targetType ).get( featureKey );
		if ( p == null )
			throw new NoSuchElementException();
		return p.projectionKeys;
	}

	@Override
	public Multiplicity multiplicity( final TargetType targetType, final String featureKey )
	{
		final FeatureProperties p = features( targetType ).get( featureKey );
		if ( p == null )
			throw new NoSuchElementException();
		return p.multiplicity;
	}

	/**
	 * Adds {@code 0 .. numSources-1} to available {@code sourceIndices}.
	 */
	public void setMinNumSources( final int numSources )
	{
		for ( int i = 0; i < numSources; i++ )
		{
			if ( !sourceIndices.contains( i ) )
				sourceIndices.add( i );
		}
		sourceIndices.sort();
	}

	/**
	 * Add {@code i} to available {@code sourceIndices}. If {@code i < 0}, do
	 * nothing.
	 */
	public void addSourceIndex( final int i )
	{
		if ( i >= 0 && !sourceIndices.contains( i ) )
		{
			sourceIndices.add( i );
			sourceIndices.sort();
		}
	}

	/**
	 * Adds {@code FeatureSpec} (from {@code FeatureSpecsService} or
	 * {@code FeatureModel}).
	 */
	public void add( final FeatureSpec< ?, ? > spec )
	{
		final Map< String, FeatureProperties > features;

		final Class< ? > target = spec.getTargetClass();
		if ( target.isAssignableFrom( vertexClass ) )
			features = vertexFeatures;
		else if ( target.isAssignableFrom( edgeClass ) )
			features = edgeFeatures;
		else
			return;

		final String key = spec.getKey();
		FeatureProperties fp = features.get( key );
		if ( fp == null )
		{
			fp = new FeatureProperties( spec.getMultiplicity() );
			features.put( key, fp );
		}
		else if ( !fp.multiplicity.equals( spec.getMultiplicity() ) )
		{
			System.err.println( "trying to add to existing feature with different multiplicity." );
			return;
		}
		spec.getProjectionSpecs().stream()
				.map( FeatureProjectionSpec::getKey )
				.forEach( fp.projectionKeys::add );
	}

	/**
	 * Adds {@code FeatureProjectionId} (from existing color mode).
	 */
	public void add( final FeatureProjectionId id )
	{
		if ( id == null )
			return;

		final Map< String, FeatureProperties > features = features( id.getTargetType() );

		final String key = id.getFeatureKey();
		FeatureProperties fp = features.get( key );
		if ( fp == null )
		{
			fp = new FeatureProperties( id.getMultiplicity() );
			features.put( key, fp );
		}
		else if ( !fp.multiplicity.equals( id.getMultiplicity() ) )
		{
			System.err.println( "trying to add to existing feature with different multiplicity." );
			return;
		}
		fp.projectionKeys.add( id.getProjectionKey() );

		addSourceIndex( id.getI0() );
		addSourceIndex( id.getI1() );
	}

	private Map< String, FeatureProperties > features( final TargetType targetType )
	{
		return ( targetType == VERTEX )
				? vertexFeatures
				: edgeFeatures;
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer( "AvailableFeatureProjections{\n" );
		sb.append( "  vertexFeatures={\n" );
		for ( final Map.Entry< String, FeatureProperties > e : vertexFeatures.entrySet() )
		{
			sb.append( "    " );
			sb.append( e.getKey() );
			sb.append( "[" );
			sb.append( e.getValue() );
			sb.append( "]\n" );
		}
		sb.append( "  },\n" );
		sb.append( "  edgeFeatures={\n" );
		for ( final Map.Entry< String, FeatureProperties > e : edgeFeatures.entrySet() )
		{
			sb.append( "    " );
			sb.append( e.getKey() );
			sb.append( "[" );
			sb.append( e.getValue() );
			sb.append( "]\n" );
		}
		sb.append( "  },\n" );
		sb.append( "  sourceIndices=" ).append( sourceIndices ).append( ",\n" );
		sb.append( "  vertexClass=" ).append( vertexClass ).append( ",\n" );
		sb.append( "  edgeClass=" ).append( edgeClass ).append( ",\n" );
		sb.append( '}' );
		return sb.toString();
	}

	/*
	 * Below this: Everything required to generate a comprehensive set of
	 * feature specs, from 3 sources: - discoverable feature specs; - feature
	 * model; - unknown feature specs mentioned in color modes.
	 */

	public static AvailableFeatureProjections createAvailableFeatureProjections(
			final FeatureSpecsService featureSpecsService,
			final int numSources,
			final FeatureModel featureModel,
			final FeatureColorModeManager featureColorModeManager,
			final Class< ? > vertexClass,
			final Class< ? > edgeClass )
	{
		final AvailableFeatureProjectionsImp projections = new AvailableFeatureProjectionsImp( vertexClass, edgeClass );

		/*
		 * Available source indices from SharedBigDataViewerData.
		 */
		projections.setMinNumSources( Math.max( numSources, 1 ) );

		/*
		 * Feature specs from service.
		 */
		featureSpecsService.getSpecs( vertexClass ).forEach( projections::add );
		featureSpecsService.getSpecs( edgeClass ).forEach( projections::add );

		/*
		 * Feature specs from feature model.
		 */
		if ( featureModel != null )
			featureModel.getFeatureSpecs().forEach( projections::add );

		/*
		 * Feature projections used in existing FeatureColorModes
		 */
		final Collection< FeatureColorMode > modes = new ArrayList<>();
		modes.addAll( featureColorModeManager.getBuiltinStyles() );
		modes.addAll( featureColorModeManager.getUserStyles() );
		for ( final FeatureColorMode mode : modes )
		{
			/*
			 * Vertex mode.
			 */
			projections.add( mode.getVertexFeatureProjection() );

			/*
			 * Edge mode.
			 */
			projections.add( mode.getEdgeFeatureProjection() );
		}

		return projections;
	}
}
