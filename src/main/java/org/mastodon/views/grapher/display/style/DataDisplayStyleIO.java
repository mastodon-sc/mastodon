/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display.style;

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
import org.mastodon.views.grapher.display.PaintGraph.VertexDrawShape;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class DataDisplayStyleIO
{
	public static final Tag COLOR_TAG = new Tag( "!color" );

	static class DataGraphStyleRepresenter extends WorkaroundRepresenter
	{
		public DataGraphStyleRepresenter()
		{
			putRepresent( new RepresentColor( this ) );
			putRepresent( new RepresentBasicStroke( this ) );
			putRepresent( new RepresentFont( this ) );
			putRepresent( new RepresentStyle( this ) );
		}
	}

	static class DataGraphConstructor extends WorkaroundConstructor
	{
		public DataGraphConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructColor( this ) );
			putConstruct( new ConstructBasicStroke( this ) );
			putConstruct( new ConstructFont( this ) );
			putConstruct( new ConstructStyle( this ) );
		}
	}

	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new DataGraphStyleRepresenter();
		final Constructor constructor = new DataGraphConstructor();
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

	public static final Tag STYLE_TAG = new Tag( "!datagraphstyle" );

	public static class RepresentStyle extends WorkaroundRepresent
	{
		public RepresentStyle( final WorkaroundRepresenter r )
		{
			super( r, STYLE_TAG, DataDisplayStyle.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final DataDisplayStyle s = ( DataDisplayStyle ) data;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			// Name.
			mapping.put( "name", s.getName() );
			// Vertex shape and size.
			mapping.put( "autoVertexSize", s.isAutoVertexSize() );
			mapping.put( "vertexFixedSize", s.getVertexFixedSize() );
			mapping.put( "vertexDrawShape", s.getVertexDrawShape().name() );
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
			mapping.put( "axisColor", s.getAxisColor() );
			// Fonts.
			mapping.put( "font", s.getFont() );
			mapping.put( "axisLabelFont", s.getAxisLabelFont() );
			mapping.put( "axisTickFont", s.getAxisTickFont() );
			// Strokes.
			mapping.put( "axisStroke", s.getAxisStroke() );
			mapping.put( "edgeStroke", s.getEdgeStroke() );
			mapping.put( "edgeHighlightStroke", s.getEdgeHighlightStroke() );
			mapping.put( "vertexStroke", s.getVertexStroke() );
			mapping.put( "vertexHighlightStroke", s.getVertexHighlightStroke() );
			mapping.put( "focusStroke", s.getFocusStroke() );
			// Paint decorations.
			mapping.put( "drawVertexName", s.isDrawVertexName() );

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
				final DataDisplayStyle s = DataDisplayStyle.defaultStyle().copy( name );
				final DataDisplayStyle df = DataDisplayStyle.defaultStyle();

				s.autoVertexSize( getBooleanOrDefault( mapping, "autoVertexSize", df.isAutoVertexSize() ) );
				s.vertexFixedSize( getDoubleOrDefault( mapping, "vertexFixedSize", df.getVertexFixedSize() ) );
				s.vertexDrawShape( VertexDrawShape.valueOf( getStringOrDefault( mapping, "vertexDrawShape", df.getVertexDrawShape().name() ) ) );

				s.edgeColor( ( Color ) mapping.getOrDefault( "edgeColor", df.getEdgeColor() ) );
				s.vertexFillColor( ( Color ) mapping.getOrDefault( "vertexFillColor", df.getVertexFillColor() ) );
				s.vertexDrawColor( ( Color ) mapping.getOrDefault( "vertexDrawColor", df.getVertexDrawColor() ) );

				s.selectedVertexFillColor( ( Color ) mapping.getOrDefault( "selectedVertexFillColor", df.getSelectedVertexFillColor() ) );
				s.selectedEdgeColor( ( Color ) mapping.getOrDefault( "selectedEdgeColor", df.getSelectedEdgeColor() ) );
				s.selectedVertexDrawColor( ( Color ) mapping.getOrDefault( "selectedVertexDrawColor", df.getSelectedVertexDrawColor() ) );
				s.simplifiedVertexFillColor( ( Color ) mapping.getOrDefault( "simplifiedVertexFillColor", df.getSimplifiedVertexFillColor() ) );
				s.selectedSimplifiedVertexFillColor( ( Color ) mapping.getOrDefault( "selectedSimplifiedVertexFillColor", df.getSelectedSimplifiedVertexFillColor() ) );

				s.backgroundColor( ( Color ) mapping.getOrDefault( "backgroundColor", df.getBackgroundColor() ) );
				s.axisColor( ( Color ) mapping.getOrDefault( "axisColor", df.getAxisColor() ) );

				s.font( ( Font ) mapping.getOrDefault( "font", df.getFont() ) );
				s.axisLabelFont( ( Font ) mapping.getOrDefault( "axisLabelFont", df.getAxisLabelFont() ) );
				s.axisTickFont( ( Font ) mapping.getOrDefault( "axisTickFont", df.getAxisTickFont() ) );

				s.axisStroke( ( Stroke ) mapping.getOrDefault( "axisStroke", df.getAxisStroke() ) );
				s.edgeStroke( ( Stroke ) mapping.getOrDefault( "edgeStroke", df.getEdgeStroke() ) );
				s.edgeHighlightStroke( ( Stroke ) mapping.getOrDefault( "edgeHighlightStroke", df.getEdgeHighlightStroke() ) );
				s.vertexStroke( ( Stroke ) mapping.getOrDefault( "vertexStroke", df.getVertexStroke() ) );
				s.vertexHighlightStroke( ( Stroke ) mapping.getOrDefault( "vertexHighlightStroke", df.getVertexHighlightStroke() ) );
				s.focusStroke( ( Stroke ) mapping.getOrDefault( "focusStroke", df.getFocusStroke() ) );

				s.drawVertexName( getBooleanOrDefault( mapping, "drawVertexName", df.isDrawVertexName() ) );

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
