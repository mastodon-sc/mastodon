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
package org.mastodon.ui.keymap;

import java.util.Objects;

import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.ui.settings.style.Style;

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
