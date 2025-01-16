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
package org.mastodon.views.grapher.display.style;

import static org.mastodon.app.ui.StyleElements.booleanElement;
import static org.mastodon.app.ui.StyleElements.colorElement;
import static org.mastodon.app.ui.StyleElements.doubleElement;
import static org.mastodon.app.ui.StyleElements.enumElement;
import static org.mastodon.app.ui.StyleElements.linkedCheckBox;
import static org.mastodon.app.ui.StyleElements.linkedColorButton;
import static org.mastodon.app.ui.StyleElements.linkedComboBoxEnumSelector;
import static org.mastodon.app.ui.StyleElements.linkedSliderPanel;
import static org.mastodon.app.ui.StyleElements.separator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.app.ui.StyleElements.BooleanElement;
import org.mastodon.app.ui.StyleElements.ColorElement;
import org.mastodon.app.ui.StyleElements.DoubleElement;
import org.mastodon.app.ui.StyleElements.EnumElement;
import org.mastodon.app.ui.StyleElements.Separator;
import org.mastodon.app.ui.StyleElements.StyleElement;
import org.mastodon.app.ui.StyleElements.StyleElementVisitor;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.model.DefaultFocusModel;
import org.mastodon.model.DefaultHighlightModel;
import org.mastodon.model.DefaultNavigationHandler;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataEdgeBimap;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.DataVertexBimap;
import org.mastodon.views.grapher.display.DataDisplayOptions;
import org.mastodon.views.grapher.display.DataDisplayPanel;
import org.mastodon.views.grapher.display.PaintGraph.VertexDrawShape;
import org.mastodon.views.trackscheme.display.style.dummygraph.DummyEdge;
import org.mastodon.views.trackscheme.display.style.dummygraph.DummyGraph;
import org.mastodon.views.trackscheme.display.style.dummygraph.DummyVertex;

import bdv.tools.brightness.SliderPanelDouble;

public class DataDisplayStyleEditorPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private final JColorChooser colorChooser;

	private final List< StyleElement > styleElements;

	public DataDisplayStyleEditorPanel( final DataDisplayStyle style )
	{
		super( new BorderLayout() );

		/*
		 * Preview panel.
		 */

		final DummyGraph.Examples ex = DummyGraph.Examples.DIVIDING_CELL;
		final DummyGraph example = ex.getGraph();
		final GraphIdBimap< DummyVertex, DummyEdge > idmap = example.getIdBimap();
		final DataGraph< DummyVertex, DummyEdge > graph = new DataGraph<>( example, idmap );
		final RefBimap< DummyVertex, DataVertex > vertexMap = new DataVertexBimap<>( graph );
		final RefBimap< DummyEdge, DataEdge > edgeMap = new DataEdgeBimap<>( graph );
		final HighlightModel< DataVertex, DataEdge > highlight =
				new HighlightModelAdapter<>( new DefaultHighlightModel<>( idmap ), vertexMap, edgeMap );
		final FocusModel< DataVertex > focus =
				new FocusModelAdapter<>( new DefaultFocusModel<>( idmap ), vertexMap, edgeMap );
		final SelectionModel< DataVertex, DataEdge > selection =
				new SelectionModelAdapter<>( ex.getSelectionModel(), vertexMap, edgeMap );
		final NavigationHandler< DataVertex, DataEdge > navigation = new DefaultNavigationHandler<>();
		final DataDisplayOptions< DataVertex, DataEdge > options = DataDisplayOptions.options();
		options.style( style );

		// Layout.

		final DataGraphLayout< DummyVertex, DummyEdge > layout = new DataGraphLayout<>( graph, selection );
		final RefSet< DataVertex > set = RefCollections.createRefSet( graph.vertices(), graph.vertices().size() );
		set.addAll( graph.vertices() );
		layout.setVertices( set );
		layout.setPaintEdges( true );
		layout.setXFeatureVertex( new FeatureProjection< DummyVertex >()
		{

			private int i = 0;

			@Override
			public FeatureProjectionKey getKey()
			{
				return FeatureProjectionKey.key( new FeatureProjectionSpec( "Frame" ) );
			}

			@Override
			public boolean isSet( final DummyVertex obj )
			{
				return true;
			}

			@Override
			public double value( final DummyVertex obj )
			{
				final String label = obj.getLabel();
				if ( label.endsWith( "C" ) )
					return obj.getTimepoint() + i++ / 10.;
				return obj.getTimepoint();
			}

			@Override
			public String units()
			{
				return "";
			}
		} );
		final Random ran = new Random( 0l );
		layout.setYFeatureVertex( new FeatureProjection< DummyVertex >()
		{

			@Override
			public FeatureProjectionKey getKey()
			{
				return FeatureProjectionKey.key( new FeatureProjectionSpec( "Intensity" ) );
			}

			@Override
			public boolean isSet( final DummyVertex obj )
			{
				return true;
			}

			@Override
			public double value( final DummyVertex obj )
			{
				final String label = obj.getLabel();
				if ( label.startsWith( "Z" ) || label.startsWith( "C" ) )
					return ran.nextDouble() * 2.;
				else if ( label.startsWith( "A" ) )
					return 3. + ran.nextDouble();
				else
					return -5. + ran.nextDouble();
			}

			@Override
			public String units()
			{
				return "counts";
			}
		} );

		final DataDisplayPanel< DummyVertex, DummyEdge > previewPanel = new DataDisplayPanel<>(
				graph, layout, highlight, focus, selection, navigation, options );
		previewPanel.getGraphOverlay().setXLabel( "Frame" );
		previewPanel.getGraphOverlay().setYLabel( "Intensity (counts)" );

		previewPanel.graphChanged();
		previewPanel.getDisplay().setFocusable( false );
		previewPanel.setPreferredSize( new Dimension( 200, 200 ) );

		/*
		 * Style edditor widgets.
		 */

		colorChooser = new JColorChooser();
		styleElements = styleElements( style );

		style.updateListeners().add( () -> {
			styleElements.forEach( StyleElement::update );
			repaint();
		} );

		final JPanel editPanel = new JPanel( new GridBagLayout() );
		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.ipadx = 0;
		c.ipady = 0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		/*
		 * Fixed color setters.
		 */

		final int numCols = 2;

		c.gridwidth = 2;
		editPanel.add( Box.createVerticalStrut( 5 ), c );
		c.gridy++;
		c.gridwidth = 1;

		c.gridx = 0;

		styleElements.forEach( element -> element.accept(
				new StyleElementVisitor()
				{
					@Override
					public void visit( final Separator separator )
					{
						if ( c.gridx != 0 )
						{
							c.gridx = 0;
							++c.gridy;
						}
						c.gridwidth = 2;
						editPanel.add( Box.createVerticalStrut( 10 ), c );
						++c.gridy;
						c.gridwidth = 1;
					}

					@Override
					public void visit( final ColorElement element )
					{
						final JButton button = linkedColorButton( element, element.getLabel(), colorChooser );
						editPanel.add( button, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}

					@Override
					public void visit( final BooleanElement element )
					{
						final JCheckBox checkbox = linkedCheckBox( element, element.getLabel() );
						editPanel.add( checkbox, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}

					@Override
					public void visit( final DoubleElement doubleElement )
					{
						if ( c.gridx != 0 )
						{
							c.gridx = 0;
							++c.gridy;
						}
						final JLabel label = new JLabel( doubleElement.getLabel() + ":" );
						editPanel.add( label, c );

						c.gridx++;
						final SliderPanelDouble slider = linkedSliderPanel( doubleElement, 3 );
						editPanel.add( slider, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}

					@Override
					public < E > void visit( final EnumElement< E > enumElement )
					{
						if ( c.gridx != 0 )
						{
							c.gridx = 0;
							++c.gridy;
						}
						final JLabel label = new JLabel( enumElement.getLabel() + ":" );
						editPanel.add( label, c );

						c.gridx++;
						final JComboBox< E > enumSelector = linkedComboBoxEnumSelector( enumElement );
						editPanel.add( enumSelector, c );
						if ( ++c.gridx == numCols )
						{
							c.gridx = 0;
							++c.gridy;
						}
					}
				} ) );

		previewPanel.setBorder( new LineBorder( Color.LIGHT_GRAY, 1 ) );
		add( previewPanel, BorderLayout.CENTER );
		add( editPanel, BorderLayout.SOUTH );
	}

	private List< StyleElement > styleElements( final DataDisplayStyle style )
	{
		return Arrays.asList(

				enumElement( "vertex shape", VertexDrawShape.values(), style::getVertexDrawShape,
						style::vertexDrawShape ),

				separator(),

				booleanElement( "auto vertex size", style::isAutoVertexSize, style::autoVertexSize ),
				doubleElement( "vertex fixed size", 1, 100, style::getVertexFixedSize, style::vertexFixedSize ),

				separator(),

				booleanElement( "draw labels", style::isDrawVertexName, style::drawVertexName ),

				separator(),

				colorElement( "vertex fill", style::getVertexFillColor, style::vertexFillColor ),
				colorElement( "selected vertex fill", style::getSelectedVertexFillColor,
						style::selectedVertexFillColor ),
				colorElement( "vertex contour", style::getVertexDrawColor, style::vertexDrawColor ),
				colorElement( "selected vertex contour", style::getSelectedVertexDrawColor,
						style::selectedVertexDrawColor ),
				colorElement( "simplified vertex fill", style::getSimplifiedVertexFillColor,
						style::simplifiedVertexFillColor ),
				colorElement( "selected simplified vertex fill", style::getSelectedSimplifiedVertexFillColor,
						style::selectedSimplifiedVertexFillColor ),
				colorElement( "edge", style::getEdgeColor, style::edgeColor ),
				colorElement( "selected edge", style::getSelectedEdgeColor, style::selectedEdgeColor ),

				separator(),

				colorElement( "axis color", style::getAxisColor, style::axisColor ),
				colorElement( "background", style::getBackgroundColor, style::backgroundColor )
		);
	}

	public static void main( final String[] args )
	{
		final DataDisplayStyle style = DataDisplayStyle.defaultStyle();
		new DataDisplayStyleEditorDialog( null, style ).setVisible( true );
	}

	public static class DataDisplayStyleEditorDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private final DataDisplayStyleEditorPanel stylePanel;

		public DataDisplayStyleEditorDialog( final JDialog dialog, final DataDisplayStyle style )
		{
			super( dialog, "Data graph style editor", false );

			stylePanel = new DataDisplayStyleEditorPanel( style );

			getContentPane().add( stylePanel, BorderLayout.CENTER );

			final ActionMap am = getRootPane().getActionMap();
			final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
			final Object hideKey = new Object();
			final Action hideAction = new AbstractAction()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{
					setVisible( false );
				}
			};
			im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
			am.put( hideKey, hideAction );

			pack();
			setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		}
	}
}
