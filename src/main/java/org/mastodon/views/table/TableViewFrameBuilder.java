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
package org.mastodon.views.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.coloring.ColorGenerator;
import org.mastodon.ui.coloring.DefaultGraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.util.FeatureUtils;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.context.ContextListener;

public class TableViewFrameBuilder
{

	private final List< GraphTableBuilder< ?, ? > > graphBuilders = new ArrayList<>();

	private String title = "Feature and tag table";

	private GroupHandle groupHandle;

	private UndoPointMarker undo;

	private final ArrayList< Runnable > runOnClose = new ArrayList<>();

	public TableViewFrameBuilder title( final String title )
	{
		this.title = title;
		return this;
	}

	public < V extends Vertex< E >, E extends Edge< V > > GraphTableBuilder< V, E >
			addGraph( final ReadOnlyGraph< V, E > graph )
	{
		final GraphTableBuilder< V, E > builder = new GraphTableBuilder<>( graph, this );
		graphBuilders.add( builder );
		return builder;
	}

	public TableViewFrameBuilder groupHandle( final GroupHandle groupHandle )
	{
		this.groupHandle = groupHandle;
		return this;
	}

	public TableViewFrameBuilder undo( final UndoPointMarker undo )
	{
		this.undo = undo;
		return this;
	}

	public MyTableViewFrame get()
	{
		final MyTableViewFrame frame = new MyTableViewFrame( title );
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				runOnClose.forEach( Runnable::run );
				runOnClose.clear();
			}
		} );

		final JPanel settingsPanel = frame.getSettingsPanel();

		/*
		 * Group handle.
		 */

		if ( groupHandle != null )
		{
			final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
			settingsPanel.add( navigationLocksPanel );
			settingsPanel.add( Box.createHorizontalGlue() );
		}

		/*
		 * Treat graphs one by one.
		 */

		for ( final GraphTableBuilder< ?, ? > graphBuilder : graphBuilders )
		{
			final GraphTableBundle< ?, ? > bundle = new GraphTableBundle<>( graphBuilder, undo, settingsPanel );
			final String vertexName = bundle.vref1.getClass().getSimpleName();
			frame.pane.add( vertexName, bundle.vertexTable );
			final String edgeName = bundle.eref.getClass().getSimpleName();
			frame.pane.add( edgeName, bundle.edgeTable );
			if ( bundle.contextChooser != null )
				frame.contextChoosers.add( bundle.contextChooser );
		}

		return frame;
	}

	/**
	 * Centralizes the creation of the vertex and edge tables for the specified
	 * graph, plus wire listeners.
	 * 
	 * @author Jean-Yves Tinevez
	 *
	 * @param <VV>
	 * @param <EE>
	 */
	private final class GraphTableBundle< VV extends Vertex< EE >, EE extends Edge< VV > >
			implements
			ContextListener< VV >,
			SelectionListener,
			org.mastodon.model.FocusListener,
			HighlightListener,
			TagSetModel.TagSetModelListener,
			FeatureModelListener
	{

		private final SelectionModel< VV, EE > selectionModel;

		private final FocusModel< VV > focusModel;

		private final HighlightModel< VV, EE > highlightModel;

		private final FeatureModel featureModel;

		private final TagSetModel< VV, EE > tagSetModel;

		private boolean ignoreTableSelectionChange = false;

		private boolean ignoreSelectionChange = false;

		private FeatureTagTablePanel< EE > edgeTable;

		private FeatureTagTablePanel< VV > vertexTable;

		private final RefList< VV > filterByVertices;

		private final RefSet< EE > filterByEdges;

		private final boolean mirrorSelection;

		private final VV vref1;

		private final VV vref2;

		private final EE eref;

		private ContextChooser< VV > contextChooser;

		public GraphTableBundle(
				final GraphTableBuilder< VV, EE > graphBuilder,
				final UndoPointMarker undo,
				final JPanel settingsPanel )
		{
			this.selectionModel = graphBuilder.selectionModel;
			this.focusModel = graphBuilder.focusModel;
			this.highlightModel = graphBuilder.highlightModel;
			this.featureModel = graphBuilder.featureModel;
			this.tagSetModel = graphBuilder.tagSetModel;
			this.filterByVertices = RefCollections.createRefList( graphBuilder.graph.vertices() );
			this.filterByEdges = RefCollections.createRefSet( graphBuilder.graph.edges() );
			this.mirrorSelection = !graphBuilder.selectionTable;
			this.vref1 = graphBuilder.graph.vertexRef();
			this.vref2 = graphBuilder.graph.vertexRef();
			this.eref = graphBuilder.graph.edgeRef();

			/*
			 * Vertex table.
			 */

			final RefPool< VV > vertices = RefCollections.tryGetRefPool( graphBuilder.graph.vertices() );
			final TablePanelBuilder< VV > vertexTableBuilder = TablePanelBuilder
					.create( vertices )
					.labelSetter( graphBuilder.vertexLabelSetter )
					.labelGetter( graphBuilder.vertexLabelGenerator )
					.coloring( v -> graphBuilder.coloring.color( v ) )
					.undo( undo );
			if ( graphBuilder.tagSetModel != null )
				vertexTableBuilder.tags( graphBuilder.tagSetModel.getVertexTags() );
			this.vertexTable = vertexTableBuilder.get();

			// Listen to selection in the vertex table.
			final JTable vt = vertexTable.getTable();
			vt.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{

				private final VV ref = graphBuilder.graph.vertexRef();

				@Override
				public void valueChanged( final ListSelectionEvent event )
				{
					if ( event.getValueIsAdjusting() || ignoreTableSelectionChange )
						return;
					ignoreSelectionChange = true;

					// // Send changes in table selection to the selection
					// model.
					if ( !graphBuilder.selectionTable && graphBuilder.selectionModel != null )
					{
						graphBuilder.selectionModel.pauseListeners();
						final RefSet< EE > selectedEdges = graphBuilder.selectionModel.getSelectedEdges();
						graphBuilder.selectionModel.clearSelection();
						final int[] selectedRows = vt.getSelectedRows();
						for ( final int row : selectedRows )
							graphBuilder.selectionModel.setSelected( vertexTable.getObjectForViewRow( row, ref ),
									true );
						for ( final EE e : selectedEdges )
							graphBuilder.selectionModel.setSelected( e, true );
						graphBuilder.selectionModel.resumeListeners();
					}

					// Navigate and focus.
					final int rowIndex = vt.getSelectionModel().getLeadSelectionIndex();
					final VV v = vertexTable.getObjectForViewRow( rowIndex, ref );
					if ( null != v )
					{
						if ( graphBuilder.navigationHandler != null )
							graphBuilder.navigationHandler.notifyNavigateToVertex( v );
						if ( graphBuilder.focusModel != null )
							graphBuilder.focusModel.focusVertex( v );
					}

					ignoreSelectionChange = false;
				}
			} );

			// Features.
			if ( graphBuilder.featureModel != null )
			{
				@SuppressWarnings( "unchecked" )
				final Class< VV > vertexClass = ( Class< VV > ) graphBuilder.graph.vertexRef().getClass();
				final Map< FeatureSpec< ?, VV >, Feature< VV > > vertexFeatures =
						FeatureUtils.collectFeatureMap( graphBuilder.featureModel, vertexClass );
				vertexTable.setFeatures( vertexFeatures );
			}

			// Tag-sets.
			if ( graphBuilder.tagSetModel != null )
			{
				final List< TagSet > tagSets = graphBuilder.tagSetModel.getTagSetStructure().getTagSets();
				vertexTable.setTagSets( tagSets );
			}

			/*
			 * Edge table.
			 */

			final RefPool< EE > edges = RefCollections.tryGetRefPool( graphBuilder.graph.edges() );
			final ColorGenerator< EE > edgeColorGenerator = new ColorGenerator< EE >()
			{

				private final VV vTmpS = graphBuilder.graph.vertexRef();

				private final VV vTmpT = graphBuilder.graph.vertexRef();

				@Override
				public int color( final EE edge )
				{
					edge.getTarget( vTmpT );
					edge.getSource( vTmpS );
					return graphBuilder.coloring.color( edge, vTmpS, vTmpT );
				}
			};
			final TablePanelBuilder< EE > edgeTableBuilder = TablePanelBuilder
					.create( edges )
					.labelGetter( graphBuilder.edgeLabelGenerator )
					.labelSetter( graphBuilder.edgeLabelSetter )
					.coloring( edgeColorGenerator )
					.undo( undo );
			if ( graphBuilder.tagSetModel != null )
				edgeTableBuilder.tags( graphBuilder.tagSetModel.getEdgeTags() );
			this.edgeTable = edgeTableBuilder.get();

			// Listen to selection in the edge table.
			final JTable et = edgeTable.getTable();
			et.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{

				private final EE eref = graphBuilder.graph.edgeRef();

				private final VV vref = graphBuilder.graph.vertexRef();

				@Override
				public void valueChanged( final ListSelectionEvent event )
				{
					if ( event.getValueIsAdjusting() || ignoreTableSelectionChange )
						return;
					ignoreSelectionChange = true;

					// Send changes in table selection to the selection model.
					if ( !graphBuilder.selectionTable && graphBuilder.selectionModel != null )
					{
						graphBuilder.selectionModel.pauseListeners();
						final RefSet< VV > selectedVertices = graphBuilder.selectionModel.getSelectedVertices();
						graphBuilder.selectionModel.clearSelection();
						final int[] selectedRows = et.getSelectedRows();
						for ( final int row : selectedRows )
							graphBuilder.selectionModel.setSelected( edgeTable.getObjectForViewRow( row, eref ), true );
						for ( final VV v : selectedVertices )
							graphBuilder.selectionModel.setSelected( v, true );
						graphBuilder.selectionModel.resumeListeners();
					}

					// Navigate and focus.
					final int rowIndex = et.getSelectionModel().getLeadSelectionIndex();
					final EE e = edgeTable.getObjectForViewRow( rowIndex, eref );
					if ( null != e )
					{
						if ( graphBuilder.navigationHandler != null )
							graphBuilder.navigationHandler.notifyNavigateToEdge( e );
						if ( graphBuilder.focusModel != null )
							graphBuilder.focusModel.focusVertex( e.getTarget( vref ) );
					}

					ignoreSelectionChange = false;
				}
			} );

			// Set objects and listen to changes in the graph.
			if ( graphBuilder.selectionTable )
			{
				// Pass only the selection.
				final SelectionListener selectionListener = () -> {
					vertexTable.setRows( selectionModel.getSelectedVertices() );
					edgeTable.setRows( selectionModel.getSelectedEdges() );
				};
				selectionModel.listeners().add( selectionListener );
				selectionListener.selectionChanged();
				runOnClose.add( () -> selectionModel.listeners().remove( selectionListener ) );
			}
			else
			{
				// Pass and listen to the full graph.
				final GraphChangeListener graphChangeListener = () -> {
					vertexTable.setRows( graphBuilder.graph.vertices() );
					edgeTable.setRows( graphBuilder.graph.edges() );
				};
				if ( graphBuilder.graph instanceof ListenableReadOnlyGraph< ?, ? > )
				{
					final ListenableReadOnlyGraph< ?, ? > lg = ( ListenableReadOnlyGraph< ?, ? > ) graphBuilder.graph;
					lg.addGraphChangeListener( graphChangeListener );
					runOnClose.add( () -> lg.removeGraphChangeListener( graphChangeListener ) );
				}
				graphChangeListener.graphChanged();

				// Listen to selection changes.
				if ( selectionModel != null )
				{
					selectionChanged();
					runOnClose.add( () -> selectionModel.listeners().remove( this ) );
				}
			}

			// Features.
			if ( graphBuilder.featureModel != null )
			{
				@SuppressWarnings( "unchecked" )
				final Class< EE > edgeClass = ( Class< EE > ) graphBuilder.graph.edgeRef().getClass();
				final Map< FeatureSpec< ?, EE >, Feature< EE > > edgeFeatures =
						FeatureUtils.collectFeatureMap( graphBuilder.featureModel, edgeClass );
				edgeTable.setFeatures( edgeFeatures );
			}

			// Tag sets.
			if ( graphBuilder.tagSetModel != null )
			{
				final List< TagSet > tagSets = graphBuilder.tagSetModel.getTagSetStructure().getTagSets();
				edgeTable.setTagSets( tagSets );
			}

			/*
			 * Move the table so that it receives the navigation events
			 * properly.
			 */

			if ( graphBuilder.navigationHandler != null )
			{
				final NavigationListener< VV, EE > navigationListener = new NavigationListener< VV, EE >()
				{

					@Override
					public void navigateToVertex( final VV vertex )
					{
						vertexTable.scrollToObject( vertex );
					}

					@Override
					public void navigateToEdge( final EE edge )
					{
						edgeTable.scrollToObject( edge );
					}
				};
				graphBuilder.navigationHandler.listeners().add( navigationListener );
				runOnClose.add( () -> graphBuilder.navigationHandler.listeners().remove( navigationListener ) );
			}

			/*
			 * Context.
			 */

			if ( graphBuilder.listenToContext )
			{
				contextChooser = new ContextChooser<>( this );
				final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
				settingsPanel.add( contextChooserPanel );
			}

			/*
			 * Wire listeners, so that the tables in this bundle are informed
			 * when something changes outside.
			 */

			if ( selectionModel != null )
			{
				selectionModel.listeners().add( this );
				runOnClose.add( () -> selectionModel.listeners().remove( this ) );
			}
			if ( focusModel != null )
			{
				focusModel.listeners().add( this );
				runOnClose.add( () -> focusModel.listeners().remove( this ) );
			}
			if ( highlightModel != null )
			{
				highlightModel.listeners().add( this );
				runOnClose.add( () -> highlightModel.listeners().remove( this ) );
			}
			if ( featureModel != null )
			{
				featureModel.listeners().add( this );
				runOnClose.add( () -> featureModel.listeners().remove( this ) );
			}
			if ( tagSetModel != null )
			{
				tagSetModel.listeners().add( this );
				runOnClose.add( () -> tagSetModel.listeners().remove( this ) );
			}
		}

		@Override
		public void contextChanged( final Context< VV > context )
		{
			ignoreTableSelectionChange = true;
			if ( null == context )
			{
				vertexTable.filter( null );
				edgeTable.filter( null );
			}
			else
			{
				filterByVertices.clear();
				filterByEdges.clear();
				final Iterable< VV > insideVertices = context.getInsideVertices( context.getTimepoint() );
				for ( final VV v : insideVertices )
				{
					filterByVertices.add( v );
					for ( final EE e : v.edges() )
						filterByEdges.add( e );
				}
				vertexTable.filter( filterByVertices );
				edgeTable.filter( filterByEdges );
			}
			if ( mirrorSelection )
				selectionChanged();
			ignoreTableSelectionChange = false;
		}

		@Override
		public void selectionChanged()
		{
			if ( ignoreSelectionChange || selectionModel == null )
				return;
			ignoreTableSelectionChange = true;

			// Vertices table.
			final RefSet< VV > selectedVertices = selectionModel.getSelectedVertices();
			final JTable vt = vertexTable.getTable();
			vt.getSelectionModel().clearSelection();
			for ( final VV v : selectedVertices )
			{
				final int row = vertexTable.getViewRowForObject( v );
				vt.getSelectionModel().addSelectionInterval( row, row );
			}

			// Edges table.
			final RefSet< EE > selectedEdges = selectionModel.getSelectedEdges();
			final JTable et = edgeTable.getTable();
			et.getSelectionModel().clearSelection();
			for ( final EE e : selectedEdges )
			{
				final int row = edgeTable.getViewRowForObject( e );
				et.getSelectionModel().addSelectionInterval( row, row );
			}
			ignoreTableSelectionChange = false;
		}

		@Override
		public void focusChanged()
		{
			if ( focusModel == null )
				return;
			final VV focusedVertex = focusModel.getFocusedVertex( vref1 );
			vertexTable.focusObject( focusedVertex );
		}

		@Override
		public void highlightChanged()
		{
			if ( highlightModel == null )
				return;
			final VV highlightedVertex = highlightModel.getHighlightedVertex( vref2 );
			vertexTable.highlightObject( highlightedVertex );
			final EE highlightedEdge = highlightModel.getHighlightedEdge( eref );
			edgeTable.highlightObject( highlightedEdge );
		}

		@Override
		public void featureModelChanged()
		{
			if ( featureModel == null )
				return;
			ignoreTableSelectionChange = true;
			final Map< ?, ? > map1 = FeatureUtils.collectFeatureMap( featureModel, vref1.getClass() );
			@SuppressWarnings( "unchecked" )
			final Map< FeatureSpec< ?, VV >, Feature< VV > > vertexFeatures =
					( Map< FeatureSpec< ?, VV >, Feature< VV > > ) map1;
			vertexTable.setFeatures( vertexFeatures );
			final Map< ?, ? > map2 = FeatureUtils.collectFeatureMap( featureModel, eref.getClass() );
			@SuppressWarnings( "unchecked" )
			final Map< FeatureSpec< ?, EE >, Feature< EE > > edgeFeatures =
					( Map< FeatureSpec< ?, EE >, Feature< EE > > ) map2;
			edgeTable.setFeatures( edgeFeatures );
			if ( mirrorSelection )
				selectionChanged();
			ignoreTableSelectionChange = false;
		}

		@Override
		public void tagSetStructureChanged()
		{
			if ( tagSetModel == null )
				return;
			ignoreTableSelectionChange = true;
			final List< TagSet > tagSets = tagSetModel.getTagSetStructure().getTagSets();
			vertexTable.setTagSets( tagSets );
			edgeTable.setTagSets( tagSets );
			if ( mirrorSelection )
				selectionChanged();
			ignoreTableSelectionChange = false;
		}
	}

	/*
	 * BUILDER FOR GRAPH TABLES.
	 */

	public static class GraphTableBuilder< V extends Vertex< E >, E extends Edge< V > >
	{

		private final ReadOnlyGraph< V, E > graph;

		private SelectionModel< V, E > selectionModel;

		private FocusModel< V > focusModel;

		private HighlightModel< V, E > highlightModel;

		private FeatureModel featureModel;

		private TagSetModel< V, E > tagSetModel;

		private GraphColorGenerator< V, E > coloring = new DefaultGraphColorGenerator<>();

		private Function< V, String > vertexLabelGenerator = o -> o.toString();

		private Function< E, String > edgeLabelGenerator;

		private BiConsumer< V, String > vertexLabelSetter = null;

		private BiConsumer< E, String > edgeLabelSetter = null;

		private NavigationHandler< V, E > navigationHandler;

		private boolean listenToContext = false;

		private boolean selectionTable = false;

		private final TableViewFrameBuilder parentBuilder;

		private GraphTableBuilder( final ReadOnlyGraph< V, E > graph, final TableViewFrameBuilder parentBuilder )
		{
			this.graph = graph;
			this.parentBuilder = parentBuilder;
			this.edgeLabelGenerator = new Function< E, String >()
			{

				private final V ref = graph.vertexRef();

				@Override
				public String apply( final E t )
				{
					t.getSource( ref );
					final String str1 = vertexLabelGenerator.apply( ref );
					t.getTarget( ref );
					final String str2 = vertexLabelGenerator.apply( ref );
					return str1 + " \u2192 " + str2;
				}
			};
		}

		public GraphTableBuilder< V, E > navigationHandler( final NavigationHandler< V, E > navigationHandler )
		{
			this.navigationHandler = navigationHandler;
			return this;
		}

		/**
		 * If sets to to <code>true</code>, the vertex and edge tables will
		 * display the content of the {@link #selectionModel}, which cannot be
		 * null.
		 * 
		 * @param selectionTable
		 *            if <code>true</code> will display the content of the
		 *            {@link #selectionModel}.
		 * @return this builder.
		 */
		public GraphTableBuilder< V, E > selectionTable( final boolean selectionTable )
		{
			this.selectionTable = selectionTable;
			return this;
		}

		public GraphTableBuilder< V, E > coloring( final GraphColorGenerator< V, E > coloring )
		{
			this.coloring = coloring;
			return this;
		}

		public GraphTableBuilder< V, E > listenToContext( final boolean listenToContext )
		{
			this.listenToContext = listenToContext;
			return this;
		}

		public GraphTableBuilder< V, E > selectionModel( final SelectionModel< V, E > selectionModel )
		{
			this.selectionModel = selectionModel;
			return this;
		}

		public GraphTableBuilder< V, E > focusModel( final FocusModel< V > focusModel )
		{
			this.focusModel = focusModel;
			return this;
		}

		public GraphTableBuilder< V, E > highlightModel( final HighlightModel< V, E > highlightModel )
		{
			this.highlightModel = highlightModel;
			return this;
		}

		public GraphTableBuilder< V, E > featureModel( final FeatureModel featureModel )
		{
			this.featureModel = featureModel;
			return this;
		}

		public GraphTableBuilder< V, E > tagSetModel( final TagSetModel< V, E > tagSetModel )
		{
			this.tagSetModel = tagSetModel;
			return this;
		}

		public GraphTableBuilder< V, E > vertexLabelGetter( final Function< V, String > labelGetter )
		{
			this.vertexLabelGenerator = labelGetter;
			return this;
		}

		public GraphTableBuilder< V, E > vertexLabelSetter( final BiConsumer< V, String > labelSetter )
		{
			this.vertexLabelSetter = labelSetter;
			return this;
		}

		public GraphTableBuilder< V, E > edgeLabelGetter( final Function< E, String > labelGetter )
		{
			this.edgeLabelGenerator = labelGetter;
			return this;
		}

		public GraphTableBuilder< V, E > edgeLabelSetter( final BiConsumer< E, String > labelSetter )
		{
			this.edgeLabelSetter = labelSetter;
			return this;
		}

		public TableViewFrameBuilder done()
		{
			if ( selectionTable && selectionModel == null )
				throw new IllegalArgumentException( "Cannot create a selection table if the selection model is null." );

			return parentBuilder;
		}
	}

	/*
	 * THE FRAME CLASS>
	 */

	public static class MyTableViewFrame extends ViewFrame
	{

		private static final long serialVersionUID = 1L;

		public final List< ContextChooser< ? > > contextChoosers;

		private final JTabbedPane pane;

		public MyTableViewFrame( final String windowTitle )
		{
			super( windowTitle );
			this.contextChoosers = new ArrayList<>();
			this.pane = new JTabbedPane( JTabbedPane.LEFT );
			add( pane, BorderLayout.CENTER );

			SwingUtilities.replaceUIActionMap( pane, keybindings.getConcatenatedActionMap() );
			SwingUtilities.replaceUIInputMap( pane, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
					keybindings.getConcatenatedInputMap() );

			setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		}

		public FeatureTagTablePanel< ? > getCurrentlyDisplayedTable()
		{
			final Component c = pane.getSelectedComponent();
			if ( c == null )
				return null;
			return ( FeatureTagTablePanel< ? > ) c;
		}

		public List< String > getTableNames()
		{
			final int nTabs = pane.getTabCount();
			final ArrayList< String > names = new ArrayList<>( nTabs );
			for ( int i = 0; i < nTabs; i++ )
				names.add( "Table" + pane.getTitleAt( i ) );
			return Collections.unmodifiableList( names );
		}

		public List< FeatureTagTablePanel< ? > > getTables()
		{
			final int nTabs = pane.getTabCount();
			final ArrayList< FeatureTagTablePanel< ? > > tables = new ArrayList<>( nTabs );
			for ( int i = 0; i < nTabs; i++ )
			{
				final FeatureTagTablePanel< ? > table = ( FeatureTagTablePanel< ? > ) pane.getComponentAt( i );
				tables.add( table );
			}
			return Collections.unmodifiableList( tables );
		}

		public void displayTable( final String name )
		{
			final int index = getTableNames().indexOf( name );
			if ( index < 0 )
				return;

			pane.setSelectedIndex( index );
		}

		public void editCurrentLabel()
		{
			getCurrentlyDisplayedTable().editCurrentLabel();
		}

		public void toggleCurrentTag()
		{
			getCurrentlyDisplayedTable().toggleCurrentTag();
		}

		public List< ContextChooser< ? > > getContextChoosers()
		{
			return contextChoosers;
		}
	}
}
