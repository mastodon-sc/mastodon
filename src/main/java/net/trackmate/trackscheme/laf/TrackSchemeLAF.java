package net.trackmate.trackscheme.laf;

import java.awt.Graphics2D;

import net.imglib2.ui.TransformListener;
import net.trackmate.trackscheme.ScreenEdge;
import net.trackmate.trackscheme.ScreenTransform;
import net.trackmate.trackscheme.ScreenVertex;
import net.trackmate.trackscheme.ScreenVertexRange;

public interface TrackSchemeLAF extends TransformListener< ScreenTransform >
{

	public void drawVertex( Graphics2D g2, ScreenVertex vertex );

	public void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	public void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );

	public void paintBackground( Graphics2D g2 );
}
