package org.mastodon.app.logging;

import org.scijava.log.LogLevel;
import org.scijava.log.LogSource;
import org.scijava.log.Logger;

/**
 * Mastodon logging interface.
 * <p>
 * Implementing classes is used to report to the user information about what
 * processes are running and how.
 *
 * @author Jean-Yves Tinevez
 */
public interface MastodonLogger extends Logger
{

	@Override
	default MastodonLogger subLogger( String name )
	{
		return this.subLogger( name, getLevel() );
	}

	@Override
	MastodonLogger subLogger( String name, int level );

	/**
	 * Sets the status message to display for an unknown process.
	 *
	 * @param status the status to set.
	 */
	public void setStatus( String status );

	/**
	 * Sets the progress to display for an unknown process.
	 * 
	 * @param progress
	 *            a <code>double</code> value between 0 and 1. If
	 *            <code>progress</code> is equal to or larger than 1, then the
	 *            process is considered finished.
	 */
	public void setProgress( double progress );

}
