/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.keymap;

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

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import bdv.ui.settings.style.AbstractStyleManager;

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
			synchronized ( KeymapManager.class )
			{
				if ( loadedBuiltinStyles == null )
					loadedBuiltinStyles = Arrays.asList(
							loadBuiltinStyle( "Default", "keyconf_mastodon.yaml" ),
							loadBuiltinStyle( "All BDV keys", "keyconf_fullbdv.yaml" ) );
			}
			return loadedBuiltinStyles;
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static List< Keymap > loadedBuiltinStyles;

	private static Keymap loadBuiltinStyle( final String name, final String filename ) throws IOException
	{
		final Reader reader = new InputStreamReader( KeymapManager.class.getResourceAsStream( filename ) );
		final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( reader ) );
		reader.close();
		return new Keymap( name, config );
	}

	@Override
	public synchronized void setSelectedStyle( final Keymap keymap )
	{
		super.setSelectedStyle( keymap );
		forwardDefaultKeymap.set( selectedStyle );
	}

	/**
	 * Returns a final {@link Keymap} instance that always has the same
	 * properties as the default keymap.
	 *
	 * @return a keymap instance that always has the same properties as the default keymap.
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
		Keymap defaultStyle = builtinStyles.get( 0 );
		try
		{
			String filename = KEYMAPS_PATH + "/keymaps.yaml";

			KeymapsListIO keymapsList = null;
			try
			{
				final FileReader input = new FileReader( filename );
				keymapsList = createYaml().loadAs( input, KeymapsListIO.class );
				input.close();
			}
			catch ( final FileNotFoundException e )
			{
				System.out.println( "Keymap list file " + filename + " not found. Using builtin styles." );
			}

			if ( keymapsList != null )
			{
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

				defaultStyle = styleForName( keymapsList.defaultKeymapName ).orElse( defaultStyle );
			}
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
		setSelectedStyle( defaultStyle );
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
					selectedStyle.getName(),
					userStyles.stream().map( Keymap::getName ).collect( Collectors.toList() ) );

			String filename = KEYMAPS_PATH + "/keymaps.yaml";
			final FileWriter output = new FileWriter( filename );
			createYaml().dump( keymapsList, output );
			output.close();

			for ( final Keymap keymap : userStyles )
			{
				filename = KEYMAPS_PATH + "/" + keymapsList.keymapNameToFileName.get( keymap.getName() );
				final List< InputTriggerDescription > descriptions =
						new InputTriggerDescriptionsBuilder( keymap.getConfig() ).getDescriptions();
				YamlConfigIO.write( descriptions, filename );
			}
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
		final Representer representer = new Representer( dumperOptions );
		representer.addClassTag( KeymapsListIO.class, new Tag( "!keymapslist" ) );
		final Constructor constructor = new Constructor( new LoaderOptions() );
		constructor.addTypeDescription( new TypeDescription( KeymapsListIO.class, "!keymapslist" ) );
		return new Yaml( constructor, representer, dumperOptions );
	}
}
