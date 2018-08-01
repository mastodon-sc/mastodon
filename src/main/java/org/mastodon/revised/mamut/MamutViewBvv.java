package org.mastodon.revised.mamut;

import javax.swing.ActionMap;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bvv.BvvOptions;
import org.mastodon.revised.bvv.BvvViewFrame;
import org.mastodon.revised.bvv.wrap.BvvEdgeWrapper;
import org.mastodon.revised.bvv.wrap.BvvGraphWrapper;
import org.mastodon.revised.bvv.wrap.BvvVertexWrapper;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.SelectionActions;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;

public class MamutViewBvv extends MamutView< BvvGraphWrapper< Spot, Link >, BvvVertexWrapper< Spot, Link >, BvvEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bvvName = 1;

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

		final String windowTitle = "BigVolumeViewer " + ( bvvName++ ); // TODO: use JY naming scheme
		BvvViewFrame frame = new BvvViewFrame(
				windowTitle,
				viewGraph,
				shared.getSources(),
				shared.getNumTimepoints(),
				shared.getCache(),
				groupHandle,
				shared.getOptions(),
				BvvOptions.options() );
		setFrame( frame );

		frame.getBvvPanel().getTransformEventHandler().install( viewBehaviours );

		MastodonFrameViewActions.install( viewActions, this );

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
	}
}
