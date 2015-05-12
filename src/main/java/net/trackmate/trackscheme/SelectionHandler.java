package net.trackmate.trackscheme;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.ui.OverlayRenderer;

public interface SelectionHandler extends MouseListener, MouseMotionListener
{
	public void setSelectionListener( SelectionListener selectionListener );

	public void setTransform( ScreenTransform transform );

	public OverlayRenderer getSelectionOverlay();

}
