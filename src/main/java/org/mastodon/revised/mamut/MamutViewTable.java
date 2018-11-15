package org.mastodon.revised.mamut;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.separator;
import static org.mastodon.revised.mamut.MamutMenuBuilder.editMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.fileMenu;
import static org.mastodon.revised.mamut.MamutMenuBuilder.viewMenu;

import java.util.function.Function;

import javax.swing.ActionMap;

import org.mastodon.app.IdentityViewGraph;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameViewActions;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.app.ui.ViewMenu;
import org.mastodon.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.table.TableViewActions;
import org.mastodon.revised.table.TableViewFrame;
import org.mastodon.revised.ui.SelectionActions;
import org.mastodon.views.context.ContextChooser;

public class MamutViewTable extends MamutView< ViewGraph< Spot, Link, Spot, Link >, Spot, Link >
{

	private static final String[] CONTEXTS = new String[] { KeyConfigContexts.TABLE };

	public MamutViewTable( final MamutAppModel appModel )

	{
		super( appModel, IdentityViewGraph.wrap( appModel.getModel().getGraph(), appModel.getModel().getGraphIdBimap() ), CONTEXTS );

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
				appModel.getModel() );
		setFrame( frame );

		final Model model = appModel.getModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();

		focusModel.listeners().add( frame );
		highlightModel.listeners().add( frame );
		featureModel.listeners().add( frame );
		tagSetModel.listeners().add( frame );

		MastodonFrameViewActions.install( viewActions, this );
		TableViewActions.install( viewActions, frame );

		onClose( () -> {
			focusModel.listeners().remove( frame );
			highlightModel.listeners().remove( frame );
			featureModel.listeners().remove( frame );
			tagSetModel.listeners().remove( frame );
		} );

		final ViewMenu menu = new ViewMenu( this );
		final ActionMap actionMap = frame.getKeybindings().getConcatenatedActionMap();

		MamutMenuBuilder.build( menu, actionMap,
				fileMenu(
						item( TableViewActions.EXPORT_TO_CSV ),
						separator()
						)
				);
		MainWindow.addMenus( menu, actionMap );
		MamutMenuBuilder.build( menu, actionMap,
				viewMenu(
						item( MastodonFrameViewActions.TOGGLE_SETTINGS_PANEL )
				),
				editMenu(
						item( TableViewActions.EDIT_LABEL ),
						item( TableViewActions.TOGGLE_TAG ),
						separator(),
						item( SelectionActions.DELETE_SELECTION )
				)
		);
		appModel.getPlugins().addMenus( menu );

		frame.setSize( 400, 400 );
		frame.setVisible( true );
	}

	@Override
	public TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > getFrame()
	{
		final ViewFrame f = super.getFrame();
		@SuppressWarnings( "unchecked" )
		final TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > vf = (org.mastodon.revised.table.TableViewFrame< MamutAppModel, ViewGraph< Spot, Link, Spot, Link >, Spot, Link > ) f;
		return vf;
	}

	public ContextChooser< Spot > getContextChooser()
	{
		return getFrame().getContextChooser();
	}
}
