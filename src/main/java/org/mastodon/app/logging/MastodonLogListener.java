package org.mastodon.app.logging;

import org.scijava.log.LogListener;
import org.scijava.log.LogSource;

public interface MastodonLogListener extends LogListener
{
	void onSetStatus( LogSource source, String status );

	void onSetProgress( LogSource source, double progress );
}
