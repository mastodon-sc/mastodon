package org.mastodon.revised.ui.keymap;

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
	 * Add a new entry, linking a {@code Command} to textual description and
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
	 * Set the current context. This context name is then used for subsequently
	 * {@link #add(String, String[], String) added} commands.
	 */
	public void setKeyconfigContext( final String context )
	{
		this.context = context;
	}

	/**
	 * Build a map from {@link Command} to textual description. This is for
	 * making a keyconfig {@link VisualEditorPanel}.
	 */
	public Map< Command, String > createCommandDescriptionsMap()
	{
		final Map< Command, String > map = new LinkedHashMap<>();
		descriptions.forEach( ( c, d ) -> map.put( c, d.description ) );
		return map;
	}

	/**
	 * Build a {@link InputTriggerConfig} with all commands and their default
	 * triggers. Commands that have no specified default triggers will have
	 * trigger {@code "not mapped"}.
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
