package org.mastodon.revised.trackscheme.display;

import org.mastodon.util.Listeners;

public class OffsetHeaders
{
	public interface OffsetHeadersListener
	{
		void updateHeadersVisibility( boolean isVisibleX, int width, boolean isVisibleY, int height );
	}

	private final Listeners.List< OffsetHeadersListener > listeners;

	private boolean isVisibleX;

	private int width;

	private boolean isVisibleY;

	private int height;

	public OffsetHeaders()
	{
		listeners = new Listeners.SynchronizedList<>();
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

	public Listeners< OffsetHeadersListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		listeners.list.forEach( l -> l.updateHeadersVisibility( isVisibleX, width, isVisibleY, height ) );
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
