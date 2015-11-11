package net.trackmate.revised.trackscheme.display.laf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public class TrackSchemeStyle
{
	// TODO: move AvailableStyles to UI or wherever it is needed
	public static enum AvailableStyles
	{
		DEFAULT( "Default" ),
		MODERN( "Modern" ),
		HOWMUCH( "Pastel" );

		private final String name;

		AvailableStyles( final String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public TrackSchemeStyle getStyle()
		{
			switch ( name ) {
			case "Modern":
				return modernStyle();
			case "Pastel":
				return howMuchDoYouKnowStyle();
			case "Default":
			default:
				return defaultStyle();
			}
		}
	}

	public Color edgeColor;

	public Color vertexFillColor;

	public Color vertexDrawColor;

	public Color selectedVertexFillColor;

	public Color selectedEdgeColor;

	public Color selectedVertexDrawColor;

	public Color simplifiedVertexFillColor;

	public Color selectedSimplifiedVertexFillColor;

	public Color backgroundColor;

	public Color currentTimepointColor;

	public Color decorationColor;

	public Color vertexRangeColor;

	public Font font;

	public Stroke edgeStroke;

	public Stroke vertexStroke;

	public TrackSchemeStyle edgeColor( final Color c )
	{
		edgeColor = c;
		return this;
	}

	public TrackSchemeStyle vertexFillColor( final Color c )
	{
		vertexFillColor = c;
		return this;
	}

	public TrackSchemeStyle vertexDrawColor( final Color c )
	{
		vertexDrawColor = c;
		return this;
	}

	public TrackSchemeStyle selectedVertexFillColor( final Color c )
	{
		selectedVertexFillColor = c;
		return this;
	}

	public TrackSchemeStyle selectedEdgeColor( final Color c )
	{
		selectedEdgeColor = c;
		return this;
	}

	public TrackSchemeStyle selectedVertexDrawColor( final Color c )
	{
		selectedVertexDrawColor = c;
		return this;
	}

	public TrackSchemeStyle simplifiedVertexFillColor( final Color c )
	{
		simplifiedVertexFillColor = c;
		return this;
	}

	public TrackSchemeStyle selectedSimplifiedVertexFillColor( final Color c )
	{
		selectedSimplifiedVertexFillColor = c;
		return this;
	}

	public TrackSchemeStyle backgroundColor( final Color c )
	{
		backgroundColor = c;
		return this;
	}

	public TrackSchemeStyle currentTimepointColor( final Color c )
	{
		currentTimepointColor = c;
		return this;
	}

	public TrackSchemeStyle decorationColor( final Color c )
	{
		decorationColor = c;
		return this;
	}

	public TrackSchemeStyle vertexRangeColor( final Color c )
	{
		vertexRangeColor = c;
		return this;
	}

	public TrackSchemeStyle font( final Font f )
	{
		font = f;
		return this;
	}

	public TrackSchemeStyle edgeStroke( final Stroke s )
	{
		edgeStroke = s;
		return this;
	}

	public TrackSchemeStyle vertexStroke( final Stroke s )
	{
		vertexStroke = s;
		return this;
	}

	private TrackSchemeStyle()
	{}

	public static TrackSchemeStyle defaultStyle()
	{
		final Color fill = new Color( 128, 255, 128 );
		return new TrackSchemeStyle().
				backgroundColor( Color.LIGHT_GRAY ).
				currentTimepointColor( new Color( 217, 217, 217 ) ).
				vertexFillColor( Color.WHITE ).
				selectedVertexFillColor( fill ).
				simplifiedVertexFillColor( Color.BLACK ).
				selectedSimplifiedVertexFillColor( new Color( 0, 128, 0 ) ).
				vertexDrawColor( Color.BLACK ).
				selectedVertexDrawColor( Color.BLACK ).
				edgeColor( Color.BLACK ).
				selectedEdgeColor( fill.darker() ).
				decorationColor( Color.YELLOW.darker().darker() ).
				vertexRangeColor( new Color( 128, 128, 128 ) ).
				font( new Font( "SansSerif", Font.PLAIN, 9 ) ).
				edgeStroke( new BasicStroke() ).
				vertexStroke( new BasicStroke() );
	}

	public static TrackSchemeStyle modernStyle()
	{
		final Color bg = new Color( 163, 199, 197 );
		final Color fill = new Color( 64, 106, 102 );
		final Color selfill = new Color( 255, 128, 128 );
		return new TrackSchemeStyle().
				backgroundColor( bg ).
				currentTimepointColor( bg.brighter() ).
				vertexFillColor( fill ).
				selectedVertexFillColor( selfill ).
				simplifiedVertexFillColor( fill ).
				selectedSimplifiedVertexFillColor( selfill ).
				vertexDrawColor( Color.WHITE ).
				selectedVertexDrawColor( Color.BLACK ).
				edgeColor( Color.WHITE ).
				selectedEdgeColor( selfill.darker() ).
				decorationColor( bg.darker() ).
				vertexRangeColor( Color.WHITE ).
				font( new Font( "Calibri", Font.PLAIN, 12 ) ).
				edgeStroke( new BasicStroke() ).
				vertexStroke( new BasicStroke() );
	}

	public static TrackSchemeStyle howMuchDoYouKnowStyle()
	{
		final Color bg = new Color( 163, 199, 197 );
		final Color fill = new Color( 53, 107, 154 );
		return new TrackSchemeStyle().
				backgroundColor( bg ).
				currentTimepointColor( bg.brighter() ).
				vertexFillColor( fill ).
				selectedVertexFillColor( fill ).
				simplifiedVertexFillColor( Color.DARK_GRAY ).
				selectedSimplifiedVertexFillColor( fill ).
				vertexDrawColor( Color.DARK_GRAY ).
				selectedVertexDrawColor( Color.WHITE ).
				edgeColor( Color.DARK_GRAY ).
				selectedEdgeColor( Color.WHITE ).
				decorationColor( bg.darker() ).
				vertexRangeColor( Color.DARK_GRAY ).
				font( new Font( "Calibri", Font.PLAIN, 12 ) ).
				edgeStroke( new BasicStroke() ).
				vertexStroke( new BasicStroke() );
	}
}
