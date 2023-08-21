package org.mastodon.ui.keymap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;

/**
 * Extends {@link KeymapManager} so that we can load the builtin keymaps for
 * Mastodon, and save keymaps in the Mastodon folder.
 */
public class MastodonKeymapManager extends KeymapManager
{

	private static final String CONFIG_DIR = System.getProperty( "user.home" ) + "/.mastodon/";

	private static List< Keymap > loadedBuiltinStyles = null;

	public MastodonKeymapManager()
	{
		super();
	}

	public MastodonKeymapManager( final boolean loadStyles )
	{
		super( CONFIG_DIR );
	}

	@Override
	protected List< Keymap > loadBuiltinStyles()
	{
		try
		{
			synchronized ( KeymapManager.class )
			{
				if ( loadedBuiltinStyles == null )
					loadedBuiltinStyles = Arrays.asList(
							loadBuiltinStyle( "Default", "keyconf_mastodon.yaml" ),
							loadBuiltinStyle( "All BDV keys", "keyconf_fullbdv.yaml" ) );
				return loadedBuiltinStyles;
			}
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static Keymap loadBuiltinStyle( final String name, final String filename ) throws IOException
	{
		final InputStreamReader reader = new InputStreamReader( MastodonKeymapManager.class.getResourceAsStream( filename ) );
		final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( reader ) );
		reader.close();
		return new Keymap( name, config );
	}
}
