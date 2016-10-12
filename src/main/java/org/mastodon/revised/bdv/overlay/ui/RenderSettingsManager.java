package org.mastodon.revised.bdv.overlay.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.yaml.snakeyaml.Yaml;


/**
 * Manages a list of {@link RenderSettings} for multiple BDV windows.
 * Provides models based on a common list of settings than can be used in swing items.
 * 
 * @author Jean-Yves Tinevez.
 *
 */
public class RenderSettingsManager
{
	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/rendersettings.yaml";

	private final Vector< RenderSettings > rs;

	public RenderSettingsManager()
	{
		this.rs = new Vector<>();
		for ( final RenderSettings r : RenderSettings.defaults )
			rs.add( r );

		loadStyles();
	}

	public MutableComboBoxModel< RenderSettings > createComboBoxModel()
	{
		return new MyListModel();
	}

	private void loadStyles()
	{
		try
		{
			final FileReader input = new FileReader( STYLE_FILE );
			final Yaml yaml = RenderSettingsIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			for ( final Object obj : objs )
				rs.add( ( RenderSettings ) obj );

		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "BDV render settings file " + STYLE_FILE + " not found. Using builtin styles." );
		}
	}

	public void saveStyles()
	{
		try
		{
			final List< RenderSettings > stylesToSave = new ArrayList<>();
			for ( int i = 0; i < rs.size(); i++ )
			{
				final RenderSettings style = rs.get( i );
				if ( RenderSettings.defaults.contains( style ) )
					continue;
				stylesToSave.add( style );
			}

			mkdirs( STYLE_FILE );
			final FileWriter output = new FileWriter( STYLE_FILE );
			final Yaml yaml = RenderSettingsIO.createYaml();
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

	private final class MyListModel extends AbstractListModel< RenderSettings > implements MutableComboBoxModel< RenderSettings >
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
			return rs.size();
		}

		@Override
		public RenderSettings getElementAt( final int index )
		{
			if ( index >= 0 && index < rs.size() )
				return rs.elementAt( index );
			else
				return null;
		}

		@Override
		public void addElement( final RenderSettings anObject )
		{
			if ( rs.contains( anObject ) )
				return;
			rs.addElement( anObject );
			fireIntervalAdded( this, rs.size() - 1, rs.size() - 1 );
			if ( rs.size() == 1 && selectedObject == null && anObject != null )
				setSelectedItem( anObject );
		}

		@Override
		public void insertElementAt( final RenderSettings anObject, final int index )
		{
			if ( rs.contains( anObject ) )
				return;
			rs.insertElementAt( anObject, index );
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

			rs.removeElementAt( index );
			fireIntervalRemoved( this, index, index );
		}

		@Override
		public void removeElement( final Object anObject )
		{
			final int index = rs.indexOf( anObject );
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
