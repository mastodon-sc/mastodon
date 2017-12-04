package org.mastodon.revised.bdv.overlay.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.yaml.snakeyaml.Yaml;

/**
 * Manages a list of {@link RenderSettings} for multiple BDV windows. Provides
 * models based on a common list of settings than can be used in swing items.
 *
 * @author Jean-Yves Tinevez
 */
public class RenderSettingsManager
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/rendersettings.yaml";

	private final List< RenderSettings > builtinRenderSettings;

	private final List< RenderSettings > userRenderSettings;

	private RenderSettings defaultRenderSettings;

	/**
	 * A {@code RenderSettings} that has the same properties as the default
	 * RenderSettings. In contrast to defaultRenderSettings this will always
	 * refer to the same object, so a consumers can just use this one
	 * RenderSettings to listen for changes and for painting.
	 */
	private final RenderSettings forwardDefaultRenderSettings;

	private final RenderSettings.UpdateListener updateForwardDefaultListeners;

	public RenderSettingsManager()
	{
		this( true );
	}

	public RenderSettingsManager( final boolean loadRenderSettings )
	{
		builtinRenderSettings = Collections.unmodifiableList( new ArrayList<>( RenderSettings.defaults ) );
		userRenderSettings = new ArrayList<>();
		defaultRenderSettings = builtinRenderSettings.get( 0 );
		forwardDefaultRenderSettings = RenderSettings.defaultStyle().copy();
		updateForwardDefaultListeners = () -> forwardDefaultRenderSettings.set( defaultRenderSettings );
		defaultRenderSettings.addUpdateListener( updateForwardDefaultListeners );
		if ( loadRenderSettings )
			loadRenderSettings();
	}

	public synchronized void setDefaultStyle( final RenderSettings renderSettings )
	{
		defaultRenderSettings.removeUpdateListener( updateForwardDefaultListeners );
		defaultRenderSettings = renderSettings;
		forwardDefaultRenderSettings.set( defaultRenderSettings );
		defaultRenderSettings.addUpdateListener( updateForwardDefaultListeners );
	}

	public List< RenderSettings > getBuiltinRenderSettings()
	{
		return builtinRenderSettings;
	}

	public List< RenderSettings > getUserRenderSettings()
	{
		return Collections.unmodifiableList( userRenderSettings );
	}

	public RenderSettings getDefaultRenderSettings()
	{
		return defaultRenderSettings;
	}

	public synchronized void remove( final RenderSettings renderSettings )
	{
		if ( defaultRenderSettings.equals( renderSettings ) )
			setDefaultStyle( builtinRenderSettings.get( 0 ) );
		userRenderSettings.remove( renderSettings );
	}

	public synchronized void rename( final RenderSettings renderSettings, final String newName )
	{
		if ( renderSettings.getName().equals( newName ) )
			return;

		if ( nameExists( newName ) )
			throw new IllegalArgumentException( "RenderSettings \"" + newName + "\" already exists." );

		renderSettings.setName( newName );
	}

	/**
	 * Returns a final {@link RenderSettings} instance that always has the same
	 * properties as the default style.
	 */
	public RenderSettings getForwardDefaultRenderSettings()
	{
		return forwardDefaultRenderSettings;
	}

	private boolean nameExists( final String name )
	{
		return styleForName( name ).isPresent();
	}

	private Optional< RenderSettings > styleForName( final String name )
	{
		return Stream.concat( builtinRenderSettings.stream(), userRenderSettings.stream() ).filter( style -> style.getName().equals( name ) ).findFirst();
	}

	/**
	 * Returns a copy of the specified {@link RenderSettings}, making sure that
	 * the copy receives a name not already present in this manager list of
	 * {@link RenderSettings}.
	 *
	 * @param current
	 *            the {@link RenderSettings} to copy.
	 * @return a new {@link RenderSettings}
	 */
	public RenderSettings duplicate( final RenderSettings current )
	{
		final String name = current.getName();
		final Pattern pattern = Pattern.compile( "(.+) \\((\\d+)\\)$" );
		final Matcher matcher = pattern.matcher( name );
		int n;
		String prefix;
		if ( matcher.matches() )
		{
			final String nstr = matcher.group( 2 );
			n = Integer.parseInt( nstr );
			prefix = matcher.group( 1 );
		}
		else
		{
			n = 1;
			prefix = name;
		}

		String newName;
		do
			newName = prefix + " (" + ( ++n ) + ")";
		while ( nameExists( newName ) );

		final RenderSettings newStyle = current.copy( newName );
		userRenderSettings.add( newStyle );
		return newStyle;
	}

	public void loadRenderSettings()
	{
		loadRenderSettings( STYLE_FILE );
	}

	public void loadRenderSettings( final String filename )
	{
		userRenderSettings.clear();
		final Set< String > names = builtinRenderSettings.stream().map( RenderSettings::getName ).collect( Collectors.toSet() );
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
					System.out.println( "RenderSettingsManager.loadRenderSettings" );
					System.out.println( defaultStyleName );
				}
				else if ( obj instanceof RenderSettings )
				{
					final RenderSettings ts = ( RenderSettings ) obj;
					if ( null != ts )
					{
						// sanity check: RenderSettings names must be unique
						if ( names.add( ts.getName() ) )
							userRenderSettings.add( ts );
						else
							System.out.println( "Discarded settings with duplicate name \"" + ts.getName() + "\"." );
					}
				}
			}
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinRenderSettings.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "RenderSettings file " + filename + " not found. Using builtin styles." );
		}
	}

	public void saveRenderSettings()
	{
		saveRenderSettings( STYLE_FILE );
	}

	public void saveRenderSettings( final String filename )
	{
		try
		{
			mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = RenderSettingsIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( defaultRenderSettings.getName() );
			objects.addAll( userRenderSettings );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public void set( final RenderSettingsManager other )
	{
		setSnapshot( other.getSnapshot() );
	}

	static class Snapshot
	{
		private final List< RenderSettings > userStyles;

		private final String defaultStyleName;

		public Snapshot( final RenderSettingsManager manager )
		{
			this.userStyles = manager.getUserRenderSettings().stream().map( s -> s.copy() ).collect( Collectors.toList() );
			this.defaultStyleName = manager.getDefaultRenderSettings().getName();
		}
	}

	Snapshot getSnapshot()
	{
		return new Snapshot( this );
	}

	void setSnapshot( final Snapshot snapshot )
	{
		userRenderSettings.clear();
		snapshot.userStyles.forEach( s -> userRenderSettings.add( s.copy() ) );
		setDefaultStyle( styleForName( snapshot.defaultStyleName ).orElseGet( () -> builtinRenderSettings.get( 0 ) ) );
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
