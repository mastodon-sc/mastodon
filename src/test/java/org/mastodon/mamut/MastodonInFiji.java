package org.mastodon.mamut;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.launcher.MastodonLauncherCommand;
import org.mastodon.mamut.project.MamutImagePlusProject;
import org.scijava.Context;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;

public class MastodonInFiji
{

	public static void main( final String[] args ) throws Exception
	{
		setSystemLookAndFeelAndLocale();
		ImageJ.main( args );

		final String path = "/Users/tinevez/Desktop/mitosis.tif";

		final ImagePlus imp = IJ.openImage( path );
//		final ImagePlus imp = IJ.openVirtual( path );

		imp.show();

		final MastodonLauncherCommand launcher = new MastodonLauncherCommand();
		try (Context context = new Context())
		{
			context.inject( launcher );
			launcher.run();
		}
	}

	public static void main2( final String[] args ) throws IOException, SpimDataException
	{
		setSystemLookAndFeelAndLocale();
		ImageJ.main( args );
		
		final String path = "/Users/tinevez/Desktop/mitosis.tif";
		
		final ImagePlus imp = IJ.openImage( path );
//		final ImagePlus imp = IJ.openVirtual( path );
		
		imp.show();

		final MamutImagePlusProject project = new MamutImagePlusProject( imp );

		final WindowManager wm = new WindowManager( new Context() );
		wm.getProjectManager().open( project );
		
		new MainWindow( wm ).setVisible( true );
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
