package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefSet;

/**
 * A {@link SelectionHandler} that implements the previous TrackMate TrackScheme
 * selection behavior:
 * <ul>
 * <li> <code>Single-click</code> on a vertex or an edge to replace the selection
 * by this vertex or edge.
 * <li> <code>Single-click</code> elsewhere to clear the selection.
 * <li> <code>Click and drag</code> to draw a box around selection.
 * <li> <code>Shift - Single-click</code> to toggle selection with the target
 * vertex or edge.
 * <li> <code>Shift - Click and drag</code> to add a box content to the current
 * selection.
 * </ul>
 * <p>
 *
 * @author Jean-Yves Tinevez
 */
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

	private SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel;

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
			final boolean clear = !e.isShiftDown();
			selectAt( transform, e.getX(), e.getY(), clear );
			selectionListener.refresh();
		}
	}

	@Override
	public void mouseDragged( MouseEvent e )
	{
		if ( e.getButton() == MouseEvent.BUTTON1 && !e.isAltDown() )
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
			final boolean clear = !e.isShiftDown();
			selectWithin( transform, oX, oY, eX, eY, clear );
			selectionListener.refresh();
		}
	}

	private void selectAt( final ScreenTransform transform, final int x, final int y, boolean clear )
	{
		if ( clear )
		{
			clearSelection();
		}

		final double lx = transform.screenToLayoutX( x );
		final double ly = transform.screenToLayoutY( y );

		final TrackSchemeVertex closestVertex = order.getClosestVertex( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.vertexRef() );

		if ( null != closestVertex )
		{
			/*
			 * We found a vertex under the mouse click. It becomes the
			 * selection.
			 */

			select( closestVertex, !clear );

		}
		else
		{
			/*
			 * We did not found a vertex under our mouse click. We then look for
			 * an edge.
			 */

			final TrackSchemeEdge closestEdge = order.getClosestEdge( lx, ly, SELECT_DISTANCE_TOLERANCE, graph.edgeRef() );

			if ( null != closestEdge )
			{
				/*
				 * We found an edge. It becomes the selection.
				 */

				select( closestEdge, !clear );
			}
		}
	}

	private void select( TrackSchemeVertex vertex, boolean toggle )
	{
		boolean selected;
		if ( toggle )
		{
			selected = !vertex.isSelected();
			selectionModel.toggle( vertex );
		}
		else
		{
			selected = true;
			selectionModel.add( vertex );
		}

		vertex.setSelected( selected );
		final ScreenVertex screenVertex = order.getScreenVertexFor( vertex );
		if ( null != screenVertex )
		{
			screenVertex.setSelected( selected );
		}
	}

	private void select( TrackSchemeEdge edge, boolean toggle )
	{
		boolean selected;
		if ( toggle )
		{
			selected = !edge.isSelected();
			selectionModel.toggle( edge );
		}
		else
		{
			selected = true;
			selectionModel.add( edge );
		}

		edge.setSelected( selected );
		final ScreenEdge screenEdge = order.getScreenEdgeFor( edge );
		if ( null != screenEdge )
		{
			screenEdge.setSelected( selected );
		}
	}

	private void clearSelection()
	{
		/*
		 * Rather than iterating over the whole model, it's best to use the
		 * selection model to deselect what is selected.
		 */
		for ( final TrackSchemeVertex vertex : selectionModel.getSelectedVertices() )
		{
			vertex.setSelected( false );
			final ScreenVertex screenVertex = order.getScreenVertexFor( vertex );
			if ( null != screenVertex )
			{
				screenVertex.setSelected( false );
			}
		}
		for ( final TrackSchemeEdge edge : selectionModel.getSelectedEdges() )
		{
			edge.setSelected( false );
			final ScreenEdge screenEdge = order.getScreenEdgeFor( edge );
			if ( null != screenEdge )
			{
				screenEdge.setSelected( false );
			}
		}
		selectionModel.clearSelection();
	}

	private void selectWithin( ScreenTransform transform, int x1, int y1, int x2, int y2, boolean clear )
	{
		if ( clear )
		{
			clearSelection();
		}

		final double lx1 = transform.screenToLayoutX( x1 );
		final double ly1 = transform.screenToLayoutY( y1 );
		final double lx2 = transform.screenToLayoutX( x2 );
		final double ly2 = transform.screenToLayoutY( y2 );

		final RefSet< TrackSchemeVertex > vs = order.getVerticesWithin( lx1, ly1, lx2, ly2 );

		TrackSchemeVertex t = graph.vertexRef();
		for ( final TrackSchemeVertex v : vs )
		{
			select( v, false );

			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				t = e.getTarget( t );
				if ( vs.contains( t ) )
				{
					select( e, false );
				}
			}
		}

		graph.releaseRef( t );
	}

	@Override
	public void setSelectionModel( SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel )
	{
		this.selectionModel = selectionModel;
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
