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
package org.mastodon.views.grapher;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.mastodon.app.MastodonAppModel;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.AbstractSpot;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.util.FeatureUtils;
import org.scijava.ui.behaviour.util.InputActionBindings;

public class GrapherViewFrame< M extends MastodonAppModel< ?, V, E >, VG extends ViewGraph< V, E, V, E >, V extends AbstractSpot< V, E, ?, ?, ? >, E extends AbstractListenableEdge< E, V, ?, ? > >
		extends ViewFrame
		implements
		SelectionListener,
		FocusListener,
		HighlightListener,
		FeatureModelListener
{

	private static final long serialVersionUID = 1L;

	private final SelectionModel< V, E > selectionModel;

	private final FocusModel< V, E > focusModel;

	private final HighlightModel< V, E > highlightModel;

	private final FeatureModel featureModel;

	private final Class< ? extends AbstractSpot > vertexClass;

	private final Class< ? extends AbstractListenableEdge > edgeClass;

	private final VG viewGraph;

	private final String spaceUnits;

	private final String timeUnits;

	private final JPanel graphPanel;

	public GrapherViewFrame(
			final M appModel,
			final VG viewGraph,
			final FeatureModel featureModel,
			final GroupHandle groupHandle,
			final NavigationHandler< V, E > navigationHandler,
			final GraphColorGenerator< V, E > coloring,
			final String spaceUnits,
			final String timeUnits )
	{
		super( "Grapher" );
		this.viewGraph = viewGraph;
		this.featureModel = featureModel;
		this.spaceUnits = spaceUnits;
		this.timeUnits = timeUnits;
		this.selectionModel = appModel.getSelectionModel();
		this.focusModel = appModel.getFocusModel();
		this.highlightModel = appModel.getHighlightModel();
		this.vertexClass = viewGraph.vertexRef().getClass();
		this.edgeClass = viewGraph.edgeRef().getClass();

		/*
		 * Settings panel.
		 */

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		add( settingsPanel, BorderLayout.NORTH );

		navigationHandler.listeners().add( new NavigationListener< V, E >()
		{

			@Override
			public void navigateToVertex( final V vertex )
			{
				// TODO
				System.out.println( "Navigate to " + vertex ); // DEBUG
			}

			@Override
			public void navigateToEdge( final E edge )
			{
				System.out.println( "Navigate to " + edge ); // DEBUG
			}

		} );

		/*
		 * Graph panel.
		 */

		graphPanel = new JPanel();
		graphPanel.setLayout( new BorderLayout() );

		/*
		 * Side panel.
		 */

		final JTabbedPane sidePanel = new JTabbedPane( JTabbedPane.TOP );

		final GrapherSidePanel vertexSidePanel = new GrapherSidePanel();
		final GrapherSidePanel edgeSidePanel = new GrapherSidePanel();
		sidePanel.add( vertexClass.getSimpleName(), vertexSidePanel );
		sidePanel.add( edgeClass.getSimpleName(), edgeSidePanel );

		vertexSidePanel.btnPlot.addActionListener( e -> plotVertices( vertexSidePanel.getGraphConfig() ) );
		edgeSidePanel.btnPlot.addActionListener( e -> plotEdges( edgeSidePanel.getGraphConfig() ) );

		final FeatureModelListener featureModelListener = () -> {
			vertexSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, vertexClass ) );
			edgeSidePanel.setFeatures( FeatureUtils.collectFeatureMap( featureModel, edgeClass ) );
		};
		featureModel.listeners().add( featureModelListener );
		featureModelListener.featureModelChanged();
		sidePanel.setSelectedIndex( 0 );
		sidePanel.setMinimumSize( new Dimension( 100, 150 ) );

		/*
		 * Main panel is a split pane.
		 */

		final JSplitPane mainPanel = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
				sidePanel, graphPanel );
		mainPanel.setOneTouchExpandable( true );
		mainPanel.setBorder( null );
		mainPanel.setDividerLocation( 250 );

		add( mainPanel );
	}

	private void plotVertices( final GraphConfig graphConfig )
	{
		final RefList< V > list = buildVerticesList(
				selectionModel.getSelectedVertices(),
				selectionModel.getSelectedEdges(),
				graphConfig.graphTrackOfSelection() );
		plot( graphConfig, list );
	}

	private void plotEdges( final GraphConfig graphConfig )
	{
		final RefList< E > list = buildEdgesList(
				selectionModel.getSelectedVertices(),
				selectionModel.getSelectedEdges(),
				graphConfig.graphTrackOfSelection() );
		plot( graphConfig, list );
	}

	private < O > void plot( final GraphConfig graphConfig, final RefList< O > items )
	{
		final EverythingDisablerAndReenabler enabler = new EverythingDisablerAndReenabler( this, new Class[] { JLabel.class } );
		enabler.disable();
		new Thread( "Mastodon grapher thread" )
		{
			@Override
			public void run()
			{
				try
				{
					graphPanel.removeAll();
					if ( items.isEmpty() )
					{
						graphPanel.add( new JLabel( "Selection is empty", JLabel.CENTER ),
								BorderLayout.CENTER );
					}
					else
					{
						final Grapher grapher = new Grapher( featureModel, spaceUnits, timeUnits );
						final JScrollPane scrollPane = grapher.graph( graphConfig, items );
						graphPanel.add( scrollPane, BorderLayout.CENTER );
					}
					graphPanel.revalidate();
				}
				finally
				{
					enabler.reenable();
				}
			};
		}.start();
	}

	private RefList< V > buildVerticesList(
			final RefSet< V > selectedVertices,
			final RefSet< E > selectedEdges,
			final boolean includeTrack )
	{
		if ( !includeTrack )
		{
			final RefList< V > list = RefCollections.createRefList( viewGraph.vertices(), selectedVertices.size() );
			list.addAll( selectedVertices );
			return list;
		}

		final RefSet< V > toSearch = RefCollections.createRefSet( viewGraph.vertices() );
		toSearch.addAll( selectedVertices );
		final V ref = viewGraph.vertexRef();
		for ( final E e : selectedEdges )
		{
			toSearch.add( e.getSource( ref ) );
			toSearch.add( e.getTarget( ref ) );
		}
		viewGraph.releaseRef( ref );

		// Prepare the iterator.
		final RefSet< V > set = RefCollections.createRefSet( viewGraph.vertices() );
		final DepthFirstSearch< V, E > search = new DepthFirstSearch<>( viewGraph, SearchDirection.UNDIRECTED );
		search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
		{
			@Override
			public void processVertexLate( final V vertex, final DepthFirstSearch< V, E > search )
			{}

			@Override
			public void processVertexEarly( final V vertex, final DepthFirstSearch< V, E > search )
			{
				set.add( vertex );
			}

			@Override
			public void processEdge( final E edge, final V from, final V to, final DepthFirstSearch< V, E > search )
			{}

			@Override
			public void crossComponent( final V from, final V to, final DepthFirstSearch< V, E > search )
			{}
		} );

		for ( final V v : toSearch )
			if ( !set.contains( v ) )
				search.start( v );

		final RefList< V > list = RefCollections.createRefList( viewGraph.vertices(), set.size() );
		list.addAll( selectedVertices );
		return list;
	}

	private RefList< E > buildEdgesList(
			final RefSet< V > selectedVertices,
			final RefSet< E > selectedEdges,
			final boolean includeTrack )
	{
		if ( !includeTrack )
		{
			final RefList< E > list = RefCollections.createRefList( viewGraph.edges(), selectedEdges.size() );
			list.addAll( selectedEdges );
			return list;
		}

		final RefSet< V > toSearch = RefCollections.createRefSet( viewGraph.vertices() );
		toSearch.addAll( selectedVertices );
		final V ref = viewGraph.vertexRef();
		for ( final E e : selectedEdges )
		{
			toSearch.add( e.getSource( ref ) );
			toSearch.add( e.getTarget( ref ) );
		}
		viewGraph.releaseRef( ref );

		// Prepare the iterator.
		final RefSet< E > edgeSet = RefCollections.createRefSet( viewGraph.edges() );
		final RefSet< V > vertexSet = RefCollections.createRefSet( viewGraph.vertices() );
		final DepthFirstSearch< V, E > search = new DepthFirstSearch<>( viewGraph, SearchDirection.UNDIRECTED );
		search.setTraversalListener( new SearchListener< V, E, DepthFirstSearch< V, E > >()
		{
			@Override
			public void processVertexLate( final V vertex, final DepthFirstSearch< V, E > search )
			{}

			@Override
			public void processVertexEarly( final V vertex, final DepthFirstSearch< V, E > search )
			{
				vertexSet.add( vertex );
			}

			@Override
			public void processEdge( final E edge, final V from, final V to, final DepthFirstSearch< V, E > search )
			{
				edgeSet.add( edge );
			}

			@Override
			public void crossComponent( final V from, final V to, final DepthFirstSearch< V, E > search )
			{}
		} );

		for ( final V v : toSearch )
			if ( !vertexSet.contains( v ) )
				search.start( v );

		final RefList< E > list = RefCollections.createRefList( viewGraph.edges(), edgeSet.size() );
		list.addAll( edgeSet );
		return list;
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	@Override
	public void selectionChanged()
	{
		System.out.println( "selection changed." ); // DEBUG
	}

	@Override
	public void focusChanged()
	{
		System.out.println( "focused changed" ); // DEBUG
	}

	@Override
	public void highlightChanged()
	{
		System.out.println( "highlight changed" ); // DEBUG
	}

	@Override
	public void featureModelChanged()
	{
		System.out.println( "feature model changed." ); // DEBUG
	}
}
