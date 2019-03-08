package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.colorMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;
import static org.mastodon.revised.mamut.MamutViewStateSerialization.FEATURE_COLOR_MODE_KEY;
import static org.mastodon.revised.mamut.MamutViewStateSerialization.FRAME_POSITION_KEY;
import static org.mastodon.revised.mamut.MamutViewStateSerialization.NO_COLORING_KEY;
import static org.mastodon.revised.mamut.MamutViewStateSerialization.TAG_SET_KEY;
import static org.mastodon.revised.mamut.MamutViewStateSerialization.TRACKSCHEME_TRANSFORM_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ActionMap;

import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraphTrackSchemeProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.revised.trackscheme.ScreenTransform;
import org.mastodon.revised.trackscheme.TrackSchemeContextListener;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.mastodon.revised.trackscheme.display.EditFocusVertexLabelAction;
import org.mastodon.revised.trackscheme.display.ToggleLinkBehaviour;
import org.mastodon.revised.trackscheme.display.TrackSchemeFrame;
import org.mastodon.revised.trackscheme.display.TrackSchemeNavigationActions;
import org.mastodon.revised.trackscheme.display.TrackSchemeOptions;
import org.mastodon.revised.trackscheme.display.TrackSchemePanel;
import org.mastodon.revised.trackscheme.display.TrackSchemeZoom;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyle;
import org.mastodon.revised.ui.EditTagActions;
import org.mastodon.revised.ui.FocusActions;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.revised.ui.coloring.ColoringModel;
import org.mastodon.revised.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.views.context.ContextChooser;
import org.scijava.ui.behaviour.KeyPressedManager;

public class MamutViewTrackScheme extends MamutView< TrackSchemeGraph< Spot, Link >, TrackSchemeVertex, TrackSchemeEdge >
{
	private final ContextChooser< Spot > contextChooser;

	private final TrackSchemePanel trackschemePanel;

	private final ColoringModel coloringModel;

	public MamutViewTrackScheme( final MamutAppModel appModel )
	{
		this( appModel, new HashMap<>() );
	}

	public MamutViewTrackScheme( final MamutAppModel appModel, final Map< String, Object > guiState )
	{
		super( appModel,
				new TrackSchemeGraph<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						new ModelGraphTrackSchemeProperties( appModel.getModel().getGraph() ),
						appModel.getModel().getGraph().getLock() ),
				new String[] { KeyConfigContexts.TRACKSCHEME } );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener<>( viewGraph );
		contextChooser = new ContextChooser<>( contextListener );

		final KeyPressedManager keyPressedManager = appModel.getKeyPressedManager();
		final Model model = appModel.getModel();

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeStyle forwardDefaultStyle = appModel.getTrackSchemeStyleManager().getForwardDefaultStyle();
		final GraphColorGeneratorAdapter< Spot, Link, TrackSchemeVertex, TrackSchemeEdge > coloring = new GraphColorGeneratorAdapter<>( viewGraph.getVertexMap(), viewGraph.getEdgeMap() );
		final TrackSchemeOptions options = TrackSchemeOptions.options()
				.shareKeyPressedEvents( keyPressedManager )
				.style( forwardDefaultStyle )
				.graphColorGenerator( coloring );
		final AutoNavigateFocusModel< TrackSchemeVertex, TrackSchemeEdge > navigateFocusModel = new AutoNavigateFocusModel<>( focusModel, navigationHandler );
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				viewGraph,
				highlightModel,
				navigateFocusModel,
				timepointModel,
				selectionModel,
				navigationHandler,
				model,
				groupHandle,
				contextChooser,
				options );

		// Restore position
		final int[] pos = ( int[] ) guiState.get( FRAME_POSITION_KEY );
		if ( null != pos )
			frame.setBounds( pos[ 0 ], pos[ 1 ], pos[ 2 ], pos[ 3 ] );
		else
			frame.setLocationRelativeTo( null );

		this.trackschemePanel = frame.getTrackschemePanel();

		trackschemePanel.setTimepointRange( appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
		trackschemePanel.graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );

		final TrackSchemeStyle.UpdateListener updateListener = () -> trackschemePanel.repaint();
		forwardDefaultStyle.updateListeners().add( updateListener );
		onClose( () -> forwardDefaultStyle.updateListeners().remove( updateListener ) );

		setFrame( frame );
		frame.setVisible( true );

		// Transform.
		final ScreenTransform tLoaded = ( ScreenTransform ) guiState.get( TRACKSCHEME_TRANSFORM_KEY );
		if ( null != tLoaded )
		{
			trackschemePanel.getTransformEventHandler().setTransform( tLoaded );
			trackschemePanel.getDisplay().transformChanged( tLoaded );
		}

		MastodonFrameViewActions.install( viewActions, this );
		HighlightBehaviours.install( viewBehaviours, viewGraph, viewGraph.getLock(), viewGraph, highlightModel, model );
		ToggleLinkBehaviour.install( viewBehaviours, trackschemePanel,	viewGraph, viewGraph.getLock(),	viewGraph, model );
		EditFocusVertexLabelAction.install( viewActions, trackschemePanel, focusModel, model );
		FocusActions.install( viewActions, viewGraph, viewGraph.getLock(), navigateFocusModel, selectionModel );
		TrackSchemeZoom.install( viewBehaviours, trackschemePanel );
		EditTagActions.install( viewActions, frame.getKeybindings(), frame.getTriggerbindings(), model.getTagSetModel(), appModel.getSelectionModel(), trackschemePanel, trackschemePanel.getDisplay(), model );
		viewActions.runnableAction( () -> System.out.println( model.getTagSetModel() ), "output tags", "U" ); // DEBUG TODO: REMOVE

		// TODO Let the user choose between the two selection/focus modes.
		trackschemePanel.getNavigationActions().install( viewActions, TrackSchemeNavigationActions.NavigatorEtiquette.FINDER_LIKE );
		trackschemePanel.getNavigationBehaviours().install( viewBehaviours );
		trackschemePanel.getTransformEventHandler().install( viewBehaviours );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		final JMenuHandle coloringMenuHandle = new JMenuHandle();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				viewMenu(
						colorMenu( coloringMenuHandle ),
						separator(),
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL )
				),
				editMenu(
						item( UndoActions.UNDO ),
						item( UndoActions.REDO ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD ),
						separator(),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.SELECT_NAVIGATE_RIGHT ),
						separator(),
						item( TrackSchemeNavigationActions.NAVIGATE_CHILD ),
						item( TrackSchemeNavigationActions.NAVIGATE_PARENT ),
						item( TrackSchemeNavigationActions.NAVIGATE_LEFT ),
						item( TrackSchemeNavigationActions.NAVIGATE_RIGHT ),
						separator(),
						item( EditFocusVertexLabelAction.EDIT_FOCUS_LABEL )
				)
		);
		appModel.getPlugins().addMenus( menu );

		this.coloringModel = registerColoring( coloring, coloringMenuHandle,
				() -> trackschemePanel.entitiesAttributesChanged() );

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

		trackschemePanel.repaint();
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return contextChooser;
	}

	/**
	 * Exposes the {@link TrackSchemePanel} displayed in this view.
	 *
	 * @return the {@link TrackSchemePanel}.
	 */
	public TrackSchemePanel getTrackschemePanel()
	{
		return trackschemePanel;
	}

	/**
	 * Exposes the coloring model that manages the coloring of this view.
	 *
	 * @return the coloring model.
	 */
	public ColoringModel getColoringModel()
	{
		return coloringModel;
	}
}
