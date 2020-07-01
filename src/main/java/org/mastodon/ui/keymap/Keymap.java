package org.mastodon.ui.keymap;

import java.util.Objects;

import org.mastodon.app.ui.settings.style.Style;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

public class Keymap implements Style< Keymap >
{
	/**
	 * The name of this keymap.
	 */
	private String name;

	private final InputTriggerConfig config;

	public interface UpdateListener
	{
		public void keymapChanged();
	}

	private final Listeners.List< UpdateListener > updateListeners;

	public Keymap( final String name, final InputTriggerConfig config )
	{
		this.name = name;
		this.config = config;
		this.updateListeners = new Listeners.SynchronizedList<>();
	}

	public Keymap()
	{
		this( "", new InputTriggerConfig() );
	}

	/**
	 * Returns a new style instance, copied from this style.
	 *
	 * @param name
	 *            the name for the copied style.
	 * @return a new style instance.
	 */
	@Override
	public Keymap copy( final String name )
	{
		final Keymap newStyle = new Keymap();
		newStyle.set( this );
		if ( name != null )
			newStyle.setName( name );
		return newStyle;
	}

	@Override
	public Keymap copy()
	{
		return copy( null );
	}

	public synchronized void set( final Keymap style )
	{
		this.name = style.name;
		this.config.set( style.config );
		notifyListeners();
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.keymapChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	public InputTriggerConfig getConfig()
	{
		return config;
	}

	/**
	 * Returns the name of this {@link Keymap}.
	 *
	 * @return the name.
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of this {@link Keymap}.
	 *
	 * @param name
	 *            the name to set.
	 */
	@Override
	public synchronized void setName( final String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
//			notifyListeners(); // TODO?
		}
	}
}
