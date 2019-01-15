package org.mastodon.revised.mamut.launcher;

import java.util.Locale;

import javax.swing.UIManager;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Plugin;

@Plugin( type = Command.class, menuPath = "Plugins>Mastodon" )
public class MastodonLauncherCommand extends ContextCommand
{

	@Override
	public void run()
	{
		final MastodonLauncher launcher = new MastodonLauncher( getContext() );
		launcher.setLocationByPlatform( true );
		launcher.setLocationRelativeTo( null );
		launcher.setVisible( true );
	}

	public static void main( final String[] args ) throws Exception
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final MastodonLauncherCommand launcher = new MastodonLauncherCommand();
		new Context().inject( launcher );
		launcher.run();
	}
}
