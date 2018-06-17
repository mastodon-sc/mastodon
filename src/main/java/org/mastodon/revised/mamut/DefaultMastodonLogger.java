package org.mastodon.revised.mamut;

import javax.swing.JPanel;

import org.scijava.log.LogSource;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;

/**
 * A default {@link MastodonLogger} that echoes messages and progress to a
 * {@link JPanel}. Suitable to be used in a GUI.
 * 
 * @author Jean-Yves Tinevez
 *
 */
@Plugin( type = DefaultMastodonLogger.class )
public class DefaultMastodonLogger extends AbstractService implements MastodonLogger
{

	private MastodonLogPanel mastodonLogPanel;

	@Override
	public void initialize()
	{
		super.initialize();
		mastodonLogPanel = new MastodonLogPanel( getContext() );
	}

	public MastodonLogPanel getMastodonLogPanel()
	{
		return mastodonLogPanel;
	}

	@Override
	public LogSource getLogSourceRoot()
	{
		return mastodonLogPanel.getRootSource();
	}

	@Override
	public void info( final String message, final LogSource source )
	{
		mastodonLogPanel.info( message, source );
	}

	@Override
	public void info( final String message )
	{
		mastodonLogPanel.info( message );
	}

	@Override
	public void error( final String message, final LogSource source )
	{
		mastodonLogPanel.error( message, source );
	}

	@Override
	public void error( final String message )
	{
		mastodonLogPanel.error( message );
	}

	@Override
	public void setStatus( final String status, final LogSource source )
	{
		mastodonLogPanel.setStatus( status, source );
	}

	@Override
	public void setStatus( final String status )
	{
		mastodonLogPanel.setStatus( status );
	}

	@Override
	public void setProgress( final double progress, final LogSource source )
	{
		mastodonLogPanel.setProgress( progress, source );
	}

	@Override
	public void setProgress( final double progress )
	{
		mastodonLogPanel.setProgress( progress );
	}
}
