package org.mastodon.revised.ui.keymap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.app.ui.settings.style.AbstractStyleManager;
import org.mastodon.revised.mamut.Mastodon;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Manages a collection of {@link Keymap}.
 * <p>
 * Provides de/serialization of user-defined keymaps.
 *
 * @author Tobias Pietzsch
 */
public class KeymapManager extends AbstractStyleManager< KeymapManager, Keymap >
{
	private static final String KEYMAPS_PATH = System.getProperty( "user.home" ) + "/.mastodon/keymaps/";

	/**
	 * A {@code Keymap} that has the same properties as the default
	 * keymap. In contrast to defaultStyle this will always
	 * refer to the same object, so a consumers can just use this one
	 * RenderSettings to listen for changes and for painting.
	 */
	private final Keymap forwardDefaultKeymap;

	public KeymapManager()
	{
		this( true );
	}

	public KeymapManager( final boolean loadStyles )
	{
		forwardDefaultKeymap = new Keymap();
		if ( loadStyles )
			loadStyles();
	}

	@Override
	protected List< Keymap > loadBuiltinStyles()
	{
		try
		{
			return Arrays.asList( loadBuiltinStyle( "Default", "keyconfig.yaml" ) );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static Keymap loadBuiltinStyle( final String name, final String filename ) throws IOException
	{
		final Reader reader = new InputStreamReader( Mastodon.class.getResourceAsStream( filename ) );
		final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( reader ) );
		reader.close();
		return new Keymap( name, config );
	}

	@Override
	public synchronized void setDefaultStyle( final Keymap keymap )
	{
		System.out.println( "KeymapManager.setDefaultStyle" );
		super.setDefaultStyle( keymap );
		forwardDefaultKeymap.set( defaultStyle );
	}

	/**
	 * Returns a final {@link Keymap} instance that always has the same
	 * properties as the default keymap.
	 */
	public Keymap getForwardDefaultKeymap()
	{
		return forwardDefaultKeymap;
	}

	public void loadStyles()
	{
		loadStyles( KEYMAPS_PATH );
	}

	public void loadStyles( final String directory )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( Keymap::getName ).collect( Collectors.toSet() );
		try
		{
			String filename = KEYMAPS_PATH + "/keymaps.yaml";

			final KeymapsListIO keymapsList;
			try
			{
				final FileReader input = new FileReader( filename );
				keymapsList = new Yaml().loadAs( input, KeymapsListIO.class );
				input.close();
			}
			catch ( final FileNotFoundException e )
			{
				System.out.println( "Keymap list file " + filename + " not found. Using builtin styles." );
				return;
			}

			for ( final Map.Entry< String, String > entry : keymapsList.getFileNameToKeymapName().entrySet() )
			{
				filename = KEYMAPS_PATH + "/" + entry.getKey();
				try
				{
					final String name = entry.getValue();
					final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( filename ) );
					// sanity check: style names must be unique
					if ( names.add( name ) )
						userStyles.add( new Keymap( name, config ) );
					else
						System.out.println( "Discarded style with duplicate name \"" + name + "\"." );
				}
				catch ( final FileNotFoundException e )
				{
					System.out.println( "Keymap file " + filename + " not found. Skipping." );
				}
			}

			final String defaultStyleName = keymapsList.defaultKeymapName;
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void saveStyles()
	{
		saveStyles( KEYMAPS_PATH );
	}

	public void saveStyles( final String directory )
	{
		try
		{
			new File( directory ).mkdirs();

			final KeymapsListIO keymapsList = new KeymapsListIO(
					defaultStyle.getName(),
					userStyles.stream().map( Keymap::getName ).collect( Collectors.toList() ) );

			String filename = KEYMAPS_PATH + "/keymaps.yaml";
			final FileWriter output = new FileWriter( filename );
			final DumperOptions options = new DumperOptions();
			options.setPrettyFlow( true );
			new Yaml( options ).dump( keymapsList, output );
			output.close();

			for ( final Keymap keymap : userStyles )
			{
				filename = KEYMAPS_PATH + "/" + keymapsList.keymapNameToFileName.get( keymap.getName() );
				final List< InputTriggerDescription > descriptions = new InputTriggerDescriptionsBuilder( keymap.getConfig() ).getDescriptions();
				YamlConfigIO.write( descriptions, filename );
			}
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
