package org.mastodon.views.trackscheme.display.style;

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

/**
 * Manages a collection of {@link TrackSchemeStyle}.
 * <p>
 * Has serialization / deserialization facilities and can return models based on
 * the collection it manages.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class TrackSchemeStyleManager extends AbstractStyleManager< TrackSchemeStyleManager, TrackSchemeStyle >
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/trackschemestyles.yaml";

	/**
	 * A {@code TrackSchemeStyle} that has the same properties as the default
	 * style. In contrast to defaultStyle this will always refer to the same
	 * object, so a trackscheme can just use this one style to listen for
	 * changes and for painting.
	 */
	private final TrackSchemeStyle forwardDefaultStyle;

	private final TrackSchemeStyle.UpdateListener updateForwardDefaultListeners;

	public TrackSchemeStyleManager()
	{
		this( true );
	}

	public TrackSchemeStyleManager( final boolean loadStyles )
	{
		forwardDefaultStyle = TrackSchemeStyle.defaultStyle().copy();
		updateForwardDefaultListeners = () -> forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
		if ( loadStyles )
			loadStyles();
	}

	@Override
	protected List< TrackSchemeStyle > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( TrackSchemeStyle.defaults ) );
	}

	@Override
	public synchronized void setDefaultStyle( final TrackSchemeStyle style )
	{
		defaultStyle.updateListeners().remove( updateForwardDefaultListeners );
		defaultStyle = style;
		forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
	}

	/**
	 * Returns a final {@link TrackSchemeStyle} instance that always has the
	 * same properties as the default style.
	 *
	 * @return a style instance that always has the same properties as the default style.
	 */
	public TrackSchemeStyle getForwardDefaultStyle()
	{
		return forwardDefaultStyle;
	}

	public void loadStyles()
	{
		loadStyles( STYLE_FILE );
	}

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( TrackSchemeStyle::getName ).collect( Collectors.toSet() );
		try
		{
			final FileReader input = new FileReader( filename );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
//					System.out.println( "TrackSchemeStyleManager.loadStyles" );
//					System.out.println( defaultStyleName );
				}
				else if ( obj instanceof TrackSchemeStyle )
				{
					final TrackSchemeStyle ts = ( TrackSchemeStyle ) obj;
					if ( null != ts )
					{
						// sanity check: style names must be unique
						if ( names.add( ts.getName() ) )
							userStyles.add( ts );
						else
						{
//							System.out.println( "Discarded style with duplicate name \"" + ts.getName() + "\"." );
						}
					}
				}
			}
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
//			System.out.println( "TrackScheme style file " + filename + " not found. Using builtin styles." );
		}
	}

	@Override
	public void saveStyles()
	{
		saveStyles( STYLE_FILE );
	}

	public void saveStyles( final String filename )
	{
		try
		{
			mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( defaultStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
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
