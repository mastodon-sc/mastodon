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
package org.mastodon.views.table;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mastodon.RefPool;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModel.FeatureModelListener;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.ObjTags;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.coloring.ColorGenerator;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.util.FeatureUtils;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextListener;

public class TableViewFrame<
		V extends Vertex< E >, 
		E extends Edge< V > >
	extends ViewFrame
	implements
		SelectionListener,
		FocusListener,
		HighlightListener,
		TagSetModel.TagSetModelListener,
		FeatureModelListener,
		ContextListener< V >
{

	private static final long serialVersionUID = 1L;

	private final SelectionModel< V, E > selectionModel;

	private final FocusModel< V, E > focusModel;

	private final HighlightModel< V, E > highlightModel;

	private final FeatureModel featureModel;

	private final TagSetModel< V, E > tagSetModel;

	private final FeatureTagTablePanel< V > vertexTable;

	private final FeatureTagTablePanel< E > edgeTable;

	private boolean ignoreTableSelectionChange = false;

	private boolean ignoreSelectionChange = false;

	private final V vref;

	private final E eref;

	/**
	 * If <code>true</code>, the selection in the {@link JTable}s will be used
	 * to update the {@link SelectionModel} of this view.
	 */
	private boolean mirrorSelection = false;

	private final JTabbedPane pane;

	private final RefList< V > filterByVertices;

	private final RefSet< E > filterByEdges;

	private final ContextChooser< V > contextChooser;

	public TableViewFrame(
			final ReadOnlyGraph< V, E > viewGraph,
			final HighlightModel< V, E > highlightModel,
			final FocusModel< V, E > focusModel,
			final SelectionModel< V, E > selectionModel,
			final FeatureModel featureModel,
			final TagSetModel< V, E > tagSetModel,
			final Function< V, String > vertexLabelGenerator,
			final Function< E, String > edgeLabelGenerator,
			final BiConsumer< V, String > vertexLabelSetter,
			final BiConsumer< E, String > edgeLabelSetter,
			final GroupHandle groupHandle,
			final NavigationHandler< V, E > navigationHandler,
			final UndoPointMarker undoPointMarker,
			final GraphColorGenerator< V, E > coloring )
	{
		super( "Feature and tag table" );
		this.highlightModel = highlightModel;
		this.focusModel = focusModel;
		this.featureModel = featureModel;
		this.selectionModel = selectionModel;
		this.tagSetModel = tagSetModel;
		this.vref = viewGraph.vertexRef();
		this.eref = viewGraph.edgeRef();
		final List< TagSet > tagSets = tagSetModel.getTagSetStructure().getTagSets();
		this.filterByVertices = RefCollections.createRefList( viewGraph.vertices() );
		this.filterByEdges = RefCollections.createRefSet( viewGraph.edges() );

		/*
		 * Settings panel.
		 */

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		this.contextChooser = new ContextChooser<>( this );
		final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		settingsPanel.add( contextChooserPanel );
		add( settingsPanel, BorderLayout.NORTH );

		/*
		 * Vertices
		 */

		final ObjTags< V > vertexTags = tagSetModel.getVertexTags();
		final RefPool< V > vertexIdBimap = RefCollections.tryGetRefPool( viewGraph.vertices() );
		vertexTable = new FeatureTagTablePanel<>(
				vertexTags,
				vertexIdBimap,
				vertexLabelGenerator,
				vertexLabelSetter,
				undoPointMarker,
				( v ) -> coloring.color( v ) );
		final JTable vt = vertexTable.getTable();

		vt.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			private final V ref = viewGraph.vertexRef();

			@Override
			public void valueChanged( final ListSelectionEvent event )
			{
				if ( event.getValueIsAdjusting() || ignoreTableSelectionChange )
					return;
				ignoreSelectionChange = true;

				// Selection.
				if ( mirrorSelection )
				{
					selectionModel.pauseListeners();
					final RefSet< E > selectedEdges = selectionModel.getSelectedEdges();
					selectionModel.clearSelection();
					final int[] selectedRows = vt.getSelectedRows();
					for ( final int row : selectedRows )
						selectionModel.setSelected( vertexTable.getObjectForViewRow( row, ref ), true );
					for ( final E e : selectedEdges )
						selectionModel.setSelected( e, true );
					selectionModel.resumeListeners();
				}

				// Navigate and focus.
				final int rowIndex = vt.getSelectionModel().getLeadSelectionIndex();
				final V v = vertexTable.getObjectForViewRow( rowIndex, ref );
				if ( null != v )
				{
					navigationHandler.notifyNavigateToVertex( v );
					focusModel.focusVertex( v );
				}

				ignoreSelectionChange = false;
			}
		} );

		@SuppressWarnings( "unchecked" )
		final Class< V > vertexClass = ( Class< V > ) viewGraph.vertexRef().getClass();
		final Map< FeatureSpec< ?, V >, Feature< V > > vertexFeatures =
				FeatureUtils.collectFeatureMap( featureModel, vertexClass );
		vertexTable.setFeatures( vertexFeatures );
		vertexTable.setTagSets( tagSets );

		/*
		 * Edges
		 */

		final ObjTags< E > edgeTags = tagSetModel.getEdgeTags();
		final RefPool< E > edgeIdBimap = RefCollections.tryGetRefPool( viewGraph.edges() );

		final ColorGenerator< E > edgeColorGenerator = new ColorGenerator< E >()
		{

			private final V vTmpS = viewGraph.vertexRef();

			private final V vTmpT = viewGraph.vertexRef();

			@Override
			public int color( final E edge )
			{
				edge.getTarget( vTmpT );
				edge.getSource( vTmpS );
				return coloring.color( edge, vTmpS, vTmpT );
			}
		};

		edgeTable = new FeatureTagTablePanel<>(
				edgeTags,
				edgeIdBimap,
				edgeLabelGenerator,
				edgeLabelSetter,
				undoPointMarker,
				edgeColorGenerator );
		final JTable et = edgeTable.getTable();
		et.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			private final E eref = viewGraph.edgeRef();

			private final V vref = viewGraph.vertexRef();

			@Override
			public void valueChanged( final ListSelectionEvent event )
			{
				if ( event.getValueIsAdjusting() || ignoreTableSelectionChange )
					return;
				ignoreSelectionChange = true;

				// Selection.
				if ( mirrorSelection )
				{
					selectionModel.pauseListeners();
					final RefSet< V > selectedVertices = selectionModel.getSelectedVertices();
					selectionModel.clearSelection();
					final int[] selectedRows = et.getSelectedRows();
					for ( final int row : selectedRows )
						selectionModel.setSelected( edgeTable.getObjectForViewRow( row, eref ), true );
					for ( final V v : selectedVertices )
						selectionModel.setSelected( v, true );
					selectionModel.resumeListeners();
				}

				// Navigate and focus.
				final int rowIndex = et.getSelectionModel().getLeadSelectionIndex();
				final E e = edgeTable.getObjectForViewRow( rowIndex, eref );
				if ( null != e )
				{
					navigationHandler.notifyNavigateToEdge( e );
					focusModel.focusVertex( e.getTarget( vref ) );
				}

				ignoreSelectionChange = false;
			}
		} );

		@SuppressWarnings( "unchecked" )
		final Class< E > edgeClass = ( Class< E > ) viewGraph.edgeRef().getClass();
		final Map< FeatureSpec< ?, E >, Feature< E > > edgeFeatures =
				FeatureUtils.collectFeatureMap( featureModel, edgeClass );
		edgeTable.setFeatures( edgeFeatures );
		edgeTable.setTagSets( tagSets );

		navigationHandler.listeners().add( new NavigationListener< V, E >()
		{

			@Override
			public void navigateToVertex( final V vertex )
			{
				vertexTable.scrollToObject( vertex );
			}

			@Override
			public void navigateToEdge( final E edge )
			{
				edgeTable.scrollToObject( edge );
			}

		} );

		/*
		 * Tab pane.
		 */

		pane = new JTabbedPane( JTabbedPane.LEFT );
		pane.add( "Vertices", vertexTable );
		pane.add( "Edges", edgeTable );
		add( pane, BorderLayout.CENTER );
	}

	/**
	 * Exposes the vertex table of this view.
	 *
	 * @return the vertex table.
	 */
	public FeatureTagTablePanel< V > getVertexTable()
	{
		return vertexTable;
	}

	/**
	 * Exposes the edge table of this view.
	 *
	 * @return the edge table.
	 */
	public FeatureTagTablePanel< E > getEdgeTable()
	{
		return edgeTable;
	}

	/**
	 * Sets the selection mirroring settings. If <code>true</code>, the
	 * selection in the {@link JTable}s will be used to update the
	 * {@link SelectionModel} of this view.
	 *
	 * @param mirrorSelection
	 *            whether the selection in the table should be mirrored on the
	 *            {@link SelectionModel}.
	 */
	public void setMirrorSelection( final boolean mirrorSelection )
	{
		this.mirrorSelection = mirrorSelection;
	}

	@Override
	public void selectionChanged()
	{
		if ( ignoreSelectionChange )
			return;
		ignoreTableSelectionChange = true;

		// Vertices table.
		final RefSet< V > selectedVertices = selectionModel.getSelectedVertices();
		final JTable vt = vertexTable.getTable();
		vt.getSelectionModel().clearSelection();
		for ( final V v : selectedVertices )
		{
			final int row = vertexTable.getViewRowForObject( v );
			vt.getSelectionModel().addSelectionInterval( row, row );
		}

		// Edges table.
		final RefSet< E > selectedEdges = selectionModel.getSelectedEdges();
		final JTable et = edgeTable.getTable();
		et.getSelectionModel().clearSelection();
		for ( final E e : selectedEdges )
		{
			final int row = edgeTable.getViewRowForObject( e );
			et.getSelectionModel().addSelectionInterval( row, row );
		}
		ignoreTableSelectionChange = false;
	}

	@Override
	public void focusChanged()
	{
		final V focusedVertex = focusModel.getFocusedVertex( vref );
		vertexTable.focusObject( focusedVertex );
	}

	@Override
	public void highlightChanged()
	{
		final V highlightedVertex = highlightModel.getHighlightedVertex( vref );
		vertexTable.highlightObject( highlightedVertex );
		final E highlightedEdge = highlightModel.getHighlightedEdge( eref );
		edgeTable.highlightObject( highlightedEdge );
	}

	@Override
	public void featureModelChanged()
	{
		ignoreTableSelectionChange = true;
		final Map< ?, ? > map1 = FeatureUtils.collectFeatureMap( featureModel, vref.getClass() );
		@SuppressWarnings( "unchecked" )
		final Map< FeatureSpec< ?, V >, Feature< V > > vertexFeatures =  ( Map< FeatureSpec< ?, V >, Feature< V > > ) map1;
		vertexTable.setFeatures( vertexFeatures );
		final Map< ?, ? > map2 = FeatureUtils.collectFeatureMap( featureModel, eref.getClass() );
		@SuppressWarnings( "unchecked" )
		final Map< FeatureSpec< ?, E >, Feature< E > >  edgeFeatures = ( Map< FeatureSpec< ?, E >, Feature< E > > ) map2;
		edgeTable.setFeatures( edgeFeatures );
		if ( mirrorSelection )
			selectionChanged();
		ignoreTableSelectionChange = false;
	}

	@Override
	public void tagSetStructureChanged()
	{
		ignoreTableSelectionChange = true;
		final List< TagSet > tagSets = tagSetModel.getTagSetStructure().getTagSets();
		vertexTable.setTagSets( tagSets );
		edgeTable.setTagSets( tagSets );
		if ( mirrorSelection )
			selectionChanged();
		ignoreTableSelectionChange = false;
	}

	public FeatureTagTablePanel< ? > getCurrentlyDisplayedTable()
	{
		final int selectedIndex = pane.getSelectedIndex();
		return selectedIndex == 0 ? vertexTable : edgeTable;
	}

	public void switchToVertexTable( final boolean toVertexTable )
	{
		pane.setSelectedIndex( toVertexTable ? 0 : 1 );
	}

	public void editCurrentLabel()
	{
		getCurrentlyDisplayedTable().editCurrentLabel();
	}

	public void toggleTag()
	{
		getCurrentlyDisplayedTable().toggleCurrentTag();
	}

	@Override
	public void contextChanged( final Context< V > context )
	{
		ignoreTableSelectionChange = true;
		if ( null == context )
		{
			vertexTable.filter( null  );
			edgeTable.filter( null );
		}
		else
		{
			filterByVertices.clear();
			filterByEdges.clear();
			final Iterable< V > insideVertices = context.getInsideVertices( context.getTimepoint() );
			for ( final V v : insideVertices )
			{
				filterByVertices.add( v );
				for ( final E e : v.edges() )
					filterByEdges.add( e );
			}
			vertexTable.filter( filterByVertices );
			edgeTable.filter( filterByEdges );
		}
		if ( mirrorSelection )
			selectionChanged();
		ignoreTableSelectionChange = false;
	}

	public ContextChooser< V > getContextChooser()
	{
		return contextChooser;
	}
}
