package net.trackmate.revised.trackscheme.display.laf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.util.ArrayList;

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

	public Color headerBackgroundColor;

	public Color headerDecorationColor;

	public Color headerCurrentTimepointColor;

	public Font font;

	public Font headerFont;

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
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle vertexFillColor( final Color c )
	{
		vertexFillColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle vertexDrawColor( final Color c )
	{
		vertexDrawColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle selectedVertexFillColor( final Color c )
	{
		selectedVertexFillColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle selectedEdgeColor( final Color c )
	{
		selectedEdgeColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle selectedVertexDrawColor( final Color c )
	{
		selectedVertexDrawColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle simplifiedVertexFillColor( final Color c )
	{
		simplifiedVertexFillColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle selectedSimplifiedVertexFillColor( final Color c )
	{
		selectedSimplifiedVertexFillColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle backgroundColor( final Color c )
	{
		backgroundColor = c;
		updateGhostColors();
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle currentTimepointColor( final Color c )
	{
		currentTimepointColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle decorationColor( final Color c )
	{
		decorationColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle vertexRangeColor( final Color c )
	{
		vertexRangeColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle headerBackgroundColor( final Color c )
	{
		headerBackgroundColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle headerDecorationColor( final Color c )
	{
		headerDecorationColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle headerCurrentTimepointColor( final Color c )
	{
		headerCurrentTimepointColor = c;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle font( final Font f )
	{
		font = f;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle headerFont( final Font f )
	{
		headerFont = f;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle edgeStroke( final Stroke s )
	{
		edgeStroke = s;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle edgeGhostStroke( final Stroke s )
	{
		edgeGhostStroke = s;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle vertexStroke( final Stroke s )
	{
		vertexStroke = s;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle vertexGhostStroke( final Stroke s )
	{
		vertexGhostStroke = s;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle highlightStroke( final Stroke s )
	{
		highlightStroke = s;
		notifyListeners();
		return this;
	}

	public TrackSchemeStyle focusStroke( final Stroke s )
	{
		focusStroke = s;
		notifyListeners();
		return this;
	}

	private TrackSchemeStyle()
	{
		updateListeners = new ArrayList<>();
	}

	public synchronized void set( final TrackSchemeStyle style )
	{
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
		this.vertexStroke = style.vertexStroke;
		this.vertexGhostStroke = style.vertexGhostStroke;
		this.highlightStroke = style.highlightStroke;
		this.focusStroke = style.focusStroke;
		notifyListeners();
	}

	public interface UpdateListener
	{
		public void trackSchemeStyleChanged();
	}

	private final ArrayList< UpdateListener > updateListeners;

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners )
			l.trackSchemeStyleChanged();
	}

	public synchronized boolean addUpdateListener( final UpdateListener l )
	{
		if ( !updateListeners.contains( l ) )
		{
			updateListeners.add( l );
			return true;
		}
		return false;
	}

	public synchronized boolean removeUpdateListener( final UpdateListener l )
	{
		return updateListeners.remove( l );
	}

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
				headerBackgroundColor( new Color( 217, 217, 217 ) ). // new Color( 238, 238, 238 ) ).
				headerDecorationColor( Color.DARK_GRAY ).
				headerCurrentTimepointColor( Color.WHITE ).
				font( new Font( "SansSerif", Font.PLAIN, 9 ) ).
				headerFont( new Font( "SansSerif", Font.PLAIN, 9 ) ).
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
				headerBackgroundColor( bg.brighter() ).
				headerDecorationColor( bg ).
				headerCurrentTimepointColor( bg.darker() ).
				font( new Font( "Calibri", Font.PLAIN, 12 ) ).
				headerFont( new Font( "Calibri", Font.PLAIN, 12 ) ).
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
				headerBackgroundColor( bg.brighter() ).
				headerDecorationColor( bg ).
				headerCurrentTimepointColor( bg.darker() ).
				font( new Font( "Calibri", Font.PLAIN, 12 ) ).
				headerFont( new Font( "Calibri", Font.PLAIN, 12 ) ).
				edgeStroke( new BasicStroke() ).
				edgeGhostStroke( DEFAULT_GHOST_STROKE ).
				vertexStroke( new BasicStroke() ).
				vertexGhostStroke( DEFAULT_GHOST_STROKE ).
				highlightStroke( new BasicStroke( 3f ) ).
				focusStroke( DEFAULT_FOCUS_STROKE );
	}
}
