package org.mastodon.mamut.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.mamut.feature.SpotTrackIDFeature;

public class ModelUtils
{

	public static final String dump( final Model model )
	{
		return dump( model, Long.MAX_VALUE );
	}

	public static final String dump( final Model model, final long maxLines )
	{
		final String spaceUnits = Optional.ofNullable( model.getSpaceUnits() ).orElse( "" );
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final StringBuilder str = new StringBuilder();
		str.append( "Model " + model.toString() + "\n" );

		/*
		 * Get feature specs and sort them by key.
		 */

		final List< FeatureSpec< ?, ? > > featureSpecs = new ArrayList<>( featureModel.getFeatureSpecs() );
		featureSpecs.sort( ( fs1, fs2 ) -> fs1.getKey().compareTo( fs2.getKey() ) );

		/*
		 * Collect spot feature headers.
		 */

		final Map< FeatureProjectionKey, FeatureProjection< Spot > > sfs = new LinkedHashMap<>();
		final List< Feature< Spot > > spotFeatures = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
			if ( featureSpec.getTargetClass().equals( Spot.class ) )
			{
				@SuppressWarnings( "unchecked" )
				final Feature< Spot > spotFeature = ( Feature< Spot > ) featureModel.getFeature( featureSpec );
				spotFeatures.add( spotFeature );
			}

		// TODO if different features have same FeatureProjectionKey, map
		// entries will be overridden!
		for ( final Feature< Spot > feature : spotFeatures )
		{
			if ( feature.projections() == null )
				continue;
			for ( final FeatureProjection< Spot > projection : feature.projections() )
				sfs.put( projection.getKey(), projection );
		}

		/*
		 * Loop over all spots.
		 */

		str.append( "Spots:\n" );
		// Name line
		final String h1a = String.format( "%9s  %9s  %6s  %9s  %9s  %9s",
				"Id", "Label", "Frame", "X", "Y", "Z" );
		str.append( h1a );
		// Unit line
		final StringBuilder unitLineSpots = new StringBuilder();
		unitLineSpots.append( String.format( "%9s  %9s  %6s  %9s  %9s  %9s",
				"", "", "", bracket( spaceUnits ), bracket( spaceUnits ), bracket( spaceUnits ) ) );

		final int[] spotColumnHeaderWidth = new int[ sfs.size() ];
		int i = 0;
		for ( final FeatureProjectionKey pk : sfs.keySet() )
		{
			final String units = Optional.ofNullable( sfs.get( pk ).units() ).orElse( "" );
			spotColumnHeaderWidth[ i ] = Math.max( pk.toString().length(), units.length() + 2 ) + 2;
			str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", pk ) );
			unitLineSpots.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", bracket( units ) ) );
			i++;
		}

		str.append( '\n' );
		str.append( unitLineSpots.toString() );

		str.append( '\n' );
		final char[] sline = new char[ h1a.length() + Arrays.stream( spotColumnHeaderWidth ).sum() + 2 * spotColumnHeaderWidth.length ];
		Arrays.fill( sline, '-' );
		str.append( sline );
		str.append( '\n' );

		/*
		 * Sort spots.
		 */

		final RefArrayList< Spot > spots = new RefArrayList<Spot>( graph.vertices().getRefPool(), graph.vertices().size() );
		spots.addAll( graph.vertices() );

		// Do we have track id?
		if (featureSpecs.contains( SpotTrackIDFeature.SPEC ))
		{
			final SpotTrackIDFeature trackID = ( SpotTrackIDFeature ) featureModel.getFeature( SpotTrackIDFeature.SPEC );
			spots.sort( new Comparator< Spot >()
			{

				@Override
				public int compare( final Spot o1, final Spot o2 )
				{
					final int track1 = trackID.get( o1 );
					final int track2 = trackID.get( o2 );
					if (track1 == track2)
						return o1.getTimepoint() - o2.getTimepoint();

					return track1 - track2;
				}
			} );
		}
		else
		{
			spots.sort( Comparator.comparingInt( Spot::getTimepoint ) );
		}


		long n = 0;
		for ( final Spot spot : spots )
		{
			if ( n++ > maxLines )
				break;

			final String h1b = String.format( "%9d  %9s  %6d  %9.1f  %9.1f  %9.1f",
					spot.getInternalPoolIndex(), spot.getLabel(), spot.getTimepoint(),
					spot.getDoublePosition( 0 ), spot.getDoublePosition( 1 ), spot.getDoublePosition( 2 ) );

			str.append( h1b );
			i = 0;
			for ( final FeatureProjectionKey pk : sfs.keySet() )
			{
				if ( sfs.get( pk ).isSet( spot ) )
					if ( sfs.get( pk ) instanceof IntFeatureProjection )
						str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "d", ( int ) sfs.get( pk ).value( spot ) ) );
					else
						str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + ".1f", sfs.get( pk ).value( spot ) ) );
				else
					str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		/*
		 * Collect link feature headers.
		 */

		final Map< FeatureProjectionKey, FeatureProjection< Link > > lfs = new LinkedHashMap<>();
		final List< Feature< Link > > linkFeatures = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
			if ( featureSpec.getTargetClass().equals( Link.class ) )
			{
				@SuppressWarnings( "unchecked" )
				final Feature< Link > linkFeature = ( Feature< Link > ) featureModel.getFeature( featureSpec );
				linkFeatures.add( linkFeature );
			}

		for ( final Feature< Link > feature : linkFeatures )
			for ( final FeatureProjection< Link > projection : feature.projections() )
				lfs.put( projection.getKey(), projection );

		/*
		 * Loop over all links.
		 */

		str.append( "Links:\n" );
		// Name line.
		final String h2a = String.format( "%9s  %9s  %9s", "Id", "Source Id", "Target Id" );
		str.append( h2a );
		// Unit line
		final StringBuilder unitLineLinks = new StringBuilder();
		unitLineLinks.append( String.format( "%9s  %9s  %9s", "", "", "" ) );

		final int[] linkColumnHeaderWidth = new int[ lfs.size() ];
		i = 0;
		for ( final FeatureProjectionKey pk : lfs.keySet() )
		{
			final String units = Optional.ofNullable( lfs.get( pk ).units() ).orElse( "" );
			linkColumnHeaderWidth[ i ] = Math.max( pk.toString().length(), units.length() + 2 ) + 2;
			str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", pk ) );
			unitLineLinks.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", bracket( units ) ) );
			i++;
		}

		str.append( '\n' );
		str.append( unitLineLinks.toString() );

		str.append( '\n' );
		final char[] lline = new char[ h2a.length() + Arrays.stream( linkColumnHeaderWidth ).sum() + 2 * linkColumnHeaderWidth.length ];
		Arrays.fill( lline, '-' );
		str.append( lline );
		str.append( '\n' );

		n = 0;
		final Spot ref = graph.vertexRef();
		for ( final Link link : graph.edges() )
		{
			if ( n++ > maxLines )
				break;

			final String h1b = String.format( "%9d  %9d  %9d", link.getInternalPoolIndex(),
					link.getSource( ref ).getInternalPoolIndex(), link.getTarget( ref ).getInternalPoolIndex() );
			str.append( h1b );
			i = 0;
			for ( final FeatureProjectionKey pk : lfs.keySet() )
			{
				if ( lfs.get( pk ).isSet( link ) )
					if ( sfs.get( pk ) instanceof IntFeatureProjection )
						str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "d", ( int ) lfs.get( pk ).value( link ) ) );
					else
						str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + ".1f", lfs.get( pk ).value( link ) ) );
				else
					str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		return str.toString();
	}

	private static final String bracket( final String str )
	{
		return str.isEmpty() ? "" : "(" + str + ")";
	}
}
