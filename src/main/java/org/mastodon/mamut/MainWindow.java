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
package org.mastodon.mamut;

import static org.mastodon.app.MastodonIcons.BDV_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.FEATURES_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.MAINWINDOW_BG;
import static org.mastodon.app.MastodonIcons.MASTODON_ICON;
import static org.mastodon.app.MastodonIcons.SAVE_AS_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.TABLE_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.TAGS_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.TRACKSCHEME_ICON_MEDIUM;
import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.mastodon.app.MastodonIcons;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.mamut.io.ProjectActions;
import org.mastodon.mamut.views.bdv.MamutBranchViewBdvFactory;
import org.mastodon.mamut.views.bdv.MamutViewBdvFactory;
import org.mastodon.mamut.views.grapher.MamutViewGrapherFactory;
import org.mastodon.mamut.views.table.MamutViewSelectionTableFactory;
import org.mastodon.mamut.views.table.MamutViewTableFactory;
import org.mastodon.mamut.views.trackscheme.MamutBranchViewTrackSchemeFactory;
import org.mastodon.mamut.views.trackscheme.MamutViewTrackSchemeFactory;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.util.RunnableActionPair;

import bdv.ui.keymap.Keymap;
import net.miginfocom.swing.MigLayout;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected final JMenuBar menubar;

	private final ViewMenu menu;

	private final ProjectModel appModel;

	public MainWindow( final ProjectModel appModel )
	{
		super( makeName( appModel ) );
		this.appModel = appModel;
		setIconImages( MASTODON_ICON );
		setLocationByPlatform( true );
		setLocationRelativeTo( null );

		// Re-register save actions, this time using this frame as parent component.
		ProjectActions.installAppActions( appModel.getProjectActions(), appModel, this );

		// Views:
		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new MigLayout() );
		final ActionMap projectActionMap = appModel.getProjectActions().getActionMap();

		final JLabel viewsLabel = new JLabel( "Views:" );
		viewsLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( viewsLabel, "span, wrap" );

		final JButton tableButton = new JButton( projectActionMap.get( MamutViewTableFactory.NEW_TABLE_VIEW ) );
		prepareButton( tableButton, "table", TABLE_ICON_MEDIUM );
		buttonsPanel.add( tableButton, "grow" );

		final JButton bdvButton = new JButton( new RunnableActionPair( MamutViewBdvFactory.NEW_BDV_VIEW, 
				() -> projectActionMap.get( MamutViewBdvFactory.NEW_BDV_VIEW ).actionPerformed( null ),
				() -> projectActionMap.get( MamutBranchViewBdvFactory.NEW_BRANCH_BDV_VIEW ).actionPerformed( null ) ) );
		prepareButton( bdvButton, "bdv", BDV_ICON_MEDIUM );
		buttonsPanel.add( bdvButton, "grow, wrap" );

		final JButton selectionTableButton = new JButton( projectActionMap.get( MamutViewSelectionTableFactory.NEW_SELECTION_TABLE_VIEW ) );
		prepareButton( selectionTableButton, "selection table", TABLE_ICON_MEDIUM );
		buttonsPanel.add( selectionTableButton, "grow" );

		final JButton trackschemeButton = new JButton( new RunnableActionPair( MamutViewTrackSchemeFactory.NEW_TRACKSCHEME_VIEW, 
						() -> projectActionMap.get( MamutViewTrackSchemeFactory.NEW_TRACKSCHEME_VIEW ).actionPerformed( null ),
						() -> projectActionMap.get( MamutBranchViewTrackSchemeFactory.NEW_BRANCH_TRACKSCHEME_VIEW ).actionPerformed( null ) ) );
		prepareButton( trackschemeButton, "trackscheme", TRACKSCHEME_ICON_MEDIUM );
		buttonsPanel.add( trackschemeButton, "grow, wrap" );

		buttonsPanel.add( new JSeparator(), "span, grow, wrap" );

		// Processing:
		final JLabel processingLabel = new JLabel( "Processing:" );
		processingLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( processingLabel, "span, wrap" );

		final JButton grapherButton = new JButton( projectActionMap.get( MamutViewGrapherFactory.NEW_GRAPHER_VIEW ) );
		prepareButton( grapherButton, "grapher", FEATURES_ICON_MEDIUM );
		buttonsPanel.add( grapherButton, "grow" );

		final JButton featureComputationButton = new JButton( projectActionMap.get( WindowManager.COMPUTE_FEATURE_DIALOG ) );
		prepareButton( featureComputationButton, "compute features", FEATURES_ICON_MEDIUM );
		buttonsPanel.add( featureComputationButton, "grow, wrap" );

		final JButton editTagSetsButton = new JButton( projectActionMap.get( WindowManager.TAGSETS_DIALOG ) );
		prepareButton( editTagSetsButton, "configure tags", TAGS_ICON_MEDIUM );
		buttonsPanel.add( editTagSetsButton, "grow, wrap" );

		buttonsPanel.add( new JSeparator(), "span, grow, wrap" );

		// Saving:
		final JLabel ioLabel = new JLabel( "Saving:" );
		ioLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( ioLabel, "span, wrap" );

		final JButton saveProjectButton = new JButton( saveAction( projectActionMap.get( ProjectActions.SAVE_PROJECT ) ) );
		prepareButton( saveProjectButton, "save", SAVE_ICON_MEDIUM );
		buttonsPanel.add( saveProjectButton, "grow" );

		final JButton saveProjectAsButton = new JButton( saveAction( projectActionMap.get( ProjectActions.SAVE_PROJECT_AS ) ) );
		prepareButton( saveProjectAsButton, "save as...", SAVE_AS_ICON_MEDIUM );
		buttonsPanel.add( saveProjectAsButton, "grow, wrap" );

		/*
		 * Background with an image.
		 */

		final JComponent content = new JPanel()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent( final Graphics g )
			{
				super.paintComponent( g );
				g.drawImage( MAINWINDOW_BG, 0, 0, this );
			}
		};
		setContentPane( content );

		buttonsPanel.setOpaque( false );
		content.add( buttonsPanel, BorderLayout.NORTH );

		menubar = new JMenuBar();
		setJMenuBar( menubar );

		final Keymap keymap = appModel.getKeymapManager().getForwardSelectedKeymap();
		menu = new ViewMenu( menubar, keymap, KeyConfigContexts.MASTODON );
		keymap.updateListeners().add( menu::updateKeymap );
		addMenus( menu, projectActionMap );
		appModel.getWindowManager().addWindowMenu( menu, projectActionMap );
		appModel.getPlugins().addMenus( menu );

		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );

		pack();
		setResizable( false );

		// Register to when the project model is closed.
		appModel.projectClosedListeners().add( () -> dispose() );
	}

	/**
	 * Adds a hook to a save action so that we update the title with the new
	 * project name after saving.
	 * 
	 * @param action
	 *            the save action.
	 * @return a wrapping action.
	 */
	private Action saveAction( final Action action )
	{
		return new AbstractAction()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				new Thread( () -> {
					action.actionPerformed( e );
					updateWindowNames();
				} ).start();
			}
		};
	}

	private void updateWindowNames()
	{
		appModel.getWindowManager().forEachWindow( w -> WindowManager.adjustTitle( w, appModel.getProjectName() ) );
		setTitle( makeName( appModel ) );
	}

	private static final String makeName( final ProjectModel pm )
	{
		final String extra = pm.getProjectName();
		if ( extra == null || extra.isEmpty() )
			return "Mastodon";
		return "Mastodon - " + extra;
	}

	/**
	 * Closes all the windows opened in Mastodon, this main window. If the model
	 * has been modified and not saved, prompts the user for confirmation.
	 * Returns <code>true</code> if Mastodon has been closed, or
	 * <code>false</code> if the user canceled closing.
	 * 
	 * @return <code>true</code> if the Mastodon instance has been closed.
	 */
	public boolean close()
	{
		final Action saveAction = appModel.getModelActions().getActionMap().get( ProjectActions.SAVE_PROJECT );
		if ( appModel.getModel().isSavePoint() )
		{
			appModel.close();
			return true;
		}

		// Custom button text
		final Object[] options = {
				"Save project",
				"Don't save",
				"Cancel" };
		final int choice = JOptionPane.showOptionDialog( this,
				"Data changed since last save. \n"
						+ "Do you want to save the project before closing mastodon?",
				"Save before close?",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				MastodonIcons.MASTODON_ICON_MEDIUM,
				options,
				JOptionPane.CANCEL_OPTION );

		switch ( choice )
		{
		case JOptionPane.CLOSED_OPTION:
		case JOptionPane.CANCEL_OPTION:
		default:
			return false;

		case JOptionPane.YES_OPTION:
			saveAction.actionPerformed( null );
			// Fall trough to closing.

		case JOptionPane.NO_OPTION:
			appModel.close();
		}
		return true;
	}

	private static void prepareButton( final JButton button, final String txt, final ImageIcon icon )
	{
		final JLabel iconLabel = new JLabel( icon );
		final JLabel clickMe = new JLabel( txt, SwingConstants.CENTER );
		button.setText( "" );
		button.setLayout( new BorderLayout() );
		button.add( iconLabel, BorderLayout.WEST );
		button.add( clickMe, BorderLayout.CENTER );
	}

	public static void addMenus( final ViewMenu menu, final ActionMap actionMap )
	{
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						// item( ProjectActions.CREATE_PROJECT ),
						// item( ProjectActions.CREATE_PROJECT_FROM_URL ),
						// item( ProjectActions.LOAD_PROJECT ),
						item( ProjectActions.SAVE_PROJECT ),
						item( ProjectActions.SAVE_PROJECT_AS ),
						separator(),
						// item( ProjectActions.IMPORT_TGMM ),
						// item( ProjectActions.IMPORT_SIMI ),
						// item( ProjectActions.IMPORT_MAMUT ),
						// item( ProjectActions.EXPORT_MAMUT ),
						//						separator(),
						item( WindowManager.PREFERENCES_DIALOG ),
						separator(),
						item( WindowManager.OPEN_ONLINE_DOCUMENTATION ) ) );
	}
}
