package org.mastodon.ui.util;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.scijava.Context;
import org.scijava.log.LogMessage;
import org.scijava.log.LogSource;
import org.scijava.ui.swing.console.LoggingPanel;

public class MastodonLogPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final String PREF_KEY = "MastodonLoggerPreferencesKey";

	private final Context context;

	private final LoggingPanel loggingPanel;

	private final JPanel panelProgressBars;

	private final Map< LogSource, JProgressBar > progressBars;

	public MastodonLogPanel( final Context context )
	{
		this.context = ( null == context ) ? new Context() : context;
		setLayout( new BorderLayout( 5, 5 ) );

		panelProgressBars = new JPanel();
		add( panelProgressBars, BorderLayout.NORTH );
		panelProgressBars.setLayout( new BoxLayout( panelProgressBars, BoxLayout.PAGE_AXIS ) );

		loggingPanel = new LoggingPanel( this.context, PREF_KEY );
		loggingPanel.setOpaque( false );
		add( loggingPanel, BorderLayout.CENTER );

		this.progressBars = new HashMap<>();
	}

	public void log( final String msg, final int level, final LogSource source )
	{
		loggingPanel.messageLogged( new LogMessage( source, level, msg ) );
	}

	public void setStatus( final String status, final LogSource source )
	{
		final JProgressBar pb = getProgressBar( source );
		pb.setString( status );
	}

	public void setProgress( final double progress, final LogSource source )
	{
		if ( progress >= 1 )
		{
			removeProgressBar( source );
			return;
		}
		final JProgressBar pb = getProgressBar( source );
		pb.setValue( ( int ) ( 100 * progress ) );
	}

	private void removeProgressBar( final LogSource source )
	{
		final JProgressBar pb = progressBars.remove( source );
		if ( null == pb )
			return;

		panelProgressBars.remove( pb );
		revalidate();
	}

	private JProgressBar getProgressBar( final LogSource source )
	{
		JProgressBar pg = progressBars.get( source );
		if ( null == pg )
		{
			pg = new JProgressBar( 0, 100 );
			pg.setStringPainted( true );
			panelProgressBars.add( pg );
			revalidate();
			progressBars.put( source, pg );
		}
		return pg;
	}
}