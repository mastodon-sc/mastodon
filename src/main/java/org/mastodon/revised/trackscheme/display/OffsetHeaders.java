package org.mastodon.revised.trackscheme.display;

import org.mastodon.util.Listeners;

public class OffsetHeaders
{
	public interface OffsetHeadersListener
	{
		void updateHeaderSize( int width, int height );
	}

	private final Listeners.List< OffsetHeadersListener > listeners;

	private int width;

	private int height;

	public OffsetHeaders()
	{
		listeners = new Listeners.SynchronizedList<>();
		width = 0;
		height = 0;
	}

	public void setHeaderSize( final int width, final int height )
	{
		this.width = width;
		this.height = height;
		notifyListeners();
	}

	public Listeners< OffsetHeadersListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		listeners.list.forEach( l -> l.updateHeaderSize( width, height ) );
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
