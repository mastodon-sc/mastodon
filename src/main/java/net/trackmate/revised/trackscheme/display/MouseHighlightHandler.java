package net.trackmate.revised.trackscheme.display;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class MouseHighlightHandler implements MouseMotionListener, TransformListener< ScreenTransform >
{
	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeHighlight highlight;

	private final TrackSchemeGraph< ?, ? > graph;

	private int x, y;

	public MouseHighlightHandler(
			final AbstractTrackSchemeOverlay graphOverlay,
			final TrackSchemeHighlight highlight,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.graphOverlay = graphOverlay;
		this.highlight = highlight;
		this.graph = graph;
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		x = e.getX();
		y = e.getY();
		highlight();
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		x = e.getX();
		y = e.getY();
		highlight();
	}

	@Override
	public void transformChanged( final ScreenTransform t )
	{
		highlight();
	}

	private void highlight()
	{
		final TrackSchemeVertex ref = graph.vertexRef();
		highlight.highlightVertex( graphOverlay.getVertexAt( x, y, ref ) );
		graph.releaseRef( ref );
	}
}
