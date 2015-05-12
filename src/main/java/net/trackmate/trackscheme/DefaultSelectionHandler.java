package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefSet;

public class DefaultSelectionHandler extends MouseAdapter implements SelectionHandler
{
	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	private final OverlayRenderer selectionBoxOverlay = new SelectionBoxOverlay();

	/**
	 * Whom to notify when selecting stuff.
	 */
	private SelectionListener selectionListener;

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private ScreenTransform transform;

	private boolean dragStarted = false;

	private final VertexOrder order;

	private final TrackSchemeGraph graph;

	public DefaultSelectionHandler( TrackSchemeGraph graph, VertexOrder order )
	{
		this.graph = graph;
		this.order = order;
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
		if ( e.getButton() == MouseEvent.BUTTON1 )
		{
			selectAt( transform, e.getX(), e.getY() );
			selectionListener.refresh();
		}
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
		if ( e.getButton() == MouseEvent.BUTTON1 )
		{
			eX = e.getX();
			eY = e.getY();
			if ( dragStarted == false )
			{
				dragStarted = true;
				oX = e.getX();
				oY = e.getY();
			}
			selectionListener.refresh();
		}
	}

	@Override
	public void mouseReleased( MouseEvent e )
	{
		if ( e.getButton() == MouseEvent.BUTTON1 && dragStarted )
		{
			dragStarted = false;
			selectWithin( transform, oX, oY, eX, eY );
			selectionListener.refresh();
		}
	}

	private void selectAt( final ScreenTransform transform, final int x, final int y )
	{
		final double lx = transform.screenToLayoutX( x );
		final double ly = transform.screenToLayoutY( y );

		final TrackSchemeVertex closestVertex = order.getClosestVertex( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.vertexRef() );

		if ( null == closestVertex )
		{
			final TrackSchemeEdge closestEdge = order.getClosestEdge( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.edgeRef() );

			if ( null != closestEdge )
			{
				final boolean selected = !closestEdge.isSelected();
				closestEdge.setSelected( selected );
				final ScreenEdge screenEdge = order.getScreenEdgeFor( closestEdge );
				if ( null != screenEdge )
				{
					screenEdge.setSelected( selected );
				}
			}
		}
		else
		{
			final boolean selected = !closestVertex.isSelected();
			closestVertex.setSelected( selected );
			final ScreenVertex screenVertex = order.getScreenVertexFor( closestVertex );
			if ( null != screenVertex )
			{
				screenVertex.setSelected( selected );
			}
		}
	}

	private void selectWithin( ScreenTransform transform, int x1, int y1, int x2, int y2 )
	{
		final double lx1 = transform.screenToLayoutX( x1 );
		final double ly1 = transform.screenToLayoutY( y1 );
		final double lx2 = transform.screenToLayoutX( x2 );
		final double ly2 = transform.screenToLayoutY( y2 );

		final RefSet< TrackSchemeVertex > vs = order.getVerticesWithin( lx1, ly1, lx2, ly2 );
		TrackSchemeVertex t = graph.vertexRef();
		final boolean selected = true;
		for ( final TrackSchemeVertex v : vs )
		{
			v.setSelected( selected );
			final ScreenVertex sv = order.getScreenVertexFor( v );
			if ( null != sv )
			{
				sv.setSelected( selected );
			}

			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				t = e.getTarget( t );
				if ( vs.contains( t ) )
				{
					e.setSelected( selected );
					final ScreenEdge se = order.getScreenEdgeFor( e );
					if ( null != se )
					{
						se.setSelected( selected );
					}
				}
			}
		}

		graph.releaseRef( t );
	}

	@Override
	public void setSelectionListener( final SelectionListener selectionListener )
	{
		this.selectionListener = selectionListener;
	}

	@Override
	public void setTransform( ScreenTransform transform )
	{
		this.transform = transform;
	}

	@Override
	public OverlayRenderer getSelectionOverlay()
	{
		return selectionBoxOverlay;
	}

	public class SelectionBoxOverlay implements OverlayRenderer
	{

		@Override
		public void drawOverlays( Graphics g )
		{
			if ( !dragStarted ) { return; }
			g.setColor( Color.RED );
			final int x = Math.min( oX, eX );
			final int y = Math.min( oY, eY );
			final int width = Math.abs( eX - oX );
			final int height = Math.abs( eY - oY );
			g.drawRect( x, y, width, height );
		}

		@Override
		public void setCanvasSize( int width, int height )
		{}

	}
}