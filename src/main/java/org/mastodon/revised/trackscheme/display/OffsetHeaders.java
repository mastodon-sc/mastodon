package org.mastodon.revised.trackscheme.display;

import java.util.ArrayList;

public class OffsetHeaders
{
	public interface OffsetHeadersListener
	{
		public void updateHeadersVisibility( boolean isVisibleX, int width, boolean isVisibleY, int height );
	}

	private final ArrayList< OffsetHeadersListener > listeners = new ArrayList< OffsetHeadersListener >();

	private boolean isVisibleX;

	private int width;

	private boolean isVisibleY;

	private int height;

	public OffsetHeaders()
	{
		isVisibleX = false;
		isVisibleY = false;
		width = 50;
		height = 50;
	}

	public void setHeaderVisibleX( final boolean isVisibleX, final int width )
	{
		this.isVisibleX = isVisibleX;
		this.width = width;
		notifyListeners();
	}

	public void setHeaderVisibleY( final boolean isVisibleY, final int height )
	{
		this.isVisibleY = isVisibleY;
		this.height = height;
		notifyListeners();
	}

	/**
	 * Registers the specified listener.
	 *
	 * @param l
	 *            the {@link OffsetHeadersListener} to register.
	 * @return {@code true} if the specified listener was added to the
	 *         listeners of this handler. {@code false} if the specified
	 *         listener was already registered.
	 */
	public synchronized boolean addOffsetHeadersListener( final OffsetHeadersListener l )
	{
		if ( !listeners.contains( l ) )
		{
			listeners.add( l );
			l.updateHeadersVisibility( isVisibleX, width, isVisibleY, height );
			return true;
		}
		return false;
	}

	public synchronized boolean removeOffsetHeadersListener( final OffsetHeadersListener l )
	{
		return listeners.remove( l );
	}

	private void notifyListeners()
	{
		for ( final OffsetHeadersListener l : listeners )
			l.updateHeadersVisibility( isVisibleX, width, isVisibleY, height );
	}

	public boolean isVisibleX()
	{
		return isVisibleX;
	}

	public boolean isVisibleY()
	{
		return isVisibleY;
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}
}
