package org.mastodon.app.logging;

import org.scijava.log.LogLevel;
import org.scijava.log.LogSource;

/**
 * Mastodon logging interface.
 * <p>
 * Implementing classes is used to report to the user information about what
 * processes are running and how.
 * 
 * @author Jean-Yves Tinevez
 */
public interface MastodonLogger
{

	/**
	 * Returns the log source for the Mastodon-app running.
	 * <p>
	 * All log sources should derive from this root, using <i>e.g.</i>:
	 * 
	 * <pre>
	 * LogSource myProcessSource = logger.getLogSourceRoot().subSource( "My process" );
	 * </pre>
	 * 
	 * @return the log source root.
	 */
	public LogSource getLogSourceRoot();

	/**
	 * Displays an information message.
	 * 
	 * @param message
	 *            the message to display.
	 * @param source
	 *            the source of the message.
	 */
	public void info( String message, LogSource source );

	/**
	 * Displays an information message. It will appear as coming from an unknown
	 * source.
	 * 
	 * @param message
	 *            the message to display.
	 */
	public void info( String message );

	/**
	 * Displays an error message.
	 * 
	 * @param message
	 *            the message to display.
	 * @param source
	 *            the source of the message.
	 */
	public void error( String message, LogSource source );

	/**
	 * Displays an error message. It will appear as coming from an unknown
	 * source.
	 * 
	 * @param message
	 *            the message to display.
	 */
	public void error( String message );

	/**
	 * General method to display a message.
	 * 
	 * @param message
	 *            the message to display.
	 * @param level
	 *            the type of message, encoded as <code>int</code>. Use
	 *            {@link LogLevel}.
	 * @param source
	 *            the source that emits the message.
	 */
	public void log( String message, int level, LogSource source );

	/**
	 * Sets the status message to display for the process with the specified log
	 * source.
	 * 
	 * @param status
	 *            the status to set.
	 * @param source
	 *            the source of the process.
	 */
	public void setStatus( String status, LogSource source );

	/**
	 * Sets the status message to display for an unknown process.
	 * 
	 * @param status
	 *            the status to set.
	 */
	public void setStatus( String status );

	/**
	 * Sets the progress to display for the process with the specified log
	 * source.
	 * 
	 * @param progress
	 *            a <code>double</code> value between 0 and 1. If
	 *            <code>progress</code> is equal to or larger than 1, then the
	 *            process is considered finished.
	 * @param source
	 *            the source of the process.
	 */
	public void setProgress( double progress, LogSource source );

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