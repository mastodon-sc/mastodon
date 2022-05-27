package org.mastodon.graph;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

public class BranchGraphExample
{

	public static void main( final String[] args ) throws IOException
	{
		setSystemLookAndFeelAndLocale();
		try (final Context context = new Context())
		{
			final String projectPath = "samples/test_branchgraph.mastodon";
//			final String projectPath = "samples/mette_e1.mastodon";
//			final String projectPath = "samples/mette_e1_small.mastodon";
			final MamutProject project = new MamutProjectIO().load( projectPath );

			final WindowManager wm = new WindowManager( context );
			wm.getProjectManager().open( project );
			wm.getAppModel().getBranchGraphSync().sync();
			new MainWindow( wm ).setVisible( true );
		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}
	}

	private static final void setSystemLookAndFeelAndLocale()
	{
		Locale.setDefault( Locale.ROOT );
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
	}
}
