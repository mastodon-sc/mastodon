package org.mastodon.views;

import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import bdv.viewer.RequestRepaint;

/**
 * Thread to repaint display.
 */
public class PainterThread extends Thread implements RequestRepaint
{
	public interface Paintable {

		void paint();
	}

	private final WeakReference< Paintable > paintable;

	private boolean pleaseRepaint;

	public PainterThread( final Paintable paintable )
	{
		this( null, "PainterThread", paintable );
	}

	public PainterThread( final ThreadGroup group, final PainterThread.Paintable paintable )
	{
		this( group, "PainterThread", paintable );
	}

	public PainterThread( final ThreadGroup group, final String name, final Paintable paintable )
	{
		super( group, name );
		this.paintable = new WeakReference<>( paintable );
		this.pleaseRepaint = false;
	}

	@Override
	public void run()
	{
		while ( !isInterrupted() )
		{
			final boolean b;
			synchronized ( this )
			{
				b = pleaseRepaint;
				pleaseRepaint = false;
			}
			if ( b )
				try
				{
					Paintable p = paintable.get();
					if( p == null )
						return;
					p.paint();
				}
				catch ( final RejectedExecutionException e )
				{
					// this happens when the rendering threadpool
					// is killed before the painter thread.
				}
			synchronized ( this )
			{
				try
				{
					if ( !pleaseRepaint )
						wait();
				}
				catch ( final InterruptedException e )
				{
					break;
				}
			}
		}
	}

	/**
	 * Request repaint. This will trigger a call to {@link Paintable#paint()}
	 * from the {@link PainterThread}.
	 */
	@Override
	public void requestRepaint()
	{
		synchronized ( this )
		{
			pleaseRepaint = true;
			notify();
		}
	}
}
