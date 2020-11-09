package org.mastodon.views.dbvv;

import bdv.viewer.TransformListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HighlightModel;
import org.mastodon.views.bdv.overlay.BdvSelectionBehaviours;

public class DBvvHighlightHandler implements MouseMotionListener, MouseListener, TransformListener< AffineTransform3D >
{
	private final ModelGraph graph;

	private final DBvvRenderer scene;

	private final HighlightModel< Spot, Link > highlight;

	private boolean mouseInside;

	private int x, y;

	public DBvvHighlightHandler(
			final ModelGraph graph,
			final DBvvRenderer scene,
			final HighlightModel< Spot, Link > highlight )
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
		final Spot vertex = graph.vertexRef();
		final Link edge = graph.edgeRef();
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
