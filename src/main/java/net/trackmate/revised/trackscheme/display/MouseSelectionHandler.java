package net.trackmate.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.RealPoint;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.util.Util;
import net.trackmate.revised.trackscheme.LineageTreeLayout;
import net.trackmate.revised.trackscheme.ScreenTransform;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class MouseSelectionHandler implements MouseListener, MouseMotionListener, OverlayRenderer
{
	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	private static final int MOUSE_MASK = InputEvent.BUTTON1_DOWN_MASK;

	private static final int MOUSE_MASK_ADDTOSELECTION = InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

	private static final int MOUSE_MASK_CLICK = InputEvent.BUTTON1_MASK;

	private static final int MOUSE_MASK_CLICK_ADDTOSELECTION = InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK;

	private final OverlayRenderer selectionBoxOverlay = new SelectionBoxOverlay();

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeSelection selection;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private boolean dragStarted = false;

	private final InteractiveDisplayCanvasComponent< ScreenTransform > display;

	private final LineageTreeLayout layout;

	private final TrackSchemeGraph< ?, ? > graph;

	public MouseSelectionHandler( final AbstractTrackSchemeOverlay graphOverlay, final TrackSchemeSelection selection, final InteractiveDisplayCanvasComponent< ScreenTransform > display, final LineageTreeLayout layout, final TrackSchemeGraph< ?, ? > graph )
	{
		this.graphOverlay = graphOverlay;
		this.selection = selection;
		this.display = display;
		this.layout = layout;
		this.graph = graph;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final ScreenTransform transform = display.getTransformEventHandler().getTransform();

		final double[] mousePos = new double[] { e.getX(), e.getY(), 0 };
		final double[] layoutPos = new double[ 3 ];
		transform.applyInverse( layoutPos, mousePos );
		
		final double ratioXtoY = transform.getScaleX() / transform.getScaleY();
		
		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeVertex vertex = layout.getClosestActiveVertex( RealPoint.wrap( layoutPos ), ratioXtoY, ref );
		System.out.println( "At layout position " + Util.printCoordinates( layoutPos ) + ", closest vertex is " + vertex.getLabel() );// DEBUG
	}

	private void select( final MouseEvent e )
	{
		final boolean clear = !( e.getModifiers() == MOUSE_MASK_CLICK_ADDTOSELECTION );

		final int vertexId = graphOverlay.getVertexIdAt( e.getX(), e.getY() );
		if ( vertexId < 0 )
		{
			// See if we can select an edge.
			final int edgeId = graphOverlay.getEdgeIdAt( e.getX(), e.getY(), SELECT_DISTANCE_TOLERANCE );
			if ( edgeId < 0 )
			{
				if ( clear )
					selection.clearSelection();
				return;
			}
			if ( clear )
			{
				selection.clearSelection();
				selection.setEdgeSelected( edgeId, true );
			}
			else
			{
				selection.toggleEdge( edgeId );
			}
		}
		else
		{
			if ( clear )
			{
				selection.clearSelection();
				selection.setVertexSelected( vertexId, true );
			}
			else
			{
				selection.toggleVertex( vertexId );
			}
		}
	}


	@Override
	public void mouseDragged( final MouseEvent e )
	{
		if ( e.getModifiersEx() == MOUSE_MASK || e.getModifiersEx() == MOUSE_MASK_ADDTOSELECTION )
		{
			eX = e.getX();
			eY = e.getY();
			if ( dragStarted == false )
			{
				dragStarted = true;
				oX = e.getX();
				oY = e.getY();
			}
			display.repaint();
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		if ( dragStarted )
		{
			dragStarted = false;

			display.repaint();
			final boolean clear = !( ( e.getModifiersEx() & MOUSE_MASK_ADDTOSELECTION ) != 0 );
		}
	}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{}

	@Override
	public void drawOverlays( final Graphics g )
	{
		selectionBoxOverlay.drawOverlays( g );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	private class SelectionBoxOverlay implements OverlayRenderer
	{

		@Override
		public void drawOverlays( final Graphics g )
		{
			System.out.println( "Draw drag = " + dragStarted );// DEBUG
			if ( !dragStarted )
				return;
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}

		@Override
		public void setCanvasSize( final int width, final int height )
		{}

	}
}
