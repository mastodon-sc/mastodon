package org.mastodon.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutViewStateSerialization.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.GROUP_HANDLE_ID_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.NO_COLORING_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.SETTINGS_PANEL_VISIBLE_KEY;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_DISPLAYING_VERTEX_TABLE;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_EDGE_TABLE_VISIBLE_POS;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_SELECTION_ONLY;
import static org.mastodon.mamut.MamutViewStateSerialization.TABLE_VERTEX_TABLE_VISIBLE_POS;
import static org.mastodon.mamut.MamutViewStateSerialization.TAG_SET_KEY;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.ActionMap;
import javax.swing.JPanel;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.SearchVertexLabel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.SpotPool;
import org.mastodon.model.SelectionListener;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.feature.FeatureColorMode;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.table.TableViewActions;
import org.mastodon.views.table.TableViewFrame;

public class MamutViewTable extends MamutView< ViewGraph< Spot, Link, Spot, Link >, Spot, Link >
{

	private static final String[] CONTEXTS = new String[] { KeyConfigContexts.TABLE };

	private final ColoringModel coloringModel;

	private final boolean selectionOnly;

	public MamutViewTable( final MamutAppModel appModel, final boolean selectionOnly )
	{
		this( appModel, Collections.singletonMap(
				TABLE_SELECTION_ONLY, Boolean.valueOf( selectionOnly ) ) );
	}

	public MamutViewTable( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel, IdentityViewGraph.wrap( appModel.getModel().getGraph(), appModel.getModel().getGraphIdBimap() ), CONTEXTS );
		this.selectionOnly = ( boolean ) guiState.getOrDefault( TABLE_SELECTION_ONLY, false );

		final GraphColorGeneratorAdapter< Spot, Link, Spot, Link > coloring = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );

		final TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > frame = new TableViewFrame<>(
				appModel,
				viewGraph,
				appModel.getModel().getFeatureModel(),
				appModel.getModel().getTagSetModel(),
				( v ) -> v.getLabel(),
				new Function< Link, String >()
				{

					private final Spot ref = appModel.getModel().getGraph().vertexRef();

					@Override
					public String apply( final Link t )
					{
						return t.getSource( ref ).getLabel() + " \u2192 " + t.getTarget( ref ).getLabel();
					}
				},
				( v, lbl ) -> v.setLabel( lbl ),
				null,
				groupHandle,
				navigationHandler,
				appModel.getModel(),
				coloring );

		setFrame( frame );

		// Restore position.
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
		{
			frame.setSize( 400, 400 );
			frame.setLocationRelativeTo( null );
		}
		// Restore group handle.
		final Integer groupID = ( Integer ) guiState.get( GROUP_HANDLE_ID_KEY );
		if ( null != groupID )
			groupHandle.setGroupId( groupID.intValue() );

		// Restore settings panel visibility.
		final Boolean settingsPanelVisible = ( Boolean ) guiState.get( SETTINGS_PANEL_VISIBLE_KEY );
		if ( null != settingsPanelVisible )
			frame.setSettingsPanelVisible( settingsPanelVisible.booleanValue() );

		/*
		 * Deal with actions.
		 */

		final Model model = appModel.getModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();

		focusModel.listeners().add( frame );
		highlightModel.listeners().add( frame );
		featureModel.listeners().add( frame );
		tagSetModel.listeners().add( frame );

		MastodonFrameViewActions.install( viewActions, this );
		TableViewActions.install( viewActions, frame );

		final JPanel searchPanel = SearchVertexLabel.install( viewActions, viewGraph, navigationHandler, selectionModel, focusModel, frame.getCurrentlyDisplayedTable() );
		frame.getSettingsPanel().add( searchPanel );

		onClose( () -> {
			focusModel.listeners().remove( frame );
			highlightModel.listeners().remove( frame );
			featureModel.listeners().remove( frame );
			tagSetModel.listeners().remove( frame );
		} );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle menuHandle = new JMenuHandle();

		MamutMenuBuilder.build( menu, actionMap,
				MamutMenuBuilder.fileMenu(
						item( TableViewActions.EXPORT_TO_CSV ),
						separator() ) );
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				MamutMenuBuilder.viewMenu(
						MamutMenuBuilder.colorMenu( menuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL ) ),
				MamutMenuBuilder.editMenu(
						item( TableViewActions.EDIT_LABEL ),
						item( TableViewActions.TOGGLE_TAG ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ) ) );
		appModel.getPlugins().addMenus( menu );

		coloringModel = registerColoring( coloring, menuHandle, () -> {
			frame.getEdgeTable().repaint();
			frame.getVertexTable().repaint();
		} );

		// Restore coloring.
		final Boolean noColoring = ( Boolean ) guiState.get( NO_COLORING_KEY );
		if ( null != noColoring && noColoring )
		{
			coloringModel.colorByNone();
		}
		else
		{
			final String tagSetName = ( String ) guiState.get( TAG_SET_KEY );
			final String featureColorModeName = ( String ) guiState.get( FEATURE_COLOR_MODE_KEY );
			if ( null != tagSetName )
			{
				for ( final TagSet tagSet : coloringModel.getTagSetStructure().getTagSets() )
				{
					if ( tagSet.getName().equals( tagSetName ) )
					{
						coloringModel.colorByTagSet( tagSet );
						break;
					}
				}
			}
			else if ( null != featureColorModeName )
			{
				final List< FeatureColorMode > featureColorModes = new ArrayList<>();
				featureColorModes.addAll( coloringModel.getFeatureColorModeManager().getBuiltinStyles() );
				featureColorModes.addAll( coloringModel.getFeatureColorModeManager().getUserStyles() );
				for ( final FeatureColorMode featureColorMode : featureColorModes )
				{
					if ( featureColorMode.getName().equals( featureColorModeName ) )
					{
						coloringModel.colorByFeature( featureColorMode );
						break;
					}
				}
			}
		}

		/*
		 * Deal with content.
		 */

		final FeatureTagTablePanel< Spot > vertexTable = frame.getVertexTable();
		final FeatureTagTablePanel< Link > edgeTable = frame.getEdgeTable();
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();

		if ( selectionOnly )
		{
			// Pass only the selection.
			frame.setTitle( "Selection table" );
			frame.setMirrorSelection( false );
			final SelectionListener selectionListener = () -> {
				vertexTable.setRows( selectionModel.getSelectedVertices() );
				edgeTable.setRows( selectionModel.getSelectedEdges() );
			};
			selectionModel.listeners().add( selectionListener );
			selectionListener.selectionChanged();
			onClose( () -> selectionModel.listeners().remove( selectionListener ) );
		}
		else
		{
			// Pass and listen to the full graph.
			final ModelGraph graph = appModel.getModel().getGraph();
			final GraphChangeListener graphChangeListener = () -> {
				vertexTable.setRows( graph.vertices() );
				edgeTable.setRows( graph.edges() );
			};
			graph.addGraphChangeListener( graphChangeListener );
			graphChangeListener.graphChanged();
			onClose( () -> graph.removeGraphChangeListener( graphChangeListener ) );

			// Listen to selection changes.
			frame.setMirrorSelection( true );
			selectionModel.listeners().add( frame );
			frame.selectionChanged();
			onClose( () -> selectionModel.listeners().remove( frame ) );
		}
		/*
		 * Register a listener to vertex label property changes, will update the
		 * table-view when the label change.
		 */
		final SpotPool spotPool = ( SpotPool ) appModel.getModel().getGraph().vertices().getRefPool();
		spotPool.labelProperty().addPropertyChangeListener( v -> frame.repaint() );

		/*
		 * Show table.
		 */

		frame.setVisible( true );

		/*
		 * Restore table visible rectangle and displayed table.
		 */

		final boolean displayingVertexTable = ( boolean ) guiState.getOrDefault( TABLE_DISPLAYING_VERTEX_TABLE, Boolean.TRUE );
		frame.switchToVertexTable( displayingVertexTable );

		final int[] visibleRectVertexTable = ( int[] ) guiState.get( TABLE_VERTEX_TABLE_VISIBLE_POS );
		if ( null != visibleRectVertexTable )
			frame.getVertexTable().getScrollPane().getViewport().setViewPosition( new Point( 
					visibleRectVertexTable[ 0 ],
					visibleRectVertexTable[ 1 ] ) );

		final int[] visibleRectEdgeTable = ( int[] ) guiState.get( TABLE_EDGE_TABLE_VISIBLE_POS );
		if ( null != visibleRectEdgeTable )
			frame.getEdgeTable().getScrollPane().getViewport().setViewPosition( new Point( 
					visibleRectEdgeTable[ 0 ],
					visibleRectEdgeTable[ 1 ] ) );
	}

	@Override
	public TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > getFrame()
	{
		final ViewFrame f = super.getFrame();
		@SuppressWarnings( "unchecked" )
		final TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > vf = ( TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > ) f;
		return vf;
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return getFrame().getContextChooser();
	}

	public ColoringModel getColoringModel()
	{
		return coloringModel;
	}

	public boolean isSelectionTable()
	{
		return selectionOnly;
	}
}
