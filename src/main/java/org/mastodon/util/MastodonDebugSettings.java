package org.mastodon.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public interface MastodonDebugSettings
{
	/**
	 * Turn off JMenu Accelerator shortcuts.
	 * <p>
	 * In older versions of java8 (eg jdk1.8.0_152) on mac the following
	 * happens: With {@code apple.laf.useScreenMenuBar == true}. After holding a
	 * key (any) for some time, the menu starts swallowing KEY_PRESSED events
	 * for accelerator shortcuts, but without triggering the linked Action. Can
	 * only be fixed by closing and reopening the window. Happens also in
	 * IntelliJ IDE for example, so not Mastodon-specific.
	 * <p>
	 * Seems to be fixed in jdk1.8.0_162.
	 *
	 * @return whether menu accelerators should be used.
	 */
	boolean isUseMenuAccelerators();

	static MastodonDebugSettings getInstance()
	{
		return IO.getInstance();
	}

	/**
	 * Load/save to {@code $HOME/.mastodon/debug.yaml}.
	 */
	class IO
	{
		public static class DebugSettingsImpl implements MastodonDebugSettings
		{
			private boolean useMenuAccelerators = true;

			@Override
			public boolean isUseMenuAccelerators()
			{
				return useMenuAccelerators;
			}

			public void setUseMenuAccelerators( final boolean useMenuAccelerators )
			{
				this.useMenuAccelerators = useMenuAccelerators;
			}
		}

		private static DebugSettingsImpl instance;

		private static DebugSettingsImpl getInstance()
		{
			synchronized ( IO.class )
			{
				if ( instance == null )
				{
					instance = load();
					if ( instance == null )
					{
						instance = new DebugSettingsImpl();
						save( instance );
					}
				}
			}
			return instance;
		}

		private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/debug.yaml";

		private static void save( final DebugSettingsImpl debugSettings )
		{
			try
			{
				new File( STYLE_FILE ).getParentFile().mkdirs();
				final FileWriter output = new FileWriter( STYLE_FILE );
				final Yaml yaml = createYaml();
				yaml.dump( debugSettings, output );
				output.close();
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}

		private static DebugSettingsImpl load()
		{
			try
			{
				final FileReader input = new FileReader( STYLE_FILE );
				final Yaml yaml = createYaml();
				final Object obj = yaml.load( input );
				if ( obj instanceof DebugSettingsImpl )
					return ( DebugSettingsImpl ) obj;
			}
			catch ( final FileNotFoundException e )
			{
				System.out.println( "Debug settings file " + STYLE_FILE + " not found. Using defaults." );
			}
			return null;
		}

		private static Yaml createYaml()
		{
			final DumperOptions dumperOptions = new DumperOptions();
			dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
			final Representer representer = new Representer();
			representer.addClassTag( DebugSettingsImpl.class, new Tag( "!debugsettings" ) );
			final Constructor constructor = new Constructor();
			constructor.addTypeDescription( new TypeDescription( DebugSettingsImpl.class, "!debugsettings" ) );
			return new Yaml( constructor, representer, dumperOptions );
		}
	}
}
