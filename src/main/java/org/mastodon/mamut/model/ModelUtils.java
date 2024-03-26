/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.mamut.feature.SpotTrackIDFeature;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetStructure;

public class ModelUtils
{
	public enum DumpFlags
	{
		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will include
		 * the string returned by {@link Model#toString()} and hence the value of
		 * {@link Model#hashCode()}. This can be useful to associate the "dump" with
		 * a specific model instance. But it comes at the cost of making the output
		 * less predictable.
		 */
		PRINT_HASH,

		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will include
		 * the values stored in th {@link Model#getFeatureModel() feature model}.
		 */
		PRINT_FEATURES,

		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will include
		 * the tags stored in the {@link Model#getTagSetModel() tag set model}.
		 */
		PRINT_TAGS
	}

	/**
	 * Returns a string representation of the specified model content as two
	 * text tables.
	 * <p>
	 * The tables by default contain all spot and link properties as well
	 * as the feature values.
	 * <p>
	 * There is also a method {@link #dump(Model, DumpFlags...)} that
	 * allows to select which information to include in the table.
	 */
	public static final String dump( final Model model )
	{
		return dump( model, Long.MAX_VALUE );
	}

	/**
	 * Returns a string representation of the specified model content as two
	 * text tables.
	 * @param model The model to generate the dump for.
	 * @param options Which information to include in the tables can be changed using {@link DumpFlags}.
	 */
	public static final String dump( final Model model, final DumpFlags... options )
	{
		return dump( model, Long.MAX_VALUE, options );
	}

	/**
	 * Returns a string representation of the specified model content as two
	 * text tables. The number of lines in each table is limited to {@code maxLines}.
	 * <p>
	 * The tables by default contain all spot and link properties as well
	 * as the feature values.
	 * <p>
	 * There is also a method {@link #dump(Model, long, DumpFlags...)} that
	 * allows to select which information to include in the table.
	 *
	 * @param maxLines the max number of rows to print in the two tables.
	 */
	public static final String dump( final Model model, final long maxLines )
	{
		return dump( model, maxLines, DumpFlags.PRINT_HASH, DumpFlags.PRINT_FEATURES );
	}

	/**
	 * Returns a string representation of the specified model content as two
	 * text tables.
	 * @param model The model to generate the dump for.
	 * @param maxLines the max number of rows to print in the two tables.
	 * @param options Which information to include in the tables can be changed using {@link DumpFlags}.
	 */
	public static final String dump( final Model model, final long maxLines, final DumpFlags... options )
	{
		Set< DumpFlags > optionsSet = EnumSet.copyOf( Arrays.asList( options ) );
		final FeatureModel featureModel = model.getFeatureModel();
		final List< FeatureSpec< ?, ? > > featureSpecs = new ArrayList<>( featureModel.getFeatureSpecs() );
		featureSpecs.sort( Comparator.comparing( FeatureSpec::getKey ) );

		final StringBuilder str = new StringBuilder();
		if ( optionsSet.contains( DumpFlags.PRINT_HASH ) )
			str.append( "Model " ).append( model ).append( "\n" );

		str.append( "Spots:\n" );
		addSpotsTable( model, maxLines, optionsSet, featureSpecs, str );

		str.append( "Links:\n" );
		addLinksTable( model, maxLines, optionsSet, featureSpecs, str );

		return str.toString();
	}

	private static void addSpotsTable( Model model, long maxLines, Set< DumpFlags > optionsSet, List< FeatureSpec< ?, ? > > featureSpecs, StringBuilder str )
	{
		final String spaceUnits = Optional.ofNullable( model.getSpaceUnits() ).orElse( "" );
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		TablePrinter< Spot > spotsTable = new TablePrinter<>();
		spotsTable.defineColumn( 9, "Id", "", spot -> Integer.toString( spot.getInternalPoolIndex() ) );
		spotsTable.defineColumn( 9, "Label", "", Spot::getLabel );
		spotsTable.defineColumn( 6, "Frame", "", spot -> Integer.toString( spot.getTimepoint() ) );
		spotsTable.defineColumn( 9, "X", bracket( spaceUnits ), spot -> String.format( Locale.US, "%9.1f", spot.getDoublePosition( 0 ) ) );
		spotsTable.defineColumn( 9, "Y", bracket( spaceUnits ), spot -> String.format( Locale.US, "%9.1f", spot.getDoublePosition( 1 ) ) );
		spotsTable.defineColumn( 9, "Z", bracket( spaceUnits ), spot -> String.format( Locale.US, "%9.1f", spot.getDoublePosition( 2 ) ) );
		if ( optionsSet.contains( DumpFlags.PRINT_TAGS ) )
			addTagColumns( spotsTable, model.getTagSetModel().getTagSetStructure(), model.getTagSetModel().getVertexTags() );
		if ( optionsSet.contains( DumpFlags.PRINT_FEATURES ) )
			addFeatureColumns( featureSpecs, featureModel, spotsTable, Spot.class );

		List< Spot > spots = getSortedSpots( graph, featureModel );
		spotsTable.print( str, spots, maxLines );
	}

	private static void addLinksTable( Model model, long maxLines, Set< DumpFlags > optionsSet, List< FeatureSpec< ?, ? > > featureSpecs, StringBuilder str )
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();
		TablePrinter< Link > linkTable = new TablePrinter<>();
		linkTable.defineColumn( 9, "Id", "", link -> Integer.toString( link.getInternalPoolIndex() ) );
		final Spot ref = graph.vertexRef();
		linkTable.defineColumn( 9, "Source Id", "", link -> Integer.toString( link.getSource( ref ).getInternalPoolIndex() ) );
		linkTable.defineColumn( 9, "Target Id", "", link -> Integer.toString( link.getTarget( ref ).getInternalPoolIndex() ) );
		if ( optionsSet.contains( DumpFlags.PRINT_TAGS ) )
			addTagColumns( linkTable, model.getTagSetModel().getTagSetStructure(), model.getTagSetModel().getEdgeTags() );
		if ( optionsSet.contains( DumpFlags.PRINT_FEATURES ) )
			addFeatureColumns( featureSpecs, featureModel, linkTable, Link.class );

		linkTable.print( str, graph.edges(), maxLines );
	}


	private static < T > void addTagColumns( TablePrinter< T > table, TagSetStructure tagSetStructure, ObjTags< T > tags )
	{
		for ( TagSetStructure.TagSet tagSet : tagSetStructure.getTagSets() )
		{
			String header = tagSet.getName();
			ObjTagMap< T, TagSetStructure.Tag > tagSetTags = tags.tags( tagSet );
			table.defineColumn( header.length(), header, "", spotOrLink -> {
				TagSetStructure.Tag tag = tagSetTags.get( spotOrLink );
				return tag == null ? "" : tag.label();
			} );
		}
	}

	private static < T > void addFeatureColumns( List< FeatureSpec< ?, ? > > featureSpecs, FeatureModel featureModel, TablePrinter< T > table, Class< T > targetClass )
	{
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
		{
			if ( !featureSpec.getTargetClass().equals( targetClass ) )
				continue;

			@SuppressWarnings( "unchecked" )
			final Feature< T > feature = ( Feature< T > ) featureModel.getFeature( featureSpec );
			if ( feature.projections() == null )
				continue;

			feature.projections().stream().sorted( Comparator.comparing( projection -> projection.getKey().toString() ) ).forEach( projection -> {
				final String title = projection.getKey().toString();
				final String unit = bracket( Optional.ofNullable( projection.units() ).orElse( "" ) );
				final int width = Math.max( title.length(), unit.length() ) + 2;
				table.defineColumn( width, title, unit, spotOrLink -> valueAsString( projection, spotOrLink, width ) );
			} );
		}
	}

	private static RefArrayList< Spot > getSortedSpots( ModelGraph graph, FeatureModel featureModel )
	{
		final RefArrayList< Spot > spots = new RefArrayList<>( graph.vertices().getRefPool(), graph.vertices().size() );
		spots.addAll( graph.vertices() );
		spots.sort( getSpotComparator( featureModel ) );
		return spots;
	}

	private static Comparator< Spot > getSpotComparator( FeatureModel featureModel )
	{
		if ( featureModel.getFeatureSpecs().contains( SpotTrackIDFeature.SPEC ) )
		{
			final SpotTrackIDFeature trackID = ( SpotTrackIDFeature ) featureModel.getFeature( SpotTrackIDFeature.SPEC );
			return Comparator.comparing( trackID::get )
					.thenComparing( Spot::getTimepoint )
					.thenComparing( Spot::getInternalPoolIndex );
		}
		else
			return Comparator.comparingInt( Spot::getTimepoint ).thenComparing( Spot::getInternalPoolIndex );
	}

	private static < T > String valueAsString( FeatureProjection< T > projection, T t, int width )
	{
		if ( projection.isSet( t ) )
			if ( projection instanceof IntFeatureProjection )
				return String.format( Locale.US, "%" + width + "d", ( int ) projection.value( t ) );
			else
				return String.format( Locale.US, "%" + width + ".1f", projection.value( t ) );
		else
			return String.format( Locale.US, "%" + width + "s", "unset" );
	}

	private static class TablePrinter< T >
	{

		private final List< Column< T > > columns = new ArrayList<>();

		public void defineColumn( int width, String title, String unit, Function< T, String > toString )
		{
			columns.add( new Column<>( columns.isEmpty(), width, title, unit, toString ) );
		}

		public void print( StringBuilder str, Iterable< T > rows, long maxLines )
		{
			for ( Column< T > column : columns )
				str.append( String.format( Locale.US, column.template, column.header ) );
			str.append( '\n' );
			for ( Column< T > column : columns )
				str.append( String.format( Locale.US, column.template, column.unit ) );
			str.append( '\n' );
			int totalWidth = columns.stream().mapToInt( c -> c.width + 2 ).sum() - 2;
			for ( int i = 0; i < totalWidth; i++ )
				str.append( '-' );
			str.append( '\n' );
			long i = 0;
			for ( T row : rows )
			{
				for ( Column< T > column : columns )
					str.append( String.format( Locale.US, column.template, column.valueToString.apply( row ) ) );
				str.append( '\n' );
				i++;
				if ( i >= maxLines )
					break;
			}
		}
	}

	private static class Column< T >
	{

		private final int width;

		private final String header;

		private final String unit;

		private final String template;

		private final Function< T, String > valueToString;

		private Column( boolean isFirst, int width, String header, String unit, Function< T, String > valueToString )
		{
			this.header = header;
			this.unit = unit;
			this.width = width;
			this.template = ( isFirst ? "" : "  " ) + "%" + width + "s";
			this.valueToString = valueToString;
		}
	}

	private static String bracket( final String str )
	{
		return str.isEmpty() ? "" : "(" + str + ")";
	}
}
