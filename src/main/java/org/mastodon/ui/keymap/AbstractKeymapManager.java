/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mastodon.app.ui.settings.style.AbstractStyleManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Manages a collection of {@link Keymap}.
 * <p>
 * Provides de/serialization of user-defined keymaps.
 *
 * @author Tobias Pietzsch
 */
public abstract class AbstractKeymapManager< T extends AbstractKeymapManager< T > >
		extends AbstractStyleManager< T, Keymap >
{
	/**
	 * A {@code Keymap} that has the same properties as the default
	 * keymap. In contrast to defaultStyle this will always
	 * refer to the same object, so a consumers can just use this one
	 * to listen for changes.
	 */
	private final Keymap forwardDefaultKeymap;

	public AbstractKeymapManager()
	{
		forwardDefaultKeymap = new Keymap();
	}

	@Override
	public synchronized void setDefaultStyle( final Keymap keymap )
	{
		super.setDefaultStyle( keymap );
		forwardDefaultKeymap.set( defaultStyle );
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

	protected void loadStyles( final File directory ) throws IOException
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( Keymap::getName ).collect( Collectors.toSet() );
		Keymap defaultStyle = builtinStyles.get( 0 );

		File file = new File( directory, "/keymaps.yaml" );
		KeymapsListData keymapsList = null;
		try ( Reader input = new FileReader( file ) )
		{
			keymapsList = createYaml().loadAs( input, KeymapsListData.class );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "Keymap list file " + file + " not found. Using builtin styles." );
		}

		if ( keymapsList != null )
		{
			for ( final Map.Entry< String, String > entry : keymapsList.getFileNameToKeymapName().entrySet() )
			{
				file = new File( directory, entry.getKey() );
				try ( Reader reader = new FileReader( file ) )
				{
					final String name = entry.getValue();
					final InputTriggerConfig config = new InputTriggerConfig( YamlConfigIO.read( reader ) );
					// sanity check: style names must be unique
					if ( names.add( name ) )
						userStyles.add( new Keymap( name, config ) );
					else
						System.out.println( "Discarded style with duplicate name \"" + name + "\"." );
				}
				catch ( final FileNotFoundException e )
				{
					System.out.println( "Keymap file " + file + " not found. Skipping." );
				}
			}
			defaultStyle = styleForName( keymapsList.defaultKeymapName ).orElse( defaultStyle );
		}

		setDefaultStyle( defaultStyle );
	}

	protected void saveStyles( final File directory ) throws IOException
	{
		directory.mkdirs();

		final KeymapsListData keymapsList = new KeymapsListData(
				defaultStyle.getName(),
				userStyles.stream().map( Keymap::getName ).collect( Collectors.toList() ) );

		File file = new File( directory, "keymaps.yaml" );
		try ( FileWriter writer = new FileWriter( file ) )
		{
			createYaml().dump( keymapsList, writer );
		}

		for ( final Keymap keymap : userStyles )
		{
			final List< InputTriggerDescription > descriptions = new InputTriggerDescriptionsBuilder( keymap.getConfig() ).getDescriptions();
			file = new File( directory, keymapsList.keymapNameToFileName.get( keymap.getName() ) );
			try ( FileWriter writer = new FileWriter( file ) )
			{
				YamlConfigIO.write( descriptions, writer );
			}
		}
	}

	/**
	 * Creates YAML de/serializer that handles {@code KeymapsListIO} objects.
	 */
	private static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
		final Representer representer = new Representer();
		representer.addClassTag( KeymapsListData.class, new Tag( "!keymapslist" ) );
		final Constructor constructor = new Constructor();
		constructor.addTypeDescription( new TypeDescription( KeymapsListData.class, "!keymapslist" ) );
		return new Yaml( constructor, representer, dumperOptions );
	}
}
