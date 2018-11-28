package org.mastodon.plugin;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.mamut.MamutMenuBuilder;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

//@Plugin( type = MastodonTestPlugin.class )
public class MastodonTestPlugin implements MastodonPlugin
{
	private static final String ACTION_1 = "[testplugin] action1";

	private static final String[] ACTION_1_KEYS = new String[] { "meta K" };

	/*
	 * Command descriptions for all provided commands
	 */
//	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( ACTION_1, ACTION_1_KEYS, "Test Plugin Action" );
		}
	}

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( ACTION_1, "Test Action" );
	}

	private final AbstractNamedAction action1 = new AbstractNamedAction( ACTION_1 )
	{

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			System.out.println( "MastodonTestPlugin.action1.actionPerformed" );
		}
	};

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				MamutMenuBuilder.menu( "Plugins",
						MamutMenuBuilder.item( ACTION_1 ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( action1, ACTION_1_KEYS );
	}

	@Override
	public void setAppModel( final MastodonPluginAppModel appModel )
	{}
}
