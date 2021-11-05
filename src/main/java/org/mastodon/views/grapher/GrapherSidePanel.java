package org.mastodon.views.grapher;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProject.ProjectReader;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.tag.ui.AbstractTagTable;
import org.mastodon.util.FeatureUtils;
import org.scijava.Context;

public class GrapherSidePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final ImageIcon ADD_ICON = new ImageIcon( AbstractTagTable.class.getResource( "add.png" ) );

	private static final ImageIcon REMOVE_ICON = new ImageIcon( AbstractTagTable.class.getResource( "delete.png" ) );

	private static final int MAX_FEATURE_ALLOWED = 10;

	private static final Dimension BUTTON_SIZE = new Dimension( 24, 24 );

	private static final Dimension COMBO_BOX_MAX_SIZE = new Dimension( 1220, 22 );

	private final List< SpecPair > specs;

	private final JPanel panelYFeatures;

	private final JComboBox< SpecPair > cmbboxXFeature;

	private final Stack< JComboBox< SpecPair > > comboBoxes = new Stack<>();

	private final Stack< Component > struts = new Stack<>();

	private final JCheckBox chkboxConnect;

	private final JRadioButton rdbtnSelection;

	final JButton btnPlot;

	public GrapherSidePanel()
	{
		this.specs = new ArrayList<>();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 288, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 30, 0, 30, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblTitle = new JLabel( "Feature selection" );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		add( lblTitle, gbcLblTitle );

		final JLabel lblXFeature = new JLabel( "Feature for X axis:" );
		lblXFeature.setFont( lblXFeature.getFont().deriveFont( lblXFeature.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblXFeature = new GridBagConstraints();
		gbcLblXFeature.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblXFeature.insets = new Insets( 5, 5, 5, 5 );
		gbcLblXFeature.gridx = 0;
		gbcLblXFeature.gridy = 1;
		add( lblXFeature, gbcLblXFeature );

		this.cmbboxXFeature = new JComboBox<>();
		cmbboxXFeature.setFont( cmbboxXFeature.getFont().deriveFont( cmbboxXFeature.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcCmbboxXFeature = new GridBagConstraints();
		gbcCmbboxXFeature.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxXFeature.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxXFeature.gridx = 0;
		gbcCmbboxXFeature.gridy = 2;
		add( cmbboxXFeature, gbcCmbboxXFeature );

		final JLabel lblYFeatures = new JLabel( "Features for Y axis:" );
		lblYFeatures.setFont( lblYFeatures.getFont().deriveFont( lblYFeatures.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblYFeatures = new GridBagConstraints();
		gbcLblYFeatures.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblYFeatures.insets = new Insets( 5, 5, 5, 5 );
		gbcLblYFeatures.gridx = 0;
		gbcLblYFeatures.gridy = 3;
		add( lblYFeatures, gbcLblYFeatures );

		this.panelYFeatures = new JPanel();
		panelYFeatures.setLayout( new BoxLayout( panelYFeatures, BoxLayout.PAGE_AXIS ) );

		final JScrollPane scrpnlYFeatures = new JScrollPane( panelYFeatures );
		scrpnlYFeatures.setBorder( null );
		scrpnlYFeatures.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

		final GridBagConstraints gbcScrpnlYFeatures = new GridBagConstraints();
		gbcScrpnlYFeatures.insets = new Insets( 5, 5, 5, 5 );
		gbcScrpnlYFeatures.fill = GridBagConstraints.BOTH;
		gbcScrpnlYFeatures.gridx = 0;
		gbcScrpnlYFeatures.gridy = 4;
		add( scrpnlYFeatures, gbcScrpnlYFeatures );

		final JPanel panelSelection = new JPanel();
		final GridBagConstraints gbcPanelSelection = new GridBagConstraints();
		gbcPanelSelection.insets = new Insets( 5, 5, 5, 5 );
		gbcPanelSelection.fill = GridBagConstraints.BOTH;
		gbcPanelSelection.gridx = 0;
		gbcPanelSelection.gridy = 6;
		add( panelSelection, gbcPanelSelection );

		rdbtnSelection = new JRadioButton( "Selection" );
		rdbtnSelection.setFont( rdbtnSelection.getFont().deriveFont( rdbtnSelection.getFont().getSize() - 2f ) );
		panelSelection.add( rdbtnSelection );

		final JRadioButton rdbtnTrackOfSelection = new JRadioButton( "Track of selection" );
		rdbtnTrackOfSelection.setFont( rdbtnTrackOfSelection.getFont().deriveFont( rdbtnTrackOfSelection.getFont().getSize() - 2f ) );
		panelSelection.add( rdbtnTrackOfSelection );

		final ButtonGroup btngrp = new ButtonGroup();
		btngrp.add( rdbtnTrackOfSelection );
		btngrp.add( rdbtnSelection );
		rdbtnTrackOfSelection.setSelected( true );

		chkboxConnect = new JCheckBox( "Connect" );
		chkboxConnect.setFont( chkboxConnect.getFont().deriveFont( chkboxConnect.getFont().getSize() - 2f ) );
		panelSelection.add( chkboxConnect );

		final JPanel panelButtons = new JPanel();
		final GridBagConstraints gbcPanelButtons = new GridBagConstraints();
		gbcPanelButtons.insets = new Insets( 5, 5, 5, 5 );
		gbcPanelButtons.anchor = GridBagConstraints.WEST;
		gbcPanelButtons.fill = GridBagConstraints.VERTICAL;
		gbcPanelButtons.gridx = 0;
		gbcPanelButtons.gridy = 5;
		add( panelButtons, gbcPanelButtons );

		final JButton btnAdd = new JButton( ADD_ICON );
		final JButton btnRemove = new JButton( REMOVE_ICON );
		btnAdd.setMaximumSize( BUTTON_SIZE );
		btnRemove.setMaximumSize( BUTTON_SIZE );
		panelButtons.add( btnAdd );
		panelButtons.add( btnRemove );

		btnPlot = new JButton( "Plot" );
		final GridBagConstraints gbcBtnPlot = new GridBagConstraints();
		gbcBtnPlot.anchor = GridBagConstraints.EAST;
		gbcBtnPlot.gridx = 0;
		gbcBtnPlot.gridy = 7;
		add( btnPlot, gbcBtnPlot );

		// Listeners.
		btnAdd.addActionListener( e -> addFeature() );
		btnRemove.addActionListener( e -> removeFeature() );
	}

	public < O > void setFeatures( final Map< FeatureSpec< ?, O >, Feature< O > > features )
	{
		specs.clear();
		if ( features != null )
		{
			for ( final FeatureSpec< ?, O > fs : features.keySet() )
			{
				for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
				{
					final SpecPair sp = new SpecPair( fs, ps );
					specs.add( sp );
				}
			}
		}
		refreshComboBoxes();
	}

	public GraphConfig getGraphConfig()
	{
		final SpecPair xFeature = ( SpecPair ) cmbboxXFeature.getSelectedItem();
		final List< SpecPair > yFeatures = new ArrayList<>( comboBoxes.size() );
		for ( final JComboBox< SpecPair > cmbbox : comboBoxes )
			yFeatures.add( ( SpecPair ) cmbbox.getSelectedItem() );

		return new GraphConfig(
				xFeature,
				yFeatures,
				!rdbtnSelection.isSelected(),
				chkboxConnect.isSelected() );
	}

	private void refreshComboBoxes()
	{
		while ( !comboBoxes.isEmpty() )
		{
			panelYFeatures.remove( comboBoxes.pop() );
			panelYFeatures.remove( struts.pop() );
		}
		addFeature();

		final DefaultComboBoxModel< SpecPair > model = new DefaultComboBoxModel<>( new Vector<>( specs ) );
		cmbboxXFeature.setModel( model );

		revalidate();
		repaint();
	}

	private void addFeature( final SpecPair sp )
	{
		if ( comboBoxes.size() > MAX_FEATURE_ALLOWED )
			return;

		final ComboBoxModel< SpecPair > cmbboxYFeatureModel = new DefaultComboBoxModel<>( new Vector<>( specs ) );
		final JComboBox< SpecPair > cmbboxYFeature = new JComboBox<>();
		cmbboxYFeature.setModel( cmbboxYFeatureModel );
		cmbboxYFeature.setFont( cmbboxYFeature.getFont().deriveFont( cmbboxYFeature.getFont().getSize() - 2f ) );
		cmbboxYFeature.setMaximumSize( COMBO_BOX_MAX_SIZE );

		cmbboxYFeature.setSelectedItem( sp );

		final Component strut = Box.createVerticalStrut( 10 );
		panelYFeatures.add( strut );
		panelYFeatures.add( cmbboxYFeature );
		panelYFeatures.revalidate();
		comboBoxes.push( cmbboxYFeature );
		struts.push( strut );
	}

	private void addFeature()
	{
		if ( specs.isEmpty() )
			return;
		SpecPair nextSp = specs.get( 0 );
		if ( !comboBoxes.isEmpty() )
		{
			int newIndex = comboBoxes.get( comboBoxes.size() - 1 ).getSelectedIndex() + 1;
			if ( newIndex >= specs.size() )
				newIndex = 0;
			nextSp = specs.get( newIndex );
		}
		addFeature( nextSp );
	}

	private void removeFeature()
	{
		if ( comboBoxes.size() <= 1 )
			return;
		panelYFeatures.remove( comboBoxes.pop() );
		panelYFeatures.remove( struts.pop() );
		panelYFeatures.revalidate();
		panelYFeatures.repaint();
	}

	private static final FeatureModel demoFM()
	{
		final Model model = new Model();
		try
		{
			final MamutProject project = new MamutProjectIO().load( "samples/drosophila_crop.mastodon" );
			final ProjectReader reader = project.openForReading();

			final FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
			// Load features.
			MamutRawFeatureModelIO.deserialize(
					new Context(),
					model,
					idmap,
					reader );
		}
		catch ( final IOException | ClassNotFoundException e )
		{
			e.printStackTrace();
		}
		return model.getFeatureModel();
	}

	public static void main( final String[] args )
	{
		final FeatureModel fm = demoFM();
		final Map< FeatureSpec< ?, Spot >, Feature< Spot > > spotFeatures = FeatureUtils.collectFeatureMap( fm, Spot.class );
		final GrapherSidePanel gsp = new GrapherSidePanel();
		gsp.setFeatures( spotFeatures );

		final JFrame frame = new JFrame( "Grapher side panel" );
		frame.getContentPane().add( gsp );
		frame.setSize( 350, 400 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
