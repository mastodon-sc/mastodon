package net.trackmate.trackscheme;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;

public interface SelectionHandler extends MouseListener, MouseMotionListener, TransformListener< ScreenTransform >
{
	/**
	 * Sets the {@link SelectionListener} that will be notified when this
	 * handler modifies the selection.
	 *
	 * @param selectionListener
	 *            the {@link SelectionListener} to notify.
	 */
	public void setSelectionListener( SelectionListener selectionListener );

	/**
	 * Returns the overlay instance that will paint selection handler widgets on
	 * the canvas.
	 *
	 * @return the overlay instance.
	 */
	public OverlayRenderer getSelectionOverlay();

	/**
	 * Sets the selection model that this handler will manage.
	 * <p>
	 * When calling this method, the current TrackScheme model state and the
	 * selection model are expected to be in sync.
	 *
	 * @param selectionModel
	 *            the selection model to manage in his handler.
	 */
	public void setSelectionModel( SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel );

	public SelectionModel< TrackSchemeVertex, TrackSchemeEdge > getSelectionModel();

	public void selectWithin( ScreenTransform transform, int x1, int y1, int x2, int y2, boolean clear );

	public void clearSelection();

	public void select( TrackSchemeEdge edge, boolean toggle );

	public void select( TrackSchemeVertex vertex, boolean toggle );

}
