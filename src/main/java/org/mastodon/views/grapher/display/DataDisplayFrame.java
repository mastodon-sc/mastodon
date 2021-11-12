/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.util.FeatureUtils;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataGraphLayout;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.scijava.ui.behaviour.MouseAndKeyHandler;

public class DataDisplayFrame< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > > extends ViewFrame
{
	private static final long serialVersionUID = 1L;

	private final DataDisplayPanel dataDisplayPanel;

	private final DataGraph< V, E > viewGraph;

	private final SelectionModel< DataVertex, DataEdge > selectionModel;

	private final GrapherSidePanel vertexSidePanel;

	private final GrapherSidePanel edgeSidePanel;

	public DataDisplayFrame(
			final DataGraph< V, E > graph,
			final FeatureModel featureModel,
			final int nSources,
			final DataGraphLayout< V, E > layout,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final FocusModel< DataVertex, DataEdge > focus,
			final SelectionModel< DataVertex, DataEdge > selection,
			final NavigationHandler< DataVertex, DataEdge > navigation,
			final UndoPointMarker undoPointMarker,
			final GroupHandle groupHandle,
			final ContextChooser< V > contextChooser,
			final DataDisplayOptions optional )
	{
		super( "Data" );
		this.viewGraph = graph;
		this.selectionModel = selection;

		/*
		 * Get the classes of the model vertices and edges. We need them to
		 * query the feature model.
		 */

		final Class< V > vertexClass = graph.getGraphIdBimap().vertexIdBimap().getRefClass();
		final Class< E > edgeClass = graph.getGraphIdBimap().edgeIdBimap().getRefClass();

		/*
		 * Side panel.
		 */

		final JTabbedPane sidePanel = new JTabbedPane( JTabbedPane.TOP );

		vertexSidePanel = new GrapherSidePanel( nSources );
		edgeSidePanel = new GrapherSidePanel( nSources );
		sidePanel.add( vertexClass.getSimpleName(), vertexSidePanel );
		sidePanel.add( edgeClass.getSimpleName(), edgeSidePanel );

		vertexSidePanel.btnPlot.addActionListener( e -> plotVertices( vertexSidePanel.getGraphConfig(), featureModel ) );
		edgeSidePanel.btnPlot.addActionListener( e -> plotEdges( edgeSidePanel.getGraphConfig(), featureModel ) );

		final FeatureModelListener featureModelListener = () -> {
			vertexSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, vertexClass ) );
			edgeSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, edgeClass ) );
		};
		featureModel.listeners().add( featureModelListener );
		featureModelListener.featureModelChanged();
		sidePanel.setSelectedIndex( 0 );
		sidePanel.setMinimumSize( new Dimension( 100, 150 ) );

		/*
		 * Plot panel.
		 */

		dataDisplayPanel = new DataDisplayPanel(
				graph,
				layout,
				highlight,
				focus,
				selection,
				navigation,
				optional );

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

		final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		settingsPanel.add( contextChooserPanel );

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
		SwingUtilities.replaceUIInputMap( dataDisplayPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		mouseAndKeyHandler.setKeypressManager( optional.values.getKeyPressedManager(), dataDisplayPanel.getDisplay() );
		dataDisplayPanel.getDisplay().addHandler( mouseAndKeyHandler );
		setLocation( optional.values.getX(), optional.values.getY() );
	}

	public DataDisplayPanel getDataDisplayPanel()
	{
		return dataDisplayPanel;
	}

	public GrapherSidePanel getVertexSidePanel()
	{
		return vertexSidePanel;
	}

	public GrapherSidePanel getEdgeSidePanel()
	{
		return edgeSidePanel;
	}

	private void plotVertices( final GraphConfig gc, final FeatureModel featureModel )
	{
		final FeatureProjection< V > xproj = gc.getXFeature().getProjection( featureModel );
		final FeatureProjection< V > yproj = gc.getYFeature().getProjection( featureModel );
		final RefSet< DataVertex > vertices = buildVerticesList(
				selectionModel.getSelectedVertices(),
				selectionModel.getSelectedEdges(),
				gc.graphTrackOfSelection() );

		@SuppressWarnings( "unchecked" )
		final DataGraphLayout< V, E > layout = ( DataGraphLayout< V, E > ) dataDisplayPanel.getDataGraphLayout();
		layout.setXFeature( xproj );
		layout.setYFeature( yproj );
		layout.setPaintEdges( gc.drawConnected() );
		if ( !gc.keepCurrent() )
			layout.setVertices( vertices );

		String xlabel = gc.getXFeature().toString();
		final String xunits = xproj.units();
		if (!xunits.isEmpty())
			xlabel += " (" + xunits + ")";
		dataDisplayPanel.getGraphOverlay().setXLabel( xlabel );

		String ylabel = gc.getYFeature().toString();
		final String yunits = yproj.units();
		if ( !yunits.isEmpty() )
			ylabel += " (" + yunits + ")";
		dataDisplayPanel.getGraphOverlay().setYLabel( ylabel );

		dataDisplayPanel.graphChanged();
	}

	private void plotEdges( final GraphConfig graphConfig, final FeatureModel featureModel )
	{
		// TODO Auto-generated method stub
		System.out.println( "plot edges" ); // DEBUG
	}

	private RefSet< DataVertex > buildVerticesList(
			final RefSet< DataVertex > selectedVertices,
			final RefSet< DataEdge > selectedEdges,
			final boolean includeTrack )
	{
		if ( !includeTrack )
		{
			final RefSet< DataVertex > set = RefCollections.createRefSet( viewGraph.vertices(), selectedVertices.size() );
			set.addAll( selectedVertices );
			return set;
		}

		final RefSet< DataVertex > toSearch = RefCollections.createRefSet( viewGraph.vertices() );
		toSearch.addAll( selectedVertices );
		final DataVertex ref = viewGraph.vertexRef();
		for ( final DataEdge e : selectedEdges )
		{
			toSearch.add( e.getSource( ref ) );
			toSearch.add( e.getTarget( ref ) );
		}
		viewGraph.releaseRef( ref );

		// Prepare the iterator.
		final RefSet< DataVertex > set = RefCollections.createRefSet( viewGraph.vertices() );
		final DepthFirstSearch< DataVertex, DataEdge > search = new DepthFirstSearch<>( viewGraph, SearchDirection.UNDIRECTED );
		search.setTraversalListener( new SearchListener< DataVertex, DataEdge, DepthFirstSearch< DataVertex, DataEdge > >()
		{
			@Override
			public void processVertexLate( final DataVertex vertex, final DepthFirstSearch< DataVertex, DataEdge > search )
			{}

			@Override
			public void processVertexEarly( final DataVertex vertex, final DepthFirstSearch< DataVertex, DataEdge > search )
			{
				set.add( vertex );
			}

			@Override
			public void processEdge( final DataEdge edge, final DataVertex from, final DataVertex to, final DepthFirstSearch< DataVertex, DataEdge > search )
			{}

			@Override
			public void crossComponent( final DataVertex from, final DataVertex to, final DepthFirstSearch< DataVertex, DataEdge > search )
			{}
		} );

		for ( final DataVertex v : toSearch )
			if ( !set.contains( v ) )
				search.start( v );
		return set;
	}
}
