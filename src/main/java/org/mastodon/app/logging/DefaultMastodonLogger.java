package org.mastodon.app.logging;

import javax.swing.JPanel;

import org.scijava.log.DefaultLogger;
import org.scijava.log.LogSource;

/**
 * A default {@link MastodonLogger} that echoes messages and progress to a
 * {@link JPanel}. Suitable to be used in a GUI.
 *
 * @author Jean-Yves Tinevez
 */
public class DefaultMastodonLogger extends DefaultLogger implements MastodonLogger, MastodonLogListener
{
	private final MastodonLogListener destination;

	public DefaultMastodonLogger( MastodonLogListener destination, LogSource source, int level )
	{
		super( destination, source, level );
		this.destination = destination;
	}

	@Override
	public MastodonLogger subLogger( String name, int level )
	{
		return new DefaultMastodonLogger( this, getSource().subSource( name ), level );
	}

	@Override
	public void setStatus( final String status )
	{
		destination.onSetStatus( getSource(), status );
	}

	@Override
	public void setProgress( final double progress )
	{
		destination.onSetProgress( getSource(), progress );
	}

	@Override
	public void onSetStatus( LogSource source, String status )
	{
		destination.onSetStatus( source, status );
	}

	@Override
	public void onSetProgress( LogSource source, double progress )
	{
		destination.onSetProgress( source, progress );
	}
}
