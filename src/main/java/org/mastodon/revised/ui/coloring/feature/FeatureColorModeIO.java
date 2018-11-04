package org.mastodon.revised.ui.coloring.feature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.revised.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.revised.io.yaml.WorkaroundConstructor;
import org.mastodon.revised.io.yaml.WorkaroundRepresent;
import org.mastodon.revised.io.yaml.WorkaroundRepresenter;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class FeatureColorModeIO
{

	private static class FeatureColorModeRepresenter extends WorkaroundRepresenter
	{
		public FeatureColorModeRepresenter()
		{
			putRepresent( new RepresentFeatureColorMode( this ) );
		}
	}

	private static class FeatureColorModeConstructor extends WorkaroundConstructor
	{
		public FeatureColorModeConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructFeatureColorMode( this ) );
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new FeatureColorModeRepresenter();
		final Constructor constructor = new FeatureColorModeConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	private static final Tag FEATURECOLORMODE_TAG = new Tag( "!featurecolormode" );

	private static class RepresentFeatureColorMode extends WorkaroundRepresent
	{
		public RepresentFeatureColorMode( final WorkaroundRepresenter r )
		{
			super( r, FEATURECOLORMODE_TAG, FeatureColorMode.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final FeatureColorMode s = ( FeatureColorMode ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			mapping.put( "name", s.getName() );

			mapping.put( "vertexColorMode", s.getVertexColorMode().name() );
			mapping.put( "vertexFeatureProjection", s.getVertexFeatureProjection() );
			mapping.put( "vertexColorMap", s.getVertexColorMap() );
			mapping.put( "vertexFeatureRange", new double[] { s.getVertexRangeMin(), s.getVertexRangeMax() } );
			mapping.put( "edgeColorMode", s.getEdgeColorMode().name() );
			mapping.put( "edgeFeatureProjection", s.getEdgeFeatureProjection() );
			mapping.put( "edgeColorMap", s.getEdgeColorMap() );
			mapping.put( "edgeFeatureRange", new double[] { s.getEdgeRangeMin(), s.getEdgeRangeMax() } );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructFeatureColorMode extends AbstractWorkaroundConstruct
	{
		public ConstructFeatureColorMode( final WorkaroundConstructor c )
		{
			super( c, FEATURECOLORMODE_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.get( "name" );
				final FeatureColorMode s = FeatureColorMode.defaultMode().copy( name );

				s.setName( ( String ) mapping.get( "name" ) );

				s.setVertexColorMode( VertexColorMode.valueOf( ( String ) mapping.get( "vertexColorMode" ) ) );
				@SuppressWarnings( "unchecked" )
				final List< String > vertexFeatureKeys = ( List< String > ) mapping.get( "vertexFeatureProjection" );
				s.setVertexFeatureProjection( vertexFeatureKeys.get( 0 ), vertexFeatureKeys.get( 1 ) );
				s.setVertexColorMap( ( String ) mapping.get( "vertexColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > vertexRange = ( List< Double > ) mapping.get( "vertexFeatureRange" );
				s.setVertexRange( vertexRange.get( 0 ), vertexRange.get( 1 ) );
				s.setEdgeColorMode( EdgeColorMode.valueOf( ( String ) mapping.get( "edgeColorMode" ) ) );
				@SuppressWarnings( "unchecked" )
				final List< String > edgeFeatureKeys = ( List< String > ) mapping.get( "edgeFeatureProjection" );
				s.setEdgeFeatureProjection( edgeFeatureKeys.get( 0 ), edgeFeatureKeys.get( 1 ) );
				s.setEdgeColorMap( ( String ) mapping.get( "edgeColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > edgeRange = ( List< Double > ) mapping.get( "edgeFeatureRange" );
				s.setEdgeRange( edgeRange.get( 0 ), edgeRange.get( 1 ) );

				return s;
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}