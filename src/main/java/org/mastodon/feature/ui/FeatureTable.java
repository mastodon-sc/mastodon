package org.mastodon.feature.ui;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.mastodon.util.Listeners;

/**
 *
 * @param <C> collection-of-elements type
 * @param <T> element type
 */
public class FeatureTable< C, T >
{
	private C elements;
	private final ToIntFunction< C > size;
	private final BiFunction< C, Integer, T > get;
	private final Function< T, String > getName;
	private final Predicate< T > isSelected;
	private final BiConsumer< T, Boolean > setSelected;
	private final Predicate< T > isUptodate;

	private final Listeners.List< SelectionListener< T > > selectionListeners;

	public FeatureTable(
			final C elements,                           // collection of elements
			final ToIntFunction< C > size,              // given collection returns number of elements
			final BiFunction< C, Integer, T > get,      // given collection and index returns element at index
			final Function< T, String > getName,        // given element returns name
			final Predicate< T > isSelected,            // given element returns whether it is selected
			final BiConsumer< T, Boolean > setSelected, // given element and boolean sets selection of element
			final Predicate< T > isUptodate )           // given element returns whether it is up-to-date
	{
		this.elements = elements;
		this.size = size;
		this.get = get;
		this.getName = getName;
		this.isSelected = isSelected;
		this.setSelected = setSelected;
		this.isUptodate = isUptodate;

		selectionListeners = new Listeners.SynchronizedList<>();
	}

	// get the JTable
	// for now maybe wrapped in ScrollPane
	public JComponent getTable()
	{
		// TODO
		return new JLabel( "TODO" );
	}

	// set collection of elements to show
	public void setElements( final C elements )
	{
		this.elements = elements;
		// TODO update table, ... ?
	}

	public interface SelectionListener< T >
	{
		void selectionChanged( T selected );
	}

	public Listeners< SelectionListener< T > > selectionListeners()
	{
		return selectionListeners;
	}
}
