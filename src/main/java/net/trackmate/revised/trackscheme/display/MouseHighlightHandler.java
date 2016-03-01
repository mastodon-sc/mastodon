package net.trackmate.revised.trackscheme.display;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.ui.TransformListener;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.OffsetDecorations.OffsetDecorationsListener;

public class MouseHighlightHandler implements MouseMotionListener, MouseListener, TransformListener< ScreenTransform >,	OffsetDecorationsListener
{
	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeHighlight highlight;

	private final TrackSchemeGraph< ?, ? > graph;

	private int x, y;

	/**
	 * current decorations width.
	 */
	private int decorationsWidth;

	/**
	 * current decorations height.
	 */
	private int decorationsHeight;

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

	@Override
	public void updateDecorationsVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
	{
		decorationsWidth = isVisibleX ? width : 0;
		decorationsHeight = isVisibleY ? height : 0;
	}

	private void highlight()
	{
		if ( x < decorationsWidth || y < decorationsHeight )
			highlight.highlightVertex( null );
		else
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			highlight.highlightVertex( graphOverlay.getVertexAt( x, y, ref ) );
			graph.releaseRef( ref );
		}
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{
		highlight.highlightVertex( null );
	}
}
