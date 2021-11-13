package org.mastodon.views.grapher.display;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProject.ProjectReader;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.util.FeatureUtils;
import org.scijava.Context;

/**
 * Panel that lets the user specifies what to plot. The user specifications are
 * bundled as a {@link FeatureGraphConfig} object.
 * 
 * @author Jean-Yves Tinevez
 */
public class GrapherSidePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final int nSources;

	private final List< FeatureSpecPair > specs;

	private final JComboBox< FeatureSpecPair > cmbboxXFeature;

	private final JComboBox< FeatureSpecPair > cmbboxYFeature;

	private final JCheckBox chkboxConnect;

	private final JRadioButton rdbtnSelection;

	private final JRadioButton rdbtnKeepCurrent;

	final JButton btnPlot;

	public GrapherSidePanel( final int nSources )
	{
		this.nSources = nSources;
		this.specs = new ArrayList<>();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 288, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 30, 0, 30, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
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

		final JLabel lblYFeatures = new JLabel( "Feature for Y axis:" );
		lblYFeatures.setFont( lblYFeatures.getFont().deriveFont( lblYFeatures.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblYFeatures = new GridBagConstraints();
		gbcLblYFeatures.anchor = GridBagConstraints.SOUTHWEST;
		gbcLblYFeatures.insets = new Insets( 5, 5, 5, 5 );
		gbcLblYFeatures.gridx = 0;
		gbcLblYFeatures.gridy = 3;
		add( lblYFeatures, gbcLblYFeatures );

		this.cmbboxYFeature = new JComboBox<>();
		cmbboxYFeature.setFont( cmbboxYFeature.getFont().deriveFont( cmbboxYFeature.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcCmbboxYFeature = new GridBagConstraints();
		gbcCmbboxYFeature.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxYFeature.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxYFeature.gridx = 0;
		gbcCmbboxYFeature.gridy = 4;
		add( cmbboxYFeature, gbcCmbboxYFeature );

		final JPanel panelSelection = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelSelection.getLayout();
		flowLayout.setAlignment( FlowLayout.LEFT );
		final GridBagConstraints gbcPanelSelection = new GridBagConstraints();
		gbcPanelSelection.insets = new Insets( 5, 5, 5, 5 );
		gbcPanelSelection.fill = GridBagConstraints.BOTH;
		gbcPanelSelection.gridx = 0;
		gbcPanelSelection.gridy = 5;
		add( panelSelection, gbcPanelSelection );

		rdbtnSelection = new JRadioButton( "Selection" );
		rdbtnSelection.setFont( rdbtnSelection.getFont().deriveFont( rdbtnSelection.getFont().getSize() - 2f ) );
		panelSelection.add( rdbtnSelection );

		final JRadioButton rdbtnTrackOfSelection = new JRadioButton( "Track of selection" );
		rdbtnTrackOfSelection.setFont( rdbtnTrackOfSelection.getFont().deriveFont( rdbtnTrackOfSelection.getFont().getSize() - 2f ) );
		panelSelection.add( rdbtnTrackOfSelection );

		rdbtnKeepCurrent = new JRadioButton( "Keep current" );
		rdbtnKeepCurrent.setFont( rdbtnKeepCurrent.getFont().deriveFont( rdbtnKeepCurrent.getFont().getSize() - 2f ) );
		panelSelection.add( rdbtnKeepCurrent );

		final ButtonGroup btngrp = new ButtonGroup();
		btngrp.add( rdbtnTrackOfSelection );
		btngrp.add( rdbtnSelection );
		btngrp.add( rdbtnKeepCurrent );
		rdbtnTrackOfSelection.setSelected( true );

		chkboxConnect = new JCheckBox( "Connect" );
		chkboxConnect.setSelected( true );
		chkboxConnect.setFont( chkboxConnect.getFont().deriveFont( chkboxConnect.getFont().getSize() - 2f ) );
		panelSelection.add( chkboxConnect );

		btnPlot = new JButton( "Plot" );
		final GridBagConstraints gbcBtnPlot = new GridBagConstraints();
		gbcBtnPlot.anchor = GridBagConstraints.EAST;
		gbcBtnPlot.gridx = 0;
		gbcBtnPlot.gridy = 6;
		add( btnPlot, gbcBtnPlot );

		/*
		 * Listener. After pressing the 'plot' button, we default to 'keep
		 * current'.
		 */
		btnPlot.addActionListener( e -> rdbtnKeepCurrent.setSelected( true ) );
	}

	public < V, E > void setFeatures(
			final Map< FeatureSpec< ?, V >, Feature< V > > vertexFeatures,
			final Map< FeatureSpec< ?, E >, Feature< E > > edgeFeatures )
	{
		specs.clear();
		if ( vertexFeatures != null )
		{
			for ( final FeatureSpec< ?, V > fs : vertexFeatures.keySet() )
			{
				final Multiplicity multiplicity = fs.getMultiplicity();
				switch ( fs.getMultiplicity() )
				{
				case ON_SOURCES:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						for ( int c = 0; c < nSources; c++ )
							specs.add( new FeatureSpecPair( fs, ps, c, false, false ) );
					}
					break;
				case ON_SOURCE_PAIRS:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						for ( int c1 = 0; c1 < nSources; c1++ )
						{
							for ( int c2 = 0; c2 < nSources; c2++ )
								specs.add( new FeatureSpecPair( fs, ps, c1, c2, false, false ) );
						}
					}
					break;
				case SINGLE:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						specs.add(  new FeatureSpecPair( fs, ps, false, false ) );
					}
					break;
				default:
					throw new IllegalArgumentException( "Unknown multiplicity: " + multiplicity );
				}

			}
		}
		if ( edgeFeatures != null )
		{
			for ( final FeatureSpec< ?, E > fs : edgeFeatures.keySet() )
			{
				final Multiplicity multiplicity = fs.getMultiplicity();
				switch ( fs.getMultiplicity() )
				{
				case ON_SOURCES:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						for ( int c = 0; c < nSources; c++ )
						{
							specs.add( new FeatureSpecPair( fs, ps, c, true, true ) );
							specs.add( new FeatureSpecPair( fs, ps, c, true, false ) );
						}
					}
					break;
				case ON_SOURCE_PAIRS:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						for ( int c1 = 0; c1 < nSources; c1++ )
							for ( int c2 = 0; c2 < nSources; c2++ )
							{
								specs.add( new FeatureSpecPair( fs, ps, c1, c2, true, true ) );
								specs.add( new FeatureSpecPair( fs, ps, c1, c2, true, false ) );
							}
					}
					break;
				case SINGLE:
					for ( final FeatureProjectionSpec ps : fs.getProjectionSpecs() )
					{
						specs.add( new FeatureSpecPair( fs, ps, true, true ) );
						specs.add( new FeatureSpecPair( fs, ps, true, false ) );
					}
					break;
				default:
					throw new IllegalArgumentException( "Unknown multiplicity: " + multiplicity );
				}

			}
		}
		specs.sort( null );
		refreshComboBoxes();
	}

	public FeatureGraphConfig getGraphConfig()
	{
		final FeatureSpecPair xFeature = ( FeatureSpecPair ) cmbboxXFeature.getSelectedItem();
		final FeatureSpecPair yFeature = ( FeatureSpecPair ) cmbboxYFeature.getSelectedItem();
		return new FeatureGraphConfig(
				xFeature,
				yFeature,
				rdbtnKeepCurrent.isSelected(),
				!rdbtnSelection.isSelected(),
				chkboxConnect.isSelected() );
	}

	public void setGraphConfig( final FeatureGraphConfig gc )
	{
		cmbboxXFeature.setSelectedItem( gc.getXFeature() );
		cmbboxYFeature.setSelectedItem( gc.getYFeature() );
		rdbtnKeepCurrent.setSelected( gc.keepCurrent() );
		rdbtnSelection.setSelected( !gc.graphTrackOfSelection() );
		chkboxConnect.setSelected( gc.drawConnected() );
	}

	private void refreshComboBoxes()
	{
		final Object previousX = cmbboxXFeature.getSelectedItem();
		final Object previousY = cmbboxYFeature.getSelectedItem();

		cmbboxXFeature.setModel( new DefaultComboBoxModel<>( new Vector<>( specs ) ) );
		cmbboxYFeature.setModel( new DefaultComboBoxModel<>( new Vector<>( specs ) ) );

		if ( previousX != null )
			cmbboxXFeature.setSelectedItem( previousX );
		if ( previousY != null )
			cmbboxYFeature.setSelectedItem( previousY );

		repaint();
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
		final Map< FeatureSpec< ?, Link >, Feature< Link > > linkFeatures = FeatureUtils.collectFeatureMap( fm, Link.class );
		final GrapherSidePanel gsp = new GrapherSidePanel( 2 );
		gsp.setFeatures( spotFeatures, linkFeatures );

		final JFrame frame = new JFrame( "Grapher side panel" );
		frame.getContentPane().add( gsp );
		frame.setSize( 350, 400 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
