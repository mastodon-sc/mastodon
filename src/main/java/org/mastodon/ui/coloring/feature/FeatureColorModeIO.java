package org.mastodon.ui.coloring.feature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.mastodon.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;

public class FeatureColorModeIO
{
	private static class FeatureColorModeRepresenter extends WorkaroundRepresenter
	{
		public FeatureColorModeRepresenter()
		{
			putRepresent( new RepresentFeatureProjectionId( this ) );
			putRepresent( new RepresentFeatureColorMode( this ) );
		}
	}

	private static class FeatureColorModeConstructor extends WorkaroundConstructor
	{
		public FeatureColorModeConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructFeatureProjectionId( this ) );
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

	public static final Tag FEATURE_PROJECTION_ID_TAG = new Tag( "!featureprojection" );

	public static class RepresentFeatureProjectionId extends WorkaroundRepresent
	{
		public RepresentFeatureProjectionId( final WorkaroundRepresenter r )
		{
			super( r, FEATURE_PROJECTION_ID_TAG, FeatureProjectionId.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final FeatureProjectionId p = ( FeatureProjectionId ) data;
			final Map< String, Object > mapping = new LinkedHashMap< >();
			mapping.put( "feature", p.getFeatureKey() );
			mapping.put( "projection", p.getProjectionKey() );
			mapping.put( "target", p.getTargetType().name() );
			mapping.put( "i0", p.getI0() );
			mapping.put( "i1", p.getI1() );
			final Node node = representMapping( getTag(), mapping, FLOW );
			return node;
		}
	}

	public static class ConstructFeatureProjectionId extends AbstractWorkaroundConstruct
	{
		public ConstructFeatureProjectionId( final WorkaroundConstructor c )
		{
			super( c, FEATURE_PROJECTION_ID_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode  ) node );
				final String featureKey = ( String ) mapping.get( "feature" );
				final String projectionKey = ( String ) mapping.get( "projection" );
				final TargetType targetType = TargetType.valueOf( ( String ) mapping.get( "target" ) );
				final int i0 = ( Integer ) mapping.get( "i0" );
				final int i1 = ( Integer ) mapping.get( "i1" );
				return new FeatureProjectionId( featureKey, projectionKey, targetType, i0, i1 );
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
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

				s.setVertexColorMode( VertexColorMode.valueOf( ( String ) mapping.get( "vertexColorMode" ) ) );
				s.setVertexFeatureProjection( ( FeatureProjectionId ) mapping.get( "vertexFeatureProjection" ) );
				s.setVertexColorMap( ( String ) mapping.get( "vertexColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > vertexRange = ( List< Double > ) mapping.get( "vertexFeatureRange" );
				s.setVertexRange( vertexRange.get( 0 ), vertexRange.get( 1 ) );
				s.setEdgeColorMode( EdgeColorMode.valueOf( ( String ) mapping.get( "edgeColorMode" ) ) );
				s.setEdgeFeatureProjection( ( FeatureProjectionId )mapping.get( "edgeFeatureProjection" ) );
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
