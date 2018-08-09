package org.mastodon.revised.bvv;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;

public class BvvHighlightHandler< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > > implements MouseMotionListener, MouseListener, TransformListener< AffineTransform3D >
{
	private final BvvGraph< V, E > graph;

	private final BvvScene< V, E > scene;

	private final HighlightModel< V, E > highlight;

	private boolean mouseInside;

	private int x, y;

	public BvvHighlightHandler(
			final BvvGraph< V, E > graph,
			final BvvScene< V, E > scene,
			final HighlightModel< V, E > highlight )
	{
		this.graph = graph;
		this.scene = scene;
		this.highlight = highlight;
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
	public void transformChanged( final AffineTransform3D t )
	{
		if ( mouseInside )
			highlight();
	}

	private void highlight()
	{
		final V vertex = graph.vertexRef();
		final E edge = graph.edgeRef();
		graph.getLock().readLock().lock();
		try
		{
			// See if we can find an edge.
			if ( scene.getEdgeAt( x, y, BdvSelectionBehaviours.EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
				highlight.highlightEdge( edge );
			// See if we can find a vertex.
			else if ( scene.getVertexAt( x, y, BdvSelectionBehaviours.POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
				highlight.highlightVertex( vertex );
			else
				highlight.clearHighlight();
		}
		finally
		{
			graph.getLock().readLock().unlock();
			graph.releaseRef( vertex );
			graph.releaseRef( edge );
		}
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{
		mouseInside = true;
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
		highlight.clearHighlight();
		mouseInside = false;
	}
}
