package net.trackmate.revised.trackscheme.display.laf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public class TrackSchemeStyle
{
	private static final Stroke DEFAULT_FOCUS_STROKE = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );

	private static final Stroke DEFAULT_GHOST_STROKE = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 3.0f }, 0.0f );

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

	public Color ghostEdgeColor;

	public Color ghostVertexFillColor;

	public Color ghostVertexDrawColor;

	public Color ghostSelectedVertexFillColor;

	public Color ghostSelectedEdgeColor;

	public Color ghostSelectedVertexDrawColor;

	public Color ghostSimplifiedVertexFillColor;

	public Color ghostSelectedSimplifiedVertexFillColor;

	public Color backgroundColor;

	public Color currentTimepointColor;

	public Color decorationColor;

	public Color vertexRangeColor;

	public Font font;

	public Stroke edgeStroke;

	public Stroke edgeGhostStroke;

	public Stroke vertexStroke;

	public Stroke vertexGhostStroke;

	public Stroke highlightStroke;

	public Stroke focusStroke;

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

	public TrackSchemeStyle edgeColor( final Color c )
	{
		edgeColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle vertexFillColor( final Color c )
	{
		vertexFillColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle vertexDrawColor( final Color c )
	{
		vertexDrawColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle selectedVertexFillColor( final Color c )
	{
		selectedVertexFillColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle selectedEdgeColor( final Color c )
	{
		selectedEdgeColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle selectedVertexDrawColor( final Color c )
	{
		selectedVertexDrawColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle simplifiedVertexFillColor( final Color c )
	{
		simplifiedVertexFillColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle selectedSimplifiedVertexFillColor( final Color c )
	{
		selectedSimplifiedVertexFillColor = c;
		updateGhostColors();
		return this;
	}

	public TrackSchemeStyle backgroundColor( final Color c )
	{
		backgroundColor = c;
		updateGhostColors();
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

	public TrackSchemeStyle edgeGhostStroke( final Stroke s )
	{
		edgeGhostStroke = s;
		return this;
	}

	public TrackSchemeStyle vertexStroke( final Stroke s )
	{
		vertexStroke = s;
		return this;
	}

	public TrackSchemeStyle vertexGhostStroke( final Stroke s )
	{
		vertexGhostStroke = s;
		return this;
	}

	public TrackSchemeStyle highlightStroke( final Stroke s )
	{
		highlightStroke = s;
		return this;
	}

	public TrackSchemeStyle focusStroke( final Stroke s )
	{
		focusStroke = s;
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
				edgeGhostStroke( DEFAULT_GHOST_STROKE ).
				vertexStroke( new BasicStroke() ).
				vertexGhostStroke( DEFAULT_GHOST_STROKE ).
				highlightStroke( new BasicStroke( 3f ) ).
				focusStroke( DEFAULT_FOCUS_STROKE );
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
				edgeGhostStroke( DEFAULT_GHOST_STROKE ).
				vertexStroke( new BasicStroke() ).
				vertexGhostStroke( DEFAULT_GHOST_STROKE ).
				highlightStroke( new BasicStroke( 3f ) ).
				focusStroke( DEFAULT_FOCUS_STROKE );
	}

	public static TrackSchemeStyle howMuchDoYouKnowStyle()
	{
		final Color bg = new Color( 163, 199, 197 );
		final Color fill = new Color( 225, 216, 183 );
		final Color selfill = new Color( 53, 107, 154 );
		return new TrackSchemeStyle().
				backgroundColor( bg ).
				currentTimepointColor( bg.brighter() ).
				vertexFillColor( fill ).
				selectedVertexFillColor( selfill ).
				simplifiedVertexFillColor( Color.DARK_GRAY ).
				selectedSimplifiedVertexFillColor( selfill ).
				vertexDrawColor( Color.DARK_GRAY ).
				selectedVertexDrawColor( Color.WHITE ).
				edgeColor( Color.DARK_GRAY ).
				selectedEdgeColor( Color.WHITE ).
				decorationColor( bg.darker() ).
				vertexRangeColor( Color.DARK_GRAY ).
				font( new Font( "Calibri", Font.PLAIN, 12 ) ).
				edgeStroke( new BasicStroke() ).
				edgeGhostStroke( DEFAULT_GHOST_STROKE ).
				vertexStroke( new BasicStroke() ).
				vertexGhostStroke( DEFAULT_GHOST_STROKE ).
				highlightStroke( new BasicStroke( 3f ) ).
				focusStroke( DEFAULT_FOCUS_STROKE );
	}
}
