package net.trackmate.trackscheme;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.ui.OverlayRenderer;

public interface SelectionHandler extends MouseListener, MouseMotionListener
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
	 * Updates this handler with the current screen transform.
	 *
	 * @param transform
	 *            the screen transform.
	 */
	public void setTransform( ScreenTransform transform );

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

}
