package org.mastodon.revised.mamut;

import bdv.tools.InitializeViewerState;
import bdv.tools.VisibilityAndGroupingDialog;
import bdv.tools.brightness.BrightnessDialog;
import bdv.viewer.ViewerOptions;
import javax.swing.ActionMap;
import net.imglib2.realtransform.AffineTransform3D;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bvv.BvvScene;
import org.mastodon.revised.bvv.NavigationActionsMamut;
import org.mastodon.revised.bvv.VolumeViewerActionsMamut;
import org.mastodon.revised.bvv.VolumeViewerFrameMamut;
import org.mastodon.revised.bvv.wrap.BvvEdgeWrapper;
import org.mastodon.revised.bvv.wrap.BvvGraphWrapper;
import org.mastodon.revised.bvv.wrap.BvvVertexWrapper;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.SelectionActions;
import tpietzsch.example2.VolumeViewerOptions;
import tpietzsch.example2.VolumeViewerPanel;
import tpietzsch.multires.SpimDataStacks;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;

public class MamutViewBvv extends MamutView< BvvGraphWrapper< Spot, Link >, BvvVertexWrapper< Spot, Link >, BvvEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bvvName = 1;

	private final BrightnessDialog brightnessDialog;

	private final VisibilityAndGroupingDialog visibilityAndGroupingDialog;

	private final VolumeViewerPanel viewer;

	public MamutViewBvv( final MamutAppModel appModel )
	{
		super( appModel,
				new BvvGraphWrapper<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getSpatioTemporalIndex(),
						appModel.getModel().getGraph().getLock(),
						new MamutModelGraphPropertiesBvv( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ),
				new String[] { KeyConfigContexts.BIGDATAVIEWER } );

		final SharedBigDataViewerData shared = appModel.getSharedBdvData();

		final String windowTitle = "BigVolumeViewer " + ( bvvName++ );

		final ViewerOptions.Values ov = shared.getOptions().values;
		final VolumeViewerOptions options = VolumeViewerOptions.options().
				width( ov.getWidth() ).
				height( ov.getHeight() ).
				shareKeyPressedEvents( ov.getKeyPressedManager() ).
				inputTriggerConfig( ov.getInputTriggerConfig() ).
				numSourceGroups( ov.getNumSourceGroups() );

		final BvvScene< ?, ? > scene = new BvvScene<>(
				viewGraph,
				selectionModel,
				highlightModel );

		// TODO: should be shared
		final SpimDataStacks stacks = new SpimDataStacks( shared.getSpimData() );

		final VolumeViewerFrameMamut frame = new VolumeViewerFrameMamut(
				windowTitle,
				shared.getSources(),
				shared.getConverterSetups(),
				stacks,
				groupHandle,
				scene,
				options );
		setFrame( frame );
		viewer = frame.getViewerPanel();
		brightnessDialog = shared.getBrightnessDialog();
		visibilityAndGroupingDialog = new VisibilityAndGroupingDialog( frame, viewer.getVisibilityAndGrouping() );

		MastodonFrameViewActions.install( viewActions, this );
		NavigationActionsMamut.install( viewActions, viewer );
		VolumeViewerActionsMamut.install( viewActions, this );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						separator(),
						item( BigDataViewerActionsMamut.LOAD_SETTINGS ),
						item( BigDataViewerActionsMamut.SAVE_SETTINGS )
				),
				viewMenu(
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL )
				),
				editMenu(
						item( UndoActions.UNDO ),
						item( UndoActions.REDO ),
						separator(),
						item( SelectionActions.DELETE_SELECTION ),
						item( SelectionActions.SELECT_WHOLE_TRACK ),
						item( SelectionActions.SELECT_TRACK_DOWNWARD ),
						item( SelectionActions.SELECT_TRACK_UPWARD )
				),
				ViewMenuBuilder.menu( "Settings",
						item( BigDataViewerActionsMamut.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActionsMamut.VISIBILITY_AND_GROUPING )
				)
		);
		appModel.getPlugins().addMenus( menu );

		frame.setVisible( true );

		final AffineTransform3D resetTransform = InitializeViewerState.initTransform( ov.getWidth(), ov.getHeight(), false, viewer.getState() );
		viewer.getTransformEventHandler().setTransform( resetTransform );
		viewActions.runnableAction( () -> {
			viewer.getTransformEventHandler().setTransform( resetTransform );
		}, "reset transform", "R" );

		viewer.setTimepoint( timepointModel.getTimepoint() );

		viewer.getTransformEventHandler().install( viewBehaviours );

		viewer.addTimePointListener( timePointIndex -> timepointModel.setTimepoint( timePointIndex ) );
		timepointModel.listeners().add( () -> viewer.setTimepoint( timepointModel.getTimepoint() ) );

		final Model model = appModel.getModel();
		final ModelGraph modelGraph = model.getGraph();

		modelGraph.addGraphChangeListener( viewer::requestRepaint );
		selectionModel.listeners().add( viewer::requestRepaint );
		highlightModel.listeners().add( viewer::requestRepaint );
	}

	public BrightnessDialog getBrightnessDialog()
	{
		return brightnessDialog;
	}

	public VisibilityAndGroupingDialog getVisibilityAndGroupingDialog()
	{
		return visibilityAndGroupingDialog;
	}

	public void requestRepaint()
	{
		viewer.requestRepaint();
	}
}
