package org.mastodon.revised.mamut;

import org.scijava.Priority;
import org.scijava.log.LogSource;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 * A low priority {@link MastodonLogger} that appends messages to
 * {@link System#out} and {@link System#err}. Progress messages are discarded.
 * 
 * @author Jean-Yves Tinevez
 */
@Plugin( type = MastodonLogger.class, priority = Priority.LOW )
public class SysOutMastodonLogger extends AbstractService implements MastodonLogger
{

	private final LogSource root = LogSource.newRoot();

	private final LogSource unknownSource = root.subSource( "Unkown source" );

	@Override
	public LogSource getLogSourceRoot()
	{
		return root;
	}

	@Override
	public void info( final String message, final LogSource source )
	{
		System.out.println( '[' + source.name() + "] " + message );
	}

	@Override
	public void info( final String message )
	{
		info( message, unknownSource );
	}

	@Override
	public void error( final String message, final LogSource source )
	{
		System.err.println( '[' + source.name() + "] " + message );
	}

	@Override
	public void error( final String message )
	{
		error( message, unknownSource );
	}

	@Override
	public void setStatus( final String status, final LogSource source )
	{
		info( status, source );
	}

	@Override
	public void setStatus( final String status )
	{
		setStatus( status, unknownSource );
	}

	@Override
	public void setProgress( final double progress, final LogSource source )
	{}

	@Override
	public void setProgress( final double progress )
	{}
}
