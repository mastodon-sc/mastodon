package org.mastodon.revised.mamut;

import bdv.tools.InitializeViewerState;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.WindowConstants;
import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.AutoNavigateFocusModel;
import org.mastodon.revised.bdv.BdvContextProvider;
import org.mastodon.revised.bdv.BigDataViewerActionsMamut;
import org.mastodon.revised.bdv.BigDataViewerMamut;
import org.mastodon.revised.bdv.NavigationActionsMamut;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.ViewerFrameMamut;
import org.mastodon.revised.bdv.ViewerPanelMamut;
import org.mastodon.revised.bdv.overlay.BdvHighlightHandler;
import org.mastodon.revised.bdv.overlay.BdvSelectionBehaviours;
import org.mastodon.revised.bdv.overlay.EditBehaviours;
import org.mastodon.revised.bdv.overlay.EditSpecialBehaviours;
import org.mastodon.revised.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.revised.bdv.overlay.OverlayNavigation;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.RenderSettings.UpdateListener;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.bvv.wrap.BvvEdgeWrapper;
import org.mastodon.revised.bvv.wrap.BvvGraphWrapper;
import org.mastodon.revised.bvv.wrap.BvvVertexWrapper;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.FocusActions;
import org.mastodon.revised.ui.HighlightBehaviours;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.views.context.ContextProvider;
import org.scijava.ui.behaviour.util.InputActionBindings;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;

public class MamutViewBvv extends MamutView< BvvGraphWrapper< Spot, Link >, BvvVertexWrapper< Spot, Link >, BvvEdgeWrapper< Spot, Link > >
{
	// TODO
	private static int bvvName = 1;

	private final SharedBigDataViewerData sharedBdvData;

	static class BvvViewFrame extends ViewFrame
	{
		public BvvViewFrame(
				final String windowTitle,
				final GroupHandle groupHandle )
		{
			super( windowTitle );

			final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
			settingsPanel.add( navigationLocksPanel );
			settingsPanel.add( Box.createHorizontalGlue() );

			pack();
			setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
			addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final WindowEvent e )
				{
					System.out.println( "TODO: BvvViewFrame.windowClosing" );
//					trackschemePanel.stop();
				}
			} );
		}

		public InputActionBindings getKeybindings()
		{
			return keybindings;
		}
	}

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

		sharedBdvData = appModel.getSharedBdvData();

		final String windowTitle = "BigVolumeViewer " + ( bvvName++ ); // TODO: use JY naming scheme

		BvvViewFrame frame = new BvvViewFrame( windowTitle, groupHandle );
		setFrame( frame );

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
