package org.mastodon.revised.bdv.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;

public class OverlayGraphStyle
{


	private final ArrayList updateListeners;

	private String name;

	public Stroke vertexStroke;

	public Stroke vertexHighlightStroke;

	public Stroke focusStroke;

	public Stroke edgeStroke;

	public Stroke edgeHighlightStroke;

	public Color color1;

	public Color color2;

	private OverlayGraphStyle()
	{
		updateListeners = new ArrayList<>();
	}

	public static final OverlayGraphStyle df;
	static
	{
		df = new OverlayGraphStyle()
				.name( "default" )
				.vertexStroke( new BasicStroke() )
				.vertexHighlightStroke( new BasicStroke( 4f ) )
				.focusStroke( new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 ) )
				.edgeStroke( new BasicStroke() )
				.edgeHighlightStroke( new BasicStroke( 3f ) )
				.color1( Color.GREEN )
				.color2( Color.RED );
	}

	public OverlayGraphStyle name( final String name )
	{
		this.name = name;
		return this;
	}

	private OverlayGraphStyle color2( final Color c2 )
	{
		color2 = c2;
		return this;
	}

	public OverlayGraphStyle color1( final Color c1 )
	{
		color1 = c1;
		return this;
	}

	public OverlayGraphStyle edgeHighlightStroke( final Stroke stroke )
	{
		edgeHighlightStroke = stroke;
		return this;
	}

	private OverlayGraphStyle edgeStroke( final Stroke stroke )
	{
		edgeStroke = stroke;
		return this;
	}

	public OverlayGraphStyle focusStroke( final Stroke stroke )
	{
		focusStroke = stroke;
		return this;
	}

	public OverlayGraphStyle vertexHighlightStroke( final Stroke stroke )
	{
		vertexHighlightStroke = stroke;
		return this;
	}

	public OverlayGraphStyle vertexStroke( final Stroke stroke )
	{
		this.vertexStroke = stroke;
		return this;
	}
}
