package org.mastodon.revised.ui.keymap;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.Command;

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

	private String defaultContext;

	public void add( final String name, final String[] defaultTriggers, final String description )
	{
		add( name, defaultContext, defaultTriggers, description );
	}

	public void add( final String name, final String context, final String[] defaultTriggers, final String description )
	{
		final Command c = new Command( name, context );
		final DescriptionAndTriggers cd = new DescriptionAndTriggers( description, defaultTriggers );
		descriptions.put( c, cd );
	}

	public void setDefaultContext( final String defaultContext )
	{
		this.defaultContext = defaultContext;
	}

	public Map< Command, String > createCommandDescriptionsMap()
	{
		final Map< Command, String > map = new LinkedHashMap<>();
		descriptions.forEach( ( c, d ) -> map.put( c, d.description ) );
		return map;
	}

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
}
