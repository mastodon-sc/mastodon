package org.mastodon.revised.trackscheme.display.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.revised.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.revised.io.yaml.WorkaroundConstructor;
import org.mastodon.revised.io.yaml.WorkaroundRepresent;
import org.mastodon.revised.io.yaml.WorkaroundRepresenter;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle.ColorEdgeBy;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle.ColorVertexBy;
import org.mastodon.revised.ui.util.ColorMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

class TrackSchemeStyleIO
{

	private static final Tag COLOR_TAG = new Tag( "!color" );

	private static final Tag COLORMAP_TAG = new Tag( "!colormap" );

	private static final Tag COLOREDGEBY_TAG = new Tag( "!colorEdgeBy" );

	private static final Tag COLORVERTEXBY_TAG = new Tag( "!colorVertexBy" );

	private static class TrackSchemeStyleRepresenter extends WorkaroundRepresenter
	{
		public TrackSchemeStyleRepresenter()
		{
			putRepresent( new RepresentColorMap( this ) );
			putRepresent( new RepresentColorEdgeBy( this ) );
			putRepresent( new RepresentColorVertexBy( this ) );
			putRepresent( new RepresentColor( this ) );
			putRepresent( new RepresentBasicStroke( this ) );
			putRepresent( new RepresentFont( this ) );
			putRepresent( new RepresentStyle( this ) );
		}
	}

	private static class TrackschemeStyleConstructor extends WorkaroundConstructor
	{
		public TrackschemeStyleConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructColorMap( this ) );
			putConstruct( new ConstructColorEdgeBy( this ) );
			putConstruct( new ConstructColorVertexBy( this ) );
			putConstruct( new ConstructColor( this ) );
			putConstruct( new ConstructBasicStroke( this ) );
			putConstruct( new ConstructFont( this ) );
			putConstruct( new ConstructStyle( this ) );
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new TrackSchemeStyleRepresenter();
		final Constructor constructor = new TrackschemeStyleConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	private static class RepresentColor extends WorkaroundRepresent
	{
		public RepresentColor( final WorkaroundRepresenter r )
		{
			super( r, COLOR_TAG, Color.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final Color c = ( Color ) data;
			final List< Integer > rgba = Arrays.asList(
					c.getRed(),
					c.getGreen(),
					c.getBlue(),
					c.getAlpha() );
			return representSequence( getTag(), rgba, Boolean.TRUE );
		}
	}

	private static class RepresentColorMap extends WorkaroundRepresent
	{

		public RepresentColorMap( final WorkaroundRepresenter r )
		{
			super( r, COLORMAP_TAG, ColorMap.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final ColorMap cm = ( ColorMap ) data;
			return new ScalarNode( getTag(), cm.getName(), null, null, null );
		}
	}

	private static class RepresentColorEdgeBy extends WorkaroundRepresent
	{

		public RepresentColorEdgeBy( final WorkaroundRepresenter r )
		{
			super( r, COLOREDGEBY_TAG, ColorEdgeBy.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final ColorEdgeBy c = ( ColorEdgeBy ) data;
			return new ScalarNode( getTag(), c.name(), null, null, null );
		}
	}

	private static class RepresentColorVertexBy extends WorkaroundRepresent
	{

		public RepresentColorVertexBy( final WorkaroundRepresenter r )
		{
			super( r, COLORVERTEXBY_TAG, ColorVertexBy.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final ColorVertexBy c = ( ColorVertexBy ) data;
			return new ScalarNode( getTag(), c.name(), null, null, null );
		}
	}

	private static class ConstructColor extends AbstractWorkaroundConstruct
	{
		public ConstructColor( final WorkaroundConstructor c )
		{
			super( c, COLOR_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				@SuppressWarnings( "unchecked" )
				final List< Integer > rgba = ( List< Integer > ) constructSequence( ( SequenceNode ) node );
				return new Color(
						rgba.get( 0 ),
						rgba.get( 1 ),
						rgba.get( 2 ),
						rgba.get( 3 ) );
			}
			catch( final Exception e )
			{}
			return null;
		}
	}

	private static final Tag STROKE_TAG = new Tag( "!stroke" );

	private static class RepresentBasicStroke extends WorkaroundRepresent
	{
		public RepresentBasicStroke( final WorkaroundRepresenter r )
		{
			super( r, STROKE_TAG, BasicStroke.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final BasicStroke s = ( BasicStroke ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();
			mapping.put( "width", s.getLineWidth() );
			mapping.put( "cap", s.getEndCap() );
			mapping.put( "join", s.getLineJoin() );
			mapping.put( "miterlimit", s.getMiterLimit() );
			ArrayList< Float > dash = null;
			final float[] dashArray = s.getDashArray();
			if ( dashArray != null )
			{
				dash = new ArrayList<>();
				for ( final float f : dashArray )
					dash.add( f );
			}
			mapping.put( "dash", dash );
			mapping.put( "dash_phase", s.getDashPhase() );
			final Node node = representMapping( getTag(), mapping, Boolean.TRUE );
			return node;
		}
	}

	private static class ConstructBasicStroke extends AbstractWorkaroundConstruct
	{
		public ConstructBasicStroke( final WorkaroundConstructor c )
		{
			super( c, STROKE_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode  ) node );
				final float width = ( ( Double ) mapping.get( "width" ) ).floatValue();
				final int cap = ( Integer ) mapping.get( "cap" );
				final int join = ( Integer ) mapping.get( "join" );
				final float miterlimit = ( ( Double ) mapping.get( "miterlimit" ) ).floatValue();
				@SuppressWarnings( "unchecked" )
				final List< Double > list = ( List< Double > ) mapping.get( "dash" );
				float[] dash = null;
				if ( list != null && !list.isEmpty() )
				{
					dash = new float[ list.size() ];
					int i = 0;
					for ( final double d : list )
						dash[ i++ ] = ( float ) d;
				}
				final float dash_phase = ( ( Double ) mapping.get( "dash_phase" ) ).floatValue();
				return new BasicStroke( width, cap, join, miterlimit, dash, dash_phase );
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static final Tag FONT_TAG = new Tag( "!font" );

	private static class RepresentFont extends WorkaroundRepresent
	{
		public RepresentFont( final WorkaroundRepresenter r )
		{
			super( r, FONT_TAG, Font.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final Font f = ( Font ) data;
			final Map< String, Object > mapping = new LinkedHashMap< >();
			mapping.put( "name", f.getName() );
			mapping.put( "style", f.getStyle() );
			mapping.put( "size", f.getSize() );
			final Node node = representMapping( getTag(), mapping, Boolean.TRUE );
			return node;
		}
	}

	private static class ConstructFont extends AbstractWorkaroundConstruct
	{
		public ConstructFont( final WorkaroundConstructor c )
		{
			super( c, FONT_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode  ) node );
				final String name = ( String ) mapping.get( "name" );
				final int style = ( Integer ) mapping.get( "style" );
				final int size = ( Integer ) mapping.get( "size" );
				return new Font( name, style, size );
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static class ConstructColorMap extends AbstractWorkaroundConstruct
	{

		public ConstructColorMap( final WorkaroundConstructor c )
		{
			super( c, COLORMAP_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final String cmName = ( ( ScalarNode ) node ).getValue();
				return ColorMap.getColorMap( cmName );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}

	}

	private static final class ConstructColorEdgeBy extends AbstractWorkaroundConstruct
	{

		public ConstructColorEdgeBy( final WorkaroundConstructor c )
		{
			super( c, COLOREDGEBY_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final String cmName = ( ( ScalarNode ) node ).getValue();
				return ColorEdgeBy.valueOf( cmName );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}

	}

	private static final class ConstructColorVertexBy extends AbstractWorkaroundConstruct
	{

		public ConstructColorVertexBy( final WorkaroundConstructor c )
		{
			super( c, COLORVERTEXBY_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final String cmName = ( ( ScalarNode ) node ).getValue();
				return ColorVertexBy.valueOf( cmName );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}

	}

	private static final Tag STYLE_TAG = new Tag( "!trackschemestyle" );

	private static class RepresentStyle extends WorkaroundRepresent
	{
		public RepresentStyle( final WorkaroundRepresenter r )
		{
			super( r, STYLE_TAG, TrackSchemeStyle.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final TrackSchemeStyle s = ( TrackSchemeStyle ) data;
			final Map< String, Object > mapping = new LinkedHashMap< >();

			// Name.
			mapping.put( "name", s.name );
			// Color edge strategy.
			mapping.put( "colorEdgeBy", s.colorEdgeBy );
			mapping.put( "edgeColorFeatureKey", s.edgeColorFeatureKey );
			mapping.put( "edgeColorMap", s.edgeColorMap );
			mapping.put( "edgeColorRange", new double[] { s.minEdgeColorRange, s.maxEdgeColorRange } );
			// Color vertex strategy.
			mapping.put( "colorVertexBy", s.colorVertexBy );
			mapping.put( "vertexColorFeatureKey", s.vertexColorFeatureKey );
			mapping.put( "vertexColorMap", s.vertexColorMap );
			mapping.put( "vertexColorRange", new double[] { s.minVertexColorRange, s.maxVertexColorRange } );
			// Fixed colors.
			mapping.put( "edgeColor", s.edgeColor );
			mapping.put( "vertexFillColor", s.vertexFillColor );
			mapping.put( "vertexDrawColor", s.vertexDrawColor );
			// Selection colors.
			mapping.put( "selectedVertexFillColor", s.selectedVertexFillColor );
			mapping.put( "selectedEdgeColor", s.selectedEdgeColor );
			mapping.put( "selectedVertexDrawColor", s.selectedVertexDrawColor );
			mapping.put( "simplifiedVertexFillColor", s.simplifiedVertexFillColor );
			mapping.put( "selectedSimplifiedVertexFillColor", s.selectedSimplifiedVertexFillColor );
			// Decoration colors.
			mapping.put( "backgroundColor", s.backgroundColor );
			mapping.put( "currentTimepointColor", s.currentTimepointColor );
			mapping.put( "decorationColor", s.decorationColor );
			mapping.put( "vertexRangeColor", s.vertexRangeColor );
			mapping.put( "headerBackgroundColor", s.headerBackgroundColor );
			mapping.put( "headerDecorationColor", s.headerDecorationColor );
			mapping.put( "headerCurrentTimepointColor", s.headerCurrentTimepointColor );
			// Fonts.
			mapping.put( "font", s.font );
			mapping.put( "headerFont", s.headerFont );
			// Strokes.
			mapping.put( "edgeStroke", s.edgeStroke );
			mapping.put( "edgeGhostStroke", s.edgeGhostStroke );
			mapping.put( "edgeHighlightStroke", s.edgeHighlightStroke );
			mapping.put( "vertexStroke", s.vertexStroke );
			mapping.put( "vertexGhostStroke", s.vertexGhostStroke );
			mapping.put( "vertexHighlightStroke", s.vertexHighlightStroke );
			mapping.put( "focusStroke", s.focusStroke );
			// Paint decorations.
			mapping.put( "highlightCurrentTimepoint", s.highlightCurrentTimepoint );
			mapping.put( "paintRows", s.paintRows );
			mapping.put( "paintColumns", s.paintColumns );
			mapping.put( "paintHeaderShadow", s.paintHeaderShadow );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructStyle extends AbstractWorkaroundConstruct
	{
		public ConstructStyle( final WorkaroundConstructor c )
		{
			super( c, STYLE_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode  ) node );
				final String name = ( String ) mapping.get( "name" );
				final TrackSchemeStyle s = TrackSchemeStyle.defaultStyle().copy( name );

				s.colorEdgeBy( ( ColorEdgeBy ) mapping.get( "colorEdgeBy" ) );
				s.edgeColorFeatureKey( ( String ) mapping.get( "edgeColorFeatureKey" ) );
				s.edgeColorMap( ( ColorMap ) mapping.get( "edgeColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > edgeColorRange = ( List< Double > ) mapping.get( "edgeColorRange" );
				if ( null == edgeColorRange )
				{
					s.minEdgeColorRange( 0. );
					s.maxEdgeColorRange( 1. );
				}
				else
				{
					s.minEdgeColorRange( edgeColorRange.get( 0 ) );
					s.maxEdgeColorRange( edgeColorRange.get( 1 ) );
				}

				s.colorVertexBy( ( ColorVertexBy ) mapping.get( "colorVertexBy" ) );
				s.vertexColorFeatureKey( ( String ) mapping.get( "vertexColorFeatureKey" ) );
				s.vertexColorMap( ( ColorMap ) mapping.get( "vertexColorMap" ) );
				@SuppressWarnings( "unchecked" )
				final List< Double > vertexColorRange = ( List< Double > ) mapping.get( "vertexColorRange" );
				if ( null == vertexColorRange )
				{
					s.minVertexColorRange( 0. );
					s.maxVertexColorRange( 1. );
				}
				else
				{
					s.minVertexColorRange( vertexColorRange.get( 0 ) );
					s.maxVertexColorRange( vertexColorRange.get( 1 ) );
				}

				s.edgeColor( ( Color ) mapping.get( "edgeColor" ) );
				s.vertexFillColor( ( Color ) mapping.get( "vertexFillColor" ) );
				s.vertexDrawColor( ( Color ) mapping.get( "vertexDrawColor" ) );
				s.selectedVertexFillColor( ( Color ) mapping.get( "selectedVertexFillColor" ) );
				s.selectedEdgeColor( ( Color ) mapping.get( "selectedEdgeColor" ) );
				s.selectedVertexDrawColor( ( Color ) mapping.get( "selectedVertexDrawColor" ) );
				s.simplifiedVertexFillColor( ( Color ) mapping.get( "simplifiedVertexFillColor" ) );
				s.selectedSimplifiedVertexFillColor( ( Color ) mapping.get( "selectedSimplifiedVertexFillColor" ) );
				s.backgroundColor( ( Color ) mapping.get( "backgroundColor" ) );
				s.currentTimepointColor( ( Color ) mapping.get( "currentTimepointColor" ) );
				s.decorationColor( ( Color ) mapping.get( "decorationColor" ) );
				s.vertexRangeColor( ( Color ) mapping.get( "vertexRangeColor" ) );
				s.headerBackgroundColor( ( Color ) mapping.get( "headerBackgroundColor" ) );
				s.headerDecorationColor( ( Color ) mapping.get( "headerDecorationColor" ) );
				s.headerCurrentTimepointColor( ( Color ) mapping.get( "headerCurrentTimepointColor" ) );

				s.font( ( Font ) mapping.get( "font" ) );
				s.headerFont( ( Font ) mapping.get( "headerFont" ) );

				s.edgeStroke( ( Stroke ) mapping.get( "edgeStroke" ) );
				s.edgeGhostStroke( ( Stroke ) mapping.get( "edgeGhostStroke" ) );
				s.edgeHighlightStroke( ( Stroke ) mapping.get( "edgeHighlightStroke" ) );
				s.vertexStroke( ( Stroke ) mapping.get( "vertexStroke" ) );
				s.vertexGhostStroke( ( Stroke ) mapping.get( "vertexGhostStroke" ) );
				s.vertexHighlightStroke( ( Stroke ) mapping.get( "vertexHighlightStroke" ) );
				s.focusStroke( ( Stroke ) mapping.get( "focusStroke" ) );

				s.highlightCurrentTimepoint( ( boolean ) mapping.get( "highlightCurrentTimepoint" ) );
				s.paintRows( ( boolean ) mapping.get( "paintRows" ) );
				s.paintColumns( ( boolean ) mapping.get( "paintColumns" ) );
				s.paintHeaderShadow( ( boolean ) mapping.get( "paintHeaderShadow" ) );

				return s;
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}