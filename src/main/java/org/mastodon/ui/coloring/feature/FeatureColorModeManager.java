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
package org.mastodon.ui.coloring.feature;

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

import org.scijava.listeners.Listeners;
import org.yaml.snakeyaml.Yaml;

import bdv.ui.settings.style.AbstractStyleManager;

public class FeatureColorModeManager extends AbstractStyleManager< FeatureColorModeManager, FeatureColorMode >
{
	private static final String COLOR_MODE_FILE = System.getProperty( "user.home" ) + "/.mastodon/colormodes.yaml";

	public interface FeatureColorModesListener
	{
		public void featureColorModesChanged();
	}

	private final Listeners.List< FeatureColorModesListener > featureColorModesListeners;

	public FeatureColorModeManager()
	{
		this( true );
	}

	public FeatureColorModeManager( final boolean loadModes )
	{
		if ( loadModes )
			loadModes();
		featureColorModesListeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public void set( final FeatureColorModeManager other )
	{
		super.set( other );
		notifyListeners();
	}

	public Listeners< FeatureColorModesListener > listeners()
	{
		return featureColorModesListeners;
	}

	@Override
	public void saveStyles()
	{
		saveStyles( COLOR_MODE_FILE );
	}

	public void saveStyles( final String filename )
	{
		try (final FileWriter output = new FileWriter( filename ))
		{
			new File( filename ).mkdirs(); // TODO pointless. FileWriter already opened or failed.
			final Yaml yaml = FeatureColorModeIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( selectedStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public void loadModes()
	{
		loadStyles( COLOR_MODE_FILE );
	}

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( FeatureColorMode::getName ).collect( Collectors.toSet() );
		try (final FileReader input = new FileReader( filename ))
		{
			final Yaml yaml = FeatureColorModeIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
				}
				else if ( obj instanceof FeatureColorMode )
				{
					final FeatureColorMode ts = ( FeatureColorMode ) obj;
					if ( null != ts )
					{
						// sanity check: style names must be unique
						if ( names.add( ts.getName() ) )
							userStyles.add( ts );
						else
							System.out.println( "Discarded color mode with duplicate name \"" + ts.getName() + "\"." );
					}
				}
			}
			setSelectedStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "Feature color mode file " + filename + " not found. Using builtin styles." );
		}
		catch ( final IOException e1 )
		{
			System.out.println( "Troube reading feature color mode file " + filename + ". Using builtin styles." );
			e1.printStackTrace();
		}
	}

	@Override
	protected List< FeatureColorMode > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( FeatureColorMode.defaults ) );
	}

	private void notifyListeners()
	{
		featureColorModesListeners.list.forEach( FeatureColorModesListener::featureColorModesChanged );
	}
}
