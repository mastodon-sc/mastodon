package org.mastodon.revised.ui.keymap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.ui.behaviour.io.gui.Command;

public final class CommandDescriptions
{
	public static final class CommandDescription
	{
		private final Command command;

		private final String description;

		private final String[] defaultTriggers;

		public CommandDescription( final Command command, final String description, final String[] defaultTriggers )
		{
			this.command = command;
			this.description = description;
			this.defaultTriggers = defaultTriggers;
		}
	}

	private final Map< Command, CommandDescription > descriptions = new LinkedHashMap<>();

	private String defaultContext;

	public void add( final String name, final String[] defaultTriggers, final String description )
	{
		add( name, defaultContext, defaultTriggers, description );
	}
	public void add( final String name, final String context, final String[] defaultTriggers, final String description )
	{
		final Command c = new Command( name, context );
		final CommandDescription cd = new CommandDescription( c, description, defaultTriggers );
		descriptions.put( c, cd );
	}

	public void setDefaultContext( final String defaultContext )
	{
		this.defaultContext = defaultContext;
	}

	public Map< Command, String > createCommandDescriptionsMap()
	{
		final Map< Command, String > map = new LinkedHashMap<>();
		descriptions.forEach( (c, d) -> map.put( c, d.description ) );
		return map;
	}
}
