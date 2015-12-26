package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;

import bdv.behaviour.DragBehaviour;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

/**
 * TODO: javadoc
 */
public class BoxSelectionBehaviour implements DragBehaviour, OverlayRenderer
{
	private final TrackSchemeSelection selection;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private boolean dragging = false;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final LineageTreeLayout layout;

	private final boolean addToSelection;

	public BoxSelectionBehaviour(
			final TrackSchemeSelection selection,
			final InteractiveDisplayCanvasComponent< ScreenTransform > display,
			final LineageTreeLayout layout,
			final boolean addToSelection )
	{
		this.selection = selection;
		this.display = display;
		this.layout = layout;
		this.addToSelection = addToSelection;
	}

	@Override
	public void init( final int x, final int y )
	{
		oX = x;
		oY = y;
		dragging = false;
	}

	@Override
	public void drag( final int x, final int y )
	{
		eX = x;
		eY = y;
		dragging = true;
		display.repaint();
	}

	@Override
	public void end( final int x, final int y )
	{
		if ( dragging )
		{
			dragging = false;
			display.repaint();
			selectWithin( oX, oY, eX, eY, addToSelection );
		}
	}

	/**
	 * Draws the selection box, if there is one.
	 */
	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( dragging )
		{
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	/*
	 * PRIVATE METHODS
	 */

	private void selectWithin( final int x1, final int y1, final int x2, final int y2, final boolean addToSelection )
	{
		final ScreenTransform transform = display.getTransformEventHandler().getTransform();

		if ( !addToSelection )
		{
			selection.clearSelection();
		}

		final double lx1 = transform.screenToLayoutX( x1 );
		final double ly1 = transform.screenToLayoutY( y1 );
		final double lx2 = transform.screenToLayoutX( x2 );
		final double ly2 = transform.screenToLayoutY( y2 );

		final RefSet< TrackSchemeVertex > vs = layout.getVerticesWithin( lx1, ly1, lx2, ly2 );
		final TrackSchemeVertex ref = vs.createRef();
		for ( final TrackSchemeVertex v : vs )
		{
			selection.setSelected( v, true );
			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				final TrackSchemeVertex t = e.getTarget( ref );
				if ( vs.contains( t ) )
				{
					selection.setSelected( e, true );
				}
			}
		}
		vs.releaseRef( ref );
	}
}
