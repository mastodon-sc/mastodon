package org.mastodon.views;

import java.lang.ref.WeakReference;
import java.util.concurrent.RejectedExecutionException;

import bdv.viewer.RequestRepaint;

/**
 * Note: This class should be replaced with {@link bdv.viewer.render.PainterThread}
 * as soon as the PR https://github.com/bigdataviewer/bigdataviewer-core/pull/163
 * is merged, released and on the FIJI update site.
 * <p>
 * This is a workaround for a memory leak related to
 * {@link bdv.viewer.render.PainterThread}. This is actually a
 * copy of the class in BDV core with the significant difference
 * that it uses a {@link WeakReference} to the {@link Paintable}.
 * This has the following consequences:
 * <ul>
 *     <li>The thread never prevents the garbage collection of the paintable.</li>
 *     <li>
 *         The thread automatically shuts down if the paintable gets garbage collected.
 *         It's not necessary (but still possible) to explicitly terminate the thread
 *         by calling {@link Thread#interrupt() thread.interrupt()}.
 *     </li>
 * </ul>
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
