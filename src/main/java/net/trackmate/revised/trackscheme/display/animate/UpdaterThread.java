package net.trackmate.revised.trackscheme.display.animate;

import java.util.concurrent.RejectedExecutionException;

/**
 * Thread to update stuff.
 */
// TODO: unused. remove?
final public class UpdaterThread extends Thread
{
	public static interface Updatable
	{
		/**
		 * This is called by the updater thread to update the stuff.
		 */
		public void update();
	}

	private final Updatable updatable;

	private boolean pleaseUpdate;

	public UpdaterThread( final Updatable updatable )
	{
		this( null, "UpdaterThread " + updatable.toString(), updatable );
	}

	public UpdaterThread( final ThreadGroup group, final Updatable updatable )
	{
		this( group, "UpdaterThread " + updatable.toString(), updatable );
	}

	public UpdaterThread( final ThreadGroup group, final String name, final Updatable updatable )
	{
		super( group, name );
		this.updatable = updatable;
		this.pleaseUpdate = false;
	}

	@Override
	public void run()
	{
		while ( !isInterrupted() )
		{
			final boolean b;
			synchronized ( this )
			{
				b = pleaseUpdate;
				pleaseUpdate = false;
			}
			if ( b )
				try
				{
					updatable.update();
				}
				catch ( final RejectedExecutionException e )
				{
					// this happens when the rendering threadpool
					// is killed before the updater thread.
				}
			synchronized ( this )
			{
				try
				{
					if ( !pleaseUpdate )
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
	 * Request update. This will trigger a call to {@link Updatable#update()}
	 * from the {@link UpdaterThread}.
	 */
	public void requestUpdate()
	{
		synchronized ( this )
		{
			pleaseUpdate = true;
			notify();
		}
	}
}
