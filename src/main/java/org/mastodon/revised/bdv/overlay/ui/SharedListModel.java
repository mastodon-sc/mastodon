/**
 *
 */
package org.mastodon.revised.bdv.overlay.ui;

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

/**
 * A mutable combo-box model where the list of objects can be shared amongst
 * several model instances.
 * <p>
 * We rely on a factory to create the several instances sharing a common list,
 * to avoid exposing the list of objects. The models created by a single factory
 * all share the same list of objects.
 *
 * @author Jean=Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 *
 */
public class SharedListModel< E > extends AbstractListModel< E > implements MutableComboBoxModel< E >
{
	private static final long serialVersionUID = 1L;

	private final Vector< E > objects;

	private Object selectedObject;

	private SharedListModel( Vector< E > objects )
	{
		this.objects = objects;
	}

	/**
	 * Set the value of the selected item. The selected item may be null.
	 *
	 * @param anObject
	 *            The combo box value or null for no selection.
	 */
	@Override
	public void setSelectedItem( Object anObject )
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
		return objects.size();
	}

	@Override
	public E getElementAt( int index )
	{
		if ( index >= 0 && index < objects.size() )
			return objects.elementAt( index );
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 *
	 * @param anObject
	 * @return an int representing the index position, where 0 is the first
	 *         position
	 */
	public int getIndexOf( Object anObject )
	{
		return objects.indexOf( anObject );
	}

	@Override
	public void addElement( E anObject )
	{
		if ( objects.contains( anObject ) )
			return;
		objects.addElement( anObject );
		fireIntervalAdded( this, objects.size() - 1, objects.size() - 1 );
		if ( objects.size() == 1 && selectedObject == null && anObject != null )
		{
			setSelectedItem( anObject );
		}
	}

	@Override
	public void insertElementAt( E anObject, int index )
	{
		if ( objects.contains( anObject ) )
			return;
		objects.insertElementAt( anObject, index );
		fireIntervalAdded( this, index, index );
	}

	@Override
	public void removeElementAt( int index )
	{
		if ( getElementAt( index ) == selectedObject )
		{
			if ( index == 0 )
			{
				setSelectedItem( getSize() == 1 ? null : getElementAt( index + 1 ) );
			}
			else
			{
				setSelectedItem( getElementAt( index - 1 ) );
			}
		}

		objects.removeElementAt( index );

		fireIntervalRemoved( this, index, index );
	}

	@Override
	public void removeElement( Object anObject )
	{
		final int index = objects.indexOf( anObject );
		if ( index != -1 )
		{
			removeElementAt( index );
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if ( objects.size() > 0 )
		{
			final int firstIndex = 0;
			final int lastIndex = objects.size() - 1;
			objects.removeAllElements();
			selectedObject = null;
			fireIntervalRemoved( this, firstIndex, lastIndex );
		}
		else
		{
			selectedObject = null;
		}
	}

	public static class Factory< F >
	{
		private final Vector< F > list;

		private final ArrayList< SharedListModel< F > > models;

		public Factory()
		{
			this.list = new Vector<>();
			this.models = new ArrayList<>();
		}

		public SharedListModel< F > create()
		{
			final SharedListModel< F > slm = new SharedListModel<>( list );
			models.add( slm );
			return slm;
		}
	}
}
