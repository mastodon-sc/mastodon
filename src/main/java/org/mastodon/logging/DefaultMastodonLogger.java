package org.mastodon.logging;

import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogSource;

/**
 * A default {@link MastodonLogger} that echoes messages and progress to a
 * {@link JPanel}. Suitable to be used in a GUI.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class DefaultMastodonLogger implements MastodonLogger
{

	private final MastodonLogPanel mastodonLogPanel;

	private final LogSource root = LogSource.newRoot();

	private final LogSource unknownSource = root.subSource( "Unkown source" );

	public DefaultMastodonLogger( final Context context )
	{
		mastodonLogPanel = new MastodonLogPanel( context );
	}

	public MastodonLogPanel getMastodonLogPanel()
	{
		return mastodonLogPanel;
	}

	@Override
	public LogSource getLogSourceRoot()
	{
		return root;
	}

	@Override
	public void info( final String msg, final LogSource source )
	{
		log( msg, LogLevel.INFO, source );
	}

	@Override
	public void info( final String msg )
	{
		info( msg, unknownSource );
	}

	@Override
	public void error( final String msg, final LogSource source )
	{
		log( msg, LogLevel.ERROR, source );
	}

	@Override
	public void error( final String msg )
	{
		error( msg, unknownSource );
	}

	@Override
	public void log( final String msg, final int level, final LogSource source )
	{
		mastodonLogPanel.log( msg, level, source );
	}

	@Override
	public void setStatus( final String status, final LogSource source )
	{
		mastodonLogPanel.setStatus( status, source );
	}

	@Override
	public void setStatus( final String status )
	{
		mastodonLogPanel.setStatus( status, unknownSource );
	}

	@Override
	public void setProgress( final double progress, final LogSource source )
	{
		mastodonLogPanel.setProgress( progress, source );
	}

	@Override
	public void setProgress( final double progress )
	{
		mastodonLogPanel.setProgress( progress, unknownSource );
	}
}
