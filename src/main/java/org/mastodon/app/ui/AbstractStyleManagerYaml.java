/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import bdv.ui.settings.style.AbstractStyleManager;
import bdv.ui.settings.style.Style;

/**
 * Intermediate abstract class for {@link AbstractStyleManager}s that offers
 * common facilities to load / save user styles from / to a YAML file.
 */
public abstract class AbstractStyleManagerYaml< M extends AbstractStyleManager< M, S >, S extends Style< S > > extends AbstractStyleManager< M, S >
{

	protected abstract Yaml createYaml();

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( S::getName ).collect( Collectors.toSet() );
		try (final FileReader input = new FileReader( filename ))
		{
			final Iterable< Object > objs = createYaml().loadAll( input );
			String defaultStyleName = null;
			@SuppressWarnings( "unchecked" )
			final Class< S > klass = ( Class< S > ) selectedStyle.getClass();
			try
			{
				for ( final Object obj : objs )
				{
					if ( obj instanceof String )
					{
						defaultStyleName = ( String ) obj;
					}
					else if ( klass.isInstance( obj ) )
					{
						@SuppressWarnings( "unchecked" )
						final S style = ( S ) obj;
						if ( null != style )
						{
							// sanity check: settings names must be unique
							if ( names.add( style.getName() ) )
								userStyles.add( style );
							else
								System.out.println( "Discarded settings with duplicate name \"" + style.getName() + "\"." );
						}
					}
				}
			}
			catch ( final YAMLException pe )
			{
				System.out.println( "Problem parsing the settings file " + filename + ":\n" + pe.getMessage() + "\nUsing builtin settings." );
				setSelectedStyle( builtinStyles.get( 0 ) );
				return;
			}
			setSelectedStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "Settings file " + filename + " not found. Using builtin settings." );
		}
		catch ( final IOException e1 )
		{
			System.out.println( "Issues reading the settings file " + filename + ": " + e1.getMessage() );
		}
	}
	
	/**
	 * Deal with possible legacy file.
	 * <p>
	 * Loads from the file if it exists, appends to the current user styles,
	 * then deletes the legacy file.
	 * 
	 * @param legacyFilePath
	 *            the path to the legacy file.
	 */
	protected void handleLegacyFile( final String legacyFilePath )
	{
		final File legacyFile = new File( legacyFilePath );
		if ( legacyFile.exists() )
		{
			final ArrayList< S > currentStyles = new ArrayList<>( userStyles );
			loadStyles( legacyFilePath );
			userStyles.addAll( currentStyles );
			try
			{
				Files.delete( legacyFile.toPath() );
			}
			catch ( final IOException e )
			{
				System.err.println( "Could not delete the legacy settings file: " + legacyFilePath );
				e.printStackTrace();
			}
		}
	}

	public void saveStyles( final String filename )
	{
		try
		{
			IOUtils.mkdirs( filename );
			new File( filename ).getParentFile().mkdirs();
			final FileWriter output = new FileWriter( filename );
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( selectedStyle.getName() );
			objects.addAll( userStyles );
			createYaml().dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			System.out.println( "Problem writing to the settings file " + filename + "\n" + e.getMessage() );
		}
	}
}
