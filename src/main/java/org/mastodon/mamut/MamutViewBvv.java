package org.mastodon.mamut;

import bdv.BigDataViewerActions;
import javax.swing.ActionMap;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.mamut.model.MamutModelGraphPropertiesBvv;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bvv.BvvOptions;
import org.mastodon.views.bvv.BvvPanel;
import org.mastodon.views.bvv.BvvViewFrame;
import org.mastodon.views.bvv.wrap.BvvEdgeWrapper;
import org.mastodon.views.bvv.wrap.BvvGraphWrapper;
import org.mastodon.views.bvv.wrap.BvvVertexWrapper;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.SelectionActions;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.mamut.MamutMenuBuilder.viewMenu;

public class MamutViewBvv extends MamutView< BvvGraphWrapper< Spot, Link >, BvvVertexWrapper< Spot, Link >, BvvEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bvvName = 1;

	private final BvvPanel bvvPanel;

	public MamutViewBvv( final MamutAppModel appModel )
	{
		super( appModel,
				new BvvGraphWrapper<>(
						appModel.getModel().getGraph(),
						appModel.getModel().getGraphIdBimap(),
						appModel.getModel().getSpatioTemporalIndex(),
						appModel.getModel().getGraph().getLock(),
						new MamutModelGraphPropertiesBvv( appModel.getModel().getGraph(), appModel.getRadiusStats() ) ),
				new String[] { KeyConfigContexts.BIGVOLUMEVIEWER } );

		final SharedBigDataViewerData shared = appModel.getSharedBdvData();

		final String windowTitle = "BigVolumeViewer " + ( bvvName++ );
		BvvViewFrame frame = new BvvViewFrame(
				windowTitle,
				viewGraph,
				selectionModel,
				highlightModel,
				shared.getSources(),
				shared.getNumTimepoints(),
				shared.getCache(),
				groupHandle,
				shared.getOptions(),
				BvvOptions.options() );
		setFrame( frame );
		bvvPanel = frame.getBvvPanel();

		frame.getBvvPanel().getTransformEventHandler().install( viewBehaviours );

		MastodonFrameViewActions.install( viewActions, this );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						separator(),
						item( BigDataViewerActions.LOAD_SETTINGS ),
						item( BigDataViewerActions.SAVE_SETTINGS )
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
						item( BigDataViewerActions.BRIGHTNESS_SETTINGS ),
						item( BigDataViewerActions.VISIBILITY_AND_GROUPING )
				)
		);
		appModel.getPlugins().addMenus( menu );

		final Model model = appModel.getModel();
		final ModelGraph modelGraph = model.getGraph();

		modelGraph.addGraphChangeListener( bvvPanel::requestRepaint );
		selectionModel.listeners().add( bvvPanel::requestRepaint );
		highlightModel.listeners().add( bvvPanel::requestRepaint );

		frame.setVisible( true );
	}
}
