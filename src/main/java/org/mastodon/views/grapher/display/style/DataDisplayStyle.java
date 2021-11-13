/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.mastodon.app.ui.settings.style.Style;
import org.mastodon.views.grapher.display.PaintGraph.VertexDrawShape;
import org.scijava.listeners.Listeners;

public class DataDisplayStyle implements Style< DataDisplayStyle >
{
	private static final Stroke DEFAULT_FOCUS_STROKE = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );

	private String name;

	private Color edgeColor;

	private Color vertexFillColor;

	private Color vertexDrawColor;

	private Color selectedVertexFillColor;

	private Color selectedEdgeColor;

	private Color selectedVertexDrawColor;

	private Color simplifiedVertexFillColor;

	private Color selectedSimplifiedVertexFillColor;

	private Color backgroundColor;

	private Color axisColor;

	private Font font;

	private Font axisTickFont;

	private Font axisLabelFont;

	private Stroke edgeStroke;

	private Stroke edgeHighlightStroke;

	private VertexDrawShape vertexDrawShape;

	private boolean autoVertexSize;

	private double vertexFixedSize;

	private Stroke vertexStroke;

	private Stroke vertexHighlightStroke;

	private Stroke focusStroke;

	private Stroke axisStroke;

	private boolean drawVertexName;
	
	/*
	 * GETTERS for non public fields.
	 */

	@Override
	public String getName()
	{
		return name;
	}

	public boolean isAutoVertexSize()
	{
		return autoVertexSize;
	}

	public Color getAxisColor()
	{
		return axisColor;
	}

	public Font getAxisLabelFont()
	{
		return axisLabelFont;
	}

	public Stroke getAxisStroke()
	{
		return axisStroke;
	}

	public Font getAxisTickFont()
	{
		return axisTickFont;
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public boolean isDrawVertexName()
	{
		return drawVertexName;
	}

	public Color getEdgeColor()
	{
		return edgeColor;
	}

	public Stroke getEdgeHighlightStroke()
	{
		return edgeHighlightStroke;
	}

	public Stroke getEdgeStroke()
	{
		return edgeStroke;
	}

	public Stroke getFocusStroke()
	{
		return focusStroke;
	}

	public Font getFont()
	{
		return font;
	}

	public Color getSelectedEdgeColor()
	{
		return selectedEdgeColor;
	}

	public Color getSelectedSimplifiedVertexFillColor()
	{
		return selectedSimplifiedVertexFillColor;
	}

	public Color getSelectedVertexDrawColor()
	{
		return selectedVertexDrawColor;
	}

	public Color getSelectedVertexFillColor()
	{
		return selectedVertexFillColor;
	}

	public Color getVertexDrawColor()
	{
		return vertexDrawColor;
	}

	public VertexDrawShape getVertexDrawShape()
	{
		return vertexDrawShape;
	}

	public Color getVertexFillColor()
	{
		return vertexFillColor;
	}

	public double getVertexFixedSize()
	{
		return vertexFixedSize;
	}

	public Color getSimplifiedVertexFillColor()
	{
		return simplifiedVertexFillColor;
	}

	public Stroke getVertexHighlightStroke()
	{
		return vertexHighlightStroke;
	}
	public Stroke getVertexStroke()
	{
		return vertexStroke;
	}

	/*
	 * SETTERS
	 */

	public DataDisplayStyle name( final String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
			notifyListeners();
		}
		return this;
	}

	@Override
	public void setName( final String name )
	{
		name( name );
	}

	public DataDisplayStyle autoVertexSize( final boolean autoVertexSize )
	{
		if ( this.autoVertexSize != autoVertexSize )
		{
			this.autoVertexSize = autoVertexSize;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle axisColor( final Color color )
	{
		if ( !Objects.equals( this.axisColor, color ) )
		{
			this.axisColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle axisLabelFont( final Font axisLabelFont )
	{
		if ( !Objects.equals( this.axisLabelFont, axisLabelFont ) )
		{
			this.axisLabelFont = axisLabelFont;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle axisStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.axisStroke, stroke ) )
		{
			this.axisStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle axisTickFont( final Font font )
	{
		if ( !Objects.equals( this.axisTickFont, font ) )
		{
			this.axisTickFont = font;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle backgroundColor( final Color color )
	{
		if ( !Objects.equals( this.backgroundColor, color ) )
		{
			this.backgroundColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle drawVertexName( final boolean drawVertexName )
	{
		if ( this.drawVertexName != drawVertexName )
		{
			this.drawVertexName = drawVertexName;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle edgeColor( final Color color )
	{
		if ( !Objects.equals( this.edgeColor, color ) )
		{
			this.edgeColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle edgeHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.edgeHighlightStroke, stroke ) )
		{
			this.edgeHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle edgeStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.edgeStroke, stroke ) )
		{
			this.edgeStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle focusStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.focusStroke, stroke ) )
		{
			this.focusStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle font( final Font font )
	{
		if ( !Objects.equals( this.font, font ) )
		{
			this.font = font;
			notifyListeners();
		}
		return this;
	}


	public DataDisplayStyle selectedEdgeColor( final Color color )
	{
		if ( !Objects.equals( this.selectedEdgeColor, color ) )
		{
			this.selectedEdgeColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle selectedVertexDrawColor( final Color color )
	{
		if ( !Objects.equals( this.selectedVertexDrawColor, color ) )
		{
			this.selectedVertexDrawColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle selectedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.selectedVertexFillColor, color ) )
		{
			this.selectedVertexFillColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle selectedSimplifiedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.selectedSimplifiedVertexFillColor, color ) )
		{
			this.selectedSimplifiedVertexFillColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle simplifiedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.simplifiedVertexFillColor, color ) )
		{
			this.simplifiedVertexFillColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexDrawColor( final Color color )
	{
		if ( !Objects.equals( this.vertexDrawColor, color ) )
		{
			this.vertexDrawColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexDrawShape( final VertexDrawShape vertexDrawShape )
	{
		if ( !Objects.equals( this.vertexDrawShape, vertexDrawShape ) )
		{
			this.vertexDrawShape = vertexDrawShape;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.vertexFillColor, color ) )
		{
			this.vertexFillColor = color;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexFixedSize( final double vertexFixedSize )
	{
		if ( this.vertexFixedSize != vertexFixedSize )
		{
			this.vertexFixedSize = vertexFixedSize;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.vertexHighlightStroke, stroke ) )
		{
			this.vertexHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public DataDisplayStyle vertexStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.vertexStroke, stroke ) )
		{
			this.vertexStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	/*
	 * 
	 */

	public interface UpdateListener
	{
		void dataGraphStyleChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	private DataDisplayStyle()
	{
		updateListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public String toString()
	{
		return name;
	}

	public synchronized void set( final DataDisplayStyle style )
	{
		this.name = style.name;
		this.autoVertexSize = style.autoVertexSize;
		this.axisColor = style.axisColor;
		this.axisLabelFont = style.axisLabelFont;
		this.axisStroke = style.axisStroke;
		this.axisTickFont = style.axisTickFont;
		this.backgroundColor = style.backgroundColor;
		this.drawVertexName = style.drawVertexName;
		this.edgeColor = style.edgeColor;
		this.edgeHighlightStroke = style.edgeHighlightStroke;
		this.edgeStroke = style.edgeStroke;
		this.focusStroke = style.focusStroke;
		this.font = style.font;
		this.selectedEdgeColor = style.selectedEdgeColor;
		this.selectedSimplifiedVertexFillColor = style.selectedSimplifiedVertexFillColor;
		this.selectedVertexDrawColor = style.selectedVertexDrawColor;
		this.selectedVertexFillColor = style.selectedVertexFillColor;
		this.simplifiedVertexFillColor = style.simplifiedVertexFillColor;
		this.vertexDrawColor = style.vertexDrawColor;
		this.vertexDrawShape = style.vertexDrawShape;
		this.vertexFillColor = style.vertexFillColor;
		this.vertexFixedSize = style.vertexFixedSize;
		this.vertexHighlightStroke = style.vertexHighlightStroke;
		this.vertexStroke = style.vertexStroke;
		notifyListeners();
	}

	private void notifyListeners()
	{
		updateListeners.listCopy().forEach( UpdateListener::dataGraphStyleChanged );
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	/**
	 * Returns a new style instance, copied from this style.
	 *
	 * @param name
	 *            the name for the copied style.
	 * @return a new style instance.
	 */
	@Override
	public DataDisplayStyle copy( final String name )
	{
		final DataDisplayStyle newStyle = new DataDisplayStyle();
		newStyle.set( this );
		if ( name != null )
			newStyle.name( name );
		return newStyle;
	}

	@Override
	public DataDisplayStyle copy()
	{
		return copy( null );
	}

	/**
	 * Returns the default TrackScheme style instance. Editing this instance
	 * will affect all view using this style.
	 *
	 * @return the single common instance for the default style.
	 */
	public static DataDisplayStyle defaultStyle()
	{
		return df;
	}

	private static final DataDisplayStyle df;
	static
	{
		final Color fill = new Color( 128, 255, 128 );
		df = new DataDisplayStyle().name( "default" ).
				autoVertexSize( true ).
				axisColor( new Color( 89, 89, 89 ) ).
				axisTickFont( new Font( "SansSerif", Font.PLAIN, 9 ) ).
				axisLabelFont( new Font( "SansSerif", Font.PLAIN, 10 ) ).
				axisStroke( new BasicStroke() ).
				backgroundColor( new Color( 204, 204, 204 ) ).
				drawVertexName( true ).
				edgeColor( Color.BLACK ).
				edgeHighlightStroke( new BasicStroke( 2f ) ).
				edgeStroke( new BasicStroke() ).
				focusStroke( DEFAULT_FOCUS_STROKE ).
				font( new Font( "SansSerif", Font.PLAIN, 9 ) ).
				selectedVertexFillColor( fill ).
				selectedSimplifiedVertexFillColor( new Color( 0, 128, 0 ) ).
				selectedVertexDrawColor( Color.BLACK ).
				selectedEdgeColor( fill.darker() ).
				simplifiedVertexFillColor( Color.BLACK ).
				vertexDrawColor( Color.BLACK ).
				vertexDrawShape( VertexDrawShape.CIRCLE ).
				vertexFillColor( Color.WHITE ).
				vertexFixedSize( 10. ).
				vertexHighlightStroke( new BasicStroke( 3f ) ).
				vertexStroke( new BasicStroke() );
	}

	public static Collection< DataDisplayStyle > defaults;
	static
	{
		defaults = new ArrayList<>( 1 );
		defaults.add( df );
	}
}
