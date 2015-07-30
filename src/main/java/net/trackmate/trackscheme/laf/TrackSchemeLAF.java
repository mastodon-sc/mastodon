package net.trackmate.trackscheme.laf;

import java.awt.Graphics2D;

import net.trackmate.trackscheme.ScreenEdge;
import net.trackmate.trackscheme.ScreenVertex;
import net.trackmate.trackscheme.ScreenVertexRange;

public interface TrackSchemeLAF
{
	public void paintBackground( Graphics2D g2, double minX, double maxX, double minY, double maxY, int screenWidth, int screenHeight, double xScale, double yScale );

	public void beforeDrawVertex( Graphics2D g2 );

	public void drawVertex( Graphics2D g2, ScreenVertex vertex );

	public void beforeDrawVertexRange( Graphics2D g2 );

	public void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	public void beforeDrawEdge( Graphics2D g2 );

	public void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );

}
