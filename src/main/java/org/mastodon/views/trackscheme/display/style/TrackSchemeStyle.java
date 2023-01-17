/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme.display.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.scijava.listeners.Listeners;

import bdv.ui.settings.style.Style;

public class TrackSchemeStyle implements Style< TrackSchemeStyle >
{
	private static final Stroke DEFAULT_FOCUS_STROKE =
			new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );

	private static final Stroke DEFAULT_GHOST_STROKE =
			new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f }, 0.0f );

	private String name;

	private Color edgeColor;

	private Color vertexFillColor;

	private Color vertexDrawColor;

	private Color selectedVertexFillColor;

	private Color selectedEdgeColor;

	private Color selectedVertexDrawColor;

	private Color simplifiedVertexFillColor;

	private Color selectedSimplifiedVertexFillColor;

	private Color ghostEdgeColor;

	private Color ghostVertexFillColor;

	private Color ghostVertexDrawColor;

	private Color ghostSelectedVertexFillColor;

	private Color ghostSelectedEdgeColor;

	private Color ghostSelectedVertexDrawColor;

	private Color ghostSimplifiedVertexFillColor;

	private Color ghostSelectedSimplifiedVertexFillColor;

	private Color backgroundColor;

	private Color currentTimepointColor;

	private Color decorationColor;

	private Color vertexRangeColor;

	private Color headerBackgroundColor;

	private Color headerDecorationColor;

	private Color headerCurrentTimepointColor;

	private Font font;

	private Font headerFont;

	private Stroke edgeStroke;

	private Stroke edgeGhostStroke;

	private Stroke edgeHighlightStroke;

	private Stroke vertexStroke;

	private Stroke vertexGhostStroke;

	private Stroke vertexHighlightStroke;

	private Stroke focusStroke;

	private Stroke decorationStroke;

	private Stroke branchGraphEdgeStroke;

	private Stroke branchGraphEdgeHighlightStroke;

	private Stroke hierarchyEdgeStroke;

	private Stroke hierarchyEdgeHighlightStroke;

	private Stroke hierarchyVertexStroke;

	private Stroke hierarchyVertexHighlightStroke;

	private boolean highlightCurrentTimepoint;

	private boolean paintRows;

	private boolean paintColumns;

	private boolean paintHeaderShadow;

	private boolean hierarchyCurvedLines;

	public // TODO?
	static Color mixGhostColor( final Color color, final Color backgroundColor )
	{
		return ( color == null || backgroundColor == null )
				? null
				: new Color(
						( color.getRed() + backgroundColor.getRed() ) / 2,
						( color.getGreen() + backgroundColor.getGreen() ) / 2,
						( color.getBlue() + backgroundColor.getBlue() ) / 2,
						color.getAlpha() );
	}

	private void updateGhostColors()
	{
		ghostEdgeColor = mixGhostColor( edgeColor, backgroundColor );
		ghostVertexFillColor = mixGhostColor( vertexFillColor, backgroundColor );
		ghostVertexDrawColor = mixGhostColor( vertexDrawColor, backgroundColor );
		ghostSelectedVertexFillColor = mixGhostColor( selectedVertexFillColor, backgroundColor );
		ghostSelectedEdgeColor = mixGhostColor( selectedEdgeColor, backgroundColor );
		ghostSelectedVertexDrawColor = mixGhostColor( selectedVertexDrawColor, backgroundColor );
		ghostSimplifiedVertexFillColor = mixGhostColor( simplifiedVertexFillColor, backgroundColor );
		ghostSelectedSimplifiedVertexFillColor = mixGhostColor( selectedSimplifiedVertexFillColor, backgroundColor );
	}

	/*
	 * GETTERS for non public fields.
	 */

	@Override
	public String getName()
	{
		return name;
	}

	public Color getBackgroundColor()
	{
		return backgroundColor;
	}

	public Color getCurrentTimepointColor()
	{
		return currentTimepointColor;
	}

	public Color getDecorationColor()
	{
		return decorationColor;
	}

	public Stroke getDecorationStroke()
	{
		return decorationStroke;
	}

	public Color getEdgeColor()
	{
		return edgeColor;
	}

	public Stroke getEdgeGhostStroke()
	{
		return edgeGhostStroke;
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

	public Color getGhostEdgeColor()
	{
		return ghostEdgeColor;
	}

	public Color getGhostSelectedEdgeColor()
	{
		return ghostSelectedEdgeColor;
	}

	public Color getGhostSelectedSimplifiedVertexFillColor()
	{
		return ghostSelectedSimplifiedVertexFillColor;
	}

	public Color getGhostSelectedVertexDrawColor()
	{
		return ghostSelectedVertexDrawColor;
	}

	public Color getGhostSelectedVertexFillColor()
	{
		return ghostSelectedVertexFillColor;
	}

	public Color getGhostSimplifiedVertexFillColor()
	{
		return ghostSimplifiedVertexFillColor;
	}

	public Color getGhostVertexDrawColor()
	{
		return ghostVertexDrawColor;
	}

	public Color getGhostVertexFillColor()
	{
		return ghostVertexFillColor;
	}

	public Color getHeaderBackgroundColor()
	{
		return headerBackgroundColor;
	}

	public Color getHeaderCurrentTimepointColor()
	{
		return headerCurrentTimepointColor;
	}

	public Color getHeaderDecorationColor()
	{
		return headerDecorationColor;
	}

	public Font getHeaderFont()
	{
		return headerFont;
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

	public Color getVertexFillColor()
	{
		return vertexFillColor;
	}

	public Color getSimplifiedVertexFillColor()
	{
		return simplifiedVertexFillColor;
	}

	public Stroke getVertexGhostStroke()
	{
		return vertexGhostStroke;
	}

	public Stroke getVertexHighlightStroke()
	{
		return vertexHighlightStroke;
	}

	public Color getVertexRangeColor()
	{
		return vertexRangeColor;
	}

	public Stroke getVertexStroke()
	{
		return vertexStroke;
	}

	public Stroke getBranchGraphEdgeStroke()
	{
		return branchGraphEdgeStroke;
	}

	public Stroke getBranchGraphEdgeHighlightStroke()
	{
		return branchGraphEdgeHighlightStroke;
	}

	public Stroke getHierarchyEdgeStroke()
	{
		return hierarchyEdgeStroke;
	}

	public Stroke getHierarchyEdgeHighlightStroke()
	{
		return hierarchyEdgeHighlightStroke;
	}

	public Stroke getHierarchyVertexStroke()
	{
		return hierarchyVertexStroke;
	}

	public Stroke getHierarchyVertexHighlightStroke()
	{
		return hierarchyVertexHighlightStroke;
	}

	public boolean isHierarchyGraphCurvedLines()
	{
		return hierarchyCurvedLines;
	}

	public boolean isPaintColumns()
	{
		return paintColumns;
	}

	public boolean isHighlightCurrentTimepoint()
	{
		return highlightCurrentTimepoint;
	}

	public boolean isPaintHeaderShadow()
	{
		return paintHeaderShadow;
	}

	public boolean isPaintRows()
	{
		return paintRows;
	}

	/*
	 * SETTERS
	 */

	public TrackSchemeStyle name( final String name )
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

	public TrackSchemeStyle edgeColor( final Color color )
	{
		if ( !Objects.equals( this.edgeColor, color ) )
		{
			this.edgeColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.vertexFillColor, color ) )
		{
			this.vertexFillColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexDrawColor( final Color color )
	{
		if ( !Objects.equals( this.vertexDrawColor, color ) )
		{
			this.vertexDrawColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle selectedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.selectedVertexFillColor, color ) )
		{
			this.selectedVertexFillColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle selectedEdgeColor( final Color color )
	{
		if ( !Objects.equals( this.selectedEdgeColor, color ) )
		{
			this.selectedEdgeColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle selectedVertexDrawColor( final Color color )
	{
		if ( !Objects.equals( this.selectedVertexDrawColor, color ) )
		{
			this.selectedVertexDrawColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle simplifiedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.simplifiedVertexFillColor, color ) )
		{
			this.simplifiedVertexFillColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle selectedSimplifiedVertexFillColor( final Color color )
	{
		if ( !Objects.equals( this.selectedSimplifiedVertexFillColor, color ) )
		{
			this.selectedSimplifiedVertexFillColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle backgroundColor( final Color color )
	{
		if ( !Objects.equals( this.backgroundColor, color ) )
		{
			this.backgroundColor = color;
			updateGhostColors();
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle currentTimepointColor( final Color color )
	{
		if ( !Objects.equals( this.currentTimepointColor, color ) )
		{
			this.currentTimepointColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle decorationColor( final Color color )
	{
		if ( !Objects.equals( this.decorationColor, color ) )
		{
			this.decorationColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexRangeColor( final Color color )
	{
		if ( !Objects.equals( this.vertexRangeColor, color ) )
		{
			this.vertexRangeColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle headerBackgroundColor( final Color color )
	{
		if ( !Objects.equals( this.headerBackgroundColor, color ) )
		{
			this.headerBackgroundColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle headerDecorationColor( final Color color )
	{
		if ( !Objects.equals( this.headerDecorationColor, color ) )
		{
			this.headerDecorationColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle headerCurrentTimepointColor( final Color color )
	{
		if ( !Objects.equals( this.headerCurrentTimepointColor, color ) )
		{
			this.headerCurrentTimepointColor = color;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle font( final Font font )
	{
		if ( !Objects.equals( this.font, font ) )
		{
			this.font = font;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle headerFont( final Font font )
	{
		if ( !Objects.equals( this.headerFont, font ) )
		{
			this.headerFont = font;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle edgeStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.edgeStroke, stroke ) )
		{
			this.edgeStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle edgeGhostStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.edgeGhostStroke, stroke ) )
		{
			this.edgeGhostStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle edgeHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.edgeHighlightStroke, stroke ) )
		{
			this.edgeHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.vertexStroke, stroke ) )
		{
			this.vertexStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexGhostStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.vertexGhostStroke, stroke ) )
		{
			this.vertexGhostStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle vertexHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.vertexHighlightStroke, stroke ) )
		{
			this.vertexHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle focusStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.focusStroke, stroke ) )
		{
			this.focusStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle decorationStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.decorationStroke, stroke ) )
		{
			this.decorationStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle branchGraphEdgeStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.branchGraphEdgeStroke, stroke ) )
		{
			this.branchGraphEdgeStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle branchGraphEdgeHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.branchGraphEdgeHighlightStroke, stroke ) )
		{
			this.branchGraphEdgeHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle hierarchyEdgeStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.hierarchyEdgeStroke, stroke ) )
		{
			this.hierarchyEdgeStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle hierarchyEdgeHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.hierarchyEdgeHighlightStroke, stroke ) )
		{
			this.hierarchyEdgeHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle hierarchyVertexStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.hierarchyVertexStroke, stroke ) )
		{
			this.hierarchyVertexStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle hierarchyVertexHighlightStroke( final Stroke stroke )
	{
		if ( !Objects.equals( this.hierarchyVertexHighlightStroke, stroke ) )
		{
			this.hierarchyVertexHighlightStroke = stroke;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle hierarchyGraphCurvedLines( final boolean b )
	{
		if ( this.hierarchyCurvedLines != b )
		{
			this.hierarchyCurvedLines = b;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle highlightCurrentTimepoint( final boolean b )
	{
		if ( this.highlightCurrentTimepoint != b )
		{
			this.highlightCurrentTimepoint = b;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle paintRows( final boolean b )
	{
		if ( this.paintRows != b )
		{
			this.paintRows = b;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle paintColumns( final boolean b )
	{
		if ( this.paintColumns != b )
		{
			this.paintColumns = b;
			notifyListeners();
		}
		return this;
	}

	public TrackSchemeStyle paintHeaderShadow( final boolean b )
	{
		if ( this.paintHeaderShadow != b )
		{
			this.paintHeaderShadow = b;
			notifyListeners();
		}
		return this;
	}

	public interface UpdateListener
	{
		void trackSchemeStyleChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	private TrackSchemeStyle()
	{
		updateListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public String toString()
	{
		return name;
	}

	public synchronized void set( final TrackSchemeStyle style )
	{
		this.name = style.name;
		this.edgeColor = style.edgeColor;
		this.vertexFillColor = style.vertexFillColor;
		this.vertexDrawColor = style.vertexDrawColor;
		this.selectedVertexFillColor = style.selectedVertexFillColor;
		this.selectedEdgeColor = style.selectedEdgeColor;
		this.selectedVertexDrawColor = style.selectedVertexDrawColor;
		this.simplifiedVertexFillColor = style.simplifiedVertexFillColor;
		this.selectedSimplifiedVertexFillColor = style.selectedSimplifiedVertexFillColor;
		this.ghostEdgeColor = style.ghostEdgeColor;
		this.ghostVertexFillColor = style.ghostVertexFillColor;
		this.ghostVertexDrawColor = style.ghostVertexDrawColor;
		this.ghostSelectedVertexFillColor = style.ghostSelectedVertexFillColor;
		this.ghostSelectedEdgeColor = style.ghostSelectedEdgeColor;
		this.ghostSelectedVertexDrawColor = style.ghostSelectedVertexDrawColor;
		this.ghostSimplifiedVertexFillColor = style.ghostSimplifiedVertexFillColor;
		this.ghostSelectedSimplifiedVertexFillColor = style.ghostSelectedSimplifiedVertexFillColor;
		this.backgroundColor = style.backgroundColor;
		this.currentTimepointColor = style.currentTimepointColor;
		this.decorationColor = style.decorationColor;
		this.vertexRangeColor = style.vertexRangeColor;
		this.headerBackgroundColor = style.headerBackgroundColor;
		this.headerDecorationColor = style.headerDecorationColor;
		this.headerCurrentTimepointColor = style.headerCurrentTimepointColor;
		this.font = style.font;
		this.headerFont = style.headerFont;
		this.edgeStroke = style.edgeStroke;
		this.edgeGhostStroke = style.edgeGhostStroke;
		this.edgeHighlightStroke = style.edgeHighlightStroke;
		this.vertexStroke = style.vertexStroke;
		this.vertexGhostStroke = style.vertexGhostStroke;
		this.vertexHighlightStroke = style.vertexHighlightStroke;
		this.focusStroke = style.focusStroke;
		this.decorationStroke = style.decorationStroke;
		this.branchGraphEdgeStroke = style.branchGraphEdgeStroke;
		this.branchGraphEdgeHighlightStroke = style.branchGraphEdgeHighlightStroke;
		this.hierarchyEdgeStroke = style.hierarchyEdgeStroke;
		this.hierarchyEdgeHighlightStroke = style.hierarchyEdgeHighlightStroke;
		this.hierarchyVertexStroke = style.hierarchyVertexStroke;
		this.hierarchyVertexHighlightStroke = style.hierarchyVertexHighlightStroke;
		this.hierarchyCurvedLines = style.hierarchyCurvedLines;
		this.highlightCurrentTimepoint = style.highlightCurrentTimepoint;
		this.paintRows = style.paintRows;
		this.paintColumns = style.paintColumns;
		this.paintHeaderShadow = style.paintHeaderShadow;
		notifyListeners();
	}

	private void notifyListeners()
	{
		updateListeners.listCopy().forEach( UpdateListener::trackSchemeStyleChanged );
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
	public TrackSchemeStyle copy( final String name )
	{
		final TrackSchemeStyle newStyle = new TrackSchemeStyle();
		newStyle.set( this );
		if ( name != null )
			newStyle.name( name );
		return newStyle;
	}

	@Override
	public TrackSchemeStyle copy()
	{
		return copy( null );
	}

	private static TrackSchemeStyle basicStyle()
	{
		return new TrackSchemeStyle().edgeStroke( new BasicStroke() ).edgeGhostStroke( DEFAULT_GHOST_STROKE )
				.edgeHighlightStroke( new BasicStroke( 2f ) ).vertexStroke( new BasicStroke() )
				.vertexGhostStroke( DEFAULT_GHOST_STROKE ).vertexHighlightStroke( new BasicStroke( 3f ) )
				.focusStroke( DEFAULT_FOCUS_STROKE ).decorationStroke( new BasicStroke() )
				.branchGraphEdgeStroke( new BasicStroke( 1.5f ) )
				.branchGraphEdgeHighlightStroke( new BasicStroke( 3f ) ).hierarchyEdgeStroke( new BasicStroke( 3f ) )
				.hierarchyEdgeHighlightStroke( new BasicStroke( 5f ) ).hierarchyVertexStroke( new BasicStroke( 3f ) )
				.hierarchyVertexHighlightStroke( new BasicStroke( 5f ) ).highlightCurrentTimepoint( true )
				.paintRows( true ).paintColumns( true ).paintHeaderShadow( true );
	}

	/**
	 * Returns the default TrackScheme style instance. Editing this instance
	 * will affect all view using this style.
	 *
	 * @return the single common instance for the default style.
	 */
	public static TrackSchemeStyle defaultStyle()
	{
		return df;
	}

	private static final TrackSchemeStyle df;
	static
	{
		final Color fill = new Color( 128, 255, 128 );
		df = basicStyle().name( "default" ).backgroundColor( Color.LIGHT_GRAY )
				.currentTimepointColor( new Color( 217, 217, 217 ) ).vertexFillColor( Color.WHITE )
				.selectedVertexFillColor( fill ).simplifiedVertexFillColor( Color.BLACK )
				.selectedSimplifiedVertexFillColor( new Color( 0, 128, 0 ) ).vertexDrawColor( Color.BLACK )
				.selectedVertexDrawColor( Color.BLACK ).edgeColor( Color.BLACK ).selectedEdgeColor( fill.darker() )
				.decorationColor( Color.YELLOW.darker().darker() ).vertexRangeColor( new Color( 128, 128, 128 ) )
				.headerBackgroundColor( new Color( 217, 217, 217 ) ). // new Color( 238, 238, 238 ) ).
				headerDecorationColor( Color.DARK_GRAY ).headerCurrentTimepointColor( Color.WHITE )
				.font( new Font( "SansSerif", Font.PLAIN, 9 ) ).headerFont( new Font( "SansSerif", Font.PLAIN, 9 ) );
	}

	/**
	 * Returns the modern TrackScheme style instance. Editing this instance will
	 * affect all view using this style.
	 *
	 * @return the single common instance for the modern style.
	 */
	public static TrackSchemeStyle modernStyle()
	{
		return modern;
	}

	private static final TrackSchemeStyle modern;
	static
	{
		final Color bg = new Color( 163, 199, 197 );
		final Color fill = new Color( 64, 106, 102 );
		final Color selfill = new Color( 255, 128, 128 );
		final Color currenttp = new Color( 38, 175, 185 );
		modern = basicStyle().name( "modern" ).backgroundColor( bg ).currentTimepointColor( currenttp )
				.vertexFillColor( fill ).selectedVertexFillColor( selfill ).simplifiedVertexFillColor( fill )
				.selectedSimplifiedVertexFillColor( selfill ).vertexDrawColor( Color.WHITE )
				.selectedVertexDrawColor( Color.BLACK ).edgeColor( Color.WHITE ).selectedEdgeColor( selfill.darker() )
				.decorationColor( bg.darker() ).vertexRangeColor( Color.WHITE ).headerBackgroundColor( bg.brighter() )
				.headerDecorationColor( bg ).headerCurrentTimepointColor( bg.darker() )
				.font( new Font( "Calibri", Font.PLAIN, 12 ) ).headerFont( new Font( "Calibri", Font.PLAIN, 12 ) );
	}

	/**
	 * Returns the lorry TrackScheme style instance. Editing this instance will
	 * affect all view using this style.
	 *
	 * @return the single common instance for the lorry style.
	 */
	public static TrackSchemeStyle lorryStyle()
	{
		return hmdyk;
	}

	private static final TrackSchemeStyle hmdyk;
	static
	{
		final Color bg = new Color( 163, 199, 197 );
		final Color fill = new Color( 225, 216, 183 );
		final Color selfill = new Color( 53, 107, 154 );
		final Color seldraw = new Color( 230, 245, 255 );
		final Color seledge = new Color( 91, 137, 158 );
		hmdyk = basicStyle().name( "lorry" ).backgroundColor( bg ).currentTimepointColor( bg.brighter() )
				.vertexFillColor( fill ).selectedVertexFillColor( selfill ).simplifiedVertexFillColor( Color.DARK_GRAY )
				.selectedSimplifiedVertexFillColor( selfill ).vertexDrawColor( Color.DARK_GRAY )
				.selectedVertexDrawColor( seldraw ).edgeColor( Color.DARK_GRAY ).selectedEdgeColor( seledge )
				.decorationColor( bg.darker() ).vertexRangeColor( Color.DARK_GRAY )
				.headerBackgroundColor( bg.brighter() ).headerDecorationColor( bg )
				.headerCurrentTimepointColor( bg.darker() ).font( new Font( "Calibri", Font.PLAIN, 12 ) )
				.headerFont( new Font( "Calibri", Font.PLAIN, 12 ) );
	}

	public static Collection< TrackSchemeStyle > defaults;
	static
	{
		defaults = new ArrayList<>( 3 );
		defaults.add( df );
		defaults.add( hmdyk );
		defaults.add( modern );
	}

}
