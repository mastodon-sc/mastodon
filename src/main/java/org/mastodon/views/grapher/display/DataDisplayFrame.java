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

import static org.mastodon.app.MastodonIcons.FEATURES_ICON;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.util.FeatureUtils;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.scijava.ui.behaviour.MouseAndKeyHandler;

public class DataDisplayFrame< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > > extends ViewFrame
{
	private static final long serialVersionUID = 1L;

	private final DataDisplayPanel< V, E > dataDisplayPanel;

	private final GrapherSidePanel sidePanel;

	private final FeatureModel featureModel;

	public DataDisplayFrame(
			final DataGraph< V, E > graph,
			final FeatureModel featureModel,
			final int nSources,
			final DataGraphLayout< V, E > layout,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final FocusModel< DataVertex > focus,
			final SelectionModel< DataVertex, DataEdge > selection,
			final NavigationHandler< DataVertex, DataEdge > navigation,
			final UndoPointMarker undoPointMarker,
			final GroupHandle groupHandle,
			final ContextChooser< V > contextChooser,
			final DataDisplayOptions< DataVertex, DataEdge > optional )
	{
		super( "Grapher" );

		/*
		 * Plot panel.
		 */

		dataDisplayPanel = new DataDisplayPanel<>(
				graph,
				layout,
				highlight,
				focus,
				selection,
				navigation,
				optional );

		/*
		 * Get the classes of the model vertices and edges. We need them to
		 * query the feature model.
		 */

		final Class< V > vertexClass = graph.getGraphIdBimap().vertexIdBimap().getRefClass();
		final Class< E > edgeClass = graph.getGraphIdBimap().edgeIdBimap().getRefClass();

		this.featureModel = featureModel;

		/*
		 * Side panel.
		 */

		sidePanel = new GrapherSidePanel( nSources, contextChooser );
		sidePanel.getBtnPlot().addActionListener( e -> plot( false ) );

		final FeatureModelListener featureModelListener = () -> sidePanel.setFeatures(
				FeatureUtils.collectFeatureMap( featureModel, vertexClass ),
				FeatureUtils.collectFeatureMap( featureModel, edgeClass ) );
		featureModel.listeners().add( featureModelListener );
		featureModelListener.featureModelChanged();

		/*
		 * Main panel is a split pane.
		 */

		final JSplitPane mainPanel = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
				sidePanel, dataDisplayPanel );
		mainPanel.setOneTouchExpandable( true );
		mainPanel.setBorder( null );
		mainPanel.setDividerLocation( 250 );

		add( mainPanel, BorderLayout.CENTER );

		/*
		 * Top settings bar.
		 */

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		//		final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		//		settingsPanel.add( contextChooserPanel );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				dataDisplayPanel.stop();
			}
		} );

		SwingUtilities.replaceUIActionMap( dataDisplayPanel, keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( dataDisplayPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
				keybindings.getConcatenatedInputMap() );

		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		mouseAndKeyHandler.setKeypressManager( optional.values.getKeyPressedManager(), dataDisplayPanel.getDisplay() );
		dataDisplayPanel.getDisplay().addHandler( mouseAndKeyHandler );
		setIconImages( FEATURES_ICON );
		
		final int x = optional.values.getX();
		final int y = optional.values.getY();
		if ( x <= 0 && y <= 0 )
			setLocationRelativeTo( null );
		else
			setLocation( x, y );
	}

	public DataDisplayPanel< V, E > getDataDisplayPanel()
	{
		return dataDisplayPanel;
	}

	public GrapherSidePanel getVertexSidePanel()
	{
		return sidePanel;
	}

	/**
	 * Plots the grapher view.
	 * @param keepCurrentScreenTransform if {@code true}, the current screen transform is kept, otherwise it zoomed out fully.
	 */
	public void plot( boolean keepCurrentScreenTransform )
	{
		dataDisplayPanel.plot( sidePanel.getGraphConfig(), featureModel,
				keepCurrentScreenTransform ? dataDisplayPanel.getScreenTransform().get() : null );
	}

	public void plot( final ScreenTransform transform )
	{
		dataDisplayPanel.plot( sidePanel.getGraphConfig(), featureModel, transform );
	}
}
