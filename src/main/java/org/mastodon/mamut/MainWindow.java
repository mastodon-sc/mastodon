/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
import static org.mastodon.mamut.MamutMenuBuilder.windowMenu;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;

import net.miginfocom.swing.MigLayout;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	protected final JMenuBar menubar;

	private final ViewMenu menu;

	private final WindowManager windowManager;

	public MainWindow( final WindowManager windowManager )
	{
		super( "Mastodon" );
		this.windowManager = windowManager;
		setIconImages( MASTODON_ICON );
		setLocationByPlatform( true );
		setLocationRelativeTo( null );

		final ActionMap actionMap = windowManager.getGlobalAppActions().getActionMap();

		/*
		 * BUTTONS
		 */

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new MigLayout() );

		// Views:
		final JLabel viewsLabel = new JLabel( "Views:" );
		viewsLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( viewsLabel, "span, wrap" );

		final JButton tableButton = new JButton( actionMap.get( WindowManager.NEW_TABLE_VIEW ) );
		prepareButton( tableButton, "table", TABLE_ICON_MEDIUM );
		buttonsPanel.add( tableButton, "grow" );

		final JButton bdvButton = new JButton( actionMap.get( WindowManager.NEW_BDV_VIEW ) );
		prepareButton( bdvButton, "bdv", BDV_ICON_MEDIUM );
		buttonsPanel.add( bdvButton, "grow, wrap" );

		final JButton selectionTableButton = new JButton( actionMap.get( WindowManager.NEW_SELECTION_TABLE_VIEW ) );
		prepareButton( selectionTableButton, "selection table", TABLE_ICON_MEDIUM );
		buttonsPanel.add( selectionTableButton, "grow" );

		final JButton trackschemeButton = new JButton( actionMap.get( WindowManager.NEW_TRACKSCHEME_VIEW ) );
		prepareButton( trackschemeButton, "trackscheme", TRACKSCHEME_ICON_MEDIUM );
		buttonsPanel.add( trackschemeButton, "grow, wrap" );

		buttonsPanel.add( new JSeparator(), "span, grow, wrap" );

		// Processing:
		final JLabel processingLabel = new JLabel( "Processing:" );
		processingLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( processingLabel, "span, wrap" );

		final JButton grapherButton = new JButton( actionMap.get( WindowManager.NEW_GRAPHER_VIEW ) );
		prepareButton( grapherButton, "grapher", FEATURES_ICON_MEDIUM );
		buttonsPanel.add( grapherButton, "grow" );

		final JButton featureComputationButton = new JButton( actionMap.get( WindowManager.COMPUTE_FEATURE_DIALOG ) );
		prepareButton( featureComputationButton, "compute features", FEATURES_ICON_MEDIUM );
		buttonsPanel.add( featureComputationButton, "grow, wrap" );

		final JButton editTagSetsButton = new JButton( actionMap.get( WindowManager.TAGSETS_DIALOG ) );
		prepareButton( editTagSetsButton, "configure tags", TAGS_ICON_MEDIUM );
		buttonsPanel.add( editTagSetsButton, "grow, wrap" );

		buttonsPanel.add( new JSeparator(), "span, grow, wrap" );

		// Saving:
		final JLabel ioLabel = new JLabel( "Saving:" );
		ioLabel.setFont( buttonsPanel.getFont().deriveFont( Font.BOLD ) );
		buttonsPanel.add( ioLabel, "span, wrap" );

		final JButton saveProjectButton = new JButton( actionMap.get( ProjectManager.SAVE_PROJECT ) );
		prepareButton( saveProjectButton, "save", SAVE_ICON_MEDIUM );
		buttonsPanel.add( saveProjectButton, "grow" );

		final JButton loadProjectButton = new JButton( actionMap.get( ProjectManager.SAVE_PROJECT_AS ) );
		prepareButton( loadProjectButton, "save as...", SAVE_AS_ICON_MEDIUM );
		buttonsPanel.add( loadProjectButton, "grow, wrap" );

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

		/*
		 * MENU
		 */

		menubar = new JMenuBar();
		setJMenuBar( menubar );

		final Keymap keymap = windowManager.getKeymapManager().getForwardDefaultKeymap();
		menu = new ViewMenu( menubar, keymap, KeyConfigContexts.MASTODON );
		keymap.updateListeners().add( menu::updateKeymap );
		addMenus( menu, actionMap );
		windowManager.getPlugins().addMenus( menu );

		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close( actionMap.get( ProjectManager.SAVE_PROJECT ), e );
			}
		} );

		pack();
		setResizable( false );
	}

	/**
	 * Closes all the windows opened in Mastodon, this main window. If the model
	 * has been modified and not saved, prompts the user for confirmation.
	 * Returns <code>true</code> if Mastodon has been closed, or
	 * <code>false</code> if the user canceled closing.
	 * 
	 * @param saveAction
	 *            the action that saves the Mastodon file.
	 * @param trigger
	 *            the event that triggered this action.
	 * @return <code>true</code> if the Mastodon instance has been closed.
	 */
	public boolean close( final Action saveAction, final WindowEvent trigger )
	{
		if ( windowManager != null && windowManager.getAppModel() == null
				|| windowManager.getAppModel().getModel().isSavePoint() )
		{
			windowManager.closeAllWindows();
			dispose();
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
			saveAction
					.actionPerformed( new ActionEvent( trigger.getSource(), trigger.getID(), trigger.paramString() ) );
			// Fall trough to closing.

		case JOptionPane.NO_OPTION:
			if ( windowManager != null )
				windowManager.closeAllWindows();
			dispose();
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
						// item( ProjectManager.CREATE_PROJECT ),
						// item( ProjectManager.CREATE_PROJECT_FROM_URL ),
						// item( ProjectManager.LOAD_PROJECT ),
						item( ProjectManager.SAVE_PROJECT ),
						item( ProjectManager.SAVE_PROJECT_AS ),
						separator(),
						// item( ProjectManager.IMPORT_TGMM ),
						// item( ProjectManager.IMPORT_SIMI ),
						// item( ProjectManager.IMPORT_MAMUT ),
						// item( ProjectManager.EXPORT_MAMUT ),
						// separator(),
						item( WindowManager.TOGGLE_LOG_DIALOG ),
						item( WindowManager.PREFERENCES_DIALOG ),
						separator(),
						item( WindowManager.OPEN_ONLINE_DOCUMENTATION ) ),
				windowMenu(
						item( WindowManager.NEW_BDV_VIEW ),
						item( WindowManager.NEW_TRACKSCHEME_VIEW ),
						item( WindowManager.NEW_TABLE_VIEW ),
						item( WindowManager.NEW_SELECTION_TABLE_VIEW ),
						item( WindowManager.NEW_GRAPHER_VIEW ),
						item( WindowManager.NEW_BRANCH_BDV_VIEW ),
						item( WindowManager.NEW_BRANCH_TRACKSCHEME_VIEW ),
						item( WindowManager.NEW_HIERARCHY_TRACKSCHEME_VIEW ) ) );
	}
}
