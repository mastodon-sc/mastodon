package org.mastodon.app.logging;

import org.scijava.log.LogSource;

/**
 * A {@link MastodonLogger} that discards all messages.
 * 
 * @author Jean-Yves Tinevez
 */
public class VoidMastodonLogger implements MastodonLogger
{

	private final LogSource root = LogSource.newRoot();

	@Override
	public LogSource getLogSourceRoot()
	{
		return root;
	}

	@Override
	public void log( final String message, final int level, final LogSource source )
	{}

	@Override
	public void info( final String message, final LogSource source )
	{}

	@Override
	public void info( final String message )
	{}

	@Override
	public void error( final String message, final LogSource source )
	{}

	@Override
	public void error( final String message )
	{}

	@Override
	public void setStatus( final String status, final LogSource source )
	{}

	@Override
	public void setStatus( final String status )
	{}

	@Override
	public void setProgress( final double progress, final LogSource source )
	{}

	@Override
	public void setProgress( final double progress )
	{}
}