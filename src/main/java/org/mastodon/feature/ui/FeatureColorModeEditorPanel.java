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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.mastodon.ui.coloring.ColorMap;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.FeatureRangeCalculator;
import org.mastodon.ui.coloring.feature.FeatureColorMode.EdgeColorMode;
import org.mastodon.ui.coloring.feature.FeatureColorMode.VertexColorMode;

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
			final FeatureRangeCalculator rangeCalculator
			)
	{
		this.mode = mode;

		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 45, 45, 45, 45, 45, 10, 45, 45, 45, 45, 45 };

		setLayout( layout );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;

		/*
		 * Vertex color mode.
		 */
		final ModeSelector< VertexColorMode > vertexColorModeSelector = new ModeSelector<>( VertexColorMode.values() );
		addToLayout( new JLabel( "vertex color mode", JLabel.TRAILING ), vertexColorModeSelector, c );

		/*
		 * Vertex feature.
		 */

		this.vertexFeatureSelectionPanel = new FeatureSelectionPanel();
		addToLayout( new JLabel( "vertex feature", JLabel.TRAILING ), vertexFeatureSelectionPanel.getPanel(), c );


		/*
		 * Vertex color map.
		 */

		final ColorMapSelector vertexColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "vertex colormap", JLabel.TRAILING ), vertexColorMapSelector, c );

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
		addToLayout( new JLabel( "vertex range", JLabel.TRAILING ), vertexFeatureRangeSelector, c );

		/*
		 * Separator.
		 */

		c.gridx = 0;
		c.weightx = 1.0;
		c.gridwidth = 2;
		add( new JSeparator(), c );
		c.gridy++;
		c.gridwidth = 1;

		/*
		 * Edge color mode.
		 */
		final ModeSelector< EdgeColorMode > edgeColorModeSelector = new ModeSelector<>( EdgeColorMode.values() );
		addToLayout( new JLabel( "edge color mode", JLabel.TRAILING ), edgeColorModeSelector, c );

		/*
		 * Edge feature.
		 */

		this.edgeFeatureSelectionPanel = new FeatureSelectionPanel();
		addToLayout( new JLabel( "edge feature", JLabel.TRAILING ), edgeFeatureSelectionPanel.getPanel(), c );

		/*
		 * Edge color map.
		 */

		final ColorMapSelector edgeColorMapSelector = new ColorMapSelector( ColorMap.getColorMapNames() );
		addToLayout( new JLabel( "edge colormap", JLabel.TRAILING ), edgeColorMapSelector, c );

		/*
		 * Edge feature range.
		 */

		final FeatureRangeSelector edgeFeatureRangeSelector = new FeatureRangeSelector()
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
		addToLayout( new JLabel( "edge range", JLabel.TRAILING ), edgeFeatureRangeSelector, c );

		/*
		 * Here comes the great dance of listeners.
		 *
		 * First the listener that listens to changes in the GUI and forward them to the
		 * model.
		 */

		/*
		 * Listen to changes in vertex color mode and hide panels or not.
		 */

		final Consumer< VertexColorMode > vertexColorModeListener = vcm -> {
			final boolean visible = !vcm.equals( VertexColorMode.NONE );
			vertexColorMapSelector.setVisible( visible );
			vertexFeatureRangeSelector.setVisible( visible );
			vertexFeatureSelectionPanel.getPanel().setVisible( visible );
			vertexFeatureSelectionPanel.setAvailableFeatureProjections( visible ? availableFeatureProjections : null, vcm.targetType() );

			if ( doForwardToMode )
				mode.setVertexColorMode( vcm );
		};
		vertexColorModeSelector.listeners().add( vertexColorModeListener );

		/*
		 * Listen to changes in edge color mode and hide panels or not. Then forward
		 * possible new feature specs to feature selection panel.
		 */

		final Consumer< EdgeColorMode > edgeColorModeListener = ecm -> {
			final boolean visible = !ecm.equals( EdgeColorMode.NONE );
			edgeColorMapSelector.setVisible( visible );
			edgeFeatureRangeSelector.setVisible( visible );
			edgeFeatureSelectionPanel.getPanel().setVisible( visible );
			edgeFeatureSelectionPanel.setAvailableFeatureProjections( visible ? availableFeatureProjections : null, ecm.targetType() );

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
		 * Listen to changes in the feature selection panels and forward it to the mode.
		 */

		// Listen to changes in the vertex feature panel and forward it to the mode.
		vertexFeatureSelectionPanel.updateListeners().add( () -> {
			if ( doForwardToMode )
				mode.setVertexFeatureProjection( vertexFeatureSelectionPanel.getSelection() );
		} );

		// Listen to changes in the vertex feature panel and forward it to the mode.
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

	private void addToLayout( final JComponent comp1, final JComponent comp2, final GridBagConstraints c )
	{
		c.gridx = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.weightx = 0.0;
		add( comp1, c );
		c.gridx++;

		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 1.0;
		add( comp2, c );
		c.gridy++;
	}

	public void setAvailableFeatureProjections( final AvailableFeatureProjections featureSpecs )
	{
		this.availableFeatureProjections = featureSpecs;

		// Pass to lists.
		doForwardToMode = false;
		vertexFeatureSelectionPanel.setAvailableFeatureProjections( featureSpecs, mode.getVertexColorMode().targetType() );
		vertexFeatureSelectionPanel.setSelection( mode.getVertexFeatureProjection() );
		edgeFeatureSelectionPanel.setAvailableFeatureProjections( featureSpecs, mode.getEdgeColorMode().targetType() );
		edgeFeatureSelectionPanel.setSelection( mode.getEdgeFeatureProjection() );
		doForwardToMode = true;

		update.run();
	}
}
