package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.windowMenu;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;
import org.scijava.Contextual;
import org.scijava.NullContextException;
import org.scijava.plugin.Parameter;

public class MainWindow extends JFrame implements Contextual
{
	protected final JMenuBar menubar;

	private final ViewMenu menu;

	public MainWindow( final WindowManager windowManager )
	{
		super( "Mastodon" );
		/*
		 * Instantiate context with required services.
		 */
		this.context = new Context( MamutFeatureComputerService.class );

		/*
		 * GUI
		 */

		final ActionMap actionMap = windowManager.getGlobalAppActions().getActionMap();

		final JPanel buttonsPanel = new JPanel();
		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 1.0, 1.0 };
		gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		buttonsPanel.setLayout( gbl );

		final GridBagConstraints separator_gbc = new GridBagConstraints();
		separator_gbc.fill = GridBagConstraints.HORIZONTAL;
		separator_gbc.gridwidth = 2;
		separator_gbc.insets = new Insets( 5, 5, 5, 5 );
		separator_gbc.gridx = 0;

		final GridBagConstraints label_gbc = new GridBagConstraints();
		label_gbc.fill = GridBagConstraints.HORIZONTAL;
		label_gbc.gridwidth = 2;
		label_gbc.insets = new Insets( 5, 5, 5, 5 );
		label_gbc.gridx = 0;

		final GridBagConstraints button_gbc_right = new GridBagConstraints();
		button_gbc_right.fill = GridBagConstraints.BOTH;
		button_gbc_right.insets = new Insets( 0, 0, 5, 0 );
		button_gbc_right.gridx = 1;

		final GridBagConstraints button_gbc_left = new GridBagConstraints();
		button_gbc_left.fill = GridBagConstraints.BOTH;
		button_gbc_left.insets = new Insets( 0, 0, 5, 5 );
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
		addMenus( menu, actionMap );

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

	// -- Contextual methods --

	@Parameter
	private Context context;

	@Override
	public Context context()
	{
		if ( context == null )
			throw new NullContextException();
		return context;
	}

	@Override
	public Context getContext()
	{
		return context;
	}

	public static void addMenus( final ViewMenu menu, final ActionMap actionMap )
	{
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						item( ProjectManager.CREATE_PROJECT ),
						item( ProjectManager.LOAD_PROJECT ),
						item( ProjectManager.SAVE_PROJECT ),
						separator(),
						item( ProjectManager.IMPORT_TGMM )
				),
				windowMenu(
						item( WindowManager.NEW_BDV_VIEW ),
						item( WindowManager.NEW_TRACKSCHEME_VIEW )
				)
		);
	}

	protected static final String dump(final Model model)
	{
		final ModelGraph graph = model.getGraph();
		final FeatureModel featureModel = model.getFeatureModel();

		final StringBuilder str = new StringBuilder();
		str.append( "Model " + model.toString()  + "\n");

		/*
		 * Collect spot feature headers.
		 */

		final Map< String, FeatureProjection< Spot > > sfs = new LinkedHashMap<>();
		Set< Feature< ?, ? > > spotFeatures = featureModel.getFeatureSet( Spot.class );
		if (null == spotFeatures)
			spotFeatures = Collections.emptySet();


		for ( final Feature< ?, ? > feature : spotFeatures )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Spot, ? > sf = ( Feature< Spot, ? > ) feature;
			final Map< String, FeatureProjection< Spot > > projections = sf.getProjections();
			sfs.putAll( projections );
		}

		/*
		 * Loop over all spots.
		 */

		str.append( "Spots:\n" );
		final String h1a = String.format( "%9s  %9s  %6s  %9s  %9s  %9s",
				"Id", "Label", "Frame", "X", "Y", "Z" );
		str.append( h1a );

		final int[] spotColumnHeaderWidth = new int[ sfs.size() ];
		int i = 0;
		for ( final String pn : sfs.keySet() )
		{
			spotColumnHeaderWidth[ i ] = pn.length() + 2;
			str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", pn ) );
			i++;
		}

		str.append( '\n' );
		final char[] sline = new char[h1a.length() + Arrays.stream( spotColumnHeaderWidth ).sum() + 2 * spotColumnHeaderWidth.length];
		Arrays.fill( sline, '-' );
		str.append( sline );
		str.append( '\n' );

		for ( final Spot spot : graph.vertices() )
		{
			final String h1b = String.format( "%9d  %9s  %6d  %9.1f  %9.1f  %9.1f",
					spot.getInternalPoolIndex(), spot.getLabel(), spot.getTimepoint(),
					spot.getDoublePosition( 0 ), spot.getDoublePosition( 1 ), spot.getDoublePosition( 2 ) );

			str.append( h1b );
			i = 0;
			for ( final String pn : sfs.keySet() )
			{
				if (sfs.get( pn ).isSet( spot ))
					str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + ".1f", sfs.get( pn ).value( spot ) ) );
				else
					str.append( String.format( "  %" + spotColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		/*
		 * Collect link feature headers.
		 */

		final Map< String, FeatureProjection< Link > > lfs = new LinkedHashMap<>();
		Set< Feature< ?, ? > > linkFeatures = featureModel.getFeatureSet( Link.class );
		if (null == linkFeatures)
			linkFeatures = Collections.emptySet();


		for ( final Feature< ?, ? > feature : linkFeatures )
		{
			@SuppressWarnings( "unchecked" )
			final Feature< Link, ? > lf = ( Feature< Link, ? > ) feature;
			final Map< String, FeatureProjection< Link > > projections = lf.getProjections();
			lfs.putAll( projections );
		}

		/*
		 * Loop over all links.
		 */

		str.append( "Links:\n" );
		final String h2a = String.format( "%9s  %9s  %9s", "Id", "Source Id", "Target Id" );
		str.append( h2a );

		final int[] linkColumnHeaderWidth = new int[ lfs.size() ];
		i = 0;
		for ( final String pn : lfs.keySet() )
		{
			linkColumnHeaderWidth[ i ] = pn.length() + 2;
			str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", pn ) );
			i++;
		}

		str.append( '\n' );
		final char[] lline = new char[h2a.length() + Arrays.stream( linkColumnHeaderWidth ).sum() + 2 * linkColumnHeaderWidth.length];
		Arrays.fill( lline, '-' );
		str.append( lline );
		str.append( '\n' );

		final Spot ref = graph.vertexRef();
		for ( final Link link : graph.edges() )
		{
			final String h1b = String.format( "%9d  %9d  %9d", link.getInternalPoolIndex(),
					link.getSource( ref ).getInternalPoolIndex(), link.getTarget(ref).getInternalPoolIndex() );
			str.append( h1b );
			i = 0;
			for ( final String pn : lfs.keySet() )
			{
				if (lfs.get( pn ).isSet( link ))
					str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + ".1f", lfs.get( pn ).value( link ) ) );
				else
					str.append( String.format( "  %" + linkColumnHeaderWidth[ i ] + "s", "unset" ) );
				i++;
			}
			str.append( '\n' );
		}

		return str.toString();
	}
}
