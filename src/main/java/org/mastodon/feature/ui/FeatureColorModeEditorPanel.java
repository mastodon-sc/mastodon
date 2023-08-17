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
package org.mastodon.feature.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.mastodon.ui.coloring.ColorMap;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.VertexColorMode;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.FeatureRangeCalculator;

import com.itextpdf.text.Font;

/**
 * JPanel to edit a single {@link FeatureColorMode}.
 *
 * @author Jean-Yves Tinevez
 */
public class FeatureColorModeEditorPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final FeatureColorMode mode;

	private final FeatureSelectionPanel edgeFeatureSelectionPanel;

	private final FeatureSelectionPanel vertexFeatureSelectionPanel;

	/**
	 * Flag used to avoid altering the mode when updating the features displayed
	 * in the JComboBoxes.
	 */
	private boolean doForwardToMode = true;

	private AvailableFeatureProjections availableFeatureProjections;

	private final Runnable update;

	public FeatureColorModeEditorPanel(
			final FeatureColorMode mode,
			final FeatureRangeCalculator rangeCalculator,
			final String vertexName,
			final String edgeName )
	{
		setPreferredSize( new Dimension( 550, 550 ) );
		this.mode = mode;

		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 20, 45, 50, 45, 45, 45, 20, 45, 50, 45, 45, 45, 45 };

		setLayout( layout );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 5, 5, 5, 5 );
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		addToLayout( new JSeparator(), c );
		final JLabel lbl1 = new JLabel( "Coloring " + vertexName + "s" );
		lbl1.setFont( getFont().deriveFont( Font.BOLD ) );
		addToLayout( lbl1, c );

		/*
		 * Vertex color mode.
		 */
		final ModeSelector< VertexColorMode > vertexColorModeSelector =
				new ModeSelector<>( VertexColorMode.values(), VertexColorMode.tooltips() );
		final JTextArea ta1 = new JTextArea( "Read " + vertexName.toLowerCase() + "\ncolor from" );
		ta1.setFont( getFont() );
		ta1.setOpaque( false );
		ta1.setFocusable( false );
		ta1.setEditable( false );
		addToLayout( ta1, vertexColorModeSelector, c );

		/*
		 * Vertex feature.
		 */

		this.vertexFeatureSelectionPanel = new FeatureSelectionPanel();
		c.fill = GridBagConstraints.HORIZONTAL;
		addToLayout( new JLabel( "feature", JLabel.TRAILING ), vertexFeatureSelectionPanel.getPanel(), c );

		/*
		 * Vertex color map.
		 */

		final ColorMapSelector vertexColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "colormap", JLabel.TRAILING ), vertexColorMapSelector, c );

		/*
		 * Vertex feature range.
		 */

		final FeatureRangeSelector vertexFeatureRangeSelector = new FeatureRangeSelector()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void autoscale()
			{
				final FeatureProjectionId projection = mode.getVertexFeatureProjection();
				if ( null == projection )
					return;
				final double[] minMax = rangeCalculator.computeMinMax( projection );
				if ( null == minMax )
					return;
				setMinMax( minMax[ 0 ], minMax[ 1 ] );
			}

		};
		addToLayout( new JLabel( "range", JLabel.TRAILING ), vertexFeatureRangeSelector, c );

		/*
		 * Separator.
		 */

		addToLayout( new JSeparator(), c );

		final JPanel panelEdgeColoringTitle = new JPanel();
		final BoxLayout panelEdgeColoringTitleLayout = new BoxLayout( panelEdgeColoringTitle, BoxLayout.LINE_AXIS );
		panelEdgeColoringTitle.setLayout( panelEdgeColoringTitleLayout );
		final JLabel lbl2 = new JLabel( "Coloring " + edgeName + "s" );
		lbl2.setFont( getFont().deriveFont( Font.BOLD ) );

		final JButton buttonCopy = new JButton( "Copy from " + vertexName + " settings" );
		buttonCopy.setFont( getFont().deriveFont( getFont().getSize2D() - 2f ) );
		buttonCopy.addActionListener( e -> copyFromVertexSettings() );

		panelEdgeColoringTitle.add( lbl2 );
		panelEdgeColoringTitle.add( Box.createHorizontalGlue() );
		panelEdgeColoringTitle.add( buttonCopy );

		addToLayout( panelEdgeColoringTitle, c );

		/*
		 * Edge color mode.
		 */
		final ModeSelector< EdgeColorMode > edgeColorModeSelector =
				new ModeSelector<>( EdgeColorMode.values(), EdgeColorMode.tooltips() );
		final JTextArea ta2 = new JTextArea( "Read " + edgeName.toLowerCase() + "\ncolor from" );
		ta2.setFont( getFont() );
		ta2.setEditable( false );
		ta2.setOpaque( false );
		ta2.setFocusable( false );
		addToLayout( ta2, edgeColorModeSelector, c );

		/*
		 * Edge feature.
		 */

		this.edgeFeatureSelectionPanel = new FeatureSelectionPanel();
		addToLayout( new JLabel( "feature", JLabel.TRAILING ), edgeFeatureSelectionPanel.getPanel(), c );

		/*
		 * Edge color map.
		 */

		final ColorMapSelector edgeColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "colormap", JLabel.TRAILING ), edgeColorMapSelector, c );

		/*
		 * Edge feature range.
		 */

		final FeatureRangeSelector edgeFeatureRangeSelector = new FeatureRangeSelector()
		{

			private static final long serialVersionUID = 1L;

			@Override
			public void autoscale()
			{
				final FeatureProjectionId projection = mode.getEdgeFeatureProjection();
				if ( null == projection )
					return;
				final double[] minMax = rangeCalculator.computeMinMax( projection );
				if ( null == minMax )
					return;
				setMinMax( minMax[ 0 ], minMax[ 1 ] );
			}
		};
		addToLayout( new JLabel( "range", JLabel.TRAILING ), edgeFeatureRangeSelector, c );

		/*
		 * Last separator.
		 */

		c.weighty = 1.;
		addToLayout( new JSeparator(), c );

		/*
		 * Tweak the JLabels. Totally not portable.
		 */

		modifyLabels( this, "vertex", vertexName.toLowerCase() );
		modifyLabels( this, "edge", edgeName.toLowerCase() );
		modifyLabels( this, "vertices", vertexName.toLowerCase() + 's' );
		modifyLabels( this, "Vertex", vertexName );
		modifyLabels( this, "Vertices", vertexName + 's' );
		modifyLabels( this, "Edge", edgeName );

		/*
		 * Here comes the great dance of listeners.
		 *
		 * First the listener that listens to changes in the GUI and forward
		 * them to the model.
		 */

		/*
		 * Listen to changes in vertex color mode and hide panels or not.
		 */

		final Consumer< VertexColorMode > vertexColorModeListener = vcm -> {
			final boolean visible = !vcm.equals( VertexColorMode.NONE );
			vertexColorMapSelector.setVisible( visible );
			vertexFeatureRangeSelector.setVisible( visible );
			vertexFeatureSelectionPanel.getPanel().setVisible( visible );
			vertexFeatureSelectionPanel.setAvailableFeatureProjections( visible ? availableFeatureProjections : null,
					vcm.targetType() );

			if ( doForwardToMode )
				mode.setVertexColorMode( vcm );
		};
		vertexColorModeSelector.listeners().add( vertexColorModeListener );

		/*
		 * Listen to changes in edge color mode and hide panels or not. Then
		 * forward possible new feature specs to feature selection panel.
		 */

		final Consumer< EdgeColorMode > edgeColorModeListener = ecm -> {
			final boolean visible = !ecm.equals( EdgeColorMode.NONE );
			edgeColorMapSelector.setVisible( visible );
			edgeFeatureRangeSelector.setVisible( visible );
			edgeFeatureSelectionPanel.getPanel().setVisible( visible );
			edgeFeatureSelectionPanel.setAvailableFeatureProjections( visible ? availableFeatureProjections : null,
					ecm.targetType() );

			if ( doForwardToMode )
				mode.setEdgeColorMode( ecm );
		};
		edgeColorModeSelector.listeners().add( edgeColorModeListener );

		/*
		 * Listen to changes in the color map selection and forward it to mode.
		 */

		edgeColorMapSelector.listeners().add( cm -> {
			if ( doForwardToMode )
				mode.setEdgeColorMap( cm );
		} );

		vertexColorMapSelector.listeners().add( cm -> {
			if ( doForwardToMode )
				mode.setVertexColorMap( cm );
		} );

		/*
		 * Listen to changes in the feature value ranges and forward it to mode.
		 */

		vertexFeatureRangeSelector.listeners().add( mm -> {
			if ( doForwardToMode )
				mode.setVertexRange( mm[ 0 ], mm[ 1 ] );
		} );

		edgeFeatureRangeSelector.listeners().add( mm -> {
			if ( doForwardToMode )
				mode.setEdgeRange( mm[ 0 ], mm[ 1 ] );
		} );

		/*
		 * Listen to changes in the feature selection panels and forward it to
		 * the mode.
		 */

		// Listen to changes in the vertex feature panel and forward it to the
		// mode.
		vertexFeatureSelectionPanel.updateListeners().add( () -> {
			if ( doForwardToMode )
				mode.setVertexFeatureProjection( vertexFeatureSelectionPanel.getSelection() );
		} );

		// Listen to changes in the vertex feature panel and forward it to the
		// mode.
		edgeFeatureSelectionPanel.updateListeners().add( () -> {
			if ( doForwardToMode )
				mode.setEdgeFeatureProjection( edgeFeatureSelectionPanel.getSelection() );
		} );

		/*
		 * Listen to changes in the mode and forward them to the view.
		 */

		final FeatureColorMode.UpdateListener l = new FeatureColorMode.UpdateListener()
		{
			@Override
			public void featureColorModeChanged()
			{
				doForwardToMode = false;
				vertexColorModeSelector.setSelected( mode.getVertexColorMode() );
				vertexColorMapSelector.setColorMap( mode.getVertexColorMap() );
				vertexFeatureRangeSelector.setMinMax( mode.getVertexRangeMin(), mode.getVertexRangeMax() );
				edgeColorModeSelector.setSelected( mode.getEdgeColorMode() );
				edgeColorMapSelector.setColorMap( mode.getEdgeColorMap() );
				edgeFeatureRangeSelector.setMinMax( mode.getEdgeRangeMin(), mode.getEdgeRangeMax() );
				vertexColorModeListener.accept( mode.getVertexColorMode() );
				edgeColorModeListener.accept( mode.getEdgeColorMode() );
				vertexFeatureSelectionPanel.setSelection( mode.getVertexFeatureProjection() );
				edgeFeatureSelectionPanel.setSelection( mode.getEdgeFeatureProjection() );
				doForwardToMode = true;
			}
		};
		update = l::featureColorModeChanged;
		mode.updateListeners().add( l );
	}

	private void copyFromVertexSettings()
	{
		switch ( mode.getVertexColorMode() )
		{
		case BRANCH_VERTEX:
			mode.setEdgeColorMode( EdgeColorMode.TARGET_BRANCH_VERTEX );
			break;
		case INCOMING_BRANCH_EDGE:
			mode.setEdgeColorMode( EdgeColorMode.INCOMING_BRANCH_EDGE );
			break;
		case INCOMING_EDGE:
			mode.setEdgeColorMode( EdgeColorMode.EDGE );
			break;
		case NONE:
			mode.setEdgeColorMode( EdgeColorMode.NONE );
			break;
		case OUTGOING_BRANCH_EDGE:
			mode.setEdgeColorMode( EdgeColorMode.OUTGOING_BRANCH_EDGE );
			break;
		case OUTGOING_EDGE:
			mode.setEdgeColorMode( EdgeColorMode.EDGE );
			break;
		case VERTEX:
			mode.setEdgeColorMode( EdgeColorMode.SOURCE_VERTEX );
			break;
		default:
			throw new IllegalArgumentException( "Unknown vertex color mode: " + mode.getVertexColorMode() );
		}
		mode.setEdgeFeatureProjection( mode.getVertexFeatureProjection() );
		mode.setEdgeColorMap( mode.getVertexColorMap() );
		mode.setEdgeRange( mode.getVertexRangeMin(), mode.getVertexRangeMax() );
	}

	private void addToLayout( final JComponent comp1, final JComponent comp2, final GridBagConstraints c )
	{
		c.gridx = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		add( comp1, c );
		c.gridx++;

		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		add( comp2, c );
		c.gridy++;
	}

	private void addToLayout( final JComponent comp1, final GridBagConstraints c )
	{
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		add( comp1, c );
		c.gridy++;
		c.gridwidth = 1;
	}

	private void modifyLabels( final Container parent, final CharSequence oldstr, final CharSequence newstr )
	{
		for ( final Component c : parent.getComponents() )
		{
			if ( c instanceof JLabel )
			{
				final JLabel lbl = ( JLabel ) c;
				final String str = lbl.getText();
				lbl.setText( str.replace( oldstr, newstr ) );
			}
			else if ( c instanceof AbstractButton )
			{
				final AbstractButton lbl = ( AbstractButton ) c;
				final String str = lbl.getText();
				lbl.setText( str.replace( oldstr, newstr ) );
			}
			else if ( c instanceof Container )
			{
				modifyLabels( ( Container ) c, oldstr, newstr );
			}
			if ( c instanceof JComponent )
			{
				final JComponent jc = ( JComponent ) c;
				final String str = jc.getToolTipText();
				if ( str != null )
					jc.setToolTipText( str.replace( oldstr, newstr ) );
			}
		}
	}

	public void setAvailableFeatureProjections( final AvailableFeatureProjections featureSpecs )
	{
		this.availableFeatureProjections = featureSpecs;

		// Pass to lists.
		doForwardToMode = false;
		vertexFeatureSelectionPanel.setAvailableFeatureProjections( featureSpecs,
				mode.getVertexColorMode().targetType() );
		vertexFeatureSelectionPanel.setSelection( mode.getVertexFeatureProjection() );
		edgeFeatureSelectionPanel.setAvailableFeatureProjections( featureSpecs, mode.getEdgeColorMode().targetType() );
		edgeFeatureSelectionPanel.setSelection( mode.getEdgeFeatureProjection() );
		doForwardToMode = true;

		update.run();
	}
}
