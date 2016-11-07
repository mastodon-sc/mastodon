package org.mastodon.revised.trackscheme.display.style;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import org.yaml.snakeyaml.Yaml;

/**
 * Manages a collection of {@link TrackSchemeStyle}.
 * <p>
 * Has serialization / deserialization facilities and can return models based on
 * the collection it manages.
 * 
 * @author Jean-Yves Tinevez
 *
 */
public class TrackSchemeStyleManager
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/trackschemestyles.yaml";

	private final Vector< TrackSchemeStyle > tss;

	public TrackSchemeStyleManager()
	{
		this.tss = new Vector<>();
		for ( final TrackSchemeStyle ts : TrackSchemeStyle.defaults )
			tss.add( ts );

		loadStyles();
	}

	public MutableComboBoxModel< TrackSchemeStyle > createComboBoxModel()
	{
		return new MyListModel();
	}

	public void add( final TrackSchemeStyle ts )
	{
		tss.add( ts );
	}

	public TrackSchemeStyle copy( TrackSchemeStyle current )
	{
		if ( null == current )
			current = TrackSchemeStyle.defaultStyle();

		final String name = current.name;
		final Pattern pattern = Pattern.compile( "(.+) \\((\\d+)\\)$" );
		final Matcher matcher = pattern.matcher( name );
		int n;
		String prefix;
		if ( matcher.matches() )
		{
			final String nstr = matcher.group( 2 );
			n = Integer.parseInt( nstr );
			prefix = matcher.group( 1 );
		}
		else
		{
			n = 1;
			prefix = name;
		}
		String newName;
		INCREMENT: while ( true )
		{
			newName = prefix + " (" + ( ++n ) + ")";
			for ( int j = 0; j < tss.size(); j++ )
			{
				if ( tss.get( j ).name.equals( newName ) )
					continue INCREMENT;
			}
			break;
		}

		final TrackSchemeStyle newStyle = current.copy( newName );
		tss.add( newStyle );
		return newStyle;
	}

	private void loadStyles()
	{
		try
		{
			final FileReader input = new FileReader( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			for ( final Object obj : objs )
			{
				final TrackSchemeStyle ts = ( TrackSchemeStyle ) obj;
				tss.add( ts );
			}

		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "TrackScheme style file " + STYLE_FILE + " not found. Using builtin styles." );
		}
	}

	public void saveStyles()
	{
		try
		{
			final List< TrackSchemeStyle > stylesToSave = new ArrayList<>();
			for ( int i = 0; i < tss.size(); i++ )
			{
				final TrackSchemeStyle style = tss.get( i );
				if ( TrackSchemeStyle.defaults.contains( style ) )
					continue;
				stylesToSave.add( style );
			}

			mkdirs( STYLE_FILE );
			final FileWriter output = new FileWriter( STYLE_FILE );
			final Yaml yaml = TrackSchemeStyleIO.createYaml();
			yaml.dumpAll( stylesToSave.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * INNER CLASS
	 */

	private final class MyListModel extends AbstractListModel< TrackSchemeStyle > implements MutableComboBoxModel< TrackSchemeStyle >
	{
		private static final long serialVersionUID = 1L;

		private Object selectedObject;

		/**
		 * Set the value of the selected item. The selected item may be null.
		 *
		 * @param anObject
		 *            The combo box value or null for no selection.
		 */
		@Override
		public void setSelectedItem( final Object anObject )
		{
			if ( ( selectedObject != null && !selectedObject.equals( anObject ) ) || selectedObject == null && anObject != null )
			{
				selectedObject = anObject;
				fireContentsChanged( this, -1, -1 );
			}
		}

		@Override
		public Object getSelectedItem()
		{
			return selectedObject;
		}

		@Override
		public int getSize()
		{
			return tss.size();
		}

		@Override
		public TrackSchemeStyle getElementAt( final int index )
		{
			if ( index >= 0 && index < tss.size() )
				return tss.elementAt( index );
			else
				return null;
		}

		@Override
		public void addElement( final TrackSchemeStyle anObject )
		{
			if ( tss.contains( anObject ) )
				return;
			tss.addElement( anObject );
			fireIntervalAdded( this, tss.size() - 1, tss.size() - 1 );
			if ( tss.size() == 1 && selectedObject == null && anObject != null )
				setSelectedItem( anObject );
		}

		@Override
		public void insertElementAt( final TrackSchemeStyle anObject, final int index )
		{
			if ( tss.contains( anObject ) )
				return;
			tss.insertElementAt( anObject, index );
			fireIntervalAdded( this, index, index );
		}

		@Override
		public void removeElementAt( final int index )
		{
			if ( getElementAt( index ) == selectedObject )
			{
				if ( index == 0 )
					setSelectedItem( getSize() == 1 ? null : getElementAt( index + 1 ) );
				else
					setSelectedItem( getElementAt( index - 1 ) );
			}

			tss.removeElementAt( index );
			fireIntervalRemoved( this, index, index );
		}

		@Override
		public void removeElement( final Object anObject )
		{
			final int index = tss.indexOf( anObject );
			if ( index != -1 )
				removeElementAt( index );
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