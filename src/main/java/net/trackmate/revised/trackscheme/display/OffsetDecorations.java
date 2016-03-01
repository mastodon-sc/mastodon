package net.trackmate.revised.trackscheme.display;

import java.util.ArrayList;

public class OffsetDecorations
{
	public interface OffsetDecorationsListener
	{
		public void updateDecorationsVisibility( boolean isVisibleX, int width, boolean isVisibleY, int height );
	}

	private final ArrayList< OffsetDecorationsListener > listeners = new ArrayList< OffsetDecorationsListener >();

	private boolean isVisibleX;

	private int width;

	private boolean isVisibleY;

	private int height;

	public OffsetDecorations()
	{
		isVisibleX = false;
		isVisibleY = false;
		width = 50;
		height = 50;
	}

	public void setDecorationsVisibleX( final boolean isVisibleX, final int width )
	{
		this.isVisibleX = isVisibleX;
		this.width = width;
		notifyListeners();
	}

	public void setDecorationsVisibleY( final boolean isVisibleY, final int height )
	{
		this.isVisibleY = isVisibleY;
		this.height = height;
		notifyListeners();
	}

	/**
	 * Registers the specified listener.
	 *
	 * @param l
	 *            the {@link OffsetDecorationsListener} to register.
	 * @return {@code true} if the specified listener was added to the
	 *         listeners of this handler. {@code false} if the specified
	 *         listener was already registered.
	 */
	public boolean addOffsetDecorationsListener( final OffsetDecorationsListener l )
	{
		if ( !listeners.contains( l ) )
		{
			listeners.add( l );
			l.updateDecorationsVisibility( isVisibleX, width, isVisibleY, height );
			return true;
		}
		return false;
	}

	public boolean removeOffsetDecorationsListener( final OffsetDecorationsListener l )
	{
		return listeners.remove( l );
	}

	private void notifyListeners()
	{
		for ( final OffsetDecorationsListener l : listeners )
			l.updateDecorationsVisibility( isVisibleX, width, isVisibleY, height );
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
