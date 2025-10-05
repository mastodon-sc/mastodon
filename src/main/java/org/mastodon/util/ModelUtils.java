package org.mastodon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.mastodon.Ref;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Model;
import org.mastodon.model.HasLabel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;

/**
 * Echoes the content of a model or graph to a String, in the shape of two
 * tables.
 */
public class ModelUtils
{

	public static < G extends ReadOnlyGraph< V, E >, V extends Vertex< E >, E extends Edge< V > > String dump(
			final MastodonModel< G, V, E > model,
			final String spaceUnits,
			final long maxLines,
			final DumpFlags... options )
	{
		return dump(
				model.getGraph(),
				model.getFeatureModel(),
				model.getTagSetModel(),
				spaceUnits,
				maxLines,
				options );
	}

	public static < G extends ReadOnlyGraph< V, E >, V extends Vertex< E >, E extends Edge< V > > String dump(
			final MastodonModel< G, V, E > model,
			final String spaceUnits,
			final long maxLines )
	{
		return dump(
				model,
				spaceUnits,
				maxLines,
				DumpFlags.PRINT_HASH, DumpFlags.PRINT_FEATURES );
	}

	public static < G extends ReadOnlyGraph< V, E >, V extends Vertex< E >, E extends Edge< V > > String dump(
			final MastodonModel< G, V, E > model,
			final String spaceUnits )
	{
		return dump(
				model,
				spaceUnits,
				Long.MAX_VALUE,
				DumpFlags.PRINT_HASH, DumpFlags.PRINT_FEATURES );
	}

	/**
	 * Returns a string representation of the specified graph content as two
	 * text tables.
	 * 
	 * @param graph
	 *            The graph to generate the dump for.
	 * @param featureModel
	 *            The feature model to get feature values from.
	 * @param tagSetModel
	 *            The tag set model to get tags from.
	 * @param spaceUnits
	 *            the units of space, for the vertex position columns.
	 * @param maxLines
	 *            the max number of rows to print in the two tables.
	 * @param options
	 *            Which information to include in the tables can be changed
	 *            using {@link DumpFlags}.
	 */
	public static final < V extends Vertex< E >, E extends Edge< V > > String dump(
			final ReadOnlyGraph< V, E > graph,
			final FeatureModel featureModel,
			final TagSetModel< V, E > tagSetModel,
			final String spaceUnits,
			final long maxLines,
			final DumpFlags... options )
	{
		final Set< DumpFlags > optionsSet = EnumSet.copyOf( Arrays.asList( options ) );
		final List< FeatureSpec< ?, ? > > featureSpecs = new ArrayList<>( featureModel.getFeatureSpecs() );
		featureSpecs.sort( Comparator.comparing( FeatureSpec::getKey ) );

		final StringBuilder str = new StringBuilder();
		if ( optionsSet.contains( DumpFlags.PRINT_HASH ) )
			str.append( "Graph " ).append( graph ).append( "\n" );

		str.append( graph.vertexRef().getClass().getSimpleName() + ":\n" );
		addVertexTable( graph, featureModel, tagSetModel, spaceUnits, maxLines, optionsSet, featureSpecs, str );

		str.append( graph.edgeRef().getClass().getSimpleName() + ":\n" );
		addEdgeTable( graph, featureModel, tagSetModel, maxLines, optionsSet, featureSpecs, str );

		return str.toString();
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	private static < V extends Vertex< E >, E extends Edge< V > > void addVertexTable(
			final ReadOnlyGraph< V, E > graph,
			final FeatureModel featureModel,
			final TagSetModel< V, E > tagSetModel,
			final String spaceUnits,
			final long maxLines,
			final Set< DumpFlags > optionsSet,
			final List< FeatureSpec< ?, ? > > featureSpecs,
			final StringBuilder str )
	{
		final TablePrinter< V > vertexTable = new TablePrinter<>();

		final Class vertexClass = graph.vertexRef().getClass();
		if ( Ref.class.isAssignableFrom( vertexClass ) )
			vertexTable.defineColumn( 9, "Id", "", v -> Integer.toString( ( ( Ref< ? > ) v ).getInternalPoolIndex() ) );
		if ( HasLabel.class.isAssignableFrom( vertexClass ) )
			vertexTable.defineColumn( 9, "Label", "", v -> ( ( HasLabel ) v ).getLabel() );
		if ( HasTimepoint.class.isAssignableFrom( vertexClass ) )
			vertexTable.defineColumn( 6, "Frame", "", v -> Integer.toString( ( ( HasTimepoint ) v ).getTimepoint() ) );
		if ( RealLocalizable.class.isAssignableFrom( vertexClass ) )
		{
			vertexTable.defineColumn( 9, "X", bracket( spaceUnits ), v -> String.format( Locale.US, "%9.1f", ( ( RealLocalizable ) v ).getDoublePosition( 0 ) ) );
			vertexTable.defineColumn( 9, "Y", bracket( spaceUnits ), v -> String.format( Locale.US, "%9.1f", ( ( RealLocalizable ) v ).getDoublePosition( 1 ) ) );
			vertexTable.defineColumn( 9, "Z", bracket( spaceUnits ), v -> String.format( Locale.US, "%9.1f", ( ( RealLocalizable ) v ).getDoublePosition( 2 ) ) );
		}

		if ( optionsSet.contains( DumpFlags.PRINT_TAGS ) )
			addTagColumns( vertexTable, tagSetModel.getTagSetStructure(), tagSetModel.getVertexTags() );
		if ( optionsSet.contains( DumpFlags.PRINT_FEATURES ) )
			addFeatureColumns( featureSpecs, featureModel, vertexTable, vertexClass );

		final List< V > vertices = getSortedVertices( graph );
		vertexTable.print( str, vertices, maxLines );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private static < V extends Vertex< E >, E extends Edge< V > > void addEdgeTable(
			final ReadOnlyGraph< V, E > graph,
			final FeatureModel featureModel,
			final TagSetModel< V, E > tagSetModel,
			final long maxLines,
			final Set< DumpFlags > optionsSet,
			final List< FeatureSpec< ?, ? > > featureSpecs,
			final StringBuilder str )
	{
		final TablePrinter< E > edgeTable = new TablePrinter<>();

		final Class edgeClass = graph.edgeRef().getClass();
		if ( Ref.class.isAssignableFrom( edgeClass ) )
			edgeTable.defineColumn( 9, "Id", "", e -> Integer.toString( ( ( Ref< ? > ) e ).getInternalPoolIndex() ) );
		final Class vertexClass = graph.vertexRef().getClass();
		if ( Ref.class.isAssignableFrom( vertexClass ) )
		{
			edgeTable.defineColumn( 9, "Source Id", "", e -> Integer.toString( ( ( Ref ) e.getSource() ).getInternalPoolIndex() ) );
			edgeTable.defineColumn( 9, "Target Id", "", e -> Integer.toString( ( ( Ref ) e.getTarget() ).getInternalPoolIndex() ) );
		}
		else
		{
			edgeTable.defineColumn( 15, "Source", "", link -> link.getSource().toString() );
			edgeTable.defineColumn( 15, "Target", "", link -> link.getTarget().toString() );
		}

		if ( optionsSet.contains( DumpFlags.PRINT_TAGS ) )
			addTagColumns( edgeTable, tagSetModel.getTagSetStructure(), tagSetModel.getEdgeTags() );
		if ( optionsSet.contains( DumpFlags.PRINT_FEATURES ) )
			addFeatureColumns( featureSpecs, featureModel, edgeTable, edgeClass );

		edgeTable.print( str, graph.edges(), maxLines );
	}

	private static < T > void addTagColumns(
			final TablePrinter< T > table,
			final TagSetStructure tagSetStructure,
			final ObjTags< T > tags )
	{
		for ( final TagSetStructure.TagSet tagSet : tagSetStructure.getTagSets() )
		{
			final String header = tagSet.getName();
			final ObjTagMap< T, TagSetStructure.Tag > tagSetTags = tags.tags( tagSet );
			table.defineColumn( header.length(), header, "", spotOrLink -> {
				final TagSetStructure.Tag tag = tagSetTags.get( spotOrLink );
				return tag == null ? "" : tag.label();
			} );
		}
	}

	private static < T > void addFeatureColumns(
			final List< FeatureSpec< ?, ? > > featureSpecs,
			final FeatureModel featureModel,
			final TablePrinter< T > table,
			final Class< T > targetClass )
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

	private static < V extends Vertex< ? > > RefList< V > getSortedVertices( final ReadOnlyGraph< V, ? > graph )
	{
		final RefList< V > vertices = RefCollections.createRefList( graph.vertices(), graph.vertices().size() );
		vertices.addAll( graph.vertices() );
		vertices.sort( getVertexComparator( vertices.createRef() ) );
		return vertices;
	}

	/**
	 * Get a comparator for vertices that sorts them by timepoint, then label,
	 * then internal pool index, or by internal pool index only if the vertex
	 * type does not implement {@link HasTimepoint} or {@link HasLabel}.
	 * 
	 * @param <V>
	 *            the vertex type.
	 * @param ref
	 *            a reference of the vertex type.
	 * @return a comparator for vertices.
	 */
	@SuppressWarnings( "unchecked" )
	private static < V > Comparator< V > getVertexComparator( final V ref )
	{
		Comparator< V > cTime = null;
		if ( ref instanceof HasTimepoint )
			cTime = ( Comparator< V > ) Comparator.comparingInt( HasTimepoint::getTimepoint );
		Comparator< V > cRef = null;
		if ( ref instanceof Ref )
			cRef = ( Comparator< V > ) Comparator.comparingInt( v -> ( ( Ref< ? > ) v ).getInternalPoolIndex() );
		Comparator< V > cLabel = null;
		if ( ref instanceof HasLabel )
			cLabel = ( Comparator< V > ) Comparator.comparing( HasLabel::getLabel, String.CASE_INSENSITIVE_ORDER );

		if ( cTime == null )
		{
			if ( cLabel == null )
			{
				if ( cRef == null )
					return Comparator.comparingInt( V::hashCode );
				else
					return cRef;
			}
			else
			{
				return cLabel.thenComparing( cRef == null ? Comparator.comparingInt( V::hashCode ) : cRef );
			}
		}
		else
		{
			if ( cLabel == null )
				return cTime.thenComparing( cRef == null ? Comparator.comparingInt( V::hashCode ) : cRef );
			return cTime.thenComparing( cLabel ).thenComparing( cRef == null ? Comparator.comparingInt( V::hashCode ) : cRef );
		}
	}

	private static < T > String valueAsString( final FeatureProjection< T > projection, final T t, final int width )
	{
		if ( !projection.isSet( t ) )
			return String.format( Locale.US, "%" + width + "s", "unset" );

		if ( projection instanceof IntFeatureProjection )
			return String.format( Locale.US, "%" + width + "d", ( int ) projection.value( t ) );
		else
			return String.format( Locale.US, "%" + width + ".1f", projection.value( t ) );
	}

	private static class TablePrinter< T >
	{

		private final List< Column< T > > columns = new ArrayList<>();

		public void defineColumn( final int width, final String title, final String unit, final Function< T, String > toString )
		{
			columns.add( new Column<>( columns.isEmpty(), width, title, unit, toString ) );
		}

		public void print( final StringBuilder str, final Iterable< T > rows, final long maxLines )
		{
			for ( final Column< T > column : columns )
				str.append( String.format( Locale.US, column.template, column.header ) );
			str.append( '\n' );
			for ( final Column< T > column : columns )
				str.append( String.format( Locale.US, column.template, column.unit ) );
			str.append( '\n' );
			final int totalWidth = columns.stream().mapToInt( c -> c.width + 2 ).sum() - 2;
			for ( int i = 0; i < totalWidth; i++ )
				str.append( '-' );
			str.append( '\n' );
			long i = 0;
			for ( final T row : rows )
			{
				for ( final Column< T > column : columns )
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

		private Column( final boolean isFirst, final int width, final String header, final String unit, final Function< T, String > valueToString )
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

	public static enum DumpFlags
	{
		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will
		 * include the string returned by {@link Model#toString()} and hence the
		 * value of {@link Model#hashCode()}. This can be useful to associate
		 * the "dump" with a specific model instance. But it comes at the cost
		 * of making the output less predictable.
		 */
		PRINT_HASH,

		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will
		 * include the values stored in th {@link Model#getFeatureModel()
		 * feature model}.
		 */
		PRINT_FEATURES,

		/**
		 * If this flag is provided, {@link #dump(Model, DumpFlags...)} will
		 * include the tags stored in the {@link Model#getTagSetModel() tag set
		 * model}.
		 */
		PRINT_TAGS
	}
}
