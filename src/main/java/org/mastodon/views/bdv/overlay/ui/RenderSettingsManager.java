package org.mastodon.views.bdv.overlay.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.app.ui.settings.style.AbstractStyleManager;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.yaml.snakeyaml.Yaml;

/**
 * Manages a list of {@link RenderSettings} for multiple BDV windows. Provides
 * models based on a common list of settings than can be used in swing items.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class RenderSettingsManager extends AbstractStyleManager< RenderSettingsManager, RenderSettings >
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/rendersettings.yaml";

	/**
	 * A {@code RenderSettings} that has the same properties as the default
	 * RenderSettings. In contrast to defaultStyle this will always
	 * refer to the same object, so a consumers can just use this one
	 * RenderSettings to listen for changes and for painting.
	 */
	private final RenderSettings forwardDefaultStyle;

	private final RenderSettings.UpdateListener updateForwardDefaultListeners;

	public RenderSettingsManager()
	{
		this( true );
	}

	public RenderSettingsManager( final boolean loadStyles )
	{
		forwardDefaultStyle = RenderSettings.defaultStyle().copy();
		updateForwardDefaultListeners = () -> forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
		if ( loadStyles )
			loadStyles();
	}

	@Override
	protected List< RenderSettings > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( RenderSettings.defaults ) );
	}

	@Override
	public synchronized void setDefaultStyle( final RenderSettings renderSettings )
	{
		defaultStyle.updateListeners().remove( updateForwardDefaultListeners );
		defaultStyle = renderSettings;
		forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
	}

	/**
	 * Returns a final {@link RenderSettings} instance that always has the same
	 * properties as the default style.
	 *
	 * @return the {@link RenderSettings} instance.
	 */
	public RenderSettings getForwardDefaultStyle()
	{
		return forwardDefaultStyle;
	}

	public void loadStyles()
	{
		loadStyles( STYLE_FILE );
	}

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( RenderSettings::getName ).collect( Collectors.toSet() );
		try
		{
			final FileReader input = new FileReader( filename );
			final Yaml yaml = RenderSettingsIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
//					System.out.println( "RenderSettingsManager.loadStyles" );
//					System.out.println( defaultStyleName );
				}
				else if ( obj instanceof RenderSettings )
				{
					final RenderSettings ts = ( RenderSettings ) obj;
					if ( null != ts )
					{
						// sanity check: style names must be unique
						if ( names.add( ts.getName() ) )
							userStyles.add( ts );
						else
						{
//							System.out.println( "Discarded style with duplicate name \"" + ts.getName() + "\"." );
						}
					}
				}
			}
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
//			System.out.println( "Bdv style file " + filename + " not found. Using builtin styles." );
		}
	}

	@Override
	public void saveStyles()
	{
		saveStyles( STYLE_FILE );
	}

	public void saveStyles( final String filename )
	{
		try
		{
			mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = RenderSettingsIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( defaultStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * STATIC UTILITIES
	 */

	private static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}
}
