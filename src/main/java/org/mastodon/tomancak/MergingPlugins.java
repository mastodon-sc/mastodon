package org.mastodon.tomancak;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.scijava.AbstractContextual;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

@Plugin( type = MergingPlugins.class )
public class MergingPlugins extends AbstractContextual implements MastodonPlugin
{
	private static final String MERGE_PROJECTS = "[tomancak] merge projects";

	private static final String[] MERGE_PROJECTS_KEYS = { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( MERGE_PROJECTS, "Merge Projects..." );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( MERGE_PROJECTS, MERGE_PROJECTS_KEYS, "Merge two Mastodon projects." );
		}
	}

	private final AbstractNamedAction mergeProjectsAction;

	private MastodonPluginAppModel pluginAppModel;

	public MergingPlugins()
	{
		mergeProjectsAction = new RunnableAction( MERGE_PROJECTS, this::mergeProjects );
		updateEnabledActions();
	}

	@Override
	public void setAppModel( final MastodonPluginAppModel model )
	{
		this.pluginAppModel = model;
		updateEnabledActions();
	}

	@Override
	public List< ViewMenuBuilder.MenuItem > getMenuItems()
	{
		return Arrays.asList(
				menu( "Plugins",
						menu( "Merging",
								item( MERGE_PROJECTS ) ) ) );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( mergeProjectsAction, MERGE_PROJECTS_KEYS );
	}

	private void updateEnabledActions()
	{
		final MamutAppModel appModel = ( pluginAppModel == null ) ? null : pluginAppModel.getAppModel();
		mergeProjectsAction.setEnabled( true );
	}

	private void mergeProjects()
	{

	}
}
