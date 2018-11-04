package org.mastodon.revised.ui.coloring.feature;

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
import org.yaml.snakeyaml.Yaml;

public class FeatureColorModeManager extends AbstractStyleManager< FeatureColorModeManager, FeatureColorMode >
{

	private static final String COLOR_MODE_FILE = System.getProperty( "user.home" ) + "/.mastodon/colormodes.yaml";

	private final FeatureColorMode forwardDefaultMode;

	private final FeatureColorMode.UpdateListener updateForwardDefaultListeners;

	public FeatureColorModeManager()
	{
		this( true );
	}

	public FeatureColorModeManager( final boolean loadModes )
	{
		forwardDefaultMode = FeatureColorMode.defaultMode().copy();
		updateForwardDefaultListeners = () -> forwardDefaultMode.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
		if ( loadModes )
			loadModes();
	}

	public FeatureColorMode getForwardDefaultMode()
	{
		return forwardDefaultMode;
	}

	@Override
	public synchronized void setDefaultStyle( final FeatureColorMode featureColorMode )
	{
		defaultStyle.updateListeners().remove( updateForwardDefaultListeners );
		defaultStyle = featureColorMode;
		forwardDefaultMode.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
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
			new File( filename ).mkdirs();
			final Yaml yaml = FeatureColorModeIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( defaultStyle.getName() );
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
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
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

}