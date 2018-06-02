package org.mastodon.revised.model.mamut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.IntFeatureProjection;

public class ModelUtils
{

	public static final String dump( final Model model )
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final StringBuilder str = new StringBuilder();
		str.append( "Model " + model.toString() + "\n" );

		/*
		 * Collect spot feature headers.
		 */

		final Map< String, FeatureProjection< Spot > > sfs = new LinkedHashMap<>();
		final Set< Feature< ?, ? > > set1 = featureModel.getFeatureSet( Spot.class );
		final List< Feature< ?, ? > > spotFeatures = set1 == null ? new ArrayList<>() : new ArrayList<>( set1 );
		spotFeatures.sort( ( f1, f2 ) -> f1.getKey().compareTo( f2.getKey() ) );
		for ( final Feature< ?, ? > feature : spotFeatures )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Spot, ? > sf = ( Feature< Spot, ? > ) feature;
			final Map< String, FeatureProjection< Spot > > projections = sf.getProjections();
			sfs.putAll( projections );
		}

		/*
		 * Loop over all spots.
		 */

		str.append( "Spots:\n" );
		final String h1a = String.format( "%9s  %9s  %6s  %9s  %9s  %9s",
				"Id", "Label", "Frame", "X", "Y", "Z" );
		str.append( h1a );

		final int[] spotColumnHeaderWidth = new int[ sfs.size() ];
		int i = 0;
		for ( final String pn : sfs.keySet() )
		{
			spotColumnHeaderWidth[ i ] = pn.length() + 2;
			str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", pn ) );
			i++;
		}

		str.append( '\n' );
		final char[] sline = new char[ h1a.length() + Arrays.stream( spotColumnHeaderWidth ).sum() + 2 * spotColumnHeaderWidth.length ];
		Arrays.fill( sline, '-' );
		str.append( sline );
		str.append( '\n' );

		for ( final Spot spot : graph.vertices() )
		{
			final String h1b = String.format( "%9d  %9s  %6d  %9.1f  %9.1f  %9.1f",
					spot.getInternalPoolIndex(), spot.getLabel(), spot.getTimepoint(),
					spot.getDoublePosition( 0 ), spot.getDoublePosition( 1 ), spot.getDoublePosition( 2 ) );

			str.append( h1b );
			i = 0;
			for ( final String pn : sfs.keySet() )
			{
				if ( sfs.get( pn ).isSet( spot ) )
					if ( sfs.get( pn ) instanceof IntFeatureProjection )
						str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "d", ( int ) sfs.get( pn ).value( spot ) ) );
					else
						str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + ".1f", sfs.get( pn ).value( spot ) ) );
				else
					str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		/*
		 * Collect link feature headers.
		 */

		final Map< String, FeatureProjection< Link > > lfs = new LinkedHashMap<>();
		final Set< Feature< ?, ? > > set2 = featureModel.getFeatureSet( Link.class );
		final List< Feature< ?, ? > > linkFeatures = set2 == null ? new ArrayList<>() : new ArrayList<>( set2 );
		linkFeatures.sort( ( f1, f2 ) -> f1.getKey().compareTo( f2.getKey() ) );
		for ( final Feature< ?, ? > feature : linkFeatures )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Link, ? > lf = ( Feature< Link, ? > ) feature;
			final Map< String, FeatureProjection< Link > > projections = lf.getProjections();
			lfs.putAll( projections );
		}

		/*
		 * Loop over all links.
		 */

		str.append( "Links:\n" );
		final String h2a = String.format( "%9s  %9s  %9s", "Id", "Source Id", "Target Id" );
		str.append( h2a );

		final int[] linkColumnHeaderWidth = new int[ lfs.size() ];
		i = 0;
		for ( final String pn : lfs.keySet() )
		{
			linkColumnHeaderWidth[ i ] = pn.length() + 2;
			str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", pn ) );
			i++;
		}

		str.append( '\n' );
		final char[] lline = new char[ h2a.length() + Arrays.stream( linkColumnHeaderWidth ).sum() + 2 * linkColumnHeaderWidth.length ];
		Arrays.fill( lline, '-' );
		str.append( lline );
		str.append( '\n' );

		final Spot ref = graph.vertexRef();
		for ( final Link link : graph.edges() )
		{
			final String h1b = String.format( "%9d  %9d  %9d", link.getInternalPoolIndex(),
					link.getSource( ref ).getInternalPoolIndex(), link.getTarget( ref ).getInternalPoolIndex() );
			str.append( h1b );
			i = 0;
			for ( final String pn : lfs.keySet() )
			{
				if ( lfs.get( pn ).isSet( link ) )
					if ( sfs.get( pn ) instanceof IntFeatureProjection )
						str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "d", ( int ) lfs.get( pn ).value( link ) ) );
					else
						str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + ".1f", lfs.get( pn ).value( link ) ) );
				else
					str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		return str.toString();
	}

}
