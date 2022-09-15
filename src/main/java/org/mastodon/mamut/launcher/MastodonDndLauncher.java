package org.mastodon.mamut.launcher;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;

import org.scijava.io.IOPlugin;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.log.LogService;

import javax.swing.WindowConstants;
import java.io.IOException;
import java.util.ArrayList;

@Plugin(type = IOPlugin.class)
public class MastodonDndLauncher extends AbstractIOPlugin<Object> {

	@Parameter
	private LogService logService;

	@Override
	public boolean supportsOpen(Location source) {
		final String sourcePath = source.getURI().getPath();
		logService.debug("MastodonDndLauncher was questioned: "+sourcePath);

		if (!(source instanceof FileLocation)) return false;
		return sourcePath.endsWith(".mastodon");
	}

	@Override
	public Object open(Location source) throws IOException {
		logService.debug("MastodonDndLauncher was asked to open: "+source.getURI().getPath());
		final FileLocation fsource = source instanceof FileLocation ? (FileLocation)source : null;
		if (fsource == null) return null; //NB: shouldn't happen... (in theory)

		final String projectPath = fsource.getFile().getAbsolutePath();

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final WindowManager windowManager = new WindowManager( getContext() );

		final MainWindow mainWindow = new MainWindow( windowManager );
		mainWindow.setVisible( true );
		mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		try {
			final MamutProject project = new MamutProjectIO().load( projectPath );
			windowManager.getProjectManager().open( project, true );
		} catch (Exception e) {
			logService.error( "Error reading Mastodon project file: " + projectPath );
			logService.error( "Error was: " + e.getMessage() );
		}

		return FAKE_INPUT;
	}

	//the "innocent" product of the (hypothetical) file reading...
	private static final Object FAKE_INPUT = new ArrayList<>(0);

	@Override
	public Class<Object> getDataType() {
		return Object.class;
	}
}
