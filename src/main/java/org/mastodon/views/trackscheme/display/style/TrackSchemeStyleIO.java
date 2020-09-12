package org.mastodon.views.trackscheme.display.style;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class TrackSchemeStyleIO
{
	public static final Tag COLOR_TAG = new Tag( "!color" );

	static class TrackSchemeStyleRepresenter extends WorkaroundRepresenter
	{
		public TrackSchemeStyleRepresenter()
		{
			putRepresent( new RepresentColor( this ) );
			putRepresent( new RepresentBasicStroke( this ) );
			putRepresent( new RepresentFont( this ) );
			putRepresent( new RepresentStyle( this ) );
		}
	}

	static class TrackschemeStyleConstructor extends WorkaroundConstructor
	{
		public TrackschemeStyleConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructColor( this ) );
			putConstruct( new ConstructBasicStroke( this ) );
			putConstruct( new ConstructFont( this ) );
			putConstruct( new ConstructStyle( this ) );
		}
	}

	public // TODO?
	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new TrackSchemeStyleRepresenter();
		final Constructor constructor = new TrackschemeStyleConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	public static class RepresentColor extends WorkaroundRepresent
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
			return representSequence( getTag(), rgba, FLOW );
		}
	}

	public static class ConstructColor extends AbstractWorkaroundConstruct
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
			catch ( final Exception e )
			{}
			return null;
		}
	}

	public static final Tag STROKE_TAG = new Tag( "!stroke" );

	public static class RepresentBasicStroke extends WorkaroundRepresent
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
			final Node node = representMapping( getTag(), mapping, FLOW );
			return node;
		}
	}

	public static class ConstructBasicStroke extends AbstractWorkaroundConstruct
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
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
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
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	public static final Tag FONT_TAG = new Tag( "!font" );

	public static class RepresentFont extends WorkaroundRepresent
	{
		public RepresentFont( final WorkaroundRepresenter r )
		{
			super( r, FONT_TAG, Font.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final Font f = ( Font ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();
			mapping.put( "name", f.getName() );
			mapping.put( "style", f.getStyle() );
			mapping.put( "size", f.getSize() );
			final Node node = representMapping( getTag(), mapping, FLOW );
			return node;
		}
	}

	public static class ConstructFont extends AbstractWorkaroundConstruct
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
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.get( "name" );
				final int style = ( Integer ) mapping.get( "style" );
				final int size = ( Integer ) mapping.get( "size" );
				return new Font( name, style, size );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	public static final Tag STYLE_TAG = new Tag( "!trackschemestyle" );

	public static class RepresentStyle extends WorkaroundRepresent
	{
		public RepresentStyle( final WorkaroundRepresenter r )
		{
			super( r, STYLE_TAG, TrackSchemeStyle.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final TrackSchemeStyle s = ( TrackSchemeStyle ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			// Name.
			mapping.put( "name", s.getName() );
			// Fixed colors.
			mapping.put( "edgeColor", s.getEdgeColor() );
			mapping.put( "vertexFillColor", s.getVertexFillColor() );
			mapping.put( "vertexDrawColor", s.getVertexDrawColor() );
			// Selection colors.
			mapping.put( "selectedVertexFillColor", s.getSelectedVertexFillColor() );
			mapping.put( "selectedEdgeColor", s.getSelectedEdgeColor() );
			mapping.put( "selectedVertexDrawColor", s.getSelectedVertexDrawColor() );
			mapping.put( "simplifiedVertexFillColor", s.getSimplifiedVertexFillColor() );
			mapping.put( "selectedSimplifiedVertexFillColor", s.getSelectedSimplifiedVertexFillColor() );
			// Decoration colors.
			mapping.put( "backgroundColor", s.getBackgroundColor() );
			mapping.put( "currentTimepointColor", s.getCurrentTimepointColor() );
			mapping.put( "decorationColor", s.getDecorationColor() );
			mapping.put( "vertexRangeColor", s.getVertexRangeColor() );
			mapping.put( "headerBackgroundColor", s.getHeaderBackgroundColor() );
			mapping.put( "headerDecorationColor", s.getHeaderDecorationColor() );
			mapping.put( "headerCurrentTimepointColor", s.getHeaderCurrentTimepointColor() );
			// Fonts.
			mapping.put( "font", s.getFont() );
			mapping.put( "headerFont", s.getHeaderFont() );
			// Strokes.
			mapping.put( "edgeStroke", s.getEdgeStroke() );
			mapping.put( "edgeGhostStroke", s.getEdgeGhostStroke() );
			mapping.put( "edgeHighlightStroke", s.getEdgeHighlightStroke() );
			mapping.put( "vertexStroke", s.getVertexStroke() );
			mapping.put( "vertexGhostStroke", s.getVertexGhostStroke() );
			mapping.put( "vertexHighlightStroke", s.getVertexHighlightStroke() );
			mapping.put( "focusStroke", s.getFocusStroke() );
			// Paint decorations.
			mapping.put( "highlightCurrentTimepoint", s.isHighlightCurrentTimepoint() );
			mapping.put( "paintRows", s.isPaintRows() );
			mapping.put( "paintColumns", s.isPaintColumns() );
			mapping.put( "paintHeaderShadow", s.isPaintHeaderShadow() );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	public static class ConstructStyle extends AbstractWorkaroundConstruct
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
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.getOrDefault( "name", "NameNotFound" );
				final TrackSchemeStyle s = TrackSchemeStyle.defaultStyle().copy( name );
				final TrackSchemeStyle df = TrackSchemeStyle.defaultStyle();

				s.edgeColor( ( Color ) mapping.getOrDefault( "edgeColor", df.getEdgeColor() ) );
				s.vertexFillColor( ( Color ) mapping.getOrDefault( "vertexFillColor", df.getVertexFillColor() ) );
				s.vertexDrawColor( ( Color ) mapping.getOrDefault( "vertexDrawColor", df.getVertexDrawColor() ) );
				s.selectedVertexFillColor( ( Color ) mapping.getOrDefault( "selectedVertexFillColor", df.getSelectedVertexFillColor() ) );
				s.selectedEdgeColor( ( Color ) mapping.getOrDefault( "selectedEdgeColor", df.getSelectedEdgeColor() ) );
				s.selectedVertexDrawColor( ( Color ) mapping.getOrDefault( "selectedVertexDrawColor", df.getSelectedVertexDrawColor() ) );
				s.simplifiedVertexFillColor( ( Color ) mapping.getOrDefault( "simplifiedVertexFillColor", df.getSimplifiedVertexFillColor() ) );
				s.selectedSimplifiedVertexFillColor( ( Color ) mapping.getOrDefault( "selectedSimplifiedVertexFillColor", df.getSelectedSimplifiedVertexFillColor() ) );
				s.backgroundColor( ( Color ) mapping.getOrDefault( "backgroundColor", df.getBackgroundColor() ) );
				s.currentTimepointColor( ( Color ) mapping.getOrDefault( "currentTimepointColor", df.getCurrentTimepointColor() ) );
				s.decorationColor( ( Color ) mapping.getOrDefault( "decorationColor", df.getDecorationColor() ) );
				s.vertexRangeColor( ( Color ) mapping.getOrDefault( "vertexRangeColor", df.getVertexRangeColor() ) );
				s.headerBackgroundColor( ( Color ) mapping.getOrDefault( "headerBackgroundColor", df.getHeaderBackgroundColor() ) );
				s.headerDecorationColor( ( Color ) mapping.getOrDefault( "headerDecorationColor", df.getHeaderDecorationColor() ) );
				s.headerCurrentTimepointColor( ( Color ) mapping.getOrDefault( "headerCurrentTimepointColor", df.getCurrentTimepointColor() ) );

				s.font( ( Font ) mapping.getOrDefault( "font", df.getFont() ) );
				s.headerFont( ( Font ) mapping.getOrDefault( "headerFont", df.getHeaderFont() ) );

				s.edgeStroke( ( Stroke ) mapping.getOrDefault( "edgeStroke", df.getEdgeStroke() ) );
				s.edgeGhostStroke( ( Stroke ) mapping.getOrDefault( "edgeGhostStroke", df.getEdgeGhostStroke() ) );
				s.edgeHighlightStroke( ( Stroke ) mapping.getOrDefault( "edgeHighlightStroke", df.getEdgeHighlightStroke() ) );
				s.vertexStroke( ( Stroke ) mapping.getOrDefault( "vertexStroke", df.getVertexStroke() ) );
				s.vertexGhostStroke( ( Stroke ) mapping.getOrDefault( "vertexGhostStroke", df.getVertexGhostStroke() ) );
				s.vertexHighlightStroke( ( Stroke ) mapping.getOrDefault( "vertexHighlightStroke", df.getVertexHighlightStroke() ) );
				s.focusStroke( ( Stroke ) mapping.getOrDefault( "focusStroke", df.getFocusStroke() ) );

				s.highlightCurrentTimepoint( ( boolean ) mapping.getOrDefault( "highlightCurrentTimepoint", df.isHighlightCurrentTimepoint() ) );
				s.paintRows( ( boolean ) mapping.getOrDefault( "paintRows", df.isPaintRows() ) );
				s.paintColumns( ( boolean ) mapping.getOrDefault( "paintColumns", df.isPaintColumns() ) );
				s.paintHeaderShadow( ( boolean ) mapping.getOrDefault( "paintHeaderShadow", df.isPaintHeaderShadow() ) );

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
