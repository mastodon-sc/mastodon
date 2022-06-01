package org.mastodon.ui;

import java.io.IOException;
import java.util.ArrayList;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

@Plugin( type = IOPlugin.class )
public class DndHandler extends AbstractIOPlugin< Object >
{

	@Override
	public boolean supportsOpen( final Location source )
	{
		if ( !( source instanceof FileLocation ) )
			return false;
		if ( !( source.getName().endsWith( ".mastodon" ) ) )
			return false;
		return true;
	}

	@Override
	public Object open( final Location source ) throws IOException
	{
		final FileLocation fsource = source instanceof FileLocation ? ( FileLocation ) source : null;
		if ( fsource == null )
			return null; // NB: shouldn't happen... (in theory)

		startMastodon( fsource.getFile().getAbsolutePath() );
		return FAKE_INPUT;
	}

	// the "innocent" product of the (hypothetical) file reading...
	private static final Object FAKE_INPUT = new ArrayList<>( 0 );

	@Override
	public Class< Object > getDataType()
	{
		return Object.class;
	}

	// ------------------------------------------------
	void startMastodon( final String projectFile )
	{
		try
		{
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			final WindowManager windowManager = new WindowManager( getContext() );
			windowManager.getProjectManager().open( new MamutProjectIO().load( projectFile ), true );
			new MainWindow( windowManager ).setVisible( true );
		}
		catch ( IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}
}
