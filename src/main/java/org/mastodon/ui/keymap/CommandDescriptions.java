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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.Command;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel;

/**
 * A map linking {@link Command} (action name and context name) to its
 * {@link DescriptionAndTriggers} (textual description and default triggers of
 * the command).
 * <p>
 * New entries are added using {@link #add(String, String[], String)}. This
 * specifies only the action name of the {@code Command}. The context of the
 * {@code Command} is the <em>current context of this
 * {@code CommandDescriptions}</em>. The current context is changed by
 * {@link #setKeyconfigContext(String)}, and then used for subsequently added
 * entries.
 */
public final class CommandDescriptions
{
	public static final class DescriptionAndTriggers
	{
		private final String description;

		private final String[] defaultTriggers;

		public DescriptionAndTriggers( final String description, final String[] defaultTriggers )
		{
			this.description = description;
			this.defaultTriggers = defaultTriggers;
		}
	}

	private final Map< Command, DescriptionAndTriggers > descriptions = new LinkedHashMap<>();

	private String context;

	/**
	 * Adds a new entry, linking a {@code Command} to textual description and
	 * default triggers.
	 *
	 * @param name
	 *            name of the {@code Action} or {@code Behaviour}. Together with
	 *            the current context name (see
	 *            {@link #setKeyconfigContext(String)}) this specifies a
	 *            {@link Command}.
	 * @param defaultTriggers
	 *            default {@link InputTrigger}s for the command.
	 * @param description
	 *            textual description of the command (for displaying in UI).
	 */
	public void add( final String name, final String[] defaultTriggers, final String description )
	{
		final Command c = new Command( name, context );
		final DescriptionAndTriggers cd = new DescriptionAndTriggers( description, defaultTriggers );
		descriptions.put( c, cd );
	}

	/**
	 * Sets the current context. This context name is then used for subsequently
	 * {@link #add(String, String[], String) added} commands.
	 *
	 * @param context
	 *            the context name.
	 */
	public void setKeyconfigContext( final String context )
	{
		this.context = context;
	}

	/**
	 * Builds a map from {@link Command} to textual description. This is for
	 * making a keyconfig {@link VisualEditorPanel}.
	 *
	 * @return a new map.
	 */
	public Map< Command, String > createCommandDescriptionsMap()
	{
		final Map< Command, String > map = new LinkedHashMap<>();
		descriptions.forEach( ( c, d ) -> map.put( c, d.description ) );
		return map;
	}

	/**
	 * Builds a {@link InputTriggerConfig} with all commands and their default
	 * triggers. Commands that have no specified default triggers will have
	 * trigger {@code "not mapped"}.
	 *
	 * @return a new {@link InputTriggerConfig}.
	 */
	public InputTriggerConfig createDefaultKeyconfig()
	{
		final InputTriggerConfig config = new InputTriggerConfig();
		descriptions.forEach( ( c, d ) -> {
			final String name = c.getName();
			final String context = c.getContext();
			final String[] triggers = d.defaultTriggers;
			if ( triggers == null || triggers.length == 0 )
				config.add( "not mapped", name, context );
			else
				Arrays.stream( triggers ).forEachOrdered( t -> config.add( t, name, context ) );
		} );
		return config;
	}

	/**
	 * For commands that are not yet defined in {@code config}, add them with
	 * their default triggers. Commands that have no specified default triggers
	 * will have trigger {@code "not mapped"}.
	 *
	 * @param config
	 *            the input trigger config to add commands to.
	 */
	public void augmentInputTriggerConfig( final InputTriggerConfig config )
	{
		descriptions.forEach( ( c, d ) -> {
			final String name = c.getName();
			final String context = c.getContext();
			if ( config.getInputs( name, context ).isEmpty() )
			{
				final String[] triggers = d.defaultTriggers;
				if ( triggers == null || triggers.length == 0 )
					config.add( "not mapped", name, context );
				else
					Arrays.stream( triggers ).forEachOrdered( t -> config.add( t, name, context ) );
			}
		} );
	}
}
