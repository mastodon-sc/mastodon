package org.mastodon.revised.mamut;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import com.sun.deploy.ref.AppModel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.XmlFileFilter;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.Context;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

public class MainWindow extends JFrame
{
	protected final JMenuBar menubar;

	private final ViewMenu menu;

	public MainWindow( final WindowManager windowManager )
	{
		super( "Mastodon" );

		final ActionMap actionMap = windowManager.getGlobalAppActions().getActionMap();

		final JPanel buttonsPanel = new JPanel();
		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 1.0, 1.0 };
		gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		buttonsPanel.setLayout(gbl);

		final GridBagConstraints separator_gbc = new GridBagConstraints();
		separator_gbc.fill = GridBagConstraints.HORIZONTAL;
		separator_gbc.gridwidth = 2;
		separator_gbc.insets = new Insets(5, 5, 5, 5);
		separator_gbc.gridx = 0;

		final GridBagConstraints label_gbc = new GridBagConstraints();
		label_gbc.fill = GridBagConstraints.HORIZONTAL;
		label_gbc.gridwidth = 2;
		label_gbc.insets = new Insets(5, 5, 5, 5);
		label_gbc.gridx = 0;

		final GridBagConstraints button_gbc_right = new GridBagConstraints();
		button_gbc_right.fill = GridBagConstraints.BOTH;
		button_gbc_right.insets = new Insets(0, 0, 5, 0);
		button_gbc_right.gridx = 1;

		final GridBagConstraints button_gbc_left = new GridBagConstraints();
		button_gbc_left.fill = GridBagConstraints.BOTH;
		button_gbc_left.insets = new Insets(0, 0, 5, 5);
		button_gbc_left.gridx = 0;

		int gridy = 0;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Views:" ), label_gbc );

		++gridy;

		final JButton bdvButton = new JButton( actionMap.get( WindowManager.NEW_BDV_VIEW ) );
		bdvButton.setText( "bdv" );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( bdvButton, button_gbc_right );

		++gridy;

		final JButton trackschemeButton = new JButton( actionMap.get( WindowManager.NEW_TRACKSCHEME_VIEW ) );
		trackschemeButton.setText( "trackscheme" );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( trackschemeButton, button_gbc_right );

		++gridy;

		separator_gbc.gridy = gridy;
		buttonsPanel.add( new JSeparator(), separator_gbc );

		++gridy;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Processing:" ), label_gbc );

		++gridy;

		final JButton featureComputationButton = new JButton();
		featureComputationButton.setText( "features and tags" );
		featureComputationButton.setEnabled( false ); // TODO
//		featureComputationButton.addActionListener( e -> toggleFeaturesDialog() );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( featureComputationButton, button_gbc_right );

		++gridy;

		separator_gbc.gridy = gridy;
		buttonsPanel.add( new JSeparator(), separator_gbc );

		++gridy;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Input / Output:" ), label_gbc );

		++gridy;

		final JButton createProjectButton = new JButton( actionMap.get( ProjectManager.CREATE_PROJECT ) );
		createProjectButton.setText( "new project" );
		button_gbc_left.gridy = gridy;
		buttonsPanel.add( createProjectButton, button_gbc_left );

		final JButton importButton = new JButton( actionMap.get( ProjectManager.IMPORT_TGMM ) );
		importButton.setText( "import tgmm" );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( importButton, button_gbc_right );

		++gridy;

		final JButton saveProjectButton = new JButton( actionMap.get( ProjectManager.SAVE_PROJECT ) );
		saveProjectButton.setText( "save project" );
		button_gbc_left.gridy = gridy;
		buttonsPanel.add( saveProjectButton, button_gbc_left );

		final JButton loadProjectButton = new JButton( actionMap.get( ProjectManager.LOAD_PROJECT ) );
		loadProjectButton.setText( "load project" );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( loadProjectButton, button_gbc_right );

		final Container content = getContentPane();
		content.add( buttonsPanel, BorderLayout.NORTH );

		menubar = new JMenuBar();
		setJMenuBar( menubar );

		menu = new ViewMenu( menubar, windowManager.getKeyConfig(), "mastodon" );
		menu.addItem( "File", "New Project", actionMap.get( ProjectManager.CREATE_PROJECT ) );
		menu.addItem( "File", "Load Project", actionMap.get( ProjectManager.LOAD_PROJECT ) );
		menu.addItem( "File", "Save Project", actionMap.get( ProjectManager.SAVE_PROJECT ) );
		menu.addSeparator( "File" );
		menu.addItem( "File", "Import TGMM tracks", actionMap.get( ProjectManager.IMPORT_TGMM ) );

		menu.addItem( "Window", "New Bdv", actionMap.get( WindowManager.NEW_BDV_VIEW ) );
		menu.addItem( "Window", "New Trackscheme", actionMap.get( WindowManager.NEW_TRACKSCHEME_VIEW ) );

//		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
//		addWindowListener( new WindowAdapter()
//		{
//			@Override
//			public void windowClosed( final WindowEvent e )
//			{
//				project = null;
//				if ( windowManager != null )
//					windowManager.closeAllWindows();
//				windowManager = null;
//			}
//		} );
		setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		pack();
	}
}
