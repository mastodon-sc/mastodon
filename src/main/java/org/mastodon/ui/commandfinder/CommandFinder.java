/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.ui.commandfinder;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.mastodon.app.ui.CloseWindowActions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.Command;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionsBuilder;
import org.scijava.ui.behaviour.util.Actions;

import bdv.tools.ToggleDialogAction;
import bdv.ui.keymap.Keymap.UpdateListener;

public class CommandFinder
{

	public static final String SHOW_COMMAND_FINDER = "show command finder";

	private static final String[] SHOW_COMMAND_FINDER_KEYS = new String[] { "ctrl shift F", "meta shift F" };

	private final JDialog dialog;

	private final List< Actions > acs;

	private final CommandFinderPanel gui;

	public static Builder build()
	{
		return new Builder();
	}

	private CommandFinder( 
			final List< Actions > acs, 
			final InputTriggerConfig config, 
			final Map< Command, String > commandMap, 
			final String[] keyConfigContexts, 
			final JFrame parent )
	{
		this.acs = acs;
		this.dialog = new JDialog( parent, "Command finder" );

		final Consumer< String > runner = ( actionName ) -> run( actionName );
		this.gui = new CommandFinderPanel( runner, commandMap, config );

		dialog.getContentPane().add( gui );
		dialog.pack();
		dialog.setLocationByPlatform( true );
		dialog.setLocationRelativeTo( null );
		// Give focus to text after being made visible.
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

	public CommandFinderPanel getGui()
	{
		return gui;
	}

	public JDialog getDialog()
	{
		return dialog;
	}

	/**
	 * Runs the 'first' action of behavior we can find with the specified name.
	 * 
	 * @param actionName
	 *            the action or behavior name.
	 */
	private void run( final String actionName )
	{
		for ( final Actions ac : acs )
		{
			final Action action = ac.getActionMap().get( actionName );
			if ( action != null )
			{
				final ActionEvent event = new ActionEvent( dialog, 0, actionName );
				action.actionPerformed( event );
				return;
			}
		}
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
			descriptions.add( SHOW_COMMAND_FINDER, SHOW_COMMAND_FINDER_KEYS, "Show the command finder dialog." );
		}
	}

	public static class Builder
	{

		private final List< Actions > acs = new ArrayList<>();

		private final List< CommandDescriptionProvider > descriptionProviders = new ArrayList<>();

		private JFrame parent;

		private final Set< String > keyConfigContexts = new HashSet<>();

		private InputTriggerConfig config;

		private Context context;

		private Listeners< UpdateListener > updateListeners;


		public Builder context( final Context context )
		{
			this.context = context;
			return this;
		}

		public Builder inputTriggerConfig( final InputTriggerConfig config )
		{
			this.config = config;
			return this;
		}

		public Builder register( final Actions actions )
		{
			this.acs.add( actions );
			return this;
		}

		public Builder parent( final JFrame parent )
		{
			this.parent = parent;
			return this;
		}

		public Builder keyConfigContext( final String kcc )
		{
			this.keyConfigContexts.add( kcc );
			return this;
		}

		public Builder keyConfigContexts( final String[] keyConfigContexts )
		{
			for ( final String kcc : keyConfigContexts )
				this.keyConfigContexts.add( kcc );
			return this;
		}

		public CommandFinder get()
		{
			if ( config == null )
				throw new IllegalArgumentException( "The InputTriggerConfig cannot be null." );
			if ( context == null )
				throw new IllegalArgumentException( "The Context cannot be null." );

			final Map< Command, String > commandDescriptions = buildCommandDescriptions();
			final CommandFinder cf = new CommandFinder(
					acs,
					config,
					commandDescriptions,
					keyConfigContexts.toArray( new String[] {} ),
					parent );
			// Refresh the command list it it changes.
			if ( updateListeners != null )
			{
				updateListeners.add( () -> {
					final Map< Command, String > cds = buildCommandDescriptions();
					cf.getGui().setCommandDescriptions( cds );
				} );
			}
			return cf;
		}

		public Builder modificationListeners( final Listeners< UpdateListener > updateListeners )
		{
			this.updateListeners = updateListeners;
			return this;
		}

		public Builder descriptionProvider( final CommandDescriptionProvider descriptionsProvider )
		{
			this.descriptionProviders.add( descriptionsProvider );
			return this;
		}

		public CommandFinder installOn( final Actions installOn )
		{
			final CommandFinder cf = get();
			installOn.namedAction( new ToggleDialogAction( SHOW_COMMAND_FINDER, cf.dialog ), SHOW_COMMAND_FINDER_KEYS );
			register( installOn );
			cf.getGui().setCommandDescriptions( buildCommandDescriptions() );
			return cf;
		}

		/**
		 * Discovers and build the command vs description map.
		 * <p>
		 * Important: in this implementation of a command finder, derived from
		 * the keymap editor, that commands are listed and filtered from the
		 * command descriptions. Which means that only the commands that have a
		 * description, provided with a CommandDescriptionProvider, will appear
		 * in the command list.
		 * 
		 * @param actionMap
		 *            the command descriptions will be filtered to only include
		 *            the commands present in the specified action map.
		 * @param keyConfigContexts
		 *            the command descriptions will be filtered to only include
		 *            those with the context in the specified array.
		 * @return the command descriptions map as a new map.
		 */
		private Map< Command, String > buildCommandDescriptions()
		{
			// Copy and sort key contexts.
			final String[] contexts = Arrays.copyOf( keyConfigContexts.toArray( new String[] {} ), keyConfigContexts.size() );
			Arrays.sort( contexts );

			final CommandDescriptionsBuilder builder = new CommandDescriptionsBuilder();
			context.inject( builder );
			builder.discoverProviders();
			for ( final CommandDescriptionProvider cdp : descriptionProviders )
				builder.addManually( cdp, contexts );
			final CommandDescriptions cd = builder.build();
			final Map< Command, String > map = cd.createCommandDescriptionsMap();
			

			// Create new command map, filtered.
			final Map< Command, String > filteredMap = new HashMap<>();

			/*
			 * Build array of keys (command name) that are in one of the actions
			 * or behaviors we were build with.
			 */

			// Loop over actions.
			final Set< String > allKeySet = new HashSet<>();
			for ( final Actions ac : acs )
			{
				// Build list of commands in the action map.
				final ActionMap actionMap = ac.getActionMap();
				final Object[] objs = actionMap.allKeys();
				if ( objs != null )
					for ( int i = 0; i < objs.length; i++ )
						allKeySet.add( ( String ) objs[ i ] );
			}
			final String[] allKeys = allKeySet.toArray( new String[] {} );
			Arrays.sort( allKeys );

			/*
			 * Loop over all command descriptions, and retain those 1/ which
			 * have a context that we manage 2/ which are in one of the action
			 * map of behavior map that was provided to us.
			 */
			for ( final Command command : map.keySet() )
			{
				if ( Arrays.binarySearch( contexts, command.getContext() ) < 0 )
					continue; // not in our contexts.

				if ( Arrays.binarySearch( allKeys, command.getName() ) < 0 )
					continue; // not a command we know.

				// Add it.
				filteredMap.put( command, map.get( command ) );
			}
			return filteredMap;
		}
	}
}
