package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;

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
	
	private static final int MOUSE_MASK = InputEvent.BUTTON1_DOWN_MASK;

	private static final int MOUSE_MASK_ADDTOSELECTION = InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

	private static final int MOUSE_MASK_CLICK = InputEvent.BUTTON1_MASK;
	
	private static final int MOUSE_MASK_CLICK_ADDTOSELECTION = InputEvent.BUTTON1_MASK  | InputEvent.SHIFT_MASK;

	private final OverlayRenderer selectionBoxOverlay = new SelectionBoxOverlay();

	/**
	 * Whom to notify when selecting stuff.
	 */
	private final HashSet< SelectionListener > selectionListeners = new HashSet< SelectionListener >();

	/**
	 * Coordinates where mouse dragging started.
	 */
	private int oX, oY;

	/**
	 * Coordinates where mouse dragging currently is.
	 */
	private int eX, eY;

	private final ScreenTransform transform;

	private boolean dragStarted = false;

	private final VertexOrder order;

	private final TrackSchemeGraph graph;

	private SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel;

	public DefaultSelectionHandler( final TrackSchemeGraph graph, final VertexOrder order )
	{
		this.graph = graph;
		this.order = order;
		this.transform = new ScreenTransform();
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		if ( e.getModifiers() == MOUSE_MASK_CLICK || e.getModifiers() == MOUSE_MASK_CLICK_ADDTOSELECTION )
		{
			final boolean clear = !(e.getModifiers() == MOUSE_MASK_CLICK_ADDTOSELECTION);
			selectAt( transform, e.getX(), e.getY(), clear );
		}
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		if ( e.getModifiersEx() == MOUSE_MASK ||  e.getModifiersEx() == MOUSE_MASK_ADDTOSELECTION )
		{
			eX = e.getX();
			eY = e.getY();
			if ( dragStarted == false )
			{
				dragStarted = true;
				oX = e.getX();
				oY = e.getY();
			}
		}
	}

	@Override
	public void mouseReleased( final MouseEvent e )
	{
		if (dragStarted)
		{
			dragStarted = false;
			final boolean clear = !( ( e.getModifiersEx() & MOUSE_MASK_ADDTOSELECTION ) != 0);
			selectWithin(transform, oX, oY, eX, eY, clear);
		}
	}

	private void selectAt( final ScreenTransform transform, final int x, final int y, final boolean clear )
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

	@Override
	public void select( final TrackSchemeVertex vertex, final boolean toggle )
	{
		boolean selected;
		boolean changed;
		if ( toggle )
		{
			selected = !vertex.isSelected();
			selectionModel.toggle( vertex );
			changed = true;
		}
		else
		{
			selected = true;
			changed = selectionModel.add( vertex );
		}
		vertex.setSelected( selected );
		if ( changed )
			fireSelectionChanged();
	}
	
	@Override
	public void select( final TrackSchemeEdge edge, final boolean toggle )
	{
		boolean selected;
		boolean changed;
		if ( toggle )
		{
			selected = !edge.isSelected();
			selectionModel.toggle( edge );
			changed = true;
		}
		else
		{
			selected = true;
			changed = selectionModel.add( edge );
		}
		edge.setSelected( selected );
		if ( changed )
			fireSelectionChanged();
	}

	@Override
	public void clearSelection()
	{
		/*
		 * Rather than iterating over the whole model, it's best to use the
		 * selection model to deselect what is selected.
		 */
		for ( final TrackSchemeVertex vertex : selectionModel.getSelectedVertices() )
			vertex.setSelected( false );
		for ( final TrackSchemeEdge edge : selectionModel.getSelectedEdges() )
			edge.setSelected( false );
		final boolean changed = selectionModel.clearSelection();
		if ( changed )
			fireSelectionChanged();
	}

	@Override
	public void selectWithin( final ScreenTransform transform, final int x1, final int y1, final int x2, final int y2, final boolean clear )
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
			selectionModel.add( v );
			v.setSelected( true );

			for ( final TrackSchemeEdge e : v.outgoingEdges() )
			{
				t = e.getTarget( t );
				if ( vs.contains( t ) )
				{
					selectionModel.add( e );
					e.setSelected( true );
				}
			}
		}

		graph.releaseRef( t );
		fireSelectionChanged();
	}

	@Override
	public SelectionModel< TrackSchemeVertex, TrackSchemeEdge > getSelectionModel()
	{
		return selectionModel;
	}

	@Override
	public void setSelectionModel( final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel )
	{
		this.selectionModel = selectionModel;
	}

	@Override
	public boolean addSelectionListener( final SelectionListener selectionListener )
	{
		return selectionListeners.add( selectionListener );
	}

	@Override
	public OverlayRenderer getSelectionOverlay()
	{
		return selectionBoxOverlay;
	}

	private void fireSelectionChanged()
	{
		for ( final SelectionListener selectionListener : selectionListeners )
		{
			selectionListener.selectionChanged();
		}
	}

	public class SelectionBoxOverlay implements OverlayRenderer
	{

		@Override
		public void drawOverlays( final Graphics g )
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
		public void setCanvasSize( final int width, final int height )
		{}

	}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		this.transform.set( transform );
	}
}
