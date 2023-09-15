package org.mastodon.app.logging;

import org.scijava.log.LogLevel;
import org.scijava.log.LogMessage;
import org.scijava.log.LogSource;

/**
 * A {@link MastodonLogger} that appends messages to {@link System#out} and
 * {@link System#err}. Progress messages are discarded.
 *
 * @author Jean-Yves Tinevez
 */
public class SysOutMastodonLogger extends DefaultMastodonLogger
{

	public SysOutMastodonLogger()
	{
		super( new MastodonLogListener()
		{
			@Override
			public void onSetStatus( LogSource source, String status )
			{
				System.out.println( source + " " + status );
			}

			@Override
			public void onSetProgress( LogSource source, double progress )
			{

			}

			@Override
			public void messageLogged( LogMessage message )
			{
				final int level = message.level();
				if ( level < LogLevel.WARN )
					System.err.println( message );
				else
					System.out.println( message );
			}
		}, LogSource.newRoot(), LogLevel.INFO );

	}

}
