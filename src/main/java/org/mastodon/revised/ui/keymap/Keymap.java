package org.mastodon.revised.ui.keymap;

import org.mastodon.app.ui.settings.style.Style;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

public class Keymap implements Style< Keymap >
{
	/**
	 * The name of this keymap.
	 */
	private String name;

	private final InputTriggerConfig config;

	public Keymap( final String name, final InputTriggerConfig config )
	{
		this.name = name;
		this.config = config;
	}

	public Keymap()
	{
		this.name = "";
		this.config = new InputTriggerConfig();
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
//		notifyListeners(); // TODO?
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
		if ( this.name != name )
		{
			this.name = name;
//			notifyListeners(); // TODO?
		}
	}
}
