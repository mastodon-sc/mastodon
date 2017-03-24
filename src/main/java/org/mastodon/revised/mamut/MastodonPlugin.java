package org.mastodon.revised.mamut;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

@Plugin( type = Command.class, menuPath = "Plugins>Mastodon (preview)" )
public class MastodonPlugin implements Command
{
	@Override
	public void run()
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final InputTriggerConfig keyconf = MainWindow.getInputTriggerConfig();
		final MainWindow mw = new MainWindow( keyconf );
		mw.pack();
		mw.setVisible( true );
	}
}
