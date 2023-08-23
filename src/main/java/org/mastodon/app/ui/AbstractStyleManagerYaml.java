package org.mastodon.app.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
		try
		{
			final FileReader input = new FileReader( filename );
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
