package org.mastodon.ui.commandfinder;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.app.ui.CloseWindowActions;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.Command;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.ToggleDialogAction;

public class CommandFinder
{

	public static final String SHOW_COMMAND_FINDER = "show command finder";

	private static final String[] SHOW_COMMAND_FINDER_KEYS = new String[] { "ctrl shift F" };

	private final ProjectModel appModel;

	private final JDialog dialog;

	/**
	 * Create an action that toggles the visibility of the command finder and
	 * register it in the specified actions.
	 *
	 * @param actions
	 *            Actions are added here.
	 * @param keyConfigContexts
	 */
	public static void install( final Actions actions, final ProjectModel appModel, final JFrame parent, final String[] keyConfigContexts )
	{
		final CommandFinder cf = new CommandFinder( actions, appModel, parent, keyConfigContexts );
		actions.namedAction( new ToggleDialogAction( SHOW_COMMAND_FINDER, cf.dialog ), SHOW_COMMAND_FINDER_KEYS );
	}

	public CommandFinder( final Actions actions, final ProjectModel appModel, final JFrame parent, final String[] keyConfigContexts )
	{
		this.appModel = appModel;
		this.dialog = new JDialog( parent, "Command finder" );

		final Map< Command, String > commandMap = buildCommandDescriptions( actions.getActionMap(), keyConfigContexts );
		final CommandFinderPanel gui = new CommandFinderPanel( appModel.getKeymap().getConfig(), commandMap, actions );

		dialog.getContentPane().add( gui );
		dialog.pack();
		dialog.setLocationByPlatform( true );
		dialog.setLocationRelativeTo( null );
		dialog.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				gui.textFieldFilter.requestFocusInWindow();
			}
		} );
		// Close with escape.
		final ActionMap am = dialog.getRootPane().getActionMap();
		final InputMap im = dialog.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Actions actionsDialog = new Actions( im, am, null, keyConfigContexts );
		CloseWindowActions.install( actionsDialog, dialog );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SHOW_COMMAND_FINDER, SHOW_COMMAND_FINDER_KEYS, "Shows the command finder dialog." );
		}
	}

	/**
	 * Discovers and build the command description map.
	 * 
	 * @param actionMap
	 *            if not <code>null</code>, the command descriptions will be
	 *            filtered to only include the commands present in the specified
	 *            action map.
	 * @param keyConfigContexts
	 * @return the command descriptions map.
	 */
	private Map< Command, String > buildCommandDescriptions( final ActionMap actionMap, final String[] keyConfigContexts )
	{
		final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
		appModel.getContext().inject( builder );
		builder.discoverProviders();
		final CommandDescriptions cd = builder.build();
		final Map< Command, String > map = cd.createCommandDescriptionsMap();
		if ( actionMap == null )
			return map;

		// Build list of commands in the action map.
		final Object[] objs = actionMap.allKeys();
		final String[] allKeys = new String[ objs.length ];
		for ( int i = 0; i < objs.length; i++ )
			allKeys[ i ] = ( String ) objs[ i ];
		Arrays.sort( allKeys );

		// Copy and sort key contexts.
		final String[] contexts = Arrays.copyOf( keyConfigContexts, keyConfigContexts.length );
		Arrays.sort( contexts );

		// Create new command map filtered by the command.
		final Map< Command, String > filteredMap = new HashMap<>( allKeys.length );
		for ( final Command command : map.keySet() )
		{

			if ( Arrays.binarySearch( contexts, command.getContext() ) < 0 )
				continue;

			if ( Arrays.binarySearch( allKeys, command.getName() ) < 0 )
				continue;

			filteredMap.put( command, map.get( command ) );
		}
		return filteredMap;
	}
}
