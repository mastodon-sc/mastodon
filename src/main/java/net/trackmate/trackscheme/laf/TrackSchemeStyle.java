package net.trackmate.trackscheme.laf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public class TrackSchemeStyle
{
	public Color edgeColor;

	public Color vertexFillColor;

	public Color vertexDrawColor;

	public Color selectedVertexFillColor;

	public Color selectedEdgeColor;

	public Color selectedVertexDrawColor;

	public Color simplifiedVertexFillColor;

	public Color selectedSimplifiedVertexFillColor;

	public Color backgroundColor;

	public Color decorationColor;

	public Color vertexRangeColor;

	public Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	public Stroke edgeStroke;

	public Stroke vertexStroke;

	public static TrackSchemeStyle defaultStyle()
	{
		final TrackSchemeStyle style = new TrackSchemeStyle();

		style.edgeColor = Color.black;

		style.vertexFillColor = Color.white;

		style.vertexDrawColor = Color.black;

		style.selectedVertexFillColor = new Color( 128, 255, 128 );

		style.selectedEdgeColor = style.selectedVertexFillColor.darker();

		style.selectedVertexDrawColor = Color.black;

		style.simplifiedVertexFillColor = Color.black;

		style.selectedSimplifiedVertexFillColor = new Color( 0, 128, 0 );

		style.backgroundColor = Color.LIGHT_GRAY;

		style.decorationColor = Color.YELLOW.darker().darker();

		style.vertexRangeColor = new Color( 128, 128, 128 );

		style.font = new Font( "SansSerif", Font.PLAIN, 9 );

		style.edgeStroke = new BasicStroke();

		style.vertexStroke = style.edgeStroke;

		return style;
	}

}
