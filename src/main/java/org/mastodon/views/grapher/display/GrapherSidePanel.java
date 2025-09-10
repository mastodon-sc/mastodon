/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.grapher.display;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO2;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectReader;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.util.FeatureUtils;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.HasContextChooser;
import org.mastodon.views.grapher.display.FeatureGraphConfig.GraphDataItemsSource;
import org.scijava.Context;

/**
 * Panel that lets the user specifies what to plot. The user specifications are
 * bundled as a {@link FeatureGraphConfig} object.
 *
 * @author Jean-Yves Tinevez
 */
public class GrapherSidePanel< V extends Vertex< E >, E extends Edge< V > > extends JPanel implements HasContextChooser< V >
{

	private static final long serialVersionUID = 1L;

	private final int nSources;

	private final List< FeatureSpecPair > specs;

	private final JComboBox< FeatureSpecPair > cmbboxXFeature;

	private final JComboBox< FeatureSpecPair > cmbboxYFeature;

	private final JCheckBox chkboxConnect;

	private final JRadioButton rdbtnSelection;

	private final JRadioButton rdbtnTrackOfSelection;

	private final JRadioButton rdbtnKeepCurrent;

	private final JRadioButton rdbtnContext;

	private final JButton btnPlot;

	private final ContextChooser< V > contextChooser;

	public GrapherSidePanel( final int nSources, final ContextChooser< V > contextChooser )
	{
		this.nSources = nSources;
		this.specs = new ArrayList<>();
		this.contextChooser = contextChooser;

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 150, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 30, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights =
				new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
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
		gbcCmbboxXFeature.insets = new Insets( 0, 5, 5, 5 );
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
		gbcCmbboxYFeature.insets = new Insets( 0, 5, 5, 5 );
		gbcCmbboxYFeature.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxYFeature.gridx = 0;
		gbcCmbboxYFeature.gridy = 4;
		add( cmbboxYFeature, gbcCmbboxYFeature );

		final JLabel lblDataItems = new JLabel( "Items to plot:" );
		lblDataItems.setFont( lblDataItems.getFont().deriveFont( lblDataItems.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblDataItems = new GridBagConstraints();
		gbcLblDataItems.anchor = GridBagConstraints.WEST;
		gbcLblDataItems.insets = new Insets( 5, 5, 5, 5 );
		gbcLblDataItems.gridx = 0;
		gbcLblDataItems.gridy = 5;
		add( lblDataItems, gbcLblDataItems );

		final ButtonGroup btngrp = new ButtonGroup();

		rdbtnSelection = new JRadioButton( "Selection" );
		final GridBagConstraints gbcRdbtnSelection = new GridBagConstraints();
		gbcRdbtnSelection.anchor = GridBagConstraints.WEST;
		gbcRdbtnSelection.insets = new Insets( 0, 5, 0, 5 );
		gbcRdbtnSelection.gridx = 0;
		gbcRdbtnSelection.gridy = 6;
		add( rdbtnSelection, gbcRdbtnSelection );
		rdbtnSelection.setFont( rdbtnSelection.getFont().deriveFont( rdbtnSelection.getFont().getSize() - 2f ) );
		btngrp.add( rdbtnSelection );

		rdbtnTrackOfSelection = new JRadioButton( "Track of selection" );
		final GridBagConstraints gbcRdbtnTrackOfSelection = new GridBagConstraints();
		gbcRdbtnTrackOfSelection.anchor = GridBagConstraints.WEST;
		gbcRdbtnTrackOfSelection.insets = new Insets( 0, 5, 0, 5 );
		gbcRdbtnTrackOfSelection.gridx = 0;
		gbcRdbtnTrackOfSelection.gridy = 7;
		add( rdbtnTrackOfSelection, gbcRdbtnTrackOfSelection );
		rdbtnTrackOfSelection.setFont(
				rdbtnTrackOfSelection.getFont().deriveFont( rdbtnTrackOfSelection.getFont().getSize() - 2f ) );
		btngrp.add( rdbtnTrackOfSelection );
		rdbtnTrackOfSelection.setSelected( true );

		rdbtnKeepCurrent = new JRadioButton( "Keep current" );
		final GridBagConstraints gbcRdbtnKeepCurrent = new GridBagConstraints();
		gbcRdbtnKeepCurrent.anchor = GridBagConstraints.WEST;
		gbcRdbtnKeepCurrent.insets = new Insets( 0, 5, 0, 5 );
		gbcRdbtnKeepCurrent.gridx = 0;
		gbcRdbtnKeepCurrent.gridy = 8;
		add( rdbtnKeepCurrent, gbcRdbtnKeepCurrent );
		rdbtnKeepCurrent.setFont( rdbtnKeepCurrent.getFont().deriveFont( rdbtnKeepCurrent.getFont().getSize() - 2f ) );
		btngrp.add( rdbtnKeepCurrent );

		rdbtnContext = new JRadioButton();
		rdbtnContext.setText( "Full graph" );
		btngrp.add( rdbtnContext );

		final GridBagConstraints gbcRdbtnContext = new GridBagConstraints();
		gbcRdbtnContext.anchor = GridBagConstraints.WEST;
		gbcRdbtnContext.insets = new Insets( 0, 5, 0, 5 );
		gbcRdbtnContext.gridx = 0;
		gbcRdbtnContext.gridy = 9;
		rdbtnContext.setFont( rdbtnContext.getFont().deriveFont( rdbtnContext.getFont().getSize() - 2f ) );
		add( rdbtnContext, gbcRdbtnContext );

		if ( contextChooser != null )
		{
			rdbtnContext.setText( "From context:" );
			final ContextChooserPanel< ? > chooserPanel = new ContextChooserPanel<>( contextChooser );
			final GridBagConstraints gbcChooserPanel = new GridBagConstraints();
			gbcChooserPanel.fill = GridBagConstraints.BOTH;
			gbcChooserPanel.insets = new Insets( 0, 5, 5, 5 );
			gbcChooserPanel.gridx = 0;
			gbcChooserPanel.gridy = 10;
			add( chooserPanel, gbcChooserPanel );
			for ( final Component c : chooserPanel.getComponents() )
				c.setFont( c.getFont().deriveFont( c.getFont().getSize2D() - 2f ) );
			// show context box only if the right button is selected.
			final EverythingDisablerAndReenabler contextEnabler =
					new EverythingDisablerAndReenabler( chooserPanel, new Class[] { Label.class } );
			contextEnabler.setEnabled( rdbtnContext.isSelected() );
			rdbtnContext.addChangeListener( e -> contextEnabler.setEnabled( rdbtnContext.isSelected() ) );
		}

		chkboxConnect = new JCheckBox( "Show edges" );
		final GridBagConstraints gbcChkboxConnect = new GridBagConstraints();
		gbcChkboxConnect.anchor = GridBagConstraints.WEST;
		gbcChkboxConnect.insets = new Insets( 5, 5, 5, 5 );
		gbcChkboxConnect.gridx = 0;
		gbcChkboxConnect.gridy = 11;
		add( chkboxConnect, gbcChkboxConnect );
		chkboxConnect.setSelected( true );
		chkboxConnect.setFont( chkboxConnect.getFont().deriveFont( chkboxConnect.getFont().getSize() - 2f ) );

		btnPlot = new JButton( "Plot" );
		final GridBagConstraints gbcBtnPlot = new GridBagConstraints();
		gbcBtnPlot.anchor = GridBagConstraints.SOUTHEAST;
		gbcBtnPlot.gridx = 0;
		gbcBtnPlot.gridy = 12;
		add( btnPlot, gbcBtnPlot );

		/*
		 * Listeners.
		 */
		// After pressing the 'plot' button, we default to 'keep current'.
		btnPlot.addActionListener( e -> {
			if ( !rdbtnContext.isSelected() )
				rdbtnKeepCurrent.setSelected( true );
		} );
	}

	/**
	 * Exposes the plot button of this panel.
	 *
	 * @return the plot button.
	 */
	public JButton getBtnPlot()
	{
		return btnPlot;
	}

	public void setFeatures(
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
						specs.add( new FeatureSpecPair( fs, ps, false, false ) );
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
		final GraphDataItemsSource itemSource = rdbtnContext.isSelected()
				? GraphDataItemsSource.CONTEXT
				: rdbtnKeepCurrent.isSelected()
						? GraphDataItemsSource.KEEP_CURRENT
				: rdbtnSelection.isSelected()
						? GraphDataItemsSource.SELECTION
				: GraphDataItemsSource.TRACK_OF_SELECTION;
		return new FeatureGraphConfig(
				xFeature,
				yFeature,
				itemSource,
				chkboxConnect.isSelected() );
	}

	public void setGraphConfig( final FeatureGraphConfig gc )
	{
		cmbboxXFeature.setSelectedItem( gc.getXFeature() );
		cmbboxYFeature.setSelectedItem( gc.getYFeature() );
		switch ( gc.itemSource() )
		{
		case CONTEXT:
			rdbtnContext.setSelected( true );
			break;
		case KEEP_CURRENT:
			rdbtnKeepCurrent.setSelected( true );
			break;
		case SELECTION:
			rdbtnSelection.setSelected( true );
		default:
			break;

		}
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

	public FeatureSpec< ?, ? > getFeatureSpec( final String featureSpecKey )
	{
		for ( final FeatureSpecPair featureSpecPair : specs )
		{
			if ( featureSpecPair.getFeatureSpecKey().equals( featureSpecKey ) )
				return featureSpecPair.featureSpec;
		}
		return null;
	}

	public FeatureProjectionSpec getFeatureProjectionSpec( final String projectionKey )
	{
		for ( final FeatureSpecPair featureSpecPair : specs )
		{
			if ( featureSpecPair.projectionKey().toString().equals( projectionKey ) )
				return featureSpecPair.projectionKey().getSpec();
		}
		return null;
	}

	@Override
	public ContextChooser< V > getContextChooser()
	{
		return this.contextChooser;
	}

	private static final FeatureModel demoFM()
	{
		final Model model = new Model();
		try
		{
			final MamutProject project = MamutProjectIO.load( "samples/drosophila_crop.mastodon" );
			final ProjectReader reader = project.openForReading();

			final FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
			// Load features.
			MamutRawFeatureModelIO2.deserialize(
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

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final FeatureModel fm = demoFM();
		final Map< FeatureSpec< ?, Spot >, Feature< Spot > > spotFeatures =
				FeatureUtils.collectFeatureMap( fm, Spot.class );
		final Map< FeatureSpec< ?, Link >, Feature< Link > > linkFeatures =
				FeatureUtils.collectFeatureMap( fm, Link.class );
		final GrapherSidePanel<Spot, Link> gsp = new GrapherSidePanel<Spot, Link>( 2, new ContextChooser<>( null ) );
		gsp.setFeatures( spotFeatures, linkFeatures );

		final JFrame frame = new JFrame( "Grapher side panel" );
		frame.getContentPane().add( gsp );
		frame.setSize( 350, 400 );
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
